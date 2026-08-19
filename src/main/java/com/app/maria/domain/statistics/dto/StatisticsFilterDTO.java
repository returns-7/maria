package com.app.maria.domain.statistics.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class StatisticsFilterDTO {

    private String keyword;
    private String productName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDate referenceDate;
}
