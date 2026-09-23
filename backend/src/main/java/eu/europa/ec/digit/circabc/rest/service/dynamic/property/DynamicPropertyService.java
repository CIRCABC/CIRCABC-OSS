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
import java.util.Map;
import java.util.Set;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Service interface defining CRUD and maintenance operations for <em>dynamic properties</em>.
 *
 * <p>A dynamic property is a user-defined, per-interest-group metadata field that can be attached to
 * content nodes. Each dynamic property definition is stored as a child node beneath the interest
 * group's root node, and its value is persisted as a string property on the content node. Dynamic
 * properties may hold a single value or multiple values; multiple values are encoded within a single
 * string separated by {@link #MULTI_VALUES_SEPARATOR}.
 *
 * <p>Implementations are responsible for creating, deleting, updating and querying these
 * definitions, as well as resolving the underlying Alfresco property {@link QName} used to store a
 * property's value.
 *
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface DynamicPropertyService {
  /** Character used to separate the individual values of a multi-valued dynamic property. */
  char MULTI_VALUES_SEPARATOR = '\n';
  /** String form of {@link #MULTI_VALUES_SEPARATOR}, convenient for string-based operations. */
  String MULTI_VALUES_SEPARATOR_STRING = "\n";
  /** Maximum number of dynamic properties allowed per interest group in CIRCABC. */
  int MAX_PROPERTY_BY_IG = 20;
  /** Maximum number of dynamic properties per interest group inherited from the legacy CIRCA system. */
  int MAX_PROPERTY_BY_IG_IN_CIRCA = 5;

  /**
   * Add a dynamic property to an interest group
   *
   * @param ig      The ig's node reference
   * @param dynamicProperty The dynamic property to add
   * @return the newly created {@link DynamicProperty}, populated with its persisted node reference
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "ig", "dynamicProperty" }
  )
  DynamicProperty addDynamicProperty(
    final NodeRef ig,
    final DynamicProperty dynamicProperty
  );

  /**
   * Delete DynamicProperty .
   *
   * @param dp The dynamic property to delete
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "dynamicProperty" })
  void deleteDynamicProperty(final DynamicProperty dp);

  /**
   * Gets the list of DynamicPropertys for an interest group.
   *
   * @param dp node reference that that belongs to interest group including ig root
   * @return a collection of {@link NodeRef}
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "dp" })
  List<DynamicProperty> getDynamicProperties(final NodeRef dp);

  /**
   * Gets the DynamicProperty for an by ID.
   *
   * @param nodeRef The ig's node reference
   * @return Dynamic Property
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "nodeRef" })
  DynamicProperty getDynamicPropertyByID(final NodeRef nodeRef);

  /**
   * Get the qname of the property on witch the keyword is setted.
   *
   * @param dp the dynamic property
   * @return the property QName
   * @see io.swagger.model.alfresco.DocumentModel#ALL_DYN_PROPS
   */
  @NotAuditable
  QName getPropertyQname(final DynamicProperty dp);

  /**
   * Update dynamic property label.
   *
   * @param dp    Dynamic property to be updated
   * @param label new label as MLText
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "dynamicProperty", "label" }
  )
  void updateDynamicPropertyLabel(final DynamicProperty dp, final MLText label);

  /**
   * Update the set of valid (allowed) values of a dynamic property, optionally propagating the
   * change to content nodes that already use the property.
   *
   * @param dp                       the dynamic property whose valid values are updated
   * @param validValues              the new valid values, encoded as a single string with individual
   *                                 values separated by {@link #MULTI_VALUES_SEPARATOR}
   * @param updateExistingProperties {@code true} to apply the renames and deletions to existing
   *                                 content nodes already referencing the affected values
   * @param deletedValues            the values that were removed and should be cleared from existing
   *                                 nodes when {@code updateExistingProperties} is {@code true}
   * @param updatedValues            a mapping from old value to new value for values that were
   *                                 renamed, applied to existing nodes when
   *                                 {@code updateExistingProperties} is {@code true}
   */
  @NotAuditable
  void updateDynamicPropertyValidValues(
    final DynamicProperty dp,
    final String validValues,
    boolean updateExistingProperties,
    Set<String> deletedValues,
    Map<String, String> updatedValues
  );
}
