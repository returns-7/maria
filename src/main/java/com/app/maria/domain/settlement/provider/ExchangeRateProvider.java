package com.app.maria.domain.settlement.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateProvider {
    BigDecimal getFinalRate(String currencyUnit, LocalDate searchDate);
}
