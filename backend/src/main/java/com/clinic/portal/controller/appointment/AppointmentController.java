package com.clinic.portal.controller.appointment;

import com.clinic.portal.dto.appointment.AppointmentRatingRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.exception.ExceptionBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Appointments", description = "Booking and managing appointments")
public interface AppointmentController {

    @PostMapping
    @Operation(summary = "Book appointment", description = "Book a new appointment with a doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Appointment booked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Time slot already taken",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    AppointmentResponseDTO book(@RequestBody @Valid AppointmentRequestDTO dto);

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    AppointmentResponseDTO getById(@PathVariable Long appointmentId);

    @GetMapping("/my/patient")
    @Operation(summary = "Get my appointments (patient)", description = "Retrieve all appointments for the currently authenticated patient.")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppointmentResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<AppointmentResponseDTO> getMyPatientAppointments();

    @GetMapping("/my/doctor")
    @Operation(summary = "Get my appointments (doctor)", description = "Retrieve all appointments for the currently authenticated doctor.")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppointmentResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<AppointmentResponseDTO> getMyDoctorAppointments();

    @PatchMapping("/{appointmentId}/status")
    @Operation(summary = "Update appointment status", description = "Transition an appointment to a new status. Only valid transitions are allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Not a participant in this appointment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    AppointmentResponseDTO updateStatus(@PathVariable Long appointmentId, @RequestBody @Valid AppointmentStatusUpdateDTO dto);

    @PostMapping("/{appointmentId}/rate")
    @Operation(summary = "Rate a completed appointment", description = "Submit a 1–5 star rating and an optional review for a completed appointment. Each appointment can be rated only once.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating submitted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Appointment not completed or already rated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Not the patient on this appointment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    AppointmentResponseDTO rate(@PathVariable Long appointmentId, @RequestBody @Valid AppointmentRatingRequestDTO dto);
}