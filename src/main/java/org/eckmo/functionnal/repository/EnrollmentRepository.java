package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findAll(Pageable pageable);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = ?1 AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByUserId(Long userId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = ?1 ORDER BY e.score DESC")
    List<Enrollment> findEnrollmentsByCourseIdOrderByScore(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollments();

    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = ?1 AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByCourseId(Long courseId);
}

