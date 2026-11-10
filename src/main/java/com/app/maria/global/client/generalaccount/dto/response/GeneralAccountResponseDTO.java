package com.app.maria.global.client.generalaccount.dto.response;

import com.app.maria.global.client.generalaccount.type.GeneralAccountStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GeneralAccountResponseDTO {
    private Long generalAccountId;
    private String accountNo;
    private GeneralAccountStatus status;
}
