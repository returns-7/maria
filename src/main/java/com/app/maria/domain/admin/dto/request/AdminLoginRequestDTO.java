package com.app.maria.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AdminLoginRequestDTO {

    @NotNull(message = "아이디를 입력하세요.")
    private String loginId;

    @NotNull(message = "비밀번호를 입력하세요.")
    private String password;
}
