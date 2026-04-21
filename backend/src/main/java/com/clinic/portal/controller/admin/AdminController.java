package com.clinic.portal.controller.admin;

import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.specialization.SpecializationRequestDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.exception.ExceptionBody;
import com.clinic.portal.model.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Admin", description = "Administrative operations — user management, doctor creation, specializations")
public interface AdminController {

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<UserResponseDTO> getAllUsers();

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role")
    @ApiResponse(responseCode = "200", description = "Users retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<UserResponseDTO> getUsersByRole(@PathVariable Role role);

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Set user active status", description = "Activate or deactivate a user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    UserResponseDTO setActiveStatus(@PathVariable Long userId, @RequestParam boolean active);

    @PostMapping("/doctors")
    @Operation(summary = "Create doctor account", description = "Create a new doctor user with a profile. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Doctor created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Email or license number already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    DoctorProfileResponseDTO createDoctor(@RequestBody @Valid AdminCreateDoctorDTO dto);

    @PutMapping("/doctors/{profileId}")
    @Operation(summary = "Update doctor profile", description = "Update biography, consultation fee, and specializations for a doctor. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor profile updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Doctor or specialization not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    DoctorProfileResponseDTO updateDoctor(@PathVariable Long profileId, @RequestBody @Valid DoctorProfileUpdateDTO dto);

    @GetMapping("/specializations")
    @Operation(summary = "Get all specializations")
    @ApiResponse(responseCode = "200", description = "Specializations retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SpecializationResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<SpecializationResponseDTO> getAllSpecializations();

    @PostMapping("/specializations")
    @Operation(summary = "Create specialization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Specialization created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpecializationResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Specialization already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    SpecializationResponseDTO createSpecialization(@RequestBody @Valid SpecializationRequestDTO dto);

    @DeleteMapping("/specializations/{id}")
    @Operation(summary = "Delete specialization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Specialization deleted"),
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSpecialization(@PathVariable Long id);
}