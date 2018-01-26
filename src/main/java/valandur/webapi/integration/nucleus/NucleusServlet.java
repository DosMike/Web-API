package valandur.webapi.integration.nucleus;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import org.eclipse.jetty.http.HttpMethod;
import org.spongepowered.api.world.Location;
import valandur.webapi.api.WebAPIAPI;
import valandur.webapi.api.servlet.BaseServlet;
import valandur.webapi.api.servlet.Endpoint;
import valandur.webapi.api.servlet.IServletData;
import valandur.webapi.api.servlet.Servlet;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Servlet(basePath = "nucleus")
public class NucleusServlet extends BaseServlet {

    public static void onRegister() {
        WebAPIAPI.getJsonService().ifPresent(srv -> {
            srv.registerCache(NamedLocation.class, CachedNamedLocation.class);
            srv.registerCache(Kit.class, CachedKit.class);
        });
    }


    @Endpoint(method = HttpMethod.GET, path = "jail", perm = "jail.list")
    public void getJails(IServletData data) {
        Optional<NucleusJailService> optSrv = NucleusAPI.getJailService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues jail service not available");
            return;
        }

        NucleusJailService srv = optSrv.get();

        Optional<List<CachedNamedLocation>> optRes = WebAPIAPI.runOnMain(
                () -> srv.getJails().values().stream().map(CachedNamedLocation::new).collect(Collectors.toList())
        );

