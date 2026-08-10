package com.app.maria.domain.admin.service;

import com.app.maria.domain.admin.dto.AdminUserDTO;
import com.app.maria.domain.admin.dto.request.AdminLoginRequestDTO;
import com.app.maria.domain.admin.dto.response.AdminLoginResponseDTO;
import com.app.maria.domain.admin.dto.response.AdminSummaryResponseDTO;
import com.app.maria.domain.admin.exception.AdminException;
import com.app.maria.domain.admin.exception.AdminNotFoundException;
import com.app.maria.domain.admin.mapper.AdminMapper;
import com.app.maria.domain.admin.type.AdminRole;
import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.service.AuditLogService;
import com.app.maria.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponseDTO login(AdminLoginRequestDTO request) {
        AdminUserDTO admin = adminMapper.selectAdminByLoginId(request.getLoginId())
                .orElseThrow(() -> new AdminException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new AdminException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(admin.getAdminId(), admin.getLoginId(), admin.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getAdminId());

        return AdminLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void updateRole(Long actorAdminId, Long targetAdminId, AdminRole newRole) {
        AdminUserDTO admin = adminMapper.selectAdminByAdminId(targetAdminId)
                .orElseThrow(() -> new AdminNotFoundException("대상 관리자가 없습니다."));

        adminMapper.updateRole(targetAdminId, newRole);

        auditLogService.log(AuditLogDTO.builder()
                .adminId(actorAdminId)
                .targetTable("ADMIN_USER")
                .targetPk(String.valueOf(targetAdminId))
                .beforeValue(admin.getRole().name())
                .afterValue(newRole.name())
                .reasonCode("ADMIN_ROLE_UPDATE")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponseDTO refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parseClaims(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AdminException("유효하지 않은 토큰입니다.");
        }

        Long adminId;
        try {
            adminId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new AdminException("유효하지 않은 토큰 정보입니다.");
        }

        AdminUserDTO admin = adminMapper.selectAdminByAdminId(adminId)
                .orElseThrow(() -> new AdminNotFoundException("대상 관리자가 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(admin.getAdminId(), admin.getLoginId(), admin.getRole());

        return AdminLoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSummaryResponseDTO> getAllAdmins() {
        List<AdminUserDTO> admins = adminMapper.selectAllAdmins();
        return admins.stream()
                .map(AdminSummaryResponseDTO::new)
                .toList();
    }

}
