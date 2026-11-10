package com.app.maria.domain.customer.service;

import com.app.maria.domain.customer.dto.response.CustomerSearchResponseDTO;
import java.util.List;

public interface CustomerSearchService {

    List<CustomerSearchResponseDTO> searchEligibleCustomers(String name);
}
