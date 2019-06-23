package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdSetLevel extends BaseCommand {

    public CmdSetLevel(){
        super(1, "SetLevel", "!setLevel");
    }


    @Override
    public boolean execute(TS3User invoker, String[] args) {

        if (invoker.hasPermission("setLevel")){
            int targetLevel;
            String targetUUID;

            try {
                targetUUID = args[1];
                targetLevel = Integer.parseInt(args[2], 10);
            } catch (NumberFormatException e){
                invoker.sendPrivateMessage("Invalid level: " + args[2]);
                return false;
            } catch (ArrayIndexOutOfBoundsException e){
                invoker.sendPrivateMessage("Invalid command parameters.");
                sendUsage(invoker);
                return false;
            }

            int invokerLevel = Bot.heimdall.getPermissionManager().getPermissionGroupById(invoker.getPermissionGroupId()).getRanking();

            if (invokerLevel <= targetLevel){
                invoker.sendPrivateMessage("Cannot set [" + targetUUID + "] to same/higher level as yourself.");
                return false;
            }

            return Bot.heimdall.getPermissionManager().setLevel(targetUUID, targetLevel);

        } else {
            invoker.sendPrivateMessage("Insufficient Permission.");
        }

        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        invoker.sendPrivateMessage("!setLevel <uuid> <int>");
    }

    @Override
    public void sendHelp(TS3User invoker) {
        invoker.sendPrivateMessage("Sets a user to a specific level. Cannot choose level higher or equal to invoker. Admins can only be declared via config file.");
    }
}
