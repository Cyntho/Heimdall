package org.cyntho.ts.heimdall.manager;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.permissions.PermissionGroup;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PermissionManager {

    private List<PermissionGroup> groups;
    private List<String> admins;

    public PermissionManager(){
        initialize();
    }


    /**
     * Load all groups from the database and initialize them
     */
    private void initialize(){

        this.groups = new ArrayList<>();
        this.admins = new ArrayList<>();

        // Load groups
        Bot.heimdall.log(LogLevelType.DBG, "Initializing permission groups.. ");
        int counter = 0;

        try {
            String qry = "SELECT * FROM tsb_perm_group_list";
            PreparedStatement statement = Bot.heimdall.getDb().getConnection().prepareStatement(qry);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                PermissionGroup group = new PermissionGroup(resultSet.getInt("id"));
                Bot.heimdall.log(LogLevelType.DBG, "Initialized Permission Group: " + group.getName());
                counter++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bot.heimdall.log(LogLevelType.DBG, "Done initializing permission groups. " + counter + " loaded.");


        // Load memberships
        Map<String, Integer> membership = new HashMap<>();

        try {
            String qry = "SELECT * FROM tsb_perm_group_members";
            PreparedStatement statement = Bot.heimdall.getDb().getConnection().prepareStatement(qry);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){

                // Load only online members
                String uuid = resultSet.getString("uuid");
                if (Bot.heimdall.getUserManager().isOnline(uuid)){
                    membership.put(uuid, resultSet.getInt("groupId"));
                }
            }

            // Assign membership
            for (TS3User user : Bot.heimdall.getUserManager().getUserList()){
                String s = user.getOfflineCopy().getNickname() + " [" + user.getOfflineCopy().getUUID() + "] to group " + membership.get(user.getOfflineCopy().getUUID());
                user.setPermissionGroupId(membership.get(user.getOfflineCopy().getUUID()));

                Bot.heimdall.log(LogLevelType.DBG, "Assigning " + s);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    /**
     * Update the admin list
     */
    public void updateAdminList(){
        List<String> adminList = Bot.heimdall.getBotConfig().getStringList("admins");
        if (adminList != null && adminList.size() > 0){
            this.admins = new ArrayList<>();
            this.admins = adminList;
        }
    }

    public boolean setLevel(String uuid, int level){

        try {
            String qry = "SELECT COUNT(*) AS TOTAL FROM tsb_perm_group_list WHERE 'uuid' = ?";
            PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(qry);
            stmt.setString(1, uuid);

            ResultSet resultSet = stmt.executeQuery();
            int counter = 0;

            while (resultSet.next()){
                counter = resultSet.getInt("TOTAL");
            }

            if (counter == 0){
                qry = "INSERT INTO tsb_perm_group_members VALUES (?, ?)";
                PreparedStatement stmtInsert = Bot.heimdall.getDb().getConnection().prepareStatement(qry);
                stmtInsert.setString(1, uuid);
                stmtInsert.setInt(2, level);

                return stmtInsert.executeUpdate() > 0;
            } else {
                qry = "UPDATE tsb_perm_group_members SET groupId = ? WHERE uuid = ?";
                PreparedStatement stmtUpdate = Bot.heimdall.getDb().getConnection().prepareStatement(qry);

                stmtUpdate.setInt(1, level);
                stmtUpdate.setString(2, uuid);

                return stmtUpdate.executeUpdate() > 0;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    // Information gathering

    public boolean getGroupPermissionById(final int id, String name) {
        PermissionGroup group = getPermissionGroupById(id);
        return group != null && group.getPermission(name);
    }

    private boolean isRegistered(final int id){
        for (PermissionGroup g : this.groups){
            if (g.getId() == id){
                return true;
            }
        }
        return false;
    }

    public PermissionGroup getPermissionGroupById(int id){
        for (PermissionGroup g : this.groups){
            if (g.getId() == id){
                return g;
            }
        }
        return null;
    }

    public boolean isAdmin(String uuid){
        return this.admins.contains(uuid);
    }

    // ---------------------------------------------------------------




}
