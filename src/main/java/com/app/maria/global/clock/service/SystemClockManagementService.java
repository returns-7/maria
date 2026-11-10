package com.app.maria.global.clock.service;

import com.app.maria.global.clock.dto.request.SystemClockChangeRequestDTO;
import java.time.LocalDateTime;

public interface SystemClockManagementService {

    LocalDateTime changeSystemTime(Long adminId, SystemClockChangeRequestDTO requestDTO);
}
