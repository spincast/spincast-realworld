package org.spincast.realworld.controllers;

import java.time.Instant;
import java.util.List;

import org.spincast.core.exchange.DefaultRequestContext;
import org.spincast.core.json.JsonObject;
import org.spincast.plugins.jdbc.utils.ItemsAndTotalCount;
import org.spincast.realworld.controllers.utils.ToResponseEntityConverter;
import org.spincast.realworld.exceptions.BadRequestException;
import org.spincast.realworld.exceptions.ForbiddenException;
import org.spincast.realworld.exceptions.NotFoundException;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.ArticleService;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;
import org.spincast.shaded.org.apache.commons.lang3.tuple.Pair;
import org.spincast.shaded.org.apache.http.HttpStatus;

import com.google.inject.Inject;

public class ArticleController extends BaseController {

    private final ArticleService articleService;

    @Inject
    public ArticleController(EntityFactory entityFactory,
                             UserService userService,
                             ToResponseEntityConverter entityConverter,
                             ArticleService articleService) {
        super(entityFactory, userService, entityConverter);
        this.articleService = articleService;
    }

    protected ArticleService getArticleService() {
        return this.articleService;
    }

    public void createArticle(DefaultRequestContext context) {

        User user = getCurrentUser(context);
        Profile userProfile = getEntityFactory().createProfile(user, false);

        JsonObject newArticleObj = context.request().getJsonBody();

        String title = newArticleObj.getString("article.title");

        String slug = getArticleService().createUniqueSlug(title);

        Instant now = Instant.now();

        Article article = getEntityFactory().createArticle(null,
                                                           now,
                                                           now,
                                                           userProfile,
                                                           newArticleObj.getJsonArrayOrEmpty("article.tagList")
                                                                        .convertToStringList(),
                                                           slug,
                                                           title,
                                                           newArticleObj.getString("article.description"),
                                                           newArticleObj.getString("article.body"),
                                                           false,
                                                           0);

        getArticleService().saveArticle(article);

        JsonObject articleToReturn = getEntityConverter().convertToOneArticle(article);
        context.response().sendJson(articleToReturn);
    }

    public void get(DefaultRequestContext context) {

        //==========================================
        // Anonymous allowed
        //==========================================
        User currentUser = null;
        if (isLoggedIn(context)) {
            currentUser = getCurrentUser(context);
        }

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        JsonObject articleToReturn = getEntityConverter().convertToOneArticle(article);
        context.response().sendJson(articleToReturn);
    }

    public void updateArticle(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article currentArticle = getArticleService().getArticle(slug, currentUser);
        if (currentArticle == null) {
            throw new NotFoundException("Article not found");
        }
        if (currentArticle.getAuthorProfile().getUser().getId() != currentUser.getId()) {
            throw new ForbiddenException("You can't modify this article");
        }

        JsonObject requestBody = context.request().getJsonBody();

        String title = requestBody.contains("article.title") ? requestBody.getString("article.title")
                                                             : currentArticle.getTitle();

        String description = requestBody.contains("article.description") ? requestBody.getString("article.description")
                                                                         : currentArticle.getDescription();

        String body = requestBody.contains("article.body") ? requestBody.getString("article.body")
                                                           : currentArticle.getBody();

        List<String> tags =
                requestBody.contains("article.tagList") ? requestBody.getJsonArrayOrEmpty("article.tagList").convertToStringList()
                                                        : currentArticle.getTagList();

        Article updatedArticle = getArticleService().updateArticle(currentArticle,
                                                                   title,
                                                                   description,
                                                                   body,
                                                                   tags);

        JsonObject articleToReturn = getEntityConverter().convertToOneArticle(updatedArticle);
        context.response().sendJson(articleToReturn);
    }

    public void deleteArticle(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }
        if (article.getAuthorProfile().getUser().getId() != currentUser.getId()) {
            throw new ForbiddenException("You can't delete this article");
        }

