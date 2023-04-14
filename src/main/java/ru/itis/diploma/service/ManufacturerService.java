package ru.itis.diploma.service;

import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
import ru.itis.diploma.dto.ManufacturerParameters;
import ru.itis.diploma.dto.NewProductionParameters;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ManufacturerService {

    Optional<ProductionParameters> getActualProductionParameters(Long manufacturerId);

    Optional<Advertisement> getActualAdvertisement(Long manufacturerId);

    List<Manufacturer> getGameManufacturers(Long gameId);

    List<ManufacturerParameters> getManufacturersActualProductionParameters(Long gameId);

    Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId);

    void defineInitialProductionParameters(InitialProductionParameters initialProductionParameters,
                                           Long accountId,
                                           Long gameId);

    void defineNewProductionParameters(NewProductionParameters newProductionParameters,
                                       Long accountId,
                                       Game game);

    ManufacturerFinancialStatus getManufacturerFinancialStatus(Long gameId, Long accountId);

    BigDecimal calculateManufacturerInvestmentCreditDebt(Manufacturer manufacturer);

    BigDecimal calculateManufacturerBusinessCreditDebt(Manufacturer manufacturer);

    List<ManufacturerParameters> getAllProductionParameters(Manufacturer manufacturer);
}
