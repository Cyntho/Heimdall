package org.cyntho.ts.heimdall.features;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;

/**
 * Created by Xida on 14.07.2017.
 */
public abstract class BaseFeature {

    /*
    Feature list:
        0x001L  UserHistory
        0x002L  CommandManager
        0x003L  WelcomeMessage
        0x004L  UserSurveillance
        0x005L  ServerStatisticsFeature
     */

    private final String name;

    protected boolean active;

    public BaseFeature(String name){
        this.name = name;
        active = false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected final boolean tryToggleStatus(boolean toActive){
        if (toActive){
            if (active){
                Bot.log(LogLevelType.BOT_EVENT, "Could not activate " + name + ", since it's already running!");
                return false;
            } else {
                Bot.log(LogLevelType.BOT_EVENT, "Feature: " + name + " has been enabled.");
                return true;
            }
        } else {
            if (!active){
                Bot.log(LogLevelType.BOT_ERROR, "Could not deactivate" + name + ", since it's not running!");
                return false;
            } else {
                Bot.log(LogLevelType.BOT_EVENT, "Feature: " + name + " has been disabled.");
                return true;
            }
        }
    }


    public final String getName() { return name; }
    public boolean isActive() { return active; }

    public abstract Long getId();
    public abstract void activate();
    public abstract void deactivate();


}
