package com.gmpatter.shorty.persistence;

import com.gmpatter.shorty.model.ShortUrl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An implementation of {@link UrlRepository} that stores <code>ShortUrl</code>
 * instances in-memory in a {@link ConcurrentHashMap}.
 *
 * @author Gareth Patterson
 */
public class MapUrlRepository implements UrlRepository {

  private static MapUrlRepository INSTANCE;

  // The concurrent hash map for storing the data
  Map<String, ShortUrl> data = new ConcurrentHashMap<>();

  private MapUrlRepository() {
  }

  /**
   * Returns the singleton instance of <code>MapUrlRepository</code>.
   *
   * @return the instance of <code>MapUrlRepository</code>.
   */
  public static MapUrlRepository getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new MapUrlRepository();
    }
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShortUrl getUrl(String token) {
    return data.get(token);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShortUrl createUrl(ShortUrl shortUrl) {
    data.putIfAbsent(shortUrl.getToken(), shortUrl);
    return shortUrl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteUrl(String id) {
    data.remove(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void purgeExpiredUrls() {
    List<Map.Entry<String, ShortUrl>> expiredList = data.entrySet()
        .stream()
        .filter(e -> isExpired(e.getValue()))
        .collect(Collectors.toList());
    expiredList.forEach(expired -> deleteUrl(expired.getKey()));
  }

  /**
   * Returns true if the short url is expired.
   *
   * @param shortUrl the short url to check
   * @return true if the short url is expired.
   */
  protected boolean isExpired(ShortUrl shortUrl) {
    LocalDateTime expiry = LocalDateTime.parse(shortUrl.getExpiry());
    return expiry.isBefore(LocalDateTime.now());
  }

}
