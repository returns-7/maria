package com.app.maria.domain.admin.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.maria.domain.admin.dto.AdminUserDTO;
import com.app.maria.domain.admin.type.AdminRole;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private AdminMapper adminMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-admin-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource =
                (PooledDataSource)
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        adminMapper = sqlSession.getMapper(AdminMapper.class);
    }

    @AfterEach
    void closeSession() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) {
            dataSource.forceCloseAll();
        }
    }

    @Test
    @DisplayName("존재하는 로그인 아이디로 조회하면 관리자 정보를 반환한다")
    void selectAdminByLoginIdReturnsAdminWhenExists() throws SQLException {
        insertAdmin(1L, "reviewer1", "리뷰어1", "encoded-password", "REVIEWER");

        Optional<AdminUserDTO> result = adminMapper.selectAdminByLoginId("reviewer1");

        assertThat(result).isPresent();
        assertThat(result.get().getAdminId()).isEqualTo(1L);
        assertThat(result.get().getLoginId()).isEqualTo("reviewer1");
        assertThat(result.get().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(result.get().getRole()).isEqualTo(AdminRole.REVIEWER);
    }

    @Test
    @DisplayName("존재하지 않는 로그인 아이디로 조회하면 빈 값을 반환한다")
    void selectAdminByLoginIdReturnsEmptyWhenNotExists() {
        Optional<AdminUserDTO> result = adminMapper.selectAdminByLoginId("nobody");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("role을 지정하지 않고 생성하면 기본값 VIEWER로 저장된다")
    void insertAdminWithoutRoleDefaultsToViewer() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO admin_user (admin_id, login_id, name, password_hash)
                    VALUES (1, 'newbie', '신입 관리자', 'encoded-password')
                    """);
        }

        Optional<AdminUserDTO> result = adminMapper.selectAdminByLoginId("newbie");

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(AdminRole.VIEWER);
    }

    @Test
    @DisplayName("정의되지 않은 role 값은 CHECK 제약조건으로 저장 자체가 막힌다")
    void invalidRoleValueIsRejectedByCheckConstraint() {
        assertThatThrownBy(
                        () -> insertAdmin(1L, "reviewer1", "리뷰어1", "encoded-password", "SUPERUSER"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("같은 로그인 아이디는 두 번 저장할 수 없다")
    void loginIdMustBeUnique() throws SQLException {
        insertAdmin(1L, "reviewer1", "리뷰어1", "encoded-password", "REVIEWER");

        assertThatThrownBy(
                        () ->
                                insertAdmin(
                                        2L,
                                        "reviewer1",
                                        "리뷰어1(다른 계정)",
                                        "another-password",
                                        "VIEWER"))
                .isInstanceOf(SQLException.class);
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute(
                    """
                    CREATE TABLE admin_user (
                        admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        login_id VARCHAR(50) NOT NULL,
                        name VARCHAR(50) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
                        CONSTRAINT uq_admin_user_login_id UNIQUE (login_id),
                        CONSTRAINT chk_admin_user_role CHECK (
                            role IN ('VIEWER', 'REVIEWER', 'SETTLEMENT', 'ADMIN')
                        )
                    )
                    """);
        }
    }

    private void insertAdmin(
            Long adminId, String loginId, String name, String passwordHash, String role)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO admin_user (admin_id, login_id, name, password_hash, role)
                    VALUES (%d, '%s', '%s', '%s', '%s')
                    """
                            .formatted(adminId, loginId, name, passwordHash, role));
        }
    }
}
