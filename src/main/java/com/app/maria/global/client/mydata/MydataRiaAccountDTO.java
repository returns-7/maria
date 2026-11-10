package com.app.maria.global.client.mydata;

import java.math.BigDecimal;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MydataRiaAccountDTO {

    private Long mydataAccountId;
    private String ciHash;
    private String brokerName;
    private BigDecimal riaLimit;
    private BigDecimal riaCumulativeSell;
}
