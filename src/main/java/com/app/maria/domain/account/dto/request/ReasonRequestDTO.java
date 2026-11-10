package com.app.maria.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ReasonRequestDTO {
    @NotBlank(message = "사유를 입력해야 합니다.")
    @Size(max = 200, message = "사유는 200자 이하로 입력해야 합니다.")
    private String reason;
}
