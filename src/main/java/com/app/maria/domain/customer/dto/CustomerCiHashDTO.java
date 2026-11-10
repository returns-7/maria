package com.app.maria.domain.customer.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class CustomerCiHashDTO {

    private Long customerId;
    private String ciHash;
}
