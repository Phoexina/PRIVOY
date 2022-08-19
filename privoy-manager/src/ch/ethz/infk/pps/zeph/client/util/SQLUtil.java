package ch.ethz.infk.pps.zeph.client.util;

import java.sql.*;

public class SQLUtil {
    public static Statement statement;
    public static Connection connection;
    static{
        try {
            Driver driver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/privoy",
                    "root","174@PriVoy");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void closeSQL() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null){
            connection.close();
        }
    }
}
