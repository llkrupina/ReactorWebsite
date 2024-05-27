package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ReactorHolder {

    // Основной HashMap, где ключ - это страна, а значение - другой HashMap
    // Внутренний HashMap, где ключ - это год, а значение - потребление
    private final Map<String, Map<String, Double>> reactorsWithConsumption;

    // Конструктор инициализирует основной HashMap
    public ReactorHolder() {
        this.reactorsWithConsumption = new HashMap<>();
    }

    // Метод для добавления потребления в структуру данных
    public void addConsumption(String reactorname, String year, double consumption) {
        // Если реактора нет в основном HashMap, добавляем её
        reactorsWithConsumption.putIfAbsent(reactorname, new HashMap<>());

        // Получаем внутренний HashMap для реактора и добавляем в него потребление для указанного года
        reactorsWithConsumption.get(reactorname).put(year, consumption);
    }

    // Метод для получения основного HashMap
    public Map<String, Map<String, Double>> getReactorsWithConsumption() {
        return reactorsWithConsumption;
    }

    public HashMap<String, Double> getReactorData(String reactorName) {
        return (HashMap<String, Double>) reactorsWithConsumption.get(reactorName);
    }

    public Set<String> getReactorNames() {
        return reactorsWithConsumption.keySet();
    }

    // Метод для печати всех данных
    public void printAllConsumption() {
        for (Map.Entry<String, Map<String, Double>> countryEntry : reactorsWithConsumption.entrySet()) {
            String reactorname = countryEntry.getKey();
            Map<String, Double> yearConsumptionMap = countryEntry.getValue();

            for (Map.Entry<String, Double> yearEntry : yearConsumptionMap.entrySet()) {
                String year = yearEntry.getKey();
                double consumption = yearEntry.getValue();

                System.out.printf("Год: %s - Реактор: %s - Потребление: %.2f%n", year, reactorname, consumption);
            }
        }
    }

}