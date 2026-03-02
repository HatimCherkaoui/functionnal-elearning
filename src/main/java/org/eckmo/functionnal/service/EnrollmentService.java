package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.EnrollmentDTO;
import org.eckmo.functionnal.exception.ResourceNotFoundException;
import org.eckmo.functionnal.functional.CertificateGenerator;
import org.eckmo.functionnal.functional.EnrollmentValidator;
import org.eckmo.functionnal.model.Certificate;
import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.model.Enrollment;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.CertificateRepository;
import org.eckmo.functionnal.repository.CourseRepository;
import org.eckmo.functionnal.repository.EnrollmentRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CertificateRepository certificateRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert Enrollment entity to EnrollmentDTO using functional mapping
     */
    private final Function<Enrollment, EnrollmentDTO> ENROLLMENT_TO_DTO = enrollment -> EnrollmentDTO.builder()
            .id(enrollment.getId())
            .userId(enrollment.getUser().getId())
            .courseId(enrollment.getCourse().getId())
            .progress(enrollment.getProgress())
            .score(enrollment.getScore())
            .status(enrollment.getStatus().name())
            .enrolledAt(enrollment.getEnrolledAt().format(DATE_FORMATTER))
            .completedAt(Optional.ofNullable(enrollment.getCompletedAt())
                    .map(date -> date.format(DATE_FORMATTER))
                    .orElse(null))
            .build();

    /**
     * Predicate for filtering active enrollments
     */
    private final Predicate<Enrollment> IS_ACTIVE = enrollment ->
            enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE;

    /**
     * Predicate for filtering completed enrollments
     */
    private final Predicate<Enrollment> IS_COMPLETED = enrollment ->
            enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED;

    /**
     * Get all enrollments
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        log.info("Fetching all enrollments");
        return enrollmentRepository.findAll()
                .stream()
                .map(ENROLLMENT_TO_DTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> getAllEnrollmentsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        return enrollmentRepository.findAll(pageable).map(ENROLLMENT_TO_DTO);
    }

    /**
     * Get user's active enrollments using functional stream
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getUserActiveEnrollments(Long userId) {
        log.info("Fetching active enrollments for user: {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return enrollmentRepository.findActiveEnrollmentsByUserId(userId)
                .stream()
                .filter(IS_ACTIVE)
                .map(ENROLLMENT_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get enrollment by ID
     */
    @Transactional(readOnly = true)
    public EnrollmentDTO getEnrollmentById(Long id) {
        log.info("Fetching enrollment with id: {}", id);
        return enrollmentRepository.findById(id)
                .map(ENROLLMENT_TO_DTO)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }

    /**
     * Enroll user in a course
     */
    public EnrollmentDTO enrollUserInCourse(Long userId, Long courseId) {
        log.info("Enrolling user {} in course {}", userId, courseId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if already enrolled using functional operation
        enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .ifPresent(e -> {
                    throw new IllegalArgumentException("User is already enrolled in this course");
                });

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .progress(0.0)
                .score(0.0)
                .status(Enrollment.EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("User enrolled successfully with enrollment id: {}", savedEnrollment.getId());
        return ENROLLMENT_TO_DTO.apply(savedEnrollment);
    }

    /**
     * Update enrollment progress
     */
    public EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, Double progress) {
        log.info("Updating enrollment {} progress to {}", enrollmentId, progress);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setProgress(Math.min(progress, 100.0));
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment progress updated successfully");
        return ENROLLMENT_TO_DTO.apply(updatedEnrollment);
    }

    /**
     * Complete enrollment and optionally generate certificate using functional interface
     */
    public EnrollmentDTO completeEnrollment(Long enrollmentId, EnrollmentValidator validator,
                                           CertificateGenerator certificateGenerator) {
        log.info("Completing enrollment: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        // Validate completion using functional interface
        if (!validator.validate(enrollment.getUser().getId(), enrollment.getCourse().getId(), enrollment.getScore())) {
            throw new IllegalArgumentException("Enrollment does not meet completion criteria");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollment.setProgress(100.0);
        enrollment.setCompletedAt(java.time.LocalDateTime.now());

        Enrollment completedEnrollment = enrollmentRepository.save(enrollment);

        // Generate certificate using functional interface
        String certificateContent = certificateGenerator.generate(
                enrollment.getUser().getFirstName() + " " + enrollment.getUser().getLastName(),
                enrollment.getCourse().getTitle(),
                "CERT-" + System.currentTimeMillis()
        );

        createCertificate(enrollment.getUser(), enrollment.getCourse(), certificateContent);

        log.info("Enrollment completed successfully with certificate generated");
        return ENROLLMENT_TO_DTO.apply(completedEnrollment);
    }

    /**
     * Get course enrollments sorted by score using functional stream
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getCourseEnrollmentsByScore(Long courseId) {
        log.info("Fetching course enrollments sorted by score for course: {}", courseId);

        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        return enrollmentRepository.findEnrollmentsByCourseIdOrderByScore(courseId)
                .stream()
                .map(ENROLLMENT_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get completed enrollments using functional filter
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getCompletedEnrollments() {
        log.info("Fetching completed enrollments");
        return enrollmentRepository.findCompletedEnrollments()
                .stream()
                .filter(IS_COMPLETED)
                .map(ENROLLMENT_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate average score for a course using functional stream
     */
    @Transactional(readOnly = true)
    public double calculateAverageScoreByCourse(Long courseId) {
        log.info("Calculating average score for course: {}", courseId);

        return enrollmentRepository.findEnrollmentsByCourseIdOrderByScore(courseId)
                .stream()
                .mapToDouble(Enrollment::getScore)
                .average()
                .orElse(0.0);
    }

    /**
     * Update enrollment score
     */
    public EnrollmentDTO updateEnrollmentScore(Long enrollmentId, Double score) {
        log.info("Updating enrollment {} score to {}", enrollmentId, score);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setScore(Math.min(Math.max(score, 0.0), 100.0));
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment score updated successfully");
        return ENROLLMENT_TO_DTO.apply(updatedEnrollment);
    }

    /**
     * Count active enrollments for a course
     */
    @Transactional(readOnly = true)
    public long countActiveEnrollmentsByCourse(Long courseId) {
        log.info("Counting active enrollments for course: {}", courseId);
        return enrollmentRepository.countActiveEnrollmentsByCourseId(courseId);
    }

    /**
     * Drop enrollment
     */
    public void dropEnrollment(Long enrollmentId) {
        log.info("Dropping enrollment: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        log.info("Enrollment dropped successfully");
    }

    /**
     * Helper method to create certificate
     */
    private void createCertificate(User user, Course course, String certificateContent) {
        Certificate certificate = Certificate.builder()
                .user(user)
                .courseName(course.getTitle())
                .courseId(course.getId())
                .issueDate(LocalDate.now())
                .build();

        certificateRepository.save(certificate);
    }
}

