package org.spincast.realworld.db;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.utils.SpincastStatics;
import org.spincast.plugins.flywayutils.SpincastFlywayFactory;
import org.spincast.plugins.flywayutils.SpincastFlywayMigrationContext;
import org.spincast.realworld.db.migrations.M_2019_02_02_01;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Applies database migrations, using
 * <a href="https://flywaydb.org">Flyway</a>.
 */
public class DatabaseMigrater {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseMigrater.class);

    private final Provider<DataSource> dataSource;
    private final SpincastFlywayFactory spincastFlywayFactory;

    @Inject
    public DatabaseMigrater(SpincastFlywayFactory spincastFlywayFactory,
                            Provider<DataSource> dataSource) {
        this.spincastFlywayFactory = spincastFlywayFactory;
        this.dataSource = dataSource;
    }

    protected SpincastFlywayFactory getSpincastFlywayFactory() {
        return this.spincastFlywayFactory;
    }

    protected DataSource getDataSource() {
        return this.dataSource.get();
    }

    public void migrateDatabase() {

        try {
            this.logger.info("Migrating database...");

            SpincastFlywayMigrationContext migrationContext =
                    getSpincastFlywayFactory().createMigrationContext(getDataSource(),
                                                                      "public",
                                                                      M_2019_02_02_01.class.getPackage()
                                                                                           .getName());
            migrationContext.migrate();

            this.logger.info("Database migration done.");
        } catch (Exception ex) {
            throw SpincastStatics.runtimize(ex);
        }
    }

}
