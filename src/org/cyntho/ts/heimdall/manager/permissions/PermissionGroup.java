package org.cyntho.ts.heimdall.manager.permissions;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.DatabaseTables;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionGroup {


    private final int id;
    private int ranking;
    private String name;
    private String descr;
    private Map<String, Boolean> permissions;


    // Constructor
    public PermissionGroup(final int id){
        this.id = id;
        this.permissions = new HashMap<>();
        init();
    }

    // Public read-only getter
    public final int getId() { return this.id; }
    public final int getRanking() { return this.ranking; }
    public final String getName() { return this.name; }
    public final String getDescr() { return this.descr; }
    public final Map<String, Boolean> getPermissions()  { return this.permissions; }

    // Void to (re-)initialize the permissions and meta data
    private void init(){
        List<String> columns = Bot.heimdall.getDb().getDatabaseColumns(DatabaseTables.PERM_GROUP_LIST);

        if (columns != null){

            // Clear if loading column names succeeded
            this.permissions.clear();

            try {
                String qry = "SELECT * FROM tsb_perm_group_list WHERE id = ? LIMIT 1";
                PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(qry);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()){

                    // Load general information
                    this.ranking = rs.getInt("rank");
                    this.name = rs.getString("name");
                    this.descr = rs.getString("descr");

                    for (String col : columns){
                        this.permissions.put(col, rs.getBoolean(col));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Bot.heimdall.log(LogLevelType.BOT_CRITICAL, "Error initializing PermissionGroup {" + this.id + "}");
        }
    }



    /**
     * Method to access a specific permission.
     * @return  The permission's value. False on default
     */
    public final boolean getPermission(String name){
        return permissions.getOrDefault(name, false);
    }



}
