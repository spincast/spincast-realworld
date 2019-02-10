package org.spincast.realworld.controllers.utils;

import java.util.List;

import org.spincast.core.json.JsonObject;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;

/**
 * Format entities to return as the specs require.
 *
 */
public interface ToResponseEntityConverter {

    public JsonObject convertUser(User user, String jwt);

    public JsonObject convertToOneUser(User user, String jwt);

    public JsonObject convertProfile(Profile profile);

    public JsonObject convertToOneProfile(Profile profile);

    public JsonObject convertArticle(Article article);

    public JsonObject convertToOneArticle(Article article);

    public JsonObject convertToArticles(List<Article> articles, long totalCount);

    public JsonObject convertComment(Comment comment);

    public JsonObject convertToOneComment(Comment comment);

    public JsonObject convertToComments(List<Comment> comments);

    public JsonObject convertToTags(List<String> tags);

}
