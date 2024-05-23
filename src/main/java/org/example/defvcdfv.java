package org.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;

import org.sqlite.SQLiteException;

import java.io.IOException;
import java.sql.*;



public class defvcdfv {
    private static final String URL = "jdbc:mysql://localhost:3306/reactordatabaselast";
    private static final String USERNAME = "newuser";
    private static final String PASSWORD = "Connection12345";
    public static void main(String[] args) throws SQLException, IOException, ParseException {
        String url = "jdbc:sqlite:reactors.sqlite";
        Connection connection = DriverManager.getConnection(url);

        String insertQuery = "INSERT INTO region (region) VALUES (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM country");

            while (rs.next()) {

                String region = rs.getString("region");


                System.out.println( region);


                preparedStatement.setString(1, region);

                try {
                    preparedStatement.executeUpdate();
                } catch (SQLiteException e){

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println( "Ошибка " );
        }



        preparedStatement.close();
        connection.close();
    }
}
