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

    public DBClass() throws SQLException, ClassNotFoundException {

        Class.forName("com.mysql.jdbc.Driver");

        this.connection= DriverManager.getConnection(
                "jdbc:mysql://remotemysql.com:3306/Fn3FS6KEOn?characterEncoding=utf8","Fn3FS6KEOn","vX4XLO6VR3"
        );


        this.statement=connection.createStatement();

        ResultSet set=statement.executeQuery("SELECT value FROM settings WHERE id_setting=1");
        set.first();
        this.serverHTTP=set.getString("value");
        System.out.println(this.serverHTTP);

    }

    public String getBeaconURL(String id_beacon) throws SQLException {

        ResultSet set=this.statement.executeQuery("SELECT url FROM beacon_map WHERE id_beacon='"+id_beacon+"'");

        set.first();

        System.out.println(set.getString("url"));

        return this.serverHTTP+set.getString("url");

    }

    public void close(){
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
