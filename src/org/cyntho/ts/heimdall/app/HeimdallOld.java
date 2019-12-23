package org.cyntho.ts.heimdall.app;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.cyntho.ts.heimdall.commands.*;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.database.DatabaseSetup;
import org.cyntho.ts.heimdall.events.GlobalListener;
import org.cyntho.ts.heimdall.features.userHistory.UserHistoryFeature;
import org.cyntho.ts.heimdall.features.welcomeMessages.WelcomeMessageFeature;
import org.cyntho.ts.heimdall.logging.BotLogger;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.CommandManager;
import org.cyntho.ts.heimdall.manager.FeatureManager;
import org.cyntho.ts.heimdall.manager.PermissionManager;
import org.cyntho.ts.heimdall.manager.UserManager;
import org.cyntho.ts.heimdall.util.PlaceHolder;
import org.cyntho.ts.heimdall.util.StringParser;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.util.StringParser.getRandomString;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;


/**
 * The HeimdallOld class is the main class for this whole bot.
 * It start's the Teamspeak API, manages users, log files etc.
 *
 *
 * @author  Xida
 * @version 1.0
 */
public class HeimdallOld {

    /* API-related */
    private TS3Config ts3Config;
    private TS3Query ts3Query;
    private TS3Api ts3Api;

    /* Configuration */
    private BotConfig botConfig;


    /* Runtime */
    boolean stopRequest;
    private int currentChannelId;
    private int botRuntimeId;
    private long upTime;
    private String nickname;
    private BotLogger logger;
    private DatabaseConnector db;

    /* Manager */
    private UserManager userManager;
    private FeatureManager featureManager;
    private CommandManager commandManager;
    private PermissionManager permissionManager;


    /* Constructor */
    public HeimdallOld(){
        this.ts3Config = new TS3Config();
        try {
            this.botConfig = new BotConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Initializing logger
        boolean logToFile = this.botConfig.getBoolean("bot.logToFile", true);

        if (DEBUG_MODE){
            this.logger = new BotLogger(logToFile, false, LogLevelType.DBG);
        } else {
            this.logger = new BotLogger(logToFile, false, LogLevelType.INFO);
        }
    }


    /* Starts the bot */
    public void start(){

    }

    /* Stop the bot */
    public void stop(){

        this.stopRequest = true;

        try {
            log(LogLevelType.BOT_EVENT, "Shutting down the bot..");

            this.featureManager.deactivateAll();
            this.commandManager.deactivate();

            if (this.ts3Api != null){

                // Clear Lobby's channel description
                for (Channel channel : this.ts3Api.getChannels()){
                    if (channel.isDefault()){
                        setChannelDescription(channel.getId(), false);
                        break;
                    }
                }

                String offlineMessage = botConfig.getString("bot.goOfflineMessage", "ERR_NONE");
                if (!offlineMessage.equalsIgnoreCase("ERR_NONE")){
                    ts3Api.sendServerMessage(PlaceHolder.handleBotPlaceholder(offlineMessage));
                }

                this.ts3Api.unregisterAllEvents();
                this.ts3Api.logout();
            }

            if (this.ts3Query != null){
                ts3Query.exit();
            }

            this.logger.close();

        } catch (Exception e){
            //
        }

        System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "] [" + LogLevelType.BOT_EVENT + "] The bot is now offline!");
    }

    /* Public GETTER */
    public TS3Api getApi(){ return this.ts3Api; }
    public int getCurrentChannelId() { return this.currentChannelId; }
    public int getBotRuntimeId() { return this.botRuntimeId; }
    public String getNickname() { return this.nickname; }
    public BotConfig getBotConfig() { return this.botConfig; }
    public DatabaseConnector getDb() { return this.db; }
    public UserManager getUserManager()  {return this.userManager; }
    public FeatureManager getFeatureManager()  {return this.featureManager; }
    public PermissionManager getPermissionManager() { return this.permissionManager; }
    public BotLogger getLogger() { return this.logger; }
    public CommandManager getCommandManager() { return this.commandManager; }
    public long getUpTime() {
        return System.currentTimeMillis() - this.upTime;
    }



    /* Helper functions */
    public void log(LogLevelType lvl, String msg){
        if (!this.logger.validate()){
            this.logger.close();
            this.logger = this.logger.generateInstance();
            //this.logger.log(LogLevelType.INFO, "Creating new log file after midnight.");
        }
        //this.logger.log(lvl, msg);
    }


    // todo: make image path dynamic (config/db/param?)
    public void setChannelDescription(int channelID, boolean online){



    }
































}
