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
public class TradingSessionResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer tradeDate;

    @ManyToOne
    private Manufacturer manufacturer;

    private Integer productNumber; //количество проданных товаров

    private BigDecimal price; //цена товара

    private BigDecimal qualityIndex;

    private Boolean isWornOut; // изношенный товар

}
