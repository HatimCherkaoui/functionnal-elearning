package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.model.Subscription;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.CourseRepository;
import org.eckmo.functionnal.repository.SubscriptionRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("unused")
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String COURSE_NOT_FOUND = "Course not found";

    // Subscribe user to course
    public Subscription subscribeToCourse(Long userId, Long courseId) {
        log.info("Subscribing user {} to course {}", userId, courseId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException(COURSE_NOT_FOUND));

        // Check if already subscribed
        var existing = subscriptionRepository.findByUserAndCourse(user, course);
        if (existing.isPresent()) {
            existing.get().setIsActive(true);
            return subscriptionRepository.save(existing.get());
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .course(course)
                .isActive(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        // Notify user about new subscription
        notificationService.createNotification(
                userId,
                "Subscription Confirmed",
                "You are now subscribed to " + course.getTitle(),
                "SUBSCRIPTION_CONFIRMED",
                "COURSE",
                courseId
        );

        return saved;
    }

    // Unsubscribe user from course
    public void unsubscribeFromCourse(Long userId, Long courseId) {
        log.info("Unsubscribing user {} from course {}", userId, courseId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException(COURSE_NOT_FOUND));

        subscriptionRepository.findByUserAndCourse(user, course)
                .ifPresent(sub -> {
                    sub.setIsActive(false);
                    subscriptionRepository.save(sub);
                });
    }

    // Get user's active subscriptions
    @Transactional(readOnly = true)
    public List<Subscription> getUserSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return subscriptionRepository.findByUserAndIsActiveTrue(user);
    }

    // Get subscribers for a course
    @Transactional(readOnly = true)
    public List<Subscription> getCourseSubscribers(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return subscriptionRepository.findByCourseAndIsActiveTrue(course);
    }

    // Count subscribers for a course
    @Transactional(readOnly = true)
    public long countCourseSubscribers(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return subscriptionRepository.countActiveSubscribersByCourse(course);
    }

    // Check if user is subscribed to course
    @Transactional(readOnly = true)
    public boolean isSubscribed(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return subscriptionRepository.findByUserAndCourse(user, course)
                .map(Subscription::getIsActive)
                .orElse(false);
    }

    // Notify all subscribers when course is published
    public void notifySubscribersOnPublish(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        getCourseSubscribers(courseId).stream()
                .forEach(sub -> notificationService.createNotification(
                        sub.getUser().getId(),
                        "Course Published",
                        course.getTitle() + " has been published!",
                        "COURSE_PUBLISHED",
                        "COURSE",
                        courseId
                ));

        log.info("Notified {} subscribers about course {} publication",
                getCourseSubscribers(courseId).size(), courseId);
    }
}

