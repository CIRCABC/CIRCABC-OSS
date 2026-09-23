package eu.europa.ec.digit.circabc.rest.service.profile.permissions;

import static org.junit.Assert.*;

import org.junit.Test;

public class CircabcServicesTest {

  @Test
  public void testValues_returnsAllEnumConstants() {
    CircabcServices[] values = CircabcServices.values();
    assertEquals(12, values.length);
  }

  @Test
  public void testValueOf_withValidName_returnsConstant() {
    assertEquals(CircabcServices.CIRCABC, CircabcServices.valueOf("CIRCABC"));
    assertEquals(
      CircabcServices.CATEGORY_HEADER,
      CircabcServices.valueOf("CATEGORY_HEADER")
    );
    assertEquals(CircabcServices.CATEGORY, CircabcServices.valueOf("CATEGORY"));
    assertEquals(
      CircabcServices.INTEREST_GROUP,
      CircabcServices.valueOf("INTEREST_GROUP")
    );
    assertEquals(CircabcServices.LIBRARY, CircabcServices.valueOf("LIBRARY"));
    assertEquals(
      CircabcServices.DIRECTORY,
      CircabcServices.valueOf("DIRECTORY")
    );
    assertEquals(
      CircabcServices.APPLICANT,
      CircabcServices.valueOf("APPLICANT")
    );
    assertEquals(
      CircabcServices.VISIBILITY,
      CircabcServices.valueOf("VISIBILITY")
    );
    assertEquals(
      CircabcServices.NEWSGROUP,
      CircabcServices.valueOf("NEWSGROUP")
    );
    assertEquals(CircabcServices.SURVEY, CircabcServices.valueOf("SURVEY"));
    assertEquals(CircabcServices.EVENT, CircabcServices.valueOf("EVENT"));
    assertEquals(
      CircabcServices.INFORMATION,
      CircabcServices.valueOf("INFORMATION")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_withInvalidName_throwsException() {
    CircabcServices.valueOf("INVALID");
  }

  @Test
  public void testName_returnsCorrectString() {
    assertEquals("LIBRARY", CircabcServices.LIBRARY.name());
    assertEquals("INTEREST_GROUP", CircabcServices.INTEREST_GROUP.name());
  }

  @Test
  public void testOrdinal_maintainsOrder() {
    assertEquals(0, CircabcServices.CIRCABC.ordinal());
    assertEquals(1, CircabcServices.CATEGORY_HEADER.ordinal());
    assertEquals(2, CircabcServices.CATEGORY.ordinal());
    assertEquals(3, CircabcServices.INTEREST_GROUP.ordinal());
  }
}
