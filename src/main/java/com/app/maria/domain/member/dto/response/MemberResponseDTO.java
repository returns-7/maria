package com.app.maria.domain.member.dto.response;

import com.app.maria.domain.member.dto.MemberDTO;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "mid")
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDTO implements Serializable {
    private String mid;
    private String mname;

    public MemberResponseDTO(MemberDTO memberDTO) {
        this.mid = memberDTO.getMid();
        this.mname = memberDTO.getMname();
    }
}
