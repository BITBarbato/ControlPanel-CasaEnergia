package com.leviponti.tourcasaenergia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBClass {

    private Connection connection;
    private Statement statement;
    private String serverHTTP;

    public DBClass() throws SQLException {

        this.connection= DriverManager.getConnection(

                "jdbc:mysql://192.168.88.213:3306/casaenergia","root",""
        );
            this.statement=connection.createStatement();
            ResultSet set=statement.executeQuery("SELECT value FROM settings WHERE id_setting=1");
            this.serverHTTP=set.getString("value");
    }

    public String getBeaconURL(String id_beacon) throws SQLException {

        ResultSet set=this.statement.executeQuery("SELECT url FROM beacon_map WHERE id_beacon="+id_beacon);

        return this.serverHTTP+set.getString("url");

    }
}
