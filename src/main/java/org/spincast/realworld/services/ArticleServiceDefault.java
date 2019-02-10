package org.spincast.realworld.services;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.spincast.plugins.jdbc.utils.ItemsAndTotalCount;
import org.spincast.realworld.exceptions.ForbiddenException;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.ArticleValidator;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.repositories.ArticleRepository;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class ArticleServiceDefault implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleValidator articleValidator;
    private final EntityFactory entityFactory;

    @Inject
    public ArticleServiceDefault(ArticleRepository articleRepository,
                                 ArticleValidator articleValidator,
                                 EntityFactory entityFactory) {
        this.articleRepository = articleRepository;
        this.articleValidator = articleValidator;
        this.entityFactory = entityFactory;
    }

    protected ArticleRepository getArticleRepository() {
        return this.articleRepository;
    }

    protected ArticleValidator getArticleValidator() {
        return this.articleValidator;
    }

    protected EntityFactory getEntityFactory() {
        return this.entityFactory;
    }

    @Override
    public Article saveArticle(Article article) {
        getArticleValidator().validateArticle(article);
        return getArticleRepository().saveArticle(article);
    }

    @Override
    public String createUniqueSlug(String title) {

        String slug;
        if (StringUtils.isBlank(title)) {
            slug = UUID.randomUUID().toString();
        } else {
            slug = title;
        }

        slug = slug.toLowerCase();

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
                         .replaceAll("[^\\p{Alpha}\\p{Digit}\\-_ ]", "")
                         .replaceAll("[_ ]", "-");

        //==========================================
        // So it doesn't conflict with "GET /api/articles/feed"
        //==========================================
        if (slug.equalsIgnoreCase("feed")) {
            slug = "feed1";
        }

        int nbr = getArticleRepository().getSlugsNbrWithBody(slug);
        if (nbr > 0) {
            slug = slug + "-" + (nbr + 1);
        }

        return slug;
    }

    @Override
    public Article getArticle(String slug, User currentUser) {
        return getArticleRepository().getArticle(slug, currentUser);
    }

    @Override
    public Article updateArticle(Article currentArticle,
                                 String title,
                                 String description,
                                 String body,
                                 List<String> tags) {

        String slug = currentArticle.getSlug();
        if (!currentArticle.getTitle().equals(title)) {
            slug = createUniqueSlug(title);
        }

        Article updatedArticle = getEntityFactory().createArticle(currentArticle.getId(),
                                                                  currentArticle.getCreatedAt(),
                                                                  Instant.now(),
                                                                  currentArticle.getAuthorProfile(),
                                                                  tags,
                                                                  slug,
                                                                  title,
                                                                  description,
                                                                  body,
                                                                  currentArticle.isFavorited(),
                                                                  currentArticle.getFavoritesCount());

        getArticleValidator().validateArticle(updatedArticle);
        return getArticleRepository().updateArticle(updatedArticle);
    }

    @Override
    public void deleteArticle(long articleId) {
        getArticleRepository().deleteArticle(articleId);
    }

    @Override
    public Article favoriteArticle(long articleId, User currentUser) {
        return getArticleRepository().favoriteArticle(articleId, currentUser);
    }

    @Override
    public Article unfavoriteArticle(long articleId, User currentUser) {
        return getArticleRepository().unfavoriteArticle(articleId, currentUser);
    }

    @Override
    public ItemsAndTotalCount<Article> findArticles(User currentUser,
                                                    String tagFilter,
                                                    String authorUsernameFilter,
                                                    String favoritedByUsernameFilter,
                                                    long offset,
                                                    int limit) {

        return getArticleRepository().findArticles(currentUser,
                                                   tagFilter,
                                                   authorUsernameFilter,
                                                   favoritedByUsernameFilter,
                                                   offset,
                                                   limit);
    }

    @Override
    public ItemsAndTotalCount<Article> getFeed(User currentUser, long offset, int limit) {
        return getArticleRepository().getFeed(currentUser,
                                              offset,
                                              limit);
    }

    @Override
    public Comment saveComment(long articleId, String body, User currentUser) {
        return getArticleRepository().saveComment(articleId, body, currentUser);
    }

    @Override
    public Comment getComment(long commentId, User currentUser) {
        return getArticleRepository().getComment(commentId, currentUser);
    }

    @Override
    public List<Comment> getComments(long articleId, User currentUser) {
        return getArticleRepository().getComments(articleId, currentUser);
    }

    @Override
    public void deleteComment(Comment comment, User currentUser) {
        if (comment.getAuthorProfile().getUser().getId() != currentUser.getId()) {
            throw new ForbiddenException("You can't delete this comment");
        }
        getArticleRepository().deleteComment(comment.getId());
    }

    @Override
    public List<String> getTags() {
        return getArticleRepository().getTags();
    }

}
