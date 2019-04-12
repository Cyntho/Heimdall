package org.cyntho.ts.heimdall.net;

public final class NetSendObjectContent {

    private final NetSendObjectType type;
    private final Object value;

    NetSendObjectContent(NetSendObjectType t, Object o){
        type = t;
        value = o;
    }

    public final NetSendObjectType getType() {
        return type;
    }

    public final Object getValue() {
        return value;
    }
}
