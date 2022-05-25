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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.filefolder.CircabcFileFolderService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.bean.repository.Node;
import org.owasp.esapi.ESAPI;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Base bean that backs the navigation inside the wai part of Circabc
 *
 * @author yanick pignot
 */
public abstract class BaseWaiNavigator implements WaiNavigator {

    public static final String NAVIGATION_JSP_FOLDER = "/jsp/extension/wai/navigation/";
    private static final long serialVersionUID = 8465549602453406044L;
    // bean common to most dialogs
    private CircabcBrowseBean browseBean;
    private CircabcNavigationBean navigator;
    // services common to most dialogs
    transient private ManagementService managementService;
    transient private NodeService nodeService;
    transient private FileFolderService fileFolderService;
    transient private SearchService searchService;
    transient private DictionaryService dictionaryService;
    transient private NamespaceService namespaceService;
    transient private PermissionService permissionService;
    transient private ProfileManagerServiceFactory profileManagerServiceFactory;
    transient private NavigationPreferencesService navigationPreferencesService;

    private CircabcFileFolderService circabcFileFolderService;

    private transient BusinessRegistry businessRegistry;

    private Map<String, String> parameters;

    //public BaseWaiNavigator(){UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);}

    public boolean getInit() {
        init(parameters);
        return false;
    }

    public void setParamsToApply(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Node getCurrentNode() {
        return navigator.getCurrentNode();
    }

    public Node getCurrentInterstGroup() {
        return navigator.getCurrentIGRoot();
    }

    public Node getCurrentCategory() {
        return navigator.getCurrentCategory();
    }

    public void areaChanged() {
        // TODO do somtehing generic?
    }

    public void contextUpdated() {
        // TODO do somtehing generic?
    }

    public void spaceChanged() {
        // TODO do somtehing generic?
    }


    public String getPageIcon() {
        // the most of the pages doesn't need an icon
        return null;
    }

    public String getPageIconAltText() {
        //	the most of the pages doesn't need an icon ...
        return null;
    }

    public void restored() {

    }

    public String getHttpUrl() {
        return WebClientHelper
                .getGeneratedWaiRelativeUrl(getCurrentNode(), ExtendedURLMode.HTTP_WAI_BROWSE);
    }

    public String getPreviousHttpUrl() {
        return WebClientHelper
                .getGeneratedWaiRelativeUrl(getPreviousNode(), ExtendedURLMode.HTTP_WAI_BROWSE);
    }

    private Node getPreviousNode() {
        return navigator.getPreviousNode();
    }

    public String getWebdavUrl() {
        // Functionally not asked but ready to run. Only uncomment the following line
        //return WebClientHelper.getGeneratedWaiRelativeUrl(getCurrentNode(), ExtendedURLMode.WEBDAV);

        return null;
    }

    public String getBrowserTitle() {
        String titleOrName = null;

        final String propertyTitle = ContentModel.PROP_TITLE.toString();
        final Object title = getCurrentNode().getProperties().get(propertyTitle);
        if (title != null) {
            ESAPI.encoder().encodeForHTML(titleOrName = (title.toString().length() > 0 ? title.toString()
                    : getCurrentNode().getName()));
        }

        return titleOrName;
    }

    public int getListPageSize() {
        return getBrowseBean().getListElementNumber();
    }

    protected String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    /**
     * @return the navigator
     */
    protected final CircabcNavigationBean getNavigator() {
        if (this.navigator == null) {
            this.navigator = Beans.getWaiNavigator();
        }
        return navigator;
    }

    /**
     * @param navigator the navigator to set
     */
    public final void setNavigator(CircabcNavigationBean navigator) {
        this.navigator = navigator;
    }

    /**
     * @return the browse bean
     */
    protected final CircabcBrowseBean getBrowseBean() {
        if (this.browseBean == null) {
            this.browseBean = Beans.getWaiBrowseBean();
        }
        return this.browseBean;
    }

    /**
     * @param browseBean The BrowseBean to set.
     */
    public final void setBrowseBean(CircabcBrowseBean browseBean) {
        this.browseBean = browseBean;
    }

    protected final ManagementService getManagementService() {
        if (this.managementService == null) {
            this.managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
        }
        return this.managementService;
    }

    /**
     * @param managementService The Management Service to set.
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    protected final NodeService getNodeService() {
        if (this.nodeService == null) {
            this.nodeService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getNodeService();
        }
        return this.nodeService;
    }

    /**
     * @param nodeService The nodeService to set.
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    protected final FileFolderService getFileFolderService() {
        if (this.fileFolderService == null) {
            this.fileFolderService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getFileFolderService();
        }
        return this.fileFolderService;
    }

    /**
     * @param fileFolderService used to manipulate folder/folder model nodes
     */
    public final void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    protected final SearchService getSearchService() {
        if (this.searchService == null) {
            this.searchService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getSearchService();
        }
        return this.searchService;
    }

    /**
     * @param searchService the service used to find nodes
     */
    public final void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    protected final DictionaryService getDictionaryService() {
        if (this.dictionaryService == null) {
            this.dictionaryService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
        }
        return this.dictionaryService;
    }

    /**
     * Sets the dictionary service
     *
     * @param dictionaryService the dictionary service
     */
    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    protected final NamespaceService getNamespaceService() {
        if (this.namespaceService == null) {
            this.namespaceService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getNamespaceService();
        }
        return this.namespaceService;
    }

    /**
     * @param namespaceService The NamespaceService
     */
    public final void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    protected final PermissionService getPermissionService() {
        if (this.permissionService == null) {
            this.permissionService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
        }
        return this.permissionService;
    }

    /**
     * @param permissionService The permission Service to set.
     */
    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    protected final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        if (profileManagerServiceFactory == null) {
            profileManagerServiceFactory = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getProfileManagerServiceFactory();
        }
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the navigationPreferencesService
     */
    protected final NavigationPreferencesService getNavigationPreferencesService() {
        if (navigationPreferencesService == null) {
            navigationPreferencesService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNavigationPreferencesService();
        }
        return navigationPreferencesService;
    }

    /**
     * @param navigationPreferencesService the navigationPreferencesService to set
     */
    public final void setNavigationPreferencesService(
            NavigationPreferencesService navigationPreferencesService) {
        this.navigationPreferencesService = navigationPreferencesService;
    }

    /**
     * @return the businessRegistry
     */
    protected final BusinessRegistry getBusinessRegistry() {
        if (businessRegistry == null) {
            businessRegistry = Services.getBusinessRegistry(FacesContext.getCurrentInstance());
        }
        return businessRegistry;
    }

    /**
     * @param businessRegistry the businessRegistry to set
     */
    public final void setBusinessRegistry(BusinessRegistry businessRegistry) {
        this.businessRegistry = businessRegistry;
    }

    public CircabcFileFolderService getCircabcFileFolderService() {
        return circabcFileFolderService;
    }

    public void setCircabcFileFolderService(CircabcFileFolderService circabcFileFolderService) {
        this.circabcFileFolderService = circabcFileFolderService;
    }

}
