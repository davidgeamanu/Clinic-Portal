package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Metadata for a file attached to a ConsultationNote.
 *
 * We store metadata here, not the file bytes.
 * The actual file lives in object storage (local filesystem in dev, S3/MinIO in prod).
 * storagePath holds the key/path needed to retrieve it from that storage.
 *
 * Storing blobs in the DB is an anti-pattern:
 *   - it bloats the DB, slows backups, and cannot be streamed efficiently.
 *   - object storage is designed for this; the DB is not.
 */
@Entity
@Table(name = "medical_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_note_id", nullable = false)
    private ConsultationNote consultationNote;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String storagePath;

    @Column(nullable = false)
    private Long fileSizeBytes;
}
