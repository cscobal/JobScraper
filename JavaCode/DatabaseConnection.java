/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
/**
 *
 * @author Chris
 */
public class DatabaseConnection {
    static Connection conn = null;
    static String name = "job_database";
    static String url = "jdbc:mysql://localhost:3306/" + name;
    static String user = "root";
    static String password = "Lasagna_9";
    private static final DatabaseConnection instance = new DatabaseConnection();

    
    private DatabaseConnection()
    {

    }
    
    public Connection connection()
    {
        
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        try{
            conn = DriverManager.getConnection(url, user, password);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return conn;
    }
    
    public static DatabaseConnection getInstance()
    {
        return instance;
    }
    
}
