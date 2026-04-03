package com.clinic.portal.exception;

public class DuplicateDataException extends ClinicException {

    public DuplicateDataException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}