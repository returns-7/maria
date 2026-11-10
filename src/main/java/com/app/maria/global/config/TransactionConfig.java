package com.app.maria.global.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 업무 DB 작업에 사용하는 트랜잭션 매니저 설정.
 *
 * <p>Settlement Job의 Tasklet 제어 트랜잭션은 ResourcelessTransactionManager를 사용하지만, 실제 DB 변경은 반드시 이 매니저를
 * 사용해야 한다.
 */
@Configuration
public class TransactionConfig {

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
