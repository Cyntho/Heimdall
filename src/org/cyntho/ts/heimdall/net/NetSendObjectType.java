package org.cyntho.ts.heimdall.net;

public enum NetSendObjectType {

    INTEGER("int", 0),
    LONG("long", 1),
    FLOAT("float", 2),
    DOUBLE("double", 3),
    CHAR("char", 4),
    STRING("string", 5),
    BOOLEAN("bool", 6),
    NET_SEND_OBJECT("net_send_object", 7),
    SERIALIZED("serialized", 666);

    private final String name;
    private final int value;

    NetSendObjectType(final String n, final int i){
        name = n;
        value = i;
    }

    public final String getName() {
        return name;
    }

    public final int getValue(){
        return value;
    }

    public static NetSendObjectType parseFromString(String s){
        for (NetSendObjectType candidate : NetSendObjectType.values()){
            if (candidate.name.equalsIgnoreCase(s)){
                return candidate;
            }
        }
        return null;
    }

    public static NetSendObjectType parseFromString(String s, NetSendObjectType def){
        NetSendObjectType temp = parseFromString(s);
        return (temp != null ? temp :  def);
    }
}
