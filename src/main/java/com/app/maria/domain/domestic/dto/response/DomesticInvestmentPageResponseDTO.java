package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticInvestmentPageDTO;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentPageResponseDTO {
    private List<DomesticInvestmentListResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public DomesticInvestmentPageResponseDTO(DomesticInvestmentPageDTO dto) {
        this.content =
                dto.getContent().stream().map(DomesticInvestmentListResponseDTO::new).toList();
        this.page = dto.getPage();
        this.size = dto.getSize();
        this.totalElements = dto.getTotalElements();
        this.totalPages = dto.getTotalPages();
    }
}
