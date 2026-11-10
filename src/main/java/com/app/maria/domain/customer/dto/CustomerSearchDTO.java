package com.app.maria.domain.customer.dto;

import java.time.LocalDate;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomerSearchDTO {

    private Long customerId;
    private String name;
    private LocalDate birthDate;
    private String phone;
    private String investorType;
}
