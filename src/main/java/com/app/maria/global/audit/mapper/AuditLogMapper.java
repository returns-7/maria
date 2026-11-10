package com.app.maria.global.audit.mapper;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.AuditLogSearchDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {

    int insertLog(AuditLogDTO auditLogDTO);

    List<AuditLogDTO> selectAuditLogs(AuditLogSearchDTO auditLogSearchDTO);

    long countAuditLogs(AuditLogSearchDTO auditLogSearchDTO);
}
