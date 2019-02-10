package org.spincast.realworld.db;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.json.JsonManager;
import org.spincast.core.utils.SpincastStatics;
import org.spincast.core.utils.SpincastUtils;
import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.plugins.jdbc.SpincastDataSourceFactory;
import org.spincast.realworld.configs.AppConfig;

import com.google.inject.Inject;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;

/**
 * Starts and stops an embedded PostgreSQL instance.
 */
public class DatabaseManager {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final AppConfig appConfig;
    private final JsonManager jsonManager;
    private final SpincastUtils spincastUtils;
    private final JdbcUtils jdbcUtils;
    private final SpincastDataSourceFactory spincastDataSourceFactory;
    private EmbeddedPostgres pg = null;
    private File pgDataDir;

    @Inject
    public DatabaseManager(AppConfig appConfig,
                           JsonManager jsonManager,
                           JdbcUtils jdbcUtils,
                           SpincastDataSourceFactory spincastDataSourceFactory,
                           SpincastUtils spincastUtils) {
        this.appConfig = appConfig;
        this.jsonManager = jsonManager;
        this.jdbcUtils = jdbcUtils;
        this.spincastDataSourceFactory = spincastDataSourceFactory;
        this.spincastUtils = spincastUtils;
    }

    protected AppConfig getAppConfig() {
        return this.appConfig;
    }

    protected JsonManager getJsonManager() {
        return this.jsonManager;
    }

    protected JdbcUtils getJdbcUtils() {
        return this.jdbcUtils;
    }

    protected SpincastUtils getSpincastUtils() {
        return this.spincastUtils;
    }

    protected SpincastDataSourceFactory getSpincastDataSourceFactory() {
        return this.spincastDataSourceFactory;
    }

    protected int getDbPort() {
        return 12346;
    }

    public void startDatabase() {

        if (this.pg != null) {
            this.logger.warn("Database already started!");
            return;
        }

        try {
            this.logger.info("Starting embedded PostgreSQL database on port " + getDbPort() + ". Please wait...");

            this.pg = EmbeddedPostgres.builder()
                                      .setPort(getDbPort())
                                      .setDataDirectory(getDbDataDir())
                                      .setCleanDataDirectory(false)
                                      .start();

            this.logger.info("Database started! Data stored in: " + getDbDataDir().getAbsolutePath());

        } catch (Exception ex) {
            throw SpincastStatics.runtimize(ex);
        }
    }

    protected File getDbDataDir() {
        if (this.pgDataDir == null) {
            File baseDir = getSpincastUtils().getAppJarDirectory();
            if (baseDir == null) {
                baseDir = getSpincastUtils().getAppRootDirectoryNoJar();
            }

            this.pgDataDir = new File(baseDir, "dbData");
            if (!this.pgDataDir.isDirectory()) {
                boolean ok = this.pgDataDir.mkdirs();
                if (!ok) {
                    throw new RuntimeException("Unable to create the temp directory to store the database data: " +
                                               this.pgDataDir.getAbsolutePath());
                }
            }
        }
        return this.pgDataDir;
    }

    /**
     * Stops Postgres.
     */
    public void stopPostgres() {
        if (this.pg != null) {
            try {
                this.pg.close();
                this.pg = null;
            } catch (Exception ex) {
                this.logger.warn("Error stopping the embedded PostgreSQL instance", ex);
            }
        }
    }

}
