package org.cyntho.ts.heimdall.logging;


/**
 * Replacement for java.util.logging.Level;
 * Provides more specific logging messages for this Bot
 * @see BotLogger
 *
 * @author  Xida
 * @version 1.0
 */
@SuppressWarnings("unused")
public enum LogLevelType {


    NONE("None", Integer.MIN_VALUE),

    DBG("Debug", 0),
    FUNCTION("Function", 0),

    INFO("Info", 1),
    FEATURE_ENABLED ("FeatureEnabled", 1),
    FEATURE_DISABLED ("FeatureDisabled", 1),

    /* Chat */
    CHAT_SERVER("ChatServer", 2),
    CHAT_CHANNEL("ChatChannel", 2),


    /* Join/Leave/Move */
    CLIENT_JOIN_EVENT("ClientJoinEvent", 3),
    CLIENT_LEAVE_EVENT("ClientLeaveEvent", 3),
    CLIENT_MOVED_EVENT("ClientMovedEvent", 3),
    CLIENT_TIMEOUT_EVENT("ClientTimeoutEvent", 3),

    /* Client kicked/banned */
    CLIENT_KICKED_FROM_CHANNEL("ClientKickedFromChannel", 4),
    CLIENT_KICKED_FROM_SERVER("ClientKickedFromServer", 4),
    CLIENT_BANNED_FROM_SERVER("ClientBannedFromServer", 4),

    /* Server/Channel Group changed */
    CHANNEL_GROUP_ASSIGNED("ChannelGroupAssigned", 5),
    CHANNEL_GROUP_REVOKED("ChannelGroupRevoked", 5),
    SERVER_GROUP_ASSIGNED("ServerGroupAssigned", 5),
    SERVER_GROUP_REVOKED("ServerGroupRevoked", 5),
    PRIVILEGE_KEY_USED("PrivilegeKeyUsed", 5),

    /* Channel modified */
    CHANNEL_CREATED("ChannelCreated", 6),
    CHANNEL_DELETED("ChannelDeleted", 6),
    CHANNEL_MOVED("ChannelMoved", 6),
    CHANNEL_EDITED("ChannelEdited", 6),

    /* Bot Messages */
    BOT_EVENT("BotEvent", 7),
    BOT_ERROR("BotError", 8),
    BOT_CRITICAL("BotCritical", 8),

    COMMAND_FIRE("CommandFired", 8),
    COMMAND_ERROR("CommandError", 8),

    /* Database related */
    DATABASE_ERROR("DatabaseError", 8),

    ALL("All", Integer.MAX_VALUE);



    private final String name;
    private final int value;

    LogLevelType(String type, int value){
        this.name = type;
        this.value = value;
    }

    public String getName(){ return this.name; }
    public int getValue() { return this.value; }


}
