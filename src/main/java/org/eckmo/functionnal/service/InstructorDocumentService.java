package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.model.InstructorDocument;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.InstructorDocumentRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstructorDocumentService {

    private final InstructorDocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/instructor-docs}")
    private String uploadDir;

    public void saveDocuments(Long userId, List<MultipartFile> files) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            InstructorDocument doc = InstructorDocument.builder()
                    .user(user)
                    .fileName(storedName)
                    .originalName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .build();
            documentRepository.save(doc);
        }
    }

    @Transactional(readOnly = true)
    public List<InstructorDocument> getDocumentsByUser(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public InstructorDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    public String getUploadDir() {
        return uploadDir;
    }
}

