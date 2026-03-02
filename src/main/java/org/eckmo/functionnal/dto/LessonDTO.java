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
public class LessonDTO {
    private Long id;

    @NotBlank(message = "Lesson title is required")
    private String title;

    @NotBlank(message = "Lesson content is required")
    private String content;

    @NotNull(message = "Order is required")
    private Integer lessonOrder;

    private String videoUrl;
    private Long durationMinutes;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String createdAt;
    private String updatedAt;
}

