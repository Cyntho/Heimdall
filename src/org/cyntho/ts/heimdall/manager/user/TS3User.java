package org.cyntho.ts.heimdall.manager.user;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelGroupClient;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.commands.BaseCommand;
import org.cyntho.ts.heimdall.commands.CommandResponse;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.net.NetSendObject;
import org.cyntho.ts.heimdall.net.streaming.ISendAble;

import java.util.*;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.logging.LogLevelType.BOT_ERROR;

/**
 *  Wrapper class for any user instance.
 * @author Xida
 * @version 1.4
 */
public class TS3User implements ISendAble {

    private String clientUUID;
    private int runtimeId;

    /* Runtime */
    private long loginDate;
    private boolean serverQuery;
    private int currentChannelId;
    private int permissionGroupId = -1;
    private UserOfflineCopy offlineCopy;
    private Client clientReference;

    // Meta data
    private String metaIpAddress;
    private String metaVersion;
    private String metaOperatingSystem;
    private String metaCountry;
    private int metaConnections;

    // Multi-Instance handling
    private TS3User parentInstance = null;
    private List<TS3User> childInstances = null;

    // Permission management
    private TS3UserPermissions permissions;


    /**
     * Constructor for the onClientJoinEvent
     * @see org.cyntho.ts.heimdall.events.GlobalListener
     * @param event ClientJoinEvent
     */
    public TS3User(ClientJoinEvent event){
        this.clientUUID = event.getUniqueClientIdentifier();
        this.runtimeId = event.getClientId();
        this.currentChannelId = getClientInfo().getChannelId();
        this.clientReference = Bot.heimdall.getApi().getClientByUId(this.clientUUID);
        init();
    }

    /**
     * Constructor for the bot's startup/restart/reconnect or manual refresh
     * @param client com.github.theholywaffle.teamspeak3.api.wrapper.Client
     */
    public TS3User(Client client){
        this.clientUUID = client.getUniqueIdentifier();
        this.runtimeId = client.getId();
        this.currentChannelId = client.getChannelId();
        this.clientReference = client;
        init();
    }

    // Initialize Object data
    private void init(){
        this.offlineCopy = new UserOfflineCopy(this);
        this.loginDate = System.currentTimeMillis();

        // Client meta data
        ClientInfo info = this.getClientInfo();
        this.metaIpAddress = info.getIp();
        this.metaConnections = info.getTotalConnections();
        this.metaCountry = info.getCountry();
        this.metaOperatingSystem = info.getPlatform();
        this.metaVersion = info.getVersion();

        // MultiInstance handling
        this.childInstances = new ArrayList<>();
        this.parentInstance = null;

        // Query
        for (Client client : Bot.heimdall.getApi().getClients()){
            if (client.getId() == this.runtimeId){
                this.serverQuery = client.isServerQueryClient();
                break;
            }
        }

        // Permissions
        this.permissions = new TS3UserPermissions(this);
    }

    /* Public GETTER */


    public ClientInfo getClientInfo() {
        return Bot.heimdall.getApi().getClientByUId(this.clientUUID);
    }

    public String getNickname(){
        ClientInfo tmp = getClientInfo();
        if (tmp != null){
            return tmp.getNickname();
        } else {
            return "unknown";
        }
    }
    public String getClientUUID() { return this.clientUUID; }
    public int getRuntimeId() { return this.runtimeId; }
    public int getCurrentChannelId() { return this.currentChannelId; }
    public long getLoginDate() { return this.loginDate; }
    public boolean isServerQuery() { return this.serverQuery; }
    public int getPermissionGroupId() { return this.permissionGroupId; }
    public Client getClientInstance(){ return this.clientReference; }
    public boolean isAdmin(){ return Bot.heimdall.getUserManager().isAdmin(this.clientUUID); }



    // Meta data
    public String getMetaIpAddress() { return this.metaIpAddress; }
    public String getMetaVersion() { return this.metaVersion; }
    public String getMetaOperatingSystem() { return this.metaOperatingSystem; }
    public String getMetaCountry() { return this.metaCountry; }
    public int getMetaConnections() { return this.metaConnections; }


