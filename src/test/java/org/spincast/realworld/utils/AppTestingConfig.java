package org.spincast.realworld.utils;

import org.spincast.core.utils.SpincastUtils;
import org.spincast.plugins.config.SpincastConfigPluginConfig;
import org.spincast.realworld.configs.AppConfigDefault;
import org.spincast.testing.core.postgres.SpincastTestingPostgres;

import com.google.inject.Inject;

/**
 * Testing configurations for the application.
 */
public class AppTestingConfig extends AppConfigDefault {

    @Inject
    protected SpincastTestingPostgres spincastTestingPostgres;

    @Inject
    protected AppTestingConfig(SpincastConfigPluginConfig spincastConfigPluginConfig,
                               SpincastUtils spincastUtils) {
        super(spincastConfigPluginConfig,
              true,
              spincastUtils);
    }

    protected SpincastTestingPostgres getSpincastTestingPostgres() {
        return this.spincastTestingPostgres;
    }

    /**
     * We do not use the embedded database provided by the
     * application, we use a <em>testing</em> instance.
     */
    @Override
    public boolean isStartEmbeddedDb() {
        return false;
    }

    /**
     * We return the connection string for the
     * testing database.
     */
    @Override
    public String getDbConnectionString() {
        return getSpincastTestingPostgres().getDbConnectionString();
    }
}
