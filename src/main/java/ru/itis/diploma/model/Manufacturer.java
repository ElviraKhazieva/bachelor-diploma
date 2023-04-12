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
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Game game;

    @ManyToOne
    private Account account;

//    private BigDecimal income; //высчитывается в конце игры по результатам

    private BigDecimal balance;

    private BigDecimal investmentCreditAmount; //величина инвестиционного кредита(стартоый капитал)

    private BigDecimal investmentCreditDebt;

    private boolean enteredInitialProductionParameters;

    private Boolean investmentCreditIsRepaid;

}
