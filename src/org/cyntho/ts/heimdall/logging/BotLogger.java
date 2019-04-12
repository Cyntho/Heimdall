package org.cyntho.ts.heimdall.logging;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.compression.Tar;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

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
    private String logFilePathAbsolute;
    private File logFileObject;

    private PrintWriter fileWriter;
    private LogLevelType logLevel;


    public BotLogger(boolean logToFile, LogLevelType lvl) {

        this.dayOfMonth = getDayOfMonth();

        this.logToFile = logToFile;
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
            this.logFilePathAbsolute = logFileObject.getAbsolutePath();

            this.fileWriter.println("-----------------------------------------------------------------");
        }

        this.currentLogFile = pathToFile.replace("/logs/", "");
        log(LogLevelType.DBG,  "Current Log-file: " + currentLogFile);
    }

    public void log(LogLevelType lvl, String msg) {

        if (!validate()) return;

        if (lvl.getValue() > this.logLevel.getValue()) {

            if (lvl.getValue() == 0 && !Bot.DEBUG_MODE) {
                // Only print debug messages if in debug mode! (makes sense o_0)
                return;
            }

            StringBuilder out = new StringBuilder();
            out.append("[").append(consoleLogFormat.format(new Date())).append("] ");
            out.append("[").append(lvl).append("] ");
            out.append(msg);


            if (this.logToFile) {
                this.fileWriter.println(out.toString());
                flush();
            }


            System.out.println(out.toString());
        }

    }

    public boolean validate() {
        return this.dayOfMonth == getDayOfMonth();
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
        return new BotLogger(this.logToFile, this.logLevel);
    }


    public String getCurrentLogFile() { return this.currentLogFile; }
}
