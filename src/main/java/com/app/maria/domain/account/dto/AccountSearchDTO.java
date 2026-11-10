package com.app.maria.domain.account.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AccountSearchDTO {

    private String accountNo;
    private String customerName;
}
