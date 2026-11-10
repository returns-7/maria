package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticAccountLiteDTO;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticAccountLiteResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;

    public DomesticAccountLiteResponseDTO(DomesticAccountLiteDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
    }
}
