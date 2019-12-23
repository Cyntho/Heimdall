package org.cyntho.ts.heimdall.features.moderate;


import com.github.theholywaffle.teamspeak3.api.event.*;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;

/**
 * Quickly set channels to 'moderated', so only the user (and higher ranking admins)
 * can talk.
 *
 * Usage:   @see CmdModerate
 */
public class ModerateFeature extends BaseFeature {

    private class ModerateListener implements TS3Listener {

        ModerateFeature feature;

        ModerateListener(ModerateFeature feature){
            this.feature = feature;
        }


        /* (Here) unused events */
        @Override public void onTextMessage(TextMessageEvent e) {}

        @Override
        public void onClientJoin(ClientJoinEvent clientJoinEvent) {

        }

        @Override
        public void onClientLeave(ClientLeaveEvent clientLeaveEvent) {

        }

        @Override public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {}
        @Override public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}
        @Override public void onServerEdit(ServerEditedEvent serverEditedEvent) {}
        @Override public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}

        @Override
        public void onClientMoved(ClientMovedEvent clientMovedEvent) {

        }

        @Override public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {}
        @Override public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {}
        @Override public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {}
        @Override public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {}

    }


    private ModerateListener listener;

    public ModerateFeature(){
        super("ModerateFeature");
    }

    public Long getId() { return 0x007L; }

    @Override
    public void activate(){
        if (super.tryToggleStatus(true)){
            this.listener = new ModerateListener(this);
            Bot.heimdall.getApi().addTS3Listeners(listener);
            super.active = true;
        }
    }

    @Override
    public void deactivate(){
        if (super.tryToggleStatus(false)){
            Bot.heimdall.getApi().removeTS3Listeners(listener);
            super.active = false;
        }
    }


}
