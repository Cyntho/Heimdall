package org.cyntho.ts.heimdall.features.userHistory;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.events.UserHistoryListener;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;
import org.cyntho.ts.heimdall.manager.user.TS3User;

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

            super.active = true;
            Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been activated.");
        } else {
            Bot.log(LogLevelType.BOT_ERROR, "Could not activate UserHistoryFeature, since it's already running!");
        }
    }

    @Override
    public void deactivate() {
        if (super.active){

            try {
                Bot.heimdall.getApi().removeTS3Listeners(listener);
            } catch (UnsupportedOperationException e){
                System.out.println("Caught bug error.");
            }

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
            Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been deactivated.");
        } else {
            Bot.log(LogLevelType.BOT_ERROR, "Could not deactivate UserHistoryFeature, since it's not running!");
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

            String qry = "SELECT COUNT(*) as TOTAL FROM prefix_user_history_login WHERE uid = ? AND logout = 0";

            ResultSet rs = Bot.heimdall.getDb().executeQuery(qry, new String[]{user.getClientUUID()});

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

                String q = "SELECT * FROM prefix_user_history_login WHERE uid = ? AND LOGOUT = 0 LIMIT 1";

                ResultSet resultSet = Bot.heimdall.getDb().executeQuery(q, new String[]{user.getClientUUID()});

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
                    q = "UPDATE prefix_user_history_login SET logout = -1 WHERE uid = ? AND logout = 0";
                    Bot.heimdall.getDb().executeQuery(q, new String[]{user.getClientUUID()});

                    // We 'repaired' the database, so we can now save the user as a new one
                    insertLoginData(user);
                    updateNicknameAlias(user);
                }

            } else {
                //  The counter remained equals -1 or it's > 1
                // So there was an error with our query (or we fucked up in the past o_O)
                Bot.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::handleLogin(TS3User)] Could not update the database after restart/crash");
            }


        } catch (SQLException ex){
            Bot.log(LogLevelType.DATABASE_ERROR, ex.getMessage());
        }
    }

    private void insertLoginData(TS3User user){

        // Save meta data
        try {
            String sql = "INSERT INTO prefix_user_history_login (uid, login, ip, version, connections, os, country) VALUES (?, ?, ?, ?, ?, ?, ?)";

            Bot.heimdall.getDb().executeQuery(sql, new Object[]{
                    user.getClientUUID(),
                    user.getLoginDate(),
                    user.getMetaIpAddress(),
                    user.getMetaVersion(),
                    user.getMetaConnections(),
                    user.getMetaOperatingSystem(),
                    user.getMetaCountry()
            });

        } catch (Exception ex){
            Bot.log(LogLevelType.DATABASE_ERROR, "SQL-Exception: " + ex.getMessage());
        }
    }

    public void updateNicknameAlias(TS3User user){
        try {

            // First, check if the user's nickname has already been used before
            String sqlCount = "SELECT COUNT(*) as TOTAL FROM prefix_user_history_nickname WHERE (uuid = ? AND (name = ? OR name_sc = ?))";

            ResultSet resCount = Bot.heimdall.getDb().executeQuery(sqlCount, new Object[]{
                    user.getClientUUID(),
                    user.getOfflineCopy().getNickname(),
                    user.getOfflineCopy().getNickname().toLowerCase()
            });

            int c = 0;
            while (resCount.next()){
                c = resCount.getInt("TOTAL");
            }

            if (c == 0){
                // If its a new one, insert it
                String sqlInsert = "INSERT INTO prefix_user_history_nickname (uuid, name, name_sc) VALUES (?, ?, ?)";

                Bot.heimdall.getDb().executeQuery(sqlInsert, new Object[]{
                        user.getClientUUID(),
                        user.getOfflineCopy().getNickname(),
                        user.getOfflineCopy().getNickname().toLowerCase()
                });

            } else {
                // Its not a new one. Increase counter
                String sqlUpdate = "UPDATE prefix_user_history_nickname SET counter = counter + 1 WHERE (uuid = ? AND name_sc = ?)";

                Bot.heimdall.getDb().executeQuery(sqlUpdate, new Object[]{
                        user.getClientUUID(),
                        user.getOfflineCopy().getNickname().toLowerCase()
                });
            }
        } catch (SQLException ex){
            Bot.log(LogLevelType.DATABASE_ERROR, ex.getMessage());
        }
    }



    public void handleLogout(TS3User user){

        try {
            String sql = "UPDATE prefix_user_history_login SET logout = ? WHERE uid = ? AND login = ?";

            Bot.heimdall.getDb().executeQuery(sql, new Object[]{
                    new Date().getTime(),
                    user.getClientUUID(),
                    user.getLoginDate()
            });
        } catch (Exception ex){
            Bot.log(LogLevelType.DATABASE_ERROR, "[UserHistoryFeature::Logout] Error updating database");
            ex.printStackTrace();
        }

    }
}
