package com.clinic.portal.exception;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

@Builder
public record ExceptionBody(
        ZonedDateTime timestamp,
        String code,
        String message,
        Map<String, String> details
) {

    public static ExceptionBody of(ExceptionCode exceptionCode) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(exceptionCode.getCode())
                .message(exceptionCode.getMessage())
                .details(Collections.emptyMap())
                .build();
    }
}
