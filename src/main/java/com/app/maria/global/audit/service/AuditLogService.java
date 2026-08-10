package com.app.maria.global.audit.service;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;

import java.util.List;

public interface AuditLogService {

    List<AuditLogResponseDTO> searchAuditLogs(AuditLogSearchRequestDTO requestDTO);
    void log(AuditLogDTO auditLogDTO);

}
