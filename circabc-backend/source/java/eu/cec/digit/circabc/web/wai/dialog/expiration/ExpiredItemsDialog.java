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
package eu.cec.digit.circabc.web.wai.dialog.expiration;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.ui.common.component.data.UIRichList;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.*;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIModeList;
import org.apache.lucene.queryParser.QueryParser;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backing bean for the "Show Expired Items" and "Delete Expired Items" dialog. The UI is
 * represented by the file showExpiredItemsDialog.jsp and deleteExpiredItemsDialog.jsp
 *
 * @author makz
 */
public class ExpiredItemsDialog extends BaseWaiDialog implements IContextListener {

    private static final long serialVersionUID = 1L;

    private static final String MSG_DELETED_ITEMS_SGL = "delete_expired_items_dialog_success_single";
    private static final String MSG_DELETED_ITEMS = "delete_expired_items_dialog_success";
    private static final String MSG_CLOSE = "close";

    private static final String FILTER_DATE_ALL = "all";
    private static final String FILTER_DATE_TODAY = "today";
    private static final String FILTER_DATE_WEEK = "week";
    private static final String FILTER_DATE_MONTH = "month";

    private static final String PROP_NAME = Repository.escapeQName(ContentModel.PROP_NAME);
    private static final String PROP_EXPIRATION_DATE = Repository
            .escapeQName(DocumentModel.PROP_EXPIRATION_DATE);

    private static final String QUERY_SEARCH_All = "PARENT:\"%s\"";
    private static final String QUERY_SEARCH_NAME =
            QUERY_SEARCH_All + " AND @" + PROP_NAME + ":\"%s\"";
    private static final String QUERY_SEARCH_TEXT = QUERY_SEARCH_All + " AND TEXT:\"%s\"";

    //***************************************************************
    //                                           DIALOG OVERRIDES ETC
    //***************************************************************
    private NodePropertyResolver resolverPath = new NodePropertyResolver() {
        private static final long serialVersionUID = -2501720368642759082L;

        public Object get(Node node) {
            Path path = getNodeService().getPath(node.getNodeRef());
            Path relativePath;
            if (path.size() > 5) {
                relativePath = path.subPath(5, path.size() - 1);
            } else {
                relativePath = path;
            }
            return relativePath;
        }
    };
    private NodePropertyResolver resolverFileType16 = new NodePropertyResolver() {
        private static final long serialVersionUID = 7462526266770371703L;

        public Object get(Node node) {
            return FileTypeImageUtils.getFileTypeImage(node.getName(), true);
        }
    };
    private NodePropertyResolver resolverSmallIcon = new NodePropertyResolver() {
        private static final long serialVersionUID = 5528945140207247127L;

        @SuppressWarnings("unchecked")
        public Object get(Node node) {
            QNameNodeMap<String, String> props = (QNameNodeMap<String, String>) node.getProperties();
            String icon = (String) props.getRaw("app:icon");
            return "/images/icons/"
                    + (icon != null ? icon + "-16.gif"
                    : BrowseBean.SPACE_SMALL_DEFAULT + ".gif");
        }
    };
    private NodePropertyResolver resolverAuthor = new NodePropertyResolver() {
        private static final long serialVersionUID = 9178556770343499694L;

        public Object get(Node node) {
            return node.getProperties().get(ContentModel.PROP_AUTHOR.toString());
        }
    };
    private NodePropertyResolver resolverExpirationDate = new NodePropertyResolver() {
        private static final long serialVersionUID = 9178556770343499694L;

        public Object get(Node node) {
            return node.getProperties().get(DocumentModel.PROP_EXPIRATION_DATE.toString());
        }
    };
    private NodePropertyResolver resolverIsFolder = new NodePropertyResolver() {
        private static final long serialVersionUID = -9181535522349485509L;

        public Object get(Node node) {
            return getDictionaryService().isSubClass(node.getType(), ContentModel.TYPE_FOLDER);
        }
    };
    private NodePropertyResolver resolverIsMultilingualContainer = new NodePropertyResolver() {
        private static final long serialVersionUID = -9181535522349485509L;

        public Object get(Node node) {
            return getNodeService()
                    .hasAspect(node.getNodeRef(), ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }
    };
    private NodePropertyResolver resolverName = new NodePropertyResolver() {
        private static final long serialVersionUID = 9178556770343499694L;

        public Object get(Node node) {
            return node.getProperties().get(ContentModel.PROP_NAME.toString());
        }
    };
    private List<MapNode> confirmationItems;

    //***************************************************************
    //                                                ACTION LISTENER
    //***************************************************************
    private HtmlDataTable confirmationTable;
    private List<Node> items;
    private String searchText;
    private boolean fullTextSearch;
    private String dateFilter = FILTER_DATE_ALL;

    //***************************************************************
    //                                              PROPERTY RESOLVER
    //***************************************************************
    private UIRichList itemsRichList;
    private String currentSpaceNode;
    private String currentSpacePath;
    transient private NodeService internalNodeService;
    transient private SearchService internalSearchService;

    @SuppressWarnings("unchecked")
    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException(
                        "Node id is a mandatory parameter");
            }

