package org.cyntho.ts.heimdall.events;

import com.github.theholywaffle.teamspeak3.api.event.*;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.userHistory.UserHistoryFeature;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import static org.cyntho.ts.heimdall.util.DebugLogger.logDebug;

public class UserHistoryListener implements TS3Listener {

    private UserHistoryFeature feature;

    public UserHistoryListener(UserHistoryFeature f){
        feature = f;
    }


    @Override public void onClientJoin(ClientJoinEvent e){
        logDebug("Debug of: UserHistoryListener.onClientJoin");

        // Get the TS3User object from the event (if it has been registered yet)
        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(e.getClientId());
        if (user != null){
            logDebug("user != null");

            if (user.isParent()){
                logDebug("user is parent");
            } else {
                logDebug("user is child");
                for (TS3User u : user.getChildren()){
                    logDebug(u.getRuntimeId());
                }
            }


            logDebug("handleLogin()");

            feature.handleLogin(user);

        } else {
            logDebug("user is null");
        }
    }

    @Override public void onClientLeave(ClientLeaveEvent e){
        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(e.getClientId());
        if (user != null && user.isParent()){
            System.out.println("Debug of: UserHistoryListener.onClientLeave");
            System.out.println("handleLogout()");

            feature.handleLogout(user);
        }
    }




    /* (Here) unused events */
    @Override public void onTextMessage(TextMessageEvent e) {}
    @Override public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {}
    @Override public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}
    @Override public void onServerEdit(ServerEditedEvent serverEditedEvent) {}
    @Override public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}
    @Override public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {}
    @Override public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {}
    @Override public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {}
    @Override public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {}
    @Override public void onClientMoved(ClientMovedEvent e) {}

}
