package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.InvestmentCreditPayment;

import java.util.List;

public interface InvestmentCreditPaymentRepository extends JpaRepository<InvestmentCreditPayment, Long> {

    List<InvestmentCreditPayment> findAllByManufacturerId(Long manufacturerId);

    List<InvestmentCreditPayment> findAllByManufacturerIdInAndNextDate(List<Long> manufacturerIds, Integer date);

}
