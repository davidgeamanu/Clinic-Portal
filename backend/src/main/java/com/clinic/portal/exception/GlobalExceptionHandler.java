package com.clinic.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleValidationErrors(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                        (existing, ignored) -> existing
                ));

        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(ExceptionCode.VALIDATION_ERROR.getCode())
                .message(ExceptionCode.VALIDATION_ERROR.getMessage())
                .details(errors)
                .build();
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionBody handleDataNotFound(DataNotFoundException exception) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(exception.getCode())
                .message(exception.getMessage())
                .details(Collections.emptyMap())
                .build();
    }

    @ExceptionHandler(DuplicateDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionBody handleDuplicateData(DuplicateDataException exception) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(exception.getCode())
                .message(exception.getMessage())
                .details(Collections.emptyMap())
                .build();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleBusinessException(BusinessException exception) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(exception.getCode())
                .message(exception.getMessage())
                .details(Collections.emptyMap())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionBody handleAccessDenied(AccessDeniedException ignored) {
        return ExceptionBody.of(ExceptionCode.ACCESS_DENIED);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionBody handleGlobalException(Exception ignored) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .code(ExceptionCode.SERVER_ERROR.getCode())
                .message(ExceptionCode.SERVER_ERROR.getMessage())
                .details(Collections.emptyMap())
                .build();
    }
}