package org.cyntho.ts.heimdall.commands;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.manager.user.TS3User;

public class CmdStream extends BaseCommand {

    public CmdStream(){
        super(1, "stream", "!stream");
    }

    @Override
    public boolean execute(TS3User invoker, String[] args) {
        // Todo: check if StreamMonitor registered at FeatureManager


        if (invoker.hasPermission("streamer")){

            boolean doToggle, toToggle, doJoin, toJoin, doPoke, toPoke, doText, toText, help;
            doToggle = toToggle = doJoin = toJoin = doPoke = toPoke = doText = toText = help = false;

            for(String arg : args){
                if (arg.equalsIgnoreCase("-on")){
                    doToggle = toToggle = true;
                }
                if (arg.equalsIgnoreCase("-off")){
                    doToggle = true;
                    toToggle = false;
                }

                if (arg.equalsIgnoreCase("-allowJoin")){
                    doJoin = toJoin = true;
                }
                if (arg.equalsIgnoreCase("-denyJoin")){
                    doJoin = true;
                    toJoin = false;
                }

                if (arg.equalsIgnoreCase("-allowPoke")){
                    doPoke = toPoke = true;
                }
                if (arg.equalsIgnoreCase("-denyPoke")){
                    doPoke = true;
                    toPoke = true;
                }

                if (arg.equalsIgnoreCase("-allowText")){
                    doText = toText = true;
                }
                if (arg.equalsIgnoreCase("-denyText")){
                    doText = true;
                    toText = false;
                }

                if (arg.equalsIgnoreCase("-help") || arg.equalsIgnoreCase("?")){
                    help = true;
                }
            }

            if (help){
                sendHelp(invoker);
                return true;
            }

            //todo



        }
        return false;
    }

    @Override
    public void sendUsage(TS3User invoker) {
        /*
        !stream <-help |  ?> [command]
        --> sends help message

        !stream -on | -off
        --> toggles stream mode.
            If active:
            1) Streamer's ICON changed to 'stream_on.png'
            2) Streamer's permissions changed:
                i_client_needed_poke_power := 55
                i_client_needed_move_power := 55
                i_client_needed_private_textmessage_power := 55
                i_client_kick_from_channel_power := 55 (can't kick mods)
            3) Channel modified:
                needed_join := 55
                needed_abo  := 55
                needed_channel_text := 55

        !stream -allowJoin | -denyJoin
        -> Sets needed_join to default|55

        !stream -allowText | -denyText
        -> Sets needed_text to default|55

        !stream -allowPoke | -denyPoke
        -> Sets needed_poke to default|55
         */
    }

    @Override
    public void sendHelp(TS3User invoker) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n!Stream <-on | -off> \t Toggle streaming mode.");
        sb.append("\tIf active, only users of the group 'moderator' and above will be able to write private messages, poke or move you.");
        sb.append("\tAlso your current channel will be modified to only allow said users to join on their own.");
        sb.append("\n");
        sb.append("!stream -allowJoin | -denyJoin\n");
        sb.append("\tAllow/Deny users to join your channel while streaming.");
        sb.append("\n");
        sb.append("!stream -allowText | -denyText\n");
        sb.append("\tAllow/Deny users to contact you via private text messages while streaming.");
        sb.append("\n");
        sb.append("!stream -allowPoke | -denyPoke\n");
        sb.append("\tAllow/Deny users to contact you via poke while streaming.");
        sb.append("\n");
        sb.append("Note: '!stream' parameters can be combined, like '!stream -on -allowJoin'\n");
        sb.append("      Unspecified actions will always be set to 'deny'");

        invoker.sendPrivateMessage(sb.toString());
    }
}
