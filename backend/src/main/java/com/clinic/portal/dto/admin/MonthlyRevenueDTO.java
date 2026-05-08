package com.clinic.portal.dto.admin;

import java.math.BigDecimal;

public record MonthlyRevenueDTO(
        String month,
        BigDecimal revenue
) {}
