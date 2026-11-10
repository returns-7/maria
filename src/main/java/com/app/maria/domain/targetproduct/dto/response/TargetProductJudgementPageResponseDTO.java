package com.app.maria.domain.targetproduct.dto.response;

import com.app.maria.domain.targetproduct.dto.TargetProductJudgementPageDTO;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductJudgementPageResponseDTO {
    private List<TargetProductJudgementListResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public TargetProductJudgementPageResponseDTO(TargetProductJudgementPageDTO dto) {
        this.content =
                dto.getContent().stream().map(TargetProductJudgementListResponseDTO::new).toList();
        this.page = dto.getPage();
        this.size = dto.getSize();
        this.totalElements = dto.getTotalElements();
        this.totalPages = dto.getTotalPages();
    }
}
