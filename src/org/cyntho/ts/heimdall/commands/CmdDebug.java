package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.SilentDatabaseConnector;
import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdDebug extends BaseCommand {

    public CmdDebug() { super (0, "DebugCommand", "!dbg"); }

    @Override
    public boolean execute(TS3User invoker, String[] args){

        if (!invoker.getClientUUID().equalsIgnoreCase("2n8nOljLkhD0i+mVCDyU/4zfjwU=")){
            return false;
        }

        System.out.println("debug..");

        // Updating "public talk 2", channel id: 58

        SilentDatabaseConnector silent = new SilentDatabaseConnector(Bot.heimdall.getBotConfig());

        System.out.println("initialized");

        boolean success = silent.updateChannelDescription(58, "This was added silently");

        if (success){
            System.out.println("Success!");
        } else {
            System.out.println("Failure!");
        }

        return true;
    }

    /*
    int id = 0;

        try {
            id = Integer.parseInt(args[1], 10);
        } catch (IndexOutOfBoundsException eIndex){
            id = invoker.getOfflineCopy().getClientDatabaseId();
        } catch (NumberFormatException eNumber){
            invoker.sendPrivateMessage("Invalid parameter");
            return false;
        }

        System.out.println("Listing permissions for db-id: " + id);

        List<PermissionAssignment> perms = Bot.heimdall.getApi().getPermissionOverview(65, 1468);

        Map<String, String> permissionMap;

        for (PermissionAssignment p : perms){
            permissionMap = p.getMap();
            for (String key : permissionMap.keySet()){
                System.out.println(key + " == " + permissionMap.get(key));
            }
        }

        System.out.println("Done!");
     */

    @Override
    public void sendUsage(TS3User invoker) {

    }

    @Override
    public void sendHelp(TS3User invoker) {

    }

}
