package com.app.maria.domain.admin.type;

public enum AdminRole {
    VIEWER, // 조회전용
    REVIEWER, // 심사담당 (조회 + 심사)
    SETTLEMENT, // 정산담당 (조회 + 정산)
    ADMIN // 최고관리자 (조회 + 심사 + 정산 + 구간가중치 관리)
}
