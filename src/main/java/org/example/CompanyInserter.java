package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class CompanyInserter {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:mylastDBreserve.sqlite";

        String selectOwnersQuery = "SELECT DISTINCT owner FROM reactor";
        String insertCompanyQuery = "INSERT INTO company (company) VALUES (?)";

        try (Connection connection = DriverManager.getConnection(url);
             Statement selectStatement = connection.createStatement();
             ResultSet resultSet = selectStatement.executeQuery(selectOwnersQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertCompanyQuery)) {

            Set<String> companies = new HashSet<>();

            // Извлечение всех уникальных компаний из столбца owner
            while (resultSet.next()) {
                String owner = resultSet.getString("owner");
                companies.add(owner);
            }

            // Вставка компаний в таблицу company
            for (String company : companies) {
                insertStatement.setString(1, company);
                insertStatement.executeUpdate();
            }

            System.out.println("Все компании успешно вставлены в таблицу company.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
