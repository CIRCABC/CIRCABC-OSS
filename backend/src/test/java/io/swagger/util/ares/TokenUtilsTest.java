package io.swagger.util.ares;

import static org.junit.Assert.*;

import org.junit.Test;

public class TokenUtilsTest {

  @Test
  public void testGenerateToken_whenValidInputs_thenReturnsHexString() {
    String token = TokenUtils.generateToken(
      "myApiKey",
      "mySecret",
      "2026-05-04",
      "GET",
      "/api/resource"
    );

    assertNotNull(token);
    assertTrue(token.matches("[0-9a-f]+"));
    assertEquals(64, token.length());
  }

  @Test
  public void testGenerateToken_whenSameInputs_thenReturnsSameToken() {
    String token1 = TokenUtils.generateToken(
      "key",
      "secret",
      "date",
      "POST",
      "/url"
    );
    String token2 = TokenUtils.generateToken(
      "key",
      "secret",
      "date",
      "POST",
      "/url"
    );

    assertEquals(token1, token2);
  }

  @Test
  public void testGenerateToken_whenDifferentInputs_thenReturnsDifferentTokens() {
    String token1 = TokenUtils.generateToken(
      "key1",
      "secret",
      "date",
      "GET",
      "/url"
    );
    String token2 = TokenUtils.generateToken(
      "key2",
      "secret",
      "date",
      "GET",
      "/url"
    );

    assertNotEquals(token1, token2);
  }

  @Test
  public void testValidateToken_whenValidToken_thenReturnsTrue() {
    String token = TokenUtils.generateToken(
      "apiKey",
      "secret",
      "2026-05-04",
      "GET",
      "/api/test"
    );

    assertTrue(
      TokenUtils.validateToken(
        token,
        "apiKey",
        "secret",
        "2026-05-04",
        "GET",
        "/api/test"
      )
    );
  }

  @Test
  public void testValidateToken_whenWrongToken_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken(
        "wrongtoken",
        "apiKey",
        "secret",
        "2026-05-04",
        "GET",
        "/api/test"
      )
    );
  }

  @Test
  public void testValidateToken_whenBlankToken_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken("", "apiKey", "secret", "date", "GET", "/url")
    );
  }

  @Test
  public void testValidateToken_whenNullApiKey_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken("token", null, "secret", "date", "GET", "/url")
    );
  }

  @Test
  public void testValidateToken_whenBlankDate_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken("token", "apiKey", "secret", " ", "GET", "/url")
    );
  }

  @Test
  public void testValidateToken_whenBlankHttpMethod_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken("token", "apiKey", "secret", "date", "", "/url")
    );
  }

  @Test
  public void testValidateToken_whenBlankUri_thenReturnsFalse() {
    assertFalse(
      TokenUtils.validateToken("token", "apiKey", "secret", "date", "GET", null)
    );
  }
}
