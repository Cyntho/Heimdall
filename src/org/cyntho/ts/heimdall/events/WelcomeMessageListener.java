package org.cyntho.ts.heimdall.events;

import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.features.welcomeMessages.WelcomeMessage;
import org.cyntho.ts.heimdall.features.welcomeMessages.WelcomeMessageFeature;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Xida on 14.07.2017.
 */
public class WelcomeMessageListener implements TS3Listener {

    private final WelcomeMessageFeature feature;
    public WelcomeMessageListener(WelcomeMessageFeature f){
        feature = f;
    }

    public void onClientJoin(ClientJoinEvent clientJoinEvent) {

        if(feature.isActive()){

            // Get welcome messages from Database
            DatabaseConnector db = Bot.heimdall.getDb();

            String qry = "SELECT * FROM prefix_wm_list WHERE active = ?";
            Object[] params = {1};

            ResultSet res = db.executeQuery(qry, params);

            TS3User invoker = Bot.heimdall.getUserManager().getUserByRuntimeId(clientJoinEvent.getClientId());

            if (invoker == null){
                return;
            }

            try {

                while (res.next()){
                    WelcomeMessage msg = new WelcomeMessage(invoker);

                    boolean shouldDisplay = false;
                    int refTyp = res.getInt("refType");
                    Object refValue = null;

                    ClientInfo clientInfo = invoker.getClientInfo();


                    if (clientInfo != null){

                        if (refTyp == 0){

                            refValue = res.getInt("refValue");
                            for (Integer i : clientInfo.getServerGroups()){
                                if (i == refValue){
                                    shouldDisplay = true;
                                    break;
                                }
                            }
                        } else if (refTyp == 2){
                            refValue = res.getString("refValue");
                            if (refValue.toString().equalsIgnoreCase(invoker.getClientUUID())){
                                shouldDisplay = true;
                            }
                        } else {
                            return;
                        }
                    }

                    if (!shouldDisplay){
                        return;
                    }


                    msg.setId(res.getInt("id"));
                    msg.setRefType(refTyp);
                    msg.setRefValue(refValue);
                    msg.setName(res.getString("name"));
                    msg.setDescription(res.getString("description"));
                    msg.setMsgPoke(res.getString("msgPoke"));
                    msg.setMsgServer(res.getString("msgServer"));
                    msg.setMsgChannel(res.getString("msgChannel"));
                    msg.setMsgPrivate(res.getString("msgPrivate"));
                    msg.setPoke(res.getBoolean("isPoke"));
                    msg.setServer(res.getBoolean("isServer"));
                    msg.setChannel(res.getBoolean("isChannel"));
                    msg.setPrivate(res.getBoolean("isPrivate"));
                    msg.setCreatedBy(res.getInt("createdBy"));
                    msg.setCreatedOn(res.getLong("createdOn"));
                    msg.setLimitType(res.getInt("limitType"));
                    msg.setLimitation(res.getLong("limitation"));
                    msg.setActive(res.getBoolean("active"));

                    msg.display();
                }

            } catch (SQLException e){
                e.printStackTrace();
            }

        }

    }


    // Unused events
    public void onTextMessage(TextMessageEvent textMessageEvent) {}
    public void onClientLeave(ClientLeaveEvent clientLeaveEvent) {}
    public void onServerEdit(ServerEditedEvent serverEditedEvent) {}
    public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}
    public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}
    public void onClientMoved(ClientMovedEvent clientMovedEvent) {}
    public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {}
    public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {}
    public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {}
    public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {}
    public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent privilegeKeyUsedEvent) {}
}