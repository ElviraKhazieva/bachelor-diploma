package ru.itis.diploma.util;

import org.springframework.stereotype.Component;

@Component
public class EconomicIndicatorsCalculator {

    private static final double M = 10; // количество продукции, производимое в день 1 станком

    public int calculateTimeToMarket(Integer productCount, Integer productionCapacityPerDay) {
        return (int) Math.ceil(productCount / (Math.ceil(productionCapacityPerDay / M) * M));
    }
}
