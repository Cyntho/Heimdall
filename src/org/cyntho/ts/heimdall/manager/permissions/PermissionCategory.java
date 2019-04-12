package org.cyntho.ts.heimdall.manager.permissions;

public enum PermissionCategory {


    BOT_RELATED("BotRelated", 0),
    USER_LOW("UserLow", 1),
    USER_MID("UserMid", 2),
    USER_HIGH("UserHigh", 3);

    private final String name;
    private final int id;

    PermissionCategory(final String n, final int i){
        this.name = n;
        this.id = i;
    }

    public String getName() { return this.name; }
    public int getId()      { return this.id; }

}
