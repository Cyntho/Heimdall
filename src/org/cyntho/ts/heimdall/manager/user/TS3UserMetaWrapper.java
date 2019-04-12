package org.cyntho.ts.heimdall.manager.user;


public class TS3UserMetaWrapper {


    private TS3User instance;

    private int runtimeId;

    private String metaIpAddress;
    private String version;
    private String operatingSystem;



    public TS3UserMetaWrapper(){
        instance = null;
    }

    public TS3UserMetaWrapper(TS3User user){
        this.instance = user;
    }



    /* Public GETTER */

    public boolean isInitialized() { return this.instance != null; }
    public TS3User getUser() { return this.instance; }











}
