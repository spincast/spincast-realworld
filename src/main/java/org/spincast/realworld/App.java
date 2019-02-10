package org.spincast.realworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.server.Server;
import org.spincast.defaults.bootstrapping.Spincast;
import org.spincast.plugins.crypto.SpincastCryptoPlugin;
import org.spincast.plugins.flywayutils.SpincastFlywayUtilsPlugin;
import org.spincast.plugins.jdbc.SpincastJdbcPlugin;
import org.spincast.plugins.routing.DefaultRouter;
import org.spincast.realworld.configs.AppConfig;
import org.spincast.realworld.controllers.ArticleController;
import org.spincast.realworld.controllers.ErrorController;
import org.spincast.realworld.controllers.UserController;
import org.spincast.realworld.db.DatabaseManager;
import org.spincast.realworld.db.DatabaseMigrater;
import org.spincast.realworld.filters.AuthFilter;

import com.google.inject.Inject;

public class App {

    protected final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * Bootstraps the application!
     */
    public static void main(String[] args) {
        Spincast.configure()
                .module(new AppModule())
                .plugin(new SpincastJdbcPlugin())
                .plugin(new SpincastFlywayUtilsPlugin())
                .plugin(new SpincastCryptoPlugin())
                .init(args);
    }

    private final Server server;
    private final DefaultRouter router;
    private final AppConfig appConfig;
    private final ErrorController errorController;
    private final UserController userController;
    private final ArticleController articleController;
    private final DatabaseManager databaseManager;
    private final DatabaseMigrater databaseMigrationsManager;
    private final AuthFilter authFilter;

    protected Server getServer() {
        return this.server;
    }

    protected DefaultRouter getRouter() {
        return this.router;
    }

    protected AppConfig getAppConfig() {
        return this.appConfig;
    }

    protected ErrorController getErrorController() {
        return this.errorController;
    }

    protected UserController getUserController() {
        return this.userController;
    }

    protected ArticleController getArticleController() {
        return this.articleController;
    }

    protected DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    protected DatabaseMigrater getDatabaseMigrater() {
        return this.databaseMigrationsManager;
    }

    protected AuthFilter getAuthFilter() {
        return this.authFilter;
    }

    /**
     * The application constructor in which Guice will inject
     * the required dependencies when the context is ready.
     */
    @Inject
    public App(Server server,
               DefaultRouter router,
               AppConfig appConfig,
               ErrorController generalController,
               UserController userController,
               DatabaseManager databaseManager,
               DatabaseMigrater databaseMigrater,
               AuthFilter authFilter,
               ArticleController articleController) {
        this.server = server;
        this.router = router;
        this.appConfig = appConfig;
        this.errorController = generalController;
        this.userController = userController;
        this.articleController = articleController;
        this.databaseManager = databaseManager;
        this.databaseMigrationsManager = databaseMigrater;
        this.authFilter = authFilter;
    }

    /**
     * Init method called by Guice once the App instance is
     * constructed.
     */
    @Inject
    protected void start() {

        //==========================================
        // Do we use the embedded database?
        //==========================================
        if (getAppConfig().isStartEmbeddedDb()) {
            getDatabaseManager().startDatabase();
        }

        //==========================================
        // Make sure the database is up-to-date
        //==========================================
        getDatabaseMigrater().migrateDatabase();

        //==========================================
        // Configure our routes
        //==========================================
        configureRoutes();

        //==========================================
        // Start the HTTP server!
        //==========================================
        getServer().start();

        this.logger.info("\n\n==========================================\n" +
                         "RealWorld.io API implementation using Spincast\n" +
                         getAppConfig().getPublicUrlBase() +
                         "\n==========================================\n\n");
    }

    protected void configureRoutes() {
        getRouter().cors();
        getRouter().notFound(getErrorController()::notFound);
        getRouter().exception(getErrorController()::exception);
        getRouter().ALL("/api/*{path}").pos(-100).found().handle(getAuthFilter()::saveJwt);
        getRouter().ALL("/api/*{path}").pos(-100).found().id("authFilter").handle(getAuthFilter()::validateJwt);

        getRouter().POST("/api/users").skip("authFilter").handle(getUserController()::register);
        getRouter().POST("/api/users/login").skip("authFilter").handle(getUserController()::login);
        getRouter().GET("/api/user").handle(getUserController()::currentUser);
        getRouter().PUT("/api/user").handle(getUserController()::updateUser);
        getRouter().GET("/api/profiles/${username}").skip("authFilter").handle(getUserController()::getProfile);
        getRouter().POST("/api/profiles/${username}/follow").handle(getUserController()::follow);
        getRouter().DELETE("/api/profiles/${username}/follow").handle(getUserController()::unfollow);
        getRouter().POST("/api/articles").handle(getArticleController()::createArticle);
        getRouter().GET("/api/articles/feed").handle(getArticleController()::feed);
        getRouter().GET("/api/articles/${slug}").skip("authFilter").handle(getArticleController()::get);
        getRouter().PUT("/api/articles/${slug}").handle(getArticleController()::updateArticle);
        getRouter().DELETE("/api/articles/${slug}").handle(getArticleController()::deleteArticle);
        getRouter().GET("/api/articles").skip("authFilter").handle(getArticleController()::findArticles);
        getRouter().POST("/api/articles/${slug}/comments").handle(getArticleController()::postComment);
        getRouter().GET("/api/articles/${slug}/comments").skip("authFilter").handle(getArticleController()::getComments);
        getRouter().DELETE("/api/articles/${slug}/comments/${commentId}").handle(getArticleController()::deleteComment);
        getRouter().POST("/api/articles/${slug}/favorite").handle(getArticleController()::favoriteArticle);
        getRouter().DELETE("/api/articles/${slug}/favorite").handle(getArticleController()::unfavoriteArticle);
        getRouter().GET("/api/tags").skip("authFilter").handle(getArticleController()::getTags);
    }

}
