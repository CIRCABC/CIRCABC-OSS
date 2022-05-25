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
package eu.cec.digit.circabc.web.wai.bean.navigation.library;

import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.news.ForumBean;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Bean that backs the navigation inside the forums of the library service
 *
 * @author yanick pignot
 */
public class DiscussionForumBean extends ForumBean {

    public static final String BEAN_NAME = "LibForumBean";
    private static final long serialVersionUID = -4789102671156849286L;
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(DiscussionForumBean.class);
    private transient MultilingualContentService multilingualContentService;
    private LibraryBean libraryBean;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_FORUM;
    }

    @Override
    public List<ActionWrapper> getActions() {
        return getLibraryBean().getActions();
    }

    /**
     * Get the list of topic nodes for the current forum
     *
     * @return List of topic nodes for the current forum
     */
    @Override
    public List<NavigableNode> getTopics() {
        return super.getTopics();
    }

    @Override
    public NavigationPreference getTopicNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.LIBRARY_SERVICE,
                NavigationPreferencesService.DISCUSSION_TYPE);
    }

    public boolean isSubForumAllowed() {
        return false;
    }

    /**
     * @return the multilingualContentService
     */
    protected final MultilingualContentService getMultilingualContentService() {
        if (multilingualContentService == null) {
            multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
        }
        return multilingualContentService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public final void setMultilingualContentService(
            MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @return the libraryBean
     */
    protected final LibraryBean getLibraryBean() {
        if (libraryBean == null) {
            libraryBean = (LibraryBean) Beans.getWaiBrosableBean(LibraryBean.BEAN_NAME);
        }
        return libraryBean;
    }

    /**
     * @param libraryBean the libraryBean to set
     */
    public final void setLibraryBean(LibraryBean libraryBean) {
        this.libraryBean = libraryBean;
    }


}
