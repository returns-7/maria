package com.app.maria.domain.targetproduct.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MydataFundResponseDTO {

    private String fundCode;
    private String fundName;
    private BigDecimal foreignStockRatio;
    private LocalDate inceptionDate;
}
