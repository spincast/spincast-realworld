package org.spincast.realworld.repositories;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.json.JsonManager;
import org.spincast.core.json.JsonObject;
import org.spincast.plugins.jdbc.JdbcQueries;
import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.plugins.jdbc.SpincastResultSet;
import org.spincast.plugins.jdbc.statements.BatchInsertStatement;
import org.spincast.plugins.jdbc.statements.DeleteStatement;
import org.spincast.plugins.jdbc.statements.InsertStatement;
import org.spincast.plugins.jdbc.statements.ResultSetHandler;
import org.spincast.plugins.jdbc.statements.SelectStatement;
import org.spincast.plugins.jdbc.statements.UpdateStatement;
import org.spincast.plugins.jdbc.utils.ItemsAndTotalCount;
import org.spincast.plugins.jdbc.utils.ItemsAndTotalCountDefault;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.UserService;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ArticleRepositoryDefault extends BaseRepository implements ArticleRepository {

    protected final static Logger logger = LoggerFactory.getLogger(ArticleRepositoryDefault.class);

    private final JsonManager jsonManager;
    private final UserRepository userRepository;

    @Inject
    public ArticleRepositoryDefault(Provider<DataSource> dataSource,
                                    JdbcUtils jdbcUtils,
                                    EntityFactory entityFactory,
                                    UserService userService,
                                    JsonManager jsonManager,
                                    UserRepository userRepository) {
        super(dataSource, jdbcUtils, entityFactory, userService);
        this.jsonManager = jsonManager;
        this.userRepository = userRepository;
    }

    protected JsonManager getJsonManager() {
        return this.jsonManager;
    }

    protected UserRepository getUserRepository() {
        return this.userRepository;
    }

    @Override
    public Article saveArticle(Article article) {

        return getJdbcUtils().scopes().transactional(getDataSource(), new JdbcQueries<Article>() {

            @Override
            public Article run(Connection connection) {

                InsertStatement stm = getJdbcUtils().statements().createInsertStatement(connection);

                //==========================================
                // Article itself
                //==========================================
                stm.sql("INSERT INTO articles(slug,  title,  description,  body,  author_id) " +
                        "VALUES              (:slug, :title, :description, :body, :author_id) ");

                stm.setString("slug", article.getSlug());
                stm.setString("title", article.getTitle());
                stm.setString("description", article.getDescription());
                stm.setString("body", article.getBody());
                stm.setLong("author_id", article.getAuthorProfile().getUser().getId());

                long generatedId = stm.insertGetGeneratedId("id");

                //==========================================
                // Tags
                //==========================================
                if (article.getTagList().size() > 0) {
                    saveTags(connection, generatedId, article.getTagList());
                }

                Article savedArticle = getEntityFactory().createArticle(generatedId,
                                                                        article.getCreatedAt(),
                                                                        article.getUpdatedAt(),
                                                                        article.getAuthorProfile(),
                                                                        article.getTagList(),
                                                                        article.getSlug(),
                                                                        article.getTitle(),
                                                                        article.getDescription(),
                                                                        article.getBody(),
                                                                        article.isFavorited(),
                                                                        article.getFavoritesCount());
                return savedArticle;
            }
        });
    }

    @Override
    public Article updateArticle(Article article) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

                //==========================================
                // Article itself
                //==========================================
                stm.sql("UPDATE articles " +
                        "SET slug = :slug, " +
                        "    title = :title, " +
                        "    description = :description, " +
                        "    body = :body " +
                        "WHERE " +
                        "   id = :id");

                stm.setLong("id", article.getId());
                stm.setString("slug", article.getSlug());
                stm.setString("title", article.getTitle());
                stm.setString("description", article.getDescription());
                stm.setString("body", article.getBody());

                stm.update();

                //==========================================
                // Tags
                //
                // Postgres 11 is supposed to have proper
                // MERGE support.
                //==========================================
                DeleteStatement stm2 = getJdbcUtils().statements().createDeleteStatement(connection);
                stm2.sql("DELETE FROM tags " +
                         "WHERE article_id = :article_id ");
                stm2.setLong("article_id", article.getId());
                stm2.delete();

                saveTags(connection, article.getId(), article.getTagList());

                return null;
            }
        });

        return article;
    }

    /**
     * For an UPDATE, current tags must already have
     * been deleted.
     */
    protected void saveTags(Connection connection, long articleId, List<String> tags) {
        if (tags.size() > 0) {
            BatchInsertStatement stm2 = getJdbcUtils().statements().createBatchInsertStatement(connection);
            stm2.sql("INSERT INTO tags(tag, article_id) " +
                     "VALUES (:tag, :article_id) " +
                     "ON CONFLICT (tag, article_id) DO NOTHING ");

            for (String tag : tags) {
                stm2.setString("tag", tag);
                stm2.setLong("article_id", articleId);
                stm2.addBatch();
            }
            stm2.batchInsert();
        }
    }

    @Override
    public int getSlugsNbrWithBody(String slugBody) {

        Integer nbr = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Integer>() {

            @Override
            public Integer run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT COUNT(*) as nbr " +
                        "FROM articles " +
                        "WHERE slug = :slugBody " +
                        "OR (slug ~* (:slugBody || '-[0-9]+')) ");

                stm.setString("slugBody", slugBody);

                return stm.selectOne(new ResultSetHandler<Integer>() {

                    @Override
                    public Integer handle(SpincastResultSet rs) throws Exception {
                        return rs.getIntegerOrZero("nbr");
                    }
                });
            }
        });

        if (nbr == null) {
            nbr = 0;
        }
        return nbr;
    }

    @Override
    public Article getArticle(String articleSlug, User currentUser) {
        List<Article> articles = getArticles(null, articleSlug, currentUser);
        if (articles == null || articles.size() == 0) {
            return null;
        }
        return articles.get(0);
    }

    public Article getArticleById(Long id, User currentUser) {
        List<Article> articles = getArticles(Lists.newArrayList(id), currentUser);
        if (articles == null || articles.size() == 0) {
            return null;
        }
        return articles.get(0);
    }

    public List<Article> getArticles(List<Long> ids, User currentUser) {
        return getArticles(ids, null, currentUser);
    }

    protected List<Article> getArticles(List<Long> ids, String articleSlug, User currentUser) {

        return getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<List<Article>>() {

            @Override
            public List<Article> run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                //==========================================
                // Get main article informations
                //==========================================
                stm.sql("SELECT articles.id, slug, title, description, body, author_id, creation_date, modification_date " +
                        "FROM articles ");

                if (ids != null && ids.size() > 0) {
                    stm.sql("INNER JOIN UNNEST('{:idsList}'::int[]) WITH ORDINALITY o(id, ord) ON o.id = articles.id ");
                    stm.setLongList("idsList", ids);
                }

                if (articleSlug != null) {
                    stm.sql("WHERE slug = :slug ");
                    stm.setString("slug", articleSlug);
                }

                if (ids != null && ids.size() > 0) {
                    stm.sql("ORDER BY o.ord ");
                }

                LinkedHashMap<Long, JsonObject> articleIdToBaseRows = new LinkedHashMap<Long, JsonObject>();

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {

                        long articleId = rs.getLongOrNull("id");
                        JsonObject infos = articleIdToBaseRows.get(articleId);
                        if (infos == null) {
                            infos = getJsonManager().create();
                            articleIdToBaseRows.put(articleId, infos);
                        }

                        infos.set("id", articleId);
                        infos.set("slug", rs.getString("slug"));
                        infos.set("title", rs.getString("title"));
                        infos.set("description", rs.getString("description"));
                        infos.set("body", rs.getString("body"));
                        infos.set("author_id", rs.getLongOrNull("author_id"));
                        infos.set("creation_date", rs.getInstant("creation_date"));
                        infos.set("modification_date", rs.getInstant("modification_date"));
                        return null;
                    }
                });

                if (articleIdToBaseRows == null || articleIdToBaseRows.size() == 0) {
                    return new ArrayList<Article>();
                }
                Set<Long> articleIds = new HashSet<>();
                Set<Long> authorsIds = new HashSet<>();
                for (Entry<Long, JsonObject> entry : articleIdToBaseRows.entrySet()) {
                    JsonObject baseInfo = entry.getValue();
                    articleIds.add(entry.getKey());
                    authorsIds.add(baseInfo.getLong("author_id"));
                }

                //==========================================
                // Get authors profiles
                //==========================================
                Map<Long, Profile> authorsProfilesMap = getUserRepository().getProfilesByUserIds(authorsIds, currentUser);

                //==========================================
                // Get tags
                //==========================================
                Map<Long, Set<String>> tagsMap = getTagsByArticlesIds(articleIds);

                //==========================================
                // Get isFavoriteds
                //==========================================
                Map<Long, Boolean> isFavoritedsMap =
                        currentUser != null ? getFavoritedByArticles(articleIds, currentUser.getId()) : null;

                //==========================================
                // Get favoritesCounts
                //==========================================
                Map<Long, Integer> favoritesCountsMap = getFavoritesCountByArticles(articleIds);

                //==========================================
                // Build final articles
                //==========================================
                List<Article> articles = new ArrayList<>();
                for (Entry<Long, JsonObject> entry : articleIdToBaseRows.entrySet()) {

                    long articleId = entry.getKey();
                    JsonObject baseInfos = entry.getValue();

                    long authorId = baseInfos.getLong("author_id");

                    Profile authorProfile = authorsProfilesMap.get(authorId);
                    Set<String> tags = tagsMap.get(articleId);
                    Boolean favorited = currentUser != null ? isFavoritedsMap.get(articleId) : false;
                    Integer favoritesCount = favoritesCountsMap.get(articleId);
                    if (authorProfile == null || tags == null || favorited == null || favoritesCount == null) {
                        logger.warn("At least one of authorProfile, tags, favorited or favoritesCount is null for article #" +
                                    articleId);
                        continue;
                    }

                    Article article = getEntityFactory().createArticle(articleId,
                                                                       baseInfos.getInstant("creation_date"),
                                                                       baseInfos.getInstant("modification_date"),
                                                                       authorProfile,
                                                                       new ArrayList<>(tags),
                                                                       baseInfos.getString("slug"),
                                                                       baseInfos.getString("title"),
                                                                       baseInfos.getString("description"),
                                                                       baseInfos.getString("body"),
                                                                       favorited,
                                                                       favoritesCount);
                    articles.add(article);
                }
                return articles;
            }
        });
    }

    @Override
    public List<String> getTags(String articleSlug) {

        final List<String> tags = new ArrayList<>();

        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT tag " +
                        "FROM tags " +
                        "WHERE article_id = " +
                        "   ( SELECT id " +
                        "       FROM articles " +
                        "       WHERE slug = :slug " +
                        "   ) ");

                stm.setString("slug", articleSlug);

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {
                        String tag = rs.getString("tag");
                        tags.add(tag);
                        return null;
                    }
                });
                return null;
            }
        });

        return tags;
    }

    protected Map<Long, Set<String>> getTagsByArticlesIds(Set<Long> articleIds) {

        Map<Long, Set<String>> tagsByArticles = new HashMap<>();
        if (articleIds == null || articleIds.size() == 0) {
            return tagsByArticles;
        }

        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT article_id, tag " +
                        "FROM tags " +
                        "WHERE article_id IN(:ids) ");
                stm.setInLong("ids", articleIds);

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {
                        long articleId = rs.getLongOrNull("article_id");
                        String tag = rs.getString("tag");

                        Set<String> tags = tagsByArticles.get(articleId);
                        if (tags == null) {
                            tags = new HashSet<String>();
                            tagsByArticles.put(articleId, tags);
                        }
                        tags.add(tag);
                        return null;
                    }
                });
                return null;
            }
        });

        return tagsByArticles;
    }

    @Override
    public int getFavoritesCount(String articleSlug) {
        Integer nbr = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Integer>() {

            @Override
            public Integer run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT COUNT(*) as nbr " +
                        "FROM favorites " +
                        "WHERE article_id = " +
                        "   ( SELECT id " +
                        "       FROM articles " +
                        "       WHERE slug = :slug " +
                        "   ) ");

                stm.setString("slug", articleSlug);

                return stm.selectOne(new ResultSetHandler<Integer>() {

                    @Override
                    public Integer handle(SpincastResultSet rs) throws Exception {
                        return rs.getIntegerOrZero("nbr");
                    }
                });
            }
        });

        if (nbr == null) {
            nbr = 0;
        }
        return nbr;
    }

    protected Map<Long, Integer> getFavoritesCountByArticles(Set<Long> articleIds) {

        Map<Long, Integer> favoritesCountByArticles = new HashMap<>();
        if (articleIds == null || articleIds.size() == 0) {
            return favoritesCountByArticles;
        }

        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT article_id, COUNT(*) as nbr " +
                        "FROM favorites " +
                        "WHERE article_id IN(:ids) " +
                        "GROUP BY article_id ");
                stm.setInLong("ids", articleIds);

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {
                        favoritesCountByArticles.put(rs.getLongOrNull("article_id"), rs.getIntegerOrZero("nbr"));
                        return null;
                    }
                });
                return null;
            }
        });

        for (Long articleId : articleIds) {
            if (!favoritesCountByArticles.containsKey(articleId)) {
                favoritesCountByArticles.put(articleId, 0);
            }
        }

        return favoritesCountByArticles;
    }

    @Override
    public boolean isFavorited(String articleSlug, long userId) {
        Boolean favorited = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Boolean>() {

            @Override
            public Boolean run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT 1 " +
                        "FROM favorites " +
                        "WHERE article_id = " +
                        "   ( SELECT id " +
                        "       FROM articles " +
                        "       WHERE slug = :slug " +
                        "   ) " +
                        "AND user_id = :user_id ");

                stm.setString("slug", articleSlug);
                stm.setLong("user_id", userId);

                return stm.selectOne(new ResultSetHandler<Boolean>() {

                    @Override
                    public Boolean handle(SpincastResultSet rs) throws Exception {
                        return true;
                    }
                });
            }
        });

        if (favorited == null) {
            favorited = false;
        }
        return favorited;
    }

    protected Map<Long, Boolean> getFavoritedByArticles(Set<Long> articleIds, long sourceUserId) {

        Map<Long, Boolean> favoritedByArticles = new HashMap<>();
        if (articleIds == null || articleIds.size() == 0) {
            return favoritedByArticles;
        }

        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT article_id " +
                        "FROM favorites " +
                        "WHERE article_id IN(:ids) " +
                        "AND user_id = :user_id ");
                stm.setInLong("ids", articleIds);
                stm.setLong("user_id", sourceUserId);

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {
                        favoritedByArticles.put(rs.getLongOrNull("article_id"), true);
                        return null;
                    }
                });
                return null;
            }
        });

        for (Long articleId : articleIds) {
            if (!favoritedByArticles.containsKey(articleId)) {
                favoritedByArticles.put(articleId, false);
            }
        }

        return favoritedByArticles;
    }

    @Override
    public void deleteArticle(long articleId) {

        getJdbcUtils().scopes().transactional(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                DeleteStatement stm = getJdbcUtils().statements().createDeleteStatement(connection);

                //==========================================
                // Delete favorites
                //==========================================
                stm.sql("DELETE FROM favorites " +
                        "WHERE article_id = :article_id ");
                stm.setLong("article_id", articleId);
                stm.delete();

                //==========================================
                // Delete tags
                //==========================================
                stm = getJdbcUtils().statements().createDeleteStatement(connection);
                stm.sql("DELETE FROM tags " +
                        "WHERE article_id = :article_id ");
                stm.setLong("article_id", articleId);
                stm.delete();

                //==========================================
                // Delete comments
                //==========================================
                stm = getJdbcUtils().statements().createDeleteStatement(connection);
                stm.sql("DELETE FROM comments " +
                        "WHERE article_id = :article_id ");
                stm.setLong("article_id", articleId);
                stm.delete();

                //==========================================
                // Delete article itself
                //==========================================
                stm = getJdbcUtils().statements().createDeleteStatement(connection);
                stm.sql("DELETE FROM articles " +
                        "WHERE id = :id ");
                stm.setLong("id", articleId);
                stm.delete();

                return null;
            }
        });
    }

    @Override
    public Article favoriteArticle(long articleId, User currentUser) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                InsertStatement stm = getJdbcUtils().statements().createInsertStatement(connection);

                stm.sql("INSERT INTO favorites(user_id, article_id) " +
                        "VALUES (:user_id, :article_id) " +
                        "ON CONFLICT (user_id, article_id) DO NOTHING ");

                stm.setLong("user_id", currentUser.getId());
                stm.setLong("article_id", articleId);

                stm.insert();

                return null;
            }
        });

        return getArticleById(articleId, currentUser);
    }

    @Override
    public Article unfavoriteArticle(long articleId, User currentUser) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                DeleteStatement stm = getJdbcUtils().statements().createDeleteStatement(connection);

                stm.sql("DELETE FROM favorites " +
                        "WHERE user_id = :user_id " +
                        "AND article_id = :article_id ");

                stm.setLong("user_id", currentUser.getId());
                stm.setLong("article_id", articleId);

                stm.delete();

                return null;
            }
        });

        return getArticleById(articleId, currentUser);
    }

    @Override
    public ItemsAndTotalCount<Article> findArticles(User currentUser,
                                                    String tagFilter,
                                                    String authorUsernameFilter,
                                                    String favoritedByUsernameFilter,
                                                    long offset,
                                                    int limit) {
        ItemsAndTotalCount<Article> articlesAndTotalCount =
                getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<ItemsAndTotalCount<Article>>() {

                    @Override
                    public ItemsAndTotalCount<Article> run(Connection connection) throws Exception {
                        SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                        //==========================================
                        // Get the articles ids to return
                        //==========================================
                        stm.sql("SELECT articles.id as articleId " +
                                "FROM articles ");

                        if (tagFilter != null) {
                            stm.sql("INNER JOIN tags " +
                                    "ON tags.article_id = articles.id " +
                                    "AND tag = :tag ");
                            stm.setString("tag", tagFilter);
                        }

                        if (favoritedByUsernameFilter != null) {
                            stm.sql("INNER JOIN favorites " +
                                    "ON favorites.article_id = articles.id " +
                                    "AND user_id = (" +
                                    "   SELECT id " +
                                    "   FROM users " +
                                    "   WHERE username = :favUsername " +
                                    ") ");
                            stm.setString("favUsername", favoritedByUsernameFilter);
                        }

                        stm.sql("WHERE 1=1 ");

                        if (authorUsernameFilter != null) {
                            stm.sql(" AND articles.author_id = (" +
                                    "   SELECT id " +
                                    "   FROM users " +
                                    "   WHERE username = :authorUsername " +
                                    ") ");
                            stm.setString("authorUsername", authorUsernameFilter);
                        }

                        stm.sql("ORDER BY articles.creation_date DESC ");

                        if (limit > 0) {
                            stm.sql("LIMIT :limit ");
                            stm.setInteger("limit", limit);
                        }
                        if (offset > 0) {
                            stm.sql("OFFSET  :offset ");
                            stm.setLong("offset", offset);
                        }

                        ItemsAndTotalCount<Long> articleIdsAndTotalCount = stm.selectListAndTotal(new ResultSetHandler<Long>() {

                            @Override
                            public Long handle(SpincastResultSet rs) throws Exception {
                                return rs.getLongOrNull("articleId");
                            }
                        });

                        if (articleIdsAndTotalCount.getTotalCount() == 0) {
                            return new ItemsAndTotalCountDefault<Article>();
                        }

                        //==========================================
                        // Get the articles themselves
                        //==========================================
                        List<Article> articles = getArticles(articleIdsAndTotalCount.getItems(), currentUser);

                        return new ItemsAndTotalCountDefault<Article>(articles, articleIdsAndTotalCount.getTotalCount());
                    }
                });

        if (articlesAndTotalCount == null) {
            articlesAndTotalCount = new ItemsAndTotalCountDefault<Article>();
        }

        return articlesAndTotalCount;
    }

    @Override
    public ItemsAndTotalCount<Article> getFeed(User currentUser, long offset, int limit) {
        ItemsAndTotalCount<Article> articlesAndTotalCount =
                getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<ItemsAndTotalCount<Article>>() {

                    @Override
                    public ItemsAndTotalCount<Article> run(Connection connection) throws Exception {
                        SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                        //==========================================
                        // Get the articles ids to return
                        //==========================================
                        stm.sql("SELECT articles.id as articleId " +
                                "FROM articles " +
                                "INNER JOIN followings " +
                                "ON followings.source_user_id = :source_user_id " +
                                "WHERE followings.target_user_id = articles.author_id " +
                                "ORDER BY articles.creation_date DESC ");
                        stm.setLong("source_user_id", currentUser.getId());

                        if (limit > 0) {
                            stm.sql("LIMIT :limit ");
                            stm.setInteger("limit", limit);
                        }
                        if (offset > 0) {
                            stm.sql("OFFSET  :offset ");
                            stm.setLong("offset", offset);
                        }

                        ItemsAndTotalCount<Long> articleIdsAndTotalCount = stm.selectListAndTotal(new ResultSetHandler<Long>() {

                            @Override
                            public Long handle(SpincastResultSet rs) throws Exception {
                                return rs.getLongOrNull("articleId");
                            }
                        });

                        if (articleIdsAndTotalCount.getTotalCount() == 0) {
                            return new ItemsAndTotalCountDefault<Article>();
                        }

                        //==========================================
                        // Get the articles themselves
                        //==========================================
                        List<Article> articles = getArticles(articleIdsAndTotalCount.getItems(), currentUser);

                        return new ItemsAndTotalCountDefault<Article>(articles, articleIdsAndTotalCount.getTotalCount());
                    }
                });

        if (articlesAndTotalCount == null) {
            articlesAndTotalCount = new ItemsAndTotalCountDefault<Article>();
        }

        return articlesAndTotalCount;
    }

    @Override
    public Comment getComment(long commentId, User currentUser) {
        List<Comment> comments = getComments(commentId, null, currentUser);
        if (comments == null || comments.size() == 0) {
            return null;
        }
        return comments.get(0);
    }

    @Override
    public List<Comment> getComments(long articleId, User currentUser) {
        return getComments(null, articleId, currentUser);
    }

    protected List<Comment> getComments(Long commentId, Long articleId, User currentUser) {

        return getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<List<Comment>>() {

            @Override
            public List<Comment> run(Connection connection) {

                //==========================================
                // Get comments base infos
                //==========================================
                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT id, author_id, article_id, body, creation_date, modification_date " +
                        "FROM comments " +
                        "WHERE 1=1 ");

                if (commentId != null) {
                    stm.sql("AND id = :id ");
                    stm.setLong("id", commentId);
                }

                if (articleId != null) {
                    stm.sql("AND article_id = :article_id ");
                    stm.setLong("article_id", articleId);
                }

                stm.sql("ORDER BY creation_date DESC ");

                LinkedHashMap<Long, JsonObject> baseInfosByCommentId = new LinkedHashMap<>();
                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {

                        long commentId = rs.getLongOrNull("id");
                        JsonObject baseInfos = getJsonManager().create();
                        baseInfosByCommentId.put(commentId, baseInfos);

                        baseInfos.set("id", commentId);
                        baseInfos.set("author_id", rs.getLongOrNull("author_id"));
                        baseInfos.set("article_id", rs.getLongOrNull("article_id"));
                        baseInfos.set("body", rs.getString("body"));
                        baseInfos.set("creation_date", rs.getInstant("creation_date"));
                        baseInfos.set("modification_date", rs.getInstant("modification_date"));

                        return null;
                    }
                });

                if (baseInfosByCommentId.size() == 0) {
                    return null;
                }

                //==========================================
                // Get authors profiles
                //==========================================
                Set<Long> authorIds = new HashSet<>();
                for (JsonObject commentBaseInfos : baseInfosByCommentId.values()) {
                    authorIds.add(commentBaseInfos.getLong("author_id"));
                }
                Map<Long, Profile> profilesByUserIds = getUserRepository().getProfilesByUserIds(authorIds, currentUser);

                //==========================================
                // Build final comments
                //==========================================
                List<Comment> comments = new ArrayList<>();
                for (JsonObject commentBaseInfos : baseInfosByCommentId.values()) {

                    long authorId = commentBaseInfos.getLong("author_id");
                    Profile authorProfile = profilesByUserIds.get(authorId);
                    if (authorProfile == null) {
                        logger.error("Profile not found for comment author #" + authorId);
                        return null;
                    }

                    Comment comment = getEntityFactory().createComment(commentBaseInfos.getLong("id"),
                                                                       authorProfile,
                                                                       commentBaseInfos.getString("body"),
                                                                       commentBaseInfos.getInstant("creation_date"),
                                                                       commentBaseInfos.getInstant("modification_date"));
                    comments.add(comment);
                }
                return comments;
            }
        });
    }

    @Override
    public Comment saveComment(long articleId, String body, User currentUser) {
        return getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Comment>() {

            @Override
            public Comment run(Connection connection) {

                InsertStatement stm = getJdbcUtils().statements().createInsertStatement(connection);

                stm.sql("INSERT INTO comments(author_id,  article_id,  body) " +
                        "VALUES              (:author_id, :article_id, :body) ");

                stm.setLong("author_id", currentUser.getId());
                stm.setLong("article_id", articleId);
                stm.setString("body", body);

                stm.insert();

                long generatedId = stm.insertGetGeneratedId("id");

                return getComment(generatedId, currentUser);
            }
        });
    }

    @Override
    public void deleteComment(long commentId) {
        getJdbcUtils().scopes().transactional(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                DeleteStatement stm = getJdbcUtils().statements().createDeleteStatement(connection);

                //==========================================
                // Delete favorites
                //==========================================
                stm.sql("DELETE FROM comments " +
                        "WHERE id = :id ");
                stm.setLong("id", commentId);
                stm.delete();

                return null;
            }
        });
    }

    @Override
    public List<String> getTags() {
        List<String> tags = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<List<String>>() {

            @Override
            public List<String> run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT DISTINCT tag " +
                        "FROM tags " +
                        "ORDER BY tag ");

                return stm.selectList(new ResultSetHandler<String>() {

                    @Override
                    public String handle(SpincastResultSet rs) throws Exception {
                        return rs.getString("tag");
                    }
                });
            }
        });

        return tags != null ? tags : Collections.emptyList();
    }
}
