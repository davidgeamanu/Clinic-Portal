package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Metadata for a file attached to a ConsultationNote.
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
