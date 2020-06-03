package org.cyntho.ts.heimdall.app;

import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.exceptions.SingleInstanceViolationException;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.BotLogger;
import org.cyntho.ts.heimdall.logging.LogEntry;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.io.IOException;
import java.util.*;

import static java.lang.Boolean.TRUE;

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


            logger = new BotLogger(logToFile, logToDb, (DEBUG_MODE ? LogLevelType.DBG : LogLevelType.INFO));
            loggerThread = new Thread(new LoggerRunnable());


        } catch (IOException e){
            System.out.println("Critical Error: Unable to create/read config file.");
            if (DEBUG_MODE){
                e.printStackTrace();
            }
            //System.exit(1);
        }


        if (DEBUG_MODE || handleDirectInput){
            inputThread = new Thread(new DirectInputRunnable());
        }

        logStack.add(new LogEntry(LogLevelType.DBG, "Test Entry", heimdall));

        loggerThread.setDaemon(true);
        inputThread.setDaemon(true);

        loggerThread.start();
        inputThread.start();


        try {
            heimdall = new Heimdall();
            heimdall.start();
        } catch (SingleInstanceViolationException e){
            System.out.println(e.getMessage());
        }
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
            System.out.println("LoggerRunnable stopped.");
        }
    }

    private static class DirectInputRunnable implements Runnable {
        @Override
        public void run(){
            log(LogLevelType.DBG, "Starting 'DirectInputRunnable'");
            Scanner scanner = new Scanner(System.in);
            String input = "";

            while (!stopRequest){
                try {
                    input = scanner.nextLine();

                    if (input.equalsIgnoreCase("shutdown")){
                        log(LogLevelType.BOT_EVENT, "Receiving shutdown command from console.");
                        break;
                    } else if (input.equalsIgnoreCase("list features")){
                        for (BaseFeature f : heimdall.getFeatureManager().getFeatures()){
                            System.out.println("\t" + f.getName() + ": " + f.isActive());
                        }
                    } else if (input.equalsIgnoreCase("list users")) {
                        for (TS3User u : heimdall.getUserManager().getUserList()) {
                            System.out.println("\t" + u.getRuntimeId() + "\t" + u.getOfflineCopy().getNickname() + " [" + u.getOfflineCopy().getUUID() + "] " + u.getLoginDate());
                        }
                    } else if (input.startsWith("poke")) {
                        poke(input.split(" "));
                    } else if (input.startsWith("test")) {
                        System.out.println("testing...");

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
            System.out.println("DirectInputRunnable stopped.");
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
        stopRequest = true;

        heimdall.stop();

        System.out.println("Shutting down InputThread..." + inputThread.getState().toString());
        try {
            if (inputThread.getState() != Thread.State.TERMINATED){
                inputThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Shutting down LoggerThread..." + loggerThread.getState().toString());
        try {
            if (loggerThread.getState() != Thread.State.TERMINATED){
                loggerThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!logStack.isEmpty()){
            logger.log(logStack.poll());
        }

        System.out.println("End of Bot.stop()"); // ToDo
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
