package com.clinic.portal.dto.admin;

import java.math.BigDecimal;

public record DailyStatsDTO(
        String day,
        int patients,
        int appointments,
        BigDecimal revenue
) {}
