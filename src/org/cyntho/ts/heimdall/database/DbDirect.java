package org.cyntho.ts.heimdall.database;

import java.sql.*;

public class DbDirect {


    private Connection connect(){


        String url  = "jdbc:sqlite:D://tools/Teamspeak-Server-3.0.13.8/ts3server.sqlitedb";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void printUsers(){

        String sql = "SELECT * FROM channels";

        try (Connection conn = this.connect()){

            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);

            ResultSetMetaData meta = resultSet.getMetaData();

            for (int i = 1; i < meta.getColumnCount(); i++){
                System.out.println("Column: " + i + " = " + meta.getColumnName(i));
            }

        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

}
