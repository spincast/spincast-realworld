package org.spincast.realworld.exceptions;

import org.spincast.core.exceptions.PublicExceptionNoLog;
import org.spincast.shaded.org.apache.http.HttpStatus;

public class UnauthorizeException extends PublicExceptionNoLog {

    private static final long serialVersionUID = 1L;

    public UnauthorizeException() {
        super("You need to be logged in to view this page.", HttpStatus.SC_UNAUTHORIZED);
    }
}
