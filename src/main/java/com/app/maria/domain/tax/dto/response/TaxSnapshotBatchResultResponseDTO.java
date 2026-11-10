package com.app.maria.domain.tax.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TaxSnapshotBatchResultResponseDTO {
    private String runId;
    private String status;

    public static TaxSnapshotBatchResultResponseDTO of(String runId) {
        return TaxSnapshotBatchResultResponseDTO.builder().runId(runId).status("REQUESTED").build();
    }
}
