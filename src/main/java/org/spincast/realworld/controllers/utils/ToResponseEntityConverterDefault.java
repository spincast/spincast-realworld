package org.spincast.realworld.controllers.utils;

import java.util.List;

import org.spincast.core.json.JsonArray;
import org.spincast.core.json.JsonManager;
import org.spincast.core.json.JsonObject;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.UserService;

import com.google.inject.Inject;

public class ToResponseEntityConverterDefault implements ToResponseEntityConverter {

    private final UserService userService;
    private final JsonManager jsonManager;

    @Inject
    public ToResponseEntityConverterDefault(UserService userService,
                                            JsonManager jsonManager) {
        this.userService = userService;
        this.jsonManager = jsonManager;
    }

    protected UserService getUserService() {
        return this.userService;
    }

    protected JsonManager getJsonManager() {
        return this.jsonManager;
    }

    @Override
    public JsonObject convertUser(User user, String jwt) {
        JsonObject userObj = getJsonManager().create();
        userObj.set("username", user.getUsername());
        userObj.set("email", user.getEmail());
        userObj.set("bio", user.getBio());
        userObj.set("image", user.getImage());
        if (jwt != null) {
            userObj.set("token", jwt);
        }
        return userObj;
    }

    @Override
    public JsonObject convertToOneUser(User user, String jwt) {
        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("user", convertUser(user, jwt));
        return wrapObj;
    }

    @Override
    public JsonObject convertProfile(Profile profile) {
        JsonObject profileObj = getJsonManager().create();
        profileObj.set("username", profile.getUser().getUsername());
        profileObj.set("bio", profile.getUser().getBio());
        profileObj.set("image", profile.getUser().getImage());
        profileObj.set("following", profile.isFollowing());
        return profileObj;
    }

    @Override
    public JsonObject convertToOneProfile(Profile profile) {
        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("profile", convertProfile(profile));
        return wrapObj;
    }

    @Override
    public JsonObject convertToOneArticle(Article article) {
        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("article", convertArticle(article));
        return wrapObj;
    }


    @Override
    public JsonObject convertArticle(Article article) {
        JsonObject articleObj = getJsonManager().fromObject(article);
        articleObj.remove("id");
        articleObj.remove("authorProfile");
        articleObj.set("author", convertProfile(article.getAuthorProfile()));

        return articleObj;
    }

    @Override
    public JsonObject convertToArticles(List<Article> articles, long totalCount) {
        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("articlesCount", totalCount);

        JsonArray articlesArray = getJsonManager().createArray();
        if (articles != null) {
            for (Article article : articles) {
                articlesArray.add(convertArticle(article));
            }
        }
        wrapObj.set("articles", articlesArray);

        return wrapObj;
    }

    @Override
    public JsonObject convertComment(Comment comment) {
        JsonObject articleObj = getJsonManager().fromObject(comment);
        articleObj.remove("authorProfile");
        articleObj.set("author", convertProfile(comment.getAuthorProfile()));

        return articleObj;
    }

    @Override
    public JsonObject convertToOneComment(Comment comment) {
        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("comment", convertComment(comment));
        return wrapObj;
    }

    @Override
    public JsonObject convertToComments(List<Comment> comments) {

        JsonArray commentsArray = getJsonManager().createArray();
        if (comments != null) {
            for (Comment comment : comments) {
                commentsArray.add(convertComment(comment));
            }
        }

        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("comments", commentsArray);

        return wrapObj;
    }

    @Override
    public JsonObject convertToTags(List<String> tags) {
        JsonArray tagsArray = getJsonManager().createArray();
        if (tags != null) {
            for (String tag : tags) {
                tagsArray.add(tag);
            }
        }

        JsonObject wrapObj = getJsonManager().create();
        wrapObj.set("tags", tagsArray);

        return wrapObj;
    }

}
