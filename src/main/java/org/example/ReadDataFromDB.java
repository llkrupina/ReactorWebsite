package org.example;

import org.sqlite.SQLiteException;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadDataFromDB {

    public static void calculateConsumption(Connection conn, ReactorHolder reactorHolder) {
        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement reactorStmt = conn.createStatement();
             ResultSet reactors = reactorStmt.executeQuery("SELECT reactorName, thermalcapacity, type FROM reactor")) {

            while (reactors.next()) {
                String reactorName = reactors.getString("reactorName");
                double thermalCapacity = reactors.getDouble("thermalcapacity");
                String type = reactors.getString("type");

                double burnup = getBurnup(conn, type);

                for (String year : years) {
                    double loadFactor = getLoadFactor(conn, reactorName, year);
                    double consumption = (thermalCapacity * loadFactor) / burnup;
                    reactorHolder.addConsumption(reactorName, year, consumption);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Структура БД не соответствует или файл не является базой данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static double getBurnup(Connection conn, String type) throws SQLException {
        String queryBurnup = "SELECT burnup FROM type WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(queryBurnup)) {
            pstmt.setString(1, type);
            try (ResultSet burnupResult = pstmt.executeQuery()) {
                if (burnupResult.next()) {
                    return burnupResult.getDouble("burnup");
                }
            }
        }
        return 10.0; // Значение по умолчанию, если данных нет
    }

    private static double getLoadFactor(Connection conn, String reactorName, String year) throws SQLException {
        String queryLoadFactor = "SELECT " + year + " FROM loadfactor WHERE reactorname = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(queryLoadFactor)) {
            pstmt.setString(1, reactorName);
            try (ResultSet loadFactorResult = pstmt.executeQuery()) {
                if (loadFactorResult.next()) {
                    return loadFactorResult.getDouble(year) / 100;
                }
            }
        }
        return 0.0; // Значение по умолчанию, если данных нет
    }

    public static HashMap<String, HashMap<String, Double>> calculateConsumptionByCountry(Connection conn, ReactorHolder reactorHolder) {
        HashMap<String, HashMap<String, Double>> consumptionByCountry = new HashMap<>();

        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement countryStmt = conn.createStatement();
             ResultSet countries = countryStmt.executeQuery("SELECT country FROM country")) {

            while (countries.next()) {
                String country = countries.getString("country");
                HashMap<String, Double> yearConsumption = new HashMap<>();

                String query = "SELECT reactorName FROM reactor WHERE country = ?";
                try (PreparedStatement reactorStmt = conn.prepareStatement(query)) {
                    reactorStmt.setString(1, country);
                    ResultSet reactors = reactorStmt.executeQuery();

                    List<String> reactorNames = new ArrayList<>();
                    while (reactors.next()) {
                        String reactorName = reactors.getString("reactorName");
                        reactorNames.add(reactorName);
                    }

                    for (String year : years) {
                        double totalConsumption = 0.0;
                        for (String reactorName : reactorNames) {
                            HashMap<String, Double> reactorData = reactorHolder.

                                    getReactorData(reactorName);
                            if (reactorData.containsKey(year)) {
                                totalConsumption += reactorData.get(year);
                            }
                        }
                        yearConsumption.put(year, totalConsumption);
                    }
                }

                consumptionByCountry.put(country, yearConsumption);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при расчете потребления по странам", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        return consumptionByCountry;
    }

    public static HashMap<String, HashMap<String, Double>> calculateConsumptionByRegion(Connection conn, ReactorHolder reactorHolder) {
        HashMap<String, HashMap<String, Double>> consumptionByRegion = new HashMap<>();

        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement regionStmt = conn.createStatement();
             ResultSet regions = regionStmt.executeQuery("SELECT region FROM region")) {

            while (regions.next()) {
                String region = regions.getString("region");
                HashMap<String, Double> yearConsumption = new HashMap<>();

                String query = "SELECT country FROM country WHERE region = ?";
                try (PreparedStatement countryStmt = conn.prepareStatement(query)) {
                    countryStmt.setString(1, region);
                    ResultSet countries = countryStmt.executeQuery();

                    List<String> countryNames = new ArrayList<>();
                    while (countries.next()) {
                        String countryName = countries.getString("country");
                        countryNames.add(countryName);
                    }

                    for (String year : years) {
                        double totalConsumption = 0.0;
                        for (String countryName : countryNames) {
                            HashMap<String, Double> countryData = calculateConsumptionByCountry(conn, reactorHolder).get(countryName);
                            if (countryData != null && countryData.containsKey(year)) {
                                totalConsumption += countryData.get(year);
                            }
                        }
                        yearConsumption.put(year, totalConsumption);
                    }
                }

                consumptionByRegion.put(region, yearConsumption);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при расчете потребления по регионам", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        return consumptionByRegion;
    }

    public static HashMap<String, HashMap<String, Double>> calculateConsumptionByCompany(Connection conn, ReactorHolder reactorHolder) {
        HashMap<String, HashMap<String, Double>> consumptionByCompany = new HashMap<>();

        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement companyStmt = conn.createStatement();
             ResultSet companies = companyStmt.executeQuery("SELECT company FROM company")) {

            while (companies.next()) {
                String company = companies.getString("company");
                HashMap<String, Double> yearConsumption = new HashMap<>();

                String query = "SELECT reactorName FROM reactor WHERE owner = ?";
                try (PreparedStatement reactorStmt = conn.prepareStatement(query)) {
                    reactorStmt.setString(1, company);
                    ResultSet reactors = reactorStmt.executeQuery();

                    List<String> reactorNames = new ArrayList<>();
                    while (reactors.next()) {
                        String reactorName = reactors.getString("reactorName");
                        reactorNames.add(reactorName);
                    }

                    for (String year : years) {

                        double totalConsumption = 0.0;
                        for (String reactorName : reactorNames) {
                            HashMap<String, Double> reactorData = reactorHolder.getReactorData(reactorName);
                            if (reactorData.containsKey(year)) {
                                totalConsumption += reactorData.get(year);
                            }
                        }
                        yearConsumption.put(year, totalConsumption);
                    }
                }

                consumptionByCompany.put(company, yearConsumption);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при расчете потребления по компаниям", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        return consumptionByCompany;
    }
}