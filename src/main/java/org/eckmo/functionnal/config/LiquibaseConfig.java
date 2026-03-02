package org.eckmo.functionnal.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Explicit Liquibase configuration to ensure migrations run before application startup.
 * This configuration ensures proper database schema initialization in Docker environments.
 * The @Order(1) ensures Liquibase runs before any other beans that might need the database.
 */
@Configuration
@Slf4j
public class LiquibaseConfig {

    @Bean
    @Order(1)
    public SpringLiquibase liquibase(
            DataSource dataSource,
            @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.yaml}") String changeLog,
            @Value("${spring.liquibase.enabled:true}") boolean enabled
    ) {
        log.info("Configuring Liquibase with changelog: {}", changeLog);

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        liquibase.setDropFirst(false);

        log.info("Liquibase configuration completed. Enabled: {}", enabled);

        return liquibase;
    }
}

