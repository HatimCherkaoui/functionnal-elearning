package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.CourseDTO;
import org.eckmo.functionnal.functional.CourseFilter;
import org.eckmo.functionnal.model.Course;
import org.eckmo.functionnal.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    /**
     * Get all courses
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        log.info("GET /api/courses - Fetching all courses");
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * Get all published courses
     */
    @GetMapping("/published")
    public ResponseEntity<List<CourseDTO>> getPublishedCourses() {
        log.info("GET /api/courses/published - Fetching published courses");
        return ResponseEntity.ok(courseService.getPublishedCourses());
    }

    /**
     * Get course by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        log.info("GET /api/courses/{} - Fetching course", id);
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * Get courses by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(@PathVariable String category) {
        log.info("GET /api/courses/category/{} - Fetching courses", category);
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
    }

    /**
     * Get courses by level
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<CourseDTO>> getCoursesByLevel(@PathVariable String level) {
        log.info("GET /api/courses/level/{} - Fetching courses", level);
        return ResponseEntity.ok(courseService.getCoursesByLevel(Course.CourseLevel.valueOf(level.toUpperCase())));
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories/list")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("GET /api/courses/categories/list - Fetching categories");
        return ResponseEntity.ok(courseService.getAllCategories());
    }

    /**
     * Get courses sorted by rating
     */
    @GetMapping("/sorted/rating")
    public ResponseEntity<List<CourseDTO>> getCoursesSortedByRating() {
        log.info("GET /api/courses/sorted/rating - Fetching courses sorted by rating");
        return ResponseEntity.ok(courseService.getCoursesSortedByRating());
    }

    /**
     * Get courses within price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<CourseDTO>> getCoursesByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        log.info("GET /api/courses/price-range - Fetching courses between {} and {}", minPrice, maxPrice);
        return ResponseEntity.ok(courseService.getCoursesByPriceRange(minPrice, maxPrice));
    }

    /**
     * Create new course
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        log.info("POST /api/courses - Creating new course");
        CourseDTO created = courseService.createCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update course
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        log.info("PUT /api/courses/{} - Updating course", id);
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    /**
     * Publish course
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<CourseDTO> publishCourse(@PathVariable Long id) {
        log.info("PUT /api/courses/{}/publish - Publishing course", id);
        return ResponseEntity.ok(courseService.publishCourse(id));
    }

    /**
     * Delete course
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("DELETE /api/courses/{} - Deleting course", id);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count course enrollments
     */
    @GetMapping("/{id}/enrollments/count")
    public ResponseEntity<Long> countEnrollments(@PathVariable Long id) {
        log.info("GET /api/courses/{}/enrollments/count - Counting enrollments", id);
        return ResponseEntity.ok(courseService.countCourseEnrollments(id));
    }

    /**
     * Filter courses with functional interface
     */
    @PostMapping("/filter")
    public ResponseEntity<List<CourseDTO>> filterCourses() {
        log.info("POST /api/courses/filter - Filtering courses");

        // Example filter implementation using functional interface
        CourseFilter filter = (title, price, level) -> price <= 100.0 && !level.equals("EXPERT");

        return ResponseEntity.ok(courseService.filterCourses(filter));
    }
}

