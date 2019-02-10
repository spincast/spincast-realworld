package org.spincast.realworld.services;

import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface UserService {

    public User saveUser(User user, String rawPassword);

    public String createJwtToken(User user);

    public String hashPassword(String rawPassword, String salt);

    public String createPasswordSalt();

    public boolean isJwtValid(String token);

    /**
     * @return the decoded JWT or <code>null</code>
     * if the token is invalid.
     */
    public DecodedJWT decodeJwt(String token);

    /**
     * The password will be validated.
     *
     * @return the user or <code>null</code>
     * if the not found or the apssword is invalid.
     */
    public User getUser(String email, String rawPassword);


    /**
     * @return the user or <code>null</code>
     * if not found.
     */
    public User getUserByEmail(String email);

    /**
     * @return the user or <code>null</code>
     * if not found.
     */
    public User getUserByUsername(String username);

    public void updateUser(User currentUser, User newUserInfo, String rawPassword);

    /**
     * @param currentUser may be <code>null</code> if not
     * logged in.
     *
     * @return the profile or <code>null</code>
     * if the user is not found.
     *
     */
    public Profile getProfileById(long userId, User currentUser);

    /**
     *
     * @param currentUser may be <code>null</code> if not
     * logged in.
     *
     * @return the profile or <code>null</code>
     * if the user is not found.
     */
    public Profile getProfileByUsername(String username, User currentUser);

    public boolean isFollowingById(long sourceUserId, long targetUserId);

    public boolean isFollowingByUsername(long sourceUserId, String username);

    public void follow(User sourceUser, String targetUsername);

    public void unfollow(User sourceUser, String targetUsername);

    public boolean isEmailTaken(String email);

    public boolean isUsernameTaken(String username);

}
