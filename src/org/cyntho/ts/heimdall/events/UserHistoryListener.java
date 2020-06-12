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
        // Get the TS3User object from the event (if it has been registered yet)
        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(e.getClientId());
        if (user != null){
            feature.handleLogin(user);
        }
    }

    @Override public void onClientLeave(ClientLeaveEvent e){
        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(e.getClientId());
        if (user != null && user.isParent()){
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
