package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.Test;

public class RestConstantsTest {

  @Test
  public void testErrorOccurredConstant() {
    assertEquals("Error occurred", RestConstants.ERROR_OCCURRED);
  }

  @Test
  public void testClassIsFinal() {
    assertTrue(Modifier.isFinal(RestConstants.class.getModifiers()));
  }

  @Test
  public void testConstructorIsPrivate() throws Exception {
    Constructor<RestConstants> constructor =
      RestConstants.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    // Verify it can be invoked via reflection (coverage of private constructor)
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }
}
