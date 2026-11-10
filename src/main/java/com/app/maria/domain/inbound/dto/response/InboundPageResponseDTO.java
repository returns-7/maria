package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundPageDTO;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundPageResponseDTO {
    private List<InboundListResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public InboundPageResponseDTO(InboundPageDTO dto) {
        this.content = dto.getContent().stream().map(InboundListResponseDTO::new).toList();
        this.page = dto.getPage();
        this.size = dto.getSize();
        this.totalElements = dto.getTotalElements();
        this.totalPages = dto.getTotalPages();
    }
}
