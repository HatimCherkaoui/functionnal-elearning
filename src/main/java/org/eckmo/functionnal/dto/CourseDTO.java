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
public class CourseDTO {
    private Long id;

    @NotBlank(message = "Course title is required")
    private String title;

    @NotBlank(message = "Course description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Level is required")
    private String level;

    @NotNull(message = "Price is required")
    private Double price;

    private Integer maxStudents;
    private Double rating;
    private Boolean isPublished;
    private String createdAt;
    private String updatedAt;
}

