package com.app.maria.domain.domestic.dto;

import com.app.maria.domain.domestic.type.DomesticStockStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentSearchDTO {
    private String customerName;
    private DomesticStockStatus status;
    private int offset;
    private int size;
}