            NodeRef nodeRef = getRelevantNode();

            if (nodeRef != null) {
                this.setCurrentSpaceNode(nodeRef.getId());
                this.setCurrentSpacePath(getNodeService().getPath(nodeRef).toString());

                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.getSessionMap().put("currentSpaceNode", getCurrentSpaceNode());
                context.getSessionMap().put("currentSpacePath", getCurrentSpacePath());
            }
        }

        // clear the item lists
        this.getItems().clear();

        // show all expired items
        searchAll(null);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        for (MapNode n : this.getConfirmationItems()) {
            // delete the node from repository
            this.getNodeService().deleteNode(n.getNodeRef());
        }

        // add a status message
        if (this.getConfirmationItems().size() > 1) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(MSG_DELETED_ITEMS, this.getConfirmationItems().size()));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_DELETED_ITEMS_SGL));
        }

        // clear the item lists
        this.getItems().clear();

        // show all expired items
        searchAll(null);

        return outcome;
    }

    @Override
    public String cancel() {
        String outcome = super.cancel();
        return outcome;
    }

    //***************************************************************
    //                                                         HELPER
    //***************************************************************

    @Override
    public String getCancelButtonLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }

    public void contextUpdated() {
        // nothing to do
    }

    public String getBrowserTitle() {
        return null;
    }

    public String getPageIconAltText() {
        return null;
    }

    //***************************************************************
    //                                              GETTER AND SETTER
    //***************************************************************

    public void areaChanged() {
        // nothing to do
    }

    public void spaceChanged() {
        // nothing to do
    }

    /**
     * Search the deleted item store by name
     */
    public void searchName(ActionEvent event) {
        this.setFullTextSearch(false);
        refreshListedItems();
    }

    /**
     * Search the deleted item store by text
     */
    public void searchContent(ActionEvent event) {
        this.setFullTextSearch(true);
        refreshListedItems();
    }

    /**
     * Action handler to show all items
     */
    public void searchAll(ActionEvent event) {
        this.setSearchText(null);
        refreshListedItems();
    }

    /**
     * Action handler called when the Date filter is changed by the user
     */
    public void dateFilterChanged(ActionEvent event) {
        UIModeList filterComponent = (UIModeList) event.getComponent();
        this.setDateFilter(filterComponent.getValue().toString());
        contextUpdated();
        refreshListedItems();
    }

    /**
     * Handler that is called when the confirmation step is initialized.
     */
    public void setupConfirmation(ActionEvent event) {
        // clear the confirmation list
        this.getConfirmationItems().clear();

        //identify the action
        String componentId = event.getComponent().getId();
        switch (componentId) {
            case "action-link-delete-item":
                @SuppressWarnings("rawtypes")
                Map params = FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap();
                if (params.containsKey("deleteNodeId")) {
                    String deleteNodeId = params.get("deleteNodeId").toString();
                    addNodeToConfirmationList(
                            new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, deleteNodeId));
                }
                break;
            case "action-link-delete-multilingual-item":

                break;
            case "action-link-delete-all-items":
                for (Node n : getItems()) {
                    addNodeToConfirmationList(n.getNodeRef());
                }
                break;
        }
    }

    private void addNodeToConfirmationList(NodeRef ref) {
        MapNode node = new MapNode(ref, getInternalNodeService(), false);
        node.addPropertyResolver("name", resolverName);
        node.addPropertyResolver("expirationDate", resolverExpirationDate);
        node.addPropertyResolver("author", resolverAuthor);
        this.getConfirmationItems().add(node);
    }

    /**
     * Starts a new Lucene query with the current filter and search settings.
     * <p>
     * It updates the item list and the RichList control that displays the items.
     */
    public void refreshListedItems() {
        UserTransaction tx = null;
        ResultSet results = null;
        List<Node> itemNodes = Collections.emptyList();
        try {
            AuthenticationUtil.setRunAsUserSystem();
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
            tx.begin();

            // fire the query to find the items
            final String query = buildSearchQuery();
            results = getInternalSearchService().query(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_LUCENE,
                    query);

            if (results != null && results.length() != 0) {
                // create a new list to store the nodes
                itemNodes = new ArrayList<>(results.length());
                for (final ResultSetRow row : results) {
                    NodeRef nodeRef = row.getNodeRef();

                    // If the node does not exist anymore because it was
                    // deleted in this transaction, but it seems that Alfresco
                    // didn't remove it from the indexes -> continue
                    if (!getInternalNodeService().exists(nodeRef)) {
                        continue;
                    }

                    MapNode node = new MapNode(nodeRef, getInternalNodeService(), false);
                    node.addPropertyResolver("path", resolverPath);
                    node.addPropertyResolver("expirationDate", resolverExpirationDate);
                    node.addPropertyResolver("author", resolverAuthor);
                    node.addPropertyResolver("isFolder", resolverIsFolder);
                    node.addPropertyResolver("isMultilingualContainer", resolverIsMultilingualContainer);

                    QName nodeType = getInternalNodeService().getType(nodeRef);
                    if (getDictionaryService().isSubClass(nodeType, ContentModel.TYPE_FOLDER) == true &&
                            getDictionaryService().isSubClass(nodeType, ContentModel.TYPE_SYSTEM_FOLDER)
                                    == false) {
                        node.addPropertyResolver("typeIcon", this.resolverSmallIcon);
                    } else {
                        node.addPropertyResolver("typeIcon", this.resolverFileType16);
                    }

                    itemNodes.add(node);
                }
            }

            tx.commit();
        } catch (final Throwable err) {
            Utils.addErrorMessage(MessageFormat.format(Application
                    .getMessage(FacesContext.getCurrentInstance(),
                            Repository.ERROR_GENERIC), err
                    .getMessage()), err);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (final Exception tex) {
            }
        } finally {
            if (results != null) {
                results.close();
            }

            //refresh the state
            setItems(itemNodes);

            //refresh the rich list control
            if (this.getItemsRichList() != null) {
                this.getItemsRichList().setValue(null);
            }
        }
    }

    /**
     * @return the search query to use when displaying the list of deleted items
     */
    private String buildSearchQuery() {
        String query;

        //NavigableNode igRootNode = this.getNavigator().getCurrentIGRoot();
        Node actionNode = this.getActionNode();
        String actionNodeRef = actionNode.getNodeRef().toString();

        // build the basic query
        if (this.getSearchText() == null || this.getSearchText().length() == 0) {
            // search all
            query = String.format(QUERY_SEARCH_All, actionNode.getNodeRef().toString());
        } else {
            final String safeText = QueryParser.escape(this.getSearchText());
            if (this.isFullTextSearch()) {
                // search text
                query = String.format(QUERY_SEARCH_TEXT, actionNodeRef, safeText);
            } else {
                // search for name
                query = String.format(QUERY_SEARCH_NAME, actionNodeRef, safeText);
            }
        }

        // search for expiration date
        String fromDate = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        switch (this.getDateFilter()) {
            case FILTER_DATE_TODAY:
                fromDate = df.format(cal.getTime());
                break;
            case FILTER_DATE_WEEK:
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                fromDate = df.format(cal.getTime());
                break;
            case FILTER_DATE_MONTH:
                cal.add(Calendar.MONTH, -1);
                fromDate = df.format(cal.getTime());
                break;
            default:
                fromDate = "MIN";
                break;
        }
        if (fromDate != null) {
            String buf = " AND @" +
                    PROP_EXPIRATION_DATE +
                    ":[" +
                    QueryParser.escape(fromDate) +
                    " TO NOW]";
            query += buf;
        }

        return query;
    }

    private NodeRef getRelevantNode() {
        final Node actionNode = getActionNode();
        NodeRef nodeRef = null;
        if (actionNode.hasAspect(CircabcModel.ASPECT_LIBRARY)
                || actionNode.hasAspect(CircabcModel.ASPECT_LIBRARY_ROOT)) {
            nodeRef = this.getManagementService().getCurrentLibrary(
                    actionNode.getNodeRef());
        }
        if (actionNode.hasAspect(CircabcModel.ASPECT_NEWSGROUP)
                || actionNode.hasAspect(CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
            nodeRef = this.getManagementService().getCurrentNewsGroup(
                    actionNode.getNodeRef());
        }
        return nodeRef;
    }

    public List<MapNode> getConfirmationItems() {
        if (confirmationItems == null) {
            confirmationItems = new ArrayList<>();
        }
        return confirmationItems;
    }

    public void setConfirmationItems(List<MapNode> list) {
        this.confirmationItems = list;
    }

    public HtmlDataTable getConfirmationTable() {
        return confirmationTable;
    }

    public void setConfirmationTable(HtmlDataTable table) {
        this.confirmationTable = table;
    }

    public List<Node> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<Node> items) {
        this.items = items;
    }

    public boolean isItemsListed() {
        if (getItems().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean isFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(boolean fullTextSearch) {
        this.fullTextSearch = fullTextSearch;
    }

    public String getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
    }

    public UIRichList getItemsRichList() {
        return itemsRichList;
    }

    public void setItemsRichList(UIRichList itemsRichList) {
        this.itemsRichList = itemsRichList;
    }

    public String getCurrentSpaceNode() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        this.currentSpaceNode = (String) context.getSessionMap().get("currentSpaceNode");
        return currentSpaceNode;
    }

    public void setCurrentSpaceNode(String nodeRef) {
        this.currentSpaceNode = nodeRef;
    }

    public String getCurrentSpacePath() {
        if (currentSpacePath == null) {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            currentSpacePath = (String) context.getSessionMap().get("currentSpacePath");
            if (currentSpacePath == null) {
                currentSpacePath = getActionNode().getNodePath().toString();
            }
        }
        return currentSpacePath;
    }

    public void setCurrentSpacePath(String path) {
        this.currentSpacePath = path;
    }

    public NodeService getInternalNodeService() {
        if (internalNodeService == null) {
            internalNodeService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredNodeService();
        }
        return this.internalNodeService;
    }

    public void setInternalNodeService(NodeService internalNodeService) {
        this.internalNodeService = internalNodeService;
    }

    public SearchService getInternalSearchService() {
        if (internalSearchService == null) {
            internalSearchService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredSearchService();
        }
        return internalSearchService;
    }

    public void setInternalSearchService(SearchService internalSearchService) {
        this.internalSearchService = internalSearchService;
    }
}
