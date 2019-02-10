package org.spincast.realworld.exceptions;

import java.util.List;

import com.google.common.collect.Lists;

public class ValidationErrorsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<ValidationError> validationErrors;

    public ValidationErrorsException(ValidationError validationErrors) {
        this(Lists.newArrayList(validationErrors));
    }

    public ValidationErrorsException(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return this.validationErrors;
    }
}
