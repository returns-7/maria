package com.app.maria.domain.domestic.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentPageDTO {
    private List<DomesticInvestmentListDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
