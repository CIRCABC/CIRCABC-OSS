package io.swagger.util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

public class SupportedLanguagesTest {

  @Test
  public void testAvailableLangCodes_containsEnglish() {
    assertTrue(SupportedLanguages.availableLangCodes.contains("en"));
  }

  @Test
  public void testAvailableLangCodes_containsExpectedSize() {
    assertEquals(24, SupportedLanguages.availableLangCodes.size());
  }

  @Test
  public void testAvailableLangCodes_doesNotContainUnsupported() {
    assertFalse(SupportedLanguages.availableLangCodes.contains("zh"));
  }

  @Test
  public void testAvailableLangCodes_isImmutable() {
    try {
      SupportedLanguages.availableLangCodes.add("xx");
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test(expected = InvocationTargetException.class)
  public void testConstructor_throwsIllegalStateException() throws Exception {
    Constructor<SupportedLanguages> constructor =
      SupportedLanguages.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
