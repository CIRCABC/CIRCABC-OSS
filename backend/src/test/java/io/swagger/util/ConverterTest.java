package io.swagger.util;

import static org.junit.Assert.*;

import io.swagger.model.I18nProperty;
import java.text.ParseException;
import java.util.*;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

public class ConverterTest {

  @Before
  public void setUp() {
    I18NUtil.setLocale(Locale.ENGLISH);
  }

  // --- toI18NProperty(String) ---

  @Test
  public void testToI18NProperty_whenStringProvided_thenReturnsPropertyWithLocale() {
    I18nProperty result = Converter.toI18NProperty("hello");
    assertEquals("hello", result.get("en"));
  }

  // --- toI18NProperty(MLText) ---

  @Test
  public void testToI18NProperty_whenMLTextProvided_thenConvertsAllEntries() {
    MLText mlText = new MLText();
    mlText.addValue(Locale.ENGLISH, "English");
    mlText.addValue(Locale.FRENCH, "Français");

    I18nProperty result = Converter.toI18NProperty(mlText);

    assertEquals("English", result.get("en"));
    assertEquals("Français", result.get("fr"));
  }

  @Test
  public void testToI18NProperty_whenMLTextHasNullValue_thenReturnsEmptyString() {
    MLText mlText = new MLText();
    mlText.addValue(Locale.ENGLISH, null);

    I18nProperty result = Converter.toI18NProperty(mlText);

    assertEquals("", result.get("en"));
  }

  // --- convertDateToString ---

  @Test
  public void testConvertDateToString_whenDateProvided_thenFormatsCorrectly() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.set(2024, Calendar.MARCH, 15, 10, 30, 0);
    Date date = cal.getTime();

    String result = Converter.convertDateToString(date);

