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

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * The base representation of a navigation customization.
 *
 * @author Yanick Pignot
 */
public interface NavigationPreference {

    String BROWSE_LINK_ACTION = "browse";
    String DOWNLOAD_LINK_ACTION = "download";

    Integer MAX_LIST_SIZE = 250;
    Integer DEFAULT_LIST_SIZE = 10;
    Boolean DEFAULT_VIEW_ACTIONS = Boolean.TRUE;
    Boolean DEFAULT_SORT_DESC = Boolean.FALSE;
    String DEFAULT_LINK_ACTION = BROWSE_LINK_ACTION;

    /**
     * @return the service
     */
    ServiceConfig getService();

    /**
     * @return the actions
     */
    List<String> getActions();

    /**
     * @return the columns
     */
    List<ColumnConfig> getColumns();

    /**
     * @return the listSize
     */
    Integer getListSize();

    /**
     * @return the link target
     */
    String getLinkTarget();

    /**
     * @return if the action column is displayed
     */
    Boolean isDisplayActionColumn();

    /**
     * @return the initialSortColumn
     */
    ColumnConfig getInitialSortColumn();

    /**
     * @return the initialSortDescending
     */
    Boolean isInitialSortDescending();

    /**
     * @return the initialSortPostsDescending
     */
    Boolean isInitialSortPostsDescending();

    /**
     * @return the viewMode
     */
    String getViewMode();

    /**
     * @return the previewAction
     */
    String getPreviewAction();

    /**
     * @return the customizedOn
     */
    NodeRef getCustomizedOn();

    /**
     * *
     *
     * @return the property used to be displayed in navigation list
     */
    String getRenderPropertyName();
}
