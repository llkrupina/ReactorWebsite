package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {
    public static void main(String[] args) {
        // Имя файла базы данных
        String url = "jdbc:sqlite:mylastDBreserve.sqlite";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Создание объекта Statement для выполнения SQL-запросов
                Statement stmt = conn.createStatement();

                // Создание таблицы region
                String createRegionTable = "CREATE TABLE IF NOT EXISTS region (" +
                        "region TEXT PRIMARY KEY" +
                        ")";
                stmt.execute(createRegionTable);

                // Создание таблицы country
                String createCountryTable = "CREATE TABLE IF NOT EXISTS country (" +
                        "country TEXT PRIMARY KEY, " +
                        "region TEXT, " +
                        "FOREIGN KEY (region) REFERENCES region(region)" +
                        ")";
                stmt.execute(createCountryTable);

                // Создание таблицы company
                String createCompanyTable = "CREATE TABLE IF NOT EXISTS company (" +
                        "company TEXT PRIMARY KEY" +
                        ")";
                stmt.execute(createCompanyTable);


                // Создание таблицы reactor
                String createReactorTable = "CREATE TABLE IF NOT EXISTS reactor (" +
                        "reactorName TEXT PRIMARY KEY, " +
                        "firstgridconnection INTEGER, " +
                        "shutdownyear INTEGER, " +
                        "country TEXT, " +
                        "owner TEXT, " +
                        "type TEXT, " +
                        "thermalcapacity DOUBLE, " +
                        "FOREIGN KEY (country) REFERENCES country(country), " +
                        "FOREIGN KEY (owner) REFERENCES company(company) " +
                        ")";
                stmt.execute(createReactorTable);

                // Создание таблицы loadfactor
                String createLoadFactorTable = "CREATE TABLE IF NOT EXISTS loadfactor (" +
                        "reactorname TEXT PRIMARY KEY, " +
                        "year INTEGER, " +
                        "consumption DOUBLE, " +
                        "FOREIGN KEY (reactorname) REFERENCES reactor(reactorName)" +
                        ")";
                stmt.execute(createLoadFactorTable);

                System.out.println("База данных и таблицы успешно созданы.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}