    public UserOfflineCopy getOfflineCopy() { return this.offlineCopy; }
    public void setCurrentChannelId(int val) { this.currentChannelId = val; }
    public void setLoginDate(long val) { this.loginDate = val; }
    public void refreshOfflineCopy(){
        this.offlineCopy = new UserOfflineCopy(this);
    }


    /* User interaction */


    public void poke(String msg){
        Bot.heimdall.getApi().pokeClient(this.runtimeId, msg);
    }

    public void sendPrivateMessage(String msg){
        Bot.heimdall.getApi().sendPrivateMessage(this.runtimeId, msg);
    }



    /* Multi instance handling */

    public boolean isParent(){
        return this.childInstances.size() > 0;
    }

    public boolean isChild(){
        return this.parentInstance != null;
    }

    public TS3User getParent(){
        return this.parentInstance;
    }

    public List<TS3User> getChildren(){
        return this.childInstances;
    }



    public void addChild(TS3User child){
        this.childInstances.add(child);
    }

    public void removeChild(TS3User child){
        this.childInstances.remove(child);
    }

    public void removeChild(int runtimeId){
        TS3User child = null;
        for (TS3User u : this.childInstances){
            if (u.getRuntimeId() == runtimeId){
                child = u;
                break;
            }
        }
        if (child != null){
            removeChild(child);
        }
    }

    public void setParent(TS3User parent){
        this.parentInstance = parent;
    }

    public void unsetParent(){
        this.parentInstance = null;
    }

    public void unsetChildren(){
        this.childInstances = new ArrayList<>();
    }


    /* Permission management */

    public boolean inServerGroup(int serverGroup){
        try {
            int[] serverGroups = clientReference.getServerGroups();
            if (serverGroups == null){
                return false;
            }

            for (int i : serverGroups){
                if (i == serverGroup){
                    return true;
                }
            }

        } catch (Exception e){
            if (DEBUG_MODE){
                e.printStackTrace();
            } else {
                Bot.log(BOT_ERROR, e.getMessage());
            }
        }
        return false;
    }

    public boolean inChannelGroup(int channelId, int groupId){

        List<ChannelGroupClient> c = Bot.heimdall.getApi().getChannelGroupClientsByChannelId(channelId);

        for (ChannelGroupClient temp : c){
            if (temp.getClientDatabaseId() == this.offlineCopy.getClientDatabaseId() && temp.getChannelGroupId() == groupId){
                return true;
            }
        }
        return false;
    }

    public void sendCommandFeedback(CommandResponse response, BaseCommand command, String message){
        switch (response){
            case SUCCESS:
                if (!message.equalsIgnoreCase(""))
                    sendPrivateMessage(message);
                break;

            case FAILURE:
                /*
                if (hasPermission("command.feedback")){
                    sendPrivateMessage("Error executing command.");
                    command.sendUsage(this);
                }
                 */
                break;

            default:
                Bot.log(LogLevelType.COMMAND_ERROR, "Denied '" + getNickname() + "' [" + clientUUID + "' to use command " + command.getLabel());
        }
    }


    /* ISendAble implementations */

    public TS3User generateFromNetSendObject(NetSendObject sendObject){
        return null;
    }

    public NetSendObject createNetSendObject(){
        NetSendObject o = new NetSendObject();

        o.setString("clientUUID", clientUUID);
        o.setInteger("runtimeId", runtimeId);

        o.setLong("loginDate", loginDate);
        o.setBoolean("serverQuery", serverQuery);
        o.setInteger("currentChannelId", currentChannelId);
        o.setInteger("permissionGroupId", permissionGroupId);

        o.setString("metaIpAddress", metaIpAddress);
        o.setString("metaVersion", metaVersion);
        o.setString("metaOperatingSystem", metaOperatingSystem);
        o.setString("metaCountry", metaCountry);
        o.setInteger("metaConnections", metaConnections);

        o.setSerialized("parentInstance", parentInstance);


        return o;
    }


    public boolean hasPermission(String name) {
        return false;
    }
}
