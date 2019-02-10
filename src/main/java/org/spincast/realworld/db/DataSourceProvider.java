package org.spincast.realworld.db;

import javax.sql.DataSource;

import org.spincast.plugins.jdbc.SpincastDataSource;
import org.spincast.plugins.jdbc.SpincastDataSourceFactory;
import org.spincast.realworld.configs.AppConfig;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates the main {@link DataSource} using
 * a <a href="https://github.com/brettwooldridge/HikariCP">Hikari</a> connection pool.
 */
public class DataSourceProvider implements Provider<DataSource> {

    private final SpincastDataSourceFactory spincastDataSourceFactory;
    private final AppConfig appConfig;
    private SpincastDataSource dataSource;

    @Inject
    public DataSourceProvider(SpincastDataSourceFactory spincastDataSourceFactory,
                              AppConfig appConfig) {
        this.spincastDataSourceFactory = spincastDataSourceFactory;
        this.appConfig = appConfig;
    }

    protected SpincastDataSourceFactory getSpincastDataSourceFactory() {
        return this.spincastDataSourceFactory;
    }

    protected AppConfig getAppConfig() {
        return this.appConfig;
    }

    @Override
    public DataSource get() {
        if (this.dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getAppConfig().getDbConnectionString());
            config.setUsername("postgres");
            config.setPassword("postgres");
            config.setMaximumPoolSize(10);
            DataSource ds = new HikariDataSource(config);
            this.dataSource = getSpincastDataSourceFactory().create(ds);
        }
        return this.dataSource;
    }
}
