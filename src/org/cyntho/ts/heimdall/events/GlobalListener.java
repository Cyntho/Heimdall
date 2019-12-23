package org.cyntho.ts.heimdall.events;

import com.github.theholywaffle.teamspeak3.api.PrivilegeKeyType;
import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.ChannelManagement;
import org.cyntho.ts.heimdall.util.StringParser;

import java.util.List;

/**
 * @author      Xida
 * @version     1.0
 *
 * This TS3Listener implementation handles the most basic
 * events that are being monitored by @see org.cyntho.ts.heimdall
 * including:
 * - onClientJoin
 * - onClientLeave
 * - onTextMessage (!off only) @see org.cyntho.ts.heimdall.events.CommandListener
 * - onPrivilegeKeyUsed
 */
public class GlobalListener implements TS3Listener {


    @Override
    public void onTextMessage(TextMessageEvent textMessageEvent) {

        // ALWAYS allow me to shutdown the bot
        if (textMessageEvent.getMessage().equalsIgnoreCase("!off") && textMessageEvent.getInvokerUniqueId().equalsIgnoreCase("2n8nOljLkhD0i+mVCDyU/4zfjwU=")){
            Bot.log(LogLevelType.BOT_EVENT, "Receiving !off command from Admin.. ");
            Bot.stop();
            return;
        }


        if (textMessageEvent.getMessage().equalsIgnoreCase("!setDescription")){
            Channel channel = null;
            for (Channel c : Bot.heimdall.getApi().getChannels()){
                if (c.isDefault()){
                    channel = c;
                    break;
                }
            }

            if (channel != null){
                Bot.heimdall.setChannelDescription(channel.getId(), true);
            }
        }

    }

    @Override
    public void onClientJoin(ClientJoinEvent clientJoinEvent) {

        TS3User user = new TS3User(clientJoinEvent);

        // Register
        Bot.heimdall.getUserManager().register(user, false);
    }

    @Override
    public void onClientLeave(ClientLeaveEvent clientLeaveEvent) {

        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(clientLeaveEvent.getClientId());

        if (user == null){
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not resolve TS3User by id '" + clientLeaveEvent.getClientId() + "'");
        } else {

            // Switch reason (leave, kick, ban, timeout..)
            int reasonId = clientLeaveEvent.getReasonId();

            switch (reasonId){
                case 3:
                    // TIMEOUT
                    Bot.heimdall.log(LogLevelType.CLIENT_TIMEOUT_EVENT, user.getOfflineCopy().getNickname() + " timed out!");
                    break;

                case 5:
                    // KICK
                    Bot.heimdall.log(LogLevelType.CLIENT_KICKED_FROM_SERVER, user.getOfflineCopy().getNickname() + " [" + user.getClientUUID() + "] been kicked from the Server by " +
                            clientLeaveEvent.getInvokerName() + " [" + clientLeaveEvent.getInvokerUniqueId() + "] - Reason: " + clientLeaveEvent.getReasonMessage());
                    break;

                case 6:
                    // BAN
                    long duration = clientLeaveEvent.getLong("bantime");
                    String durationFinal;

                    if (duration == 0){
                        durationFinal = "permanent";
                    } else if (duration == -1){
                        Bot.heimdall.log(LogLevelType.DBG, "Error while parsing bantime!");
                        durationFinal = "permanent";
                    } else {
                        durationFinal = StringParser.longToDateString(duration);
                    }

                    Bot.heimdall.log(LogLevelType.CLIENT_BANNED_FROM_SERVER, user.getOfflineCopy().getNickname() + " [" + user.getOfflineCopy().getUUID() + "] has been banned from the Server by " +
                            clientLeaveEvent.getInvokerName() + " [" + clientLeaveEvent.getInvokerUniqueId() + "] - Reason: [" + clientLeaveEvent.getReasonMessage() + "] Duration: [" + durationFinal + "]");
                    break;

                default:
                    // Left by himself
                    Bot.heimdall.log(LogLevelType.CLIENT_LEAVE_EVENT, user.getOfflineCopy().getNickname() + " [" + user.getOfflineCopy().getUUID() + "] has left the server.");
                    break;
            }


            // Unregister
            boolean unregister = Bot.heimdall.getUserManager().unregister(clientLeaveEvent.getClientId());
            if (!unregister){
                Bot.heimdall.log(LogLevelType.CLIENT_LEAVE_EVENT, "Cannot unregister user..");
            }

        }


    }
    @Override
    public void onClientMoved(ClientMovedEvent e) {
        int reasonId = e.getReasonId();

        int channelIdOld;
        int channelIdNew = e.getTargetChannelId();
        StringBuilder logMessage = new StringBuilder();

        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(e.getClientId());

        if (user == null){
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "GlobalListener.onClientMoved(): Could not resolve user");
            return;
        }

        user.getOfflineCopy().updateNickname(user.getClientInfo().getNickname());
        channelIdOld = user.getCurrentChannelId();
        user.setCurrentChannelId(channelIdNew);


        switch (reasonId){

            case 0: // Moved by himself
                logMessage.append(user.getOfflineCopy().getNickname()).append(" [").append(user.getOfflineCopy().getUUID()).append("] went from ");
                logMessage.append(ChannelManagement.getPathToChannelById(channelIdOld, true)).append(" to ");
                logMessage.append(ChannelManagement.getPathToChannelById(channelIdNew, true));
                break;

            case 1: // Got moved by someone
                logMessage.append(user.getOfflineCopy().getNickname()).append(" [").append(user.getOfflineCopy().getUUID()).append(" ] was moved by ");
                logMessage.append(e.getInvokerName()).append(" [").append(e.getInvokerUniqueId()).append("] ");
                logMessage.append(" from ").append(ChannelManagement.getPathToChannelById(channelIdOld, true));
                logMessage.append(" to ").append(ChannelManagement.getPathToChannelById(channelIdNew, true));
                break;

            case 4: // Got kicked from Channel
                logMessage.append(user.getOfflineCopy().getNickname()).append(" [").append(user.getClientUUID()).append("] was kicked from Channel ");
                logMessage.append(ChannelManagement.getPathToChannelById(channelIdOld, true));
                logMessage.append(" by ").append(e.getInvokerName()).append(" [").append(e.getInvokerUniqueId()).append("]");
                logMessage.append(" Reason: {").append(e.getReasonMessage()).append("}");
                break;

            default:
                Bot.heimdall.log(LogLevelType.BOT_ERROR, "GlobalListener.onClientMoved(): Unhandled reason id '" + reasonId + "'");
                return;
        }

        Bot.heimdall.log(LogLevelType.CLIENT_MOVED_EVENT, logMessage.toString());
    }



    @Override
    public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent privilegeKeyUsedEvent) {
        boolean isServerQueryKey = privilegeKeyUsedEvent.getPrivilegeKeyType().equals(PrivilegeKeyType.SERVER_GROUP);
        String groupTitle = (isServerQueryKey ? "Server Group" : "Channel Group");


        Bot.heimdall.log(LogLevelType.PRIVILEGE_KEY_USED, "Key used! Type: {" + groupTitle + "} id: {" + privilegeKeyUsedEvent.getPrivilegeKeyGroupId() + "} user: {" +
            privilegeKeyUsedEvent.getInvokerName() + "} uuid: {" + privilegeKeyUsedEvent.getInvokerUniqueId() + "}");
    }





    /* (Here) unused events */
    @Override public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}
    @Override public void onServerEdit(ServerEditedEvent serverEditedEvent) {}
    @Override public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}
    @Override public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {}
    @Override public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {}
    @Override public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {}
    @Override public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {}
}
