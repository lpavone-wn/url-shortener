package com.gmpatter.shorty.service;

import com.gmpatter.shorty.model.ShortUrl;
import com.gmpatter.shorty.persistence.UrlRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link UrlService} class.
 */
public class UrlServiceTest extends Mockito {

  /**
   * Test generateToken() returns a token with the expected
   * length
   */
  @Test
  public void testGenerateTokenReturnsToken() {
    UrlService urlService = UrlService.getInstance();
    String token = urlService.generateToken();
    Assertions.assertEquals(7, token.length());
  }

  /**
   * Test generateUniqueToken() generates a unique token.
   */
  @Test
  public void testGenerateUniqueTokenAvoidsConflicts() {
    UrlService urlService = spy(UrlService.getInstance());
    UrlRepository urlRepository = mock(UrlRepository.class);
    ShortUrl shortUrl = mock(ShortUrl.class);

    // mock an existing url in the repository
    String existingToken = "f3eg342";
    String newToken = "GE535Gx";
    when(urlService.generateToken()).thenReturn(existingToken, newToken);
    when(urlService.getUrlRepository()).thenReturn(urlRepository);
    when(urlRepository.getUrl(existingToken)).thenReturn(shortUrl);

    String uniqueToken = urlService.generateUniqueToken();
    Assertions.assertEquals(newToken, uniqueToken);
  }
}
