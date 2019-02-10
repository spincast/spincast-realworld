package org.spincast.realworld.configs;

import org.spincast.core.guice.TestingMode;
import org.spincast.core.utils.SpincastUtils;
import org.spincast.plugins.config.SpincastConfigDefault;
import org.spincast.plugins.config.SpincastConfigPluginConfig;

import com.google.inject.Inject;

/**
 * Most of the application configurations values are
 * loaded from the "app-config.yaml" file.
 */
public class AppConfigDefault extends SpincastConfigDefault implements AppConfig {

    private final SpincastUtils spincastUtils;

    @Inject
    protected AppConfigDefault(SpincastConfigPluginConfig spincastConfigPluginConfig,
                               @TestingMode boolean testingMode,
                               SpincastUtils spincastUtils) {
        super(spincastConfigPluginConfig, testingMode);
        this.spincastUtils = spincastUtils;
    }

    protected SpincastUtils getSpincastUtils() {
        return this.spincastUtils;
    }

    @Override
    public boolean isDevelopmentMode() {
        return getBoolean("spincast.isDevelopementMode");
    }

    @Override
    public String getPublicUrlBase() {
        return getString("spincast.publicAccess.urlBase");
    }

    @Override
    public int getHttpServerPort() {

        return -1;
    }

    @Override
    public int getHttpsServerPort() {
        return 12345;
    }

    @Override
    public String getHttpsKeyStorePath() {
        return "certificates/devKeyStore.jks";
    }

    @Override
    public String getHttpsKeyStoreType() {
        return "JKS";
    }

    @Override
    public String getHttpsKeyStoreStorePass() {
        return "myStorePass";
    }

    @Override
    public String getHttpsKeyStoreKeyPass() {
        return "myKeyPass";
    }

    @Override
    public boolean isStartEmbeddedDb() {
        return getBoolean("db.startEmbeddedDb");
    }

    @Override
    public String getDbConnectionString() {
        return getString("db.connectionString");
    }

    @Override
    public String getAuthJwtSecret() {
        return getString("auth.jwt.secret");
    }

    @Override
    public int getAuthJwtTtlMinutes() {
        return getInteger("auth.jwt.ttlMinutes");
    }


}
