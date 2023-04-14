package ru.itis.diploma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.diploma.model.enums.GameStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Game {

    public static int currentDay = 165; //todo: вернуть на 0

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private Integer timeUnit; // модельное время в минутах(сутки = ? минут)

    private BigDecimal interestRateInvestmentCredit; // процентная ставка на стартовый капитал(инвестиционный кредит - для организации производства(оборудование, помещение))

    private BigDecimal investmentCreditTermMonths; // cрок инвестиционного кредита в месяцах

    private BigDecimal interestRateBusinessCredit; // процентная ставка на оборотные средства(оборотный кредит - на сырье для выпуска продукции)

    private BigDecimal salesTax; // налог с продаж

    private BigDecimal baseCostPrice;//базовая себестоимость производства единицы продукции

    private BigDecimal baseAdvertisementPrice; //базовая цена рекламы

    private BigDecimal productPower; //сколько необходимо денег для того чтобы производить на единицу продукции больше

    private BigDecimal assortmentWeight;

    private BigDecimal qualityWeight;

    private BigDecimal advertisementWeight;

//    private Integer requiredQuantity; // спрос покупателя (то, сколько покупателю нужно купить) 15 ед нужно купить

}
