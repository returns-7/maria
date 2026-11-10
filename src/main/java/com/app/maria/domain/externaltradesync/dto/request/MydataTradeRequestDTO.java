package com.app.maria.domain.externaltradesync.dto.request;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MydataTradeRequestDTO {

    private String ciHash;
    private LocalDate fromDate;
}
