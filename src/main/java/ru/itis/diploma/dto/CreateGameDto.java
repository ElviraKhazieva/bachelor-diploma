package ru.itis.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGameDto {

    private String name;

    private Integer timeUnit;

    private BigDecimal interestRateInvestmentCredit;

    private BigDecimal interestRateBusinessCredit;

    private BigDecimal salesTax;

    private BigDecimal baseCostPrice;

    private BigDecimal baseAdvertisementPrice;

    private BigDecimal productPower; // цена создания единицы мощности производства (цена станка)

    private Integer requiredQuantity;

    private List<Long> accountIds;

}
