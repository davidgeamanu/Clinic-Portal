package com.clinic.portal.controller.consultation;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.DoctorNoteListItemDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.dto.consultation.PatientHistoryItemDTO;
import com.clinic.portal.exception.ExceptionBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Consultation Notes", description = "Managing consultation notes and medical documents")
public interface ConsultationNoteController {

    @PostMapping("/appointment/{appointmentId}")
    @Operation(summary = "Create consultation note", description = "Write a consultation note for a completed appointment. Doctor only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConsultationNoteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Appointment is not completed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "409", description = "Note already exists for this appointment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    ConsultationNoteResponseDTO createNote(@PathVariable Long appointmentId, @RequestBody @Valid ConsultationNoteRequestDTO dto);

    @PutMapping("/appointment/{appointmentId}")
    @Operation(summary = "Update consultation note", description = "Update an existing consultation note. Doctor only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConsultationNoteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Note not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ConsultationNoteResponseDTO updateNote(@PathVariable Long appointmentId, @RequestBody @Valid ConsultationNoteRequestDTO dto);

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get consultation note", description = "Retrieve the consultation note for an appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConsultationNoteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Note not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ConsultationNoteResponseDTO getNoteByAppointment(@PathVariable Long appointmentId);

    @GetMapping("/mine")
    @Operation(summary = "Get my consultation notes", description = "Returns all consultation notes written by the authenticated doctor.")
    @ApiResponse(responseCode = "200", description = "Notes retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DoctorNoteListItemDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<DoctorNoteListItemDTO> getMyNotes();

    @GetMapping("/my-history")
    @Operation(summary = "Get my medical records", description = "Returns all consultation notes for the authenticated patient, across all doctors.")
    @ApiResponse(responseCode = "200", description = "Medical records retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PatientHistoryItemDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<PatientHistoryItemDTO> getMyMedicalRecords();

    @GetMapping("/patient/{patientProfileId}")
    @Operation(summary = "Get patient medical history", description = "Returns all consultation notes for a patient across all doctors. Requires the requesting doctor to have treated this patient.")
    @ApiResponse(responseCode = "200", description = "History retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PatientHistoryItemDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<PatientHistoryItemDTO> getPatientHistory(@PathVariable Long patientProfileId);

    @GetMapping("/{noteId}/documents/{documentId}")
    @Operation(summary = "Download medical document", description = "Download a file attached to a consultation note.")
    ResponseEntity<Resource> downloadDocument(@PathVariable Long noteId, @PathVariable Long documentId);

    @org.springframework.web.bind.annotation.DeleteMapping("/{noteId}/documents/{documentId}")
    @Operation(summary = "Delete medical document", description = "Remove a file from a consultation note. Doctor who owns the note only.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDocument(@PathVariable Long noteId, @PathVariable Long documentId);

    @PostMapping(value = "/{noteId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload medical document", description = "Attach a file to a consultation note. Accepted: PDF, images, Word documents. Doctor only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document uploaded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MedicalDocumentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    MedicalDocumentResponseDTO uploadDocument(@PathVariable Long noteId, @RequestPart("file") MultipartFile file);
}