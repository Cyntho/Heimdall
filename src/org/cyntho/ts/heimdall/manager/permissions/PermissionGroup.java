package org.cyntho.ts.heimdall.manager.permissions;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import org.cyntho.ts.heimdall.app.Bot;

import java.util.*;

public class PermissionGroup {

    private final String name;
    private Map<Integer, Boolean> channelMap;
    private ServerGroup serverGroup;
    private int ranking = 0;

    public PermissionGroup(String name){
        this.name = name;
        this.channelMap = new HashMap<>();
    }

    public void setServerGroup(int id){
        for (ServerGroup g : Bot.heimdall.getApi().getServerGroups()){
            if (g.getId() == id){
                this.serverGroup = g;
                return;
            }
        }
    }

    public void setRanking(int i){
        ranking = i;
    }

    public int getRanking(){
        return ranking;
    }

    public String getName() {
        return name;
    }

    public ServerGroup getServerGroup(){
        return serverGroup;
    }

    public Map<Integer, Boolean> getChannelMap(){
        return channelMap;
    }

    public void setChannel(Integer id, boolean canJoin){
        if (canJoin){
            channelMap.put(id, true);
        } else {
            channelMap.remove(id);
        }
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(this.name).append(" (Rank ").append(ranking).append("): \n");

        for (Map.Entry<Integer, Boolean> entry : channelMap.entrySet()){
            sb.append("\tChannel: ").append(entry.getKey());
            sb.append(" ");
            sb.append(entry.getValue() ? "Allowed" : "Denied");
            sb.append("\n");
        }

        return sb.toString();
    }




}
