package com.app.maria.global.clock.dto;

import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SystemClockDTO {
    private Long clockId;
    private LocalDateTime baseDatetime;
    private LocalDateTime currentDatetime;
    private LocalDateTime referenceRealDatetime;
}
