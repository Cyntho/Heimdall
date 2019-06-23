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

    public static String VERSION = "1.7";
    public static String AUTHOR = "Xida";
    public static String CONTACT = "info@cyntho.org";

    public static Heimdall heimdall;
    public static volatile boolean stopRequest;

    static volatile Stack<LogEntry> logStack;

    public static volatile BotLogger logger;
    public static volatile BotConfig config;

    // TODO: Fix issue of changing bot's name when multiple bot instances are running

    public static void main(String[] args){

        // TODO: Printing out some information for the user (like console commands etc.)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        stopRequest = false;
        logStack = new Stack<>();

        boolean handleDirectInput = false;

        if (args != null && args.length > 0){

            for (String s : args){
                if (s.equalsIgnoreCase("-d")){
                    DEBUG_MODE = true;
                }

                if (s.equalsIgnoreCase("-h")){
                    handleDirectInput = true;
                }
            }

        }

        try {
            config = new BotConfig();

            boolean logToFile = config.getBoolean("bot.logToFile", true);
            if (DEBUG_MODE){
                logger = new BotLogger(logToFile, LogLevelType.DBG);
            } else {
                logger = new BotLogger(logToFile, LogLevelType.INFO);
            }

            Thread loggerThread = new Thread(new LoggerRunnable());
            loggerThread.start();

        } catch (IOException e){
            System.out.println("Critical Error: Unable to create/read config file.");
            if (DEBUG_MODE){
                e.printStackTrace();
            }
            System.exit(1);
        }

        try {
            heimdall = new Heimdall();
            heimdall.start();
        } catch (SingleInstanceViolationException e){
            e.printStackTrace();
        }

        if (DEBUG_MODE || handleDirectInput){
            handleDirectInput();
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
                        logger.log(logStack.pop());
                    }
                    Thread.sleep(1000);
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

    public static void log(LogLevelType type, String msg){
        log(type, msg, heimdall);
    }

    public static void log(LogLevelType type, String msg, SimpleBotInstance instance){
        logStack.push(new LogEntry(type, msg, instance));
    }





    private static void handleDirectInput(){

        Scanner scanner = new Scanner(System.in);

        while (stopRequest){

            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("shutdown")){
                heimdall.log(LogLevelType.BOT_EVENT, "Receiving shutdown command from console.");
                heimdall.stop();
            } else if (input.equalsIgnoreCase("list features")){
                for (BaseFeature f : heimdall.getFeatureManager().getFeatures()){
                    System.out.println("\t" + f.getName() + ": " + f.isActive());
                }
            } else if (input.equalsIgnoreCase("list users")) {
                for (TS3User u : heimdall.getUserManager().getUserList()) {
                    System.out.println("\t" + u.getRuntimeId() + "\t" + u.getOfflineCopy().getNickname() + " [" + u.getOfflineCopy().getUUID() + "] " + u.getLoginDate() + " " + u.getDescription());
                }
            } else if (input.startsWith("poke")) {
                poke(input.split(" "));
            } else if (input.startsWith("test")){

                TS3User user = Bot.heimdall.getUserManager().getUserList().get(0);

                String stream = user.createNetSendObject().generateDataStream();

                System.out.println(stream);

            } else {
                System.out.println("Invalid command: " + input);
            }

        }

        scanner.close();
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
