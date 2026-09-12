package com.app.maria.domain.account.api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.maria.domain.account.service.AccountService;
import com.app.maria.global.config.SecurityConfig;
import com.app.maria.global.jwt.JwtTokenProvider;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountApi.class)
@Import(SecurityConfig.class)
class AccountApiSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean AccountService accountService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;

    static Stream<Arguments> writes() {
        return Stream.of("ADMIN", "REVIEWER", "SETTLEMENT", "VIEWER")
                .flatMap(
                        role ->
                                Stream.of(
                                        Arguments.of(
                                                role,
                                                "POST",
                                                "/applications",
                                                "{\"customerId\":1,\"limitAmount\":1000}",
                                                201),
                                        Arguments.of(
                                                role,
                                                "POST",
                                                "/1/reapply",
                                                "{\"limitAmount\":1000}",
                                                200),
                                        Arguments.of(role, "POST", "/1/approve", "{}", 200),
                                        Arguments.of(
                                                role,
                                                "POST",
                                                "/1/reject",
                                                "{\"reason\":\"심사 반려\"}",
                                                200),
                                        Arguments.of(
                                                role,
                                                "POST",
                                                "/1/override",
                                                "{\"reason\":\"재심사 승인\"}",
                                                200),
                                        Arguments.of(
                                                role,
                                                "PUT",
                                                "/update/limit",
                                                "{\"customerId\":1,\"expectedCurrentLimit\":1000,\"limitAmount\":2000}",
                                                200)));
    }

    @ParameterizedTest
    @MethodSource("writes")
    void onlyReviewerCanWrite(String role, String method, String path, String body, int success)
            throws Exception {
        boolean allowed = role.equals("REVIEWER");
        mockMvc.perform(
                        request(HttpMethod.valueOf(method), "/api/account" + path)
                                .with(user("operator").roles(role))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().is(allowed ? success : 403));
        if (!allowed) verifyNoInteractions(accountService);
    }

    static Stream<Arguments> reads() {
        return Stream.of("ADMIN", "REVIEWER", "SETTLEMENT", "VIEWER")
                .flatMap(
                        role ->
                                Stream.of(
                                                "/list",
                                                "/requiring-action-count",
                                                "/available-limit?customerId=1",
                                                "/1",
                                                "/1/status-logs",
                                                "/1/management-detail",
                                                "/search?customerName=Kim")
                                        .map(path -> Arguments.of(role, path)));
    }

    @ParameterizedTest
    @MethodSource("reads")
    void onlyBusinessRolesCanRead(String role, String path) throws Exception {
        mockMvc.perform(get("/api/account" + path).with(user("operator").roles(role)))
                .andExpect(status().is(role.equals("ADMIN") ? 403 : 200));
        if (role.equals("ADMIN")) verifyNoInteractions(accountService);
    }
}
