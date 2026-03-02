package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.CourseDTO;
import org.eckmo.functionnal.exception.ResourceNotFoundException;
import org.eckmo.functionnal.functional.CourseFilter;
import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CourseService {

    private final CourseRepository courseRepository;
    private final KafkaProducerService kafkaProducerService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert Course entity to CourseDTO using functional mapping
     */
    private final Function<Course, CourseDTO> COURSE_TO_DTO = course -> CourseDTO.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .category(course.getCategory())
            .level(course.getLevel().name())
            .price(course.getPrice())
            .maxStudents(course.getMaxStudents())
            .rating(course.getRating())
            .isPublished(course.getIsPublished())
            .createdAt(course.getCreatedAt().format(DATE_FORMATTER))
            .updatedAt(course.getUpdatedAt().format(DATE_FORMATTER))
            .build();

    /**
     * Predicate for filtering published courses
     */
    private final Predicate<Course> IS_PUBLISHED = Course::getIsPublished;

    /**
     * Predicate for filtering by category
     */
    private final Function<String, Predicate<Course>> BY_CATEGORY = category ->
            course -> category.equalsIgnoreCase(course.getCategory());

    /**
     * Get all courses as DTOs
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll()
                .stream()
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllCoursesPaged(int page, int size, String sortBy, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Course> courses = (search != null && !search.isBlank())
                ? courseRepository.searchCourses(search, pageable)
                : courseRepository.findAll(pageable);
        return courses.map(COURSE_TO_DTO);
    }

    /**
     * Get all published courses sorted by rating using functional stream
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getPublishedCourses() {
        log.info("Fetching all published courses");
        return courseRepository.findAllPublishedCoursesSortedByRating()
                .stream()
                .filter(IS_PUBLISHED)
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by category using functional filtering
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByCategory(String category) {
        log.info("Fetching courses by category: {}", category);
        return courseRepository.findCoursesByCategory(category)
                .stream()
                .filter(BY_CATEGORY.apply(category))
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by level
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByLevel(Course.CourseLevel level) {
        log.info("Fetching courses by level: {}", level);
        return courseRepository.findCoursesByLevel(level)
                .stream()
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct categories
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.info("Fetching all course categories");
        return courseRepository.findAllDistinctCategories()
                .stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        log.info("Fetching course with id: {}", id);
        return courseRepository.findById(id)
                .map(COURSE_TO_DTO)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Create new course
     */
    public CourseDTO createCourse(CourseDTO courseDTO) {
        log.info("Creating new course: {}", courseDTO.getTitle());

        Course course = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .category(courseDTO.getCategory())
                .level(Course.CourseLevel.valueOf(courseDTO.getLevel()))
                .price(courseDTO.getPrice())
                .maxStudents(Optional.ofNullable(courseDTO.getMaxStudents()).orElse(100))
                .rating(0.0)
                .isPublished(false)
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with id: {}", savedCourse.getId());
        return COURSE_TO_DTO.apply(savedCourse);
    }

    /**
     * Update course with functional null-safe operations
     */
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        log.info("Updating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        Optional.ofNullable(courseDTO.getTitle()).ifPresent(course::setTitle);
        Optional.ofNullable(courseDTO.getDescription()).ifPresent(course::setDescription);
        Optional.ofNullable(courseDTO.getCategory()).ifPresent(course::setCategory);
        Optional.ofNullable(courseDTO.getLevel())
                .ifPresent(level -> course.setLevel(Course.CourseLevel.valueOf(level)));
        Optional.ofNullable(courseDTO.getPrice()).ifPresent(course::setPrice);
        Optional.ofNullable(courseDTO.getMaxStudents()).ifPresent(course::setMaxStudents);
        Optional.ofNullable(courseDTO.getIsPublished()).ifPresent(course::setIsPublished);

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully");
        return COURSE_TO_DTO.apply(updatedCourse);
    }

    /**
     * Publish course
     */
    public CourseDTO publishCourse(Long id) {
        log.info("Publishing course with id: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setIsPublished(true);
        Course updatedCourse = courseRepository.save(course);
        CourseDTO courseDTO = COURSE_TO_DTO.apply(updatedCourse);

        // Publish to Kafka
        kafkaProducerService.publishCourse(courseDTO);

        log.info("Course published successfully");
        return courseDTO;
    }

    /**
     * Delete course
     */
    public void deleteCourse(Long id) {
        log.info("Deleting course with id: {}", id);
        courseRepository.findById(id)
                .ifPresentOrElse(
                        courseRepository::delete,
                        () -> {
                            throw new ResourceNotFoundException("Course not found with id: " + id);
                        }
                );
        log.info("Course deleted successfully");
    }

    /**
     * Filter courses using custom functional interface
     */
    public List<CourseDTO> filterCourses(CourseFilter filter) {
        log.info("Filtering courses with custom filter");
        return courseRepository.findAll()
                .stream()
                .filter(course -> filter.test(course.getTitle(), course.getPrice(), course.getLevel().name()))
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses within price range using functional stream
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByPriceRange(Double minPrice, Double maxPrice) {
        log.info("Fetching courses between {} and {}", minPrice, maxPrice);
        return courseRepository.findAll()
                .stream()
                .filter(course -> course.getPrice() >= minPrice && course.getPrice() <= maxPrice)
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }

    /**
     * Count enrollments for a course
     */
    @Transactional(readOnly = true)
    public long countCourseEnrollments(Long courseId) {
        log.info("Counting enrollments for course: {}", courseId);
        return courseRepository.countEnrollmentsByCourseId(courseId);
    }

    /**
     * Get courses sorted by a custom comparator using functional stream
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesSortedByRating() {
        log.info("Fetching courses sorted by rating");
        return courseRepository.findAll()
                .stream()
                .sorted((c1, c2) -> Double.compare(c2.getRating(), c1.getRating()))
                .map(COURSE_TO_DTO)
                .collect(Collectors.toList());
    }
}
