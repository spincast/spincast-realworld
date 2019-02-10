package org.spincast.realworld.models.users;

public interface User {

    /**
     * Will be <code>null</code> until the entity
     * is saved.
     */
    public Long getId();

    public String getEmail();

    public String getUsername();

    public String getHashedPassword();

    public String getPasswordSalt();

    public String getBio();

    public String getImage();
}
