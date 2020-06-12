package org.cyntho.ts.heimdall.database;


import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.config.BotConfig;
import org.cyntho.ts.heimdall.exceptions.DatabaseAuthException;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The DatabaseConnector class provides a mysql instance to
 * interact with one database connection.
 * Additionally it provides a method called executeQuery
 * to dynamically create prepared statements
 *
 * @see BotConfig
 * @author  Xida
 * @version 1.0
 */
public class DatabaseConnector {

    private final String dbHost;
    private final String dbUser;
    private final String dbPass;
    private final String dbBase;
    private final String dbPref;
    private final int dbPort;

    private Connection connection = null;
    private boolean initialized = false;

    //TODO: escape prefix
    public DatabaseConnector(BotConfig cfg){
        dbHost = cfg.getDbHost();
        dbPort = cfg.getDbPort();
        dbUser = cfg.getDbUser();
        dbPass = cfg.getDbPass();
        dbBase = cfg.getDbBase();
        dbPref = cfg.getString("database.prefix", "tsb");

        try {
            initialize();
        } catch (DatabaseAuthException e){
            Bot.log(LogLevelType.DATABASE_ERROR, e.getMessage());
        }
    }

    private void initialize() throws DatabaseAuthException {
        try {
            // Load Driver class
            try {
                // Class.forName("com.mysql.jdbc.Driver"); // DEPRECATED
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e){
                Bot.log(LogLevelType.BOT_CRITICAL, "Unable to load mysql driver class!");
                System.exit(1);
            }

            connection = DriverManager.getConnection(getConnectionString(), dbUser, dbPass);

            initialized = true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw new DatabaseAuthException(getConnectionString(), dbUser, dbPass);
        }
    }

    public boolean isInitialized() { return this.initialized; }

    public String getConnectionString() { return ("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbBase + "?user=" + dbUser + "&pass=" + dbPass); }

    protected Connection getConnection() { return this.connection; }

    protected Connection getConnectionInstance() throws SQLException {
        return DriverManager.getConnection(getConnectionString(), dbUser, dbPass);
    }

    public void close() throws SQLException {
        if (this.connection != null){
            connection.close();
        }
    }

    /* Prefixed queries */

    public ResultSet executeQuery(String qry, Object[] args){

        try {

            // Try to replace the prefix
            String prefix = Bot.heimdall.getBotConfig().getString("database.prefix", "tsb");
            if (!prefix.equalsIgnoreCase("")){
                if (qry.contains("prefix")){
                    qry = qry.replace("prefix", prefix);
                }
            }



            PreparedStatement stmt = this.connection.prepareStatement(qry);

            int i = 1;
            for (Object o : args) {

                if (o instanceof Integer) {
                    stmt.setInt(i, Integer.parseInt(o.toString()));
                } else if (o instanceof String){
                    stmt.setString(i, o.toString());
                } else if (o instanceof Boolean){
                    stmt.setBoolean(i, (Boolean) o);
                } else if (o instanceof Long){
                    stmt.setLong(i, Long.parseLong(o.toString()));
                } else if (o instanceof Float){
                    stmt.setFloat(i, (Float) o);
                } else if (o instanceof Double){
                    stmt.setDouble(i, (Double) o);
                } else {
                    System.out.println("Undefined object type : " + o.toString());
                }
                i++;
            }

            return stmt.executeQuery();
        } catch (NumberFormatException e){
            e.printStackTrace();
        } catch (SQLException e){
            Bot.log(LogLevelType.DATABASE_ERROR, "Error while executing query: " + qry);
        }

        return null;
    }

    /**
     * Get a List of Strings containing all columns from the
     * requested table
     * @param table String  The name of the requested database table
     * @param ignore String[] Optional array of Columns that will not be included
     * @return List<String> on success, NULL on failure
     */
    @Deprecated
    public List<String> getDatabaseColumnsOld(String table, String... ignore){
        List<String> columns = new ArrayList<>();

        try {
            String qry = "SELECT * FROM ?";

            Connection con = getConnectionInstance();

            PreparedStatement stmt = con.prepareStatement(qry);
            stmt.setString(1, table);

            ResultSet resultSet = stmt.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 0; i < resultSetMetaData.getColumnCount(); i++){
                String col = resultSetMetaData.getColumnName(i);
                if (!Arrays.asList(ignore).contains(col)){
                    columns.add(col);
                }
            }
            return columns;

        } catch (SQLException e){
            //Bot.log(LogLevelType.DATABASE_ERROR, "Could not resolve columns for database '" + table + "', query was: " + e.getMessage());
            System.out.println(e.getMessage());
        }

        return null;
    }

    public List<String> getDatabaseColumns(DatabaseTables table, String... ignored){

        List<String> columns = new ArrayList<>();

        String qry = String.format("SELECT * FROM %s_%s", this.dbPref, table.getName());

        try {
            Connection con = getConnectionInstance();
            PreparedStatement stmt = con.prepareStatement(qry);

            ResultSet resultSet = stmt.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 1; i < resultSetMetaData.getColumnCount(); i++){
                String col = resultSetMetaData.getColumnName(i);
                if (!Arrays.asList(ignored).contains(col)){
                    columns.add(col);
                }
            }
            return columns;


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }















































}
