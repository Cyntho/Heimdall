package org.cyntho.ts.heimdall.features.userSurveillance;

import com.github.theholywaffle.teamspeak3.api.event.*;
import org.cyntho.ts.heimdall.features.userSurveillance.runners.UserSurveillanceBaseRunner;

public class UserSurveillanceListener implements TS3Listener {

    private UserSurveillanceBaseRunner runner;

    public UserSurveillanceListener(UserSurveillanceBaseRunner runner){
        this.runner = runner;
    }


    @Override
    public void onTextMessage(TextMessageEvent textMessageEvent) {

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
