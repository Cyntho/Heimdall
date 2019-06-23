package org.cyntho.ts.heimdall.logging;

import org.cyntho.ts.heimdall.app.SimpleBotInstance;

public class LogEntry {
    private final LogLevelType type;
    private final String msg;
    private final SimpleBotInstance instance;

    public LogEntry(LogLevelType t, String m, SimpleBotInstance i){
        type = t;
        msg = m;
        instance = i;
    }

    public LogLevelType getType() {
        return type;
    }

    public String getMsg(){
        return msg;
    }

    public SimpleBotInstance getInstance() { return instance; }
}
