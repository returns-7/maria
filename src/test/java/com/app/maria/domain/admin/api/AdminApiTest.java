package com.app.maria.domain.admin.api;

import com.app.maria.domain.admin.dto.request.AdminLoginRequestDTO;
import com.app.maria.domain.admin.dto.request.AdminRefreshRequestDTO;
import com.app.maria.domain.admin.dto.request.AdminRoleUpdateRequestDTO;
import com.app.maria.domain.admin.dto.response.AdminLoginResponseDTO;
import com.app.maria.domain.admin.exception.AdminException;
import com.app.maria.domain.admin.exception.AdminNotFoundException;
import com.app.maria.domain.admin.service.AdminService;
import com.app.maria.domain.admin.type.AdminRole;
import com.app.maria.global.config.SecurityConfig;
import com.app.maria.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminApi.class)
@Import(SecurityConfig.class)
class AdminApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AdminService adminService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    private AdminLoginRequestDTO loginRequest(String loginId, String password) {
        return AdminLoginRequestDTO.builder()
                .loginId(loginId)
                .password(password)
                .build();
    }

    private AdminRoleUpdateRequestDTO roleUpdateRequest(AdminRole role) {
        return AdminRoleUpdateRequestDTO.builder()
                .role(role)
                .build();
    }

    private AdminRefreshRequestDTO refreshRequest(String refreshToken) {
        return AdminRefreshRequestDTO.builder()
                .refreshToken(refreshToken)
                .build();
    }

    @Test
    @DisplayName("로그인 성공시 200과 토큰을 반환한다")
    void loginReturns200WithTokensOnSuccess() throws Exception {
        AdminLoginRequestDTO request = loginRequest("reviewer1", "raw-password");
        AdminLoginResponseDTO response = AdminLoginResponseDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(adminService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("서비스에서 인증 예외가 발생하면 401을 반환한다")
    void loginReturns401WhenServiceThrowsAdminException() throws Exception {
        AdminLoginRequestDTO request = loginRequest("reviewer1", "wrong-password");

        when(adminService.login(any()))
                .thenThrow(new AdminException("아이디 또는 비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("아이디가 없으면 검증 실패로 400을 반환하고 서비스는 호출되지 않는다")
    void loginReturns400WhenLoginIdMissing() throws Exception {
        AdminLoginRequestDTO request = loginRequest(null, "raw-password");

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).login(any());
    }

    @Test
    @DisplayName("비밀번호가 없으면 검증 실패로 400을 반환하고 서비스는 호출되지 않는다")
    void loginReturns400WhenPasswordMissing() throws Exception {
        AdminLoginRequestDTO request = loginRequest("reviewer1", null);

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).login(any());
    }

    @Test
    @DisplayName("ADMIN 권한이면 역할 변경에 성공하고 200을 반환한다")
    @WithMockUser(roles = "ADMIN")
    void updateRoleReturns200WhenCallerIsAdmin() throws Exception {
        AdminRoleUpdateRequestDTO request = roleUpdateRequest(AdminRole.REVIEWER);

        mockMvc.perform(patch("/api/auth/admin/{adminId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("역할이 변경되었습니다."));

        verify(adminService).updateRole(any(), eq(1L), eq(AdminRole.REVIEWER));
    }

    @Test
    @DisplayName("인증되지 않은 요청이면 401을 반환하고 서비스는 호출되지 않는다")
    void updateRoleReturns401WhenNotAuthenticated() throws Exception {
        AdminRoleUpdateRequestDTO request = roleUpdateRequest(AdminRole.REVIEWER);

        mockMvc.perform(patch("/api/auth/admin/{adminId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(adminService, never()).updateRole(any(), anyLong(), any());
    }

    @Test
    @DisplayName("ADMIN이 아니면 403을 반환하고 서비스는 호출되지 않는다")
    @WithMockUser(roles = "VIEWER")
    void updateRoleReturns403WhenCallerIsNotAdmin() throws Exception {
        AdminRoleUpdateRequestDTO request = roleUpdateRequest(AdminRole.REVIEWER);

        mockMvc.perform(patch("/api/auth/admin/{adminId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(adminService, never()).updateRole(any(), anyLong(), any());
    }

    @Test
    @DisplayName("role이 없으면 400을 반환하고 서비스는 호출되지 않는다")
    @WithMockUser(roles = "ADMIN")
    void updateRoleReturns400WhenRoleMissing() throws Exception {
        AdminRoleUpdateRequestDTO request = roleUpdateRequest(null);

        mockMvc.perform(patch("/api/auth/admin/{adminId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).updateRole(any(), anyLong(), any());
    }

    @Test
    @DisplayName("대상 관리자가 없으면 404를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void updateRoleReturns404WhenAdminNotFound() throws Exception {
        AdminRoleUpdateRequestDTO request = roleUpdateRequest(AdminRole.REVIEWER);

        doThrow(new AdminNotFoundException("대상 관리자가 없습니다."))
                .when(adminService).updateRole(any(), eq(1L), eq(AdminRole.REVIEWER));

        mockMvc.perform(patch("/api/auth/admin/{adminId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("대상 관리자가 없습니다."));
    }

    @Test
    @DisplayName("유효한 refreshToken을 보내면 새 accessToken을 받는다")
    void refreshReturns200AndNewAccessTokenWhenTokenValid() throws Exception {
        AdminRefreshRequestDTO request = refreshRequest("valid-refresh-token");
        AdminLoginResponseDTO response = AdminLoginResponseDTO.builder()
                .accessToken("new-access-token")
                .refreshToken("valid-refresh-token")
                .build();

        when(adminService.refresh("valid-refresh-token")).thenReturn(response);

        mockMvc.perform(post("/api/auth/admin/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"));

        verify(adminService).refresh("valid-refresh-token");
    }

    @Test
    @DisplayName("refreshToken이 없으면 400을 반환하고 서비스는 호출되지 않는다")
    void refreshReturns400WhenRefreshTokenMissing() throws Exception {
        AdminRefreshRequestDTO request = refreshRequest(null);

        mockMvc.perform(post("/api/auth/admin/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).refresh(any());
    }

    @Test
    @DisplayName("서비스에서 토큰 예외가 발생하면 401을 반환한다")
    void refreshReturns401WhenTokenInvalid() throws Exception {
        AdminRefreshRequestDTO request = refreshRequest("broken-token");

        when(adminService.refresh("broken-token"))
                .thenThrow(new AdminException("유효하지 않은 토큰입니다."));

        mockMvc.perform(post("/api/auth/admin/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("토큰의 대상 관리자가 없으면 404를 반환한다")
    void refreshReturns404WhenAdminNotFound() throws Exception {
        AdminRefreshRequestDTO request = refreshRequest("valid-refresh-token");

        when(adminService.refresh("valid-refresh-token"))
                .thenThrow(new AdminNotFoundException("대상 관리자가 없습니다."));

        mockMvc.perform(post("/api/auth/admin/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("대상 관리자가 없습니다."));
    }
}
