package com.gmpatter.shorty;

import com.gmpatter.shorty.model.ShortUrl;
import com.gmpatter.shorty.rest.ShortyV1Application;
import com.gmpatter.shorty.service.UrlService;
import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jersey.JerseySupport;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import static io.helidon.config.ConfigSources.classpath;

/**
 * Main class that creates, configures, and starts the Helidon server.
 *
 * @author Gareth Patterson
 */
public class ServerMain {

  public static void main(String[] args) {
    // Initialize Helidon config, including classpath application.yaml
    // containing our default configuration
    Config config = Config.builder()
        .sources(classpath("application.yaml"))
        .build();
    startServer(config);
  }

  /**
   * Start and return the Helidon web server.
   *
   * @return the web server instance.
   */
  public static WebServer startServer(Config config) {
    // Build and start the Helidon server registering our JAXRS application
    WebServer webServer = WebServer.builder(
            Routing.builder()
                // Register our static content routing to render the UI
                .register("/", StaticContentSupport.builder("/web/static")
                    .welcomeFileName("layout/index.html")
                    .build())
                // Redirect request handling, redirect requests to
                // "/go/{token}" to the original url for the token
                .get("/go/{token}", (req, res) -> {
                  String token = req.path().param("token");
                  ShortUrl url = UrlService.getInstance().getUrl(token);
                  // If url isn't found then return a 404
                  if (url == null) {
                    res.status(Http.Status.NOT_FOUND_404);
                    res.send();
                  } else {
                    // Return a redirect to the original URL
                    // retrieved from the url service
                    res.status(Http.Status.MOVED_PERMANENTLY_301);
                    res.addHeader(Http.Header.LOCATION, url.getOriginalUrl());
                    res.send();
                  }
                })
                // Registry or shorty jaxrs application with the web server
                .register("/shorty/v1", JerseySupport.create(new ShortyV1Application()))
                // Register OpenApi support
                .register(OpenAPISupport.create(config))
                .build())
        // Pass any "server" configuration (e.g. port) to the web server builder
        .config(config.get("server"))
        .build();
    webServer.start();
    return webServer;
  }
}
