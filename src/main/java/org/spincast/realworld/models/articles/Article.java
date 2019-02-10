package org.spincast.realworld.models.articles;

import java.time.Instant;
import java.util.List;

import org.spincast.realworld.models.users.Profile;

public interface Article {

    /**
     * Will be <code>null</code> until the entity
     * is saved.
     */
    public Long getId();

    public String getSlug();

    public Instant getCreatedAt();

    public Instant getUpdatedAt();

    public Profile getAuthorProfile();

    public List<String> getTagList();

    public String getTitle();

    public String getDescription();

    public String getBody();

    /**
     * Always <code>false</code> if the user is anonymous.
     */
    public boolean isFavorited();

    public int getFavoritesCount();

}
