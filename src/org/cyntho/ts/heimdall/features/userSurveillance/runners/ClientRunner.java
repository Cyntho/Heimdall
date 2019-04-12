package org.cyntho.ts.heimdall.features.userSurveillance.runners;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.*;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.userSurveillance.UserSurveillanceConfig;
import org.cyntho.ts.heimdall.features.userSurveillance.UserSurveillanceListener;

import java.io.IOException;

public class ClientRunner implements UserSurveillanceBaseRunner {

    private TS3Query query;
    private final String uuid;

    private UserSurveillanceConfig config;
    private UserSurveillanceListener listener;
    private boolean cancelled;


    public ClientRunner(String uuid) throws IOException {
        this.uuid = uuid;
        this.config = new UserSurveillanceConfig();
        cancelled = true;
    }

    @Override
    public void start(){
        cancelled = false;

        final TS3Config cfg = new TS3Config();
        cfg.setHost(Bot.heimdall.getBotConfig().getDbHost());
        cfg.setDebugToFile(false);
        cfg.setCommandTimeout(Bot.heimdall.getBotConfig().getServerCommandTimeout());
        cfg.setFloodRate(Bot.heimdall.getBotConfig().getFloodRate());

        query = new TS3Query(cfg);
        query.connect();

        TS3Api api = query.getApi();

        api.login(config.getString("query.username", "username"), config.getString("query.password", "password"));
        api.selectVirtualServerById(Bot.heimdall.getBotConfig().getServerId());
        api.setNickname(config.getString("query.nickname", "[Bot] NSA"));


        api.registerAllEvents();
        api.addTS3Listeners(new TS3EventAdapter() {
            @Override
            public void onTextMessage(TextMessageEvent e) {
                super.onTextMessage(e);
            }

            @Override
            public void onClientJoin(ClientJoinEvent e) {
                super.onClientJoin(e);
            }

            @Override
            public void onClientLeave(ClientLeaveEvent e) {
                super.onClientLeave(e);
            }

            @Override
            public void onServerEdit(ServerEditedEvent e) {
                super.onServerEdit(e);
            }

            @Override
            public void onChannelEdit(ChannelEditedEvent e) {
                super.onChannelEdit(e);
            }

            @Override
            public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) { super.onChannelDescriptionChanged(e); }

            @Override
            public void onClientMoved(ClientMovedEvent e) {
                super.onClientMoved(e);
            }

            @Override
            public void onChannelCreate(ChannelCreateEvent e) {
                super.onChannelCreate(e);
            }

            @Override
            public void onChannelDeleted(ChannelDeletedEvent e) {
                super.onChannelDeleted(e);
            }

            @Override
            public void onChannelMoved(ChannelMovedEvent e) {
                super.onChannelMoved(e);
            }

            @Override
            public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {
                super.onChannelPasswordChanged(e);
            }

            @Override
            public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {
                super.onPrivilegeKeyUsed(e);
            }
        });

    }

    @Override
    public void stop(){
        cancelled = true;
    }
}
