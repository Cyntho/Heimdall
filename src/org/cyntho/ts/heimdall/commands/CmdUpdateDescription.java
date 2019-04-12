package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdUpdateDescription extends BaseCommand {

    public CmdUpdateDescription(){
        super(7, "update description", "!descr");
    }

    @Override
    public boolean execute(TS3User invoker, String[] args) {

        if (args != null && (args.length >= 2)){

            try {
                int targetID = Integer.parseInt(args[1], 10);

                TS3User target = Bot.heimdall.getUserManager().getUserByRuntimeId(targetID);

                if (target == null) {
                    invoker.sendPrivateMessage("Could not resolve target by id: " + targetID);
                    return false;
                }

                for (int i = 2; i < args.length; i++){
                    target.getDescription().addData("key_" + i, args[i]);
                }

                invoker.sendPrivateMessage(target.getNickname() + "'s Description has been updated.");
                return true;

            } catch (NumberFormatException e){
                invoker.sendPrivateMessage("Invalid Arguments.");
            }
        }


        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        sendHelp(invoker);
    }

    @Override
    public void sendHelp(TS3User invoker) {
        invoker.sendPrivateMessage("!descr <id> <description>");
    }
}
