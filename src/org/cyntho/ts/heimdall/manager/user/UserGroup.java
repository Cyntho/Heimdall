package org.cyntho.ts.heimdall.manager.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.cyntho.ts.heimdall.app.Bot;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xida on 15.07.2017.
 */
public class UserGroup {

    private final int id;
    private final String name;

    private Map<String, Integer> permissions;


    public UserGroup(int id, String name, @Nullable String load){
        this.id = id;
        this.name = name;
        this.permissions = new HashMap<>();

        if (load != null && load.equalsIgnoreCase("true")){
            init();
        }
    }

    private void init(){

        String qry = "SELECT * FROM prefix_user_group_permissions WHERE id = ? LIMIT 1";
        Object[] params = {this.id};

        ResultSet res = Bot.heimdall.getDb().executeQuery(qry, params);

        if (res != null){
            // TODO

        }

    }






}
