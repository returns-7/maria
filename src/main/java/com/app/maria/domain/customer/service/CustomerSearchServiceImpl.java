package com.app.maria.domain.customer.service;

import com.app.maria.domain.customer.dto.response.CustomerSearchResponseDTO;
import com.app.maria.domain.customer.mapper.CustomerMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerSearchServiceImpl implements CustomerSearchService {

    private static final int SEARCH_RESULT_LIMIT = 10;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerSearchResponseDTO> searchEligibleCustomers(String name) {
        return customerMapper
                .searchEligibleCustomersByName(name.trim(), SEARCH_RESULT_LIMIT)
                .stream()
                .map(CustomerSearchResponseDTO::new)
                .toList();
    }
}
