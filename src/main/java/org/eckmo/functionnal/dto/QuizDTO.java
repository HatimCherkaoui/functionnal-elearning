package org.eckmo.functionnal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {
    private Long id;

    @NotBlank(message = "Quiz title is required")
    private String title;

    @NotBlank(message = "Quiz description is required")
    private String description;

    @NotNull(message = "Passing score is required")
    private Double passingScore;

    private Integer attemptLimit;

    @NotNull(message = "Lesson ID is required")
    private Long lessonId;

    private String createdAt;
    private String updatedAt;
}

