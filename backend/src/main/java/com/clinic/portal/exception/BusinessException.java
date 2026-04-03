package com.clinic.portal.exception;

public class BusinessException extends ClinicException {

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}