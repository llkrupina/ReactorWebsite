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
                HashMap<String, HashMap<String, Double>> consumptionByRegion = readDataFromDB.calculateConsumptionByRegion(conn, reactorHolder);

                // Добавить данные в лист "Потребление по регионам"
                ExcelExporter.exportToExcel(consumptionByRegion, "Потребление по регионам", workbook);
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

    private void saveToSheet(Workbook workbook, String sheetName, HashMap<String, HashMap<String, Double>> data) {
        Sheet sheet = workbook.createSheet(sheetName);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue(sheetName);
        headerRow.createCell(1).setCellValue("Год");
        headerRow.createCell(2).setCellValue("Потребление");

        for (String key : data.keySet()) {
            HashMap<String, Double> yearConsumption = data.get(key);
            for (String year : yearConsumption.keySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(key);
                row.createCell(1).setCellValue(year);
                row.createCell(2).setCellValue(yearConsumption.get(year));
            }
        }
    }
}
