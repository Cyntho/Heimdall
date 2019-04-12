package org.cyntho.ts.heimdall.logging;

public class LogEntry {
    private final LogLevelType type;
    private final String msg;

    public LogEntry(LogLevelType t, String m){
        type = t;
        msg = m;
    }

    public LogLevelType getType() {
        return type;
    }

    public String getMsg(){
        return msg;
    }
}
