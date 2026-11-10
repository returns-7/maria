package com.app.maria.domain.admin.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AdminLoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}
