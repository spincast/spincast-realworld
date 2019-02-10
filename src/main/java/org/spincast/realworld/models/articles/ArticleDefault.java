package org.spincast.realworld.models.articles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.spincast.realworld.models.users.Profile;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class ArticleDefault implements Article {

    private final Long id;
    private final Instant creationDate;
    private final Instant modificationDate;
    private final Profile authorProfile;
    private final List<String> tags;
    private final String slug;
    private final String title;
    private final String description;
    private final String body;
    private final boolean favorited;
    private final int favoritesCount;

    @AssistedInject
    public ArticleDefault(@Assisted("id") @Nullable Long id,
                          @Assisted("creationDate") Instant creationDate,
                          @Assisted("modificationDate") Instant modificationDate,
                          @Assisted("authorProfile") Profile authorProfile,
                          @Assisted("tags") @Nullable List<String> tags,
                          @Assisted("slug") String slug,
                          @Assisted("title") String title,
                          @Assisted("description") String description,
                          @Assisted("body") String body,
                          @Assisted("favorited") boolean favorited,
                          @Assisted("favoritesCount") int favoritesCount) {
        this.id = id;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.authorProfile = authorProfile;
        this.tags = tags != null ? tags : new ArrayList<>();
        Collections.sort(this.tags);

        this.slug = slug;
        this.title = title;
        this.description = description;
        this.body = body;
        this.favorited = favorited;
        this.favoritesCount = favoritesCount;
    }

    @Override
    public Instant getCreatedAt() {
        return this.creationDate;
    }

    @Override
    public Instant getUpdatedAt() {
        return this.modificationDate;
    }

    @Override
    public Profile getAuthorProfile() {
        return this.authorProfile;
    }

    @Override
    public List<String> getTagList() {
        return this.tags;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getSlug() {
        return this.slug;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public boolean isFavorited() {
        return this.favorited;
    }

    @Override
    public int getFavoritesCount() {
        return this.favoritesCount;
    }
}
