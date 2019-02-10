package org.spincast.realworld.db.migrations;

import java.sql.Connection;

import javax.sql.DataSource;

import org.spincast.plugins.flywayutils.SpincastFlywayMigrationBase;
import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.plugins.jdbc.statements.UpdateStatement;
import org.spincast.realworld.configs.AppConfig;

import com.google.inject.Inject;

/**
 * First database migrations.
 * <p>
 * Creates the required tables/indexes/etc.
 */
public class M_2019_02_02_01 extends SpincastFlywayMigrationBase {

    private final AppConfig appConfig;

    @Inject
    public M_2019_02_02_01(DataSource dataSource,
                           JdbcUtils jdbcUtils,
                           AppConfig appConfig) {
        super(dataSource, jdbcUtils);
        this.appConfig = appConfig;
    }

    protected AppConfig getAppConfig() {
        return this.appConfig;
    }

    @Override
    protected void runMigration(Connection connection) {

        //==========================================
        // For performant LIKE and ILIKE
        //==========================================
        installPgTrgm(connection);

        //==========================================
        // To search without accents
        //==========================================
        installUnaccent(connection);

        //==========================================
        // To update a "modification_date" column
        // if the row is updated
        //==========================================
        installModificationDateFunction(connection);

        createUsersTable(connection);
        createFollowingsTable(connection);
        createArticleTable(connection);
        createFavoritesTable(connection);
        createTagsTable(connection);
        createCommentsTable(connection);
    }

    protected void installPgTrgm(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE EXTENSION pg_trgm;");
        stm.update();
    }

    protected void installUnaccent(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE EXTENSION unaccent;");
        stm.update();

        //==========================================
        // "noaccent" function
        // @see https://stackoverflow.com/a/11007216/843699
        //==========================================
        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE OR REPLACE FUNCTION noaccent(text) ");
        stm.sql("RETURNS text AS ");
        stm.sql("$func$ ");
        stm.sql("SELECT public.unaccent('public.unaccent', $1) ");
        stm.sql("$func$  LANGUAGE sql IMMUTABLE; ");
        stm.update();
    }

    protected void installModificationDateFunction(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        //==========================================
        // The column in the table HAS to be called "modification_date"
        // @see https://stackoverflow.com/a/26284695/843699
        //==========================================
        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE OR REPLACE FUNCTION update_modification_date() " +
                "RETURNS TRIGGER AS $$ " +
                "BEGIN " +
                "    IF row(NEW.*) IS DISTINCT FROM row(OLD.*) THEN " +
                "        NEW.modification_date = clock_timestamp(); " +
                "        RETURN NEW; " +
                "    ELSE " +
                "        RETURN OLD; " +
                "    END IF; " +
                "END; " +
                "$$ language 'plpgsql'; ");
        stm.update();
    }

    protected void createUsersTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE users (" +
                "   id SERIAL PRIMARY KEY, " +
                "   username VARCHAR(255) UNIQUE NOT NULL, " +
                "   email VARCHAR(255) UNIQUE NOT NULL, " +
                "   hashed_password VARCHAR(512) NOT NULL, " +
                "   password_salt VARCHAR(64) NOT NULL, " +
                "   bio TEXT DEFAULT NULL, " +
                "   image VARCHAR(2048) DEFAULT NULL, " +
                "   creation_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(), " +
                "   modification_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()" +
                ")");

        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE UNIQUE INDEX ON users(LOWER(email))");
        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE UNIQUE INDEX ON users(LOWER(username))");
        stm.update();

        //==========================================
        // pg_trgm index for LIKE search
        //==========================================
        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON users USING gin (noaccent(username) gin_trgm_ops)");
        stm.update();
    }

    protected void createFollowingsTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE followings (" +
                "   source_user_id INTEGER NOT NULL REFERENCES users(id), " +
                "   target_user_id INTEGER NOT NULL REFERENCES users(id), " +
                "   creation_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp() " +
                ")");

        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE UNIQUE INDEX ON followings(source_user_id, target_user_id)");
        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON followings(target_user_id)");
        stm.update();
    }

    protected void createArticleTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE articles (" +
                "   id SERIAL PRIMARY KEY, " +
                "   slug VARCHAR(512) UNIQUE NOT NULL, " +
                "   author_id INTEGER NOT NULL REFERENCES users(id), " +
                "   title VARCHAR(255) NOT NULL, " +
                "   description TEXT NOT NULL, " +
                "   body TEXT NOT NULL, " +
                "   creation_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(), " +
                "   modification_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()" +
                ")");

        stm.update();
    }

    protected void createFavoritesTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE favorites ( " +
                "   user_id INTEGER NOT NULL REFERENCES users(id), " +
                "   article_id INTEGER NOT NULL REFERENCES articles(id), " +
                "   creation_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp() " +
                ")");

        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE UNIQUE INDEX ON favorites(user_id, article_id)");
        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON favorites(article_id)");
        stm.update();
    }

    protected void createTagsTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE tags (" +
                "   tag VARCHAR(255) NOT NULL, " +
                "   article_id INTEGER NOT NULL REFERENCES articles(id) " +
                ")");

        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE UNIQUE INDEX ON tags(tag, article_id)");
        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON tags(article_id)");
        stm.update();
    }

    protected void createCommentsTable(Connection connection) {

        UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

        stm.sql("CREATE TABLE comments (" +
                "   id SERIAL PRIMARY KEY, " +
                "   author_id INTEGER NOT NULL REFERENCES users(id), " +
                "   article_id INTEGER NOT NULL REFERENCES articles(id), " +
                "   body TEXT NOT NULL, " +
                "   creation_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(), " +
                "   modification_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()" +
                ")");

        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON comments(article_id)");
        stm.update();

        stm = getJdbcUtils().statements().createUpdateStatement(connection);
        stm.sql("CREATE INDEX ON comments(author_id)");
        stm.update();
    }
}
