package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.Advertisement;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByManufacturerId(Long manufacturerId);

    Advertisement findByManufacturerIdAndStartDate(Long manufacturerId, Integer startDate);
}
