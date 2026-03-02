package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateCode(String certificateCode);

    @Query("SELECT c FROM Certificate c WHERE c.user.id = ?1 ORDER BY c.issueDate DESC")
    List<Certificate> findCertificatesByUserId(Long userId);

    @Query("SELECT c FROM Certificate c WHERE c.courseId = ?1")
    List<Certificate> findCertificatesByCourseId(Long courseId);

    long countByUserId(Long userId);
}

