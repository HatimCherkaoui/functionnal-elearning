package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.EnrollmentDTO;
import org.eckmo.functionnal.functional.CertificateGenerator;
import org.eckmo.functionnal.functional.EnrollmentValidator;
import org.eckmo.functionnal.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Get all enrollments
     */
    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        log.info("GET /api/enrollments - Fetching all enrollments");
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    /**
     * Get user's active enrollments
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<EnrollmentDTO>> getUserActiveEnrollments(@PathVariable Long userId) {
        log.info("GET /api/enrollments/user/{}/active - Fetching active enrollments", userId);
        return ResponseEntity.ok(enrollmentService.getUserActiveEnrollments(userId));
    }

    /**
     * Get enrollment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        log.info("GET /api/enrollments/{} - Fetching enrollment", id);
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    /**
     * Enroll user in a course
     */
    @PostMapping
    public ResponseEntity<EnrollmentDTO> enrollUserInCourse(@RequestBody EnrollmentDTO enrollmentDTO) {
        log.info("POST /api/enrollments - Enrolling user {} in course {}",
                enrollmentDTO.getUserId(), enrollmentDTO.getCourseId());
        EnrollmentDTO created = enrollmentService.enrollUserInCourse(
                enrollmentDTO.getUserId(),
                enrollmentDTO.getCourseId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update enrollment progress
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<EnrollmentDTO> updateEnrollmentProgress(
            @PathVariable Long id,
            @RequestParam Double progress) {
        log.info("PUT /api/enrollments/{}/progress - Updating progress", id);
        return ResponseEntity.ok(enrollmentService.updateEnrollmentProgress(id, progress));
    }

    /**
     * Update enrollment score
     */
    @PutMapping("/{id}/score")
    public ResponseEntity<EnrollmentDTO> updateEnrollmentScore(
            @PathVariable Long id,
            @RequestParam Double score) {
        log.info("PUT /api/enrollments/{}/score - Updating score", id);
        return ResponseEntity.ok(enrollmentService.updateEnrollmentScore(id, score));
    }

    /**
     * Complete enrollment with certificate generation
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<EnrollmentDTO> completeEnrollment(@PathVariable Long id) {
        log.info("PUT /api/enrollments/{}/complete - Completing enrollment", id);

        // Functional validator implementation
        EnrollmentValidator validator = (userId, courseId, score) -> score >= 70.0;

        // Functional certificate generator implementation
        CertificateGenerator certificateGenerator = (userName, courseName, code) ->
                String.format("Certificate for %s in %s - Code: %s", userName, courseName, code);

        return ResponseEntity.ok(enrollmentService.completeEnrollment(id, validator, certificateGenerator));
    }

    /**
     * Get course enrollments sorted by score
     */
    @GetMapping("/course/{courseId}/by-score")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollmentsByScore(@PathVariable Long courseId) {
        log.info("GET /api/enrollments/course/{}/by-score - Fetching enrollments", courseId);
        return ResponseEntity.ok(enrollmentService.getCourseEnrollmentsByScore(courseId));
    }

    /**
     * Get completed enrollments
     */
    @GetMapping("/completed")
    public ResponseEntity<List<EnrollmentDTO>> getCompletedEnrollments() {
        log.info("GET /api/enrollments/completed - Fetching completed enrollments");
        return ResponseEntity.ok(enrollmentService.getCompletedEnrollments());
    }

    /**
     * Calculate average score for a course
     */
    @GetMapping("/course/{courseId}/average-score")
    public ResponseEntity<Double> calculateAverageScore(@PathVariable Long courseId) {
        log.info("GET /api/enrollments/course/{}/average-score - Calculating average", courseId);
        return ResponseEntity.ok(enrollmentService.calculateAverageScoreByCourse(courseId));
    }

    /**
     * Count active enrollments for a course
     */
    @GetMapping("/course/{courseId}/active-count")
    public ResponseEntity<Long> countActiveEnrollments(@PathVariable Long courseId) {
        log.info("GET /api/enrollments/course/{}/active-count - Counting active enrollments", courseId);
        return ResponseEntity.ok(enrollmentService.countActiveEnrollmentsByCourse(courseId));
    }

    /**
     * Drop enrollment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> dropEnrollment(@PathVariable Long id) {
        log.info("DELETE /api/enrollments/{} - Dropping enrollment", id);
        enrollmentService.dropEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}

