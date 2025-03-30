package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
    private Connection con;
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/dbcarometro";
    private String user = "root";
    private String password = "Samuel2304";

    public Connection conectar() {
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);
            return con;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }
}

