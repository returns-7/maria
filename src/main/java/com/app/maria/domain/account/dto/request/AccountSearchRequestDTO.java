package com.app.maria.domain.account.dto.request;

import com.app.maria.domain.account.dto.AccountSearchDTO;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AccountSearchRequestDTO {

    private String accountNo;
    private String customerName;

    @AssertTrue(message = "정보를 입력해주세요.")
    public boolean isSearchConditionValid() {
        return (accountNo != null && !accountNo.isBlank())
                || (customerName != null && !customerName.isBlank());
    }

    public AccountSearchDTO toAccountSearchDTO() {
        return AccountSearchDTO.builder().accountNo(accountNo).customerName(customerName).build();
    }
}