        getArticleService().deleteArticle(article.getId());
        context.response().setStatusCode(HttpStatus.SC_OK);
    }

    public void favoriteArticle(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        article = getArticleService().favoriteArticle(article.getId(), currentUser);

        JsonObject articleToReturn = getEntityConverter().convertToOneArticle(article);
        context.response().sendJson(articleToReturn);
    }

    public void unfavoriteArticle(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        article = getArticleService().unfavoriteArticle(article.getId(), currentUser);

        JsonObject articleToReturn = getEntityConverter().convertToOneArticle(article);
        context.response().sendJson(articleToReturn);
    }

    public void findArticles(DefaultRequestContext context) {

        //==========================================
        // Anonymous allowed
        //==========================================
        User currentUser = null;
        if (isLoggedIn(context)) {
            currentUser = getCurrentUser(context);
        }

        Pair<Long, Integer> offsetAndLimit = getOffsetlAndLimit(context, 20, 1000);

        String tagFilter = context.request().getQueryStringParamFirst("tag");
        String authorUsernameFilter = context.request().getQueryStringParamFirst("author");
        String favoritedByUsernameFilter = context.request().getQueryStringParamFirst("favorited");

        ItemsAndTotalCount<Article> articlesAndTotalCount = getArticleService().findArticles(currentUser,
                                                                                             tagFilter,
                                                                                             authorUsernameFilter,
                                                                                             favoritedByUsernameFilter,
                                                                                             offsetAndLimit.getLeft(),
                                                                                             offsetAndLimit.getRight());

        JsonObject articlesToReturn =
                getEntityConverter().convertToArticles(articlesAndTotalCount.getItems(), articlesAndTotalCount.getTotalCount());
        context.response().sendJson(articlesToReturn);
    }

    public void feed(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        Pair<Long, Integer> offsetAndLimit = getOffsetlAndLimit(context, 20, 1000);

        ItemsAndTotalCount<Article> articlesAndTotalCount = getArticleService().getFeed(currentUser,
                                                                                        offsetAndLimit.getLeft(),
                                                                                        offsetAndLimit.getRight());

        JsonObject articlesToReturn =
                getEntityConverter().convertToArticles(articlesAndTotalCount.getItems(), articlesAndTotalCount.getTotalCount());
        context.response().sendJson(articlesToReturn);
    }

    public void postComment(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        String body = context.request().getJsonBody().getString("comment.body");

        Comment comment = getArticleService().saveComment(article.getId(), body, currentUser);

        JsonObject commentToReturn = getEntityConverter().convertToOneComment(comment);
        context.response().sendJson(commentToReturn);
    }

    public void getComments(DefaultRequestContext context) {

        //==========================================
        // Anonymous allowed
        //==========================================
        User currentUser = null;
        if (isLoggedIn(context)) {
            currentUser = getCurrentUser(context);
        }

        String slug = context.request().getPathParam("slug");
        if (StringUtils.isBlank(slug)) {
            throw new BadRequestException("The slug path param is required");
        }

        Article article = getArticleService().getArticle(slug, currentUser);
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        List<Comment> comments = getArticleService().getComments(article.getId(), currentUser);

        JsonObject commentsToReturn = getEntityConverter().convertToComments(comments);
        context.response().sendJson(commentsToReturn);
    }

    public void deleteComment(DefaultRequestContext context) {

        User currentUser = getCurrentUser(context);

        //==========================================
        // We don't care about the article slug, just the
        // comment id.
        //==========================================
        //String slug = context.request().getPathParam("slug");

        String commentIdStr = context.request().getPathParam("commentId");
        long commentId = -1;
        try {
            commentId = Long.parseLong(commentIdStr);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid comment id : " + commentIdStr);
        }

        Comment comment = getArticleService().getComment(commentId, currentUser);
        if (comment == null) {
            throw new NotFoundException("Comment not found");
        }

        getArticleService().deleteComment(comment, currentUser);

        context.response().setStatusCode(HttpStatus.SC_OK);
    }

    public void getTags(DefaultRequestContext context) {

        List<String> tags = getArticleService().getTags();

        JsonObject commentsToReturn = getEntityConverter().convertToTags(tags);
        context.response().sendJson(commentsToReturn);
    }

}
