package org.cyntho.ts.heimdall.app;

import com.github.theholywaffle.teamspeak3.TS3Config;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.exceptions.SingleInstanceViolationException;
import org.cyntho.ts.heimdall.logging.BotLogger;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.CommandManager;
import org.cyntho.ts.heimdall.manager.FeatureManager;
import org.cyntho.ts.heimdall.manager.PermissionManager;
import org.cyntho.ts.heimdall.manager.UserManager;

import java.io.IOException;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;

public class Heimdall extends SimpleBotInstance {

    /* >> Runtime */
    private volatile BotLogger logger;
    private volatile DatabaseConnector db;

    /* >> Manager */
    private volatile UserManager userManager;
    private volatile FeatureManager featureManager;
    private volatile CommandManager commandManager;
    private volatile PermissionManager permissionManager;




    /* Constructor for the main Bot */
    public Heimdall() throws SingleInstanceViolationException {
        super("Heimdall");

        super.ts3Config = new TS3Config();
        try {
            super.botConfig = new BotConfig();

            boolean logToFile = botConfig.getBoolean("bot.logToFile", true);
            if (DEBUG_MODE){
                logger = new BotLogger(logToFile, LogLevelType.DBG);
            } else {
                logger = new BotLogger(logToFile, LogLevelType.INFO);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }




    @Override
    public void start(){




    }

    @Override
    public void stop(){

    }

    @Override
    public void log(LogLevelType lvl, String msg){

    }

    @Override
    public Heimdall clone(){
        return null;
    }


}
