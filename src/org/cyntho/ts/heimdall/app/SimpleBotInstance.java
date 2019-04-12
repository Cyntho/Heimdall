package org.cyntho.ts.heimdall.app;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.logging.BotLogger;

public class SimpleBotInstance {

    /* API-related */
    private TS3Config ts3Config;
    private TS3Query  ts3Query;
    private TS3Api    ts3Api;

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




}
