package com.app.maria.domain.foreignproduct.service;

import com.app.maria.domain.foreignproduct.dto.response.ForeignProductResponseDTO;
import java.util.List;

public interface ForeignProductService {
    List<ForeignProductResponseDTO> getAllForeignProducts();

    ForeignProductResponseDTO getForeignProduct(Long foreignProductId);
}
