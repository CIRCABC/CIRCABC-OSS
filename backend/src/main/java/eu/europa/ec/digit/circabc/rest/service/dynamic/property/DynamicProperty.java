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

import java.util.List;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Represents a single dynamic (user-defined) property that can be attached to CIRCABC content.
 *
 * <p>A dynamic property carries its own metadata: an ordering index, a multilingual label, a value
 * type (e.g. text field, text area, date field or selection) and, for selection types, the set of
 * valid values. Each property is backed by a node in the Alfresco repository, identified by {@link
 * #getId()}.
 *
 * <p>Implementations are {@link Comparable} so that a collection of properties can be sorted, the
 * natural ordering being based on the property {@link #getIndex() index}.
 */
public interface DynamicProperty extends Comparable<DynamicProperty> {
  /**
   * Returns the ordering index of the property, used to control the display/sort order relative to
   * other dynamic properties.
   *
   * @return the property index
   */
  Long getIndex();

  /**
   * Returns the multilingual, human-readable label of the property.
   *
   * @return the localized label as {@link MLText}
   */
  MLText getLabel();

  /**
   * Returns the type of the property, which determines how it is edited and rendered (for example a
   * date field, text field, text area or selection).
   *
   * @return the dynamic property type
   */
  DynamicPropertyType getType();

  /**
   * Returns the raw definition of the valid values accepted by a selection-type property, encoded
   * as a single string (similar in spirit to the constants of a Java enum).
   *
   * @return the raw valid-values definition, or {@code null}/empty when not a selection type
   */
  String getValidValues();

  /**
   * Returns the valid values formatted for display to the end user.
   *
   * @return the display-friendly representation of the valid values
   */
  String getDisplayValidValues();

  /**
   * Returns the valid values parsed into a list of individual entries.
   *
   * @return the list of valid values, typically empty when the property is not a selection type
   */
  List<String> getListOfValidValues();

  /**
   * Indicates whether this property is a selection type (i.e. its value must be chosen from a
   * predefined set of valid values).
   *
   * @return {@code true} if the property is a selection type, {@code false} otherwise
   */
  boolean isSelectionType();

  /**
   * @return the node reference where the dynamic property is stored
   */
  NodeRef getId();

  /**
   * Returns the technical name of the property.
   *
   * @return the property name
   */
  String getName();

  /**
   * Returns the languages associated with the property (for example the locales for which the label
   * or values are defined).
   *
   * @return the languages descriptor
   */
  String getLanguages();
}
