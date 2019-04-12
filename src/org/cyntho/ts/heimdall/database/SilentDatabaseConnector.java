package org.cyntho.ts.heimdall.database;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.config.BotConfig;

import java.io.File;
import java.sql.*;

public class SilentDatabaseConnector {

    private BotConfig config;
    private int serverID;
    private Connection conn;

    private boolean initialized = false;

    public SilentDatabaseConnector(BotConfig cfg){
        this.config = cfg;
        init();
    }

    private void init(){

        // Set server's id
        this.serverID = this.config.getServerId();


        // Try to localize sql file
        String fileLocation = this.config.getString("database.local", "../ts3server.sqlitedb");

        File sFile = new File(fileLocation);
        if (!sFile.exists()) return;

        String url = "jdbc:sqlite:" + fileLocation;

        try {
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("SilentDatabaseConnector: Cannot access local ts3server.sqlitedb file");
            return;
        }
        this.initialized = true;
        System.out.println("initialized should be true");
    }


    public boolean updateChannelDescription(int channelID, String description){

        if (!initialized) return false;

        Channel targetChannel = null;

        for (Channel channel : Bot.heimdall.getApi().getChannels()){
            if (channel.getId() == channelID){
                targetChannel = channel;
                break;
            }
        }

        System.out.println("channel found");

        if (targetChannel == null) return false;

        try {
            int rows = SilentDatabaseExecutor.executeUpdateChannelDescription(this, targetChannel.getId(), description);

            System.out.println("rows affected: " + rows);
            return true;
        } catch (NullPointerException n){
            System.out.println("Error updating silently. Is the server running? " + n.getMessage());
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
        }

        return false;
    }



    private final static class SilentDatabaseExecutor {

        static int executeUpdateChannelDescription(SilentDatabaseConnector connector, int id, String value) throws NullPointerException, SQLException {

            String sql = "UPDATE `channel_properties` SET `value` = ? WHERE (`ident` = ? AND `server_id` = ? AND id = ?)";


            PreparedStatement stmt = connector.conn.prepareStatement(sql);
            stmt.setString(1, value);
            stmt.setString(2, "channel_description");
            stmt.setInt(3, connector.serverID);
            stmt.setInt(4, id);


            System.out.println("Query: " + sql);
            System.out.println("1: " + connector.serverID);
            System.out.println("2: " + value);
            System.out.println("3: " + "channel_description");
            System.out.println("4: " + id);

            return stmt.executeUpdate();
        }


    }





}