        data.addData("ok", optRes.isPresent(), false);
        data.addData("jails", optRes.orElse(null), data.getQueryParam("details").isPresent());
    }

    @Endpoint(method = HttpMethod.GET, path = "jail/:name", perm = "jail.one")
    public void getJail(IServletData data, String name) {
        Optional<NucleusJailService> optSrv = NucleusAPI.getJailService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues jail service not available");
            return;
        }

        NucleusJailService srv = optSrv.get();

        Optional<CachedNamedLocation> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<NamedLocation> optJail = srv.getJail(name);
            if (!optJail.isPresent()) {
                data.sendError(HttpServletResponse.SC_NOT_FOUND, "Jail not found");
                return null;
            }

            return new CachedNamedLocation(optJail.get());
        });

        data.addData("ok", optRes.isPresent(), false);
        data.addData("jail", optRes.orElse(null), true);
    }

    @Endpoint(method = HttpMethod.POST, path = "jail", perm = "jail.create")
    public void createJail(IServletData data) {
        Optional<NucleusJailService> optSrv = NucleusAPI.getJailService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues jail service not available");
            return;
        }

        NucleusJailService srv = optSrv.get();

        Optional<CachedNamedLocation> optReq = data.getRequestBody(CachedNamedLocation.class);
        if (!optReq.isPresent()) {
            data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid jail data: " +
                    data.getLastParseError().getMessage());
            return;
        }

        CachedNamedLocation req = optReq.get();
        if (req.getLocation() == null) {
            data.sendError(HttpServletResponse.SC_BAD_REQUEST, "A location is required");
            return;
        }

        Optional<CachedNamedLocation> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<Location> optLive = req.getLocation().getLive();
            if (!optLive.isPresent()) {
                data.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not get live location");
                return null;
            }
            Vector3d rot = req.getRotation() == null ? Vector3d.FORWARD : req.getRotation();
            srv.setJail(req.getName(), optLive.get(), rot);
            Optional<NamedLocation> optJail = srv.getJail(req.getName());
            return optJail.map(CachedNamedLocation::new).orElse(null);
        });

        data.setStatus(HttpServletResponse.SC_CREATED);
        data.addData("ok", optRes.isPresent(), false);
        data.addData("jail", optRes.orElse(null), true);
    }

    @Endpoint(method = HttpMethod.PUT, path = "jail/:name", perm = "jail.change")
    public void changeJail(IServletData data) {
        data.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not implemented");
    }

    @Endpoint(method = HttpMethod.DELETE, path = "jail/:name", perm = "jail.delete")
    public void deleteJail(IServletData data, String name) {
        Optional<NucleusJailService> optSrv = NucleusAPI.getJailService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues jail service not available");
            return;
        }

        NucleusJailService srv = optSrv.get();

        Optional<CachedNamedLocation> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<NamedLocation> optJail = srv.getJail(name);
            if (!optJail.isPresent()) {
                data.sendError(HttpServletResponse.SC_NOT_FOUND, "Jail not found");
                return null;
            }

            srv.removeJail(name);

            return new CachedNamedLocation(optJail.get());
        });

        data.addData("ok", optRes.isPresent(), false);
        data.addData("jail", optRes.orElse(null), true);
    }


    @Endpoint(method = HttpMethod.GET, path = "kit", perm = "kit.list")
    public void getKits(IServletData data) {
        Optional<NucleusKitService> optSrv = NucleusAPI.getKitService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues kit service not available");
            return;
        }

        NucleusKitService srv = optSrv.get();

        Optional<List<CachedKit>> kits = WebAPIAPI.runOnMain(
                () -> srv.getKitNames().stream()
                        .map(name -> srv.getKit(name).map(CachedKit::new).orElse(null))
                        .collect(Collectors.toList())
        );

        data.addData("ok", kits.isPresent(), false);
        data.addData("kits", kits.orElse(null), data.getQueryParam("details").isPresent());
    }

    @Endpoint(method = HttpMethod.GET, path = "kit/:name", perm = "kit.one")
    public void getKit(IServletData data, String name) {
        Optional<NucleusKitService> optSrv = NucleusAPI.getKitService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues kit service not available");
            return;
        }

        NucleusKitService srv = optSrv.get();

        Optional<CachedKit> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<Kit> optKit = srv.getKit(name);
            if (!optKit.isPresent()) {
                data.sendError(HttpServletResponse.SC_NOT_FOUND, "Kit not found");
                return null;
            }

            return new CachedKit(optKit.get());
        });

        data.addData("ok", optRes.isPresent(), false);
        data.addData("kit", optRes.orElse(null), true);
    }

    @Endpoint(method = HttpMethod.POST, path = "kit", perm = "kit.create")
    public void createKit(IServletData data) {
        Optional<NucleusKitService> optSrv = NucleusAPI.getKitService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues kit service not available");
            return;
        }

        NucleusKitService srv = optSrv.get();

        Optional<CachedKit> optReq = data.getRequestBody(CachedKit.class);
        if (!optReq.isPresent()) {
            data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid kit data: " + data.getLastParseError().getMessage());
            return;
        }

        CachedKit req = optReq.get();

        if (req.getName().isEmpty()) {
            data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid kit name");
            return;
        }

        Optional<CachedKit> resKit = WebAPIAPI.runOnMain(() -> {
            Kit kit = srv.createKit(req.getName());
            kit.setCost(req.getCost());
            kit.setCooldown(Duration.ofMillis(req.getCooldown()));
            if (req.getStacks() != null) {
                try {
                    kit.setStacks(req.getStacks());
                } catch (Exception e) {
                    data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not process item stack: " + e.getMessage());
                    return null;
                }
            }
            if (req.getCommands() != null) {
                kit.setCommands(req.getCommands());
            }
            srv.saveKit(kit);
            return new CachedKit(kit);
        });

        data.addData("ok", resKit.isPresent(), false);
        data.addData("kit", resKit.orElse(null), true);
    }

    @Endpoint(method = HttpMethod.PUT, path = "kit/:name", perm = "kit.change")
    public void changeKit(IServletData data, String name) {
        Optional<NucleusKitService> optSrv = NucleusAPI.getKitService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues kit service not available");
            return;
        }

        NucleusKitService srv = optSrv.get();

        Optional<CachedKit> optReq = data.getRequestBody(CachedKit.class);
        if (!optReq.isPresent()) {
            data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid kit data: " + data.getLastParseError().getMessage());
            return;
        }

        CachedKit req = optReq.get();

        Optional<CachedKit> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<Kit> optKit = srv.getKit(name);
            if (!optKit.isPresent()) {
                data.sendError(HttpServletResponse.SC_NOT_FOUND, "Kit not found");
                return null;
            }

            Kit kit = optKit.get();
            if (req.getCost() != null) {
                kit.setCost(req.getCost());
            }
            if (req.getCooldown() != null) {
                kit.setCooldown(Duration.ofMillis(req.getCooldown()));
            }
            if (req.getCommands() != null) {
                kit.setCommands(req.getCommands());
            }
            if (req.getStacks() != null) {
                try {
                    kit.setStacks(req.getStacks());
                } catch (Exception e) {
                    data.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not process item stack: " + e.getMessage());
                    return null;
                }
            }

            srv.saveKit(kit);

            return new CachedKit(kit);
        });

        data.addData("ok", optRes.isPresent(), false);
        data.addData("kit", optRes.orElse(null), true);
    }

    @Endpoint(method = HttpMethod.DELETE, path = "kit/:name", perm = "kit.delete")
    public void deleteKit(IServletData data, String name) {
        Optional<NucleusKitService> optSrv = NucleusAPI.getKitService();
        if (!optSrv.isPresent()) {
            data.sendError(HttpServletResponse.SC_NOT_FOUND, "Nuclues kit service not available");
            return;
        }

        NucleusKitService srv = optSrv.get();

        Optional<CachedKit> optRes = WebAPIAPI.runOnMain(() -> {
            Optional<Kit> optKit = srv.getKit(name);
            if (!optKit.isPresent()) {
                data.sendError(HttpServletResponse.SC_NOT_FOUND, "Kit not found");
                return null;
            }

            srv.removeKit(name);
            return new CachedKit(optKit.get());
        });

        data.addData("ok", optRes.isPresent(), false);
        data.addData("kit", optRes.orElse(null), true);
    }
}
