package org.spincast.realworld.services;

import java.util.List;

import org.spincast.plugins.jdbc.utils.ItemsAndTotalCount;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.User;

public interface ArticleService {

    public Article saveArticle(Article article);

    public String createUniqueSlug(String title);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public Article getArticle(String slug, User currentUser);

    public Article updateArticle(Article currentArticle,
                                 String title,
                                 String description,
                                 String body,
                                 List<String> tags);

    public void deleteArticle(long articleId);

    public Article favoriteArticle(long articleId, User currentUser);

    public Article unfavoriteArticle(long articleId, User currentUser);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public ItemsAndTotalCount<Article> findArticles(User currentUser,
                                                    String tagFilter,
                                                    String authorUsernameFilter,
                                                    String favoritedByUsernameFilter,
                                                    long offset,
                                                    int limit);

    public ItemsAndTotalCount<Article> getFeed(User currentUser,
                                               long offset,
                                               int limit);

    public Comment saveComment(long articleId, String body, User currentUser);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public Comment getComment(long commentId, User currentUser);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public List<Comment> getComments(long articleId, User currentUser);

    public void deleteComment(Comment comment, User currentUser);

    public List<String> getTags();
}
