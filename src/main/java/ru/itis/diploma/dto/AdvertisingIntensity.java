package ru.itis.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisingIntensity {

    private Integer startDate;

    private Integer endDate;

    private Integer intensityIndex;

}
