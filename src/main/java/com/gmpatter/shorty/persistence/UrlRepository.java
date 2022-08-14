package com.gmpatter.shorty.persistence;

import com.gmpatter.shorty.model.ShortUrl;

public interface UrlRepository {

  /**
   * Returns the short url for the given token.
   *
   * @param token the token for the short url
   * @return the short url.
   */
  ShortUrl getUrl(String token);

  /**
   * Creates a new short url in the repository.
   *
   * @param shortUrl the short url to create
   * @return the newly created short url.
   */
  ShortUrl createUrl(ShortUrl shortUrl);

  /**
   * Delete the short url for the given token.
   *
   * @param token the token for the short url
   */
  void deleteUrl(String token);

  /**
   * Find and delete any expired short urls in the repository.
   */
  void purgeExpiredUrls();
}
