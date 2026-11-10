package com.app.maria.domain.admin.dto.request;

import com.app.maria.domain.admin.type.AdminRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AdminRoleUpdateRequestDTO {

    @NotNull(message = "역할을 입력하세요.")
    private AdminRole role;
}
