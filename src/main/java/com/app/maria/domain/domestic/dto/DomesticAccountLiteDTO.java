package com.app.maria.domain.domestic.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticAccountLiteDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
}
