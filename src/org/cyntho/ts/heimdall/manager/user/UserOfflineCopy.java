package org.cyntho.ts.heimdall.manager.user;

import org.cyntho.ts.heimdall.net.NetSendObject;
import org.cyntho.ts.heimdall.net.streaming.ISendAble;

/**
 * Created by Xida on 14.07.2017.
 */
public class UserOfflineCopy implements ISendAble {

    private String nickname;
    private String uuid;
    private int runtimeId;
    private int clientDatabaseId;
    private String description;
    private TS3User user;

    private UserOfflineCopy(){
    }

    public UserOfflineCopy(TS3User user){
        nickname = user.getNickname();
        uuid = user.getClientUUID();
        runtimeId = user.getRuntimeId();
        clientDatabaseId = user.getClientInfo().getDatabaseId();
        this.user = user;
        this.description = user.getClientInfo().getDescription();
    }


    public String getNickname() { return this.nickname; }
    public String getUUID() { return this.uuid; }
    public String getDescription() { return this.description; }
    public int getRuntimeId() { return this.runtimeId; }
    public int getClientDatabaseId() { return this.clientDatabaseId; }

    public void updateNickname(String val) { this.nickname = val; }

    /* ISendAble implementation */
    public UserOfflineCopy generateFromNetSendObject(NetSendObject sendObject){
        UserOfflineCopy created = new UserOfflineCopy();
        created.nickname = sendObject.getString("nickname");
        created.uuid = sendObject.getString("uuid");
        created.runtimeId = sendObject.getInt("runtimeId");
        created.clientDatabaseId = sendObject.getInt("clientDatabaseId");
        created.user = null;
        return created;
    }

    public NetSendObject createNetSendObject(){
        NetSendObject ret = new NetSendObject();

        ret.setString("nickname", nickname);
        ret.setString("uuid", uuid);
        ret.setInteger("runtimeId", runtimeId);
        ret.setInteger("clientDatabaseId", clientDatabaseId);
        ret.setString("description", description);
        ret.setString("user", "COMMING_SOON");

        return ret;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("\t").append("nickname: ").append(nickname);
        sb.append("\t").append("uuid: ").append(uuid);
        sb.append("\t").append("runtimeId: ").append(runtimeId);
        sb.append("\t").append("clientDatabaseId: ").append(clientDatabaseId);
        sb.append("\t").append("description: ").append(description);
        sb.append("\t").append("user: ").append("comming soon");

        return sb.toString();
    }

}
