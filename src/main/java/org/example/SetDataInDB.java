package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sqlite.SQLiteException;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.Date;

public class SetDataInDB {
    public static void main(String[] args) throws SQLException, IOException, ParseException {
        String url = "jdbc:sqlite:mylastDBreserve.sqlite";
        Connection connection = DriverManager.getConnection(url);

        String insertQueryReactor = "INSERT INTO reactor (reactorName, firstgridconnection, shutdownyear, country, owner, type, thermalcapacity) VALUES (?, ?, ?, ?, ?, ?,?)";
        String insertQueryLoadfactor = "INSERT INTO loadfactor (reactorname, year, consumption) VALUES (?, ?, ?)";


        PreparedStatement preparedStatementReactor = connection.prepareStatement(insertQueryReactor);
        PreparedStatement preparedStatementLoadFactor = connection.prepareStatement(insertQueryLoadfactor);

        // Создание экземпляра веб-драйвера
        WebDriver driver = new ChromeDriver();
        String baseUrl = "https://pris.iaea.org";
        String urlMain = baseUrl + "/PRIS/CountryStatistics/CountryStatisticsLandingPage.aspx";

        // Создаем HashMap для хранения ссылок на реакторы для каждой страны
        HashMap<String, List<String>> countryReactorLinks = new HashMap<>();

        Document doc = Jsoup.connect(urlMain).get();
        Element sidebar = doc.selectFirst("div#sidebar-first");
        if (sidebar != null) {
            Elements countries = sidebar.select("li a");
            for (Element country : countries) {
                String countryName = country.text();
                String relativeLink = country.attr("href");
                String fullLink = baseUrl + relativeLink;
                driver.get(fullLink);

                // Находим все ссылки на странице, начинающиеся с "javascript:__doPostBack"
                List<WebElement> links = driver.findElements(By.xpath("//a[starts-with(@href, 'javascript:__doPostBack')]"));

                // Переходим на страницу первого реактора
                if (!links.isEmpty()) {
                    WebElement firstLink = links.get(0);
                    firstLink.click();

                    // Получаем все ссылки на новой странице реакторов
                    List<WebElement> reactorLinks = driver.findElements(By.xpath("//div[@id='sidebar-first']//ul//li//a[starts-with(@id, 'MainContent_rptSideNavigation_hypNavigation')]"));
                    List<String> countryReactorList = new ArrayList<>();

                    for (WebElement reactorLink : reactorLinks) {
                        String reactorUrl = reactorLink.getAttribute("href");
                        countryReactorList.add(reactorUrl);
                    }

                    countryReactorLinks.put(countryName, countryReactorList);
                }
            }

        }


        // Теперь можно перебрать HashMap и вывести данные для каждой страны
        for (Map.Entry<String, List<String>> entry : countryReactorLinks.entrySet()) {
            try {

                String reactorCountry = entry.getKey();
                List<String> reactorUrls = entry.getValue();

                System.out.println("Country: " + reactorCountry);

                for (String reactorUrl : reactorUrls) {

                    driver.get(reactorUrl);
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                    // Название реактора
                    WebElement reactorNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblReactorName")));
                    String reactorName = reactorNameElement.getText();

                    // Проверяем статус реактора
                    WebElement reactorStatusElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblReactorStatus")));
                    String reactorStatus = reactorStatusElement.getText();


                    if (reactorStatus.equals("Under Construction")) {
                        preparedStatementReactor.setNull(2, Types.INTEGER);
                        preparedStatementReactor.setNull(3, Types.INTEGER);



                        for (int year = 2014; year <= 2024; year++) {
                            //System.out.println(i + " " + reactorName + ", year: " + year + ", Load Factor: Строится");
                            preparedStatementLoadFactor.setString(1, reactorName);
                            preparedStatementLoadFactor.setInt(2, year);
                            preparedStatementLoadFactor.setDouble(3, 0.0);

                            preparedStatementLoadFactor.executeUpdate();

                        }
                    } else if (reactorStatus.equals("Operational")) {

                        // Дата начала работы
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);

                        WebElement firstGridConnection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblGridConnectionDate")));
                        String firstGridDataString = firstGridConnection.getText();
                        Date firstGridData = dateFormat.parse(firstGridDataString);

                        int firstGridYear = getYearAsDate(firstGridData);
                        preparedStatementReactor.setInt(2, firstGridYear);

                        // Cтавим null для года закрытия
                        preparedStatementReactor.setNull(3, Types.INTEGER);



                        // ТАБЛИЦА  LOADFACTOR

                        for (int year = 2014; year <= 2024; year++) {
                            double loadFactorValue = 85.0; // Значение по умолчанию
                            boolean executeUpdateNeeded = true;

                            if (firstGridYear < year) {
                                try {
                                    WebElement tableRow = driver.findElement(By.xpath("//td[contains(text(), '" + year + "')]//ancestor::tr"));
                                    WebElement secondLastElement = tableRow.findElements(By.tagName("td")).get(7); // Получаем восьмой элемент в строке (предпоследний)
                                    String loadFactor = secondLastElement.getText();
                                    loadFactorValue = Double.parseDouble(loadFactor);
                                } catch (NoSuchElementException | IndexOutOfBoundsException e) {
                                    // Значение по умолчанию остается 85.0
                                } catch (NumberFormatException e) {
                                    System.out.println(reactorName + " СОВПАЛ ГОД");
                                } catch (Exception e) {
                                    executeUpdateNeeded = false; // Не выполнять обновление в случае других исключений
                                }
                            } else if (firstGridYear == year) {
                                loadFactorValue = 100.0;
                            } else {
                                loadFactorValue = 0.0;
                            }

                            if (executeUpdateNeeded) {
                                preparedStatementLoadFactor.setString(1, reactorName);
                                preparedStatementLoadFactor.setInt(2, year);
                                preparedStatementLoadFactor.setDouble(3, loadFactorValue);
                                try {
                                    preparedStatementLoadFactor.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace(); // Обработка исключения, если оно произойдет
                                }
                            }
                        }

                    } else {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);

                        int firstGridYear = 0;
                        // Год открытия
                        try {
                            WebElement firstGridConnection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblGridConnectionDate")));
                            String firstGridDataString = firstGridConnection.getText();
                            Date firstGridData = dateFormat.parse(firstGridDataString);

                            firstGridYear = getYearAsDate(firstGridData);
                            preparedStatementReactor.setInt(2, firstGridYear);
                        } catch (ParseException e) {
                            preparedStatementReactor.setNull(2, Types.INTEGER);
                        }

                        // Год закрытия
                        int shutDownYear = 0;
                        try {
                            WebElement shutDownYearElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblPermanentShutdownDate")));
                            String shutDownYearString = shutDownYearElement.getText();
                            Date shutDownYearData = dateFormat.

                                    parse(shutDownYearString);
                            shutDownYear = getYearAsDate(shutDownYearData);
                            preparedStatementReactor.setInt(3, shutDownYear);
                        } catch (TimeoutException e) {
                            WebElement shutDownYearElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblLongTermShutdownDate")));
                            String shutDownYearString = shutDownYearElement.getText();
                            Date shutDownYearData = dateFormat.parse(shutDownYearString);
                            shutDownYear = getYearAsDate(shutDownYearData);
                            preparedStatementReactor.setInt(3, shutDownYear);
                        }


                        for (int year = 2014; year <= 2024; year++) {
                            double loadFactorValue = 85.0; // Значение по умолчанию
                            boolean executeUpdateNeeded = true;

                            if (firstGridYear < year && shutDownYear >= year) {
                                try {
                                    WebElement tableRow = driver.findElement(By.xpath("//td[contains(text(), '" + year + "')]//ancestor::tr"));
                                    try {
                                        WebElement secondLastElement = tableRow.findElements(By.tagName("td")).get(7); // Получаем восьмой элемент в строке (предпоследний)
                                        String loadFactor = secondLastElement.getText();
                                        loadFactorValue = Double.parseDouble(loadFactor);
                                    } catch (NoSuchElementException | IndexOutOfBoundsException |
                                             NumberFormatException e) {
                                        // Значение по умолчанию остается 85.0
                                    }
                                } catch (NoSuchElementException e) {
                                    System.out.println("Element not found for year: " + year);
                                    executeUpdateNeeded = false;
                                }
                            } else if (firstGridYear == year) {
                                loadFactorValue = 100.0;
                            } else {
                                loadFactorValue = 0.0;
                            }

                            if (executeUpdateNeeded) {
                                preparedStatementLoadFactor.setString(1, reactorName);
                                preparedStatementLoadFactor.setInt(2, year);
                                preparedStatementLoadFactor.setDouble(3, loadFactorValue);
                                try {
                                    preparedStatementLoadFactor.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace(); // Обработка исключения, если оно произойдет
                                }
                            }
                        }

                    }

                    WebElement reactorThermalCapacity = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblThermalCapacity")));
                    String thermalCapacity = reactorThermalCapacity.getText();
                    preparedStatementReactor.setDouble(7, Double.parseDouble(thermalCapacity));


                    WebElement reactorModel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_lblType")));
                    String type = reactorModel.getText();
                    preparedStatementReactor.setString(6, type);


                    // ВЛАДЕЛЕЦ !!!!!!!!!!!!
                    try {
                        try {
                            WebElement reactorOwnerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_MainContent_hypOwnerUrl")));
                            String reactorOwner = reactorOwnerElement.getText();

                            // Разделение строки по запятой и взятие только первой части
                            String[] ownerParts = reactorOwner.split(",");
                            reactorOwner = ownerParts[0].trim(); // trim() убирает лишние пробелы

                            preparedStatementReactor.setString(5, reactorOwner);

                            //System.out.println(reactorName + "  Владелец: " + reactorOwner);
                        } catch (TimeoutException e) {
                            try {

                                WebElement modelElement = driver.findElement(By.id("MainContent_MainContent_lblModel"));
                                //System.out.println(modelElement.getText());

                                WebElement reactorOwnerElement = modelElement.findElement(By.xpath("following::td[1]/h5"));

                                String reactorOwner = reactorOwnerElement.getText();
                                //System.out.println(reactorOwner);


                                // Разделение строки по запятой и взятие только первой части
                                String[] ownerParts = reactorOwner.split(",");
                                reactorOwner = ownerParts[0].trim(); // trim() убирает лишние пробелы

                                preparedStatementReactor.setString(5, reactorOwner);

                                //System.out.println(reactorName + "  Владелец: " + reactorOwner);


                            } catch (TimeoutException n) {
                                System.out.println("Нет владельца для реактора: " + reactorName);
                                preparedStatementReactor.setString(5, null);
                            }
                        }
                    } catch (NoSuchElementException e) {
                        System.out.println("Нет владельца для реактора: " + reactorName);
                        preparedStatementReactor.setString(5, null);
                    }




                    preparedStatementReactor.setString(1, reactorName);



                    preparedStatementReactor.setString(4, reactorCountry);

                    try {
                        preparedStatementReactor.executeUpdate();
                    } catch (SQLiteException e) {

                    }


                }
                System.out.println("-------------");
            } catch (SQLIntegrityConstraintViolationException e) {
                // Обработка исключения, если данные уже существуют
            }


        }

        // Закрытие веб-драйвера и соединения с базой данных
        driver.quit();
        preparedStatementReactor.close();
        preparedStatementLoadFactor.close();
        connection.close();
    }

    private static int getYearAsDate(Date date) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return Integer.parseInt(yearFormat.format(date));
    }
}
