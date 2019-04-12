package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.util.List;

/**
 * Created by Xida on 16.07.2017.
 */
public class CmdList extends BaseCommand {

    public CmdList(){
        super(4,  "List", "!list");
    }

    @Override
    public boolean execute(TS3User invoker, String[] args) {

        String listWhat = "";

        if (args.length > 0){
            listWhat = args[1];
        }

        if (listWhat.equalsIgnoreCase("features")){
            List<BaseFeature> features = Bot.heimdall.getFeatureManager().getFeatures();

            StringBuilder ret = new StringBuilder();
            for (BaseFeature f : features){
                ret.append(f.getName()).append("\n");
            }

            invoker.sendPrivateMessage("Active features: \n" + ret.toString());
            return true;
        }

        if (listWhat.equalsIgnoreCase("users")){
            String ret = "";

            for (TS3User user : Bot.heimdall.getUserManager().getUserList()){
                ret += user.getOfflineCopy().getNickname() + " [" + user.getClientUUID() + "] \n";
            }

            invoker.sendPrivateMessage(ret);
            return true;
        }

        if (listWhat.equalsIgnoreCase("commands")){

            StringBuilder sb = new StringBuilder();

            for (BaseCommand cmd : Bot.heimdall.getCommandManager().getRegistered()){
                sb.append(cmd.getName()).append("\n");
            }

            invoker.sendPrivateMessage("Activated Commands:\n" + sb.toString());

            return true;
        }


        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        invoker.sendPrivateMessage("Usage: !list <features | users | commands>");
    }

    @Override
    public void sendHelp(TS3User invoker) {
        invoker.sendPrivateMessage("todo");
    }
}
