package org.cyntho.ts.heimdall.manager;

import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.config.PermissionConfig;
import org.cyntho.ts.heimdall.config.YAML.file.FileConfiguration;
import org.cyntho.ts.heimdall.manager.permissions.PermissionGroup;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.ChannelManagement;

import java.io.IOException;
import java.util.*;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.logging.LogLevelType.*;

public class PermissionManager {

    private List<PermissionGroup> permissionGroups;
    private boolean initialized = false;

    // Update stuff
    private final Map<String, Long> lastUpdated;
    private final long minTime = 60000;

    public PermissionManager(boolean refresh){
        permissionGroups = new ArrayList<>();
        lastUpdated = new HashMap<>();

        try {
            load();

            if (refresh){
                recalculateAll();
            }
        } catch (IOException e){
            Bot.log(BOT_CRITICAL, (DEBUG_MODE ? e.getMessage() : "Error while loading permissions.yml"));
        }
    }

    public boolean isInitialized() { return initialized; }

    private void load() throws IOException{

        // Init permissions.yml and the FileConfiguration object
        PermissionConfig config = new PermissionConfig();
        FileConfiguration fileConfiguration = config.getFileConfiguration();
        if (fileConfiguration == null){
            Bot.log(BOT_ERROR, "Cannot load permissions.yml");
            return;
        }

        // Load all channels, ServerGroups
        List<Channel> channelList = Bot.heimdall.getApi().getChannels();
        List<ServerGroup> serverGroupList = Bot.heimdall.getApi().getServerGroups();
        Set<String> permissionGroupSet;

        // Load list of groups defined in the permissions.yml
        try {
            permissionGroupSet = fileConfiguration.getRoot().getKeys(false);
        } catch (NullPointerException e){
            // There are now groups defined.
            Bot.log(BOT_EVENT, "Permissions.yml is empty. Ignoring it..");
            return;
        }

        List<PermissionGroup> permissionGroupList = new ArrayList<>();

        // Make sure that the listed groups exist
        // They are identified by the [name].serverGroupId
        for (String permissionGroupEntry : permissionGroupSet){
            PermissionGroup group = new PermissionGroup(permissionGroupEntry);

            boolean groupExists = false;
            int configId = config.getInt(String.format("%s.serverGroupId", permissionGroupEntry), -1);

            if (configId == -1){
                Bot.log(BOT_ERROR, String.format("Cannot find assigned ServerGroup ID for group %s. Check your permissions.yml", permissionGroupEntry));
                continue;
            }

            for (ServerGroup serverGroup : serverGroupList){
                // Can use -1 as check since server group ids are always >= 0
                if (serverGroup.getId() == config.getInt(String.format("%s.serverGroupId", permissionGroupEntry), -1)){
                    groupExists = true;
                    break;
                }
            }

            if (!groupExists){
                Bot.log(BOT_ERROR, String.format("Unable to find Server Group for group '%s' with id %d",
                        permissionGroupEntry, configId));
                continue;
            }

            // Assign the Server Group:
            group.setServerGroup(configId);

            String inherit = config.getString(String.format("%s.inherit", permissionGroupEntry), "");
            if (!inherit.equals("")){
                for (PermissionGroup temp : permissionGroupList){
                    if (temp.getName().equalsIgnoreCase(inherit)){

                        // The group inherits from some other group. Copy channel settings before overwriting them
                        Set<Integer> keySet = temp.getChannelMap().keySet();
                        for (Integer i : keySet){
                            temp.setChannel(i, temp.getChannelMap().get(i));
                        }

                        // TODO: Command permissions

                        break;
                    }
                }
            }

            // Load the assigned channels
            List<String> rawChannelList = config.getStringList(String.format("%s.channel", permissionGroupEntry));
            for (String channelListEntry : rawChannelList){

                // a) signed integer value --> Set channel and done
                // b) negative integer value --> set as negative and done
                // c) not a number
                //      --> May be number followed by a '*' as a wildcard for sub channels.
                //      --> handle that later

                int channelId = -1;
                boolean deep = false;
                boolean allowJoin = false;

                // Handle '*' wildcard e.g. for admins
                if (channelListEntry.equalsIgnoreCase("*")){
                    for (Channel c : channelList){
                        group.setChannel(c.getId(), true);
                    }
                    continue;
                }

                try {
                    if (channelListEntry.endsWith("*")){
                        channelListEntry = channelListEntry.substring(0, channelListEntry.length() - 1);
                        deep = true;
                    }

                    channelId = Integer.parseInt(channelListEntry, 10);

                    if (channelId < 0){
                        channelId *= -1;
                    } else {
                        allowJoin = true;
                    }

                } catch (NumberFormatException e) {
                    Bot.log(BOT_ERROR, String.format("Unable to identify channel '%s' for permission group '%s'",
                            channelListEntry,
                            permissionGroupEntry));
                    continue;
                }

                // Assign channel
                if (deep){
                    for (Integer i : ChannelManagement.getSubChannels(null, channelId, true)){
                        group.setChannel(i, allowJoin);
                    }
                }

                group.setChannel(channelId, allowJoin);
            }
            // Assign ranking for later sorting
            group.setRanking(config.getInt(String.format("%s.rank", group.getName()), 0));

            // Finally, add the PermissionGroup to the list
            permissionGroupList.add(group);
        }

        // Sort by ranking (asc)
        permissionGroupList.sort((o1, o2) -> Integer.compare(o1.getRanking(), o2.getRanking()));

        /*
            Assertions:
            1)  List is sorted by ranking (asc.)
            2)  Groups don't inherit from themselves
            3)  Cannot inherit from groups with higher rank
         */

        for (PermissionGroup p : permissionGroupList){
            String inheritFrom = config.getString(String.format("%s.inherit", p.getName()), "");
            if (inheritFrom.equals("")){
                //System.out.println(String.format("Group '%s' doesnt inherit from any group.", p.getName()));
                continue;
            }

            for (PermissionGroup parentCandidate : permissionGroupList){

                if (parentCandidate.getName().equalsIgnoreCase(inheritFrom)){

                    if (p.getRanking() <= parentCandidate.getRanking()){
                        Bot.log(BOT_ERROR, String.format("Cannot inherit permissions from a group with a lower rank:" +
                                "%s --> %s (%d <= %d)",
                                p.getName(),
                                parentCandidate.getName(),
                                p.getRanking(),
                                parentCandidate.getRanking()));
                    } else {
                        for (Map.Entry<Integer, Boolean> entry : parentCandidate.getChannelMap().entrySet()){
                            p.setChannel(entry.getKey(), entry.getValue());
                        }
                    }

                }
            }
        }



        this.permissionGroups = permissionGroupList;
        this.initialized = true;
    }

