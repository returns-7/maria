package com.app.maria.domain.member.mapper;

import com.app.maria.domain.member.dto.MemberDTO;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    public Optional<MemberDTO> selectById(@Param("id") String mid);
}
