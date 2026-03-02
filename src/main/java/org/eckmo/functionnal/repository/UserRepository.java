package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(User.UserRole role, Pageable pageable);

    Page<User> findByApprovalStatus(User.ApprovalStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'INSTRUCTOR'")
    List<User> findAllInstructors();

    @Query("SELECT u FROM User u WHERE u.role = 'INSTRUCTOR'")
    Page<User> findAllInstructors(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.approvalStatus = 'PENDING' AND u.role = 'INSTRUCTOR'")
    List<User> findPendingInstructors();

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(String q, Pageable pageable);

    long countByRole(User.UserRole role);

    long countByApprovalStatus(User.ApprovalStatus status);
}

