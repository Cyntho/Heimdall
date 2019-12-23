package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdHasPermission extends BaseCommand {


    public CmdHasPermission() {
        super(0, "hasPermission", "!hasPermission");
    }

    @Override
    public boolean execute(TS3User invoker, String[] args) {

        Bot.log(LogLevelType.DBG, "Firing !hasPermission command.");

        // check if runtimeID is submitted
        try {
            int runtimeID = Integer.parseInt(args[1]);

            TS3User target = Bot.heimdall.getUserManager().getUserByRuntimeId(runtimeID);

            if (target != null){
                boolean hp = target.hasPermission(args[2]);
                if (hp){
                    invoker.sendPrivateMessage(target.getNickname() + " has the permission.");
                } else {
                    invoker.sendPrivateMessage(target.getNickname() + " doesn't have the permission.");
                }
                return true;
            } else {
                invoker.sendPrivateMessage("Could not find target.");
                return false;
            }
        } catch (IndexOutOfBoundsException e1){
            invoker.sendPrivateMessage("Invalid arguments");
        } catch (NumberFormatException e2){
            // TODO

            TS3User target = Bot.heimdall.getUserManager().getUserByUUID(args[1]);
            if (target != null){
                invoker.sendPrivateMessage(String.valueOf(target.hasPermission(args[2])));
                return true;
            }
        }

        Bot.log(LogLevelType.DBG, "test");

        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        invoker.sendPrivateMessage("!hasPermission (<uuid> | <runtimeID>) <name>");
    }

    @Override
    public void sendHelp(TS3User invoker) {
        sendUsage(invoker);
    }
}
