package org.eckmo.functionnal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lesson_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"lesson", "questions"})
@ToString(exclude = {"lesson", "questions"})
public class LessonStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Integer stepOrder;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String htmlContent; // Rich text editor output

    @Column(columnDefinition = "TEXT")
    private String codeSnippet;

    private String programmingLanguage;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrls; // JSON array of image URLs

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StepType stepType = StepType.CONTENT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "lessonStep", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StepQuestion> questions = new HashSet<>();

    public enum StepType {
        CONTENT,      // Rich text with images
        CODE,         // Code snippet
        QUESTION,     // Multiple choice or true/false
        VIDEO,        // Video content
        EXERCISE      // Hands-on exercise
    }
}
