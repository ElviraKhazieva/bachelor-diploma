package ru.itis.diploma.service;

import ru.itis.diploma.dto.EditProductionParameters;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ManufacturerService {

    Optional<ProductionParameters> getActualProductionParameters(Long manufacturerId);

    Optional<Advertisement> getActualAdvertisement(Long manufacturerId);

    List<Manufacturer> getGameManufacturers(Long gameId);

    Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId);

    void enterInitialProductionParameters(InitialProductionParameters initialProductionParameters,
                                          Long accountId,
                                          Long gameId);

    void editProductionParameters(EditProductionParameters editProductionParameters,
                                  Long accountId,
                                  Long gameId);

    ManufacturerFinancialStatus getManufacturerFinancialStatus(Long gameId, Long accountId);

    BigDecimal calculateManufacturerInvestmentCreditDebt(Manufacturer manufacturer);

    BigDecimal calculateManufacturerBusinessCreditDebt(ProductionParameters productionParameters);
}
