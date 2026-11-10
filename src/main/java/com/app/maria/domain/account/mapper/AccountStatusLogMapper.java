package com.app.maria.domain.account.mapper;

import com.app.maria.domain.account.dto.AccountStatusLogDTO;
import com.app.maria.domain.account.type.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountStatusLogMapper {
    // 로그 생성
    int insertLog(AccountStatusLogDTO accountStatusLogDTO);

    // 계좌별 상태 이력 조회: 오래된 순
    List<AccountStatusLogDTO> selectByAccountId(Long accountId);

    // 가장 최근 상태 이력 조회
    Optional<AccountStatusLogDTO> selectLatestByAccountId(Long accountId);

    // 같은 상태로 기록된 마이페이지 한도 변경 이력 조회
    List<AccountStatusLogDTO> selectLimitChangesByAccountId(Long accountId);

    // 반려 후 재신청 구조에서 현재 신청일 조회
    LocalDateTime selectLatestApplicationAt(Long accountId);

    // 처리된 계좌 분류
    int countByNewStatusBetween(Status newStatus, LocalDateTime start, LocalDateTime end);
}
