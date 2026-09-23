package eu.europa.ec.digit.circabc.rest.service.keyword;

import static org.junit.Assert.*;

import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class KeywordImplTest {

  private NodeRef nodeRef;

  @Before
  public void setUp() {
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetValue_whenNonMultilingual_thenReturnsValue() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "testValue");
    assertEquals("testValue", keyword.getValue());
    assertNull(keyword.getMLValues());
    assertFalse(keyword.isKeywordTranslated());
  }

  @Test
  public void testGetValue_whenMultilingualWithLocale_thenValueIsNull() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    assertNull(keyword.getValue());
    assertNotNull(keyword.getMLValues());
    assertTrue(keyword.isKeywordTranslated());
    assertEquals("hello", keyword.getMLValues().getValue(Locale.ENGLISH));
  }

  @Test
  public void testConstructor_whenLocaleIsNull_thenNonMultilingual() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, null, "plain");
    assertEquals("plain", keyword.getValue());
    assertFalse(keyword.isKeywordTranslated());
  }

  @Test
  public void testConstructor_withMLText() {
    MLText mlText = new MLText();
    mlText.put(Locale.FRENCH, "bonjour");
    mlText.put(Locale.ENGLISH, "hello");
    KeywordImpl keyword = new KeywordImpl(nodeRef, mlText);
    assertTrue(keyword.isKeywordTranslated());
    assertNull(keyword.getValue());
    assertEquals("bonjour", keyword.getMLValues().getValue(Locale.FRENCH));
  }

  @Test
  public void testGetId_returnsNodeRef() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    assertEquals(nodeRef, keyword.getId());
  }

  @Test
  public void testMakeMultilingual_whenNonMultilingual_thenConverts() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "original");
    keyword.makeMultilingual(Locale.GERMAN);
    assertTrue(keyword.isKeywordTranslated());
    assertNull(keyword.getValue());
    assertEquals("original", keyword.getMLValues().getValue(Locale.GERMAN));
  }

  @Test(expected = NullPointerException.class)
  public void testMakeMultilingual_whenLocaleNull_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    keyword.makeMultilingual(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMakeMultilingual_whenAlreadyMultilingual_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "val");
    keyword.makeMultilingual(Locale.FRENCH);
  }

  @Test
  public void testAddTranslation_whenMultilingual_thenAddsLocale() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    keyword.addTranlatation(Locale.FRENCH, "bonjour");
    assertEquals("bonjour", keyword.getMLValues().getValue(Locale.FRENCH));
  }

  @Test(expected = NullPointerException.class)
  public void testAddTranslation_whenLocaleNull_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    keyword.addTranlatation(null, "value");
  }

  @Test(expected = NullPointerException.class)
  public void testAddTranslation_whenKeywordNull_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    keyword.addTranlatation(Locale.FRENCH, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddTranslation_whenNotMultilingual_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "plain");
    keyword.addTranlatation(Locale.FRENCH, "bonjour");
  }

  @Test
  public void testSetTranslations_replacesExisting() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    MLText newTranslations = new MLText();
    newTranslations.put(Locale.ITALIAN, "ciao");
    keyword.setTranlatations(newTranslations);
    assertEquals("ciao", keyword.getMLValues().getValue(Locale.ITALIAN));
    assertNull(keyword.getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testSetTranslations_whenNull_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    keyword.setTranlatations(null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetTranslations_whenEmpty_thenThrows() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    keyword.setTranlatations(new MLText());
  }

  @Test
  public void testExists_whenLocaleAndValueMatch_thenTrue() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    assertTrue(keyword.exists(Locale.ENGLISH, "hello"));
  }

  @Test
  public void testExists_whenValueDoesNotMatch_thenFalse() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    assertFalse(keyword.exists(Locale.ENGLISH, "world"));
  }

  @Test
  public void testExists_whenLocaleNotPresent_thenFalse() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    assertFalse(keyword.exists(Locale.FRENCH, "hello"));
  }

  @Test
  public void testExists_whenNotMultilingual_thenFalse() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "plain");
    assertFalse(keyword.exists(Locale.ENGLISH, "plain"));
  }

  @Test
  public void testToString_whenNonMultilingual_thenReturnsValue() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "simple");
    assertEquals("simple", keyword.toString());
  }

  @Test
  public void testToString_whenMultilingual_thenFormatsEntries() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "hello");
    String result = keyword.toString();
    assertTrue(result.contains("en"));
    assertTrue(result.contains("hello"));
  }

  @Test
  public void testSelected_getterSetter() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    assertNull(keyword.getSelected());
    keyword.setSelected(true);
    assertTrue(keyword.getSelected());
  }

  @Test
  public void testHashCode_sameFields_sameHash() {
    KeywordImpl k1 = new KeywordImpl(nodeRef, "val");
    KeywordImpl k2 = new KeywordImpl(nodeRef, "val");
    assertEquals(k1.hashCode(), k2.hashCode());
  }

  @Test
  public void testEquals_sameInstance_thenTrue() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    assertTrue(keyword.equals(keyword));
  }

  @Test
  public void testEquals_differentClass_thenFalse() {
    KeywordImpl keyword = new KeywordImpl(nodeRef, "val");
    assertFalse(keyword.equals("string"));
  }
}
