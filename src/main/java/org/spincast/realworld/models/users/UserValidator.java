package org.spincast.realworld.models.users;

import java.util.ArrayList;
import java.util.List;

import org.spincast.core.utils.SpincastUtils;
import org.spincast.realworld.exceptions.ValidationError;
import org.spincast.realworld.exceptions.ValidationErrorsException;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;
import org.spincast.shaded.org.apache.commons.validator.routines.EmailValidator;
import org.spincast.shaded.org.apache.commons.validator.routines.UrlValidator;

import com.google.inject.Inject;

public class UserValidator {

    protected static final char[] EXTRA_INVALID_CHARACTERS =
            {'`', '<', '>', '"', '\'', ' '};

    private final UserService userService;
    private final SpincastUtils spincastUtils;
    private UrlValidator urlValidator;

    @Inject
    public UserValidator(UserService userService,
                         SpincastUtils spincastUtils) {
        this.userService = userService;
        this.spincastUtils = spincastUtils;
    }

    protected UserService getUserService() {
        return this.userService;
    }

    protected SpincastUtils getSpincastUtils() {
        return this.spincastUtils;
    }

    /**
     * Validate a {@link User}.
     *
     * @throws {@link ValidationErrorsException} if something is
     * not valid.
     */
    public void validateUser(User user, String rawPassword, User currentUser, boolean isNewUser) {

        List<ValidationError> errors = new ArrayList<>();

        if (user.getEmail() == null ||
            !EmailValidator.getInstance().isValid(user.getEmail())) {
            errors.add(new ValidationError("email", "Invalid email: " + user.getEmail()));
        } else if (isNewUser && getUserService().isEmailTaken(user.getEmail())) {
            errors.add(new ValidationError("email",
                                           "This email is already taken: " + user.getEmail()));
        }

        if (user.getUsername() == null ||
            user.getUsername().length() < 3 ||
            user.getUsername().length() > 255) {
            errors.add(new ValidationError("username",
                                           "Invalid username, must be between 3 and 255 characters: " + user.getUsername()));
        } else if (isNewUser && getUserService().isUsernameTaken(user.getUsername())) {
            errors.add(new ValidationError("username",
                                           "This username is already taken: " + user.getUsername()));
        }

        if (getSpincastUtils().isContainsSpecialCharacters(user.getUsername()) ||
            StringUtils.containsAny(user.getUsername(), EXTRA_INVALID_CHARACTERS)) {
            errors.add(new ValidationError("password",
                                           "The username contains whitespaces or invalid characters: " + user.getUsername()));
        }

        boolean validatePassword = currentUser == null || rawPassword != null;
        if (validatePassword && (rawPassword == null ||
                                 rawPassword.length() < 6 ||
                                 rawPassword.length() > 255)) {
            errors.add(new ValidationError("password",
                                           "Invalid password, must be between 6 and 255 characters: " + rawPassword));
        }

        if (getSpincastUtils().isContainsSpecialCharacters(rawPassword) ||
            StringUtils.containsAny(rawPassword, EXTRA_INVALID_CHARACTERS)) {
            errors.add(new ValidationError("password",
                                           "The password contains whitespaces or invalid characters: " + rawPassword));
        }

        if (user.getBio() != null &&
            user.getBio().length() > 10000) {
            errors.add(new ValidationError("bio",
                                           "Invalid bio, maximum 10000 characters. Currently: " + user.getBio().length() +
                                                  " characters."));
        }

        if (user.getImage() != null &&
            (user.getImage().length() > 2048 || !getUrlValidator().isValid(user.getImage()))) {
            errors.add(new ValidationError("image",
                                           "Invalid image path: " + user.getImage()));
        }

        if (currentUser != null) {
            if (!currentUser.getEmail().equals(user.getEmail())) {
                User existingUser = getUserService().getUserByEmail(user.getEmail());
                if (existingUser != null) {
                    errors.add(new ValidationError("email",
                                                   "This email is not available: " + user.getEmail()));
                }
            }
            if (!currentUser.getUsername().equals(user.getUsername())) {
                User existingUser = getUserService().getUserByUsername(user.getUsername());
                if (existingUser != null) {
                    errors.add(new ValidationError("username",
                                                   "This username is not available: " + user.getUsername()));
                }
            }
        }

        if (errors.size() > 0) {
            throw new ValidationErrorsException(errors);
        }
    }

    public UrlValidator getUrlValidator() {
        if (this.urlValidator == null) {
            String[] schemes = {"http", "https"};
            this.urlValidator = new UrlValidator(schemes);
        }
        return this.urlValidator;
    }
}
