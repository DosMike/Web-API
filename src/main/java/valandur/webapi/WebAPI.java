package valandur.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.sentry.Sentry;
import io.sentry.context.Context;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.bstats.sponge.Metrics;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.ExpireEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.user.BanUserEvent;
import org.spongepowered.api.event.world.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;
import valandur.webapi.api.block.IBlockService;
import valandur.webapi.api.cache.ICacheService;
import valandur.webapi.api.extension.IExtensionService;
import valandur.webapi.api.hook.IWebHookService;
import valandur.webapi.api.serialize.ISerializeService;
import valandur.webapi.api.message.IMessageService;
import valandur.webapi.api.permission.IPermissionService;
import valandur.webapi.api.server.IServerService;
import valandur.webapi.api.servlet.IServletService;
import valandur.webapi.api.servlet.BaseServlet;
import valandur.webapi.block.BlockOperation;
import valandur.webapi.block.BlockOperationStatusChangeEvent;
import valandur.webapi.block.BlockService;
import valandur.webapi.cache.CacheService;
import valandur.webapi.cache.chat.CachedChatMessage;
import valandur.webapi.cache.command.CachedCommandCall;
import valandur.webapi.command.CommandRegistry;
import valandur.webapi.command.CommandSource;
import valandur.webapi.extension.ExtensionService;
import valandur.webapi.hook.WebHook;
import valandur.webapi.hook.WebHookSerializer;
import valandur.webapi.hook.WebHookService;
import valandur.webapi.integration.huskycrates.HuskyCratesServlet;
import valandur.webapi.integration.mmctickets.MMCTicketsServlet;
import valandur.webapi.integration.nucleus.NucleusServlet;
import valandur.webapi.integration.universalmarket.UniversalMarketServlet;
import valandur.webapi.integration.webbooks.WebBookServlet;
import valandur.webapi.serialize.SerializeService;
import valandur.webapi.message.MessageService;
import valandur.webapi.permission.PermissionService;
import valandur.webapi.server.ServerService;
import valandur.webapi.servlet.*;
import valandur.webapi.servlet.base.ApiServlet;
import valandur.webapi.servlet.base.ServletService;
import valandur.webapi.servlet.handler.AssetHandler;
import valandur.webapi.servlet.handler.AuthHandler;
import valandur.webapi.servlet.handler.ErrorHandler;
import valandur.webapi.servlet.handler.RateLimitHandler;
import valandur.webapi.user.UserPermission;
import valandur.webapi.user.UserPermissionConfigSerializer;
import valandur.webapi.user.Users;
import valandur.webapi.util.Constants;
import valandur.webapi.util.JettyLogger;
import valandur.webapi.util.Timings;
import valandur.webapi.util.Util;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Plugin(
        id = Constants.ID,
        version = Constants.VERSION,
        name = Constants.NAME,
        url = Constants.URL,
        description = Constants.DESCRIPTION,
        authors = {
                "Valandur"
        }
)
public class WebAPI {

    private static WebAPI instance;
    public static WebAPI getInstance() {
        return WebAPI.instance;
    }

    private static SpongeExecutorService syncExecutor;
    private static SpongeExecutorService asyncExecutor;

    private boolean devMode = false;
    public static boolean isDevMode() {
        return WebAPI.getInstance().devMode;
    }

    private boolean adminPanelEnabled = true;
    public static boolean isAdminPanelEnabled() {
        return WebAPI.getInstance().adminPanelEnabled;
    }

    private static String spongeApi;
    private static String spongeGame;
    private static String spongeImpl;
    private static String pluginList;

    private static String serverHost;
    private static Integer serverPortHttp;
    private static Integer serverPortHttps;
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyStoreMgrPassword;
    private Server server;

    @Inject
    private Metrics metrics;

