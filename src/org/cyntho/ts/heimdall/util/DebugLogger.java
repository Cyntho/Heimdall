package org.cyntho.ts.heimdall.util;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLogger {

    private static final DateFormat consoleLogFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void logDebug(String msg){
        if (!Bot.DEBUG_MODE) return;

        String out = "[" + consoleLogFormat.format(new Date()) + "] " +
                "[" + LogLevelType.DBG + "] " +
                msg;
        System.out.println(out);
    }

    public static void logDebug(int i){
        logDebug(Integer.toString(i));
    }

    public static void logDebug(long l){
        logDebug(Long.toString(l));
    }

    public static void logDebug(float f){
        logDebug(Float.toString(f));
    }

    public static void logDebug(double d){
        logDebug(Double.toString(d));
    }



}
