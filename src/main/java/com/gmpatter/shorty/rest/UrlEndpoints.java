package com.gmpatter.shorty.rest;

import com.gmpatter.shorty.model.ShortUrl;
import com.gmpatter.shorty.service.UrlService;
import io.helidon.common.http.Http;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Url resource endpoint class, containing endpoints for operations
 * against the URL resource.
 *
 * @author Gareth Patterson
 */
@Path("url")
public class UrlEndpoints {

  public static final String ERROR_MESSAGE_FIELD = "message";
  public static final String URL_FIELD = "url";
  public static final String EXPIRY_CODE_FIELD = "expiryCode";
  public static final String EXPIRY_FIELD = "expiry";
  public static final String SHORT_URL_FIELD = "shortUrl";
  public static final String TOKEN_FIELD = "token";
  public static final String REDIRECT_CONTEXT_ROOT = "/go";

  /**
   * Returns the <code>UrlService</code> used to create,
   * fetch and delete short urls.
   *
   * @return the url service.
   */
  protected UrlService getUrlService() {
    return UrlService.getInstance();
  }

  /**
   * Get a short url for the given token.
   * 
   * @param token the token for the short url
   * @param context the current request context
   * @return the response object.
   */
  @GET
  @Path("/{token}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUrl(@PathParam("token") String token,
                         @Context ContainerRequestContext context) {
    if (token == null || token.isEmpty()) {
      return buildErrorResponse(Http.Status.BAD_REQUEST_400.code(),
          "The \"" + token + "\" path param is missing or empty");
    }
    ShortUrl shortUrl = getUrlService().getUrl(token);
    if (shortUrl == null) {
      return buildErrorResponse(Http.Status.NOT_FOUND_404.code(),
          "Url not found for token " + token + ".");
    }
    JsonObject response = buildShortUrlJson(context, shortUrl);
    return Response.ok(response.toString()).build();
  }

  /**
   * Creates a new short url for the given input.
   * 
   * @param input the json request body
   * @param context the current container request context
   * @return the response object.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createShortUrl(JsonObject input,
                                 @Context ContainerRequestContext context) {
    String url = null;
    String expiryCode = null;
    if (input.containsKey(URL_FIELD)) {
      url = input.getJsonString(URL_FIELD).getString();
    }
    if (input.containsKey(EXPIRY_CODE_FIELD)) {
      expiryCode = input.getJsonString(EXPIRY_CODE_FIELD).getString();
    }

    // Return 400 response if url or expiry code are not set
    if (url == null || url.isEmpty()) {
      return buildErrorResponse(Http.Status.BAD_REQUEST_400.code(), 
          "The \"" + URL_FIELD + "\" field is missing or empty");
    }
    if (expiryCode == null || expiryCode.isEmpty()) {
      return buildErrorResponse(Http.Status.BAD_REQUEST_400.code(),
          "The \"" + EXPIRY_CODE_FIELD + "\" field is missing or empty");
    }

    // Get the short url from the url service
    ShortUrl shortUrl = getUrlService().createUrl(url, Integer.parseInt(expiryCode));
    
    // Build and return the json response
    JsonObject response = buildShortUrlJson(context, shortUrl);
    return Response.status(Http.Status.CREATED_201.code())
        .entity(response.toString())
        .build();
  }

  /**
   * Deletes the short url for the given token.
   *
   * @param token the token to delete by
   * @return the response object.
   */
  @DELETE
  @Path("{token}")
  public Response deleteShortUrl(@PathParam("token") String token) {
    // Return a 404 if the url is not found by the url service
    if (getUrlService().getUrl(token) == null) {
      return buildErrorResponse(Http.Status.NOT_FOUND_404.code(), "Url not found.");
    }
    // Delete the url and return 204 no content
    getUrlService().deleteUrl(token);
    return Response.noContent().build();
  }

  /**
   * Builds and returns a <code>JsonObject</code> representing the passed
   * <code>ShortUrl</code>.
   * @param context the current request context
   * @param shortUrl the short url
   * @return the json object representation of the short url.
   */
  private JsonObject buildShortUrlJson(ContainerRequestContext context, ShortUrl shortUrl) {
    JsonObject response = Json.createObjectBuilder()
        .add(SHORT_URL_FIELD, getBaseUrl(context) + REDIRECT_CONTEXT_ROOT + "/" + shortUrl.getToken())
        .add(URL_FIELD, shortUrl.getOriginalUrl())
        .add(EXPIRY_FIELD, shortUrl.getExpiry())
        .add(TOKEN_FIELD, shortUrl.getToken())
        .build();
    return response;
  }

  /**
   * Builds a {@link Response} json error response for the 
   * given error code and message.
   * 
   * @param errorCode the error code for the response
   * @param message the message for the response
   * @return the error response.
   */
  private Response buildErrorResponse(int errorCode, String message) {
    JsonObject errorJson = Json.createObjectBuilder()
        .add(ERROR_MESSAGE_FIELD, message)
        .build();
    return Response.status(errorCode)
        .entity(errorJson)
        .build();
  }

  /**
   * Returns the base url for requests to this application for the 
   * given {@link ContainerRequestContext}.
   * 
   * @param requestContext the current request context.
   * @return the base url string.
   */
  private String getBaseUrl(ContainerRequestContext requestContext) {
    return UriBuilder.fromUri(requestContext.getUriInfo().getBaseUri()).replacePath("").build().toString();
  }
}
