package org.spincast.realworld.controllers;

import org.spincast.core.exchange.DefaultRequestContext;
import org.spincast.core.json.JsonObject;
import org.spincast.realworld.controllers.utils.ToResponseEntityConverter;
import org.spincast.realworld.exceptions.BadRequestException;
import org.spincast.realworld.exceptions.ForbiddenException;
import org.spincast.realworld.exceptions.NotFoundException;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class UserController extends BaseController {

    @Inject
    public UserController(EntityFactory entityFactory,
                          UserService userService,
                          ToResponseEntityConverter entityConverter) {
        super(entityFactory, userService, entityConverter);
    }

    public void register(DefaultRequestContext context) {

        JsonObject newUserObj = context.request().getJsonBody();
        String passwordSalt = getUserService().createPasswordSalt();

        String hashedPassword = getUserService().hashPassword(newUserObj.getString("user.password"), passwordSalt);
        User user = getEntityFactory().createUser(null,
                                                  newUserObj.getString("user.email"),
                                                  newUserObj.getString("user.username"),
                                                  hashedPassword,
                                                  passwordSalt,
                                                  null,
                                                  null);

        getUserService().saveUser(user, newUserObj.getString("user.password"));

        String jwt = getUserService().createJwtToken(user);
        outputUser(context, user, jwt);
    }

    public void login(DefaultRequestContext context) {

        JsonObject requestBody = context.request().getJsonBody();
        String email = requestBody.getString("user.email");
        String password = requestBody.getString("user.password");

        User user = getUserService().getUser(email, password);
        if (user == null) {
            throw new ForbiddenException("Invalid credentials");
        }

        String jwt = getUserService().createJwtToken(user);
        outputUser(context, user, jwt);
    }

    public void currentUser(DefaultRequestContext context) {
        User user = getCurrentUser(context);
        outputUser(context, user, getDecodedJWT(context).getToken());
    }

    public void updateUser(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        JsonObject requestBody = context.request().getJsonBody();

        String email = requestBody.contains("user.email") ? requestBody.getString("user.email")
                                                          : currentUser.getEmail();

        String username = requestBody.contains("user.username") ? requestBody.getString("user.username")
                                                                : currentUser.getUsername();

        String image = requestBody.contains("user.image") ? requestBody.getString("user.image")
                                                          : currentUser.getImage();

        String bio = requestBody.contains("user.bio") ? requestBody.getString("user.bio")
                                                      : currentUser.getBio();

        String rawPassword = null;
        String hashedPassword = currentUser.getHashedPassword();
        String passwordSalt = currentUser.getPasswordSalt();
        if (requestBody.contains("user.password")) {
            rawPassword = requestBody.getString("user.password");
            passwordSalt = getUserService().createPasswordSalt();
            hashedPassword = getUserService().hashPassword(rawPassword, passwordSalt);
        }

        User updatedUser = getEntityFactory().createUser(currentUser.getId(),
                                                         email,
                                                         username,
                                                         hashedPassword,
                                                         passwordSalt,
                                                         bio,
                                                         image);

        getUserService().updateUser(currentUser, updatedUser, rawPassword);

        outputUser(context, updatedUser, getDecodedJWT(context).getToken());
    }

    public void getProfile(DefaultRequestContext context) {

        //==========================================
        // Anonymous allowed
        //==========================================
        User currentUser = null;
        if (isLoggedIn(context)) {
            currentUser = getCurrentUser(context);
        }

        String username = context.request().getPathParam("username");
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("The username path param can't be empty");
        }

        Profile profile = getUserService().getProfileByUsername(username, currentUser);
        if (profile == null) {
            throw new NotFoundException("User not found");
        }

        JsonObject profileToReturn = getEntityConverter().convertToOneProfile(profile);
        context.response().sendJson(profileToReturn);
    }

    public void follow(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String username = context.request().getPathParam("username");
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("The username path param can't be empty");
        }

        getUserService().follow(currentUser, username);

        Profile profile = getUserService().getProfileByUsername(username, currentUser);
        if (profile == null) {
            throw new NotFoundException("User not found");
        }

        JsonObject profileToReturn = getEntityConverter().convertToOneProfile(profile);
        context.response().sendJson(profileToReturn);
    }

    public void unfollow(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String username = context.request().getPathParam("username");
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("The username path param can't be empty");
        }

        getUserService().unfollow(currentUser, username);

        Profile profile = getUserService().getProfileByUsername(username, currentUser);
        if (profile == null) {
            throw new NotFoundException("User not found");
        }

        JsonObject profileToReturn = getEntityConverter().convertToOneProfile(profile);
        context.response().sendJson(profileToReturn);
    }

    protected void outputUser(DefaultRequestContext context, User user, String jwt) {
        JsonObject userToReturn = getEntityConverter().convertToOneUser(user, jwt);
        context.response().sendJson(userToReturn);
    }
}
