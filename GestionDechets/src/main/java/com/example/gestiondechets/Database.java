package com.example.gestiondechets;


import java.sql.Connection;
import java.sql.DriverManager;


public class Database {

    public static Connection connectDB()
    {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_dechets","root","45689001h");

            if (connect != null) {
                System.out.println("Connected to the database successfully");
            }
            return connect;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
