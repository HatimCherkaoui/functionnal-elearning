package org.eckmo.functionnal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DataInitializationConfig is disabled – data is now managed by Liquibase migrations.
 * See src/main/resources/db/changelog/
 *   001-initial-schema.yaml   → full schema
 *   003-seed-admin-user.yaml  → admin@elearning.com / admin123
 */
@Configuration
@Profile("disabled")
@Slf4j
public class DataInitializationConfig {
}
