package org.cyntho.ts.heimdall.features.welcomeMessages;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.manager.user.TS3User;
import org.cyntho.ts.heimdall.util.PlaceHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import static org.cyntho.ts.heimdall.logging.LogLevelType.DBG;

/**
 * Created by Xida on 14.07.2017.
 */
public class WelcomeMessage {

    private int id;
    private int refType;
    private Object refValue;

    private String name, descr, msgPoke, msgServer, msgChannel, msgPrivate;
    private boolean isPoke, isServer, isChannel, isPrivate, isActive;

    private int createdBy, limitType;
    private long createdOn, limitation;

    private TS3User user;

    public WelcomeMessage(TS3User user){
        this.user = user;
    }


    /* Public SETTER */
    public void setId(int id) {
        this.id = id;
    }
    public void setRefType(int refType) {
        this.refType = refType;
    }
    public void setRefValue(Object refValue) {
        this.refValue = refValue;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.descr = description;
    }
    public void setMsgPoke(String msgPoke) {
        this.msgPoke = msgPoke;
    }
    public void setMsgServer(String msgServer) {
        this.msgServer = msgServer;
    }
    public void setMsgChannel(String msgChannel) {
        this.msgChannel = msgChannel;
    }
    public void setMsgPrivate(String msgPrivate) {
        this.msgPrivate = msgPrivate;
    }
    public void setPoke(boolean poke) {
        isPoke = poke;
    }
    public void setServer(boolean server) {
        isServer = server;
    }
    public void setChannel(boolean channel) {
        isChannel = channel;
    }
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }
    public void setLimitType(int limitType) {
        this.limitType = limitType;
    }
    public void setLimitation(long limitation) {
        this.limitation = limitation;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString(){

        String t = "id:" + id;
        t += " refType:" + refType;
        t += " refValue:" + refValue;
        t += " limitType:" + limitType;
        t += " limitation:" + limitation;
        t += " name:" + name;
        t += " description:" + descr;
        t += " msgPoke:" + msgPoke;
        t += " msgServer:" + msgServer;
        t += " msgPrivate:" + msgPrivate;
        t += " isPoke:" + isPoke;
        t += " isServer:" + isServer;
        t += " isPrivate:" + isPrivate;
        t += " isActive:" + isActive;

        return t;
    }

    /* Display this message */

    public void display(){
        if (!isActive){
            System.out.println("Not displaying msg, since message inactive!");
            return;
        } else {
            System.out.println("Displaying msg..");

            System.out.println(this.toString());
        }

        if (limitType == 1){

            if (limitation >= 0){
                DatabaseConnector db = new DatabaseConnector(Bot.heimdall.getBotConfig());

                String qry = "SELECT * FROM tsb_wm_ref WHERE msgid = ? AND uuid = ? LIMIT 1";

                try {
                    PreparedStatement stmt = db.getConnection().prepareStatement(qry);
                    stmt.setInt(1, this.id);
                    stmt.setString(2, user.getClientUUID());

                    ResultSet res = stmt.executeQuery();

                    while (res.next()){
                        int remaining = res.getInt("remaining");
                        remaining--;

                        PreparedStatement statement;

                        if (remaining > 0){
                            statement = db.getConnection().prepareStatement("UPDATE tsb_wm_ref SET remaining = ? WHERE msgid = ? AND uuid = ?");
                            statement.setInt(1, remaining);
                            statement.setInt(2, this.id);
                            statement.setString(3, user.getClientUUID());
                        } else {
                            statement = db.getConnection().prepareStatement("DELETE FROM tsb_wm_ref WHERE msgid = ? AND uuid = ?");
                            statement.setInt(1, this.id);
                            statement.setString(2, user.getClientUUID());

                            Bot.log(DBG, "Welcome-Message expired (remaining = 0) for user " + user.getClientInfo().getNickname() + " [UUID=" +
                                    user.getClientUUID() + "] for msg id: " + this.id);
                            statement.execute();
                        }
                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try { db.close(); } catch (Exception ex) { /* Ignore */ }
                }
            }
        } else if (limitType == 2){
            // Duration

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                long validUntil = this.limitation;
                long now = new java.util.Date().getTime();

                if ((now - validUntil) > 0){
                    Bot.log(DBG, "Welcome message expired! ID: " +this.id);
                    return;
                } else {
                    // TODO: Log validity
                }
            } catch (Exception e){
                // TODO
            }
        }

        if (isPoke){
            msgPoke = PlaceHolder.handleUserPlaceholder(user, msgPoke);

            if (msgPoke.contains("\\n")){
                String[] messages = msgPoke.split(Pattern.quote("\\n"));
                for (String msg : messages){
                    user.poke(msg);
                }
            } else {
                user.poke(msgPoke);
            }
        }


        if (isServer){
            msgServer = PlaceHolder.handleUserPlaceholder(user, msgServer);

            if (msgServer.contains("\\n")){
                String[] messages = msgServer.split(Pattern.quote("\\n"));
                for (String msg : messages){
                    Bot.heimdall.getApi().sendServerMessage(msg);
                }
            } else {
                Bot.heimdall.getApi().sendServerMessage(msgServer);
            }
        }


        if (isChannel){
            // TODO: Unsupported at the moment
        }

        if (isPrivate){
            msgPrivate = PlaceHolder.handleUserPlaceholder(user, msgPrivate);
            user.sendPrivateMessage(msgPrivate);
        }


    }


}
