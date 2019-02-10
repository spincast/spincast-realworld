package org.spincast.realworld.repositories;

import javax.sql.DataSource;

import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.services.UserService;

import com.google.inject.Inject;
import com.google.inject.Provider;

public abstract class BaseRepository {

    private final Provider<DataSource> dataSource;
    private final JdbcUtils jdbcUtils;
    private final EntityFactory entityFactory;
    private final UserService userService;

    @Inject
    public BaseRepository(Provider<DataSource> dataSource,
                          JdbcUtils jdbcUtils,
                          EntityFactory entityFactory,
                          UserService userService) {
        this.dataSource = dataSource;
        this.jdbcUtils = jdbcUtils;
        this.entityFactory = entityFactory;
        this.userService = userService;
    }

    protected DataSource getDataSource() {
        return this.dataSource.get();
    }

    protected JdbcUtils getJdbcUtils() {
        return this.jdbcUtils;
    }

    protected EntityFactory getEntityFactory() {
        return this.entityFactory;
    }

    protected UserService getUserService() {
        return this.userService;
    }
}
