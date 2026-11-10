package com.app.maria.domain.domestic.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticCashHeavyPageDTO {
    private List<DomesticCashHeavyAccountDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
