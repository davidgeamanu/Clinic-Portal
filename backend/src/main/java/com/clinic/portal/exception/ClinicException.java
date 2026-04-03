package com.clinic.portal.exception;

public abstract class ClinicException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    protected ClinicException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public String getCode() {
        return exceptionCode.getCode();
    }
}