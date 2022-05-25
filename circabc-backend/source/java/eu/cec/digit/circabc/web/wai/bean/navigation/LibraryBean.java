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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.customisation.nav.ColumnConfig;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs the navigation inside the Library Service
 *
 * @author yanick pignot
 */
public class LibraryBean extends InterestGroupBean {


    public static final String TARGET_GROUP_LINK_TO_SHARED = "targetGroupLinkToShared";

    public static final String FROM_LINK_TO_SHARED = "fromLinkToShared";

    public static final String LIBRARY_BULK_OPERATION_DIALOG_ICON_TOOLTIP = "library_bulk_operation_dialog_icon_tooltip";
    public static final String JSP_NAME = "library-home.jsp";
    public static final String BEAN_NAME = "LibraryBean";
    public static final String MSG_PAGE_DESCRIPTION = "library_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "library_icon_tooltip";
    public static final String MSG_SEARCH_PAGE_TITLE = "search_results_title";
    public static final String MSG_SEARCH_PAGE_ICON_ALT = "search_results_icon_tooltip";
    public static final String MSG_SEARCH_PAGE_DESCRIPTION = "search_results_text";
    public static final String MSG_SEARCH_PAGE_SUB_DESCRIPTION = "search_results_title_desc";
    public static final String MSG_LIBRARY_NUMBER_ELEMENTS = "library_number_of_elements";
    public static final String BULK_OPERATION_TOOLTIP = "library_bulk_operation_dialog_browser_title";
    protected static final String IMPORT_TOOLTIP = "import_tooltip";
    protected static final String IMPORT_BEAN_START = "ImportDialogWai.start";
    protected static final String IMPORT = "import";
    protected static final String LIBRARY_ADD_URL_TOOLTIP = "library_add_url_tooltip";
    protected static final String WAI_DIALOG_ADD_URL_WAI = "wai:dialog:addUrlWai";
    protected static final String LIBRARY_ADD_URL_ACTION = "library_add_url_action";
    private static final String WAI_DIALOG_LIBRARY_BULK_OPERATION_DIALOG = "wai:dialog:libraryBulkOperationDialog";
    private static final String MS = "ms";
    private static final String TIME_TO_QUERY_AND_BUILD_MAP_NODES = "Time to query and build map nodes: ";
    private static final String NEWSGROUPS_TOPIC_CREATE_POST_TOOLTIP = "newsgroups_topic_create_post_tooltip";
    private static final String WAI_DIALOG_CREATE_POST_WAI = "wai:dialog:createPostWai";
    private static final String NEWSGROUPS_TOPIC_CREATE_POST = "newsgroups_topic_create_post";
    private static final String NEWSGROUPS_FORUM_CREATE_TOPIC_TOOLTIP = "newsgroups_forum_create_topic_tooltip";
    private static final String WAI_DIALOG_CREATE_TOPIC_WAI = "wai:dialog:createTopicWai";
    private static final String NEWSGROUPS_FORUM_CREATE_TOPIC = "newsgroups_forum_create_topic";
    private static final String IMAGES_ICONS_SEARCH_RESULTS_LARGE_GIF = "/images/icons/search_results_large.gif";
    private static final String IMAGES_ICONS_SPACE_ICON_DOC_GIF = "/images/icons/space-icon-doc.gif";
    private static final String UNEXPECTED_ERROR = "Unexpected error:";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    private static final Log logger = LogFactory.getLog(LibraryBean.class);
    /**
     * Lists of container nodes for display
     */
    private List<NavigableNode> containers = null;
    /**
     * Lists of content nodes for display
     */
    private List<NavigableNode> contents = null;
    /**
     * The action to be displayed
     */
    private List<ActionWrapper> actions = null;

    private NodeRef sessionIgRef;


    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getCurrentNodeDescription() {
        if (getCurrentNode() == null) {
            return "";
        }

        if (getCurrentNode().getProperties() == null) {
            return "";
        }

        Object description = getCurrentNode().getProperties().get("description");
        return (description == null) ? "" : (String) description;
    }