    @Inject
    private Logger logger;
    public static Logger getLogger() {
        return WebAPI.getInstance().logger;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configPath;
    public static Path getConfigPath() {
        return WebAPI.getInstance().configPath;
    }

    @Inject
    private PluginContainer container;
    public static PluginContainer getContainer() {
        return WebAPI.getInstance().container;
    }

    private boolean reportErrors;
    public static boolean reportErrors() {
        return WebAPI.getInstance() == null || WebAPI.getInstance().reportErrors;
    }

    private AuthHandler authHandler;
    public static AuthHandler getAuthHandler() {
        return WebAPI.getInstance().authHandler;
    }

    // Services
    private BlockService blockService;
    public static BlockService getBlockService() {
        return WebAPI.getInstance().blockService;
    }

    private CacheService cacheService;
    public static CacheService getCacheService() {
        return WebAPI.getInstance().cacheService;
    }

    private ExtensionService extensionService;
    public static ExtensionService getExtensionService() {
        return WebAPI.getInstance().extensionService;
    }

    private SerializeService serializeService;
    public static SerializeService getSerializeService() {
        return WebAPI.getInstance().serializeService;
    }

    private MessageService messageService;
    public static MessageService getMessageService() {
        return WebAPI.getInstance().messageService;
    }

    private PermissionService permissionService;
    public static PermissionService getPermissionService() {
        return WebAPI.getInstance().permissionService;
    }

    private ServerService serverService;
    public static ServerService getServerService() {
        return WebAPI.getInstance().serverService;
    }

    private ServletService servletService;
    public static ServletService getServletService() {
        return WebAPI.getInstance().servletService;
    }

    private WebHookService webHookService;
    public static WebHookService getWebHookService() {
        return WebAPI.getInstance().webHookService;
    }


    public WebAPI() {
        System.setProperty("sentry.dsn", "https://fb64795d2a5c4ff18f3c3e4117d7c245:53cf4ea85ae44608ab5b189f0c07b3f1@sentry.io/203545");
        System.setProperty("sentry.release", Constants.VERSION.split("-")[0]);
        System.setProperty("sentry.maxmessagelength", "2000");
        System.setProperty("sentry.stacktrace.app.packages", WebAPI.class.getPackage().getName());

        Sentry.init();

        // Add our own jar to the system classloader classpath,
        // because some external libraries don't work otherwise.
        try {
            CodeSource src = WebAPI.class.getProtectionDomain().getCodeSource();
            if (src == null) {
                throw new IOException("Could not get code source!");
            }

            URL jar = src.getLocation();
            String str = jar.toString();
            if (str.indexOf("!") > 0) {
                jar = new URL(jar.toString().substring(0, str.indexOf("!") + 2));
            }
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;

            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, jar);
        } catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        WebAPI.instance = this;

        Timings.STARTUP.startTiming();

        Platform platform = Sponge.getPlatform();
        spongeApi = platform.getContainer(Component.API).getVersion().orElse(null);
        spongeGame = platform.getContainer(Component.GAME).getVersion().orElse(null);
        spongeImpl = platform.getContainer(Component.IMPLEMENTATION).getVersion().orElse(null);
        pluginList = Sponge.getPluginManager().getPlugins().stream()
                .map(PluginContainer::getId).collect(Collectors.joining(","));

        // Create our config directory if it doesn't exist
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath);
            } catch (IOException e) {
                e.printStackTrace();
                if (WebAPI.reportErrors()) WebAPI.sentryCapture(e);
            }
        }

        // Reusable sync executor to run code on main server thread
        syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);

        // Register custom serializers
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(WebHook.class), new WebHookSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(UserPermission.class), new UserPermissionConfigSerializer());

        // Setup services
        this.blockService = new BlockService();
        this.cacheService = new CacheService();
        this.extensionService = new ExtensionService();
        this.serializeService = new SerializeService();
        this.messageService = new MessageService();
        this.permissionService = new PermissionService();
        this.serverService = new ServerService();
        this.servletService = new ServletService();
        this.webHookService = new WebHookService();


        // Register services
        Sponge.getServiceManager().setProvider(this, IBlockService.class, blockService);
        Sponge.getServiceManager().setProvider(this, ICacheService.class, cacheService);
        Sponge.getServiceManager().setProvider(this, IExtensionService.class, extensionService);
        Sponge.getServiceManager().setProvider(this, ISerializeService.class, serializeService);
        Sponge.getServiceManager().setProvider(this, IMessageService.class, messageService);
        Sponge.getServiceManager().setProvider(this, IPermissionService.class, permissionService);
        Sponge.getServiceManager().setProvider(this, IServerService.class, serverService);
        Sponge.getServiceManager().setProvider(this, IServletService.class, servletService);
        Sponge.getServiceManager().setProvider(this, IWebHookService.class, webHookService);

        Timings.STARTUP.stopTiming();
    }
    @Listener
    public void onInitialization(GameInitializationEvent event) {
        Timings.STARTUP.startTiming();

        logger.info(Constants.NAME + " v" + Constants.VERSION + " is starting...");

        logger.info("Setting up jetty logger...");
        Log.setLog(new JettyLogger());

        // Create permission handler
        authHandler = new AuthHandler();

        // Main init function, that is also called when reloading the plugin
        init(null);

        logger.info("Registering servlets...");
        servletService.registerServlet(BlockServlet.class);
        servletService.registerServlet(CmdServlet.class);
        servletService.registerServlet(EntityServlet.class);
        servletService.registerServlet(HistoryServlet.class);
        servletService.registerServlet(InfoServlet.class);
        servletService.registerServlet(MapServlet.class);
        servletService.registerServlet(MessageServlet.class);
        servletService.registerServlet(PlayerServlet.class);
        servletService.registerServlet(PluginServlet.class);
        servletService.registerServlet(RecipeServlet.class);
        servletService.registerServlet(RegistryServlet.class);
        servletService.registerServlet(ServletServlet.class);
        servletService.registerServlet(TileEntityServlet.class);
        servletService.registerServlet(UserServlet.class);
        servletService.registerServlet(WorldServlet.class);

        // Other plugin integrations
        try {
            Class.forName("com.codehusky.huskycrates.HuskyCrates");
            logger.info("  Integrating with HuskyCrates...");
            servletService.registerServlet(HuskyCratesServlet.class);
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("net.moddedminecraft.mmctickets.Main");
            logger.info("  Integrating with MMCTickets...");
            servletService.registerServlet(MMCTicketsServlet.class);
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("io.github.nucleuspowered.nucleus.api.NucleusAPI");
            logger.info("  Integrating with Nucleus...");
            servletService.registerServlet(NucleusServlet.class);
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("de.dosmike.sponge.WebBooks.WebBooks");
            logger.info("  Integrating with WebBooks...");
            servletService.registerServlet(WebBookServlet.class);
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("com.xwaffle.universalmarket.UniversalMarket");
            logger.info("  Integrating with UniversalMarket...");
            servletService.registerServlet(UniversalMarketServlet.class);
        } catch (ClassNotFoundException ignored) { }

        logger.info(Constants.NAME + " ready");

        Timings.STARTUP.stopTiming();
    }
    @Listener(order = Order.POST)
    public void onPostInitialization(GamePostInitializationEvent event) {
        Timings.STARTUP.startTiming();

        // Load base data
        cacheService.updateWorlds();
        cacheService.updatePlugins();
        cacheService.updateCommands();

        Timings.STARTUP.stopTiming();
    }

    private void init(Player triggeringPlayer) {
        Timings.STARTUP.startTiming();

        logger.info("Loading configuration...");

        Tuple<ConfigurationLoader, ConfigurationNode> tup = Util.loadWithDefaults("config.conf", "defaults/config.conf");
        ConfigurationNode config = tup.getSecond();

        // Save important config values to variables
        devMode = config.getNode("devMode").getBoolean();
        reportErrors = config.getNode("reportErrors").getBoolean();
        serverHost = config.getNode("host").getString();
        serverPortHttp = config.getNode("http").getInt(-1);
        serverPortHttps = config.getNode("https").getInt(-1);
        adminPanelEnabled = config.getNode("adminPanel").getBoolean();
        keyStoreLocation = config.getNode("customKeyStore").getString();
        keyStorePassword = config.getNode("customKeyStorePassword").getString();
        keyStoreMgrPassword = config.getNode("customKeyStoreManagerPassword").getString();
        CmdServlet.CMD_WAIT_TIME = config.getNode("cmdWaitTime").getInt();

        if (devMode) {
            logger.warn("Web-API IS RUNNING IN DEV MODE. USING NON-SHADOWED REFERENCES! ERROR REPORTING IS OFF!");
            reportErrors = false;
        }

        // Use the container instead of VERSION so the compiler doesn't remove it because it's constant
        if (this.container.getVersion().orElse("").equalsIgnoreCase("@version@")) {
            logger.warn("Web-API VERSION SIGNALS DEV MODE. ERROR REPORTING IS OFF!");
            reportErrors = false;
        }

        authHandler.init();

        blockService.init(config);

        extensionService.init();

        cacheService.init();

        webHookService.init();

        serializeService.init();

        serverService.init();

        CommandRegistry.init();

        Users.init();

        if (triggeringPlayer != null) {
            triggeringPlayer.sendMessage(Text.builder().color(TextColors.AQUA)
                    .append(Text.of("[" + Constants.NAME + "] " + Constants.NAME + " has been reloaded!"))
                    .build());
        }

        Timings.STARTUP.stopTiming();
    }
    private void startWebServer(Player player) {
        // Start web server
        logger.info("Starting Web Server...");

        asyncExecutor.execute(() -> {
            try {
                server = new Server();

                // HTTP config
                HttpConfiguration httpConfig = new HttpConfiguration();
                httpConfig.setOutputBufferSize(32768);

                String baseUri = null;

                // HTTP
                if (serverPortHttp >= 0) {
                    if (serverPortHttp < 1024) {
                        logger.warn("You are using an HTTP port < 1024 which is not recommended! \n" +
                                "This might cause errors when not running the server as root/admin. \n" +
                                "Running the server as root/admin is not recommended. " +
                                "Please use a port above 1024 for HTTP."
                        );
                    }
                    ServerConnector httpConn = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
                    httpConn.setHost(serverHost);
                    httpConn.setPort(serverPortHttp);
                    httpConn.setIdleTimeout(30000);
                    server.addConnector(httpConn);

                    baseUri = "http://" + serverHost + ":" + serverPortHttp;
                }

                // HTTPS
                if (serverPortHttps >= 0) {
                    if (serverPortHttps < 1024) {
                        logger.warn("You are using an HTTPS port < 1024 which is not recommended! \n" +
                                "This might cause errors when not running the server as root/admin. \n" +
                                "Running the server as root/admin is not recommended. " +
                                "Please use a port above 1024 for HTTPS."
                        );
                    }

                    // Update http config
                    httpConfig.setSecureScheme("https");
                    httpConfig.setSecurePort(serverPortHttps);

                    String loc = keyStoreLocation;
                    String pw = keyStorePassword;
                    String mgrPw = keyStoreMgrPassword;
                    if (loc == null || loc.isEmpty()) {
                        loc = Sponge.getAssetManager().getAsset(WebAPI.getInstance(), "keystore.jks")
                                .map(a -> a.getUrl().toString()).orElse("");
                        pw = "mX4z%&uJ2E6VN#5f";
                        mgrPw = "mX4z%&uJ2E6VN#5f";
                    }

                    // SSL Factory
                    SslContextFactory sslFactory = new SslContextFactory();
                    sslFactory.setKeyStorePath(loc);
                    sslFactory.setKeyStorePassword(pw);
                    sslFactory.setKeyManagerPassword(mgrPw);

                    // HTTPS config
                    HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
                    SecureRequestCustomizer src = new SecureRequestCustomizer();
                    src.setStsMaxAge(2000);
                    src.setStsIncludeSubDomains(true);
                    httpsConfig.addCustomizer(src);


                    ServerConnector httpsConn = new ServerConnector(server,
                            new SslConnectionFactory(sslFactory, HttpVersion.HTTP_1_1.asString()),
                            new HttpConnectionFactory(httpsConfig)
                    );
                    httpsConn.setHost(serverHost);
                    httpsConn.setPort(serverPortHttps);
                    httpsConn.setIdleTimeout(30000);
                    server.addConnector(httpsConn);

                    baseUri = "https://" + serverHost + ":" + serverPortHttps;
                }

                if (baseUri == null) {
                    logger.error("You have disabled both HTTP and HTTPS - The WebAPI will be unreachable!");
                }

                // Add error handler
                server.addBean(new ErrorHandler(server));

                // Collection of all handlers
                List<Handler> mainHandlers = new LinkedList<>();

                // Asset handlers
                mainHandlers.add(newContext("/docs", new AssetHandler("pages/redoc.html")));
                mainHandlers.add(newContext("/swagger", new AssetHandler("swagger", (path) -> {
                    if (!path.endsWith("swagger/index.yaml"))
                        return null;
                    return (data) -> {
                        String text = new String(data);
                        text = text.replaceFirst("<host>", serverHost + ":" + serverPortHttp);
                        text = text.replaceFirst("<version>", Constants.VERSION);
                        return text.getBytes();
                    };
                })));

                if (adminPanelEnabled) {
                    // Rewrite handler
                    RewriteHandler rewrite = new RewriteHandler();
                    rewrite.setRewriteRequestURI(true);
                    rewrite.setRewritePathInfo(true);

                    RedirectPatternRule redirect = new RedirectPatternRule();
                    redirect.setPattern("/*");
                    redirect.setLocation("/admin");
                    rewrite.addRule(redirect);
                    mainHandlers.add(newContext("/", rewrite));

                    mainHandlers.add(newContext("/admin", new AssetHandler("admin")));
                }

                // Setup all servlets
                servletService.init();

                // Main servlet context
                ServletContextHandler servletsContext = new ServletContextHandler();
                servletsContext.addServlet(ApiServlet.class, "/*");

                // Use a list to make requests first go through the auth handler and rate-limit handler
                HandlerList list = new HandlerList();
                list.setHandlers(new Handler[]{ authHandler, new RateLimitHandler(), servletsContext });
                mainHandlers.add(newContext("/api", list));

                // Add collection of handlers to server
                ContextHandlerCollection coll = new ContextHandlerCollection();
                coll.setHandlers(mainHandlers.toArray(new Handler[mainHandlers.size()]));
                server.setHandler(coll);

                server.start();

                logger.info("AdminPanel: " + baseUri + "/admin");
                logger.info("API Docs: " + baseUri + "/docs");
            } catch (SocketException e) {
                logger.error("Web-API webserver could not start, probably because one of the ports needed for HTTP " +
                        "and/or HTTPS are in use or not accessible (ports below 1024 are protected)");
            } catch (MultiException e) {
                e.getThrowables().forEach(t -> {
                    if (t instanceof SocketException) {
                        logger.error("Web-API webserver could not start, probably because one of the ports needed for HTTP " +
                                "and/or HTTPS are in use or not accessible (ports below 1024 are protected)");
                    } else {
                        t.printStackTrace();
                        WebAPI.sentryCapture(t);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                WebAPI.sentryCapture(e);
            }

            if (player != null) {
                player.sendMessage(Text.builder()
                        .color(TextColors.AQUA)
                        .append(Text.of("[" + Constants.NAME + "] The web server has been restarted!"))
                        .toText()
                );
            }
        });
    }
    private void stopWebServer() {
        if (server != null) {
            try {
                server.stop();
                server = null;
            } catch (Exception e) {
                e.printStackTrace();
                if (WebAPI.reportErrors()) WebAPI.sentryCapture(e);
            }
        }
    }

    private void checkForUpdates() {
        if (devMode) {
            logger.warn("SKIPPING UPDATE CHECK IN DEV MODE");
            return;
        }

        asyncExecutor.execute(() -> {
            HttpsURLConnection connection = null;
            try {
                java.net.URL url = new URL(Constants.UPDATE_URL);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Web-API");
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches(false);

                //Get Response
                int code = connection.getResponseCode();
                if (code != 200) {
                    logger.warn("Could not check for updates: " + code);
                    return;
                }

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                String respString = response.toString().trim();
                if (respString.isEmpty() || respString.equalsIgnoreCase("OK")) {
                    logger.warn("Empty response received when checking for updates");
                    return;
                }

                ObjectMapper map = new ObjectMapper();
                JsonNode resp = map.readTree(respString);

                String version = container.getVersion().orElse("").split("-")[0];
                String newVersion = resp.get("tag_name").asText().substring(1).split("-")[0];

                if (newVersion.equalsIgnoreCase(version)) {
                    return;
                }

                String[] changes = resp.get("body").asText().split("\n");

                logger.warn("------- Web-API Update -------");
                logger.warn("Latest: " + newVersion);
                logger.warn("Current: " + version);
                logger.warn("---------- Changes -----------");
                for (String change : changes)
                    logger.warn(change);
                logger.warn("------------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private ContextHandler newContext(String path, Handler handler) {
        ContextHandler context = new ContextHandler();
        context.setContextPath(path);
        context.setHandler(handler);
        return context;
    }

    // Event listeners
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        startWebServer(null);

        checkForUpdates();

        // Add custom bstats metrics
        metrics.addCustomChart(new Metrics.DrilldownPie("plugin_version", () -> {
            String[] vers = Constants.VERSION.split("-");

            Map<String, Integer> entry = new HashMap<>();
            entry.put(vers[1], 1);

            Map<String, Map<String, Integer>> map = new HashMap<>();
            map.put(vers[0], entry);

            return map;
        }));
        metrics.addCustomChart(new Metrics.SimplePie("report_errors", () -> reportErrors ? "Yes" : "No"));
        metrics.addCustomChart(new Metrics.SimplePie("admin_panel", () -> adminPanelEnabled ? "Yes" : "No"));
        metrics.addCustomChart(new Metrics.SimpleBarChart("protocols", () -> {
            Map<String, Integer> map = new HashMap<>();
            map.put("HTTP", serverPortHttp >= 0 ? 1 : 0);
            map.put("HTTPS", serverPortHttps >= 0 ? 1 : 0);
            return map;
        }));
        metrics.addCustomChart(new Metrics.SimpleBarChart("servlets", () -> {
            Map<String, Integer> map = new HashMap<>();
            Collection<Class<? extends BaseServlet>> servlets = servletService.getLoadedServlets().values();
            for (Class<? extends BaseServlet> servlet : servlets) {
                map.put(servlet.getClass().getName(), 1);
            }
            return map;
        }));

        webHookService.notifyHooks(WebHookService.WebHookType.SERVER_START, event);
    }
    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.SERVER_STOP, event);

        stopWebServer();
    }
    @Listener
    public void onReload(GameReloadEvent event) {
        Optional<Player> p = event.getCause().first(Player.class);

        logger.info("Reloading " + Constants.NAME + " v" + Constants.VERSION + "...");

        cacheService.updatePlugins();
        cacheService.updateCommands();

        stopWebServer();

        init(p.orElse(null));

        startWebServer(p.orElse(null));

        checkForUpdates();

        logger.info("Reloaded " + Constants.NAME);
    }

    @Listener(order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event) {
        cacheService.updateWorld(event.getTargetWorld());

        webHookService.notifyHooks(WebHookService.WebHookType.WORLD_LOAD, event);
    }
    @Listener(order = Order.POST)
    public void onWorldUnload(UnloadWorldEvent event) {
        cacheService.updateWorld(event.getTargetWorld().getProperties());

        webHookService.notifyHooks(WebHookService.WebHookType.WORLD_UNLOAD, event);
    }
    @Listener(order = Order.POST)
    public void onWorldSave(SaveWorldEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.WORLD_SAVE, event);
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        cacheService.updatePlayer(event.getTargetEntity());

        webHookService.notifyHooks(WebHookService.WebHookType.PLAYER_JOIN, event);
    }
    @Listener(order = Order.POST)
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        // Send the message first because the player is removed from cache afterwards
        webHookService.notifyHooks(WebHookService.WebHookType.PLAYER_LEAVE, event);

        cacheService.removePlayer(event.getTargetEntity().getUniqueId());
    }

    @Listener(order = Order.POST)
    public void onUserKick(KickPlayerEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.PLAYER_KICK, event);
    }
    @Listener(order = Order.POST)
    public void onUserBan(BanUserEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.PLAYER_BAN, event);
    }

    @Listener(order = Order.POST)
    public void onEntitySpawn(SpawnEntityEvent event) {
        for (Entity entity : event.getEntities()) {
            cacheService.updateEntity(entity);
        }
    }
    @Listener(order = Order.POST)
    public void onEntityDespawn(DestructEntityEvent event) {
        cacheService.removeEntity(event.getTargetEntity().getUniqueId());

        Entity ent = event.getTargetEntity();
        if (ent instanceof Player) {
            webHookService.notifyHooks(WebHookService.WebHookType.PLAYER_DEATH, event);
        }
    }
    @Listener(order = Order.POST)
    public void onEntityExpire(ExpireEntityEvent event) {
        cacheService.removeEntity(event.getTargetEntity().getUniqueId());
    }

    @Listener(order = Order.POST)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        CachedChatMessage msg = cacheService.addChatMessage(player, event);
        webHookService.notifyHooks(WebHookService.WebHookType.CHAT, msg);
    }

    @Listener(order = Order.POST)
    public void onInteractBlock(InteractBlockEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.INTERACT_BLOCK, event);
    }
    @Listener(order = Order.POST)
    public void onInteractInventory(InteractInventoryEvent.Open event) {
        webHookService.notifyHooks(WebHookService.WebHookType.INVENTORY_OPEN, event);
    }
    @Listener(order = Order.POST)
    public void onInteractInventory(InteractInventoryEvent.Close event) {
        webHookService.notifyHooks(WebHookService.WebHookType.INVENTORY_CLOSE, event);
    }

    @Listener(order = Order.POST)
    public void onPlayerAchievement(GrantAchievementEvent.TargetPlayer event) {
        Player player = event.getTargetEntity();

        // Check if we already have the achievement
        if (player.getAchievementData().achievements().get().stream().anyMatch(a -> a.getId().equals(event.getAchievement().getId())))
            return;

        webHookService.notifyHooks(WebHookService.WebHookType.ACHIEVEMENT, event);
    }

    @Listener(order = Order.POST)
    public void onGenerateChunk(GenerateChunkEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.GENERATE_CHUNK, event);
    }

    @Listener(order = Order.POST)
    public void onExplosion(ExplosionEvent event) {
        webHookService.notifyHooks(WebHookService.WebHookType.EXPLOSION, event);
    }

    @Listener(order = Order.POST)
    public void onCommand(SendCommandEvent event) {
        CachedCommandCall call = cacheService.addCommandCall(event);
        webHookService.notifyHooks(WebHookService.WebHookType.COMMAND, call);
    }

    @Listener(order = Order.POST)
    public void onBlockUpdateStatusChange(BlockOperationStatusChangeEvent event) {
        BlockOperation on = event.getBlockOperation();
        switch (on.getStatus()) {
            case DONE:
                logger.info("Block op " + on.getUUID() + " is done");
                break;

            case ERRORED:
                logger.warn("Block op " + on.getUUID() + " failed: " + on.getError());
                break;

            case CANCELED:
                logger.info("Block op " + on.getUUID() + " was canceled");
                break;

            case PAUSED:
                logger.info("Block op " + on.getUUID() + " paused");
                break;

            case RUNNING:
                logger.info("Block op " + on.getUUID() + " started");
                break;
        }

        webHookService.notifyHooks(WebHookService.WebHookType.BLOCK_OPERATION_STATUS, event);
    }

    // Execute a command
    public static CommandResult executeCommand(String command, CommandSource source) {
        CommandManager cmdManager = Sponge.getGame().getCommandManager();
        return cmdManager.process(source, command);
    }

    // Run functions on the main server thread
    public static void runOnMain(Runnable runnable) {
        if (Sponge.getServer().isMainThread()) {
            runnable.run();
        } else {
            CompletableFuture future = CompletableFuture.runAsync(runnable, WebAPI.syncExecutor);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (WebAPI.reportErrors()) WebAPI.sentryCapture(e);
            }
        }
    }
    public static <T> Optional<T> runOnMain(Supplier<T> supplier) {
        if (Sponge.getServer().isMainThread()) {
            Timings.RUN_ON_MAIN.startTiming();
            T obj = supplier.get();
            Timings.RUN_ON_MAIN.stopTiming();
            return obj == null ? Optional.empty() : Optional.of(obj);
        } else {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, WebAPI.syncExecutor);
            try {
                T obj = future.get();
                if (obj == null)
                    return Optional.empty();
                return Optional.of(obj);
            } catch (InterruptedException ignored) {
                return Optional.empty();
            } catch (ExecutionException e) {
                e.printStackTrace();
                if (WebAPI.reportErrors()) WebAPI.sentryCapture(e);
                return Optional.empty();
            }
        }
    }

    // Sentry logging
    public static void sentryNewRequest(HttpServletRequest req) {
        Sentry.clearContext();
        Context context = Sentry.getContext();
        context.addExtra("request_protocol", req.getProtocol());
        context.addExtra("request_method", req.getMethod());
        context.addExtra("request_uri", req.getRequestURI());
    }
    public static void sentryExtra(String name, Object value) {
        Sentry.getContext().addExtra(name, value);
    }

    private static void addDefaultContext() {
        Context context = Sentry.getContext();
        context.addTag("full_release", Constants.VERSION);

        context.addTag("java_version", System.getProperty("java.version"));
        context.addTag("os_name", System.getProperty("os.name"));
        context.addTag("os_arch", System.getProperty("os.arch"));
        context.addTag("os_version", System.getProperty("os.version"));

        context.addExtra("processors", Runtime.getRuntime().availableProcessors());
        context.addExtra("memory_max", Runtime.getRuntime().maxMemory());
        context.addExtra("memory_total", Runtime.getRuntime().totalMemory());
        context.addExtra("memory_free", Runtime.getRuntime().freeMemory());

        context.addExtra("server_host", serverHost);
        context.addExtra("server_port_http", serverPortHttp);
        context.addExtra("server_port_https", serverPortHttps);

        context.addExtra("sponge_api", spongeApi);
        context.addExtra("sponge_game", spongeGame);
        context.addExtra("sponge_impl", spongeImpl);

        context.addExtra("plugins", pluginList);
    }
    public static void sentryCapture(Exception e) {
        addDefaultContext();
        Sentry.capture(e);
    }
    public static void sentryCapture(Throwable t) {
        addDefaultContext();
        Sentry.capture(t);
    }
    public static void sentryCapture(String msg) {
        addDefaultContext();
        Sentry.capture(msg);
    }
}
