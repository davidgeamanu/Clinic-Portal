package com.clinic.portal.controller.triage;

import com.clinic.portal.dto.triage.TriageRequestDTO;
import com.clinic.portal.dto.triage.TriageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Triage", description = "AI-assisted symptom triage for booking")
public interface TriageController {

    @PostMapping
    @Operation(summary = "Triage symptoms",
            description = "Recommends one of the clinic's departments from a free-text symptom description. Uses the Claude API when configured; falls back to a keyword classifier otherwise. Not medical advice.")
    @ApiResponse(responseCode = "200", description = "Triage recommendation computed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TriageResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    TriageResponseDTO triage(@RequestBody @Valid TriageRequestDTO request);
}
