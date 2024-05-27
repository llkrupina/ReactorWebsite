package org.example;


import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class GUI extends JFrame {
    private JButton chooseDatabaseButton;
    private JButton calculateCountryButton;
    private JButton calculateRegionButton;
    private JButton calculateCompanyButton;
    private  File selectedFile;

    public GUI() {
        setTitle("Калькулятор потребления реакторов");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрируем окно по центру экрана
        setLayout(new GridLayout(4, 1)); // Макет с 4 строками и 1 столбцом

        // Инициализация кнопок
        chooseDatabaseButton = new JButton("Выбрать базу данных");
        calculateCountryButton = new JButton("Рассчитать для стран");
        calculateRegionButton = new JButton("Рассчитать для регионов");
        calculateCompanyButton = new JButton("Рассчитать для компаний");

        // Добавление кнопок на фрейм
        add(chooseDatabaseButton);
        add(calculateCountryButton);
        add(calculateRegionButton);
        add(calculateCompanyButton);

        // Слушатели событий для кнопок
        chooseDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обработка события нажатия на кнопку выбора базы данных
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();

                    System.out.println("Выбранный файл базы данных: " + selectedFile.getName());


                }
            }
        });

        Manager manager = new Manager();
        Workbook workbook = new XSSFWorkbook();
        String outputFileName = "/consumption.xlsx"; // Имя выходного файла Excel


        calculateCountryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Пошла");
                    manager.CalculateForCountries(selectedFile.getName(), workbook);

                    String outputPuth = System.getProperty("user.dir") + outputFileName;
                    try (FileOutputStream fileOut = new FileOutputStream(outputPuth)) {
                        workbook.write(fileOut);
                    }

                    System.out.println("Создана");
                } catch (SQLException | IOException ep) {

                }
            }
        });

        calculateRegionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Пошла");
                    manager.CalculateForRegion(selectedFile.getName(), workbook);

                    String outputPuth = System.getProperty("user.dir") + outputFileName;
                    try (FileOutputStream fileOut = new FileOutputStream(outputPuth)) {
                        workbook.write(fileOut);
                    }

                    System.out.println("Создана");
                } catch (SQLException | IOException ep) {
                    ep.printStackTrace();
                }

            }
        });

        calculateCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Пошла");
                    manager.CalculateForCompany(selectedFile.getName(), workbook);

                    String outputPuth = System.getProperty("user.dir") + outputFileName;
                    try (FileOutputStream fileOut = new FileOutputStream(outputPuth)) {
                        workbook.write(fileOut);
                    }

                    System.out.println("Создана");
                } catch (SQLException | IOException ep) {
                    ep.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        // Создание и отображение GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUI gui = new GUI();
                gui.setVisible(true);
            }
        });
    }

}
