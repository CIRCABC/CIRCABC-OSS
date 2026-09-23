package io.swagger.model;

/*
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 * Simple key/value data holder representing a single statistic entry.
 *
 * <p>A {@code StatData} pairs a name that identifies the statistic ({@link #dataName}) with its
 * associated value ({@link #dataValue}). Because the value is typed as {@link Object}, an instance
 * can carry heterogeneous statistic values (for example numbers, strings or nested structures) and
 * is typically serialized as part of a statistics REST response.
 *
 * @author beaurpi
 */
public class StatData {

  /** Name identifying the statistic this entry represents. */
  private String dataName;

  /** Value associated with the statistic; may be of any type. */
  private Object dataValue;

  /** Creates an empty {@code StatData} with no name or value set. */
  public StatData() {}

  /**
   * Creates a fully populated {@code StatData}.
   *
   * @param name the name identifying the statistic
   * @param value the value associated with the statistic
   */
  public StatData(String name, Object value) {
    this.dataName = name;
    this.dataValue = value;
  }

  /**
   * Returns the name identifying the statistic.
   *
   * @return the statistic name
   */
  public String getDataName() {
    return dataName;
  }

  /**
   * Sets the name identifying the statistic.
   *
   * @param dataName the statistic name to set
   */
  public void setDataName(String dataName) {
    this.dataName = dataName;
  }

  /**
   * Returns the value associated with the statistic.
   *
   * @return the statistic value
   */
  public Object getDataValue() {
    return dataValue;
  }

  /**
   * Sets the value associated with the statistic.
   *
   * @param dataValue the statistic value to set
   */
  public void setDataValue(Object dataValue) {
    this.dataValue = dataValue;
  }
}
