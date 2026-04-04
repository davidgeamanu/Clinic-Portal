package com.clinic.portal.repository;

import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    // Retrieve all documents attached to a consultation note
    List<MedicalDocument> findByConsultationNote(ConsultationNote consultationNote);
}
