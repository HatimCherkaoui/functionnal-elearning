package org.eckmo.functionnal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String type; // COURSE_PUBLISHED, ENROLLMENT_UPDATE, INSTRUCTOR_MESSAGE, etc.

    @Column(nullable = false)
    private String relatedEntityType; // COURSE, ENROLLMENT, etc.

    private Long relatedEntityId;

    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        COURSE_PUBLISHED,
        NEW_ENROLLMENT,
        ENROLLMENT_COMPLETED,
        INSTRUCTOR_MESSAGE,
        SYSTEM_NOTIFICATION
    }
}

