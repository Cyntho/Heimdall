package org.cyntho.ts.heimdall.app;

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
import org.cyntho.ts.heimdall.exceptions.SingleInstanceViolationException;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.util.StringParser.getRandomString;

public class Heimdall extends SimpleBotInstance {

    /* >> Runtime */
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
    }

    /* Public GETTER */
    public UserManager getUserManager() { return this.userManager; }
    public FeatureManager getFeatureManager() { return this.featureManager; }
    public CommandManager getCommandManager() { return this.commandManager; }
    public PermissionManager getPermissionManager() { return this.permissionManager; }
    public DatabaseConnector getDb() { return this.db; }

    public String getUpTimeFormatted(SimpleDateFormat format){
        return format.format(new Date(System.currentTimeMillis() - getUptime()));
    }

    @Override
    public void start(){
        super.startupTime = System.currentTimeMillis();

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
        ts3Config.setFloodRate(botConfig.getFloodRate());
        ts3Config.setHost(botConfig.getQryHost());
        ts3Config.setQueryPort(botConfig.getQryPort());
        ts3Config.setDebugLevel(Level.OFF);
        ts3Config.setCommandTimeout(botConfig.getServerCommandTimeout());

        ts3Config.setDebugToFile(false);



        log(LogLevelType.BOT_EVENT, "Environment: ready!");


        // Initialize Database configuration
        db = new DatabaseConnector(this.botConfig);

        // Check on startup, if database tables need to be created using resource file
        try {
            if (!(DatabaseSetup.doSetup())){
                log(LogLevelType.DATABASE_ERROR, "Error while initializing database");
            }
        } catch (Exception e){
            log(LogLevelType.DATABASE_ERROR, e.getMessage());
            System.out.println("exiting..");
            System.exit(1);
        }

        log(LogLevelType.BOT_EVENT, "Database: ready!");

        // Initialize API
        ts3Query = new TS3Query(ts3Config);
        try {
            ts3Query.connect();
        } catch (TS3ConnectionFailedException e){
            log(LogLevelType.BOT_ERROR, "API: Could not connect to the Teamspeak 3 Server. Make sure it is running!");
            stop();
            return;
        }

        ts3Api = ts3Query.getApi();
        log(LogLevelType.BOT_EVENT, "API: connected!");

        // Login with server query
        if (!ts3Api.login(botConfig.getQryUser(), botConfig.getQryPass())){
            ts3Query.exit();
            log(LogLevelType.BOT_CRITICAL, "Could not login with teamspeak query credentials!");
            System.exit(1);
        }

        try {
            ts3Api.selectVirtualServerById(botConfig.getServerId());
        } catch (Exception e){
            int tmp = botConfig.getServerId();
            log(LogLevelType.BOT_CRITICAL, "Could neither use server-id '" + tmp + "' nor default '1'. Make sure your server is running!");
            try {
                ts3Query.exit();
                ts3Api.logout();
                System.exit(1);
            } catch (Exception ignore) { /* ignore */ }
        }

        log(LogLevelType.BOT_EVENT, "Server-Query: Logged in!");


        // For security reasons, inform host if the query uses the 'ServerAdmin' credentials
        if (botConfig.getQryUser().equalsIgnoreCase("ServerAdmin") && !DEBUG_MODE){
            log(LogLevelType.BOT_CRITICAL, "Attention! The Query uses the default 'ServerAdmin' account. This could lead to security issues!");
        }


        // Try to set nickname
        if (!ts3Api.setNickname(botConfig.getString("bot.nick", "[Bot] HeimdallOld"))){
            log(LogLevelType.BOT_ERROR, "The bot's specified nickname is already in use. Trying to kick user...");

            for (Client client : ts3Api.getClients()){
                if (client.getNickname().equalsIgnoreCase(botConfig.getString("bot.nick", "[Bot] HeimdallOld"))){
                    if (client.isServerQueryClient()){
                        log(LogLevelType.BOT_ERROR, "Could not kick the user, since its a server query client!");
                    } else {
                        if(ts3Api.kickClientFromServer("This username is reserved!", client)){
                            log(LogLevelType.BOT_EVENT, "User has been kicked. Trying again to use nickname..");

                            if (!ts3Api.setNickname(botConfig.getString("bot.nick", "[Bot] HeimdallOld"))){
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
        nickname = botConfig.getString("bot.nick", "[Bot] HeimdallOld");
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
        ts3Api.addTS3Listeners(listener);
        log(LogLevelType.BOT_EVENT, "Event-Listener: registered!");


        // Register features
        featureManager = new FeatureManager();

        // --> userHistory
        if (botConfig.getBoolean("features.userHistory", true)){
            featureManager.register(new UserHistoryFeature());
        }

        // --> customWelcomeMessages
        if (botConfig.getBoolean("features.welcomeMessages", true)){
            featureManager.register(new WelcomeMessageFeature());
        }


        // Load user manager
        userManager = new UserManager(this);
        userManager.refreshUserList();


        // Load Permission Manager
        //permissionManager = new PermissionManager();

        ts3Api.registerAllEvents();

        //
        // TODO --> webNotifications


        /* Register commands */
        commandManager = new CommandManager();
        if (botConfig.getBoolean("commands.debug", false) && DEBUG_MODE)
            commandManager.registerCommand(new CmdDebug());

        if (botConfig.getBoolean("commands.hasPermission", true))
            commandManager.registerCommand(new CmdHasPermission());

        /*if (botConfig.getBoolean("commands.list", true))
            commandManager.registerCommand(new CmdList());*/

        if (botConfig.getBoolean("commands.getPath", false))
            commandManager.registerCommand(new CmdGetPath());

        if (botConfig.getBoolean("commands.setLevel", true))
            commandManager.registerCommand(new CmdSetLevel());

        if (botConfig.getBoolean("commands.shutdown", false))
            commandManager.registerCommand(new CmdShutdown());

        if (botConfig.getBoolean("commands.updateDescription", false))
            commandManager.registerCommand(new CmdUpdateDescription());

        // TODO: Command to do backup

        // Activate all features
        featureManager.register(commandManager);
        featureManager.activateAll();



        // Set own runtime Id
        botRuntimeId = ts3Api.whoAmI().getId();


        // Set the default channel's description to be a TS-Link to this bot's Identity
        setChannelDescription(currentChannelId, true);



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

    @Override
    public void stop(){
        super.stopRequest = true;

        try {
            log(LogLevelType.BOT_EVENT, "Shutting down bot instance " + getInstanceIdentifier());

            this.featureManager.deactivateAll();
            this.commandManager.deactivate();

            if (ts3Api != null){

                for (Channel channel : ts3Api.getChannels()){
                    if (channel.isDefault()){
                        setChannelDescription(channel.getId(), false);
                        break;
                    }
                }

                String offlineMessage = botConfig.getString("bot.offlineMessage", "ERR_NONE");
                if (!offlineMessage.equalsIgnoreCase("ERR_NONE"))
                    ts3Api.sendServerMessage(PlaceHolder.handleBotPlaceholder(offlineMessage));

                ts3Api.unregisterAllEvents();
                ts3Api.logout();
            }

            if (ts3Query != null)
                ts3Query.exit();
        } catch (Exception e){
            e.printStackTrace();
        }

        log(LogLevelType.BOT_EVENT, getInstanceIdentifier() + " is now offline");
    }


    @Override
    public Heimdall clone(){
        return null;
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
