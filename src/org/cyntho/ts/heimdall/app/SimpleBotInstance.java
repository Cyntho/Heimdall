package org.cyntho.ts.heimdall.app;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.exceptions.SingleInstanceViolationException;
import org.cyntho.ts.heimdall.logging.LogEntry;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.io.IOException;


/**
 * Abstract Bot Instance
 * Base version of a bot's instance for further implementation
 * Useful for mini versions of the bot that for example monitor
 * a certain channel or user and are in general only online for a
 * limited time period.
 *
 * At any time there should be only ONE implementation for the main bot
 *
 * @author Cyntho
 * @version 1.0
 */
public abstract class SimpleBotInstance {

    /* API-related */
    TS3Config ts3Config;
    TS3Query  ts3Query;
    TS3Api    ts3Api;

    /* Configuration */
    BotConfig botConfig;

    /* Runtime */
    int currentChannelId;
    int botRuntimeId;
    long startupTime;
    String nickname;

    private final String instanceIdentifier;
    private final boolean forceSingleInstance;
    private SimpleBotInstance parent;


    /* Constructor */
    SimpleBotInstance(String identifier) throws SingleInstanceViolationException {
        this.instanceIdentifier = identifier;
        this.forceSingleInstance = true;
        this.parent = null;
        init();
    }

    SimpleBotInstance(String identifier, SimpleBotInstance parent) throws SingleInstanceViolationException {
        this.instanceIdentifier = identifier;
        this.parent = parent;
        this.forceSingleInstance = false;
        init();
    }

    /* ForceSingleInstance handling */
    private void init() throws SingleInstanceViolationException {
        if (forceSingleInstance && Bot.heimdall != null){
            throw new SingleInstanceViolationException(Bot.heimdall);
        } else if (forceSingleInstance){
            //TODO: Bot.heimdall = this;
            try {
                botConfig = new BotConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Public final GETTER */
    public final TS3Api getApi(){
        return this.ts3Api;
    }

    public final TS3Query getQuery(){
        return this.ts3Query;
    }

    public final BotConfig getBotConfig(){
        return this.botConfig;
    }

    public final String getInstanceIdentifier(){
        return this.instanceIdentifier;
    }

    public final int getCurrentChannelId(){
        return this.currentChannelId;
    }

    public final int getBotRuntimeId(){
        return this.botRuntimeId;
    }

    public final long getStartupTime(){
        return this.startupTime;
    }

    public final long getUptime(){
        return System.currentTimeMillis() - this.startupTime;
    }

    public final String getNickname() {
        return this.nickname;
    }

    public final void setNickname(String n){
        this.nickname = n;
    }

    public final boolean isParent() {
        return this.parent == null;
    }

    public final SimpleBotInstance getParent(){
        return this.parent;
    }


    /* Public abstract methods */

    public abstract void start();
    public abstract void stop();

    public abstract SimpleBotInstance clone();













































}
