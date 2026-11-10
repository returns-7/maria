package com.app.maria.domain.member.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "mid")
public class MemberDTO {
    private String mid;
    private String mname;
}
