package com.clinic.portal.controller.doctor;

import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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