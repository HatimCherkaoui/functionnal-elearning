package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q FROM Quiz q WHERE q.lesson.id = ?1")
    List<Quiz> findQuizzesByLessonId(Long lessonId);

    @Query("SELECT q FROM Quiz q WHERE q.lesson.course.id = ?1")
    List<Quiz> findQuizzesByCourseId(Long courseId);

    long countByLessonId(Long lessonId);
}

