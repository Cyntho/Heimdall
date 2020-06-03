package org.cyntho.ts.heimdall.manager;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.app.SimpleBotInstance;
import org.cyntho.ts.heimdall.features.userHistory.UserHistoryFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.ChannelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The UserManager class keeps track of all logged in users
 * for the Bot and provides functions and lists to interact
 * with them.
 *
 * @author  Xida
 * @version 1.0
 */
public class UserManager {

    private List<TS3User> userList;
    private List<String> adminUUIDs;

    private Map<Integer, Boolean> crossRefQueries;
    private Map<Integer, String> crossRefRuntimeId;

    private SimpleBotInstance instance;

    // Constructor
    public UserManager(SimpleBotInstance instance){
        this.instance = instance;
        this.userList = new ArrayList<>();
        this.adminUUIDs = new ArrayList<>();
        this.crossRefQueries = new HashMap<>();
        this.crossRefRuntimeId = new HashMap<>();
    }

    // (re-) load admins from config file
    private synchronized void refreshAdminList(){
        this.adminUUIDs.clear();
        this.adminUUIDs = instance.getBotConfig().getStringList("admins");
    }

    public synchronized void refreshUserList(){
        this.userList.clear();
        for (Client client : instance.getApi().getClients()){
            TS3User user = new TS3User(client);
            register(user, true);
        }
    }

    public synchronized void register(TS3User user, boolean startUp){
        // Prevent duplicate entries
        // (Although this could only happen, if the bot remained running while the Teamspeak server got restarted)
        if (crossRefRuntimeId.containsKey(user.getRuntimeId())){
            Bot.log(LogLevelType.BOT_CRITICAL, "Registered duplicate runtime ids! Did the Teamspeak server restart? - Refreshing..");
            refreshUserList();
            refreshAdminList();
            // TODO: Implement check when the last refresh happened to prevent spamming!!
            return;
        }

        // Set Server query status
        crossRefQueries.put(user.getRuntimeId(), user.isServerQuery());

        // Set runtime cross-reference to uuid
        crossRefRuntimeId.put(user.getRuntimeId(), user.getClientUUID());


        // Check if the user is already online with another instance
        boolean managed = false;
        for (TS3User candidate : this.userList){
            if (candidate.getClientUUID().equalsIgnoreCase(user.getClientUUID())){
                // There is/are already one or more instances available

                // Check if the candidate is a child itself
                // If so, get the parent and set relationship
                TS3User parent = candidate.getParent();
                if (parent != null){
                    parent.addChild(user);
                    user.setParent(parent);

                    //Bot.heimdall.log(LogLevelType.DBG,"User {" + parent.getRuntimeId() + "} is now parent of {" + user.getRuntimeId() + "}");

                } else {
                    // The candidate is not a parent, so we make it one (since its online longer)
                    candidate.addChild(user);
                    user.setParent(candidate);

                    //Bot.heimdall.log(LogLevelType.DBG,"User {" + candidate.getRuntimeId() + "} is now parent of {" + user.getRuntimeId() + "}");
                }

                // We break the for loop here, since there can only be one parent
                managed = true;
                break;
            }
        }

        // If the UserHistory feature is active AND the user is only online with one instance,
        // Log the login to the database
        UserHistoryFeature userHistoryFeature = (UserHistoryFeature) Bot.heimdall.getFeatureManager().getFeatureById(0x001L);

        if (userHistoryFeature != null){
            if ((!managed && userHistoryFeature.isActive() || startUp) && !user.isServerQuery()){
                userHistoryFeature.handleLogin(user);
            }
        }

        // Add the user to our managed list for later use
        this.userList.add(user);

        // Distinguish the log message depending on the user being a server query client or not
        String logMessage;
        if (user.isServerQuery()){
            logMessage = user.getOfflineCopy().getNickname() + " [SERVER-QUERY] [" + user.getClientUUID() + "] ";
        } else {
            logMessage= user.getOfflineCopy().getNickname() + " [" + user.getClientUUID() + "] logged in into " + ChannelManagement.getPathToChannelById(user.getCurrentChannelId(), true);
        }

        // Print the log message to the console (and file, if logToFile active)
        Bot.log(LogLevelType.CLIENT_JOIN_EVENT, logMessage);
    }

