package com.app.maria.domain.account.type;

public enum BenefitType {
    POSSIBLE,
    REDUCED,
    IMPOSSIBLE;

    /** null(미분류)은 배제로 보지 않는다. */
    public static boolean isReliefExcluded(BenefitType benefit) {
        return benefit == IMPOSSIBLE;
    }
}
