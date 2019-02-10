package org.spincast.realworld.models.users;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class UserDefault implements User {

    private final Long id;
    private final String email;
    private final String hashedPassword;
    private final String passwordSalt;
    private final String username;
    private final String bio;
    private final String image;

    @AssistedInject
    public UserDefault(@Assisted("id") @Nullable Long id,
                       @Assisted("email") String email,
                       @Assisted("username") String username,
                       @Assisted("hashedPassword") String hashedPassword,
                       @Assisted("passwordSalt") String passwordSalt,
                       @Assisted("bio") @Nullable String bio,
                       @Assisted("image") @Nullable String image) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.passwordSalt = passwordSalt;
        this.username = username;
        this.bio = bio;
        this.image = image;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getHashedPassword() {
        return this.hashedPassword;
    }

    @Override
    public String getPasswordSalt() {
        return this.passwordSalt;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @Nullable
    public String getBio() {
        return this.bio;
    }

    @Override
    @Nullable
    public String getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return "User " + getUsername();
    }
}
