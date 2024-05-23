package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {
    public static void main(String[] args) {
        // Имя файла базы данных
        String url = "jdbc:sqlite:reactors.sqlite";

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

                // Создание таблицы type
                String createTypeTable = "CREATE TABLE IF NOT EXISTS type (" +
                        "type TEXT PRIMARY KEY, " +
                        "burnup DOUBLE" +
                        ")";
                stmt.execute(createTypeTable);

                // Создание таблицы reactor
                String createReactorTable = "CREATE TABLE IF NOT EXISTS reactor (" +
                        "reactorName TEXT PRIMARY KEY, " +
                        "firstgridconnection TEXT, " +
                        "shutdownyear TEXT, " +
                        "country TEXT, " +
                        "owner TEXT, " +
                        "type TEXT, " +
                        "thermalcapacity DOUBLE, " +
                        "FOREIGN KEY (country) REFERENCES country(country), " +
                        "FOREIGN KEY (owner) REFERENCES company(company), " +
                        "FOREIGN KEY (type) REFERENCES type(type)" +
                        ")";
                stmt.execute(createReactorTable);

                // Создание таблицы loadfactor
                String createLoadFactorTable = "CREATE TABLE IF NOT EXISTS loadfactor (" +
                        "reactorname TEXT PRIMARY KEY, " +
                        "`2014` DOUBLE, " +
                        "`2015` DOUBLE, " +
                        "`2016` DOUBLE, " +
                        "`2017` DOUBLE, " +
                        "`2018` DOUBLE, " +
                        "`2019` DOUBLE, " +
                        "`2020` DOUBLE, " +
                        "`2021` DOUBLE, " +
                        "`2022` DOUBLE, " +
                        "`2023` DOUBLE, " +
                        "`2024` DOUBLE, " +
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