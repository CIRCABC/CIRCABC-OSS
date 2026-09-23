package io.swagger.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class EmailUtilTest {

  @Test
  public void testIsValidEmailAddress_whenValidEmail_thenReturnsTrue() {
    assertTrue(EmailUtil.isValidEmailAddress("user@example.com"));
    assertTrue(EmailUtil.isValidEmailAddress("first.last@domain.org"));
    assertTrue(EmailUtil.isValidEmailAddress("user+tag@sub.domain.co"));
  }

  @Test
  public void testIsValidEmailAddress_whenNullOrEmpty_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress(null));
    assertFalse(EmailUtil.isValidEmailAddress(""));
  }

  @Test
  public void testIsValidEmailAddress_whenContainsWhitespace_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("user @example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user\t@example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user\n@example.com"));
  }

  @Test
  public void testIsValidEmailAddress_whenMissingAtSign_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("userexample.com"));
  }

  @Test
  public void testIsValidEmailAddress_whenMultipleAtSigns_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("user@@example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user@sub@example.com"));
  }

  @Test
  public void testIsValidEmailAddress_whenConsecutiveDots_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("user..name@example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user@example..com"));
  }

  @Test
  public void testIsValidEmailAddress_whenLocalPartStartsOrEndsWithDot_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress(".user@example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user.@example.com"));
  }

  @Test
  public void testIsValidEmailAddress_whenDomainMissingDot_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("user@localhost"));
  }

  @Test
  public void testIsValidEmailAddress_whenDomainStartsOrEndsWithHyphen_thenReturnsFalse() {
    assertFalse(EmailUtil.isValidEmailAddress("user@-example.com"));
    assertFalse(EmailUtil.isValidEmailAddress("user@example-.com"));
  }

  @Test
  public void testIsValidEmailAddress_whenExceedsLocalPartLength_thenReturnsFalse() {
    String longLocal = "a".repeat(65) + "@example.com";
    assertFalse(EmailUtil.isValidEmailAddress(longLocal));
  }

  @Test
  public void testIsValidEmailAddress_whenExceedsTotalLength_thenReturnsFalse() {
    String longDomain =
      "a".repeat(64) +
      "@" +
      "b".repeat(63) +
      "." +
      "c".repeat(63) +
      "." +
      "d".repeat(63) +
      ".com";
    // total > 254
    assertFalse(EmailUtil.isValidEmailAddress(longDomain));
  }

  @Test
  public void testSanitizeEmailAddresses_whenNull_thenReturnsNull() {
    assertNull(EmailUtil.sanitizeEmailAddresses(null));
  }

  @Test
  public void testSanitizeEmailAddresses_whenMultipleValidEmails_thenReturnsCommaSeparated() {
    String result = EmailUtil.sanitizeEmailAddresses(
      "a@b.com; c@d.org, e@f.net"
    );
    assertEquals("a@b.com, c@d.org, e@f.net", result);
  }

  @Test
  public void testSanitizeEmailAddresses_whenMixedValidAndInvalid_thenReturnsOnlyValid() {
    String result = EmailUtil.sanitizeEmailAddresses(
      "good@example.com; bad@@invalid, ok@test.org"
    );
    assertEquals("good@example.com, ok@test.org", result);
  }

  @Test
  public void testSanitizeEmailAddresses_whenAllInvalid_thenReturnsEmpty() {
    String result = EmailUtil.sanitizeEmailAddresses("bad; also-bad; @@nope");
    assertEquals("", result);
  }

  @Test
  public void testSanitizeEmailAddresses_whenEmptyString_thenReturnsEmpty() {
    assertEquals("", EmailUtil.sanitizeEmailAddresses(""));
  }
}
