package org.spincast.realworld.models.articles;

import java.time.Instant;

import org.spincast.realworld.models.users.Profile;

public interface Comment {

    public long getId();

    public Instant getCreatedAt();

    public Instant getUpdatedAt();

    public String getBody();

    public Profile getAuthorProfile();
}
