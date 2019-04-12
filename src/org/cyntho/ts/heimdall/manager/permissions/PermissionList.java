package org.cyntho.ts.heimdall.manager.permissions;


import static org.cyntho.ts.heimdall.manager.permissions.PermissionCategory.*;

public enum PermissionList {

    // Name - ID - Category

    BOT_SHUTDOWN            ("Bot_Shutdown", 0, BOT_RELATED),
    BOT_RESTART             ("Bot_Restart", 1, BOT_RELATED),
    BOT_FEATURE_MANAGING    ("Bot_Feature_Managing", 2, BOT_RELATED),

    LOW_HELP                ("Low_Help", 3, USER_LOW),
    LOW_INFO                ("Low_Info", 4, USER_LOW),
    LOW_VERSION             ("Low_Version", 5, USER_LOW),

    MID_MULTI_MOVE          ("Mid_Multi_Move", 6, USER_MID),
    MID_MULTI_PRIVATE       ("Mid_Multi_Private", 7, USER_MID),
    MID_MULTI_POKE          ("Mid_Multi_Poke", 8, USER_MID),
    MID_STAT_LOW            ("Mid_Stat_Low", 9, USER_MID),

    HIGH_MULTI_KICK         ("High_Multi_Kick", 11, USER_HIGH),
    HIGH_PROMOTE            ("High_Promote", 12, USER_HIGH),
    HIGH_DEMOTE             ("High_Demote", 13, USER_HIGH),
    HIGH_WRITE_AS_BOT       ("High_WriteAsBot", 14, USER_HIGH),
    HIGH_POKE_AS_BOT        ("High_PokeAsBot", 15, USER_HIGH),
    HIGH_STAT_HIGH          ("High_Stat_High", 16, USER_HIGH);


    private final String name;
    private final int id;
    private final PermissionCategory category;

    PermissionList(String n, int i, PermissionCategory c){
        name = n;
        id = i;
        category = c;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public PermissionCategory getCategory() { return category; }







    /*

    Bot related:
        - Shutdown
        - Restart
        - enable and disable features

    User low:
        - help
        - info
        - version
        - move self

    User mid:
        - multi move
        - multi private
        - multi poke
        - access low level statistics

    User high:
        - multi kick
        - promote
        - demote
        - 'write as bot'
        - 'poke as bot'
        - access high level statistics
        -


     */


}
