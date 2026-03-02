package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT l FROM Lesson l WHERE l.course.id = ?1 ORDER BY l.lessonOrder ASC")
    List<Lesson> findLessonsByCourseIdOrderByLessonOrder(Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = ?1 AND l.lessonOrder = ?2")
    java.util.Optional<Lesson> findLessonByCourseIdAndOrder(Long courseId, Integer order);

    long countByCourseId(Long courseId);
}

