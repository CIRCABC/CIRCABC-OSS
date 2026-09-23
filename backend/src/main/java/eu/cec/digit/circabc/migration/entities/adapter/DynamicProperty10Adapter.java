package eu.cec.digit.circabc.migration.entities.adapter;

import eu.cec.digit.circabc.migration.entities.TypedProperty;

public class DynamicProperty10Adapter extends PropertyAdapterBase {

  @Override
  public TypedProperty unmarshal(String propertyStringValue) throws Exception {
    return new TypedProperty.DynamicProperty10(propertyStringValue);
  }
}
