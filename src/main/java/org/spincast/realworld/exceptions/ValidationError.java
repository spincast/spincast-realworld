package org.spincast.realworld.exceptions;

import java.util.List;

import com.google.common.collect.Lists;

public class ValidationError {

    private final String fieldName;
    private final List<String> errors;

    public ValidationError(String fieldName, String error) {
        this(fieldName, Lists.newArrayList(error));
    }

    public ValidationError(String fieldName, List<String> errors) {
        this.fieldName = fieldName;
        this.errors = errors;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public List<String> getErrors() {
        return this.errors;
    }

}
