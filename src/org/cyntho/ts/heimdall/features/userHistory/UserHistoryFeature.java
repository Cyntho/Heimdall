package org.cyntho.ts.heimdall.features.userHistory;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.events.UserHistoryListener;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


/**
 * @author  Xida
 * @version 1.0
 *
 * A TS-Bot feature to monitor some user activity.
 * Keeps track on who logged in when and for how long,
 * the client's IP Address, Teamspeak-Client version,
 * operating system, total connection counter and country code.
 */
public class UserHistoryFeature extends BaseFeature {

    private UserHistoryListener listener;

    public UserHistoryFeature(){
        super("UserHistoryFeature");
    }

    public Long getId() { return 0x001L; }

    @Override
    public void activate() {
        if (!super.active){

            listener = new UserHistoryListener(this);

            Bot.heimdall.getApi().addTS3Listeners(listener);
            Bot.heimdall.getApi().registerAllEvents();

            super.active = true;
            Bot.heimdall.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been activated.");
        } else {
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not activate UserHistoryFeature, since it's already running!");
        }
    }

    @Override
    public void deactivate() {
        if (super.active){

            Bot.heimdall.getApi().removeTS3Listeners(listener);
            listener = null;

            // Write the current timestamp into the database for each user
            // To prevent the row 'tsb_user_history_list.logout' to be 0
            List<TS3User> users = Bot.heimdall.getUserManager().getUserList();

            if (users != null){
                for (TS3User user : users){
                    // Skip server query clients, since those won't be registered here anyway
                    if (!user.isServerQuery()){
                        handleLogout(user);
                    }
                }
            }

            super.active = false;
            Bot.heimdall.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been deactivated.");
        } else {
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not deactivate UserHistoryFeature, since it's not running!");
        }

    }



    // Feature specific functions


