package com.app.maria.domain.domestic.api;

import com.app.maria.domain.domestic.dto.request.DomesticInvestmentSearchRequestDTO;
import com.app.maria.domain.domestic.dto.response.DomesticAccountDetailResponseDTO;
import com.app.maria.domain.domestic.dto.response.DomesticInvestmentPageResponseDTO;
import com.app.maria.domain.domestic.service.DomesticInvestmentService;
import com.app.maria.domain.domestic.type.DomesticStockStatus;
import com.app.maria.global.response.ApiResponseDTO;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/domestic-investments")
public class DomesticInvestmentApi {

    private final DomesticInvestmentService domesticInvestmentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SETTLEMENT', 'REVIEWER', 'VIEWER')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<DomesticInvestmentPageResponseDTO>> getInvestments(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) DomesticStockStatus status,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size) {
        DomesticInvestmentSearchRequestDTO request =
                DomesticInvestmentSearchRequestDTO.builder()
                        .customerName(customerName)
                        .status(status)
                        .page(page)
                        .size(size)
                        .build();
        DomesticInvestmentPageResponseDTO result =
                new DomesticInvestmentPageResponseDTO(
                        domesticInvestmentService.getInvestments(request));
        return ResponseEntity.ok(ApiResponseDTO.of("국내투자 현황 목록 조회 성공", result));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SETTLEMENT', 'REVIEWER', 'VIEWER')")
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponseDTO<DomesticAccountDetailResponseDTO>> getAccountDetail(
            @PathVariable @Positive Long accountId) {
        DomesticAccountDetailResponseDTO result =
                new DomesticAccountDetailResponseDTO(
                        domesticInvestmentService.getAccountDetail(accountId));
        return ResponseEntity.ok(ApiResponseDTO.of("국내투자 현황 상세 조회 성공", result));
    }
}
