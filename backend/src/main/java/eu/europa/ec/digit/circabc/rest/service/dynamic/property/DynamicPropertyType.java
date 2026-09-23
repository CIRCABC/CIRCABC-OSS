/**
 * ***************************************************************************** Copyright 2006
 * European Community
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
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

/**
 * Enumerates the kinds of user-definable dynamic properties supported by CIRCABC.
 *
 * <p>Each constant represents a distinct input type that can back a dynamic property attached to a
 * node (for example a date picker, a free-text field, a multi-line text area, or single/multiple
 * value selection lists). Every constant exposes its own {@link #getModelDataDefinition()}
 * implementation, allowing the content-model data definition associated with the property type to
 * be resolved per type.
 *
 * @author Yanick Pignot
 */
public enum DynamicPropertyType {
  /** A property rendered and stored as a single date value. */
  DATE_FIELD {
    /**
     * {@inheritDoc}
     *
     * @return the model data definition for a date field, i.e. this constant's {@link #name()}
     */
    public String getModelDataDefinition() {
      return this.name();
    }
  },

  /** A property rendered and stored as a single-line free-text value. */
  TEXT_FIELD {
    /**
     * {@inheritDoc}
     *
     * @return the model data definition for a text field, i.e. this constant's {@link #name()}
     */
    public String getModelDataDefinition() {
      return this.name();
    }
  },

  /** A property rendered and stored as a multi-line free-text value. */
  TEXT_AREA {
    /**
     * {@inheritDoc}
     *
     * @return the model data definition for a text area, i.e. this constant's {@link #name()}
     */
    public String getModelDataDefinition() {
      return this.name();
    }
  },

  /** A property whose value is chosen from a predefined single-selection list. */
  SELECTION {
    /**
     * {@inheritDoc}
     *
     * @return the model data definition for a single-selection field, i.e. this constant's {@link
     *     #name()}
     */
    public String getModelDataDefinition() {
      return this.name();
    }
  },

  /** A property whose value allows choosing several entries from a predefined list. */
  MULTI_SELECTION {
    /**
     * {@inheritDoc}
     *
     * @return the model data definition for a multi-selection field, i.e. this constant's {@link
     *     #name()}
     */
    public String getModelDataDefinition() {
      return this.name();
    }
  };

  /**
   * Returns the content-model data definition identifier associated with this dynamic property
   * type.
   *
   * @return the model data definition for this property type
   */
  public abstract String getModelDataDefinition();
}
