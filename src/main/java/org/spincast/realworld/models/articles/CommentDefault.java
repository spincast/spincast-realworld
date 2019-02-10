package org.spincast.realworld.models.articles;

import java.time.Instant;

import org.spincast.realworld.models.users.Profile;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class CommentDefault implements Comment {

    private final long id;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String body;
    private final Profile authorProfile;

    @AssistedInject
    public CommentDefault(@Assisted("id") Long id,
                          @Assisted("authorProfile") Profile authorProfile,
                          @Assisted("body") String body,
                          @Assisted("creationDate") Instant creationDate,
                          @Assisted("modificationDate") Instant modificationDate) {
        this.id = id;
        this.createdAt = creationDate;
        this.updatedAt = modificationDate;
        this.body = body;
        this.authorProfile = authorProfile;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public Profile getAuthorProfile() {
        return this.authorProfile;
    }
}
