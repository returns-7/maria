package com.app.maria.domain.externaltradesync.mapper;

import com.app.maria.domain.externaltradesync.dto.ExternalTradeSyncCursorDTO;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExternalTradeSyncCursorMapper {

    Optional<ExternalTradeSyncCursorDTO> selectByCustomerId(Long customerId);

    void upsertCursor(ExternalTradeSyncCursorDTO cursor);
}
