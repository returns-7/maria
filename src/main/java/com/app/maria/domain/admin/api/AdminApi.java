package com.app.maria.domain.admin.api;
import com.app.maria.domain.admin.dto.request.AdminLoginRequestDTO;
import com.app.maria.domain.admin.dto.request.AdminRefreshRequestDTO;
import com.app.maria.domain.admin.dto.request.AdminRoleUpdateRequestDTO;
import com.app.maria.domain.admin.dto.response.AdminLoginResponseDTO;
import com.app.maria.domain.admin.dto.response.AdminSummaryResponseDTO;
import com.app.maria.domain.admin.service.AdminService;
import com.app.maria.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin")
public class AdminApi {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AdminLoginResponseDTO>> login(@Valid @RequestBody AdminLoginRequestDTO request) {
        AdminLoginResponseDTO response = adminService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.of("로그인 성공", response));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AdminLoginResponseDTO>> refresh(@Valid @RequestBody AdminRefreshRequestDTO request) {
        AdminLoginResponseDTO response = adminService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponseDTO.of("토큰이 재발급되었습니다.", response));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{adminId}/role")
    public ResponseEntity<ApiResponseDTO<Void>> updateRole(@AuthenticationPrincipal Long actorAdminId, @PathVariable Long adminId, @Valid @RequestBody AdminRoleUpdateRequestDTO request) {
        adminService.updateRole(actorAdminId, adminId, request.getRole());
        return ResponseEntity.ok(ApiResponseDTO.of("역할이 변경되었습니다."));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AdminSummaryResponseDTO>>> getAllAdmins() {
        List<AdminSummaryResponseDTO> list = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponseDTO.of("조회 성공", list));
    }
}