    public String getPageDescription() {
        if (getSearchContext() == null) {
            return translate(MSG_PAGE_DESCRIPTION, getCurrentNode().getName());
        } else {
            return translate(MSG_SEARCH_PAGE_DESCRIPTION, getSearchContext().getText()) + "<br />"
                    + translate(MSG_SEARCH_PAGE_SUB_DESCRIPTION);
        }
    }

    public String getPageTitle() {
        if (getSearchContext() == null) {
            return translate("igroot_library");
        } else {
            return translate(MSG_SEARCH_PAGE_TITLE);
        }
    }

    public String getPageIcon() {
        if (getSearchContext() == null) {
            if (getCurrentNode().getProperties().get(ICON) != null) {
                return IMAGES_ICONS + getCurrentNode().getProperties().get(ICON) + GIF;
            } else {
                return IMAGES_ICONS_SPACE_ICON_DOC_GIF;
            }
        } else {
            return IMAGES_ICONS_SEARCH_RESULTS_LARGE_GIF;
        }
    }

    public String getPageIconAltText() {
        if (getSearchContext() == null) {
            return translate(MSG_PAGE_ICON_ALT);
        } else {
            return translate(MSG_SEARCH_PAGE_ICON_ALT);
        }
    }

    public void init(final Map<String, String> parameters) {
        this.actions = null;
        this.containers = null;
        this.contents = null;

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            Map<String, Object> session = fc.getExternalContext().getSessionMap();
            if (session != null && session.containsKey(FROM_LINK_TO_SHARED)) {

                this.sessionIgRef = getNodeService()
                        .getPrimaryParent((NodeRef) session.get(FROM_LINK_TO_SHARED)).getParentRef();

                // to know is we are leaving the shared space -> clean the session values

                NodeRef tmpRef = getManagementService().getCurrentInterestGroup(
                        (NodeRef) fc.getExternalContext().getSessionMap().get(TARGET_GROUP_LINK_TO_SHARED));
                if (tmpRef != null && this.getCurrentInterstGroup() != null) {
                    if (!tmpRef.equals(this.getCurrentInterstGroup().getNodeRef())) {
                        fc.getExternalContext().getSessionMap().remove(FROM_LINK_TO_SHARED);
                        fc.getExternalContext().getSessionMap().remove(TARGET_GROUP_LINK_TO_SHARED);

                        this.sessionIgRef = null;
                    }
                }
            }
        }

    }

    public String getGenerateBackLinkText() {

        return translate("share_space_helper_from_text", WebClientHelper
                .getGeneratedWaiRelativeUrl(new Node(sessionIgRef), ExtendedURLMode.HTTP_WAI_BROWSE));

    }

    @Override
    public void restored() {
        init(null);
    }

    /**
     * @return the list of available action for the current node that should be a Library root or
     * child
     */
    public List<ActionWrapper> getActions() {
        if (!FacesContext.getCurrentInstance().getViewRoot().getViewId()
                .equals(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE)) {
            // don't display actions when a dialog or a wizard is launched
            return null;
        }
        if (getSearchContext() != null) {
            // don't display actions when a search is performed
            return null;
        }

        actions = new ArrayList<>(6);

        final NavigableNodeType type = getNavigator().getCurrentNodeType();

        if (type.equals(NavigableNodeType.LIBRARY)
                || type.equals(NavigableNodeType.LIBRARY_SPACE)) {

            actions.add(
                    new ActionWrapper(
                            CREATE_CHILDREN,
                            translate(ADD_CONTENT),
                            null,
                            ADD_CONTENT_BEAN_START,
                            translate(ADD_CONTENT_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId()
                    )
            );
            actions.add(
                    new ActionWrapper(
                            CREATE_CHILDREN,
                            translate(CREATE_SPACE_ACTION),
                            WAI_DIALOG_CREATE_SPACE_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(CREATE_SPACE_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );
            // specific requirement from ECHA (remove the add url as 'alternative browse' is disabled)
            if (!CircabcConfig.ECHA) {
                actions.add(
                        new ActionWrapper(
                                CREATE_CHILDREN,
                                translate(LIBRARY_ADD_URL_ACTION),
                                WAI_DIALOG_ADD_URL_WAI,
                                DIALOG_MANAGER_SETUP_PARAMETERS,
                                translate(LIBRARY_ADD_URL_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId())
                );
            }
            actions.add(
                    new ActionWrapper(
                            CREATE_CHILDREN,
                            translate(IMPORT),
                            null,
                            IMPORT_BEAN_START,
                            translate(IMPORT_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId()
                    )
            );
            actions.add(
                    new ActionWrapper(
                            CREATE_CHILDREN,
                            translate(BULK_OPERATION_TOOLTIP),
                            WAI_DIALOG_LIBRARY_BULK_OPERATION_DIALOG,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(LIBRARY_BULK_OPERATION_DIALOG_ICON_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId()
                    )
            );
        } else if (type.equals(NavigableNodeType.LIBRARY_FORUM)
                || type.equals(NavigableNodeType.LIBRARY_ML_CONTENT_FORUM)) {
            actions.add(
                    new ActionWrapper(
                            CREATE_CHILDREN,
                            translate(NEWSGROUPS_FORUM_CREATE_TOPIC),
                            WAI_DIALOG_CREATE_TOPIC_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(NEWSGROUPS_FORUM_CREATE_TOPIC_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );
        } else if (type.equals(NavigableNodeType.LIBRARY_TOPIC)
                || type.equals(NavigableNodeType.LIBRARY_ML_CONTENT_TOPIC)) {
            actions.add(
                    new ActionWrapper(
                            NewsGroupBean.CREATE_POST,
                            translate(NEWSGROUPS_TOPIC_CREATE_POST),
                            WAI_DIALOG_CREATE_POST_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(NEWSGROUPS_TOPIC_CREATE_POST_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );
        } else {
            actions = null;
        }
        return actions;
    }

    public NavigationPreference getContainerNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.LIBRARY_SERVICE,
                NavigationPreferencesService.CONTAINER_TYPE);
    }

    public String getRenderPropertyNameNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(getCurrentNode().getNodeRef(),
                NavigationPreferencesService.LIBRARY_SERVICE, NavigationPreferencesService.CONTAINER_TYPE)
                .getRenderPropertyName();
    }

    public NavigationPreference getContentNavigationPreference() {
        NavigationPreference contentPreference = getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.LIBRARY_SERVICE,
                NavigationPreferencesService.CONTENT_TYPE);

        List<ColumnConfig> columns = contentPreference.getColumns();
        List<DynamicProperty> dynamicProperties = null;
        for (ColumnConfig columnConfig : columns) {
            if (columnConfig.isDynamicProperty()) {
                if (dynamicProperties == null) {
                    DynamicPropertyService dynamicPropertieService = Services
                            .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                            .getDynamicPropertieService();
                    dynamicProperties = dynamicPropertieService
                            .getDynamicProperties(getCurrentInterstGroup().getNodeRef());
                }
                int index = Integer.valueOf(columnConfig.getName().replace("dynAttr", ""));
                String dynamicPropertylabel = getDynamicPropertylabelByIndex(dynamicProperties, index);
                columnConfig.setLabel(dynamicPropertylabel);
            }
        }
        return contentPreference;
    }

    private String getDynamicPropertylabelByIndex(
            List<DynamicProperty> dynamicProperties, int index) {
        for (DynamicProperty dynamicProperty : dynamicProperties) {
            if (index == dynamicProperty.getIndex()) {
                return dynamicProperty.getLabel().getDefaultValue();
            }
        }
        return "";
    }

    /**
     * Get list of container nodes for the currentNodeRef
     *
     * @return The list of container nodes for the currentNodeRef
     */
    public List<NavigableNode> getContainers() {
        if (this.containers == null) {
            fillBrowseNodes(getNavigator().getCurrentNode());
        }

        return this.containers;
    }

    /***
     *  if null default return is 0
     * @return nNumber of contairs
     */
    public Integer getContainerCount() {
        if (this.containers == null) {
            fillBrowseNodes(getNavigator().getCurrentNode());
        }

        return this.containers.size();
    }

    /***
     *
     * @return Number of contents
     */
    public Integer getContentCount() {
        if (this.contents == null) {
            fillBrowseNodes(getNavigator().getCurrentNode());
        }

        return this.contents.size();
    }

    public String getSpaceNumberOfElements() {
        return translate(MSG_LIBRARY_NUMBER_ELEMENTS, getContainerCount(), getContentCount());
    }

    /**
     * Get list of content nodes for the currentNodeRef
     *
     * @return The list of content nodes for the currentNodeRef
     */
    public List<NavigableNode> getContents() {
        if (this.contents == null) {
            // user performs a simple browsing
            fillBrowseNodes(getNavigator().getCurrentNode());
        }

        return this.contents;
    }


    protected SearchContext getSearchContext() {
        return getNavigator().getSearchContext();
    }

    /**
     * Query a list of nodes for the specified parent node Id<br> Based on
     * BrowseBean.queryBrowseNodes()
     *
     * @param currentNode of the parent node or null for the root node
     */
    private void fillBrowseNodes(final Node currentNode) {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                try {
                    boolean canNotReadChildren = getPermissionService()
                            .hasPermission(currentNode.getNodeRef(), PermissionService.READ_CHILDREN)
                            == AccessStatus.DENIED;

                    final List<FileInfo> children;

                    if (canNotReadChildren) {

                        children = getCircabcFileFolderService().list(currentNode.getNodeRef());
                    } else {
                        children = getFileFolderService().list(currentNode.getNodeRef());
                    }
                    containers = new ArrayList<>(children.size());
                    contents = new ArrayList<>(children.size());
                    //NodeRef nodeRef;
                    QName type;
                    String userName = AuthenticationUtil.getRunAsUser();
                    try {
                        if (canNotReadChildren) {
                            AuthenticationUtil.setRunAsUserSystem();
                        }
                        for (final FileInfo fileInfo : children) {
                            type = getNodeService().getType(fileInfo.getNodeRef());
                            if (fileInfo.isLink()) {
                                //look for File Link object node
                                if (ApplicationModel.TYPE_FILELINK.equals(type)) {
                                    final NavigableNode node = ResolverHelper
                                            .createFileLinkRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                    if (node != null) {
                                        contents.add(node);
                                    }
                                } else if (ApplicationModel.TYPE_FOLDERLINK.equals(type)) {
                                    final NavigableNode node = ResolverHelper
                                            .createFolderLinkRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                    if (node != null) {
                                        containers.add(node);
                                    }
                                }
                            } else if (fileInfo.isFolder()) {
                                // hide discussions forum node
                                if (ForumModel.TYPE_FORUM.equals(type) == false) {
                                    final NavigableNode node = ResolverHelper
                                            .createFolderRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                    if (node != null) {
                                        containers.add(node);
                                    }
                                }
                            } else {
                                final NavigableNode node = ResolverHelper
                                        .createContentRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                if (node != null) {
                                    contents.add(node);
                                }
                            }
                        }
                    } finally {
                        AuthenticationUtil.setRunAsUser(userName);
                    }

                    // all is OK, mem the node id

                } catch (final InvalidNodeRefException refErr) {
                    Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()), refErr);
                    containers = Collections.emptyList();
                    contents = Collections.emptyList();

                } catch (final Throwable err) {
                    Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
                    containers = Collections.emptyList();
                    contents = Collections.emptyList();

                    if (logger.isErrorEnabled()) {
                        logger.error(UNEXPECTED_ERROR + err.getMessage(), err);
                    }
                }
                return null;
            }

        };
        txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug(TIME_TO_QUERY_AND_BUILD_MAP_NODES + (endTime - startTime) + MS);
        }
    }

    /**
     * @return the sessionIgRef
     */
    public NodeRef getSessionIgRef() {
        return sessionIgRef;
    }

    /**
     * @param sessionIgRef the sessionIgRef to set
     */
    public void setSessionIgRef(NodeRef sessionIgRef) {
        this.sessionIgRef = sessionIgRef;
    }

    public void closeBackLinkText() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            Map<String, Object> session = fc.getExternalContext().getSessionMap();
            if (session != null && session.containsKey(FROM_LINK_TO_SHARED)) {
                session.put(FROM_LINK_TO_SHARED, null);
                this.sessionIgRef = null;
            }
        }
    }


}
