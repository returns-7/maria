package com.app.maria.domain.settlement.type;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public enum ProvisionalRate {
    PROVISIONAL_RATE(new BigDecimal("0.99"));

    private final BigDecimal value;

    ProvisionalRate(BigDecimal value) {
        this.value = value;
    }
}
