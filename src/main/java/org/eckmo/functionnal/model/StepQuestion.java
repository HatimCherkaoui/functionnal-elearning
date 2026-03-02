package org.eckmo.functionnal.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "step_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_step_id", nullable = false)
    private LessonStep lessonStep;

    @Column(nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;

    @Column(nullable = false)
    private String correctAnswer; // JSON or simple value

    @Column(columnDefinition = "TEXT")
    private String options; // JSON array of options

    @Builder.Default
    private Double points = 1.0;

    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        SHORT_ANSWER
    }
}

