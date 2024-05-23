package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseHandler {
    private static final String URL = "jdbc:sqlite:reactors.db";

    public Connection getDbConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}