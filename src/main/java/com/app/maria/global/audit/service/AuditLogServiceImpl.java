package com.app.maria.global.audit.service;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.AuditLogSearchDTO;
import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;
import com.app.maria.global.audit.exception.AuditLogInsertException;
import com.app.maria.global.audit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> searchAuditLogs(AuditLogSearchRequestDTO requestDTO) {
        AuditLogSearchDTO searchDTO = requestDTO.toAuditLogSearchDTO();
        List<AuditLogDTO> auditLogs = auditLogMapper.selectAuditLogs(searchDTO);
        return auditLogs.stream().map(AuditLogResponseDTO::new).toList();
    }

    @Override
    public void log(AuditLogDTO auditLogDTO) {
        int insertRows = auditLogMapper.insertLog(auditLogDTO);
        if (insertRows != 1) {
            throw new AuditLogInsertException("AUDIT_LOG 저장에 실패했습니다.");
        }
    }

}
