package com.clinic.portal.controller.triage;

import com.clinic.portal.dto.triage.TriageRequestDTO;
import com.clinic.portal.dto.triage.TriageResponseDTO;
import com.clinic.portal.service.TriageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/triage")
@RequiredArgsConstructor
public class TriageControllerImpl implements TriageController {

    private final TriageService triageService;

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public TriageResponseDTO triage(TriageRequestDTO request) {
        log.info("[TRIAGE] Triage requested ({} chars)", request.symptoms().length());
        return triageService.triage(request);
    }
}
