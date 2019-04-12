package org.cyntho.ts.heimdall.commands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

/**
 * Created by Xida on 15.07.2017.
 */
public abstract class BaseCommand {

    private final int minLevel;
    private final String name;
    private final String label;


    BaseCommand(final int minLevel, final String name, final String label){
        this.name = name;
        this.minLevel = minLevel;
        this.label = label;
    }

    public String getName() { return this.name; }
    public String getLabel() { return this.label; }
    public int getMinLevel() { return this.minLevel; }

    public final void executeWithLog(@NotNull TS3User invoker, @Nullable String[] args){

        boolean success = execute(invoker, args);
        StringBuilder params = new StringBuilder();

        for (String s : args){
            params.append(s).append(" ");
        }

        if (success){
            Bot.heimdall.log(LogLevelType.COMMAND_FIRE, invoker.getOfflineCopy().getNickname() + " used command: '" + params.toString() + "'");
        } else {
            Bot.heimdall.log(LogLevelType.COMMAND_ERROR, invoker.getOfflineCopy().getNickname() + " [" + invoker.getClientUUID() + "] failed to use command " + this.label);
        }
    }


    public abstract boolean execute(@NotNull TS3User invoker, @Nullable String[] args);

    public abstract void sendUsage(@NotNull TS3User invoker);

    public abstract void sendHelp(@NotNull TS3User invoker);

}
