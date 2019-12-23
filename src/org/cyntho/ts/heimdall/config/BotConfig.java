package org.cyntho.ts.heimdall.config;

import com.github.theholywaffle.teamspeak3.TS3Query;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by Xida on 12.07.2017.
 */
@SuppressWarnings("FieldCanBeLocal")
public class BotConfig extends CustomConfig {

    // Database Defaults
    private static String DEFAULT_DB_HOST = "localhost";
    private static String DEFAULT_DB_USER = "username";
    private static String DEFAULT_DB_PASS = "password";
    private static String DEFAULT_DB_BASE = "database";
    private static int    DEFAULT_DB_PORT = 3306;

    // Query Defaults
    private static String             DEFAULT_QRY_USER       = "serveradmin";
    private static String             DEFAULT_QRY_PASS       = "password";
    private static String             DEFAULT_QRY_HOST       = "localhost";
    private static int                DEFAULT_QRY_PORT       = 10011;
    private static TS3Query.FloodRate DEFAULT_QRY_FLOOD_RATE = TS3Query.FloodRate.DEFAULT;

    private static int    DEFAULT_SERVER_ID              = 1;
    private static int    DEFAULT_SERVER_COMMAND_TIMEOUT = 4000;
    private static Level  DEFAULT_SERVER_DEBUG_LEVEL     = Level.INFO;


    public BotConfig() throws IOException{
        super("defaultBotConfig.yml", "config.yml");

        if (!initialized){
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not load config.yml file!");
        }
    }


    public String getDbHost(){
        return getString("database.host", DEFAULT_DB_HOST);
    }

    public String getDbUser() {
        return getString("database.username", DEFAULT_DB_USER);
    }

    public String getDbPass() {
        return getString("database.password", DEFAULT_DB_PASS);
    }

    public String getDbBase() {
        return getString("database.database", DEFAULT_DB_BASE);
    }

    public int getDbPort(){
        return getInt("database.port", DEFAULT_DB_PORT);
    }


    // Query
    public String getQryUser() { return getString("query.username", DEFAULT_QRY_USER); }

    public String getQryPass() { return getString("query.password", DEFAULT_QRY_PASS); }

    public String getQryHost() { return getString("query.host", DEFAULT_QRY_HOST); }

    public int getQryPort() { return getInt("query.port", DEFAULT_QRY_PORT); }

    public TS3Query.FloodRate getFloodRate(){
        String tmp = getString("query.floodRate", null);

        if (tmp == null || tmp == ""){
            return DEFAULT_QRY_FLOOD_RATE;
        }

        if (tmp.equalsIgnoreCase("UNLIMITED")){
            return TS3Query.FloodRate.UNLIMITED;
        }

        // Custom value?
        try {
            int i = Integer.parseInt(tmp, 10);
            return TS3Query.FloodRate.custom(i);
        } catch (NumberFormatException e){
            return TS3Query.FloodRate.DEFAULT;
        }
    }

    public Level getDebugLevel(){
        String tmp = getString("query.debugLevel", null);

        if (tmp == null || tmp == ""){
            return DEFAULT_SERVER_DEBUG_LEVEL;
        }

        if (tmp.equalsIgnoreCase("ALL")){
            return Level.ALL;
        }

        if (tmp.equalsIgnoreCase("WARNING")){
            return Level.WARNING;
        }

        if (tmp.equalsIgnoreCase("CONFIG")){
            return Level.CONFIG;
        }

        return DEFAULT_SERVER_DEBUG_LEVEL;
    }

    public int getServerId(){ return getInt("server.id", DEFAULT_SERVER_ID); }
    public int getServerCommandTimeout()  { return getInt("server.commandTimeout", DEFAULT_SERVER_COMMAND_TIMEOUT); }


}
