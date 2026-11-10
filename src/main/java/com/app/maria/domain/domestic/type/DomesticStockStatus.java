package com.app.maria.domain.domestic.type;

public enum DomesticStockStatus {
    HOLDING, // 보유중
    PARTIALLY_SOLD, // 일부매도
    SOLD_OUT, // 전량매도
    TRADE_RESTRICTED, // 거래제한
    TRADE_SUSPENDED, // 거래정지
    TERMINATED // 보유종료
}
