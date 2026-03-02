package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findAll(Pageable pageable);

    Page<Course> findByIsPublished(Boolean isPublished, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isPublished = true ORDER BY c.rating DESC")
    List<Course> findAllPublishedCoursesSortedByRating();

    @Query("SELECT c FROM Course c WHERE c.category = ?1 AND c.isPublished = true")
    List<Course> findCoursesByCategory(String category);

    @Query("SELECT c FROM Course c WHERE c.level = ?1")
    List<Course> findCoursesByLevel(Course.CourseLevel level);

    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.isPublished = true")
    List<String> findAllDistinctCategories();

    @Query("SELECT COUNT(e) FROM Course c JOIN c.enrollments e WHERE c.id = ?1")
    long countEnrollmentsByCourseId(Long courseId);

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.category) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Course> searchCourses(String q, Pageable pageable);
}

