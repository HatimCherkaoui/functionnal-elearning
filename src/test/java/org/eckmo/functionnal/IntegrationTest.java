package org.eckmo.functionnal;

import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.CourseRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.eckmo.functionnal.service.KafkaConsumerService;
import org.eckmo.functionnal.service.KafkaProducerService;
import org.eckmo.functionnal.dto.CourseDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Full integration test using Testcontainers:
 *  - Real MySQL 8 via Testcontainers
 *  - Liquibase migrations run on startup → schema + seed data verified
 *  - Real Kafka via Testcontainers
 *  - All 4 topics created and functional
 *  - Producer sends, consumer receives verified
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestLiquibaseConfig.class)
class IntegrationTest {

    // ── Containers ────────────────────────────────────────────────────────────

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("elearning_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(false);

    // ── Dynamic properties ────────────────────────────────────────────────────

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      mysql::getJdbcUrl);
        registry.add("spring.datasource.username",  mysql::getUsername);
        registry.add("spring.datasource.password",  mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.liquibase.enabled",    () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    // ── Injected beans ────────────────────────────────────────────────────────

    @Autowired JdbcTemplate           jdbc;
    @Autowired UserRepository         userRepository;
    @Autowired CourseRepository       courseRepository;
    @Autowired KafkaProducerService   kafkaProducer;
    @Autowired KafkaConsumerService   kafkaConsumer;
    @Autowired KafkaAdmin             kafkaAdmin;

    // ═════════════════════════════════════════════════════════════════════════
    // 1. Application context loads
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("1 · Application context loads successfully")
    void contextLoads() {
        assertThat(mysql.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. Liquibase migrations – schema integrity
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("2 · Liquibase: all expected tables exist")
    void liquibase_allTablesCreated() {
        List<String> tables = jdbc.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME",
                String.class);

        assertThat(tables).as("Required tables should exist after Liquibase migration")
                .contains("users", "courses", "enrollments", "lessons",
                          "certificates", "notifications", "subscriptions",
                          "instructor_documents", "DATABASECHANGELOG");
    }

    @Test
    @Order(3)
    @DisplayName("3 · Liquibase: DATABASECHANGELOG has all 4 changesets recorded")
    void liquibase_changelogRecorded() {
        List<String> changeIds = jdbc.queryForList(
                "SELECT ID FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED",
                String.class);

        assertThat(changeIds)
                .as("All changesets must be recorded in DATABASECHANGELOG")
                .contains(
                    "001-create-users-table",
                    "001-create-courses-table",
                    "002-add-instructor-approval-fields",
                    "002-create-instructor-documents-table",
                    "003-seed-admin-user"
                );
    }

    @Test
    @Order(4)
    @DisplayName("4 · Liquibase seed: admin user exists with correct role")
    void liquibase_adminUserSeeded() {
        var adminOpt = userRepository.findByEmail("admin@elearning.com");

        assertThat(adminOpt).as("Admin user must be seeded by Liquibase changeset 003").isPresent();

        User admin = adminOpt.get();
        assertThat(admin.getFirstName()).isEqualTo("Admin");
        assertThat(admin.getLastName()).isEqualTo("User");
        assertThat(admin.getRole()).isEqualTo(User.UserRole.ADMIN);
        assertThat(admin.getIsActive()).isTrue();
        assertThat(admin.getPassword()).as("Password must be BCrypt hashed")
                .startsWith("$2a$");
    }

    @Test
    @Order(5)
    @DisplayName("5 · Liquibase: users table has all new columns (approval, bio, profile_picture)")
    void liquibase_newColumnsExist() {
        List<String> columns = jdbc.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' " +
                "ORDER BY ORDINAL_POSITION",
                String.class);

        assertThat(columns).contains(
                "id", "email", "password", "role",
                "approval_status", "rejection_reason", "bio", "profile_picture",
                "is_active", "created_at", "updated_at");
    }

    @Test
    @Order(6)
    @DisplayName("6 · Liquibase: instructor_documents table has correct FK to users")
    void liquibase_instructorDocumentsTable() {
        // Table exists
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'instructor_documents'",
                Integer.class);
        assertThat(count).as("instructor_documents table must exist").isEqualTo(1);

        // FK exists
        Integer fkCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'instructor_documents' " +
                "AND REFERENCED_TABLE_NAME = 'users'",
                Integer.class);
        assertThat(fkCount).as("FK from instructor_documents to users must exist").isGreaterThan(0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. JPA / Repository layer
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("7 · JPA: can save and retrieve a User entity")
    void jpa_saveAndRetrieveUser() {
        User u = User.builder()
                .firstName("Test").lastName("Student")
                .email("student@test.com")
                .password("$2a$10$xxxx")
                .role(User.UserRole.STUDENT)
                .isActive(true)
                .approvalStatus(User.ApprovalStatus.APPROVED)
                .build();

        User saved = userRepository.save(u);
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("student@test.com");
        assertThat(found.getRole()).isEqualTo(User.UserRole.STUDENT);
    }

    @Test
    @Order(8)
    @DisplayName("8 · JPA: can save and retrieve a Course entity")
    void jpa_saveAndRetrieveCourse() {
        Course c = Course.builder()
                .title("Spring Boot Integration Testing")
                .description("Learn integration testing with Spring Boot and Testcontainers")
                .category("Backend")
                .level(Course.CourseLevel.INTERMEDIATE)
                .price(49.99)
                .isPublished(false)
                .build();

        Course saved = courseRepository.save(c);
        assertThat(saved.getId()).isNotNull();

        Course found = courseRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Spring Boot Integration Testing");
        assertThat(found.getPrice()).isEqualTo(49.99);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 4. Kafka – topics created + produce + consume
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("9 · Kafka: all 4 topics are created on startup")
    void kafka_topicsCreated() throws Exception {
        var adminClient = org.apache.kafka.clients.admin.AdminClient.create(
                kafkaAdmin.getConfigurationProperties());

        var topics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
        adminClient.close();

        assertThat(topics).as("All application Kafka topics must exist")
                .contains(
                    KafkaProducerService.TOPIC_COURSE_PUBLISHED,
                    KafkaProducerService.TOPIC_COURSE_UPDATED,
                    KafkaProducerService.TOPIC_USER_REGISTERED,
                    KafkaProducerService.TOPIC_ENROLLMENT
                );
    }

    @Test
    @Order(10)
    @DisplayName("10 · Kafka: publishCourse sends message and consumer receives it")
    void kafka_publishCourseFlowEndToEnd() {
        kafkaConsumer.getReceivedMessages().clear();

        CourseDTO dto = CourseDTO.builder()
                .id(1L).title("Kafka Test Course")
                .category("Testing").price(19.99).build();

        kafkaProducer.publishCourse(dto);

        await().atMost(Duration.ofSeconds(15))
               .pollInterval(Duration.ofMillis(300))
               .untilAsserted(() ->
                   assertThat(kafkaConsumer.getReceivedMessages())
                       .anyMatch(m -> m.contains("Kafka Test Course"))
               );
    }

    @Test
    @Order(11)
    @DisplayName("11 · Kafka: publishUserRegistered message received by consumer")
    void kafka_publishUserRegistered() {
        kafkaConsumer.getReceivedMessages().clear();

        kafkaProducer.publishUserRegistered("newuser@test.com", "STUDENT");

        await().atMost(Duration.ofSeconds(15))
               .pollInterval(Duration.ofMillis(300))
               .untilAsserted(() ->
                   assertThat(kafkaConsumer.getReceivedMessages())
                       .anyMatch(m -> m.contains("newuser@test.com"))
               );
    }

    @Test
    @Order(12)
    @DisplayName("12 · Kafka: publishEnrollment message received by consumer")
    void kafka_publishEnrollment() {
        kafkaConsumer.getReceivedMessages().clear();

        kafkaProducer.publishEnrollment(42L, 7L);

        await().atMost(Duration.ofSeconds(15))
               .pollInterval(Duration.ofMillis(300))
               .untilAsserted(() ->
                   assertThat(kafkaConsumer.getReceivedMessages())
                       .anyMatch(m -> m.contains("user=42") && m.contains("course=7"))
               );
    }

    @Test
    @Order(13)
    @DisplayName("13 · Kafka: publishCourseUpdate message received by consumer")
    void kafka_publishCourseUpdate() {
        kafkaConsumer.getReceivedMessages().clear();

        CourseDTO dto = CourseDTO.builder()
                .id(2L).title("Updated Course Title")
                .category("DevOps").price(29.99).build();

        kafkaProducer.publishCourseUpdate(dto);

        await().atMost(Duration.ofSeconds(15))
               .pollInterval(Duration.ofMillis(300))
               .untilAsserted(() ->
                   assertThat(kafkaConsumer.getReceivedMessages())
                       .anyMatch(m -> m.contains("Updated Course Title"))
               );
    }
}

