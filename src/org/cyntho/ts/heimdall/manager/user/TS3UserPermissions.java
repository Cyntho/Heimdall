package org.cyntho.ts.heimdall.manager.user;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.DatabaseTables;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TS3UserPermissions {


    private final TS3User owner;

    private Map<String, Integer> permissions;

    public TS3UserPermissions(TS3User owner){
        this.owner = owner;
        this.permissions = new HashMap<>();
        initialize();
    }

    private void initialize(){
        this.permissions = new HashMap<>();

        List<String> columns = Bot.heimdall.getDb().getDatabaseColumns(DatabaseTables.PERM_SPECIAL);
        if (columns == null){
            return;
        }

        try {
            String qry = "SELECT * FROM tsb_perm_special WHERE uuid = ?";
            PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(qry);

            stmt.setString(1, this.owner.getOfflineCopy().getUUID());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                for (String p : columns){
                    permissions.put(p, rs.getInt(p));
                }
            }

        } catch (SQLException e){
            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "Could not receive special permissions for user " + owner.getOfflineCopy().getUUID());
        }
    }


    public int getPermissionByName(String name){
        return this.permissions.getOrDefault(name, -1);
    }
}
