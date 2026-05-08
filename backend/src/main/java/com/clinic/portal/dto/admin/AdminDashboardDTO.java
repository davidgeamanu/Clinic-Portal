package com.clinic.portal.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardDTO(
        long totalPatients,
        long activeDoctors,
        long todaysAppointments,
        BigDecimal monthlyRevenue,
        List<DailyStatsDTO> weeklyStats,
        List<DepartmentLoadDTO> departmentLoad
) {}
