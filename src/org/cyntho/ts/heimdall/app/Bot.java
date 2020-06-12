package org.cyntho.ts.heimdall.app;

import com.github.theholywaffle.teamspeak3.api.PermissionGroupDatabaseType;
import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.exceptions.SingleInstanceViolationException;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.BotLogger;
import org.cyntho.ts.heimdall.logging.LogEntry;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.PermissionManager;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.manager.permissions.PermissionGroup;

import java.io.IOException;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static org.cyntho.ts.heimdall.logging.LogLevelType.*;

/**
 * The main application for this Bot.
 *
 * @author  Xida
 * @version 1.7
 */
public class Bot  {

    public static boolean DEBUG_MODE = TRUE; // TODO: Change to default FALSE for deployment

    public static String VERSION = "1.8.0";
    public static String AUTHOR = "Xida";
    public static String CONTACT = "info@cyntho.org";

    public static Heimdall heimdall;
    private static volatile boolean stopRequest;

    private static volatile Queue<LogEntry> logStack;

    public static volatile BotLogger logger;
    public static volatile BotConfig config;

    private static volatile Thread loggerThread;
    private static volatile Thread inputThread;

    public static volatile PermissionManager pcm;

    // TODO: Fix issue of changing bot's name when multiple bot instances are running

    public static void main(String[] args){

        // TODO: Printing out some information for the user (like console commands etc.)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        stopRequest = false;
        logStack = new LinkedList<>();

        boolean handleDirectInput = false;
        boolean install = false;

        if (args != null && args.length > 0){

            for (String s : args){
                if (s.equalsIgnoreCase("-d")){
                    DEBUG_MODE = true;
                }

                if (s.equalsIgnoreCase("-h")){
                    handleDirectInput = true;
                }

                if (s.equalsIgnoreCase("-i")){
                    install = true; //ToDo
                }
            }

        }

        try {
            config = new BotConfig();

            boolean logToFile = config.getBoolean("bot.logToFile", true);
            boolean logToDb   = config.getBoolean("bot.logToDb", false);


            logger = new BotLogger(logToFile, logToDb, (DEBUG_MODE ? DBG : LogLevelType.INFO));
            loggerThread = new Thread(new LoggerRunnable());


        } catch (IOException e){
            System.out.println("Critical Error: Unable to create/read config file.");
            if (DEBUG_MODE){
                e.printStackTrace();
            }
        }


        if (DEBUG_MODE || handleDirectInput){
            inputThread = new Thread(new DirectInputRunnable());
        }

        loggerThread.start();
        inputThread.start();

        try {
            heimdall = new Heimdall();
            heimdall.start();
        } catch (SingleInstanceViolationException e){
            log(BOT_ERROR, e.getMessage());
        }

        log(DBG,"Waiting 2 Seconds to load permissions.yml");
        Thread permLoader = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            Bot.pcm = new PermissionManager(true);

            if (pcm.isInitialized()){
                log(BOT_EVENT, "Loaded permissions.yml");
            } else {
                log(BOT_ERROR, "Error loading permissions.yml");
            }
        });

        permLoader.start();
    }

    /**
     * Little helper thread that deals with the program-wide logging.
     * Every Thread that needs to log something (here: every implementation of SimpleBotInstance)
     * will automatically call the ::log(.) method that puts the message
     * on the stack. This thread will then iterate over the stack and periodically pull all
     * messages and log them according to the settings specified in the config file.
     *
     * @see SimpleBotInstance
     * @see BotLogger
     */
    private static class LoggerRunnable implements Runnable {
        @Override
        public void run(){
            while (!stopRequest){
                try {
                    while (!logStack.isEmpty()){
                        LogEntry e = logStack.poll();
                        if (e != null && !e.getMsg().equalsIgnoreCase("")){
                            logger.log(e);
                        }

                    }
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    if (DEBUG_MODE){
                        e.printStackTrace();
                    } else {
                        System.out.println("Error in Logger Thread!");
                    }
                }
            }
        }
    }

    private static class DirectInputRunnable implements Runnable {
        @Override
        public void run(){
            log(DBG, "Starting 'DirectInputRunnable'");
            Scanner scanner = new Scanner(System.in);
            String input;

            while (!stopRequest){
                try {
                    input = scanner.nextLine();

                    if (input.equalsIgnoreCase("shutdown")){
                        log(COMMAND_FIRE, "Receiving shutdown command from console.");
                        stop();
                        break;
                    } else if (input.equalsIgnoreCase("list features")){
                        for (BaseFeature f : heimdall.getFeatureManager().getFeatures()){
                            System.out.println("\t" + f.getName() + ": " + f.isActive());
                        }
                    } else if (input.equalsIgnoreCase("list users")) {
                        for (TS3User u : heimdall.getUserManager().getUserList()) {
                            System.out.println("\t" + u.getRuntimeId() + "\t" + u.getOfflineCopy().getNickname() + " [" + u.getOfflineCopy().getUUID() + "] " + u.getLoginDate());
                        }

                    } else if (input.equalsIgnoreCase("list groups")) {
                        System.out.println(pcm.toString());

                    } else if (input.equalsIgnoreCase("reload")) {
                        pcm = new PermissionManager(true);

                    } else if (input.startsWith("grp")) {

                        Client c = heimdall.getApi().getClientByUId("jIKSv0a8bavNq2mvpH/N4vG+vFs=");

                        if (c != null) {
                            Map<Integer, Boolean> m = pcm.calculateChannelEntrancePermissions(c);

                            System.out.println(String.format("Channel permissions for client '%s'", c.getUniqueIdentifier()));
                            for (Map.Entry<Integer, Boolean> entry : m.entrySet()) {
                                if (!entry.getValue()) continue;

                                System.out.println(String.format("\tChannel ID: %d\t Can join: %b", entry.getKey(), true));
                            }
                        }

                    } else if (input.equalsIgnoreCase("toggle")){

                        int channelId = 127; // Public Talk 1
                        int guestGroupId = 8;
                        int unlockedGroupId = 10;

                        Client c = heimdall.getApi().getClientByUId("jIKSv0a8bavNq2mvpH/N4vG+vFs=");
                        if (c != null){

                            List<ChannelGroupClient> channelGroupClients = heimdall.getApi().getChannelGroupClientsByChannelId(channelId);
                            boolean found = false;

                            for (ChannelGroupClient channelGroupClient : channelGroupClients){
                                if (channelGroupClient.getClientDatabaseId() == c.getDatabaseId()){
                                    // User is in any group --> reset
                                    System.out.println("Found some assigned channel group, resetting!");
                                    heimdall.getApi().setClientChannelGroup(guestGroupId, channelId, c.getDatabaseId());
                                    found = true;
                                    break;
                                }
                            }

                            if (!found){
                                System.out.println("Setting to unlocked");
                                heimdall.getApi().setClientChannelGroup(unlockedGroupId, channelId, c.getDatabaseId());
                            }

                        }


                    } else if (input.equalsIgnoreCase("assign")) {

                        TS3User victim = heimdall.getUserManager().getUserByUUID("jIKSv0a8bavNq2mvpH/N4vG+vFs=");

                        int channelId = 127;
                        int groupId = 8;
                        int dbID;

                        if (victim != null){
                            dbID = victim.getClientInfo().getDatabaseId();

                            System.out.println("getChannelGroupClientsByChannelId:");

                            for (ChannelGroupClient c : heimdall.getApi().getChannelGroupClientsByChannelId(channelId)){
                                System.out.println("\t" + c.toString());
                            }


                            System.out.println();
                            System.out.println("getChannelGroupClients(channelId, dbID, groupId");

                            for (ChannelGroupClient c : heimdall.getApi().getChannelGroupClients(channelId, dbID, groupId)){
                                System.out.println("\t" + c.toString());
                            }







                        }


                    } else if (input.startsWith("test")) {
                        List<ServerGroup> srvGroups = heimdall.getApi().getServerGroups();
                        for (ServerGroup g : srvGroups) {
                            if (g.getType() == PermissionGroupDatabaseType.REGULAR) {
                                for (Permission p : heimdall.getApi().getServerGroupPermissions(g)) {
                                    if (p.getName().equalsIgnoreCase("i_icon_id")) {
                                        System.out.println(String.format("Server Group '%s' contains 'i_icon_id' with value %s", g.getName(), p.getValue()));
                                    }
                                }

                            }
                        }

                    } else if (input.startsWith("cj")){

                        try {
                            if (pcm.isInitialized()){

                                String[] args = input.split(" ");

                                // cj [clientUUID] [channelId]
                                if (args.length != 3){
                                    System.out.println("Invalid command args.");
                                } else {
                                    TS3User target = heimdall.getUserManager().getUserByUUID(args[1]);
                                    if (target == null){
                                        System.out.println("Cannot resolve target by uuid " + args[1]);
                                    } else {
                                        int targetChannelId = Integer.parseInt(args[2]);

                                        boolean canJoin = false;

                                        for (PermissionGroup p : pcm.getPermissionGroups()){
                                            for (int groupId : target.getClientInfo().getServerGroups()){
                                                if (groupId == p.getServerGroup().getId()){

                                                    if (p.getChannelMap().get(targetChannelId) == null){
                                                        continue;
                                                    }

                                                    canJoin = p.getChannelMap().get(targetChannelId);

                                                    System.out.println(String.format("PermissionGroup %s %s access to channel %d",
                                                            p.getName(),
                                                            (canJoin ? "allows" : "denies"),
                                                            targetChannelId));
                                                }
                                            }
                                        }

                                        System.out.println("Final value: " + canJoin);
                                    }
                                }



                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }


                    } else if (input.startsWith("enc")){

                        TS3User cyn = Bot.heimdall.getUserManager().getUserByUUID("2n8nOljLkhD0i+mVCDyU/4zfjwU=");
                        if (cyn != null){

                            System.out.println("Avatar: " + cyn.getClientInfo().getAvatar());

                        }

                    } else {
                        System.out.println("Invalid command: " + input);
                    }
                } catch (Exception e){
                    if (DEBUG_MODE){
                        e.printStackTrace();
                    }
                }
            }
            scanner.close();
            log(DBG, "DirectInputRunnable stopped.");
            stop();
        }
    }

    public static void log(LogLevelType type, String msg){
        log(type, msg, heimdall);
    }

    public static void log(LogLevelType type, String msg, SimpleBotInstance instance){
        logStack.add(new LogEntry(type, msg, instance));
    }

    public static void stop(){
        if (stopRequested()){
            return;
        }

        stopRequest = true;

        if (!heimdall.stopRequested()){
            heimdall.stop();
        }

        while (!logStack.isEmpty()){
            logger.log(logStack.poll());
        }

        System.exit(0);
    }

    public static boolean stopRequested(){
        return stopRequest;
    }



    private static void poke(String[] args){
        // poke <-u | -c | -a> <-uuid | -chId> <[-m] msg>

        /*

        args[0] = 'poke'
        args[1] = -u || -c || -a
        args[2] = value
        args[3] = -m || null
        args[4] = value || null
         */
        String usage = "Usage: Poke <-u | -c | -a> <uuid | chId> <msg>";

        boolean isUser = false;
        boolean isChannel = false;
        boolean isAll = false;

        String ref = "";
        String msg;

        // minimum length: poke -a <msg> --> 3

        try {

            if (args[1].equalsIgnoreCase("-u")){
                isUser = true;
            } else if (args[1].equalsIgnoreCase("-c")){
                isChannel = true;
            } else if (args[2].equalsIgnoreCase("-a")){
                isAll = true;
            } else {
                StringBuilder sb = new StringBuilder();
                for (String arg : args){
                    sb.append(arg);
                }
                System.out.println("got: " + sb.toString());
                System.out.println(usage);
                return;
            }


            if (!isAll){
                ref = args[2];
                msg = args[3];
            } else {
                msg = args[2];
            }

            System.out.println("isUser: " + isUser + "; isChannel: " + isChannel + "; isAll: " + isAll + ";");
            System.out.println("Reference: " + ref);
            System.out.println("Message: " + msg);



        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println(usage);
        }
    }

}
