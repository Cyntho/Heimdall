package org.cyntho.ts.heimdall.logging;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.app.SimpleBotInstance;
import org.cyntho.ts.heimdall.compression.Tar;

import java.io.*;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.util.PlaceHolder.getDayOfMonth;

/**
 * The BotLogger class provides functions to easily log
 * messages in a predefined format to the console and/or
 * a log file.
 * TODO: encryption/decryption?
 *
 * @author      Xida
 * @version     1.0
 */
public class BotLogger {

    private final int dayOfMonth;
    private final String currentLogFile;

    private DateFormat consoleLogFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean logToFile;
    private boolean logToDb;
    private File logFileObject;

    private PrintWriter fileWriter;
    private LogLevelType logLevel;


    public BotLogger(boolean logToFile, boolean logToDb, LogLevelType lvl) {

        this.dayOfMonth = getDayOfMonth();

        this.logToFile = logToFile;
        this.logToDb = logToDb;
        this.logLevel = lvl;

        PrintWriter out = null;
        String pathToFile = ("/logs/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) +  "_heimdall.log");

        if (logToFile) {

            File currentLogFile = new File(System.getProperty("user.dir") + pathToFile);

            if (!new File(System.getProperty("user.dir") + "/logs").mkdirs() && !new File(System.getProperty("user.dir") + "/logs").exists()) {
                System.out.println("[" + consoleLogFormat.format(new Date()) + "] [" + Level.SEVERE + "] Could not create '/logs' directory.");
            }

            try {
                if (!(currentLogFile.exists() && !currentLogFile.isDirectory())) {
                    if (!currentLogFile.createNewFile()) {
                        System.out.println("[" + consoleLogFormat.format(new Date()) + "] [" + Level.SEVERE + "] Could not create log file!");
                    }
                }
                out = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile, true)), true);
            } catch (IOException io) {
                System.out.println("[" + consoleLogFormat.format(new Date()) + "] [" + Level.SEVERE + "] Could not create log file!");
                io.printStackTrace();
                logToFile = false;
            } catch (Exception e) {
                e.printStackTrace();
                logToFile = false;
            }
        }

        this.fileWriter = out;




        if (logToFile) {
            this.logFileObject = new File(pathToFile);

            this.fileWriter.println("-----------------------------------------------------------------");
        }

        this.currentLogFile = pathToFile.replace("/logs/", "");
    }

    public void log(LogEntry entry){
        log(entry, logToDb);
    }

    public void log(LogEntry entry, boolean skipDb){
        log(entry.getType(), entry.getMsg(), entry.getInstance(), skipDb);
    }

    public void log(LogLevelType lvl, String msg, SimpleBotInstance instance){
        log(lvl, msg, instance, false);
    }


    public void log(LogLevelType lvl, String msg, SimpleBotInstance instance, boolean skipDb) {
        if (!validate()) return;

        if (lvl.getValue() > this.logLevel.getValue()) {

            if (lvl.getValue() == 0 && !DEBUG_MODE) {
                // Only print debug messages if in debug mode! (makes sense o_0)
                return;
            }

            StringBuilder out = new StringBuilder();
            out.append("[").append(consoleLogFormat.format(new Date())).append("] ");
            out.append("[").append(lvl).append("] ");
            out.append("[").append(instance.getInstanceIdentifier()).append("] ");
            out.append(msg);


            if (this.logToFile) {
                this.fileWriter.println(out.toString());
                flush();
            }


            System.out.println(out.toString());
        }

        if (!skipDb){
            logDb(lvl, msg, instance, null, null);
        }

    }

    public boolean validate() {
        return this.dayOfMonth == getDayOfMonth();
    }


    public void logDb(LogLevelType lvl,
                      String msg,
                      SimpleBotInstance instance,
                      String invokerUUID,
                      String targetUUID){

        if (Bot.heimdall.getDb() == null || !this.logToDb) return;

        String sql = "INSERT INTO prefix_log (LogLevelType, " +
                "LogLevelVersion, " +
                "Server, " +
                "InvokerUUID, " +
                "TargetUUID, " +
                "time, " +
                "msg) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Object[] params = {lvl.getName(),
                LogLevelType.VERSION,
                Bot.heimdall.getApi().getServerInfo().getId(),
                invokerUUID,
                targetUUID,
                System.currentTimeMillis(),
                msg};

        ResultSet set = Bot.heimdall.getDb().executeQuery(sql, params);

        if (set == null && DEBUG_MODE){
            String prep = String.format("INSERT INTO prefix_log (LogLevelType, " +
                    "LogLevelVersion, " +
                    "Server, " +
                    "InvokerUUID, " +
                    "TargetUUID, " +
                    "time, " +
                    "msg) VALUES (%s, %d, %d, %s, %s, %d, %s)",
                    lvl.getName(), LogLevelType.VERSION, Bot.heimdall.getApi().getServerInfo().getId(),
                    invokerUUID, targetUUID, System.currentTimeMillis(), msg);
            log(lvl, prep, instance, true);
        }


    }

    public void close() {
        if (this.logToFile) {
            this.fileWriter.flush();
            this.fileWriter.close();

            if (Bot.heimdall.getBotConfig().getBoolean("bot.logFileZip", false)){
                try {
                    Tar.compress(this.logFileObject.getName(), this.logFileObject);
                    if (this.logFileObject.delete()){
                        System.out.println("Old log file deleted.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void flush() {
        if (this.logToFile) {
            this.fileWriter.flush();
        }
    }

    public BotLogger generateInstance() {
        return new BotLogger(this.logToFile, this.logToDb, this.logLevel);
    }


    public String getCurrentLogFile() { return this.currentLogFile; }
}
