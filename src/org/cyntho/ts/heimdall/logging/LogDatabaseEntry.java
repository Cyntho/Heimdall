package org.cyntho.ts.heimdall.logging;

import org.cyntho.ts.heimdall.app.Bot;

import java.sql.ResultSet;
import java.util.Date;

public class LogDatabaseEntry {


    public static void log(int server,
                           int type,
                           int invokerID,
                           int targetID,
                           String invokerUUID,
                           String targetUUID,
                           String msg){


        String sql = "INSERT INTO prefix_log_entries (server_id, log_type, invoker_id, target_id, invoker_uuid, target_uuid, loggedOn, message) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] params = {server, type, invokerID, targetID, invokerUUID, targetUUID, new Date().getTime(), msg };


        ResultSet resultSet = Bot.heimdall.getDb().executeQuery(sql, params);

        if (resultSet != null) {
            Bot.heimdall.log(LogLevelType.DBG, "Added log entry to Database.");
        } else {
            String prepared = String.format("INSERT INTO prefix_log_entries (server_id, log_type, invoker_id, target_id, " +
                    "invoker_uuid, target_uuid, loggedOn, message) VALUES (%d, %d, %d, %d, %s, %s, %d, %s)",
                    server, type, invokerID, targetID, invokerUUID, targetUUID, new Date().getTime(), msg);

            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, prepared);
        }

    }











}
