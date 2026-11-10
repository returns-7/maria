package com.app.maria.global.audit.api;

import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;
import com.app.maria.global.audit.service.AuditLogService;
import com.app.maria.global.response.ApiResponseDTO;
import com.app.maria.global.response.PageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
public class AuditLogApi {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<AuditLogResponseDTO>>> searchAuditLogs(
            @Valid @ModelAttribute AuditLogSearchRequestDTO requestDTO) {
        PageResponseDTO<AuditLogResponseDTO> result = auditLogService.searchAuditLogs(requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.of("감사로그 조회 성공", result));
    }
}
