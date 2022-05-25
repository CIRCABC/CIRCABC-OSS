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
package eu.cec.digit.circabc.service.customisation.nav;

import java.util.List;

/**
 * The base representation of a service navigation customisation.
 *
 * @author Yanick Pignot
 */
public interface ServiceConfig {

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the type
     */
    String getType();

    /**
     * @return the actions
     */
    List<String> getActions();

    /**
     * @return the columns
     */
    List<ColumnConfig> getColumns();

    /**
     * @return the columns
     */
    List<ColumnConfig> getKeyColumns();

    /**
     * @return the displayActionMax
     */
    int getDisplayActionMax();

    /**
     * @return the displayActionMin
     */
    int getDisplayActionMin();

    /**
     * @return the displayColMax
     */
    int getDisplayColMax();

    /**
     * @return the displayColMin
     */
    int getDisplayColMin();

    /**
     * @return the displayRowMax
     */
    int getDisplayRowMax();

    /**
     * @return the displayRowMin
     */
    int getDisplayRowMin();

    /**
     * @return the actionConfigName
     */
    String getActionConfigName();

    /**
     * @return the mandatory action
     */
    String getMandatoryAction();

    /**
     * @return is Bulk Operation Allowed
     */
    boolean isBulkOperationAllowed();
}
