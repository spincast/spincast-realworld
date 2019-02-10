package org.spincast.realworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spincast.core.json.JsonObject;
import org.spincast.plugins.httpclient.HttpResponse;
import org.spincast.realworld.models.articles.Article;
import org.spincast.realworld.utils.TestBase;
import org.spincast.shaded.org.apache.http.HttpStatus;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;

/**
 * Tests in this file are meant to all be ran together
 * and in order since they may depend on each other!
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArticlesTest extends TestBase {

    String jwt = null;

    @Test
    public void t01_createArticleNotLoggedIn() {

        String expectedSlug = "le-buf-et-lelephant";

        Article article = getArticleService().getArticle(expectedSlug, null);
        assertNull(article);

        JsonObject articleToCreate = getSpincastJsonManager().create();
        articleToCreate.set("article.title", "Le Bœuf et l'éléphant");
        articleToCreate.set("article.description", "This is the description");
        articleToCreate.set("article.body", "This is the body");
        articleToCreate.set("article.tagList", Sets.newHashSet("tag1", "tag2"));

        HttpResponse response = POST("/api/articles").setJsonStringBody(articleToCreate).send();
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void t02_register() {

        JsonObject registrationData = getSpincastJsonManager().create();
        registrationData.set("user.email", "titi@example.com");
        registrationData.set("user.password", "12345678");
        registrationData.set("user.username", "titi");

        HttpResponse response = POST("/api/users").setJsonStringBody(registrationData).send();
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        JsonObject createdUser = response.getContentAsJsonObject();
        this.jwt = createdUser.getString("user.token");
        assertNotNull(this.jwt);
    }

    @Test
    public void t03_createArticleLoggedIn() {

        String expectedSlug = "le-buf-et-lelephant";

        Article article = getArticleService().getArticle(expectedSlug, null);
        assertNull(article);

        JsonObject articleToCreate = getSpincastJsonManager().create();
        articleToCreate.set("article.title", "Le Bœuf et l'éléphant");
        articleToCreate.set("article.description", "This is the description");
        articleToCreate.set("article.body", "This is the body");
        articleToCreate.set("article.tagList", Sets.newHashSet("tag1", "tag2"));

        HttpResponse response = POST("/api/articles").setJsonStringBody(articleToCreate)
                                                     .addHeaderValue(HttpHeaders.AUTHORIZATION, "Token " + this.jwt)
                                                     .send();
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        article = getArticleService().getArticle(expectedSlug, null);
        assertNotNull(article);
        assertNotNull(article.getId());
        assertEquals(expectedSlug, article.getSlug());
        assertEquals(2, article.getTagList().size());
    }

    @Test
    public void t04_articleCantHaveFeedAsItsSlug() {

        JsonObject articleToCreate = getSpincastJsonManager().create();
        articleToCreate.set("article.title", "feed");
        articleToCreate.set("article.description", "This is the description");
        articleToCreate.set("article.body", "This is the body");
        articleToCreate.set("article.tagList", Sets.newHashSet("tag1", "tag2"));

        HttpResponse response = POST("/api/articles").setJsonStringBody(articleToCreate)
                                                     .addHeaderValue(HttpHeaders.AUTHORIZATION, "Token " + this.jwt)
                                                     .send();
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        JsonObject article = response.getContentAsJsonObject();
        assertEquals("feed", article.getString("article.title"));
        assertNotEquals("feed", article.getString("article.slug"));
    }
}
