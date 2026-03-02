package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.quiz.id = ?1")
    List<Question> findQuestionsByQuizId(Long quizId);

    long countByQuizId(Long quizId);
}

