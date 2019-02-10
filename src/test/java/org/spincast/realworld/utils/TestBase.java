package org.spincast.realworld.utils;

import java.io.File;

import javax.sql.DataSource;

import org.spincast.core.config.SpincastConfig;
import org.spincast.core.guice.SpincastGuiceModuleBase;
import org.spincast.defaults.testing.AppBasedDefaultContextTypesTestingBase;
import org.spincast.plugins.jacksonjson.SpincastJsonManager;
import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.realworld.App;
import org.spincast.realworld.configs.AppConfig;
import org.spincast.realworld.services.ArticleService;
import org.spincast.realworld.services.UserService;
import org.spincast.testing.core.AppTestingConfigs;
import org.spincast.testing.core.postgres.PostgresDataDir;
import org.spincast.testing.core.postgres.SpincastTestingPostgres;
import org.spincast.testing.core.utils.SpincastConfigTestingDefault;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;

public abstract class TestBase extends AppBasedDefaultContextTypesTestingBase {

    @Inject
    protected UserService userService;

    @Inject
    protected ArticleService articleService;

    @Inject
    private SpincastJsonManager spincastJsonManager;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    protected SpincastTestingPostgres spincastTestingPostgres;

    @Inject
    private DataSource testDataSource;

    protected UserService getUserService() {
        return this.userService;
    }

    protected ArticleService getArticleService() {
        return this.articleService;
    }

    protected SpincastJsonManager getSpincastJsonManager() {
        return this.spincastJsonManager;
    }

    protected JdbcUtils getJdbcUtils() {
        return this.jdbcUtils;
    }

    protected SpincastTestingPostgres getPg() {
        return this.spincastTestingPostgres;
    }

    protected DataSource getTestDataSource() {
        return this.testDataSource;
    }

    /**
     * Starts the actual application so we can
     * make real HTTP requests to it.
     */
    @Override
    protected void callAppMainMethod() {
        App.main(null);
    }

    @Override
    protected Module getExtraOverridingModule() {
        return Modules.override(super.getExtraOverridingModule()).with(new SpincastGuiceModuleBase() {

            @Override
            protected void configure() {

                //==========================================
                // Configure Spincast Postgres which is a
                // embedded Potsgres instance to be
                // used to run tests.
                //
                // The database will be started automatically
                // and info about it can then be retrieved using
                // SpincastTestingPostgres.
                //==========================================
                bind(File.class).annotatedWith(PostgresDataDir.class).toInstance(createTestingDir());
                bind(DataSource.class).toProvider(SpincastTestingPostgres.class).in(Scopes.SINGLETON);
            }
        });
    }

    /**
     * This allows us to tweak the app configurations
     * that are going to be used during the tests.
     */
    @Override
    protected AppTestingConfigs getAppTestingConfigs() {
        return new AppTestingConfigs() {

            @Override
            public boolean isBindAppClass() {
                return true;
            }

            @Override
            public Class<? extends SpincastConfig> getSpincastConfigTestingImplementationClass() {

                //==========================================
                // Those are default configs suggested by Spincast
                // to be used when running tests. For example,
                // a free port will be found and used to start
                // the HTTP server.
                //==========================================
                return SpincastConfigTestingDefault.class;
            }

            @Override
            public Class<?> getAppConfigTestingImplementationClass() {

                //==========================================
                // We tweak the default configurations of
                // the app, for example to use a testing
                // database instance instead of the provided one.
                //==========================================
                return AppTestingConfig.class;
            }

            @Override
            public Class<?> getAppConfigInterface() {
                return AppConfig.class;
            }
        };
    }

    @Override
    public void afterClass() {
        getPg().stopPostgres();
        super.afterClass();
    }

}
