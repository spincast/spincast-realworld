package org.spincast.realworld.filters;

import org.spincast.core.exchange.DefaultRequestContext;
import org.spincast.realworld.configs.AppConstants;
import org.spincast.realworld.exceptions.ForbiddenException;
import org.spincast.realworld.exceptions.UnauthorizeException;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;

/**
 * JWT validation filter.
 */
public class AuthFilter {

    private final UserService userService;

    @Inject
    public AuthFilter(UserService userService) {
        this.userService = userService;
    }

    protected UserService getUserService() {
        return this.userService;
    }

    /**
     * If present, saves the JWT as a request variable.
     */
    public void saveJwt(DefaultRequestContext context) {
        DecodedJWT decodedJwt = null;
        String jwt = extractJwt(context);
        if (!StringUtils.isBlank(jwt)) {
            decodedJwt = getUserService().decodeJwt(jwt);
        }
        if (decodedJwt != null) {
            context.variables().set(AppConstants.CONTEXT_VARIABLES_DECODED_JWT, decodedJwt);
        }
    }

    public void validateJwt(DefaultRequestContext context) {
        String jwt = extractJwt(context);
        if (StringUtils.isBlank(jwt)) {
            throw new UnauthorizeException();
        }
        DecodedJWT decodedJwt = (DecodedJWT)context.variables().get(AppConstants.CONTEXT_VARIABLES_DECODED_JWT);
        if (decodedJwt == null) {
            throw new ForbiddenException();
        }
    }

    protected String extractJwt(DefaultRequestContext context) {
        String authorizationHeader = context.request().getHeaderFirst("Authorization");
        if (StringUtils.isBlank(authorizationHeader)) {
            return null;
        }
        String jwt = null;
        if (authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring("Bearer ".length());
        } else if (authorizationHeader.startsWith("Token ")) {
            jwt = authorizationHeader.substring("Token ".length());
        }
        return jwt;
    }
}
