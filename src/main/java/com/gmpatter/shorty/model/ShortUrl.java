package com.gmpatter.shorty.model;

/**
 * Model class for representing short urls.
 *
 * <p>Instances are created via a <code>Builder</code>.
 *
 * @author Gareth Patterson
 */
public class ShortUrl {

  private String token;
  private String originalUrl;
  private String expiry;

  private ShortUrl(Builder builder) {
    this.token = builder.token;
    this.originalUrl = builder.originalUrl;
    this.expiry = builder.expiry;
  }

  /**
   * Returns the token that uniquely identifies a short url.
   *
   * @return the short url token.
   */
  public String getToken() {
    return token;
  }

  /**
   * Returns the original url for the short url.
   *
   * @return the original url.
   */
  public String getOriginalUrl() {
    return originalUrl;
  }

  /**
   * Returns the expiry for the short url.
   *
   * @return the expiry.
   */
  public String getExpiry() {
    return expiry;
  }

  /**
   * Builder class for building instances of {@link ShortUrl}.
   */
  public static class Builder {
    private String token;
    private String originalUrl;
    private String expiry;

    /**
     * Build a new instance of <code>ShortUrl</code>.
     *
     * @return the new short url instance.
     */
    public ShortUrl build() {
      return new ShortUrl(this);
    }

    /**
     * Set the token that uniquely identifies a short url.
     *
     * @param token the token for the short url
     * @return the current builder.
     */
    public Builder token(String token) {
      this.token = token;
      return this;
    }

    /**
     * Set the original url for the short url.
     *
     * @param originalUrl the original url for the short url
     * @return the current builder.
     */
    public Builder originalUrl(String originalUrl) {
      this.originalUrl = originalUrl;
      return this;
    }

    /**
     * Set the expiry date for the short url.
     *
     * @param expiry the expiry date for the short url
     * @return the current builder.
     */
    public Builder expiry(String expiry) {
      this.expiry = expiry;
      return this;
    }
  }
}
