package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import static org.junit.Assert.*;

import org.junit.Test;

public class DynamicPropertyServiceTest {

  @Test
  public void testConstants() {
    assertEquals('\n', DynamicPropertyService.MULTI_VALUES_SEPARATOR);
    assertEquals("\n", DynamicPropertyService.MULTI_VALUES_SEPARATOR_STRING);
    assertEquals(20, DynamicPropertyService.MAX_PROPERTY_BY_IG);
    assertEquals(5, DynamicPropertyService.MAX_PROPERTY_BY_IG_IN_CIRCA);
  }
}
