package com.app.maria.domain.sellorder.dto.request;

import com.app.maria.domain.sellorder.dto.SellOrderDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderRequestDTO {

    @NotNull(message = "계좌 ID를 입력하세요.")
    private Long accountId;

    @NotNull(message = "종목 ID를 입력하세요.")
    private Long foreignProductId;

    @NotNull(message = "매도 수량을 입력하세요.")
    @DecimalMin(value = "0", inclusive = false, message = "매도 수량은 0보다 커야 합니다.")
    private BigDecimal sellQty;

    public SellOrderDTO toSellOrderDTO() {
        return SellOrderDTO.builder()
                .accountId(accountId)
                .foreignProductId(foreignProductId)
                .sellQty(sellQty)
                .build();
    }
}
