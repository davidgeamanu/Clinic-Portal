package com.clinic.portal.service;

import com.clinic.portal.dto.triage.TriageRequestDTO;
import com.clinic.portal.dto.triage.TriageResponseDTO;

public interface TriageService {

    TriageResponseDTO triage(TriageRequestDTO request);
}
