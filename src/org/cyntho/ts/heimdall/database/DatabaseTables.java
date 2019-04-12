package org.cyntho.ts.heimdall.database;

public enum DatabaseTables {

    PERM_GROUP_LIST ("perm_group_list"),
    PERM_GROUP_MEMBERS ("perm_group_members"),
    PERM_SPECIAL ("perm_special"),


    USER_HISTORY_LOGIN ("user_history_login"),
    USER_HISTORY_NICKNAME ("user_history_nickname"),
    USER_LIST ("user_list"),

    WM_LIST ("wm_list"),
    WM_REF ("wm_ref");

    private final String name;

    DatabaseTables(final String n){
        name = n;
    }

    public final String getName() { return this.name; }


    public DatabaseTables parseFromString(String name){
        for (DatabaseTables table : values()){
            if (table.name.equalsIgnoreCase(name)){
                return table;
            }
        }
        return null;
    }

}
