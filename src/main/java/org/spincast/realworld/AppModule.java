package org.spincast.realworld;

import javax.sql.DataSource;

import org.spincast.core.guice.SpincastGuiceModuleBase;
import org.spincast.realworld.configs.AppConfig;
import org.spincast.realworld.configs.AppConfigDefault;
import org.spincast.realworld.controllers.ArticleController;
import org.spincast.realworld.controllers.UserController;
import org.spincast.realworld.controllers.utils.ToResponseEntityConverter;
import org.spincast.realworld.controllers.utils.ToResponseEntityConverterDefault;
import org.spincast.realworld.db.DataSourceProvider;
import org.spincast.realworld.db.DatabaseManager;
import org.spincast.realworld.db.DatabaseMigrater;
import org.spincast.realworld.filters.AuthFilter;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.models.articles.ArticleDefault;
import org.spincast.realworld.models.articles.ArticleValidator;
import org.spincast.realworld.models.articles.Comment;
import org.spincast.realworld.models.articles.CommentDefault;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.ProfileDefault;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.models.users.UserDefault;
import org.spincast.realworld.models.users.UserValidator;
import org.spincast.realworld.repositories.ArticleRepository;
import org.spincast.realworld.repositories.ArticleRepositoryDefault;
import org.spincast.realworld.repositories.UserRepository;
import org.spincast.realworld.repositories.UserRepositoryDefault;
import org.spincast.realworld.services.ArticleService;
import org.spincast.realworld.services.ArticleServiceDefault;
import org.spincast.realworld.services.UserService;
import org.spincast.realworld.services.UserServiceDefault;

import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module for the application.
 */
public class AppModule extends SpincastGuiceModuleBase {

    @Override
    protected void configure() {

        bind(AppConfig.class).to(AppConfigDefault.class).in(Scopes.SINGLETON);
        bind(DatabaseManager.class).in(Scopes.SINGLETON);
        bind(DatabaseMigrater.class).in(Scopes.SINGLETON);
        bind(DataSource.class).toProvider(DataSourceProvider.class).in(Scopes.SINGLETON);
        bind(UserController.class).in(Scopes.SINGLETON);
        bind(ArticleController.class).in(Scopes.SINGLETON);
        bind(UserService.class).to(UserServiceDefault.class).in(Scopes.SINGLETON);
        bind(ArticleService.class).to(ArticleServiceDefault.class).in(Scopes.SINGLETON);
        bind(UserRepository.class).to(UserRepositoryDefault.class).in(Scopes.SINGLETON);
        bind(ArticleRepository.class).to(ArticleRepositoryDefault.class).in(Scopes.SINGLETON);
        bind(AuthFilter.class).in(Scopes.SINGLETON);
        bind(ToResponseEntityConverter.class).to(ToResponseEntityConverterDefault.class).in(Scopes.SINGLETON);
        bind(UserValidator.class).in(Scopes.SINGLETON);
        bind(ArticleValidator.class).in(Scopes.SINGLETON);

        install(new FactoryModuleBuilder().implement(User.class,
                                                     UserDefault.class)
                                          .implement(Profile.class,
                                                     ProfileDefault.class)
                                          .implement(Article.class,
                                                     ArticleDefault.class)
                                          .implement(Comment.class,
                                                     CommentDefault.class)
                                          .build(EntityFactory.class));
    }
}
