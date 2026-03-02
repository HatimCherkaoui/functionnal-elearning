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
public class AnswerDTO {
    private Long id;

    @NotBlank(message = "Answer text is required")
    private String answerText;

    private Boolean isCorrect;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private String createdAt;
    private String updatedAt;
}

