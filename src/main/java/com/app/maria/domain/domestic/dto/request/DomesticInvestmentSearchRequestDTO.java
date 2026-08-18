package com.app.maria.domain.domestic.dto.request;

import com.app.maria.domain.domestic.dto.DomesticInvestmentSearchDTO;
import com.app.maria.domain.domestic.type.DomesticStockStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentSearchRequestDTO {
    private String customerName;
    private DomesticStockStatus status;
    private int page;
    private int size;

    public DomesticInvestmentSearchDTO toDomesticInvestmentSearchDTO() {
        return DomesticInvestmentSearchDTO.builder()
                .customerName(customerName)
                .status(status)
                .offset(page * size)
                .size(size)
                .build();
    }
}
