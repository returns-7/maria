package com.app.maria.global.clock.mapper;

import com.app.maria.global.clock.dto.SystemClockDTO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemClockMapper {
    public Optional<SystemClockDTO> selectSystemClock();

    int updateSystemClock(
            @Param("newDatetime") LocalDateTime newDatetime,
            @Param("expectedReferenceRealDatetime") LocalDateTime expectedReferenceRealDatetime);
}