    public synchronized boolean unregister(int runtimeId){

        TS3User user = null;
        for (TS3User candidate : userList){
            if (candidate.getRuntimeId() == runtimeId){
                user = candidate;
                break;
            }
        }

        // If the user cannot be resolved, log an error message and return false
        if (user == null){
            Bot.log(LogLevelType.BOT_ERROR, "Unable to unregister user with id = {" + runtimeId + "} - Cannot resolve TS3User Object");
            return false;
        }

        // Some assertions here:
        // 1) The Parent instance of any TS3User Object is always 'older' than it's children
        // 2) No TS3User can be parent to others and child of one other instance at the same time


        assert (user.isParent() && user.isChild()) : "Assertion error: Cannot be parent and child at the same time!";

        // Check, if the client is parent to any other instance
        if (user.isParent()){

            // If so, list all children, select the 'oldest' and set it as parent to the remaining
            TS3User oldest = null;
            for (TS3User candidate : user.getChildren()){
                if (oldest == null){
                    // First
                    oldest = candidate;
                } else {
                    // The rest
                    if (candidate.getLoginDate() < oldest.getLoginDate()){
                        oldest = candidate;
                    }
                }
            }

            assert oldest != null : "Assertion error: user.IsParent() but oldest == null!";

            // Add the children to their new parent
            oldest.setParent(null);
            oldest.setLoginDate(user.getLoginDate());
            for (TS3User child : user.getChildren()){
                if (child.getRuntimeId() != oldest.getRuntimeId()){
                    // Logically, skip the oldest (see assertions)
                    oldest.addChild(child);
                    child.setParent(oldest);
                }
            }

            //Bot.heimdall.log(LogLevelType.DBG,"{" + user.getRuntimeId() + "} is no longer parent. It's child {" + oldest.getRuntimeId() + "} took the job!");

        } else if (user.isChild()){
            // This client is a child itself
            TS3User parent = user.getParent();
            parent.removeChild(user);

            //Bot.heimdall.log(LogLevelType.DBG,"Removed " + user.getRuntimeId() + " as child from " + parent.getRuntimeId());
        } else {
            // If the UserHistory feature is active and there is no other instance
            // left than the one leaving right now, log it to the database
            UserHistoryFeature userHistoryFeature = (UserHistoryFeature) Bot.heimdall.getFeatureManager().getFeatureById(0x001L);
            if (userHistoryFeature != null && userHistoryFeature.isActive()){
                userHistoryFeature.handleLogout(user);
            }
        }



        // Remove the object from the cross-ref lists and the userList itself
        this.crossRefQueries.remove(user.getRuntimeId());
        this.crossRefRuntimeId.remove(user.getRuntimeId());

        this.adminUUIDs.remove(user.getClientUUID());

        this.userList.remove(user);

        //Bot.heimdall.log(LogLevelType.CLIENT_LEAVE_EVENT, "Successfully unregistered " + user.getOfflineCopy().getNickname() + " [uuid=" + user.getClientUUID() + "] [RuntimeId=" + user.getRuntimeId() + "]");

        return true;
    }

    public boolean isQuery(int runtimeId){ return this.crossRefQueries.get(runtimeId); }

    public boolean isAdmin(String uuid) { return this.adminUUIDs.contains(uuid); }


    /* Methods to get access to a user */
    public TS3User getUserByRuntimeId(final int id){
        if (crossRefRuntimeId.get(id) == null){
            return null;
        } else {
            for (TS3User user : userList){
                if (user.getRuntimeId() == id){
                    return user;
                }
            }
        }
        return null;
    }

    public TS3User getUserByUUID(String uuid){
        for (TS3User user : userList){
            if (user.getOfflineCopy().getUUID().equalsIgnoreCase(uuid)){
                return user;
            }
        }
        return null;
    }

    public boolean isOnline(String uuid){
        for (TS3User user : this.userList){
            if (user.getOfflineCopy().getUUID().equalsIgnoreCase(uuid)){
                return true;
            }
        }
        return false;
    }

    public List<TS3User> getUserList(){
        return this.userList;
    }






















































}
