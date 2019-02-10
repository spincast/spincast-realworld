package org.spincast.realworld.exceptions;

import org.spincast.core.exceptions.PublicExceptionNoLog;
import org.spincast.shaded.org.apache.http.HttpStatus;

public class BadRequestException extends PublicExceptionNoLog {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message, HttpStatus.SC_BAD_REQUEST);
    }
}
