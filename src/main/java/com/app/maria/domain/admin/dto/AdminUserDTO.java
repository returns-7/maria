package com.app.maria.domain.admin.dto;

import com.app.maria.domain.admin.type.AdminRole;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "adminId")
public class AdminUserDTO {

    private Long adminId;
    private String loginId;
    private String passwordHash;
    private AdminRole role;
    private String name;
}
