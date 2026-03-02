package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Notification;
import org.eckmo.functionnal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = ?1 AND n.isRead = false")
    long countUnreadByUser(User user);

    @Query("SELECT n FROM Notification n WHERE n.user = ?1 ORDER BY n.createdAt DESC LIMIT 50")
    List<Notification> findRecentByUser(User user);
}

