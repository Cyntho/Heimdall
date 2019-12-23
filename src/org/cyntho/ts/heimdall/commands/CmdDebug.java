package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.SilentDatabaseConnector;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.RC4Crypto;

public class CmdDebug extends BaseCommand {

    public CmdDebug() { super (0, "DebugCommand", "!dbg"); }

    @Override
    public boolean execute(TS3User invoker, String[] args){

        if (!invoker.getClientUUID().equalsIgnoreCase("2n8nOljLkhD0i+mVCDyU/4zfjwU=")){
            return false;
        }

        System.out.println("debug..");

        String raw = Bot.heimdall.getApi().getClientInfo(invoker.getClientInfo().getId()).getDescription();
        System.out.println("descr. raw: " + raw);

        String dec = null;

        try {
            dec = RC4Crypto.decrypt(raw.getBytes(), "43a136a6-2bdb-4e9e-85f9-bc755699ac21");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("dec: " + dec);

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
