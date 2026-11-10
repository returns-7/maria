package com.app.maria.global.audit.service;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;
import com.app.maria.global.response.PageResponseDTO;

public interface AuditLogService {

    PageResponseDTO<AuditLogResponseDTO> searchAuditLogs(AuditLogSearchRequestDTO requestDTO);

    void log(AuditLogDTO auditLogDTO);
}
