package ru.itis.diploma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductionParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер дня с момента запуска игры
     */
    private Integer startDate;

    @ManyToOne
    private Manufacturer manufacturer;

    /**
     * Количество продукции, которое производитель будет выпускать при очередном выпуске
     */
    private Integer productCount;

    /**
     * Цена единицы продукции, выставляемая производителем
     */
    private BigDecimal price;

    /**
     * Себестоимость единицы продукции, вычисляемое
     */
    private BigDecimal costPrice;

    /**
     * Ассортимент (количество типов продукции)
     */
    private Integer assortment;

    /**
     * Величина оборотного кредита на сырье и рекламу - не вводится при старте игры производителем
     */
    private BigDecimal businessCreditAmount;


    private BigDecimal interestRateBusinessCredit; //всегда берется базовая, если не получается выполнить списание по оборотному кредиту, то на остаток повышается процент

    private BigDecimal businessCreditDebt;

    private Boolean businessCreditIsRepaid;

    /**
     * Индекс качества продукции(0-1) - вводится ли производителем при старте? - да валидировать на 0-1
     */
    private BigDecimal qualityIndex;

    /**
     * Мощность производства(ед/сутки) - максимум, который сможет выпускать
     */
    private Integer productionCapacityPerDay;

    /**
     * Время выхода на рынок - количество дней для производства(влияет на то, будет ли производитель участвовать в текущей торговой сессии), не вводится производителем
     * ПОКА НЕ ИСПОЛЬЗУЕМ
     */
    private Integer timeToMarket; // вычисляется по формуле

}
