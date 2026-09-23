package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DynamicPropertyImplTest {

  private DynamicPropertyImpl property;
  private MLText label;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    label = new MLText(Locale.ENGLISH, "Test Label");
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
  }

  @Test
  public void testGetListOfValidValues_whenValuesPresent_thenReturnsList() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    List<String> values = prop.getListOfValidValues();
    assertEquals(3, values.size());
    assertEquals("a", values.get(0));
    assertEquals("b", values.get(1));
    assertEquals("c", values.get(2));
  }

  @Test
  public void testGetListOfValidValues_whenNull_thenReturnsEmptyList() {
    List<String> values = property.getListOfValidValues();
    assertTrue(values.isEmpty());
  }

  @Test
  public void testGetListOfValidValues_whenEmpty_thenReturnsEmptyList() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      ""
    );
    List<String> values = prop.getListOfValidValues();
    assertTrue(values.isEmpty());
  }

  @Test
  public void testGetDisplayValidValues_whenValuesPresent_thenFormatsWithSemicolon() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    assertEquals("a; b; c", prop.getDisplayValidValues());
  }

  @Test
  public void testGetDisplayValidValues_whenNull_thenReturnsEmpty() {
    assertEquals("", property.getDisplayValidValues());
  }

  @Test
  public void testIsSelectionType_whenSelection_thenTrue() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb"
    );
    assertTrue(prop.isSelectionType());
  }

  @Test
  public void testIsSelectionType_whenMultiSelection_thenTrue() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.MULTI_SELECTION,
      "a\nb"
    );
    assertTrue(prop.isSelectionType());
  }

  @Test
  public void testIsSelectionType_whenTextField_thenFalse() {
    assertFalse(property.isSelectionType());
  }

  @Test
  public void testGetName_whenLabelPresent_thenReturnsDefaultValue() {
    assertEquals("Test Label", property.getName());
  }

  @Test
  public void testGetName_whenLabelNull_thenReturnsEmpty() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      null,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("", prop.getName());
  }

  @Test
  public void testGetLanguages_whenLabelPresent_thenReturnsLocales() {
    String languages = property.getLanguages();
    assertTrue(languages.contains("en"));
  }

  @Test
  public void testGetLanguages_whenLabelNull_thenReturnsBrackets() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      1L,
      nodeRef,
      null,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("[]", prop.getLanguages());
  }

  @Test
  public void testEquals_whenSameObject_thenTrue() {
    assertEquals(property, property);
  }

  @Test
  public void testEquals_whenEqualProperties_thenTrue() {
    DynamicPropertyImpl other = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals(property, other);
  }

  @Test
  public void testEquals_whenNull_thenFalse() {
    assertNotEquals(null, property);
  }

  @Test
  public void testEquals_whenDifferentType_thenFalse() {
    DynamicPropertyImpl other = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.DATE_FIELD,
      null
    );
    assertNotEquals(property, other);
  }

  @Test
  public void testHashCode_whenEqualObjects_thenSameHash() {
    DynamicPropertyImpl other = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals(property.hashCode(), other.hashCode());
  }

  @Test
  public void testCompareTo_whenDifferentIndex_thenReturnsCorrectOrder() {
    DynamicPropertyImpl other = new DynamicPropertyImpl(
      3L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertTrue(property.compareTo(other) < 0);
    assertTrue(other.compareTo(property) > 0);
  }

  @Test
  public void testGetters() {
    assertEquals(Long.valueOf(1L), property.getIndex());
    assertEquals(nodeRef, property.getId());
    assertEquals(label, property.getLabel());
    assertEquals(DynamicPropertyType.TEXT_FIELD, property.getType());
    assertNull(property.getValidValues());
  }

  @Test
  public void testSetValidValues() {
    property.setValidValues("x\ny");
    assertEquals("x\ny", property.getValidValues());
  }

  @Test
  public void testConstructor_threeArgs() {
    DynamicPropertyImpl prop = new DynamicPropertyImpl(
      label,
      DynamicPropertyType.TEXT_AREA,
      "val"
    );
    assertNull(prop.getIndex());
    assertNull(prop.getId());
    assertEquals(label, prop.getLabel());
    assertEquals(DynamicPropertyType.TEXT_AREA, prop.getType());
    assertEquals("val", prop.getValidValues());
  }
}
