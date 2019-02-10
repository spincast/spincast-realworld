package org.spincast.realworld.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.config.SpincastConstants.RequestScopedVariables;
import org.spincast.core.exceptions.CustomStatusCodeException;
import org.spincast.core.exceptions.PublicException;
import org.spincast.core.exchange.DefaultRequestContext;
import org.spincast.core.json.JsonArray;
import org.spincast.core.json.JsonObject;
import org.spincast.core.utils.SpincastStatics;
import org.spincast.realworld.exceptions.ValidationError;
import org.spincast.realworld.exceptions.ValidationErrorsException;
import org.spincast.shaded.org.apache.http.HttpStatus;

/**
 * Formats the errors to return as required by the specs.
 */
public class ErrorController {

    protected final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    /**
     * Server Error
     */
    public void exception(DefaultRequestContext context) {

        Throwable exception = context.variables().get(RequestScopedVariables.EXCEPTION, Throwable.class);

        if (exception instanceof ValidationErrorsException) {
            validationException(context, (ValidationErrorsException)exception);
        } else {
            if (!(exception instanceof PublicException) || ((PublicException)exception).isLogTheException()) {
                this.logger.error("An exception occured :\n" + SpincastStatics.getStackTrace(exception));
            }

            String msg = "An error occured";
            if (exception instanceof PublicException) {
                msg = exception.getMessage();
            }

            int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            if (exception instanceof CustomStatusCodeException) {
                httpStatus = ((CustomStatusCodeException)exception).getStatusCode();
            }

            generalException(context, httpStatus, msg);
        }
    }

    /**
     * Not Found
     */
    public void notFound(DefaultRequestContext context) {
        generalException(context,
                         HttpStatus.SC_NOT_FOUND,
                         "Not found");
    }

    protected void generalException(DefaultRequestContext context,
                                    int httpStatusCode,
                                    String message) {
        JsonObject errorBody = context.json().create();
        errorBody.set("general", message);

        exceptionWithStatusAndMessage(context,
                                      httpStatusCode,
                                      errorBody);
    }

    protected void validationException(DefaultRequestContext context, ValidationErrorsException exception) {
        JsonObject errorBody = context.json().create();

        for (ValidationError validationError : exception.getValidationErrors()) {
            JsonArray allErrors = context.json().createArray();
            for (String error : validationError.getErrors()) {
                allErrors.add(error);
            }
            errorBody.set(validationError.getFieldName(), allErrors);

        }

        exceptionWithStatusAndMessage(context,
                                      HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                      errorBody);
    }

    protected void exceptionWithStatusAndMessage(DefaultRequestContext context,
                                                 int httpStatusCode,
                                                 JsonObject errorBody) {
        JsonObject errorObj = context.json().create();
        errorObj.set("errors", errorBody);
        context.response().setStatusCode(httpStatusCode).sendJson(errorObj);
    }
}
