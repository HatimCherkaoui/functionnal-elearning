package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.LessonStep;
import org.eckmo.functionnal.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonStepRepository extends JpaRepository<LessonStep, Long> {
    List<LessonStep> findByLessonOrderByStepOrder(Lesson lesson);
}

