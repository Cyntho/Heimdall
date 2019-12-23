package org.cyntho.ts.heimdall.manager.user;


import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.util.RC4Crypto;

import java.io.*;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TS3UserDescription implements Serializable {

    /**
     * The password doesn't need to be very secure, just consistent.
     * So we're using a static String, since we won't store sensitive data
     * in the description anyway.
     *
     * TODO: implement encryption
     */


    private final TS3User user;
    public TS3UserDescriptionData data;

    TS3UserDescription(TS3User user) {
        this.user = user;

        String tmp = user.getOfflineCopy().getDescription();

        if (tmp.isEmpty()){
            data = new TS3UserDescriptionData();
        } else {
            data = new TS3UserDescriptionData(tmp);
        }



    }

    private void update(){
        if (this.data != null){
            try {
                System.out.println("runtime: " + this.user.getRuntimeId() );
                Bot.heimdall.getApi().editClient(this.user.getRuntimeId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, this.data.toString()));
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("data null");
        }
    }

    public void clear(){
        try {
            Bot.heimdall.getApi().editClient(this.user.getRuntimeId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, ""));
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void addData(String key, Object data){
        this.data.put(key, data);
        update();
    }

    public void removeData(String key){
        addData(key, null);
    }

    public Object get(String key){
        return this.data.get(key);
    }

    public Map<String, Object> getAll(){
        return this.data.data;
    }



    //------------------------------------------------







}
