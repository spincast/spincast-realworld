package org.spincast.realworld.repositories;

import java.util.List;

import org.spincast.plugins.jdbc.utils.ItemsAndTotalCount;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.User;

public interface ArticleRepository {

    public Article saveArticle(Article article);

    public int getSlugsNbrWithBody(String slugBody);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public Article getArticle(String articleSlug, User currentUser);

    public List<String> getTags(String articleSlug);

    public int getFavoritesCount(String articleSlug);

    public boolean isFavorited(String articleSlug, long userId);

    public Article updateArticle(Article article);

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

    public Comment getComment(long commentId, User currentUser);

    public Comment saveComment(long articleId, String body, User currentUser);

    /**
     * @param currentUser can be <code>null</code> if not
     * logged in.
     */
    public List<Comment> getComments(long articleId, User currentUser);

    public void deleteComment(long commentId);

    public List<String> getTags();

}
