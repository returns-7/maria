package com.app.maria.domain.accountclosure.mapper;

import com.app.maria.domain.accountclosure.dto.AccountClosureDTO;
import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountClosureMapper {
    int insertClosureRequest(AccountClosureDTO closure);

    Optional<AccountClosureDTO> selectByIdForUpdate(Long closureRequestId);

    int completeClosureRequest(AccountClosureDTO closure);

    int rejectClosureRequest(AccountClosureDTO closure);

    List<AccountClosureDTO> selectByStatus(AccountClosureStatus status);

    Optional<AccountClosureDTO> selectById(Long closureRequestId);
}
