package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.StatisticsInfo;

public interface StatisticsInfoRepository extends JpaRepository<StatisticsInfo, Long> {
}
