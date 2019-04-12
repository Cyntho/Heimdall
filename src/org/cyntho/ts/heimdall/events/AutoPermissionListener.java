package org.cyntho.ts.heimdall.events;

import com.github.theholywaffle.teamspeak3.api.event.*;

/**
 * Created by Xida on 10.08.2017.
 */
public class AutoPermissionListener implements TS3Listener {

    public void onClientJoin(ClientJoinEvent clientJoinEvent) {



    }


    // Unused events
    public void onTextMessage(TextMessageEvent textMessageEvent) {}
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
