package org.cyntho.ts.heimdall.manager.user;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.util.RC4Crypto;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class TS3UserDescriptionData implements Serializable {

    private final static long serialVersionUID = 2;
    private final static String pw = "43a136a6-2bdb-4e9e-85f9-bc755699ac21";

    Map<String, Object> data;


    TS3UserDescriptionData(){
        data = new HashMap<>();
    }

    TS3UserDescriptionData(String base){
        data = new HashMap<>();
        String plain;

        try {
            plain = RC4Crypto.decrypt(base.getBytes(), pw);
        } catch (Exception e){
            return;
        }

        try {

            byte[] b = Base64.getDecoder().decode(plain);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));

            Object o = ois.readObject();
            ois.close();

            if (o instanceof Map){
                this.data = (Map<String, Object>) o;
            } else {
                Bot.log(LogLevelType.BOT_ERROR, "error casting object to map");
            }

        } catch (Exception e) {
            Bot.log(LogLevelType.BOT_ERROR,"error while casting user description");
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

            String t= Base64.getEncoder().encodeToString(RC4Crypto.encrypt(stream.toString(), pw));
            System.out.println(t);
            return t;

        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

}