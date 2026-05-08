package com.clinic.portal.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminAnalyticsDTO(
        BigDecimal totalRevenue,
        long totalPatients,
        List<MonthlyRevenueDTO> monthlyRevenue,
        List<MonthlyPatientCountDTO> patientTrend
) {}
