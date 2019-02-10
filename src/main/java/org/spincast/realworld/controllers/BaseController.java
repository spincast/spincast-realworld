package org.spincast.realworld.controllers;

import org.spincast.core.exchange.DefaultRequestContext;
import org.spincast.realworld.configs.AppConstants;
import org.spincast.realworld.controllers.utils.ToResponseEntityConverter;
import org.spincast.realworld.exceptions.ForbiddenException;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;
import org.spincast.shaded.org.apache.commons.lang3.tuple.Pair;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;

public abstract class BaseController {

    private final EntityFactory entityFactory;
    private final UserService userService;
    private final ToResponseEntityConverter entityConverter;

    @Inject
    public BaseController(EntityFactory entityFactory,
                          UserService userService,
                          ToResponseEntityConverter entityConverter) {
        this.entityFactory = entityFactory;
        this.userService = userService;
        this.entityConverter = entityConverter;
    }

    protected EntityFactory getEntityFactory() {
        return this.entityFactory;
    }

    protected UserService getUserService() {
        return this.userService;
    }

    protected ToResponseEntityConverter getEntityConverter() {
        return this.entityConverter;
    }

    protected DecodedJWT getDecodedJWT(DefaultRequestContext context) {
        DecodedJWT decodedJWT = (DecodedJWT)context.variables().get(AppConstants.CONTEXT_VARIABLES_DECODED_JWT);
        return decodedJWT;
    }

    protected boolean isLoggedIn(DefaultRequestContext context) {
        return getDecodedJWT(context) != null;
    }

    /**
     * Returns the current user.
     *
     * @throws ForbiddenException if the current visitor is
     * not logged in.
     */
    protected User getCurrentUser(DefaultRequestContext context) {
        DecodedJWT decodedJWT = getDecodedJWT(context);
        if (decodedJWT == null) {
            throw new ForbiddenException();
        }

        Claim usernameClaim = decodedJWT.getClaim(AppConstants.JWT_CLAIMS_USERNAME);
        if (usernameClaim.isNull()) {
            throw new ForbiddenException();
        }

        String username = usernameClaim.asString();

        User user = getUserService().getUserByUsername(username);
        if (user == null) {
            throw new ForbiddenException();
        }
        return user;
    }

    protected Pair<Long, Integer> getOffsetlAndLimit(DefaultRequestContext context,
                                                     int defaultLimit,
                                                     int maxLimit) {

        String offsetStr = context.request().getQueryStringParamFirst("offset");
        long offset = 0;
        if (!StringUtils.isBlank(offsetStr)) {
            try {
                long offsetTry = Long.valueOf(offsetStr);
                if (offsetTry < 0) {
                    offsetTry = 0;
                }
                offset = offsetTry;
            } catch (Exception ex) {
                // ok
            }
        }

        String limitStr = context.request().getQueryStringParamFirst("limit");
        int limit = defaultLimit;
        if (!StringUtils.isBlank(limitStr)) {
            try {
                int limitTry = Integer.valueOf(limitStr);
                if (limitTry < 1) {
                    limitTry = defaultLimit;
                } else if (limitTry > maxLimit) {
                    limitTry = maxLimit;
                }
                limit = limitTry;
            } catch (Exception ex) {
                // ok
            }
        }

        return Pair.of(offset, limit);
    }
}
