package org.cyntho.ts.heimdall.features.autoPromotion;

/**
 * Created by Xida on 10.08.2017.
 */
public enum AutoPermissionCondition {

    SERVER_GROUP ("ServerGroup", 0),
    CHANNEL_GROUP ("ChannelGroup", 1),
    ICON ("Icon", 2);

    private final String name;
    private final int id;

    AutoPermissionCondition(String n, int i){
        name = n;
        id = i;
    }

    public String getName() { return name; }
    public int getId() { return id; }
}
