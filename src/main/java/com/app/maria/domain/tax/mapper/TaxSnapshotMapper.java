package com.app.maria.domain.tax.mapper;

import com.app.maria.domain.tax.dto.TaxSnapshotDTO;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaxSnapshotMapper {
    int upsertSnapshots(@Param("snapshots") List<TaxSnapshotDTO> snapshots);

    Optional<TaxSnapshotDTO> selectByAccountId(@Param("accountId") Long accountId);

    List<TaxSnapshotDTO> selectByAccountIds(@Param("accountIds") List<Long> accountIds);
}
