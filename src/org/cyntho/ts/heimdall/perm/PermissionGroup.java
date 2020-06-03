package org.cyntho.ts.heimdall.perm;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import org.cyntho.ts.heimdall.app.Bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionGroup {

    private final String name;
    private int serverGroupId;
    private Map<Channel, Boolean> channelMap;

    public PermissionGroup(String name){
        this.name = name;
        this.channelMap = new HashMap<>();
    }

    public void setServerGroupId(int id){
        this.serverGroupId = id;
    }

    public String getName() {
        return name;
    }

    public int getServerGroupId(){
        return serverGroupId;
    }

    public void setChannel(int id, boolean canJoin){
        for (Channel c : Bot.heimdall.getApi().getChannels()){
            if (c.getId() == id){
                channelMap.put(c, canJoin);
                return;
            }
        }
    }


}
