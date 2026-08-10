package com.app.maria.global.audit.mapper;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.AuditLogSearchDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface AuditLogMapper {
    int insertLog(AuditLogDTO auditLogDTO);
    List<AuditLogDTO> selectAuditLogs(AuditLogSearchDTO auditLogSearchDTO);
}
