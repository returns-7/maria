package com.app.maria.domain.inbound.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundPageDTO {
    private List<InboundListDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
