package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.Manufacturer;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    List<Manufacturer> findByAccount_Id(Long accountId);
    Manufacturer findByAccount_IdAndGame_Id(Long accountId, Long gameId);
    List<Manufacturer> findByGame_Id(Long gameId);
}
