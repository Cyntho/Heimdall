package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.ChannelManagement;

/**
 * Created by Xida on 15.07.2017.
 */
public class CmdGetPath extends BaseCommand {

    public CmdGetPath(){
        super(0, "GetPath", "!getPath");
    }


    @Override
    public boolean execute(TS3User invoker, String[] args) {

        if (invoker.hasPermission(super.getName())){

            int id = -1;
            boolean skipSpacer;

            /*
            0 = !getPath
            1 = "-s" || [id]
            2 = [id] || Null
             */

            if (args.length == 1) {
                // No parameters given. Skip spaces by default and use the user's
                // current channel as desired path location
                id = invoker.getCurrentChannelId();
                skipSpacer = true;

            } else if (args.length == 2){
                // 1 Argument given. Could be '-s' to include spacers or the channel id
                if (args[1].equalsIgnoreCase("-s")) {
                    skipSpacer = false;
                    id = invoker.getCurrentChannelId();
                } else {
                    skipSpacer = true;
                    id = resolveChannelID(args[1]);
                }
            } else {
                // Assume that (at least) 2 arguments given
                skipSpacer = false;
                id = resolveChannelID(args[2]);
            }

            String tmp = ChannelManagement.getPathToChannelById(id, skipSpacer);

            if (tmp == null){
                invoker.sendPrivateMessage("Error: Could not resolve channel with id '" + id + "'");
            } else {
                invoker.sendPrivateMessage("The path is: " + tmp);
                return true;
            }
        } else {
            Bot.log(LogLevelType.COMMAND_ERROR, "Insufficient permissions.");
        }

        return false;

    }

    @Override
    public void sendUsage(TS3User invoker) {

    }

    @Override
    public void sendHelp(TS3User invoker) {

    }


    private int resolveChannelID(String arg) {
        try {
            return Integer.parseInt(arg, 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
