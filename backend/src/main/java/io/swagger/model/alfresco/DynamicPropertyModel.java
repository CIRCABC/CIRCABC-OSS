/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package io.swagger.model.alfresco;

import static io.swagger.model.alfresco.BaseCircabcModel.CIRCABC_NAMESPACE;

import org.alfresco.service.namespace.QName;

/**
 * Constants describing the Alfresco content model used for CIRCABC "dynamic
 * properties".
 *
 * <p>A dynamic property is a user-configurable metadata field (for example a
 * date, free text, text area or single/multi selection list) that can be
 * attached to nodes. This class centralises the qualified names ({@link QName})
 * of the model's namespace, types, associations and properties, together with
 * the set of supported property types.
 *
 * <p>This is a non-instantiable utility holder: it only exposes constants and
 * has no state or behaviour.
 *
 * @author Slobodan Filipovic
 */
public final class DynamicPropertyModel {

  /**
   * Prevents instantiation; this class only exposes static model constants.
   */
  private DynamicPropertyModel() {}

  /**
   * Circabc Dynamic Properties namespace
   */
  public static final String CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/dynamicproperties/1.0";

  /**
   * Circabc dynamic property prefix
   */
  public static final String CIRCABC_DYNAMIC_PROPERTY_MODEL_PREFIX = "dz";

  /**
   * Content type of the container node that holds the dynamic properties.
   */
  public static final QName TYPE_DYNAMIC_PROPERTY_CONTAINER = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "Container"
  );

  /**
   * Content type representing a single dynamic property definition.
   */
  public static final QName TYPE_DYNAMIC_PROPERTY = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "DynProp"
  );

  /**
   * Association linking the container to its dynamic property definitions.
   */
  public static final QName ASSOC_DYNAMIC_PROPERTY = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "DynPropAss"
  );

  /**
   * Property holding the display order (index) of a dynamic property.
   */
  public static final QName PROP_DYNAMIC_PROPERTY_INDEX = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "Index"
  );

  /**
   * Property holding the human-readable label of a dynamic property.
   */
  public static final QName PROP_DYNAMIC_PROPERTY_LABEL = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "Label"
  );

  /**
   * Property holding the type of a dynamic property; see
   * {@link #DYNAMIC_PROPERTY_TYPE_VALUES} for the allowed values.
   */
  public static final QName PROP_DYNAMIC_PROPERTY_TYPE = QName.createQName(
    CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI,
    "Type"
  );

  /**
   * Property holding the list of valid values for a (multi) selection dynamic
   * property.
   */
  public static final QName PROP_DYNAMIC_PROPERTY_VALID_VALUES =
    QName.createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "ValidValues");

  /**
   * The possible values of the dynamic property type
   */
  protected static final String[] DYNAMIC_PROPERTY_TYPE_VALUES = {
    "DATE_FIELD",
    "TEXT_FIELD",
    "TEXT_AREA",
    "SELECTION",
    "MULTI_SELECTION",
  };
}
