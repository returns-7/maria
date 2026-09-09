package com.app.maria.global.config;

import com.app.maria.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_URLS = {
        "/",
        "/favicon.ico",

        // 정적 리소스와 SPA 진입점
        "/css/**",
        "/js/**",
        "/images/**",

        // Thymeleaf 페이지 셸: 페이지 자체는 공개, 데이터는 JS가 JWT로 API 호출 시 검증
        // 여기에는 PageController의 뷰(HTML) 라우트만 추가할 것 - API는 절대 여기 넣지 말 것
        "/login",
        "/admin/statistics",
        "/admin/target-products",
        "/admin/sell-orders",
        "/admin/account",
        "/admin/audit-log",
        "/admin/settlement",
        "/admin/inbound",
        "/admin/admin-users",
        "/admin/tax",
        "/admin/domestic-investment",

        // JWT 인증 API
        "/api/auth/admin/login",
        "/api/auth/admin/refresh",

        // Swagger
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",

        // 인출·계좌 해지 관리 화면
        "/admin/withdrawals",
        "/admin/account-closures"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT는 서버 세션을 사용하지 않는다.
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Access Token을 Authorization 헤더로 전달하는 Stateless API 기준 설정이다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(PUBLIC_URLS)
                                        .permitAll()

                                        // 화면 전환은 SPA가 담당하고 실제 데이터 접근은 API에서 검증한다.
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(
                                                (request, response, ex) ->
                                                        writeJsonError(
                                                                response,
                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                "UNAUTHORIZED",
                                                                "로그인이 필요합니다."))
                                        .accessDeniedHandler(
                                                (request, response, ex) ->
                                                        writeJsonError(
                                                                response,
                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                "FORBIDDEN",
                                                                "접근 권한이 없습니다.")))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeJsonError(
            HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(
                        """
        {
          "code": "%s",
          "message": "%s"
        }
        """
                                .formatted(code, message));
    }
}
