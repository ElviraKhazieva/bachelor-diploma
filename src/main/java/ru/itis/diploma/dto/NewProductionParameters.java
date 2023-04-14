package ru.itis.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewProductionParameters extends CommonProductionParameters {

    private BigDecimal productionCosts;
    private BigDecimal businessCreditAmount;

}
