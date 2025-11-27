package com.trongtin.application_service.entity;

import com.trongtin.application_service.entity.ApplicationHistory;
import com.trongtin.application_service.entity.ApplicationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "applications")
@Data // Lombok
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    private UUID id;
    private UUID jobId;
    private UUID candidateId;
    private ApplicationStatus status;
    private String coverLetter;
    private UUID cvId;

    // Lịch sử được nhúng (Embedded Document)
    private List<ApplicationHistory> history;

    private Instant createdAt;
    private Instant updatedAt;

    // Constructor/Methods khởi tạo/cập nhật
    // ...
}