    /**
     * Handles the login, if this features is active
     * Updates the database and stores some meta data about
     * the logging in user. Also keeps track of the nickname
     * and how often this specific user used this nickname before
     *
     * @see     UserHistoryFeature
     * @param   user  TS3User Object that needs to be handled
     */
    public void handleLogin(TS3User user){

        // Since this function will also be called on startUp
        // We need to determine whether the user was already online before the bot started or not
        // If so, we've to check the current 'status of registration'
        // There are two cases we need to check for:
        //
        // a) The Bot has been offline for a while, so the current user is 'new'
        // b) The Bot just restarted, so the 'user' might have been registered before
        //    see tsb_user_history_login.login != 0 AND tsb_user_history.logout == 0



        try {
            /*
             * To check for a) and b) we simply count the rows in tsb_user_history_login
             * that have a login time set, but no logout.
             * If the counter equals 0, the user is new. If it equals 1 it has been registered
             * before and we need to check if we've to update the login time and whether we've
             * to update the parent-child instance relationship.
             */

            String qry = "SELECT COUNT(*) as TOTAL FROM tsb_user_history_login WHERE uid = ? AND logout = 0";
            PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(qry);
            stmt.setString(1, user.getClientUUID());

            ResultSet rs = stmt.executeQuery();
            int counter = -1;

            while (rs.next()){
                counter = rs.getInt("TOTAL");
            }


            if (counter == 0){
                // The user is new
                insertLoginData(user);
                updateNicknameAlias(user);

            } else if (counter == 1){
                // Check if the user needs to be updated

                String q = "SELECT * FROM tsb_user_history_login WHERE uid = ? AND LOGOUT = 0 LIMIT 1";
                PreparedStatement statement = Bot.heimdall.getDb().getConnection().prepareStatement(q);
                statement.setString(1, user.getClientUUID());

                ResultSet resultSet = statement.executeQuery();

                long userSet = 0;
                long dbSet = 0;

                while (resultSet.next()){

                    userSet = user.getLoginDate();
                    dbSet = resultSet.getLong("login");
                }



                // Determine the differences between the value saved in the Database and the time set
                // in our TS3User object.
                // Too big to be valid:     diff >= 10 hours
                // Needs to be updated:     diff <  10 hours

                long diff = Math.abs(dbSet - userSet);

                System.out.println("userSet: " + userSet + "\tdbSet: " + dbSet + "\tDifference: " + diff);

                if (diff <= (1000 * 60 * 60 * 10)){
                    user.setLoginDate(dbSet);
                } else {
                    // The database entry is outdated
                    q = "UPDATE tsb_user_history_login SET logout = -1 WHERE uid = ? AND logout = 0";
                    statement = Bot.heimdall.getDb().getConnection().prepareStatement(q);
                    statement.setString(1, user.getClientUUID());

                    if (statement.executeUpdate() <= 0){
                        Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "Could not repair tsb_user_history_login after restart");
                    }

                    // We 'repaired' the database, so we can now save the user as a new one
                    insertLoginData(user);
                    updateNicknameAlias(user);
                }

            } else {
                //  The counter remained equals -1 or it's > 1
                // So there was an error with our query (or we fucked up in the past o_O)
                Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::handleLogin(TS3User)] Could not update the database after restart/crash");
            }


        } catch (SQLException ex){
            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, ex.getMessage());
        }
    }

    private void insertLoginData(TS3User user){

        // Save meta data
        try {
            String sql = "INSERT INTO tsb_user_history_login (uid, login, ip, version, connections, os, country) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(sql);


            stmt.setString(1, user.getClientUUID());
            stmt.setLong(2, user.getLoginDate());
            stmt.setString(3, user.getMetaIpAddress());
            stmt.setString(4, user.getMetaVersion());
            stmt.setInt(5, user.getMetaConnections());
            stmt.setString(6, user.getMetaOperatingSystem());
            stmt.setString(7, user.getMetaCountry());

            if (stmt.executeUpdate() <= 0){
                Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "Error inserting login meta data");
            }

        } catch (SQLException ex){
            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "SQL-Exception: " + ex.getMessage());
        }
    }

    public void updateNicknameAlias(TS3User user){
        try {

            // First, check if the user's nickname has already been used before
            String sqlCount = "SELECT COUNT(*) as TOTAL FROM tsb_user_history_nickname WHERE (uuid = ? AND (name = ? OR name_sc = ?))";
            PreparedStatement stmtCount = Bot.heimdall.getDb().getConnection().prepareStatement(sqlCount);

            stmtCount.setString(1, user.getClientUUID());
            stmtCount.setString(2, user.getOfflineCopy().getNickname());
            stmtCount.setString(3, user.getOfflineCopy().getNickname().toLowerCase());

            ResultSet resCount = stmtCount.executeQuery();

            int c = 0;
            while (resCount.next()){
                c = resCount.getInt("TOTAL");
            }

            if (c == 0){
                // If its a new one, insert it
                String sqlInsert = "INSERT INTO tsb_user_history_nickname (uuid, name, name_sc) VALUES (?, ?, ?)";
                PreparedStatement stmtInsert = Bot.heimdall.getDb().getConnection().prepareStatement(sqlInsert);

                stmtInsert.setString(1, user.getClientUUID());
                stmtInsert.setString(2, user.getOfflineCopy().getNickname());
                stmtInsert.setString(3, user.getOfflineCopy().getNickname().toLowerCase());

                if (stmtInsert.executeUpdate() <= 0){
                    Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::updateNicknameAlias(TS3User)] Error inserting meta data into database");
                }
            } else {
                // Its not a new one. Increase counter
                String sqlUpdate = "UPDATE tsb_user_history_nickname SET counter = counter + 1 WHERE (uuid = ? AND name_sc = ?)";
                PreparedStatement stmtUpdate = Bot.heimdall.getDb().getConnection().prepareStatement(sqlUpdate);

                stmtUpdate.setString(1, user.getClientUUID());
                stmtUpdate.setString(2, user.getOfflineCopy().getNickname().toLowerCase());

                if (stmtUpdate.executeUpdate() <= 0){
                    Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::updateNicknameAlias(TS3User)] Error updating database");
                }
            }


        } catch (SQLException ex){
            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, ex.getMessage());
        }
    }



    public void handleLogout(TS3User user){

        try {
            String sql = "UPDATE tsb_user_history_login SET logout = ? WHERE uid = ? AND login = ?";
            PreparedStatement stmt = Bot.heimdall.getDb().getConnection().prepareStatement(sql);

            stmt.setLong(1, new Date().getTime());
            stmt.setString(2, user.getClientUUID());
            stmt.setLong(3, user.getLoginDate());

            if (stmt.executeUpdate() <= 0){
                Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::Logout] Unable to update Database for uuid=" + user.getClientUUID());
            }

        } catch (SQLException ex){
            Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::Logout] Error updating database");
            ex.printStackTrace();
        }

    }
}
