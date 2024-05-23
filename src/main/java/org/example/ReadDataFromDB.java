package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            e.printStackTrace();
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

        return 10.0; // Для двух реакторов, у которых не было указано
    }

    private static double getLoadFactor(Connection conn, String reactorName, String year) throws SQLException {
        String queryLoadFactor = "SELECT " + year + " FROM loadfactor WHERE reactorname = ?";
        PreparedStatement pstmt = conn.prepareStatement(queryLoadFactor);
        pstmt.setString(1, reactorName);
        ResultSet loadFactorResult = pstmt.executeQuery();

        return loadFactorResult.getDouble(year) / 100;
    }

    public static HashMap<String, HashMap<String, Double>> calculateConsumptionByCountry(Connection conn, ReactorHolder reactorHolder) {
        HashMap<String, HashMap<String, Double>> consumptionByCountry = new HashMap<>();

        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement countryStmt = conn.createStatement();
             ResultSet countries = countryStmt.executeQuery("SELECT country FROM country")) {

            while (countries.next()) {
                String country = countries.getString("country");
                HashMap<String, Double> yearConsumption = new HashMap<>();

                // Получаем список реакторов для текущей страны
                String query = "SELECT reactorName FROM reactor WHERE country = ?";
                try (PreparedStatement reactorStmt = conn.prepareStatement(query)) {
                    reactorStmt.setString(1, country);
                    ResultSet reactors = reactorStmt.executeQuery();

                    // Создаем список реакторов для обработки по годам
                    List<String> reactorNames = new ArrayList<>();
                    while (reactors.next()) {
                        String reactorName = reactors.getString("reactorName");
                        reactorNames.add(reactorName);
                    }

                    // Для каждого года считаем суммарное потребление по реакторам
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

                consumptionByCountry.put(country, yearConsumption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

                // Получаем список стран для текущего региона
                String query = "SELECT country FROM country WHERE region = ?";
                try (PreparedStatement countryStmt = conn.prepareStatement(query)) {
                    countryStmt.setString(1, region);
                    ResultSet countries = countryStmt.executeQuery();

                    // Создаем список стран для обработки по годам
                    List<String> countryNames = new ArrayList<>();
                    while (countries.next()) {
                        String countryName = countries.getString("country");
                        countryNames.add(countryName);
                    }

                    // Для каждого года считаем суммарное потребление по странам
                    for (String year : years) {
                        double totalConsumption = 0.0;
                        for (String countryName : countryNames) {
                            // Получаем потребление для текущей страны из реакторного хранилища
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
            e.printStackTrace();
        }

        return consumptionByRegion;
    }

    public static HashMap<String, HashMap<String, Double>> calculateConsumptionByCompany (Connection conn, ReactorHolder reactorHolder) {
        HashMap<String, HashMap<String, Double>> consumptionByCountry = new HashMap<>();

        String[] years = {"2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024"};

        try (Statement companyStmt = conn.createStatement();
             ResultSet companies = companyStmt.executeQuery("SELECT company FROM company")) {

            while (companies.next()) {
                String company = companies.getString("company");
                HashMap<String, Double> yearConsumption = new HashMap<>();

                // Получаем список реакторов для текущей компании
                String query = "SELECT reactorName FROM reactor WHERE owner = ?";
                try (PreparedStatement reactorStmt = conn.prepareStatement(query)) {
                    reactorStmt.setString(1, company);
                    ResultSet reactors = reactorStmt.executeQuery();

                    // Создаем список реакторов для обработки по годам
                    List<String> reactorNames = new ArrayList<>();
                    while (reactors.next()) {
                        String reactorName = reactors.getString("reactorName");
                        reactorNames.add(reactorName);
                    }

                    // Для каждого года считаем суммарное потребление по реакторам
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

                consumptionByCountry.put(company, yearConsumption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return consumptionByCountry;
    }
}

