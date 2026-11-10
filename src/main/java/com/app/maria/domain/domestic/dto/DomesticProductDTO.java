package com.app.maria.domain.domestic.dto;

import com.app.maria.domain.domestic.type.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticProductDTO {
    private Long domesticProductId;
    private String ticker;
    private String name;
    private String market;
    private Type type;
    private BigDecimal domesticStockRatio;
    private LocalDate inceptionDate;
}
