package org.spincast.realworld.configs;

import org.spincast.core.config.SpincastConfig;

/**
 * The application specific configurations.
 */
public interface AppConfig extends SpincastConfig {

    /**
     * If <code>true</code>, the provided embedded
     * Postgres database instance will be used and
     * {@link #getDbConnectionString()} must be
     * "jdbc:postgresql://localhost:12346/postgres"!
     */
    public boolean isStartEmbeddedDb();

    public String getDbConnectionString();

    public String getAuthJwtSecret();

    public int getAuthJwtTtlMinutes();



}
