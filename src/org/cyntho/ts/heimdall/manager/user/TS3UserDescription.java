package org.cyntho.ts.heimdall.manager.user;


import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import org.cyntho.ts.heimdall.app.Bot;

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
    private final static String pw = "43a136a6-2bdb-4e9e-85f9-bc755699ac21";

    private final TS3User user;
    private TS3UserDescriptionData data;

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
                Bot.heimdall.getApi().editClient(this.user.getRuntimeId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, this.data.toString()));
            } catch (NullPointerException e){
                e.printStackTrace();
            }
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

    public final class TS3UserDescriptionData implements Serializable {

        private final static long serialVersionUID = 2;

        Map<String, Object> data;

        private boolean registered;
        private long registerDate;

        private String nameBoard;



        TS3UserDescriptionData(){
            data = new HashMap<>();
        }

        TS3UserDescriptionData(String base){
            data = new HashMap<>();

            try {

                byte[] b = Base64.getDecoder().decode(base);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));

                Object o = ois.readObject();
                ois.close();

                if (o instanceof Map){
                    this.data = (Map<String, Object>) o;
                } else {
                    System.out.println("error casting object to map");
                }

            } catch (Exception e) {
                System.out.println("error while casting user description");

            }
        }


        public Map<String, Object> getMap(){
            return this.data;
        }

        public void put(String key, Object value) {
            this.data.put(key, value);
        }

        public Object get(String key){
            return this.data.get(key);
        }

        @Override
        public String toString(){

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(stream);

                oos.writeObject(this);
                oos.close();

                return Base64.getEncoder().encodeToString(stream.toByteArray());

            } catch (IOException e){
                e.printStackTrace();
            }
            return "";
        }

    }





}