    public List<PermissionGroup> getPermissionGroups(){
        return this.permissionGroups;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Listing all Permission Groups:\n\n");

        for (PermissionGroup p :  permissionGroups){
            sb.append(p.toString());
        }

        return sb.toString();
    }


    public Map<Integer, Boolean> calculateChannelEntrancePermissions(Client client){

        // ChannelID -> <LastRankApplied, CanJoin>
        Map<Integer, Map.Entry<Integer, Boolean>> channelPerms = new HashMap<>();

        // ChannelId, CanJoin
        Map<Integer, Boolean> canJoinMap = new HashMap<>();

        // ChannelId, highestRank
        Map<Integer, Integer> highestRankMap = new HashMap<>();

        int[] serverGroupArray = client.getServerGroups();

        // skip admins and queries
        if ((client.isServerQueryClient() && client.getId() == Bot.heimdall.getApi().whoAmI().getId() ) || Bot.heimdall.getUserManager().isAdmin(client.getUniqueIdentifier())){
            for (Channel c : Bot.heimdall.getApi().getChannels()){
                canJoinMap.put(c.getId(), true);
            }
            return canJoinMap;
        }


        // init maps
        for (Channel c : Bot.heimdall.getApi().getChannels()){
            canJoinMap.put(c.getId(), false);
            highestRankMap.put(c.getId(), -1);
        }


        // For each PermissionGroup, iterate over every server Group the client is in
        // See if the channel is set for that server group and assign the permission
        // Note the permission group's rank and only override it if the previous rank was lower
        for (PermissionGroup permissionGroup : this.permissionGroups){

            int permRank = permissionGroup.getRanking();

            for (int value : serverGroupArray) {
                if (permissionGroup.getServerGroup().getId() == value) {
                    // Iterate over channels
                    for (Integer channelId : permissionGroup.getChannelMap().keySet()) {

                        if (canJoinMap.get(channelId) != null && highestRankMap.get(channelId) != null) {

                            // not set yet
                            canJoinMap.put(channelId, permissionGroup.getChannelMap().get(channelId));
                            highestRankMap.put(channelId, permRank);
                        } else {

                            // already set, check if rank higher than before
                            int currentRank = highestRankMap.get(channelId);
                            if (currentRank < permissionGroup.getRanking()) {

                                // if so, store new value in both maps
                                canJoinMap.put(channelId, permissionGroup.getChannelMap().get(channelId));
                                highestRankMap.put(channelId, permissionGroup.getRanking());
                            }
                        }
                    }
                }
            }
        }

        return canJoinMap;
    }


