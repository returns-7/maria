package com.app.maria.domain.externaltradesync.service;

import com.app.maria.domain.externaltradesync.dto.response.ExternalTradeSyncResultDTO;

public interface ExternalTradeSyncService {
    ExternalTradeSyncResultDTO syncAll();
}
