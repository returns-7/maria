package com.app.maria.domain.settlement.dto;

import com.app.maria.domain.sellorder.type.SellOrderStatus;
import com.app.maria.domain.settlement.type.SettlementStatus;
import com.app.maria.domain.settlement.type.SettlementItemResult;
import com.app.maria.domain.settlement.type.SettlementFailureCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "itemId")
public class SettlementJoinDTO {
  private Long itemId;
  private Long batchId;
  private SettlementItemResult result;
  private LocalDateTime processedAt;
  private SettlementFailureCode failureCode;
  private String failureMessage;

  //krw_exchange
  private Long exchangeId;
  private Long accountId;
  private Long orderId;
  private BigDecimal provisionalAmount;
  private LocalDateTime provisionalAt;
  private SettlementStatus settlementStatus;

  //sell_order
  private BigDecimal settlementFxRate;
  private SellOrderStatus sellOrderStatus;

  //inbound_detail
  private String purchaseCurrency;
}
