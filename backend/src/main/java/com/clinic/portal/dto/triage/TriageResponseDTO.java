package com.clinic.portal.dto.triage;

/**
 * Result of the AI symptom triage: which of the clinic's departments best
 * matches the described symptoms.
 *
 * {@code aiPowered} is false when the recommendation came from the keyword
 * fallback (no ANTHROPIC_API_KEY configured, or the API call failed).
 * {@code recommendedSpecialization} may be null when no department could be
 * determined — the patient should choose manually.
 */
public record TriageResponseDTO(
        String recommendedSpecialization,
        Long specializationId,
        String reasoning,
        String urgency,
        boolean aiPowered
) {}
