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
 /*--+
     |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
     |
     |          http://ec.europa.eu/idabc/en/document/6523
     |
     +--*/

package eu.cec.digit.circabc.web.bean.override;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.authentication.MultifactorConfigurationService;
import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.app.context.UICircabcContextService;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.search.SearchContextDelegate;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType.*;

/**
 * Extension of the alfresco navigation bean that manage the specific navigation of Circabc.
 *
 * @author yanick pignot
 */
public class CircabcNavigationBean extends NavigationBean implements Serializable,
        IContextListener {

    private static final Log logger = LogFactory.getLog(CircabcNavigationBean.class);

    private static final String A_CLOSING_TAG = "</a>";

    private static final String A_HREF_CLOSE = "\">";

    private static final String TOOLTIP = "\" tooltip=\"";

    private static final String A_HREF = "<a href=\"";

    private static final String ECAS_REDIRECTION_PAGE_BUTTON_CAPTION = "ecas_redirection_page_button_caption";

    private static final String FACES_JSP_EXTENSION_WAI_NAVIGATION_CONTAINER_JSP = "/faces/jsp/extension/wai/navigation/container.jsp";

    private static final long serialVersionUID = 1L;
    NavigableNodeType currentNodeType = null;
    /**
     * The circabc management service reference
     */
    private transient ManagementService managementService;
    /**
     * The directory service reference
     */
    private transient DictionaryService dictionaryService;
    /**
     * The non-proxied nodeService (for performace issues)
     */
    private transient NodeService internalNodeService;
    /**
     * the Multilingual Content Service
     */
    private transient MultilingualContentService multilingualContentService;
    private transient ApplicationCustomisationService applicationCustomisationService;
    private Stack<String> visitedNodes = new Stack<>();
    private CircabcRootNode circabcHomeNode = null;
    private String memCatHeaderId = null;
    private String memCategoryId = null;
    private String memIgRootId = null;
    private String memIgServiceId = null;

    private CategoryHeaderNode currentCategoryHeader = null;
    private CategoryNode currentCategory = null;
    private InterestGroupNode currentIGRoot = null;
    private IGServicesNode currentIgService = null;

    private String userName;
    private String contactLink;
    private String sourceOrganisation;
    private String customBannerLink;
    private Boolean searchLinkDisplayed, legalLinkDisplayed;

    private MultifactorConfigurationService multifactorConfigurationService = null;

    public CircabcNavigationBean() {
        super();
        UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
    }

    /**
     * If the current current node is an Interest Group or any of its child, this method activate any
     * valid IG service.
     *
     * @see eu.cec.digit.circabc.web.repository.InterestGroupNode#VALID_SERVICES
     */
    public void activateIGService(final String service) {
        if (getCurrentIGRoot() != null) {
            if (isIgServiceValid(service)) {
                if (getCurrentIGService() != null) {
                    memIgServiceId = getCurrentIGService().getId();
                }

                this.currentIgService = (IGServicesNode) getCurrentIGRoot().get(service);
                this.currentNodeType = currentIgService.getNavigableNodeType();

                if (logger.isDebugEnabled()) {
                    logger.debug("activateIGService:" + service);
                }

                if (memIgServiceId != null && getCurrentIGService() != null && !currentIgService.getId()
                        .equals(memIgServiceId)) {
                    UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).igServiceChanged();
                }
            } else {
                throw new IllegalArgumentException(
                        "The service specified doesn't exist: " + service + ". It must be contains in " + Arrays
                                .toString(InterestGroupNode.VALID_SERVICES.toArray()));
            }
        } else {
            throw new IllegalStateException(
                    "You can't activate an Interest Group Service if the current node is not a Interest Group");
        }

    }

    /**
     * Return circabc container.
     */
    public String getCircabcUrl() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final String caption = WebClientHelper
                .translate(ECAS_REDIRECTION_PAGE_BUTTON_CAPTION, getAppName());
        final String tooltip = WebClientHelper
                .translate(ECAS_REDIRECTION_PAGE_BUTTON_CAPTION, getAppName());
        //final String requestContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        final String containerUrl = FACES_JSP_EXTENSION_WAI_NAVIGATION_CONTAINER_JSP;

        final String circabcUrl =
                A_HREF + WebClientHelper.getCircabcUrl() + containerUrl + TOOLTIP + tooltip + A_HREF_CLOSE
                        + caption + A_CLOSING_TAG;
        return circabcUrl;
    }

    /**
     * Return circabc home link.
     */
    public String getCircabcRootLink() {
        return WebClientHelper.getCircabcUrl();
    }

    /**
     * Return the circabc home node.
     *
     * @see org.alfresco.web.bean.NavigationBean#getCompanyHomeNode()
     */
    public CircabcRootNode getCircabcHomeNode() {
        if (this.circabcHomeNode == null) {
            if (getManagementService().getCircabcNodeRef() != null) {
                this.circabcHomeNode = new CircabcRootNode(
                        getManagementService().getCircabcNodeRef()
                );
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("The circabc root needs to be created first.");
                }

                throw new IllegalStateException(
                        "The application can't be running without the circabc root node.");
            }
        }

        return this.circabcHomeNode;
    }

    public boolean isMultifactorEnabled() {
        return multifactorConfigurationService.isMultifactorEnabled();
    }

    public boolean isEnterpriseEnabled() {
        return CircabcConfig.ENT;
    }

    public String getAppName() {
        return CircabcConfiguration.getApplicationName();
    }

    public String getSwitchNewUiLink() {
        return CircabcConfiguration.getSwitchNewUiLink();
    }

    public String getBannerContactLink() {
        return CircabcConfiguration.getProperty(CircabcConfiguration.BANNER_CONTACT_LINK);
    }

    @Override
    public void setCurrentNodeId(final String currentNodeId) {
        final String previousNodeId = super.getCurrentNodeId();
        if (!currentNodeId.equals(previousNodeId)) {
            {
                if (!visitedNodes.contains(previousNodeId) && previousNodeId != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("push previous Node:" + previousNodeId);
                    }
                    visitedNodes.push(previousNodeId);
                }
                while (visitedNodes.contains(currentNodeId)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("pop Node:" + currentNodeId);
                    }
                    visitedNodes.pop();
                }
            }
        }
        super.setCurrentNodeId(currentNodeId);
        if (logger.isDebugEnabled()) {
            logger.debug("setCurrentNodeId:" + currentNodeId);
        }
    }

    @Override
    public Node getCurrentNode() {
        if (getCurrentNodeId() == null) {
            return this.circabcHomeNode;
        }

        return super.getCurrentNode();

    }

    public Node getPreviousNode() {
        Node result = this.circabcHomeNode;

        {
            if (!visitedNodes.isEmpty()) {
                final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        visitedNodes.peek());
                result = new Node(nodeRef);
            }
        }
        return result;

    }

    public NavigableNode getCurrentIGRoot() {
        return currentIGRoot;
    }

    public NavigableNode getCurrentIGService() {
        return currentIgService;
    }

    public NavigableNode getCurrentCategory() {
        if (currentCategory == null && getCurrentIGRoot() != null) {
            currentCategory = (CategoryNode) currentIGRoot.getNavigableParent();
        }

        return currentCategory;
    }

    public NavigableNode getCurrentCategoryHeader() {
        if (currentCategoryHeader == null && getCurrentCategory() != null) {
            currentCategoryHeader = (CategoryHeaderNode) currentCategory.getNavigableParent();
        }

        return currentCategoryHeader;
    }

    public NavigableNodeType getCurrentNodeType() {
        return currentNodeType;
    }

    public String getCurrentUserName() {
        if (userName == null) {
            userName = PermissionUtils
                    .computeUserLogin(super.getCurrentUser().getPerson(), getNodeService());
        }
        return userName;
    }

    public String getCurrentAlfrescoUserName() {
        final User user = super.getCurrentUser();
        return user.getUserName();
    }

    @Override
    public List<IBreadcrumbHandler> getLocation() {
        List<NodeRef> parents = null;

        if (getCurrentNode() != null) {
            parents = getManagementService().getAllParents(getCurrentNode().getNodeRef(), false);
        }

        if (parents == null || parents.size() < 1) {
            return super.getLocation();
        } else {
            final List<IBreadcrumbHandler> elements = new ArrayList<>(parents.size());

            Serializable name;
            for (final NodeRef ref : parents) {
                name = getNodeService().getProperty(ref, ContentModel.PROP_NAME);
                elements.add(new NavigationBreadcrumbHandler(ref, (String) name));
            }

            return elements;
        }
    }

    public void areaChanged() {
        // nothing to do
    }

    public void contextUpdated() {
        // nothing to do
    }


    public void spaceChanged() {
        updateCircabcNavigationContext();
    }

    public boolean isGuest() {
        return getIsGuest();
    }

    public void updateCircabcNavigationContext() {
        final Node currentNode = getCurrentNode();

        // remeber the last visited node
        memCatHeaderId = (this.getCurrentCategoryHeader() == null) ? memCatHeaderId
                : getCurrentCategoryHeader().getId();
        memCategoryId =
                (this.getCurrentCategory() == null) ? memCategoryId : getCurrentCategory().getId();
        memIgRootId = (this.getCurrentIGRoot() == null) ? memIgRootId : getCurrentIGRoot().getId();
        memIgServiceId =
                (this.getCurrentIGService() == null) ? memIgServiceId : getCurrentIGService().getId();

        final boolean wasInCircabc = NavigableNodeType.NOT_CIRCABC_CHILD.equals(currentNodeType);

        currentNodeType = NavigableNodeType.NOT_CIRCABC_CHILD;

        // only reset circabchome node cache when user click on circabc
        if (currentNode.getNodeRef().equals(getCircabcHomeNode().getNodeRef())) {
            getCircabcHomeNode().resetCache();
        }

        // TODO we can optimize the cache...
        this.currentIGRoot = null;
        this.currentCategory = null;
        this.currentCategoryHeader = null;
        this.currentIgService = null;

        if (currentNode instanceof NavigableNode) {
            // it is not possible yet. But this way is interesting to explore how to force the current node
            // to become a NavigableNode without override all the code of the native original NavigationBean.
            this.currentNodeType = ((NavigableNode) currentNode).getNavigableNodeType();

            switch (currentNodeType) {
                case CIRCABC_ROOT:
                    this.circabcHomeNode = (CircabcRootNode) currentNode;
                    break;
                case CATEGORY_HEADER:
                    this.currentCategoryHeader = (CategoryHeaderNode) currentNode;
                    break;
                case CATEGORY:
                    this.currentCategory = (CategoryNode) currentNode;
                    break;
                case IG_ROOT:
                    this.currentIGRoot = (InterestGroupNode) currentNode;
                    break;
                default:
                    this.currentIgService = (IGServicesNode) currentNode;
                    break;
            }
        } else {
            this.currentNodeType = AspectResolver.resolveType(getCurrentNode());

            if (logger.isDebugEnabled()) {
                logger.debug("The requested node type for the browsing " + currentNodeType);
            }

            if (currentNodeType == null || NavigableNodeType.NOT_CIRCABC_CHILD.equals(currentNodeType)
                    || CIRCABC_ROOT.equals(currentNodeType)) {
                // nothing more to do.
            } else {

                switch (this.currentNodeType) {
                    case CATEGORY_HEADER:
                        this.currentCategoryHeader = new CategoryHeaderNode(currentNode.getNodeRef());
                        break;
                    case CATEGORY:
                        this.currentCategory = new CategoryNode(currentNode.getNodeRef());
                        break;
                    case IG_ROOT:
                        this.currentIGRoot = new InterestGroupNode(currentNode.getNodeRef());
                        this.currentCategory = (CategoryNode) this.currentIGRoot.getNavigableParent();
                        break;
                    default:
                        if (currentNodeType.isEqualsOrUnder(IG_ROOT)) {
                            final NodeRef igNodeRef = getManagementService()
                                    .getCurrentInterestGroup(currentNode.getNodeRef());

                            if (igNodeRef != null) {
                                if (getPermissionService().hasPermission(igNodeRef, PermissionService.READ)
                                        .equals(AccessStatus.ALLOWED)) {
                                    this.currentIGRoot = new InterestGroupNode(igNodeRef);
                                }

                                if (currentNodeType.isEqualsOrUnder(DIRECTORY)) {
                                    this.currentIgService = currentIGRoot.getDirectory();
                                } else {
                                    this.currentIgService = new IGServicesNode(currentNode.getNodeRef(),
                                            currentIGRoot);
                                }

                            }
                        }
                }
            }
        }

        // Inform the listener that the state has changed...
        if (NavigableNodeType.NOT_CIRCABC_CHILD.equals(currentNodeType) && wasInCircabc == true) {
            // we are not longer in circabc
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).circabcLeaved();
        } else if (NavigableNodeType.NOT_CIRCABC_CHILD.equals(currentNodeType)
                && wasInCircabc == false) {
            // we are just entered in circabc
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).circabcEntered();
        }

        if (memCatHeaderId != null && getCurrentCategoryHeader() != null && !currentCategoryHeader
                .getId().equals(memCatHeaderId)) {
            // we are just leaving
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance())
                    .categoryHeaderChanged();
        }
        if (memCategoryId != null && getCurrentCategory() != null && !currentCategory.getId()
                .equals(memCategoryId)) {
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).categoryChanged();
        }
        if (memIgRootId != null && getCurrentIGRoot() != null && !currentIGRoot.getId()
                .equals(memIgRootId)) {
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).igRootChanged();
        }
        if (memIgServiceId != null && getCurrentIGService() != null && !currentIgService.getId()
                .equals(memIgServiceId)) {
            UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).igServiceChanged();
        }
    }

    @Override
    public void setSearchContext(SearchContext searchContext) {
        if (searchContext instanceof SearchContextDelegate) {
            super.setSearchContext(searchContext);
        } else {
            super.setSearchContext(new SearchContextDelegate(searchContext));
        }

    }


    private boolean isIgServiceValid(final String service) {
        boolean valid = false;

        if (service != null) {
            for (final String srv : InterestGroupNode.VALID_SERVICES) {
                if (srv.equals(service)) {
                    valid = true;
                    break;
                }
            }
        }

        return valid;
    }

    /**
     * @return the ManagementService
     */
    protected final ManagementService getManagementService() {
        if (managementService == null) {
            managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
        }
        return managementService;
    }

    /**
     * @param managementService the ManagementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }


    /**
     * @return the dictionaryService
     */
    protected final DictionaryService getDictionaryService() {
        if (dictionaryService == null) {
            dictionaryService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getDictionaryService();
        }
        return dictionaryService;
    }


    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setDictionaryService(final DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the dictionaryService
     */
    protected final ApplicationCustomisationService getApplicationCustomisationService() {
        return applicationCustomisationService;
    }


    /**
     * @param applicationCustomisationService the applicationCustomisationService to set
     */
    public final void setApplicationCustomisationService(
            final ApplicationCustomisationService applicationCustomisationService) {
        this.applicationCustomisationService = applicationCustomisationService;
    }


    /**
     * @return the internalNodeService
     */
    @Deprecated
    protected final NodeService getInternalNodeService() {
        if (internalNodeService == null) {
            internalNodeService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredNodeService();
        }
        return this.internalNodeService;
    }


    /**
     * @param internalNodeService the internalNodeService to set
     */
    public final void setInternalNodeService(final NodeService internalNodeService) {
        this.internalNodeService = internalNodeService;
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
            final MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * Sets the value of the multifactorConfigurationService
     *
     * @param multifactorConfigurationService the multifactorConfigurationService to set.
     */
    public void setMultifactorConfigurationService(
            MultifactorConfigurationService multifactorConfigurationService) {
        this.multifactorConfigurationService = multifactorConfigurationService;
    }

    /**
     * @return the sourceOrganisation
     */
    public String getSourceOrganisation() {

        if (sourceOrganisation == null) {
            sourceOrganisation = "DIGIT";
        }

        return sourceOrganisation;
    }

    /**
     * @return the contactLink
     */
    public String getContactLink() {

        contactLink = applicationCustomisationService.getContactlink();
        return contactLink;
    }

    /**
     * @param contactLink the contactLink to set
     */
    public void setContactLink(String contactLink) {
        this.contactLink = contactLink;
    }

    public boolean isEchaEnabled() {
        return CircabcConfig.ECHA;
    }

    /**
     * @return the customBannerLink
     */
    public String getCustomBannerLink() {

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {
                if (applicationCustomisationService.getBannerLogoRef() != null) {
                    customBannerLink = WebClientHelper
                            .getGeneratedWaiUrl(applicationCustomisationService.getBannerLogoRef(),
                                    ExtendedURLMode.HTTP_DOWNLOAD, true);
                } else {
                    customBannerLink = null;
                }

                return customBannerLink;

            }
        }, AuthenticationUtil.getAdminUserName());

        return customBannerLink;
    }

    /**
     * @param customBannerLink the customBannerLink to set
     */
    public void setCustomBannerLink(String customBannerLink) {
        this.customBannerLink = customBannerLink;
    }

    /**
     * @return the searchLinkDisplayed
     */
    public Boolean getSearchLinkDisplayed() {

        searchLinkDisplayed = applicationCustomisationService.getDisplaySearchLink();

        return searchLinkDisplayed;
    }

    /**
     * @param searchLinkDisplayed the searchLinkDisplayed to set
     */
    public void setSearchLinkDisplayed(Boolean searchLinkDisplayed) {
        this.searchLinkDisplayed = searchLinkDisplayed;
    }

    /**
     * @return the legalLinkDisplayed
     */
    public Boolean getLegalLinkDisplayed() {

        legalLinkDisplayed = applicationCustomisationService.getDisplayLegalLink();

        return legalLinkDisplayed;
    }

    /**
     * @param legalLinkDisplayed the legalLinkDisplayed to set
     */
    public void setLegalLinkDisplayed(Boolean legalLinkDisplayed) {
        this.legalLinkDisplayed = legalLinkDisplayed;
    }

}
