package com.app.maria.domain.admin.api;

import com.app.maria.domain.admin.dto.request.AdminLoginRequestDTO;
import com.app.maria.domain.admin.dto.request.AdminRefreshRequestDTO;
import com.app.maria.domain.admin.dto.response.AdminLoginResponseDTO;
import com.app.maria.domain.admin.service.AdminService;
import com.app.maria.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin")
public class AdminAuthApi {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AdminLoginResponseDTO>> login(
            @Valid @RequestBody AdminLoginRequestDTO request) {
        // ponytail: body 반환 유지 — Task 3에서 HttpOnly 쿠키로 전환
        AdminLoginResponseDTO response = adminService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.of("로그인 성공", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AdminLoginResponseDTO>> refresh(
            @Valid @RequestBody AdminRefreshRequestDTO request) {
        // ponytail: @RequestBody — Task 3에서 @CookieValue로 전환
        AdminLoginResponseDTO response = adminService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponseDTO.of("토큰이 재발급되었습니다.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout() {
        // ponytail: 쿠키 만료 처리는 Task 3에서 추가
        return ResponseEntity.ok(ApiResponseDTO.of("로그아웃되었습니다."));
    }
}
