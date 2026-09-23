package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DynamicPropertyTest {

  private DynamicProperty property;
  private NodeRef nodeRef;
  private MLText label;

  @Before
  public void setUp() {
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
    label = new MLText(Locale.ENGLISH, "Test Label");
  }

  @Test
  public void testGetIndex_whenSet_thenReturnsValue() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals(Long.valueOf(1L), property.getIndex());
  }

  @Test
  public void testGetLabel_whenSet_thenReturnsMLText() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals(label, property.getLabel());
  }

  @Test
  public void testGetType_whenSelection_thenReturnsSelection() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    assertEquals(DynamicPropertyType.SELECTION, property.getType());
  }

  @Test
  public void testGetValidValues_whenSet_thenReturnsRawString() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    assertEquals("a\nb\nc", property.getValidValues());
  }

  @Test
  public void testGetDisplayValidValues_whenSet_thenSeparatedBySemicolon() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    assertEquals("a; b; c", property.getDisplayValidValues());
  }

  @Test
  public void testGetDisplayValidValues_whenNull_thenReturnsEmpty() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("", property.getDisplayValidValues());
  }

  @Test
  public void testGetListOfValidValues_whenSet_thenReturnsList() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb\nc"
    );
    List<String> values = property.getListOfValidValues();
    assertEquals(3, values.size());
    assertEquals("a", values.get(0));
    assertEquals("b", values.get(1));
    assertEquals("c", values.get(2));
  }

  @Test
  public void testGetListOfValidValues_whenNull_thenReturnsEmptyList() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertTrue(property.getListOfValidValues().isEmpty());
  }

  @Test
  public void testGetListOfValidValues_whenEmpty_thenReturnsEmptyList() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      ""
    );
    assertTrue(property.getListOfValidValues().isEmpty());
  }

  @Test
  public void testIsSelectionType_whenSelection_thenTrue() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.SELECTION,
      "a\nb"
    );
    assertTrue(property.isSelectionType());
  }

  @Test
  public void testIsSelectionType_whenMultiSelection_thenTrue() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.MULTI_SELECTION,
      "a\nb"
    );
    assertTrue(property.isSelectionType());
  }

  @Test
  public void testIsSelectionType_whenTextField_thenFalse() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertFalse(property.isSelectionType());
  }

  @Test
  public void testGetId_whenSet_thenReturnsNodeRef() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals(nodeRef, property.getId());
  }

  @Test
  public void testGetName_whenLabelSet_thenReturnsDefaultValue() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("Test Label", property.getName());
  }

  @Test
  public void testGetName_whenLabelNull_thenReturnsEmpty() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      null,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("", property.getName());
  }

  @Test
  public void testGetLanguages_whenLabelSet_thenReturnsLocaleArray() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    String languages = property.getLanguages();
    assertTrue(languages.contains("en"));
  }

  @Test
  public void testGetLanguages_whenLabelNull_thenReturnsEmptyBrackets() {
    property = new DynamicPropertyImpl(
      1L,
      nodeRef,
      null,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertEquals("[]", property.getLanguages());
  }

  @Test
  public void testCompareTo_whenDifferentIndex_thenOrdersByIndex() {
    DynamicProperty prop1 = new DynamicPropertyImpl(
      1L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    DynamicProperty prop2 = new DynamicPropertyImpl(
      3L,
      nodeRef,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    assertTrue(prop1.compareTo(prop2) < 0);
    assertTrue(prop2.compareTo(prop1) > 0);
  }
}
