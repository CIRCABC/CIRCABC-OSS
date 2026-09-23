package eu.europa.ec.digit.circabc.rest.service.keyword;

import static org.junit.Assert.*;

import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class KeywordTest {

  private NodeRef nodeRef;

  @Before
  public void setUp() {
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetValue_whenNonMultilingual_thenReturnsValue() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    assertEquals("environment", keyword.getValue());
    assertFalse(keyword.isKeywordTranslated());
  }

  @Test
  public void testGetMLValues_whenMultilingual_thenReturnsMLText() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    assertNotNull(keyword.getMLValues());
    assertTrue(keyword.isKeywordTranslated());
    assertNull(keyword.getValue());
  }

  @Test
  public void testGetId_returnsNodeRef() {
    Keyword keyword = new KeywordImpl(nodeRef, "test");
    assertEquals(nodeRef, keyword.getId());
  }

  @Test
  public void testAddTranslation_whenMultilingual_thenAddsLocale() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    keyword.addTranlatation(Locale.FRENCH, "environnement");
    assertEquals(
      "environnement",
      keyword.getMLValues().getValue(Locale.FRENCH)
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddTranslation_whenNotMultilingual_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    keyword.addTranlatation(Locale.FRENCH, "environnement");
  }

  @Test(expected = NullPointerException.class)
  public void testAddTranslation_whenNullLocale_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    keyword.addTranlatation(null, "value");
  }

  @Test(expected = NullPointerException.class)
  public void testAddTranslation_whenEmptyKeyword_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    keyword.addTranlatation(Locale.FRENCH, "");
  }

  @Test
  public void testMakeMultilingual_whenNonMultilingual_thenConverts() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    assertFalse(keyword.isKeywordTranslated());
    keyword.makeMultilingual(Locale.ENGLISH);
    assertTrue(keyword.isKeywordTranslated());
    assertEquals("environment", keyword.getMLValues().getValue(Locale.ENGLISH));
    assertNull(keyword.getValue());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMakeMultilingual_whenAlreadyMultilingual_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    keyword.makeMultilingual(Locale.FRENCH);
  }

  @Test(expected = NullPointerException.class)
  public void testMakeMultilingual_whenNullLocale_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    keyword.makeMultilingual(null);
  }

  @Test
  public void testSetTranslations_replacesExisting() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "old");
    MLText translations = new MLText();
    translations.put(Locale.ENGLISH, "new");
    translations.put(Locale.FRENCH, "nouveau");
    keyword.setTranlatations(translations);
    assertEquals("new", keyword.getMLValues().getValue(Locale.ENGLISH));
    assertEquals("nouveau", keyword.getMLValues().getValue(Locale.FRENCH));
  }

  @Test(expected = NullPointerException.class)
  public void testSetTranslations_whenNull_thenThrows() {
    Keyword keyword = new KeywordImpl(nodeRef, "test");
    keyword.setTranlatations(null);
  }

  @Test
  public void testExists_whenLocaleAndValueMatch_thenReturnsTrue() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    assertTrue(keyword.exists(Locale.ENGLISH, "environment"));
  }

  @Test
  public void testExists_whenValueDoesNotMatch_thenReturnsFalse() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    assertFalse(keyword.exists(Locale.ENGLISH, "other"));
  }

  @Test
  public void testExists_whenLocaleDoesNotExist_thenReturnsFalse() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    assertFalse(keyword.exists(Locale.FRENCH, "environment"));
  }

  @Test
  public void testExists_whenNotMultilingual_thenReturnsFalse() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    assertFalse(keyword.exists(Locale.ENGLISH, "environment"));
  }

  @Test
  public void testGetSelectedAndSetSelected() {
    Keyword keyword = new KeywordImpl(nodeRef, "test");
    assertNull(keyword.getSelected());
    keyword.setSelected(Boolean.TRUE);
    assertEquals(Boolean.TRUE, keyword.getSelected());
  }

  @Test
  public void testGetString_whenNonMultilingual_thenReturnsValue() {
    Keyword keyword = new KeywordImpl(nodeRef, "environment");
    assertEquals("environment", keyword.getString());
  }

  @Test
  public void testGetString_whenMultilingual_thenReturnsFormattedTranslations() {
    Keyword keyword = new KeywordImpl(nodeRef, Locale.ENGLISH, "environment");
    String result = keyword.getString();
    assertTrue(result.contains("en"));
    assertTrue(result.contains("environment"));
  }
}
