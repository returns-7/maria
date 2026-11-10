package com.app.maria.global.client.generalaccount.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class GeneralAccountRequestDTO {
    private String ciHash;
    private Long generalAccountId;
}
