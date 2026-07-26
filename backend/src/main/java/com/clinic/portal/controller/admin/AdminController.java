package com.clinic.portal.controller.admin;

import com.clinic.portal.dto.admin.AdminAnalyticsDTO;
import com.clinic.portal.dto.admin.AdminDashboardDTO;
import com.clinic.portal.dto.admin.AdminDepartmentDTO;
import com.clinic.portal.dto.admin.AdminPatientDTO;
import com.clinic.portal.dto.admin.DoctorRoomAssignmentDTO;
import com.clinic.portal.dto.admin.RoomResponseDTO;
import com.clinic.portal.dto.admin.RoomUpdateDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.exception.ExceptionBody;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Admin", description = "Administrative operations")
public interface AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AdminDashboardDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    AdminDashboardDTO getAdminDashboard();

    @GetMapping("/analytics")
    @Operation(summary = "Get analytics data")
    @ApiResponse(responseCode = "200", description = "Analytics data retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AdminAnalyticsDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    AdminAnalyticsDTO getAnalytics();

    @GetMapping("/departments")
    @Operation(summary = "Get all departments with stats")
    @ApiResponse(responseCode = "200", description = "Departments retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AdminDepartmentDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<AdminDepartmentDTO> getDepartments();

    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms")
    @ApiResponse(responseCode = "200", description = "Rooms retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RoomResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<RoomResponseDTO> getRooms();

    @PatchMapping("/rooms/{roomId}")
    @Operation(summary = "Update room assignment, type, or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoomResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    RoomResponseDTO updateRoom(@PathVariable Long roomId, @RequestBody @Valid RoomUpdateDTO dto);

    @GetMapping("/patients")
    @Operation(summary = "Get patients (paginated)",
            description = "Returns one page of patients with profile data. Filtering is applied across the whole "
                    + "table, not just the returned page. Query params: search (name or email), active, page, size, sort.")
    @ApiResponse(responseCode = "200", description = "Patients page retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AdminPatientDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    PagedModel<AdminPatientDTO> getAdminPatients(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/patients/{patientProfileId}/appointments")
    @Operation(summary = "Get appointments for a specific patient")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    List<AppointmentResponseDTO> getPatientAppointments(@PathVariable Long patientProfileId);

    @GetMapping("/doctors/{doctorProfileId}/appointments")
    @Operation(summary = "Get appointments for a specific doctor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    List<AppointmentResponseDTO> getDoctorAppointments(@PathVariable Long doctorProfileId);

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
    @Operation(summary = "Set user active status")
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

    @PatchMapping("/doctors/{doctorProfileId}/room")
    @Operation(summary = "Assign or unassign a consult room for a doctor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room assignment updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DoctorProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Room is not a consult type or already assigned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Doctor or room not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    DoctorProfileResponseDTO assignDoctorRoom(@PathVariable Long doctorProfileId, @RequestBody DoctorRoomAssignmentDTO dto);

    @PostMapping("/doctors")
    @Operation(summary = "Create doctor account")
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

    @GetMapping("/specializations")
    @Operation(summary = "Get all specializations")
    @ApiResponse(responseCode = "200", description = "Specializations retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SpecializationResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<SpecializationResponseDTO> getAllSpecializations();

    @GetMapping("/appointments")
    @Operation(summary = "Get appointments (paginated)",
            description = "Returns one page of appointments. Filtering is applied across the whole table, not just "
                    + "the returned page. Query params: search (patient or doctor name), status, mode, page, size, "
                    + "sort (default: scheduledAt,desc).")
    @ApiResponse(responseCode = "200", description = "Appointments page retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppointmentResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    PagedModel<AppointmentResponseDTO> getAllAppointments(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) AppointmentMode mode,
            @ParameterObject @PageableDefault(size = 20, sort = "scheduledAt", direction = Sort.Direction.DESC) Pageable pageable);

    @PatchMapping("/appointments/{appointmentId}/cancel")
    @Operation(summary = "Cancel appointment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment cancelled",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Appointment already completed or cancelled",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    AppointmentResponseDTO cancelAppointment(@PathVariable Long appointmentId);
}