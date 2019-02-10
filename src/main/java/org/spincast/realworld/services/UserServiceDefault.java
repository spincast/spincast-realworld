package org.spincast.realworld.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.spincast.core.exceptions.PublicExceptionNoLog;
import org.spincast.core.json.JsonManager;
import org.spincast.core.utils.SpincastStatics;
import org.spincast.plugins.crypto.SpincastCryptoUtils;
import org.spincast.realworld.configs.AppConfig;
import org.spincast.realworld.configs.AppConstants;
import org.spincast.realworld.exceptions.NotFoundException;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.models.users.UserValidator;
import org.spincast.realworld.repositories.UserRepository;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;
import org.spincast.shaded.org.apache.http.HttpStatus;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.inject.Inject;

public class UserServiceDefault implements UserService {

    private final UserRepository userRepository;
    private final JsonManager jsonManager;
    private final UserValidator userValidator;
    private final AppConfig appConfig;
    private final SpincastCryptoUtils spincastCryptoUtils;
    private final EntityFactory entityFactory;
    private JWTVerifier jwtVerifier;
    private Algorithm jwtAlgorithm;

    @Inject
    public UserServiceDefault(UserRepository userRepository,
                              JsonManager jsonManager,
                              UserValidator userValidator,
                              AppConfig appConfig,
                              SpincastCryptoUtils spincastCryptoUtils,
                              EntityFactory entityFactory) {
        this.userRepository = userRepository;
        this.jsonManager = jsonManager;
        this.userValidator = userValidator;
        this.appConfig = appConfig;
        this.spincastCryptoUtils = spincastCryptoUtils;
        this.entityFactory = entityFactory;
    }

    protected UserRepository getUserRepository() {
        return this.userRepository;
    }

    protected JsonManager getJsonManager() {
        return this.jsonManager;
    }

    protected UserValidator getUserValidator() {
        return this.userValidator;
    }

    protected AppConfig getAppConfig() {
        return this.appConfig;
    }

    protected SpincastCryptoUtils getSpincastCryptoUtils() {
        return this.spincastCryptoUtils;
    }

    protected EntityFactory getEntityFactory() {
        return this.entityFactory;
    }

    @Override
    public User saveUser(User user, String rawPassword) {
        getUserValidator().validateUser(user, rawPassword, null, true);
        return getUserRepository().saveUser(user);
    }

    @Override
    public String hashPassword(String rawPassword, String salt) {
        if (rawPassword == null) {
            return null;
        }
        return getSpincastCryptoUtils().hashSecure(rawPassword, salt);
    }

    @Override
    public String createPasswordSalt() {
        return getSpincastCryptoUtils().generateNewHashSecureSalt();
    }

    @Override
    public String createJwtToken(User user) {
        try {
            String token = JWT.create()
                              .withClaim(AppConstants.JWT_CLAIMS_USERNAME, user.getUsername())
                              .withClaim(PublicClaims.EXPIRES_AT,
                                         Date.from(Instant.now().plus(getAppConfig().getAuthJwtTtlMinutes(),
                                                                      ChronoUnit.MINUTES)))
                              .sign(getJwtAlgorithm());
            return token;
        } catch (Exception ex) {
            throw SpincastStatics.runtimize(ex);
        }
    }

    protected Algorithm getJwtAlgorithm() {
        if (this.jwtAlgorithm == null) {
            this.jwtAlgorithm = Algorithm.HMAC256(getAppConfig().getAuthJwtSecret());
        }
        return this.jwtAlgorithm;
    }

    protected JWTVerifier getJwtVerifier() {
        if (this.jwtVerifier == null) {
            this.jwtVerifier = JWT.require(getJwtAlgorithm())
                                  .acceptExpiresAt(0)
                                  .build();
        }
        return this.jwtVerifier;
    }

    @Override
    public DecodedJWT decodeJwt(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        try {
            DecodedJWT jwt = getJwtVerifier().verify(token);
            return jwt;
        } catch (JWTVerificationException ex) {
            return null;
        } catch (Exception ex) {
            throw SpincastStatics.runtimize(ex);
        }
    }

    @Override
    public boolean isJwtValid(String token) {
        DecodedJWT decodedJwt = decodeJwt(token);
        return decodedJwt != null;
    }

    @Override
    public User getUserByEmail(String email) {
        return getUserRepository().getUserByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        return getUserRepository().getUserByUsername(username);
    }

    public User getUserById(long userId) {
        return getUserRepository().getUserById(userId);
    }

    @Override
    public User getUser(String email, String rawPassword) {
        User candidate = getUserByEmail(email);

        // Validate password
        if (candidate == null ||
            !hashPassword(rawPassword, candidate.getPasswordSalt()).equals(candidate.getHashedPassword())) {
            return null;
        }
        return candidate;
    }

    @Override
    public void updateUser(User currentUser, User newUserInfo, String rawPassword) {
        getUserValidator().validateUser(newUserInfo, rawPassword, currentUser, false);
        getUserRepository().updateUser(newUserInfo);
    }

    @Override
    public Profile getProfileById(long userId, User currentUser) {
        return getProfile(userId, null, currentUser);
    }

    @Override
    public Profile getProfileByUsername(String username, User currentUser) {
        return getProfile(null, username, currentUser);
    }

    public Profile getProfile(Long userId, String username, User currentUser) {

        User user = userId != null ? getUserById(userId) : getUserByUsername(username);
        if (user == null) {
            return null;
        }

        boolean following = false;
        if (currentUser != null) {
            following = userId != null ? isFollowingById(currentUser.getId(), userId)
                                       : isFollowingByUsername(currentUser.getId(), username);
        }

        Profile profile = getEntityFactory().createProfile(user, following);
        return profile;
    }

    @Override
    public boolean isFollowingById(long sourceUserId, long targetUserId) {
        return getUserRepository().isFollowingById(sourceUserId, targetUserId);
    }

    @Override
    public boolean isFollowingByUsername(long sourceUserId, String username) {
        return getUserRepository().isFollowingByUsername(sourceUserId, username);
    }

    @Override
    public void follow(User sourceUser, String targetUsername) {

        User targetUser = getUserByUsername(targetUsername);
        if (targetUser == null) {
            throw new NotFoundException("User to follow not found");
        }

        if (sourceUser.getId() == targetUser.getId()) {
            throw new PublicExceptionNoLog("You can't follow yourself", HttpStatus.SC_BAD_REQUEST);
        }

        getUserRepository().follow(sourceUser.getId(), targetUser.getId());
    }

    @Override
    public void unfollow(User sourceUser, String targetUsername) {

        User targetUser = getUserByUsername(targetUsername);
        if (targetUser == null) {
            return;
        }

        getUserRepository().unfollow(sourceUser.getId(), targetUser.getId());
    }

    @Override
    public boolean isEmailTaken(String email) {
        return getUserRepository().isEmailTaken(email);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return getUserRepository().isUsernameTaken(username);
    }
}
