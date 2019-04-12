package org.cyntho.ts.heimdall.manager.user;

import java.util.List;

/**
 * Created by Xida on 14.07.2017.
 */
public class UserInstance {

    private final int runtimeId;
    private final long loginDate;
    private int currentChannelId;

    private UserInstance parent;
    private List<UserInstance> children;

    public UserInstance(int runtimeId){
        this.runtimeId = runtimeId;
        this.loginDate = System.currentTimeMillis();
    }

    // Public GETTER
    public int getRuntimeId() { return this.runtimeId; }
    public int getCurrentChannelId() { return this.currentChannelId; }
    public long getLoginDate() { return this.loginDate; }


    // Public SETTER
    public void setCurrentChannelId(int val) { this.currentChannelId = val; }

}
