package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class Manager {
    private static final String DATABASE_URL = "jdbc:sqlite:";

    public void CalculateForCountries(String fileName, Workbook  workbook) throws SQLException {
        ReactorHolder reactorHolder = new ReactorHolder();
        ReadDataFromDB readDataFromDB = new ReadDataFromDB();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL + fileName)) {
            if (conn != null) {
                readDataFromDB.calculateConsumption(conn, reactorHolder);
                HashMap<String, HashMap<String, Double>> consumptionByCountry = readDataFromDB.calculateConsumptionByCountry(conn, reactorHolder);

                // Добавить данные в лист "Потребление по странам"
                ExcelExporter.exportToExcel(consumptionByCountry, "Потребление по cтранам", workbook);
            }

        }
    }

    public void CalculateForRegion(String fileName, Workbook workbook) throws SQLException {
        ReactorHolder reactorHolder = new ReactorHolder();
        ReadDataFromDB readDataFromDB = new ReadDataFromDB();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL + fileName)) {
            if (conn != null) {
                readDataFromDB.calculateConsumption(conn, reactorHolder);
                HashMap<String, HashMap<String, Double>> consumptionByRegion = readDataFromDB.calculateConsumptionByRegion(conn, reactorHolder);

                // Добавить данные в лист "Потребление по регионам"
                ExcelExporter.exportToExcel(consumptionByRegion, "Потребление по регионам", workbook);
            }

        }
    }

    public void CalculateForCompany(String fileName, Workbook workbook) throws SQLException {
        ReactorHolder reactorHolder = new ReactorHolder();
        ReadDataFromDB readDataFromDB = new ReadDataFromDB();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL + fileName)) {
            if (conn != null) {
                readDataFromDB.calculateConsumption(conn, reactorHolder);
                HashMap<String, HashMap<String, Double>> consumptionByCompany = readDataFromDB.calculateConsumptionByCompany(conn, reactorHolder);

                // Добавить данные в лист "Потребление по компаниям"
                ExcelExporter.exportToExcel(consumptionByCompany, "Потребление по компаниям", workbook);
            }

        }
    }

}
