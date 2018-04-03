package valandur.webapi.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class MainConfig extends BaseConfig {

    @Setting(comment = "Set this to false to completely disable the admin panel")
    public boolean adminPanel = true;

    @Setting(comment = "Change AdminPanel settings")
    public APConfig adminPanelConfig = new APConfig();

    @Setting(comment = "Set this to true when working on the WebAPI. \n" +
            "This is NOT detailed debug info, so don't turn on if not \n" +
            "running the Web-API from an IDE")
    public boolean devMode = false;

    @Setting(comment = "This is the host the API will be listening on.\n" +
            "Default is \"localhost\" to prevent any access from outside\n" +
            "the server, but you can set this to \"0.0.0.0\" to listen\n" +
            "on all addresses IF YOU HAVE CONFIGURED THE PERMISSIONS")
    public String host = "localhost";

    @Setting(comment = "This tells the API on which port to listen for requests.\n" +
            "It is recommended to use a port above 1024, as those below\n" +
            "are reserved for the system and might not be available.\n" +
            "Choose which protocols to enable. Set a port to -1 to\n" +
            "disable that protocol.")
    public int http = 8080;
    public int https = 8081;

    @Setting(comment = "Set this path to your java key store if you don't want to\n" +
            "use the default self-signed one provided by the Web-API")
    public String customKeyStore = null;
    public String customKeyStorePassword = null;
    public String customKeyStoreManagerPassword = null;

    @Setting(comment = "The default amount of time, in milliseconds, that a command\n" +
            "execution waits for response messages.")
    public int cmdWaitTime = 1000;

    @Setting(comment = "This controls the maximum amount of blocks a client can get\n" +
            "in one Web-API call")
    public int maxBlockGetSize = 1000000;

    @Setting(comment = "This is the maximum amount of blocks that a client can change\n" +
            "in one Web-API call. Please note that not all blocks are changed\n" +
            "at once (see below)")
    public int maxBlockUpdateSize = 1000000;

    @Setting(comment = "The maximum number of blocks that are changed per second during\n" +
            "a block update (related to the setting above)")
    public int maxBlocksPerSecond = 1000;

    @Setting(comment = "The number of stat entries that are saved per stat. Together with\n" +
            "the stat interval this defines how far back the stats history goes.")
    public int maxStats = 4320;

    @Setting(comment = "The interval in seconds at which the server stats are recorded.")
    public int statsInterval = 5;

    @Setting(comment = "Automatically report errors (your server IP is NOT collected,\n" +
            "neither any personal information). This just helps finding bugs.")
    public boolean reportErrors = true;


    @ConfigSerializable
    public static class APServer {
        @Setting(comment = "The display name of the server")
        public String name = "Localhost";
        @Setting(comment = "The Web-API URL for the server")
        public String apiUrl = "http://localhost:8080";

        private APServer() {}
    }
    @ConfigSerializable
    public static class APConfig {
        @Setting(comment = "The base path where the AdminPanel is served")
        public String basePath = "/admin/";
        @Setting(comment = "The list of servers in this AdminPanel")
        public List<APServer> servers;

        private APConfig() {
            servers = new ArrayList<>();
            servers.add(new APServer());
        }
    }
}
