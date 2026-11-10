package com.app.maria.domain.admin.dto.response;

import com.app.maria.domain.admin.dto.AdminUserDTO;
import com.app.maria.domain.admin.type.AdminRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AdminSummaryResponseDTO {

    private Long adminId;
    private String loginId;
    private AdminRole role;
    private String name;

    public AdminSummaryResponseDTO(AdminUserDTO dto) {
        this.adminId = dto.getAdminId();
        this.loginId = dto.getLoginId();
        this.role = dto.getRole();
        this.name = dto.getName();
    }
}
