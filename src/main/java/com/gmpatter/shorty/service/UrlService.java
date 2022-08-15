package com.gmpatter.shorty.service;

import com.gmpatter.shorty.model.ShortUrl;
import com.gmpatter.shorty.persistence.MapUrlRepository;
import com.gmpatter.shorty.persistence.UrlRepository;
import io.helidon.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.helidon.config.ConfigSources.classpath;

/**
 * The controller class for managing URLs.
 *
 * <p>Creates, fetches and deletes <code>ShortUrl</code> in the
 * {@link UrlRepository}.
 *
 * <p>Implements the logic for generating unique tokens and setting
 * expiry dates.
 *
 * @author Gareth Patterson
 */
public class UrlService {

  protected static final Logger LOGGER = LoggerFactory.getLogger(UrlService.class);

  // All the available characters for generating tokens
  private char[] tokenCharacters;
  private int tokenLength;

  private static UrlService INSTANCE;

  private UrlService() {
    // Get any configuration for token generation
    Config config = Config.builder()
        .sources(classpath("application.yaml"))
        .build();
    Config tokenConfig = config.get("shorty.token");
    tokenLength = tokenConfig.get("token-length").asInt().orElse(7);
    tokenCharacters = tokenConfig.get("token-characters").toString().toCharArray();

    // Start a scheduler that will periodically cleanup expired urls
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> expiredUrlCleanup(), 1, 1, TimeUnit.MINUTES);
  }

  /**
   * Cleans up any expired urls in the <code>UrlRepository</code>.
   */
  protected void expiredUrlCleanup() {
    LOGGER.debug("Running scheduled cleanup of expired urls.");
    getUrlRepository().purgeExpiredUrls();
  }

  /**
   * Returns the singleton instance of <code>UrlService</code>.
   *
   * @return the instance of <code>UrlService</code>.
   */
  public static UrlService getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new UrlService();
    }
    return INSTANCE;
  }

  /**
   * Returns the short url for the given token.
   *
   * @param token the token for the short url.
   * @return the short url.
   */
  public ShortUrl getUrl(String token) {
    ShortUrl url = getUrlRepository().getUrl(token);
    if (url != null) {
      LOGGER.debug("Returning url for token {}.", token);
    } else {
      LOGGER.debug("Url not found for token {}.", token);
    }
    return url;
  }

  /**
   * Creates a short url in the <code>UrlRepository</code>, generating a
   * unique token and calculating the expiry date.
   *
   * @param url the original url
   * @param expiryCode the expiry code
   * @return the newly created short url.
   */
  public ShortUrl createUrl(String url, int expiryCode) {
    String token = generateUniqueToken();
    String expiry = getExpiry(expiryCode);
    LOGGER.debug("Creating url with token {}.", token);
    return getUrlRepository().createUrl(new ShortUrl.Builder()
        .token(token)
        .originalUrl(url)
        .expiry(expiry)
        .build());
  }

  /**
   * Calculates and returns a date string representing the expiry
   * date of a url, for the given expiry code.
   *
   * <p>Expiry code can be 0,1,2, or 3.
   *
   * @param expiryCode the expiry code
   * @return the string expiry date.
   * @throws IllegalArgumentException if expiry code is invalid
   */
  protected String getExpiry(int expiryCode) {
    LocalDateTime expiry = null;

    // Throw exception if the expiry code is not in the expected range
    if (expiryCode < 0 || expiryCode > 3) {
      throw new IllegalArgumentException("Expiry code must be between 0, 1, 2 or 3.");
    }

    switch (expiryCode) {
      // 0 = 1 minute
      case 0:
        expiry = LocalDateTime.now().plusMinutes(1);
        break;
      // 1 = 1 hour
      case 1:
        expiry = LocalDateTime.now().plusHours(1);
        break;
      // 2 = 1 day
      case 2:
        expiry = LocalDateTime.now().plusDays(1);
        break;
      // 3 = 1 year
      case 3:
        expiry = LocalDateTime.now().plusYears(1);
        break;
    }
    return expiry.toString();
  }

  /**
   * Deletes the short url for the given token.
   *
   * @param token the token for the short url
   */
  public void deleteUrl(String token) {
    LOGGER.debug("Deleting url for token {}.", token);
    getUrlRepository().deleteUrl(token);
  }

  /**
   * A new unique token for a short url.
   *
   * @return the unique token.
   */
  protected String generateUniqueToken() {
    boolean unique = false;
    String token = null;
    // Avoid conflicts in the repository by continuing until
    // we generate a new unique token
    while (!unique) {
      token = generateToken();
      unique = getUrlRepository().getUrl(token) == null;
    }
    return token;
  }

  /**
   * Generates and returns a new 7-digit token.
   *
   * @return the new token.
   */
  protected String generateToken() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<tokenLength; i++) {
      // Add random numbers from the token character array
      int random = new Random().nextInt(tokenCharacters.length);
      sb.append(tokenCharacters[random]);
    }
    return sb.toString();
  }

  /**
   * Returns the <code>UrlRepository</code>.
   *
   * @return the url repository.
   */
  protected UrlRepository getUrlRepository() {
    return MapUrlRepository.getInstance();
  }
}
