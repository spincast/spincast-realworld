package org.spincast.realworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spincast.core.json.JsonObject;
import org.spincast.plugins.httpclient.HttpResponse;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.utils.TestBase;
import org.spincast.shaded.org.apache.http.HttpStatus;

/**
 * Tests in this file are meant to all be ran together
 * and in order since they may depend on each other!
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsersTest extends TestBase {

    @Test
    public void t01_register() {

        User user = getUserService().getUserByUsername("titi");
        assertNull(user);

        JsonObject registrationData = getSpincastJsonManager().create();
        registrationData.set("user.email", "titi@example.com");
        registrationData.set("user.password", "12345678");
        registrationData.set("user.username", "titi");

        HttpResponse response = POST("/api/users").setJsonStringBody(registrationData).send();
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        JsonObject createdUser = response.getContentAsJsonObject();
        assertEquals("titi", createdUser.getString("user.username"));

        user = getUserService().getUserByUsername("titi");
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("titi", user.getUsername());
    }

    @Test
    public void t02_usernameAlreadyTaken() {

        JsonObject registrationData = getSpincastJsonManager().create();
        registrationData.set("user.email", "titi2@example.com");
        registrationData.set("user.password", "12345678");
        registrationData.set("user.username", "TiTi");

        HttpResponse response = POST("/api/users").setJsonStringBody(registrationData).send();
        assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, response.getStatus());
    }

    @Test
    public void t03_emailAlreadyTaken() {

        JsonObject registrationData = getSpincastJsonManager().create();
        registrationData.set("user.email", "Titi@ExamPLE.com");
        registrationData.set("user.password", "12345678");
        registrationData.set("user.username", "titi2");

        HttpResponse response = POST("/api/users").setJsonStringBody(registrationData).send();
        assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, response.getStatus());
    }

    @Test
    public void t04_anotherUser() {

        JsonObject registrationData = getSpincastJsonManager().create();
        registrationData.set("user.email", "titi2@example.com");
        registrationData.set("user.password", "12345678");
        registrationData.set("user.username", "titi2");

        HttpResponse response = POST("/api/users").setJsonStringBody(registrationData).send();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }
}
