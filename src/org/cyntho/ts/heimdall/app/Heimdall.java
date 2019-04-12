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
 * The Heimdall class is the main class for this whole bot.
 * It start's the Teamspeak API, manages users, log files etc.
 *
 *
 * @author  Xida
 * @version 1.0
 */
public class Heimdall {

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
    public Heimdall(){
        this.ts3Config = new TS3Config();
        try {
            this.botConfig = new BotConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Initializing logger
        boolean logToFile = this.botConfig.getBoolean("bot.logToFile", true);

        if (DEBUG_MODE){
            this.logger = new BotLogger(logToFile, LogLevelType.DBG);
        } else {
            this.logger = new BotLogger(logToFile, LogLevelType.INFO);
        }
    }


    /* Starts the bot */
    public void start(){

        this.upTime = System.currentTimeMillis();


        if (DEBUG_MODE){
            log(LogLevelType.BOT_EVENT, "Trying to start new Bot Instance [DEBUG MODE]");
        } else {
            log(LogLevelType.BOT_EVENT, "Trying to start new Bot Instance..");
        }

        log(LogLevelType.BOT_EVENT, "Initializing environment..");

        // Create Directories
        File historyDir = new File(System.getProperty("user.dir") + "/history");
        if (!historyDir.mkdirs() && !historyDir.exists()){
            log(LogLevelType.BOT_ERROR, "Could not create /history directory!");
        }

        // Initialize configuration
        this.ts3Config.setFloodRate(botConfig.getFloodRate());
        this.ts3Config.setHost(botConfig.getQryHost());
        this.ts3Config.setQueryPort(botConfig.getQryPort());
        this.ts3Config.setDebugLevel(Level.OFF);
        this.ts3Config.setCommandTimeout(botConfig.getServerCommandTimeout());

        this.ts3Config.setDebugToFile(false);



        log(LogLevelType.BOT_EVENT, "Environment: ready!");


        // Initialize Database configuration
        this.db = new DatabaseConnector(this.botConfig);

        // Check on startup, if database tables need to be created using resource file
        try {
            if (!(DatabaseSetup.doSetup())){
                this.log(LogLevelType.DATABASE_ERROR, "Error while initializing database");
            }
        } catch (Exception e){
            this.log(LogLevelType.DATABASE_ERROR, e.getMessage());
            System.out.println("exiting..");
            System.exit(1);
        }

        log(LogLevelType.BOT_EVENT, "Database: ready!");


        // Initialize API
        this.ts3Query = new TS3Query(this.ts3Config);
        try {
            this.ts3Query.connect();
        } catch (TS3ConnectionFailedException e){
            this.log(LogLevelType.BOT_ERROR, "API: Could not connect to the Teamspeak 3 Server. Make sure it is running!");
            stop();
            return;
        }

        this.ts3Api = this.ts3Query.getApi();
        log(LogLevelType.BOT_EVENT, "API: connected!");


        // Login with server query
        if (!this.ts3Api.login(botConfig.getQryUser(), botConfig.getQryPass())){
            this.ts3Query.exit();
            log(LogLevelType.BOT_CRITICAL, "Could not login with teamspeak query credentials!");
            System.exit(1);
        }

        try {
            this.ts3Api.selectVirtualServerById(botConfig.getServerId());
        } catch (Exception e){
            int tmp = botConfig.getServerId();
            log(LogLevelType.BOT_CRITICAL, "Could neither use server-id '" + tmp + "' nor default '1'. Make sure your server is running!");
            try {
                this.ts3Query.exit();
                this.ts3Api.logout();
                System.exit(1);
            } catch (Exception ignore) { /* ignore */ }
        }

        log(LogLevelType.BOT_EVENT, "Server-Query: Logged in!");


        // For security reasons, inform host if the query uses the 'ServerAdmin' credentials
        if (botConfig.getQryUser().equalsIgnoreCase("ServerAdmin") && !DEBUG_MODE){
            log(LogLevelType.BOT_CRITICAL, "Attention! The Query uses the default 'ServerAdmin' account. This could lead to security issues!");
        }


        // Try to set nickname
        if (!this.ts3Api.setNickname(botConfig.getString("bot.nick", "[Bot] Heimdall"))){
            log(LogLevelType.BOT_ERROR, "The bot's specified nickname is already in use. Trying to kick user...");

            for (Client client : ts3Api.getClients()){
                if (client.getNickname().equalsIgnoreCase(botConfig.getString("bot.nick", "[Bot] Heimdall"))){
                    if (client.isServerQueryClient()){
                        log(LogLevelType.BOT_ERROR, "Could not kick the user, since its a server query client!");
                    } else {
                        if(this.ts3Api.kickClientFromServer("This username is reserved!", client)){
                            log(LogLevelType.BOT_EVENT, "User has been kicked. Trying again to use nickname..");

                            if (!this.ts3Api.setNickname(botConfig.getString("bot.nick", "[Bot] Heimdall"))){
                                log(LogLevelType.BOT_ERROR, "Still not able to set nickname.. Shutting down!");
                                stop();
                                System.exit(1);
                            }

                        } else {
                            log(LogLevelType.BOT_ERROR, "Unable to kick this user!");
                            System.exit(1);
                        }
                    }
                    break;
                }
            }
        }
        this.nickname = this.botConfig.getString("bot.nick", "[Bot] Heimdall");
        log(LogLevelType.BOT_EVENT, "Nickname: set!");


        // Set current channel
        for (Channel c : ts3Api.getChannels()){
            if (c.isDefault()){
                this.currentChannelId = c.getId();
                break;
            }
        }

        // Register Event-Listener
        TS3Listener listener = new GlobalListener();
        this.ts3Api.addTS3Listeners(listener);
        log(LogLevelType.BOT_EVENT, "Event-Listener: registered!");


        // Register features
        this.featureManager = new FeatureManager();

        // --> userHistory
        if (botConfig.getBoolean("features.userHistory", true)){
            this.featureManager.register(new UserHistoryFeature());
        }

        // --> customWelcomeMessages
        if (botConfig.getBoolean("features.welcomeMessages", true)){
            featureManager.register(new WelcomeMessageFeature());
        }


        // Load user manager
        this.userManager = new UserManager();
        this.userManager.refreshUserList();


        // Load Permission Manager
        this.permissionManager = new PermissionManager();

        this.ts3Api.registerAllEvents();

        //
        // TODO --> webNotifications


        /* Register commands */
        this.commandManager = new CommandManager();
        commandManager.registerCommand(new CmdGetPath());
        commandManager.registerCommand(new CmdShutdown());
        commandManager.registerCommand(new CmdHasPermission());
        commandManager.registerCommand(new CmdList());

        commandManager.registerCommand(new CmdUpdateDescription());

        if (DEBUG_MODE) { commandManager.registerCommand(new CmdDebug()); }

        commandManager.activate();


        // Activate all features
        featureManager.activateAll();



        // Set own runtime Id
        this.botRuntimeId = this.ts3Api.whoAmI().getId();


        // Set the default channel's description to be a TS-Link to this bot's Identity
        setChannelDescription(this.currentChannelId, true);



        log(LogLevelType.BOT_EVENT, "Bot is now running!");

        if (DEBUG_MODE){
            ts3Api.sendServerMessage("Online!");
        }

        String onlineMessage = botConfig.getString("bot.goOnlineMessage", "ERR_NONE");
        if (!onlineMessage.equalsIgnoreCase("ERR_NONE")){
            onlineMessage = PlaceHolder.handleBotPlaceholder(onlineMessage);
            ts3Api.sendServerMessage(onlineMessage);
        } else if (DEBUG_MODE) {
            System.out.println(onlineMessage);
        }




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
    public String getUpTimeFormatted(SimpleDateFormat format){
        return format.format(new Date(getUpTime()));
    }


    /* Helper functions */
    public void log(LogLevelType lvl, String msg){
        if (!this.logger.validate()){
            this.logger.close();
            this.logger = this.logger.generateInstance();
            this.logger.log(LogLevelType.INFO, "Creating new log file after midnight.");
        }
        this.logger.log(lvl, msg);
    }


    // todo: make image path dynamic (config/db/param?)
    public void setChannelDescription(int channelID, boolean online){

        String nickPre = this.nickname;
        String nickTemp = "000_" + getRandomString(10) + "_000";

        this.ts3Api.setNickname(nickTemp);

        // Image link: [IMG]ts3image://online.png?channel=56&path=/[/IMG]


        Client client = this.getApi().getClientByNameExact(nickTemp, false);

        if (client != null && client.isServerQueryClient() && online){

            String link = "[URL=client://" + client.getId() + "/";
            link += client.getUniqueIdentifier() + "~";

            String original = nickPre;
            String converted = StringParser.convertToTeamspeakName(original);

            original = StringParser.replace(original, "\\\\[", '[');
            original = StringParser.replace(original, "\\\\]", ']');

            link += converted + "]" + original + "[/URL]";

            String imgLink = "[IMG]ts3image://online.png?channel=" + channelID + "&path=/[/IMG]";

            Map<ChannelProperty, String> map = new HashMap<>();

            map.put(ChannelProperty.CHANNEL_DESCRIPTION, imgLink + " " + link);

            this.ts3Api.setNickname(nickPre);
            ts3Api.editChannel(channelID, map);

        } else {
            this.ts3Api.setNickname(nickPre);


            String imgLink = "[IMG]ts3image://offline.png?channel=56&path=/[/IMG]";

            Map<ChannelProperty, String> map = new HashMap<>();
            map.put(ChannelProperty.CHANNEL_DESCRIPTION, imgLink + " " + nickPre);
            this.ts3Api.editChannel(channelID, map);
        }



    }
































}