    public void assignChannelPermissions(Client client, Map<Integer, Boolean> permissions){

        int lockedGroupId   = Bot.heimdall.getBotConfig().getInt("features.ChannelGroupSystem.lockedGroupId", -1);
        int unlockedGroupId = Bot.heimdall.getBotConfig().getInt("features.ChannelGroupSystem.unlockedGroupId", -1);

        if (unlockedGroupId < 0 || lockedGroupId < 0){
            Bot.log(BOT_ERROR, "Invalid configuration for locked/unlocked Channel Groups.");
            return;
        }

        if (client.getDatabaseId() == 1 || client.isServerQueryClient() || Bot.heimdall.getUserManager().isAdmin(client.getUniqueIdentifier())){
            return;
        }

        Bot.log(PERMISSION_UPDATE, String.format("Updating channel permissions for client '%s' [%s]",
                client.getUniqueIdentifier(), client.getNickname()));

        TS3User user = Bot.heimdall.getUserManager().getUserByRuntimeId(client.getId());

        if (user == null){
            Bot.log(BOT_ERROR, String.format("PermissionManager: Unable to resolve TS3User by runtime id: %d", client.getId()));
            return;
        }

        for (Channel channel : Bot.heimdall.getApi().getChannels()){

            if (permissions.get(channel.getId()) != null){

                // Set channel group
                int targetGroupId = (permissions.get(channel.getId())) ? unlockedGroupId : lockedGroupId;

                // Check if already in that group
                boolean inChannelGroup = user.inChannelGroup(channel.getId(), targetGroupId);
                boolean shouldBeInGroup = permissions.get(channel.getId());

                if (inChannelGroup && shouldBeInGroup){
                    continue;
                }

                Bot.heimdall.getApi().setClientChannelGroup(
                        targetGroupId,
                        channel.getId(),
                        client.getDatabaseId());

                // Log it only if in debug mode
                if (!DEBUG_MODE) continue;

                Bot.log(DBG, String.format((permissions.get(channel.getId()) ? "Allowing" : "Denying") + " Access to Channel '%d' for user '%s' [%s]",
                        channel.getId(),
                        client.getNickname(),
                        client.getUniqueIdentifier()));

            }

        }


    }


    public void recalculate(Client c){
        if (c.isServerQueryClient()
                || c.getId() == Bot.heimdall.getApi().whoAmI().getId()
                || c.getUniqueIdentifier().equalsIgnoreCase(Bot.heimdall.getApi().whoAmI().getUniqueIdentifier())
                || Bot.heimdall.getUserManager().isAdmin(c.getUniqueIdentifier())

        ){
            return;
        }
        assignChannelPermissions(c, calculateChannelEntrancePermissions(c));
    }

    public void recalculateAll(){
        for (Client client : Bot.heimdall.getApi().getClients()){
            assignChannelPermissions(client, calculateChannelEntrancePermissions(client));
        }
    }

    public void updateFor(TS3User user){
        if (user == null || user.isServerQuery() || user.getRuntimeId() == Bot.heimdall.getApi().whoAmI().getId() || Bot.heimdall.getUserManager().isAdmin(user.getClientUUID())){
            return;
        }

        long last = (lastUpdated.get(user.getClientUUID()) == null ? -1 : lastUpdated.get(user.getClientUUID()));
        long now = System.currentTimeMillis();

        if (last == -1 || last - now > minTime){
            recalculate(user.getClientInfo());
            lastUpdated.put(user.getClientUUID(), System.currentTimeMillis());

            Bot.log(BOT_EVENT, String.format("PermissionManager.recalculate() canceled for user '%s' ['%s] due to time limit restrictions",
                    user.getNickname(),
                    user.getClientUUID()));

        }
    }


































}
