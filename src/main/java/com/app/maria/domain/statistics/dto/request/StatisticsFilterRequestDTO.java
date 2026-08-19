package com.app.maria.domain.statistics.dto.request;

import com.app.maria.domain.statistics.dto.StatisticsFilterDTO;
import java.time.LocalDate;
import java.time.LocalTime;
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
@Builder
public class StatisticsFilterRequestDTO {

    private String keyword;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;

    public StatisticsFilterDTO toFilterDTO() {
        return StatisticsFilterDTO.builder()
                .keyword(keyword)
                .productName(productName)
                .startDateTime(startDate == null ? null : startDate.atStartOfDay())
                .endDateTime(endDate == null ? null : endDate.atTime(LocalTime.of(23, 59, 59)))
                .build();
    }
}
