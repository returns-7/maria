package com.app.maria.domain.targetproduct.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductJudgementPageDTO {
    private List<TargetProductJudgementListDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
