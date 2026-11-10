package com.app.maria.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AdminRefreshRequestDTO {

    @NotNull(message = "재인증을 위한 토큰 정보가 없습니다.")
    private String refreshToken;
}
