package com.gmpatter.shorty.rest;

import com.gmpatter.shorty.ServerMain;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.Map;

import static io.helidon.config.ConfigSources.classpath;

/**
 * Integration tests for the service Rest API.
 */
public class RestApiIT {

  private static WebServer server;

  /**
   * Start the server before all tests.
   */
  @BeforeAll
  public static void setup() {
    Config config = Config.builder()
        .sources(classpath("application.yaml"))
        .sources(ConfigSources.create(Map.of("server.port", "9080")))
        .build();
    server = ServerMain.startServer(config);
  }

  /**
   * Shutdown the server after all tests.
   */
  @AfterAll
  public static void cleanup() {
    server.shutdown();
  }

  /**
   * GET url/{token} should return 404 when url does not
   * exist for the given token.
   */
  @Test
  public void testGetUrlNotFound() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");
    Response response = target
        .path("/url/f347dff")
        .request()
        .get();
    Assertions.assertEquals(404, response.getStatus());
  }

  /**
   * GET url/{token} should return 200 and expected
   * body content when url exists.
   */
  @Test
  public void testGetUrlSuccess() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");

    JsonObject urlBody = Json.createObjectBuilder()
        .add("url", "https://www.google.co.uk")
        .add("expiryCode", "0")
        .build();

    // first create the url
    Response postResponse = target
        .path("/url")
        .request()
        .post(Entity.json(urlBody));

    // get the token from the create response
    JsonObject postJsonResponse = postResponse.readEntity(JsonObject.class);
    String token = postJsonResponse.getString("token");

    // Now do a get to test the success
    Response getResponse = target
        .path("/url/" + token)
        .request()
        .get();
    JsonObject getJsonResponse = getResponse.readEntity(JsonObject.class);
    Assertions.assertEquals(200, getResponse.getStatus());
    Assertions.assertNotNull(getJsonResponse.getString("expiry"));
    Assertions.assertNotNull(getJsonResponse.getString("token"));
    Assertions.assertEquals(token, getJsonResponse.getString("token"));
    Assertions.assertEquals("https://www.google.co.uk", getJsonResponse.getString("url"));
  }

  /**
   * POST url should return 201 and expected body content
   * when successfully created.
   */
  @Test
  public void testCreateUrlSuccess() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");

    JsonObject urlBody = Json.createObjectBuilder()
        .add("url", "https://www.google.co.uk")
        .add("expiryCode", "0")
        .build();
    // first create the url
    Response postResponse = target
        .path("/url")
        .request()
        .post(Entity.json(urlBody));

    JsonObject getJsonResponse = postResponse.readEntity(JsonObject.class);
    Assertions.assertEquals(201, postResponse.getStatus());
    Assertions.assertNotNull(getJsonResponse.getString("expiry"));
    Assertions.assertNotNull(getJsonResponse.getString("token"));
    Assertions.assertEquals("https://www.google.co.uk", getJsonResponse.getString("url"));
  }

  /**
   * POST url should return 400 when required fields
   * are missing.
   */
  @Test
  public void testCreateUrlMissingFields() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");

    JsonObject missingUrlBody = Json.createObjectBuilder()
        .add("url", "")
        .add("expiryCode", "0")
        .build();
    // try creating a url with a missing url field
    Response postResponse = target
        .path("/url")
        .request()
        .post(Entity.json(missingUrlBody));

    // Expect 400 error for missing field
    Assertions.assertEquals(400, postResponse.getStatus());

    JsonObject missingExpiryBody = Json.createObjectBuilder()
        .add("url", "https://www.google.co.uk")
        .add("expiryCode", "")
        .build();
    // first create the url
    postResponse = target
        .path("/url")
        .request()
        .post(Entity.json(missingExpiryBody));

    // Expect 400 error for missing field
    Assertions.assertEquals(400, postResponse.getStatus());
  }

  /**
   * POST url should return 400 when required fields
   * are missing.
   */
  @Test
  public void testDeleteUrlNotFound() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");
    Response response = target
        .path("/url/f347dff")
        .request()
        .delete();
    Assertions.assertEquals(404, response.getStatus());
  }

  /**
   * Delete url/{token} should return 204 and delete
   * the token when url exists for given token.
   */
  @Test
  public void testDeleteUrlSuccess() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:9080/shorty/v1");

    JsonObject urlBody = Json.createObjectBuilder()
        .add("url", "https://www.google.co.uk")
        .add("expiryCode", "0")
        .build();

    // first create the url
    Response postResponse = target
        .path("/url")
        .request()
        .post(Entity.json(urlBody));

    // get the token from the create response
    JsonObject postJsonResponse = postResponse.readEntity(JsonObject.class);
    String token = postJsonResponse.getString("token");

    // Delete the token
    Response deleteResponse = target
        .path("/url/" + token)
        .request()
        .delete();

    // Delete response should be 204
    Assertions.assertEquals(204, deleteResponse.getStatus());

    // Now do a get on the token again
    Response getResponse = target
        .path("/url/" + token)
        .request()
        .get();

    // Expect 404 for now deleted url
    Assertions.assertEquals(404, getResponse.getStatus());
  }


}
