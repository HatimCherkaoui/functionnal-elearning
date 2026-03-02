package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.StepQuestion;
import org.eckmo.functionnal.model.LessonStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepQuestionRepository extends JpaRepository<StepQuestion, Long> {
    List<StepQuestion> findByLessonStep(LessonStep lessonStep);
}

