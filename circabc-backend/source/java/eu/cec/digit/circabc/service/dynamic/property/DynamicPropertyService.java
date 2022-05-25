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
package eu.cec.digit.circabc.service.dynamic.property;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for dynamic property operations. A dynamic property
 *
 * <p>The dynamic property are stored as string property attribyte1 attribyte2 Each dynamic property
 * definition is bellow interest group root node.
 *
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface DynamicPropertyService {

    char MULTI_VALUES_SEPARATOR = '\n';
    String MULTI_VALUES_SEPARATOR_STRING = "\n";
    int MAX_PROPERTY_BY_IG = 20;
    int MAX_PROPERTY_BY_IG_IN_CIRCA = 5;

    /**
     * Add a dynamic property to an interest group
     *
     * @param ig      The ig's node reference
     * @param keyword The keyword to add
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"ig", "dynamicProperty"})
    DynamicProperty addDynamicProperty(final NodeRef ig, final DynamicProperty dynamicProperty);

    /**
     * Delete DynamicProperty .
     *
     * @param ig The ig's node reference
     * @return a collection of {@link DynamicProperty}
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"dynamicProperty"})
    void deleteDynamicProperty(final DynamicProperty dp);

    /**
     * Gets the list of DynamicPropertys for an interest group.
     *
     * @param dp node reference that that belongs to interest group including ig root
     * @return a collection of {@link NodeRef}
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"dp"})
    List<DynamicProperty> getDynamicProperties(final NodeRef dp);

    /**
     * Gets the DynamicProperty for an by ID.
     *
     * @param ig The ig's node reference
     * @return Dynamic Property
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef"})
    DynamicProperty getDynamicPropertyByID(final NodeRef nodeRef);

    /**
     * Get the qname of the property on witch the keyword is setted.
     *
     * @param dp the dynamic property
     * @return the property QName
     * @see eu.cec.digit.circabc.model.DocumentModel#ALL_DYN_PROPS
     */
    @NotAuditable
    QName getPropertyQname(final DynamicProperty dp);

    /**
     * Update dynamic property label.
     *
     * @param dp    Dynamic property to be updated
     * @param label new label as MLText
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"dynamicProperty", "label"})
    void updateDynamicPropertyLabel(final DynamicProperty dp, final MLText label);

    /**
     * @param dp
     * @param validValues
     * @param updateExistingProperties
     * @param deletedValues
     * @param updatedValues
     */
    @NotAuditable
    void updateDynamicPropertyValidValues(
            final DynamicProperty dp,
            final String validValues,
            boolean updateExistingProperties,
            Set<String> deletedValues,
            Map<String, String> updatedValues);
}
