package com.clinic.portal.exception;

public class UnauthorizedException extends ClinicException {

    public UnauthorizedException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}