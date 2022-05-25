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

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public interface DynamicProperty extends Comparable<DynamicProperty> {

    // get the index of the property
    Long getIndex();

    MLText getLabel();

    // DateField , TextField, TextArea Selection
    DynamicPropertyType getType();

    // Valid values for  Selection type (similar like java enum)
    String getValidValues();

    String getDisplayValidValues();

    List<String> getListOfValidValues();

    boolean isSelectionType();

    /**
     * @return the node reference where the dynamic property is stored
     */
    NodeRef getId();

    String getName();

    String getLanguages();
}
