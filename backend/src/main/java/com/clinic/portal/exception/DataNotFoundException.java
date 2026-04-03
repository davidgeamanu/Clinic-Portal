package com.clinic.portal.exception;

public class DataNotFoundException extends ClinicException {

    public DataNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}