package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticCashHeavyPageDTO;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
public class DomesticCashHeavyPageResponseDTO {
    private List<DomesticCashHeavyAccountResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public DomesticCashHeavyPageResponseDTO(DomesticCashHeavyPageDTO dto) {
        this.content =
                dto.getContent().stream().map(DomesticCashHeavyAccountResponseDTO::new).toList();
        this.page = dto.getPage();
        this.size = dto.getSize();
        this.totalElements = dto.getTotalElements();
        this.totalPages = dto.getTotalPages();
    }
}
