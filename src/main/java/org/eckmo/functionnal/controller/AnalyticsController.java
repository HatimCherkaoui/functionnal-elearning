package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Analytics controller demonstrating advanced functional streaming operations
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Calculate total revenue
     */
    @GetMapping("/revenue/total")
    public ResponseEntity<Double> getTotalRevenue() {
        log.info("GET /api/analytics/revenue/total - Calculating total revenue");
        return ResponseEntity.ok(analyticsService.calculateTotalRevenue());
    }

    /**
     * Get top performing students
     */
    @GetMapping("/students/top")
    public ResponseEntity<List<Map<String, Object>>> getTopStudents(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/students/top - Fetching top {} students", limit);
        return ResponseEntity.ok(analyticsService.getTopPerformingStudents(limit));
    }

    /**
     * Get course statistics
     */
    @GetMapping("/courses/statistics")
    public ResponseEntity<List<Map<String, Object>>> getCourseStatistics() {
        log.info("GET /api/analytics/courses/statistics - Fetching course statistics");
        return ResponseEntity.ok(analyticsService.getCourseStatistics());
    }

    /**
     * Get enrollment trends by category
     */
    @GetMapping("/enrollments/by-category")
    public ResponseEntity<Map<String, Long>> getEnrollmentTrends() {
        log.info("GET /api/analytics/enrollments/by-category - Fetching enrollment trends");
        return ResponseEntity.ok(analyticsService.getEnrollmentTrendsByCategory());
    }

    /**
     * Get completion rate by category
     */
    @GetMapping("/completion-rate/by-category")
    public ResponseEntity<Map<String, Double>> getCompletionRate() {
        log.info("GET /api/analytics/completion-rate/by-category - Fetching completion rates");
        return ResponseEntity.ok(analyticsService.getCompletionRateByCategory());
    }

    /**
     * Get user engagement metrics
     */
    @GetMapping("/engagement/metrics")
    public ResponseEntity<Map<String, Object>> getUserEngagementMetrics() {
        log.info("GET /api/analytics/engagement/metrics - Fetching engagement metrics");
        return ResponseEntity.ok(analyticsService.getUserEngagementMetrics());
    }

    /**
     * Get popular categories
     */
    @GetMapping("/categories/popular")
    public ResponseEntity<List<String>> getPopularCategories(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/categories/popular - Fetching popular categories");
        return ResponseEntity.ok(analyticsService.getPopularCategories(limit));
    }

    /**
     * Get revenue by category
     */
    @GetMapping("/revenue/by-category")
    public ResponseEntity<Map<String, Double>> getRevenueByCategory() {
        log.info("GET /api/analytics/revenue/by-category - Fetching revenue by category");
        return ResponseEntity.ok(analyticsService.getRevenueByCategory());
    }

    /**
     * Get instructor performance
     */
    @GetMapping("/instructors/performance")
    public ResponseEntity<List<Map<String, Object>>> getInstructorPerformance() {
        log.info("GET /api/analytics/instructors/performance - Fetching instructor performance");
        return ResponseEntity.ok(analyticsService.getInstructorPerformance());
    }

    /**
     * Get learning path recommendations for a user
     */
    @GetMapping("/learning-path/{userId}")
    public ResponseEntity<Map<String, List<String>>> getLearningPath(@PathVariable Long userId) {
        log.info("GET /api/analytics/learning-path/{} - Fetching learning path", userId);
        return ResponseEntity.ok(analyticsService.getRecommendedLearningPaths(userId));
    }
}

