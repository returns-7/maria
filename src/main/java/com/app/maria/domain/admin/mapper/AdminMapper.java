package com.app.maria.domain.admin.mapper;

import com.app.maria.domain.admin.dto.AdminUserDTO;
import com.app.maria.domain.admin.type.AdminRole;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    Optional<AdminUserDTO> selectAdminByLoginId(@Param("loginId") String loginId);

    Optional<AdminUserDTO> selectAdminByAdminId(@Param("adminId") Long adminId);

    void updateRole(@Param("adminId") Long adminId, @Param("role") AdminRole role);

    List<AdminUserDTO> selectAllAdmins();
}
