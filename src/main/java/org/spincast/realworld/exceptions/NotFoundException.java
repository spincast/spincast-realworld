package org.spincast.realworld.exceptions;

import org.spincast.core.exceptions.PublicExceptionNoLog;
import org.spincast.shaded.org.apache.http.HttpStatus;

public class NotFoundException extends PublicExceptionNoLog {

    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        this("Not Found");
    }

    public NotFoundException(String msg) {
        super(msg, HttpStatus.SC_NOT_FOUND);
    }
}
