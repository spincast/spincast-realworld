package org.spincast.realworld.exceptions;

import org.spincast.core.exceptions.PublicExceptionNoLog;
import org.spincast.shaded.org.apache.http.HttpStatus;

public class ForbiddenException extends PublicExceptionNoLog {

    private static final long serialVersionUID = 1L;

    public ForbiddenException() {
        this("You do not have sufficient rights to view this page.");
    }

    public ForbiddenException(String msg) {
        super(msg, HttpStatus.SC_FORBIDDEN);
    }
}
