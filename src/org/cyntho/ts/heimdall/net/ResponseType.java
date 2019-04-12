package org.cyntho.ts.heimdall.net;

public enum ResponseType {

    CONNECTION ("Connection", "con", 0),
    HEARTBEAT("HeartBeat", "hb", 0),

    NOTIFICATION("Notification", "notify", 1),
    INFORMATION("Information", "info", 2),
    WARNING("Warning", "warn", 3),

    ERROR("Error", "err", 4);

    private final String type, prefix;
    private final int value;

    ResponseType(String t, String p, int v){
        type = t;
        prefix = p;
        value = v;
    }

    // Public GETs
    public String getType()   { return type; }
    public String getPrefix() { return prefix; }
    public int getValue()     { return value;}

    // Comparing and stuff

    public boolean equalsTo(ResponseType other) {
        return this.value == other.value;
    }

    public static ResponseType parseFromStringType(String t){
        for (ResponseType candidate : ResponseType.values()){
            if (candidate.getType().equalsIgnoreCase(t)){
                return candidate;
            }
        }
        return null;
    }

    public static ResponseType parseFromStringType(String t, ResponseType def){
        ResponseType temp = parseFromStringType(t);
        return (temp != null ? temp : def);
    }

    public static ResponseType parseFromStringPrefix(String p){
        for (ResponseType candidate : ResponseType.values()){
            if (candidate.getPrefix().equalsIgnoreCase(p)){
                return candidate;
            }
        }
        return null;
    }

    public static ResponseType parseFromStringPrefix(String p, ResponseType def){
        ResponseType temp = parseFromStringPrefix(p);
        return (temp != null ? temp : def);
    }

}
