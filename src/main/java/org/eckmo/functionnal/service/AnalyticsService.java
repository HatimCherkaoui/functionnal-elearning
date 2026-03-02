package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.model.Enrollment;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.CourseRepository;
import org.eckmo.functionnal.repository.EnrollmentRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service demonstrating advanced functional programming patterns
 * including reduction, grouping, and complex stream operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Calculate total revenue using functional reduction
     */
    public Double calculateTotalRevenue() {
        log.info("Calculating total revenue");
        return enrollmentRepository.findCompletedEnrollments()
                .stream()
                .map(Enrollment::getCourse)
                .mapToDouble(Course::getPrice)
                .sum();
    }

    /**
     * Get top performing students using functional streaming and sorting
     */
    public List<Map<String, Object>> getTopPerformingStudents(int limit) {
        log.info("Fetching top {} performing students", limit);

        return enrollmentRepository.findCompletedEnrollments()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getUser(),
                        Collectors.averagingDouble(Enrollment::getScore)
                ))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", entry.getKey().getId());
                    result.put("userName", entry.getKey().getFirstName() + " " + entry.getKey().getLastName());
                    result.put("averageScore", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get course statistics using functional grouping and reduction
     */
    public List<Map<String, Object>> getCourseStatistics() {
        log.info("Fetching course statistics");

        return courseRepository.findAll()
                .stream()
                .map(course -> {
                    List<Enrollment> enrollments = course.getEnrollments()
                            .stream()
                            .filter(e -> e.getStatus().name().equals("COMPLETED"))
                            .collect(Collectors.toList());

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("courseId", course.getId());
                    stats.put("courseTitle", course.getTitle());
                    stats.put("totalEnrollments", course.getEnrollments().size());
                    stats.put("completedEnrollments", enrollments.size());
                    stats.put("averageScore", enrollments.isEmpty() ? 0.0 :
                            enrollments.stream()
                                    .mapToDouble(Enrollment::getScore)
                                    .average()
                                    .orElse(0.0));
                    stats.put("revenue", course.getPrice() * enrollments.size());
                    return stats;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get enrollment trends by category using functional grouping
     */
    public Map<String, Long> getEnrollmentTrendsByCategory() {
        log.info("Fetching enrollment trends by category");

        return enrollmentRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCourse().getCategory(),
                        Collectors.counting()
                ));
    }

    /**
     * Get completion rate by course using functional stream operations
     */
    public Map<String, Double> getCompletionRateByCategory() {
        log.info("Fetching completion rate by category");

        return enrollmentRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCourse().getCategory(),
                        Collectors.collectingAndThen(
                                Collectors.partitioningBy(
                                        e -> e.getStatus().name().equals("COMPLETED")
                                ),
                                partition -> {
                                    long completed = partition.get(true).size();
                                    long total = partition.get(true).size() + partition.get(false).size();
                                    return total == 0 ? 0.0 : (double) completed / total * 100;
                                }
                        )
                ));
    }

    /**
     * Get user engagement metrics using functional operations
     */
    public Map<String, Object> getUserEngagementMetrics() {
        log.info("Fetching user engagement metrics");

        List<Enrollment> allEnrollments = enrollmentRepository.findAll();

        return Map.ofEntries(
                Map.entry("totalEnrollments", (long) allEnrollments.size()),
                Map.entry("averageProgress", allEnrollments.stream()
                        .mapToDouble(Enrollment::getProgress)
                        .average()
                        .orElse(0.0)),
                Map.entry("activeEnrollments", allEnrollments.stream()
                        .filter(e -> e.getStatus().name().equals("ACTIVE"))
                        .count()),
                Map.entry("completedEnrollments", allEnrollments.stream()
                        .filter(e -> e.getStatus().name().equals("COMPLETED"))
                        .count()),
                Map.entry("averageScore", allEnrollments.stream()
                        .mapToDouble(Enrollment::getScore)
                        .average()
                        .orElse(0.0))
        );
    }

    /**
     * Get course recommendations based on popular categories
     */
    public List<String> getPopularCategories(int limit) {
        log.info("Fetching popular categories");

        return courseRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Course::getCategory,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get revenue by category using functional reduction
     */
    public Map<String, Double> getRevenueByCategory() {
        log.info("Fetching revenue by category");

        return courseRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Course::getCategory,
                        Collectors.summingDouble(course ->
                                course.getEnrollments()
                                        .stream()
                                        .filter(e -> e.getStatus().name().equals("COMPLETED"))
                                        .count() * course.getPrice()
                        )
                ));
    }

    /**
     * Get instructor performance metrics
     */
    public List<Map<String, Object>> getInstructorPerformance() {
        log.info("Fetching instructor performance metrics");

        return userRepository.findAllInstructors()
                .stream()
                .map(instructor -> {
                    List<Course> courses = courseRepository.findAll()
                            .stream()
                            .filter(c -> c.getId() > 0) // Placeholder - should have instructor_id
                            .collect(Collectors.toList());

                    long totalEnrollments = courses.stream()
                            .mapToLong(c -> c.getEnrollments().size())
                            .sum();

                    Map<String, Object> performance = new HashMap<>();
                    performance.put("instructorId", instructor.getId());
                    performance.put("instructorName", instructor.getFirstName() + " " + instructor.getLastName());
                    performance.put("totalCourses", (long) courses.size());
                    performance.put("totalEnrollments", totalEnrollments);
                    performance.put("averageRating", courses.stream()
                            .mapToDouble(Course::getRating)
                            .average()
                            .orElse(0.0));
                    return performance;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get student learning path recommendations based on completed courses
     */
    public Map<String, List<String>> getRecommendedLearningPaths(Long userId) {
        log.info("Fetching learning path recommendations for user: {}", userId);

        List<String> completedCategories = enrollmentRepository.findAll()
                .stream()
                .filter(e -> e.getUser().getId().equals(userId))
                .filter(e -> e.getStatus().name().equals("COMPLETED"))
                .map(e -> e.getCourse().getCategory())
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<String>> recommendations = new HashMap<>();

        completedCategories.forEach(category -> {
            List<String> relatedCourses = courseRepository.findCoursesByCategory(category)
                    .stream()
                    .filter(c -> c.getLevel() == Course.CourseLevel.ADVANCED)
                    .map(Course::getTitle)
                    .collect(Collectors.toList());

            recommendations.put(category + " - Advanced", relatedCourses);
        });

        return recommendations;
    }
}

