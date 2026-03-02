package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query("SELECT a FROM Answer a WHERE a.question.id = ?1")
    List<Answer> findAnswersByQuestionId(Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.question.id = ?1 AND a.isCorrect = true")
    List<Answer> findCorrectAnswersByQuestionId(Long questionId);

    long countByQuestionId(Long questionId);
}

