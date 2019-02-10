package org.spincast.realworld.models;

import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;

import com.google.inject.assistedinject.Assisted;

public interface EntityFactory {

    public User createUser(@Assisted("id") Long id,
                           @Assisted("email") String email,
                           @Assisted("username") String username,
                           @Assisted("hashedPassword") String hashedPassword,
                           @Assisted("passwordSalt") String passwordSalt,
                           @Assisted("bio") @Nullable String bio,
                           @Assisted("image") @Nullable String image);

    public Profile createProfile(@Assisted("user") User user,
                                 @Assisted("following") boolean following);

    public Article createArticle(@Assisted("id") Long id,
                                 @Assisted("creationDate") Instant creationDate,
                                 @Assisted("modificationDate") Instant modificationDate,
                                 @Assisted("authorProfile") Profile authorProfile,
                                 @Assisted("tags") @Nullable List<String> tags,
                                 @Assisted("slug") String slug,
                                 @Assisted("title") String title,
                                 @Assisted("description") String description,
                                 @Assisted("body") String body,
                                 @Assisted("favorited") boolean favorited,
                                 @Assisted("favoritesCount") int favoritesCount);

    public Comment createComment(@Assisted("id") Long id,
                                 @Assisted("authorProfile") Profile authorProfile,
                                 @Assisted("body") String body,
                                 @Assisted("creationDate") Instant creationDate,
                                 @Assisted("modificationDate") Instant modificationDate);
}
