package org.cyntho.ts.heimdall.events;

import com.github.theholywaffle.teamspeak3.api.event.*;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.commands.BaseCommand;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.CommandManager;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;

/**
 * Created by Xida on 15.07.2017.
 */
public class CommandListener implements TS3Listener{

    private CommandManager manager;

    public CommandListener(CommandManager m){
        this.manager = m;
    }


    @Override
    public void onTextMessage(TextMessageEvent textMessageEvent) {

        if (textMessageEvent.getMessage().equalsIgnoreCase("!off") && textMessageEvent.getInvokerUniqueId().equalsIgnoreCase("2n8nOljLkhD0i+mVCDyU/4zfjwU=")){
            // Ignore the !off command from [2n8nOljLkhD0i+mVCDyU/4zfjwU=]
            // Since it will always be handled by the GlobalListener class
            return;
        }

        if (textMessageEvent.getMessage().startsWith("!")){

            TS3User invoker = Bot.heimdall.getUserManager().getUserByRuntimeId(textMessageEvent.getInvokerId());

            if (invoker == null){
                Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not resolve command invoker " + textMessageEvent.toString());
                return;
            }

            String cmdLabel = textMessageEvent.getMessage().split(" ")[0];

            if (!cmdLabel.equalsIgnoreCase("")){
                BaseCommand cmd = manager.getCommandByLabel(cmdLabel);

                if (cmd != null){
                    if (DEBUG_MODE){
                        cmd.executeWithLog(invoker, textMessageEvent.getMessage().split( " "));
                    } else {
                        cmd.execute(invoker, textMessageEvent.getMessage().split(" "));
                    }
                    Bot.log(LogLevelType.COMMAND_FIRE, textMessageEvent.getMessage());
                } else {
                    Bot.heimdall.log(LogLevelType.COMMAND_ERROR, "Could not resolve command by label " + cmdLabel + "; " + textMessageEvent.getMessage());
                }
            } else {
                // Todo: should be asserted and never called?
                Bot.heimdall.log(LogLevelType.COMMAND_ERROR, "Empty command string: " + textMessageEvent.getMessage());
            }

        }
    }


    // Unused events
    public void onClientJoin(ClientJoinEvent clientJoinEvent) {}
    public void onClientLeave(ClientLeaveEvent clientLeaveEvent) {}
    public void onServerEdit(ServerEditedEvent serverEditedEvent) {}
    public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}
    public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}
    public void onClientMoved(ClientMovedEvent clientMovedEvent) {}
    public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {}
    public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {}
    public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {}
    public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {}
    public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent privilegeKeyUsedEvent) {}
}
