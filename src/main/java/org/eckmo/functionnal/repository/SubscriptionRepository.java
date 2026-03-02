package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Subscription;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserAndIsActiveTrue(User user);

    List<Subscription> findByCourseAndIsActiveTrue(Course course);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.course = ?1 AND s.isActive = true")
    long countActiveSubscribersByCourse(Course course);

    @Query("SELECT s FROM Subscription s WHERE s.user = ?1 AND s.course = ?2")
    Optional<Subscription> findByUserAndCourse(User user, Course course);

    @Query("SELECT s.course FROM Subscription s WHERE s.user = ?1 AND s.isActive = true")
    List<Course> findSubscribedCoursesByUser(User user);
}

