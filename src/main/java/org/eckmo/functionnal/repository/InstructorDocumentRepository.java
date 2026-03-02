package org.eckmo.functionnal.repository;

import org.eckmo.functionnal.model.InstructorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorDocumentRepository extends JpaRepository<InstructorDocument, Long> {
    List<InstructorDocument> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}

