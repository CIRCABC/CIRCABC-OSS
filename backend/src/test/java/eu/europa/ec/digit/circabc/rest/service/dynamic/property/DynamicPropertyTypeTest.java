package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import static org.junit.Assert.*;

import org.junit.Test;

public class DynamicPropertyTypeTest {

  @Test
  public void testGetModelDataDefinition_returnsEnumName() {
    assertEquals(
      "DATE_FIELD",
      DynamicPropertyType.DATE_FIELD.getModelDataDefinition()
    );
    assertEquals(
      "TEXT_FIELD",
      DynamicPropertyType.TEXT_FIELD.getModelDataDefinition()
    );
    assertEquals(
      "TEXT_AREA",
      DynamicPropertyType.TEXT_AREA.getModelDataDefinition()
    );
    assertEquals(
      "SELECTION",
      DynamicPropertyType.SELECTION.getModelDataDefinition()
    );
    assertEquals(
      "MULTI_SELECTION",
      DynamicPropertyType.MULTI_SELECTION.getModelDataDefinition()
    );
  }

  @Test
  public void testValues_containsAllExpectedTypes() {
    DynamicPropertyType[] values = DynamicPropertyType.values();
    assertEquals(5, values.length);
  }

  @Test
  public void testValueOf_validName_returnsEnum() {
    assertEquals(
      DynamicPropertyType.DATE_FIELD,
      DynamicPropertyType.valueOf("DATE_FIELD")
    );
    assertEquals(
      DynamicPropertyType.SELECTION,
      DynamicPropertyType.valueOf("SELECTION")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalidName_throwsException() {
    DynamicPropertyType.valueOf("INVALID");
  }
}
