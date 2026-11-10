package com.app.maria.domain.foreignproduct.dto;

import com.app.maria.domain.foreignproduct.type.ForeignProductType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ForeignProductDTO {
    private Long foreignProductId;
    private String ticker;
    private String name;
    private String market;
    private String currency;
    private ForeignProductType foreignProductType;
}
