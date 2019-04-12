package org.cyntho.ts.heimdall.commands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

/**
 * Created by Xida on 15.07.2017.
 */
public class CmdShutdown extends BaseCommand {

    public CmdShutdown(){
        super(0,  "Shutdown", "!shutdown");
    }

    public boolean execute(@NotNull TS3User invoker, @Nullable String[] args){

        if (invoker.isAdmin() || invoker.isHost() || invoker.hasPermission("shutdown")){
            Bot.heimdall.log(LogLevelType.BOT_EVENT, "Receiving shutdown command from user " + invoker.getOfflineCopy().getNickname() + " [" + invoker.getOfflineCopy().getUUID() + "]");
            Bot.heimdall.stop();
            return true;
        }

        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        invoker.sendPrivateMessage("!shutdown");
    }

    @Override
    public void sendHelp(TS3User invoker) {
        invoker.sendPrivateMessage("!shutdown: \tStops the Bot instance via command.");
    }
}
