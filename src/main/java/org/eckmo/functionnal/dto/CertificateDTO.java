package org.eckmo.functionnal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDTO {
    private Long id;
    private Long userId;
    private String courseName;
    private Long courseId;
    private String issueDate;
    private String certificateCode;
    private String createdAt;
}

