package ru.itis.diploma.service;

import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;

import java.util.List;

public interface ManufacturerService {

    ProductionParameters getActualProductionParameters(Long manufacturerId);

    Advertisement getActualAdvertisement(Long manufacturerId);

    List<Manufacturer> getGameManufacturers(Long gameId);

    Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId);

    void enterInitialProductionParameters(InitialProductionParameters initialProductionParameters,
                                          Long accountId,
                                          Long gameId);

}
