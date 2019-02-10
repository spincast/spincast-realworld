package org.spincast.realworld.models.users;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class ProfileDefault implements Profile {

    private final User user;
    private final boolean following;

    @AssistedInject
    public ProfileDefault(@Assisted("user") User user,
                          @Assisted("following") boolean following) {
        this.user = user;
        this.following = following;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public boolean isFollowing() {
        return this.following;
    }

    @Override
    public String toString() {
        String str = "Profile of " + getUser().getUsername();
        if (isFollowing()) {
            str += " - following";
        }
        return str;
    }
}
