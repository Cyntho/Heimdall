package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdSetLevel extends BaseCommand {

    public CmdSetLevel(){
        super(1, "SetLevel", "!setLevel");
    }


    @Override
    public boolean execute(TS3User invoker, String[] args) {

        if (invoker.hasPermission("setLevel")){



        } else {
            invoker.sendPrivateMessage("Insufficient Permission.");
        }

        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {

    }

    @Override
    public void sendHelp(TS3User invoker) {

    }
}
