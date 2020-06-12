package org.cyntho.ts.heimdall.manager;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Permission;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cyntho.ts.heimdall.logging.LogLevelType.BOT_EVENT;

public class StreamMonitor extends BaseFeature {

    private StreamEventListener listener;

    private Map<String, TS3User> streamers;
    private Map<String, Integer> uuidToChannelIdMap;

    private Map<Integer, List<Permission>> oldChannelValues;
    private Map<String, List<Permission>> oldClientValues;


    public StreamMonitor(){
        super("StreamMonitor");
    }

    public void registerStreamer(TS3User user){
        if (streamers.get(user.getClientUUID()) == null && user.hasPermission("streamer")){

            streamers.put(user.getClientUUID(), user);
            uuidToChannelIdMap.put(user.getClientUUID(), user.getCurrentChannelId());

            saveClientState(user.getClientUUID());
            saveChannelState(user.getCurrentChannelId());
        }
    }

    public void unregister(TS3User user){

        restoreChannelState(uuidToChannelIdMap.get(user.getClientUUID()));
        restoreClientState(user.getClientUUID());

        streamers.remove(user.getClientUUID());
        uuidToChannelIdMap.remove(user.getClientUUID());
    }

    public void setChannelJoin(TS3User streamer, boolean allow){
        int id = uuidToChannelIdMap.get(streamer.getClientUUID());
        List<Permission> current = Bot.heimdall.getApi().getChannelPermissions(id);


        Bot.heimdall.getApi().addChannelPermission(id, "i_channel_needed_join_power", 50);
    }

    public void saveChannelState(int channelId){
        List<Permission> current = Bot.heimdall.getApi().getChannelPermissions(channelId);
        oldChannelValues.put(channelId, current);
    }

    public void restoreChannelState(int channelId){
        List<Permission> current = oldChannelValues.get(channelId);
        if (current != null){
            for (Permission p : current){
                Bot.heimdall.getApi().addChannelPermission(channelId, p.getName(), p.getValue());
            }
        }
        Bot.log(BOT_EVENT, "Channel '" + channelId + "' has been restored.");
    }

    public void saveClientState(String uuid){
        TS3User streamer = streamers.get(uuid);
        if (streamer == null) return;

        int channel = uuidToChannelIdMap.get(uuid);

        List<Permission> permissions = Bot.heimdall.getApi().getChannelClientPermissions(channel, streamer.getClientInfo().getDatabaseId());
        if (permissions == null) return;

        oldClientValues.put(uuid, permissions);
    }

    public void restoreClientState(String uuid){
        TS3User streamer = streamers.get(uuid);
        if (streamers == null) return;

        List<Permission> state = oldClientValues.get(uuid);
        if (state == null) return;

        int channelId = uuidToChannelIdMap.get(uuid);

        for (Permission p : state){
            Bot.heimdall.getApi().addChannelClientPermission(channelId, streamer.getClientInfo().getDatabaseId(), p.getName(), p.getValue());
        }
        Bot.log(BOT_EVENT, "User '" + streamer.getOfflineCopy().getNickname() + "' [" + uuid + "] has been restored.");
    }

    /* Available channel/client flags */
    public enum StreamFlag {
        POKE_ALLOW("i_client_needed_poke_power"),
        POKE_DENY("i_client_needed_poke_power"),
        TEXT_ALLOW("i_client_needed_private_textmessage_power"),
        TEXT_DENY("i_client_needed_private_textmessage_power"),
        JOIN_ALLOW("i_channel_needed_join_power"),
        JOIN_DENY("i_channel_needed_join_power");

        final String name;
        StreamFlag(String s){
            name = s;
        }
    }

    /*
    public void setStreamFlag(int channelId, TS3User streamer, StreamFlag...  flags){
        for (StreamFlag flag : flags){
            switch (flag){
                case POKE:
                    Bot.heimdall.getApi().addChannelClientPermission(channelId, streamer.getClientInfo().getDatabaseId(), flag.name);

            }
        }
    }
    */


    /* BaseFeature implementation */

    @Override
    public Long getId() {
        return 0x007L;
    }

    @Override
    public void activate() {
        if  (super.tryToggleStatus(true)){
            listener = new StreamEventListener(this);
            streamers = new HashMap<>();
            oldChannelValues = new HashMap<>();
            oldClientValues = new HashMap<>();
            uuidToChannelIdMap = new HashMap<>();
            Bot.heimdall.getApi().addTS3Listeners(listener);
        }
    }

    @Override
    public void deactivate() {
        if (super.tryToggleStatus(false)){

            // TODO: set all channels to default etc.

            try {
                Bot.heimdall.getApi().removeTS3Listeners(listener);
            } catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    /* Private TS3Listener implementation */

    private static class StreamEventListener implements TS3Listener{

        private StreamMonitor monitor;

        StreamEventListener(StreamMonitor m){
            monitor = m;
        }

        @Override
        public void onTextMessage(TextMessageEvent textMessageEvent) {

        }

        @Override
        public void onClientJoin(ClientJoinEvent clientJoinEvent) {

        }

        @Override
        public void onClientLeave(ClientLeaveEvent clientLeaveEvent) {

        }

        @Override
        public void onServerEdit(ServerEditedEvent serverEditedEvent) {

        }

        @Override
        public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {

        }

        @Override
        public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {

        }

        @Override
        public void onClientMoved(ClientMovedEvent clientMovedEvent) {

        }

        @Override
        public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {

        }

        @Override
        public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {

        }

        @Override
        public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {

        }

        @Override
        public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {

        }

        @Override
        public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent privilegeKeyUsedEvent) {

        }
    }


}
