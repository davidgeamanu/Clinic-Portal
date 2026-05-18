package com.clinic.portal.controller.doctor;

import com.clinic.portal.dto.doctor.BookedSlotDTO;
import com.clinic.portal.dto.doctor.DoctorPatientListItemDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.doctor.PatientSummaryDTO;
import com.clinic.portal.dto.doctor.RecentPatientDTO;
import com.clinic.portal.exception.ExceptionBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Doctor", description = "Doctor profile management and discovery")
public interface DoctorController {

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieve all doctor profiles.")
    @ApiResponse(responseCode = "200", description = "Doctors retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DoctorProfileResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<DoctorProfileResponseDTO> getAllDoctors();

    @GetMapping("/{profileId}/booked-slots")
    @Operation(summary = "Get doctor's booked slots", description = "Returns non-cancelled appointments for a given doctor on a given date — used by patients during booking to show busy time slots.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booked slots retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BookedSlotDTO.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    List<BookedSlotDTO> getBookedSlots(
            @PathVariable Long profileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @GetMapping("/{profileId}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve a doctor's profile by their profile ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    DoctorProfileResponseDTO getDoctorById(@PathVariable Long profileId);

    @GetMapping("/specialization/{specializationId}")
    @Operation(summary = "Get doctors by specialization", description = "Retrieve all doctors with a given specialization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctors retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    List<DoctorProfileResponseDTO> getDoctorsBySpecialization(@PathVariable Long specializationId);

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Retrieve the profile of the currently authenticated doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    DoctorProfileResponseDTO getMyProfile();

    @GetMapping("/me/recent-patients")
    @Operation(summary = "Get my recent patients", description = "Returns the last 3 distinct patients seen by the authenticated doctor, with their latest consultation note.")
    @ApiResponse(responseCode = "200", description = "Recent patients retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RecentPatientDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<RecentPatientDTO> getMyRecentPatients();

    @GetMapping("/me/patients")
    @Operation(summary = "Get my patients", description = "Returns all distinct patients this doctor has had a non-cancelled appointment with.")
    @ApiResponse(responseCode = "200", description = "Patient list retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DoctorPatientListItemDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<DoctorPatientListItemDTO> getMyPatients();

    @GetMapping("/me/patients/{patientProfileId}")
    @Operation(summary = "Get patient summary", description = "Returns profile details for a patient who has had an appointment with the authenticated doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient summary retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PatientSummaryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found or no shared appointment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PatientSummaryDTO getPatientSummary(@PathVariable Long patientProfileId);

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update the profile of the currently authenticated doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    DoctorProfileResponseDTO updateMyProfile(@RequestBody @Valid DoctorProfileUpdateDTO dto);
}