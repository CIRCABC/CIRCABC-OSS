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
package eu.cec.digit.circabc.model;

import org.alfresco.service.namespace.QName;

/**
 * It is the model for the dynamic properties specification
 *
 * @author Slobodan Filipovic
 */
public interface DynamicPropertyModel extends BaseCircabcModel {

    /**
     * Circabc Dynamic Properties namespace
     */
    String CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI =
            CIRCABC_NAMESPACE + "/model/dynamicproperties/1.0";

    /**
     * Circabc dynamic property prefix
     */
    String CIRCABC_DYNAMIC_PROPERTY_MODEL_PREFIX = "dz";

    /**
     * Circabc dynamic property root container
     */
    QName TYPE_DYNAMIC_PROPERTY_CONTAINER = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "Container");

    /**
     * Circabc dynamic property
     */
    QName TYPE_DYNAMIC_PROPERTY = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "DynProp");

    /**
     * Circabc dynamic property
     */
    QName ASSOC_DYNAMIC_PROPERTY = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "DynPropAss");

    /**
     * Circabc dynamic property index
     */
    QName PROP_DYNAMIC_PROPERTY_INDEX = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "Index");

    /**
     * Circabc dynamic property
     */
    QName PROP_DYNAMIC_PROPERTY_LABEL = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "Label");

    /**
     * Circabc dynamic property label
     */
    QName PROP_DYNAMIC_PROPERTY_TYPE = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "Type");

    /**
     * Circabc dynamic property
     */
    QName PROP_DYNAMIC_PROPERTY_VALID_VALUES = QName
            .createQName(CIRCABC_DYNAMIC_PROPERTY_MODEL_1_0_URI, "ValidValues");

    /**
     * The possible values of the dynamic property type
     */
    String[] DYNAMIC_PROPERTY_TYPE_VALUES = {
            "DATE_FIELD", "TEXT_FIELD", "TEXT_AREA", "SELECTION", "MULTI_SELECTION"
    };

}