    assertEquals("2024-03-15T10:30Z", result);
  }

  // --- convertDateWithoutTimeToString ---

  @Test
  public void testConvertDateWithoutTimeToString_whenDateProvided_thenFormatsDateOnly() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.set(2024, Calendar.MARCH, 15, 10, 30, 0);
    Date date = cal.getTime();

    String result = Converter.convertDateWithoutTimeToString(date);

    assertTrue(result.startsWith("2024-03-15"));
  }

  // --- convertStringToDate ---

  @Test
  public void testConvertStringToDate_whenValidString_thenReturnsDate()
    throws ParseException {
    Date result = Converter.convertStringToDate("2024-03-15T10:30:00.000Z");

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.setTime(result);
    assertEquals(2024, cal.get(Calendar.YEAR));
    assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
    assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
  }

  @Test(expected = ParseException.class)
  public void testConvertStringToDate_whenInvalidString_thenThrowsParseException()
    throws ParseException {
    Converter.convertStringToDate("not-a-date");
  }

  // --- convertMlToI18nProperty ---

  @Test
  public void testConvertMlToI18nProperty_whenKeyLongerThan2_thenTruncates() {
    Map<String, String> ml = new LinkedHashMap<>();
    ml.put("eng", "English");
    ml.put("fr", "French");

    I18nProperty result = Converter.convertMlToI18nProperty(ml);

    assertEquals("English", result.get("en"));
    assertEquals("French", result.get("fr"));
  }

  // --- convertHtmlToText ---

  @Test
  public void testConvertHtmlToText_whenHtmlProvided_thenStripsTagsKeepsText() {
    String html = "<p>Hello</p><p>World</p>";
    String result = Converter.convertHtmlToText(html);

    assertTrue(result.contains("Hello"));
    assertTrue(result.contains("World"));
    assertFalse(result.contains("<p>"));
  }

  @Test
  public void testConvertHtmlToText_whenTrailingNewlines_thenRemovesThem() {
    String html = "<p>Hello</p>";
    String result = Converter.convertHtmlToText(html);

    assertFalse(result.endsWith("\n"));
  }

  // --- convertHtmlToJsonText ---

  @Test
  public void testConvertHtmlToJsonText_whenHtmlWithBlocks_thenUsesEscapedNewlines() {
    String html = "<p>Line1</p><p>Line2</p>";
    String result = Converter.convertHtmlToJsonText(html);

    assertTrue(result.contains("\\\\n"));
    assertFalse(result.contains("<p>"));
  }

  // --- toMLText(I18nProperty) ---

  @Test
  public void testToMLText_whenI18nPropertyProvided_thenConvertsToMLText() {
    I18nProperty prop = new I18nProperty();
    prop.put("en", "English");
    prop.put("fr", "French");

    MLText result = Converter.toMLText(prop);

    assertEquals("English", result.getValue(Locale.ENGLISH));
    assertEquals("French", result.getValue(Locale.FRENCH));
  }

  @Test
  public void testToMLText_whenNullValue_thenSetsEmptyString() {
    I18nProperty prop = new I18nProperty();
    prop.put("en", null);

    MLText result = Converter.toMLText(prop);

    assertEquals("", result.getValue(Locale.ENGLISH));
  }

  // --- toMLTextEN ---

  @Test
  public void testToMLTextEN_whenStringProvided_thenReturnsEnglishMLText() {
    MLText result = Converter.toMLTextEN("test");
    assertEquals("test", result.getValue(Locale.ENGLISH));
  }

  // --- createNodeRefFromId ---

  @Test
  public void testCreateNodeRefFromId_whenIdProvided_thenReturnsWorkspaceNodeRef() {
    NodeRef result = Converter.createNodeRefFromId("abc-123");

    assertEquals(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      result.getStoreRef()
    );
    assertEquals("abc-123", result.getId());
  }

  // --- createArchiveNodeRefFromId ---

  @Test
  public void testCreateArchiveNodeRefFromId_whenIdProvided_thenReturnsArchiveNodeRef() {
    NodeRef result = Converter.createArchiveNodeRefFromId("arc-456");

    assertEquals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, result.getStoreRef());
    assertEquals("arc-456", result.getId());
  }

  // --- extractNodeRefId ---

  @Test
  public void testExtractNodeRefId_whenValidNodeRef_thenReturnsId() {
    String result = Converter.extractNodeRefId(
      "workspace://SpacesStore/abc-123"
    );
    assertEquals("abc-123", result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractNodeRefId_whenNoSlash_thenThrowsException() {
    Converter.extractNodeRefId("noslash");
  }

  // --- getOriginalFileName ---

  @Test
  public void testGetOriginalFileName_whenTempFile_thenReturnsOriginal() {
    String result = Converter.getOriginalFileName("myFile.jpg1234567890cbctmp");
    assertEquals("myFile.jpg", result);
  }

  @Test
  public void testGetOriginalFileName_whenNoCbctmp_thenReturnsUnchanged() {
    String result = Converter.getOriginalFileName("normalFile.txt");
    assertEquals("normalFile.txt", result);
  }

  // --- escapeQName ---

  @Test
  public void testEscapeQName_whenSpecialChars_thenEscapesThem() {
    QName qName = QName.createQName("{http://www.alfresco.org/model}name");
    String result = Converter.escapeQName(qName);

    assertTrue(result.contains("\\{"));
    assertTrue(result.contains("\\}"));
  }

  // --- replaceEnclosingSingleQuotes ---

  @Test
  public void testReplaceEnclosingSingleQuotes_whenEnclosed_thenReplacesWithDouble() {
    String result = Converter.replaceEnclosingSingleQuotes("'hello'");
    assertEquals("\"hello\"", result);
  }

  @Test
  public void testReplaceEnclosingSingleQuotes_whenNotEnclosed_thenReturnsUnchanged() {
    String result = Converter.replaceEnclosingSingleQuotes("hello");
    assertEquals("hello", result);
  }

  @Test
  public void testReplaceEnclosingSingleQuotes_whenNull_thenReturnsNull() {
    String result = Converter.replaceEnclosingSingleQuotes(null);
    assertNull(result);
  }

  @Test
  public void testReplaceEnclosingSingleQuotes_whenTooShort_thenReturnsUnchanged() {
    String result = Converter.replaceEnclosingSingleQuotes("'a");
    assertEquals("'a", result);
  }

  // --- getStringOrMLTextValue ---

  @Test
  public void testGetStringOrMLTextValue_whenNull_thenReturnsEmpty() {
    String result = Converter.getStringOrMLTextValue(null, "en");
    assertEquals("", result);
  }

  @Test
  public void testGetStringOrMLTextValue_whenString_thenReturnsString() {
    String result = Converter.getStringOrMLTextValue("hello", "en");
    assertEquals("hello", result);
  }

  @Test
  public void testGetStringOrMLTextValue_whenMLText_thenReturnsValueForLocale() {
    MLText mlText = new MLText();
    mlText.addValue(Locale.FRENCH, "Bonjour");

    String result = Converter.getStringOrMLTextValue(mlText, "fr");
    assertEquals("Bonjour", result);
  }

  @Test
  public void testGetStringOrMLTextValue_whenNullLanguage_thenUsesEnglish() {
    MLText mlText = new MLText();
    mlText.addValue(Locale.ENGLISH, "Hello");

    String result = Converter.getStringOrMLTextValue(mlText, null);
    assertEquals("Hello", result);
  }
}
