package eu.europa.ec.digit.circabc.rest.service.keyword;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeywordEntryTest {

  @Test
  public void testDefaultConstructor_thenFieldsAreNull() {
    KeywordEntry entry = new KeywordEntry();
    assertNull(entry.getLanguage());
    assertNull(entry.getValue());
  }

  @Test
  public void testParameterizedConstructor_thenFieldsAreSet() {
    KeywordEntry entry = new KeywordEntry("en", "keyword1");
    assertEquals("en", entry.getLanguage());
    assertEquals("keyword1", entry.getValue());
  }

  @Test
  public void testSetLanguage() {
    KeywordEntry entry = new KeywordEntry();
    entry.setLanguage("fr");
    assertEquals("fr", entry.getLanguage());
  }

  @Test
  public void testSetValue() {
    KeywordEntry entry = new KeywordEntry();
    entry.setValue("test");
    assertEquals("test", entry.getValue());
  }

  @Test
  public void testToString_whenFieldsSet_thenReturnsLanguageColonValue() {
    KeywordEntry entry = new KeywordEntry("de", "schlüsselwort");
    assertEquals("de:schlüsselwort", entry.toString());
  }

  @Test
  public void testToString_whenFieldsNull_thenReturnsNullColonNull() {
    KeywordEntry entry = new KeywordEntry();
    assertEquals("null:null", entry.toString());
  }
}
