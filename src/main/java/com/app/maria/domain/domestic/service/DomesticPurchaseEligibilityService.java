package com.app.maria.domain.domestic.service;

import com.app.maria.domain.domestic.type.Type;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface DomesticPurchaseEligibilityService {
    boolean isPurchasable(Long domesticProductId);

    boolean isPurchasable(Type type, BigDecimal domesticStockRatio, LocalDate inceptionDate);
}
