package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.CourseDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    public static final String TOPIC_COURSE_PUBLISHED = "course.published";
    public static final String TOPIC_COURSE_UPDATED   = "course.updated";
    public static final String TOPIC_USER_REGISTERED  = "user.registered";
    public static final String TOPIC_ENROLLMENT       = "enrollment.created";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishCourse(CourseDTO course) {
        try {
            String msg = String.format("Course Published: %s [%s] $%.2f",
                    course.getTitle(), course.getCategory(), course.getPrice());
            kafkaTemplate.send(TOPIC_COURSE_PUBLISHED, String.valueOf(course.getId()), msg);
            log.info("Kafka → {}: {}", TOPIC_COURSE_PUBLISHED, msg);
        } catch (Exception e) {
            log.warn("Kafka not available: {}", e.getMessage());
        }
    }

    public void publishCourseUpdate(CourseDTO course) {
        try {
            String msg = String.format("Course Updated: %s [%s]",
                    course.getTitle(), course.getCategory());
            kafkaTemplate.send(TOPIC_COURSE_UPDATED, String.valueOf(course.getId()), msg);
            log.info("Kafka → {}: {}", TOPIC_COURSE_UPDATED, msg);
        } catch (Exception e) {
            log.warn("Kafka not available: {}", e.getMessage());
        }
    }

    public void publishUserRegistered(String email, String role) {
        try {
            String msg = String.format("User Registered: %s as %s", email, role);
            kafkaTemplate.send(TOPIC_USER_REGISTERED, email, msg);
            log.info("Kafka → {}: {}", TOPIC_USER_REGISTERED, msg);
        } catch (Exception e) {
            log.warn("Kafka not available: {}", e.getMessage());
        }
    }

    public void publishEnrollment(Long userId, Long courseId) {
        try {
            String msg = String.format("Enrollment: user=%d course=%d", userId, courseId);
            kafkaTemplate.send(TOPIC_ENROLLMENT, String.valueOf(userId), msg);
            log.info("Kafka → {}: {}", TOPIC_ENROLLMENT, msg);
        } catch (Exception e) {
            log.warn("Kafka not available: {}", e.getMessage());
        }
    }
}
