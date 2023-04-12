package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.BusinessCreditPayment;

import java.util.List;

public interface BusinessCreditPaymentRepository extends JpaRepository<BusinessCreditPayment, Long> {
    List<BusinessCreditPayment> findAllByProductionParametersIdInAndNextDate(List<Long> productionParametersIds, Integer nextDate);

    List<BusinessCreditPayment> findAllByProductionParametersId(Long productionParametersId);
}
