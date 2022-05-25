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
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package eu.cec.digit.circabc.web.wai.dialog.trashcan;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import net.sf.acegisecurity.Authentication;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.*;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIModeList;
import org.apache.lucene.queryParser.QueryParser;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backing bean for the Manage Deleted Items (soft delete and archiving) pages.
 *
 * @author
 */
public class TrashcanDialog extends BaseWaiDialog implements IContextListener {

    /**
     *
     */
    private static final long serialVersionUID = -7399415656164875270L;
    private static final String MSG_DELETED_ITEMS_FOR = "deleted_items_for";
    private static final String MSG_DELETED_ITEMS = "deleted_items";
    private static final String MSG_RECOVERED_ITEM_INTEGRITY_S = "recovered_item_integrity_short";
    private static final String MSG_RECOVERED_ITEM_PERMISSION_S = "recovered_item_permission_short";
    private static final String MSG_RECOVERED_ITEM_PARENT_S = "recovered_item_parent_short";
    private static final String MSG_RECOVERED_ITEM_FAILURE_S = "recovered_item_failure_short";
    private static final String MSG_RECOVERY_REASON = "recovery_report_reason";
    private static final String MSG_LOCATION = "location";
    private static final String MSG_NAME = "name";
    private static final String MSG_CLOSE = "close";
    private static final String PROP_RECOVERSTATUS = "recoverstatus";
    private static final String FILTER_DATE_ALL = "all";
    private static final String FILTER_DATE_TODAY = "today";
    private static final String FILTER_DATE_WEEK = "week";
    private static final String FILTER_DATE_MONTH = "month";
    private static final String OUTCOME_DIALOGCLOSE = "wai:dialog:close";
    private static final String NAME_ATTR = Repository
            .escapeQName(ContentModel.PROP_NAME);
    private static final String DATE_ATTR = Repository
            .escapeQName(ContentModel.PROP_ARCHIVED_DATE);
    private static final String PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR = Repository
            .escapeQName(CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED);
    private static final String SEARCH_ALL = "PARENT:\"%s\" AND ASPECT:\"%s\""
            + "  AND @" + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR + ":\"%s\"";
    private static final String SEARCH_NAME = "PARENT:\"%s\" AND ASPECT:\"%s\" AND @"
            + NAME_ATTR
            + ":*%s*"
            + "  AND @"
            + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR + ":\"%s\"";
    private static final String SEARCH_TEXT = "PARENT:\"%s\" AND ASPECT:\"%s\" AND TEXT:%s"
            + "  AND @"
            + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR + ":\"%s\"";
    private static final String SEARCH_NAME_QUOTED = "PARENT:\"%s\" AND ASPECT:\"%s\" AND @"
            + NAME_ATTR
            + ":\"%s\""
            + "  AND @"
            + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR + ":\"%s\"";
    private static final String SEARCH_TEXT_QUOTED = "PARENT:\"%s\" AND ASPECT:\"%s\" AND TEXT:\"%s\""
            + "  AND @" + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR + ":\"%s\"";
    private static final String AT = "@";
    private static final String BRAKET = "[";
    private static final String TO = " TO ";
    private static final String AND = "] AND ";
    private static final char BLANK = ' ';
    private static final String SEMICOLON = ":";
    private static final String SLASH = "/";
    protected TrashcanDialogProperty property;
    transient private NodeService internalNodeService;
    transient private SearchService internalSearchService;
    private NodePropertyResolver resolverLocationPath = new NodePropertyResolver() {
        private static final long serialVersionUID = -2501720368642759082L;

        public Object get(Node node) {
            final ChildAssociationRef childRef =
                    (ChildAssociationRef) node.getProperties()
                            .get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC.toString());
            if (getNodeService().exists(childRef.getParentRef())) {
                final Path path = getNodeService().getPath(childRef.getParentRef());
                Path relativePath;
                if (path.size() > 5) {
                    relativePath = path.subPath(5, path.size() - 1);
                } else {
                    relativePath = path;
                }
                return relativePath;
            } else {
                return null;
            }
        }
    };
    private NodePropertyResolver resolverDisplayPath = new NodePropertyResolver() {
        private static final long serialVersionUID = 9178556770343499694L;

        public Object get(Node node) {
            ChildAssociationRef childRef = (ChildAssociationRef) node
                    .getProperties().get(
                            ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC.toString());
            if (getInternalNodeService().exists(childRef.getParentRef())) {
                return Repository.getDisplayPath(getInternalNodeService().getPath(
                        childRef.getParentRef()), true);
            } else {
                return "";
            }
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
            QNameNodeMap props = (QNameNodeMap) node.getProperties();
            String icon = (String) props.getRaw("app:icon");
            return "/images/icons/"
                    + (icon != null ? icon + "-16.gif"
                    : BrowseBean.SPACE_SMALL_DEFAULT + ".gif");
        }
    };
    private NodePropertyResolver resolverFileType32 = new TrashcanDialog.ResolverFileType32();
    private NodePropertyResolver resolverLargeIcon = new TrashcanDialog.ResolverLargeIcon();
    private NodePropertyResolver resolverMimetype = new TrashcanDialog.ResolverMimetype();
    private NodePropertyResolver resolverSize = new TrashcanDialog.ResolverSize();
    private NodePropertyResolver resolverEncoding = new TrashcanDialog.ResolverEncoding();
    private NodePropertyResolver resolverDeletedDate = new TrashcanDialog.ResolverDeletedDate();
    private NodePropertyResolver resolverDeletedBy = new TrashcanDialog.ResolverDeletedBy();
    private NodePropertyResolver resolverIsFolder = new NodePropertyResolver() {
        private static final long serialVersionUID = -9181535522349485509L;

        public Object get(Node node) {
            return getDictionaryService().isSubClass(node.getType(),
                    ContentModel.TYPE_FOLDER);
        }
    };
    // Added methods
    private String currentSpaceNode;
    private String currentSpacePath;

    public TrashcanDialogProperty getProperty() {
        return property;
    }

    public void setProperty(TrashcanDialogProperty property) {
        this.property = property;
    }

    /**
     * @return Message to display in the title of the panel area
     */
    public String getPanelMessage() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String msg = Application.getMessage(fc, MSG_DELETED_ITEMS);
        if (isAdminUser() == false) {
            msg = msg
                    + ' '
                    + MessageFormat.format(Application.getMessage(fc,
                    MSG_DELETED_ITEMS_FOR), Application.getCurrentUser(
                    fc).getUserName());
        }
        return msg;
    }

    /**
     * Returns the URL to the content for the current document item
     *
     * @return Content url to the current document item
     */
    public String getItemBrowserUrl() {
        return Utils.generateURL(FacesContext.getCurrentInstance(), property
                .getItem(), URLMode.HTTP_INLINE);
    }

    /**
     * Returns the download URL to the content for the current document item
     *
     * @return Download url to the current document item
     */
    public String getItemDownloadUrl() {
        return Utils.generateURL(FacesContext.getCurrentInstance(), property
                .getItem(), URLMode.HTTP_DOWNLOAD);
    }

    /**
     * Return the Alfresco NodeRef URL for the current item node
     *
     * @return the Alfresco NodeRef URL
     */
    public String getItemNodeRefUrl() {
        return property.getItem().getNodeRef().toString();
    }

    /**
     * @return HTML table of the listed items
     */
    public String getListedItemsTable() {
        return buildItemsTable(property.getListedItems(), "recoveredItemsList",
                false, true);
    }

    /**
     * @return HTML table of the items successfully recovered
     */
    public String getSuccessItemsTable() {
        return buildItemsTable(property.getSuccessItems(),
                "recoveredItemsList", false, false);
    }

    /**
     * @return HTML table of the items that failed to recover
     */
    public String getFailureItemsTable() {
        return buildItemsTable(property.getFailureItems(), "failedItemsList",
                true, false);
    }

    /**
     * @return the list of deleted items to display
     */
    public List<Node> getItems() {
        // to get deleted items from deleted items store
        // use a search to find the items - also filters by name/username
        List<Node> itemNodes = null;

        UserTransaction tx = null;
        ResultSet results = null;

        final String currentNodePath = getCurrentSpacePath() + SLASH;
        try {

            AuthenticationUtil.setRunAsUserSystem();
            tx = Repository.getUserTransaction(FacesContext
                    .getCurrentInstance(), true);
            tx.begin();

            // get the root node to the deleted items store
            if (getArchiveRootRef() != null && property.isShowItems()) {

                final String query = buildSearchQuery();
                final SearchParameters sp = new SearchParameters();
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery(query);
                sp.addStore(getArchiveRootRef().getStoreRef()); // the Archived Node store

                results = getInternalSearchService().query(sp);
                itemNodes = new ArrayList<>(results.length());
            }

            if (results != null && results.length() != 0) {
                ChildAssociationRef childRef;
                String originalNodePath;
                for (final ResultSetRow row : results) {
                    NodeRef nodeRef = row.getNodeRef();
                    childRef = (ChildAssociationRef) getInternalNodeService()
                            .getProperty(nodeRef, ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
                    if (getInternalNodeService().exists(childRef.getParentRef())) {
                        originalNodePath =
                                getInternalNodeService().getPath(childRef.getParentRef()).toString() + SLASH;
                        if (originalNodePath.startsWith(currentNodePath)) {
                            if (getInternalNodeService().exists(nodeRef)) {
                                originalNodePath =
                                        getInternalNodeService().getPath(childRef.getParentRef()).toString() + SLASH;
                                QName type = getInternalNodeService().getType(nodeRef);

                                MapNode node = new MapNode(nodeRef, getInternalNodeService(),
                                        false);

                                node.addPropertyResolver("locationPath",
                                        resolverLocationPath);
                                node.addPropertyResolver("displayPath",
                                        resolverDisplayPath);
                                node.addPropertyResolver("deletedDate",
                                        resolverDeletedDate);
                                node
                                        .addPropertyResolver("deletedBy",
                                                resolverDeletedBy);
                                node.addPropertyResolver("isFolder", resolverIsFolder);

                                if (getDictionaryService().isSubClass(type,
                                        ContentModel.TYPE_FOLDER) == true
                                        && getDictionaryService().isSubClass(type,
                                        ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                                    node.addPropertyResolver("typeIcon",
                                            this.resolverSmallIcon);
                                } else {
                                    node.addPropertyResolver("typeIcon",
                                            this.resolverFileType16);
                                }
                                itemNodes.add(node);
                            }
                        }
                    }
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
        }

        property.setListedItems((itemNodes != null ? itemNodes : Collections
                .<Node>emptyList()));

        return property.getListedItems();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Exception {
        return null;
    }

    /**
     * Search the deleted item store by name
     */
    public void searchName(ActionEvent event) {
        // simply clear the current list and refresh the screen
        // the search query text will be found and processed by the getItems() method
        contextUpdated();
        property.setShowItems(true);
        property.setFullTextSearch(false);
    }

    /**
     * Search the deleted item store by text
     */
    public void searchContent(ActionEvent event) {
        // simply clear the current list and refresh the screen
        // the search query text will be found and processed by the getItems() method
        contextUpdated();
        property.setShowItems(true);
        property.setFullTextSearch(true);
    }

    /**
     * Action handler to clear the current search results and show all items
     */
    public void clearSearch(ActionEvent event) {
        contextUpdated();
        property.setSearchText(null);
        property.setShowItems(true);
    }

    /**
     * Action handler called to prepare the selected item for an action
     */
    public void setupItemAction(ActionEvent event) {
        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        String id = params.get("id");
        if (id != null && id.length() != 0) {
            try {
                // create the node ref, then our node representation
                NodeRef ref = new NodeRef(getArchiveRootRef().getStoreRef(), id);
                Node node = new Node(ref);

                node.addPropertyResolver("locationPath", resolverLocationPath);
                node.addPropertyResolver("deletedDate", resolverDeletedDate);
                node.addPropertyResolver("deletedBy", resolverDeletedBy);
                node.addPropertyResolver("isFolder", resolverIsFolder);
                node.addPropertyResolver("mimetype", resolverMimetype);
                node.addPropertyResolver("size", resolverSize);
                node.addPropertyResolver("encoding", resolverEncoding);

                if (getDictionaryService().isSubClass(node.getType(),
                        ContentModel.TYPE_FOLDER) == true
                        && getDictionaryService().isSubClass(node.getType(),
                        ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                    node.addPropertyResolver("icon", this.resolverLargeIcon);
                } else {
                    node.addPropertyResolver("icon", this.resolverFileType32);
                }

                // prepare a node for the action context
                property.setItem(node);
                property.setDestination(null);
            } catch (InvalidNodeRefException refErr) {
                Utils
                        .addErrorMessage(MessageFormat.format(Application
                                        .getMessage(FacesContext.getCurrentInstance(),
                                                Repository.ERROR_NODEREF),
                                id));
            }
        } else {
            property.setItem(null);
        }

        // clear the UI state in preparation for finishing the next action
        contextUpdated();
    }

    // ------------------------------------------------------------------------------
    // Action handlers

    /**
     * Action handler to setup actions that act on lists
     */
    public void setupListAction(ActionEvent event) {
        // clear the UI state in preparation for finishing the next action
        property.setDestination(null);
        contextUpdated();
    }

    /**
     * @return outcome to close the main list screen and reset other beans ready for display
     */
    public String close() {
        // call beans to update UI context for other screens
        UIContextService.getInstance(FacesContext.getCurrentInstance())
                .notifyBeans();
        return OUTCOME_DIALOGCLOSE;
    }

    /**
     * Action handler to initially setup the trashcan screen
     */
    public void setupTrashcan(ActionEvent event) {
        contextUpdated();
    }

    /**
     * Action handler called when the Date filter is changed by the user
     */
    public void dateFilterChanged(ActionEvent event) {
        UIModeList filterComponent = (UIModeList) event.getComponent();
        property.setDateFilter(filterComponent.getValue().toString());
        contextUpdated();
        property.setShowItems(true);
    }

    /**
     * Action handler called when the User filter is changed by the user
     */
    public void userFilterChanged(ActionEvent event) {
        UIModeList filterComponent = (UIModeList) event.getComponent();
        property.setUserFilter(filterComponent.getValue().toString());
        contextUpdated();
        property.setShowItems(true);
    }

    /**
     * @return the archive store root node ref
     */
    private NodeRef getArchiveRootRef() {
        if (property.getArchiveRootRef() == null) {
            property.setArchiveRootRef(property.getNodeArchiveService()
                    .getStoreArchiveNode(Repository.getStoreRef()));
        }
        return property.getArchiveRootRef();
    }

    /**
     * @return the search query to use when displaying the list of deleted items
     */
    @SuppressWarnings("deprecation")
    private String buildSearchQuery() {
        String query;
        final NodeRef igRootNode = this.getNavigator().getCurrentIGRoot()
                .getNodeRef();
        final String id = igRootNode.getId();
        if (property.getSearchText() == null
                || property.getSearchText().length() == 0) {
            query = String.format(SEARCH_ALL, property.getArchiveRootRef(),
                    ContentModel.ASPECT_ARCHIVED, id);
        } else {
            // search by name in the archive store
            final String safeText = QueryParser
                    .escape(property.getSearchText());
            if (safeText.indexOf(BLANK) == -1) {
                if (property.isFullTextSearch()) {
                    query = String.format(SEARCH_TEXT, property
                                    .getArchiveRootRef(), ContentModel.ASPECT_ARCHIVED,
                            safeText, id);
                } else {
                    query = String.format(SEARCH_NAME, property
                                    .getArchiveRootRef(), ContentModel.ASPECT_ARCHIVED,
                            safeText, id);
                }
            } else {
                if (property.isFullTextSearch()) {
                    query = String.format(SEARCH_TEXT_QUOTED, property
                                    .getArchiveRootRef(), ContentModel.ASPECT_ARCHIVED,
                            safeText, id);
                } else {
                    query = String.format(SEARCH_NAME_QUOTED, property
                                    .getArchiveRootRef(), ContentModel.ASPECT_ARCHIVED,
                            safeText, id);
                }
            }
        }

        // append user search clause
        // append date search clause
        if (FILTER_DATE_ALL.equals(property.getDateFilter()) == false) {
            final Date toDate = new Date();
            Date fromDate = null;
            if (FILTER_DATE_TODAY.equals(property.getDateFilter())) {
                fromDate = new Date(toDate.getYear(), toDate.getMonth(), toDate
                        .getDate(), 0, 0, 0);
            } else if (FILTER_DATE_WEEK.equals(property.getDateFilter())) {
                fromDate = new Date(toDate.getTime()
                        - (1000L * 60L * 60L * 24L * 7L));
            } else if (FILTER_DATE_MONTH.equals(property.getDateFilter())) {
                fromDate = new Date(toDate.getTime()
                        - (1000L * 60L * 60L * 24L * 30L));
            }
            if (fromDate != null) {
                final SimpleDateFormat df = CachingDateFormat.getDateFormat();
                final String strFromDate = QueryParser.escape(df
                        .format(fromDate));
                final String strToDate = QueryParser.escape(df.format(toDate));
                String buf = AT + DATE_ATTR + SEMICOLON + BRAKET + strFromDate + TO +
                        strToDate + AND;

                query = buf + query;
            }
        }

        return query;
    }

    /**
     * Save the detail of the items that were successfully or unsuccessfully restored
     *
     * @param reports The List of RestoreNodeReport objects to walk for results
     */
    protected void saveReportDetail(List<RestoreNodeReport> reports) {
        // store the results ready for the next dialog page
        property.setSuccessItems(new ArrayList<Node>(reports.size()));
        property.setFailureItems(new ArrayList<Node>(reports.size()));
        for (RestoreNodeReport report : reports) {
            if (RestoreStatus.SUCCESS == report.getStatus()) {
                Node node = new Node(report.getRestoredNodeRef());
                node.getProperties()
                        .put(PROP_RECOVERSTATUS, report.getStatus());
                property.getSuccessItems().add(node);
            } else {
                Node node = new Node(report.getArchivedNodeRef());
                node.getProperties()
                        .put(PROP_RECOVERSTATUS, report.getStatus());
                property.getFailureItems().add(node);
            }
        }
    }

    /**
     * Build an HTML table of the items that are to be or have been recovered.
     *
     * @param items        List of Node objects to display in the table
     * @param cssClass     CSS style to apply to the table
     * @param report       Set true to report the reason for any failure. This flag requires that the Node
     *                     object has a pseudo property "recoverstatus" containing the RestoreStatus.
     * @param archivedPath Set true to show the path from the 'sys:archivedOriginalParentAssoc'
     *                     property, else the current Node Path will be used.
     * @return HTML table of node info
     */
    private String buildItemsTable(List<Node> items, String cssClass,
                                   boolean report, boolean archivedPath) {
        Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
        String userName = AuthenticationUtil.getRunAsUser();
        try {
            AuthenticationUtil.setRunAsUserSystem();

            FacesContext fc = FacesContext.getCurrentInstance();
            String contextPath = fc.getExternalContext().getRequestContextPath();

            StringBuilder buf = new StringBuilder(1024);

            // outer table
            buf
                    .append("<table width=100% cellspacing=1 cellpadding=1 border=0 class='");
            buf.append(cssClass);
            buf.append("'>");
            // title row
            buf.append("<tr style='border-bottom:1px'><th></th><th align=left><b>");
            buf.append(Application.getMessage(fc, MSG_NAME));
            buf.append("</b></th>");
            if (report == true) {
                buf.append("<th align=left>");
                buf.append(Application.getMessage(fc, MSG_RECOVERY_REASON));
                buf.append("</th>");
            } else {
                buf.append("<th align=left><b>");
                buf.append(Application.getMessage(fc, MSG_LOCATION));
                buf.append("</b></th>");
            }
            buf.append("</tr>");
            for (Node node : items) {
                // listed item rows
                buf.append("<tr><td width=16>");
                String img;
                if (getDictionaryService().isSubClass(node.getType(),
                        ContentModel.TYPE_FOLDER)) {
                    String icon = (String) node.getProperties().get("app:icon");
                    img = "/images/icons/"
                            + (icon != null ? icon + "-16.gif"
                            : BrowseBean.SPACE_SMALL_DEFAULT + ".gif");
                } else {
                    img = FileTypeImageUtils.getFileTypeImage(node.getName(), true);
                }
                buf.append("<img width=16 height=16 alt='' src='").append(
                        contextPath).append(img).append("'>");
                buf.append("</td><td>");
                buf.append(node.getName());
                buf.append("</td>");

                if (report) {
                    buf.append("<td>");
                    String msg;
                    switch ((RestoreStatus) node.getProperties().get(
                            PROP_RECOVERSTATUS)) {
                        case FAILURE_INVALID_PARENT:
                            msg = MSG_RECOVERED_ITEM_PARENT_S;
                            break;

                        case FAILURE_PERMISSION:
                            msg = MSG_RECOVERED_ITEM_PERMISSION_S;
                            break;

                        case FAILURE_INTEGRITY:
                            msg = MSG_RECOVERED_ITEM_INTEGRITY_S;
                            break;

                        default:
                            msg = MSG_RECOVERED_ITEM_FAILURE_S;
                            break;
                    }
                    buf.append(Application.getMessage(fc, msg));
                    buf.append("</td>");
                } else {
                    buf.append("<td>");
                    if (archivedPath) {
                        ChildAssociationRef childRef = (ChildAssociationRef) node
                                .getProperties()
                                .get(
                                        ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC.toString());
                        if (getNodeService().exists(childRef.getParentRef())) {
                            buf.append(
                                    getDisplayPath(getNodeService().getPath(childRef.getParentRef()), true, 5));
                        }
                    } else {
                        buf.append(Repository.getNamePath(getNodeService(),
                                getNodeService().getPath(node.getNodeRef()), null,
                                "/", null));
                    }
                    buf.append("</td>");
                }

                buf.append("</tr>");
            }
            // end table
            buf.append("</table>");

            return buf.toString();
        } finally {
            if (originalFullAuthentication == null) {
                AuthenticationUtil.clearCurrentSecurityContext();
            } else {
                AuthenticationUtil.setFullAuthentication(originalFullAuthentication);
                AuthenticationUtil.setRunAsUser(userName);
            }
        }
    }

    private boolean isAdminUser() {
        return Application.getCurrentUser(FacesContext.getCurrentInstance())
                .isAdmin();
    }

    // ------------------------------------------------------------------------------
    // Private helpers

    /**
     * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
     */
    public void contextUpdated() {
        if (property.getItemsRichList() != null) {
            property.getItemsRichList().setValue(null);
        }
        property.setShowItems(false);
    }

    /**
     * @see org.alfresco.web.app.context.IContextListener#areaChanged()
     */
    public void areaChanged() {
        // nothing to do
    }

    /**
     * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
     */
    public void spaceChanged() {
        // nothing to do
    }

    @Override
    public String cancel() {
        close();
        return super.cancel();
    }

    ///
    @Override
    public String getCancelButtonLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                MSG_CLOSE);
    }

    // ------------------------------------------------------------------------------
    // IContextListener implementation

    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getCurrentSpaceNode() {
        currentSpaceNode = (String) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("currentSpaceNode");
        return currentSpaceNode;

    }

    private String getCurrentSpacePath() {

        currentSpacePath = (String) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("currentSpacePath");
        if (currentSpacePath == null) {
            currentSpacePath = getActionNode().getNodePath().toString();
        }
        return currentSpacePath;
    }

    @Override
    public void init(final Map<String, String> parameters) {
        //deleteAllItemsOK();
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException(
                        "Node id is a mandatory parameter");
            }

            NodeRef nodeRef = getRelevantNode();

            if (nodeRef != null) {

                this.currentSpaceNode = nodeRef.getId();
                this.currentSpacePath = getNodeService().getPath(nodeRef).toString();
                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("currentSpaceNode",
                        currentSpaceNode);
                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("currentSpacePath",
                        currentSpacePath);

            }

        }
        clearSearch(null);
    }

    private NodeRef getRelevantNode() {
        final Node actionNode = getActionNode();
        // library or newsgroup noderef
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

    /**
     * Return the human readable form of the specified node Path. Fast version of the method that
     * simply converts QName localname components to Strings.
     *
     * @param path       Path to extract readable form from
     * @param showLeaf   Whether to process the final leaf element of the path
     * @param startIndex Index of first element you want to return
     * @return human readable form of the Path excluding the final element
     */
    private String getDisplayPath(Path path, boolean showLeaf, int startIndex) {
        StringBuilder buf = new StringBuilder(64);

        int count = path.size() - (showLeaf ? 0 : 1);
        for (int i = startIndex; i < count; i++) {
            String elementString = null;
            Path.Element element = path.get(i);
            if (element instanceof Path.ChildAssocElement) {
                ChildAssociationRef elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    elementString = elementRef.getQName().getLocalName();
                }
            } else {
                elementString = element.getElementString();
            }

            if (elementString != null) {
                if (i >= startIndex) {
                    buf.append("/");
                    buf.append(elementString);
                }
            }
        }

        return buf.toString();
    }

    /**
     * @return the internalNodeService
     */
    public NodeService getInternalNodeService() {
        if (internalNodeService == null) {
            internalNodeService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredNodeService();
        }
        return this.internalNodeService;
    }

    /**
     * @param internalNodeService the internalNodeService to set
     */
    public void setInternalNodeService(NodeService internalNodeService) {
        this.internalNodeService = internalNodeService;
    }

    /**
     * @return the internalSearchService
     */
    public SearchService getInternalSearchService() {
        if (internalSearchService == null) {
            internalSearchService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredSearchService();
        }
        return internalSearchService;
    }

    /**
     * @param internalSearchService the internalSearchService to set
     */
    public void setInternalSearchService(SearchService internalSearchService) {
        this.internalSearchService = internalSearchService;
    }

    private static class ResolverFileType32 implements NodePropertyResolver {

        private static final long serialVersionUID = -5681639025578263060L;

        public Object get(Node node) {
            return FileTypeImageUtils.getFileTypeImage(node.getName(), false);
        }
    }

    private static class ResolverLargeIcon implements NodePropertyResolver {

        private static final long serialVersionUID = -8334570770580388654L;

        @SuppressWarnings("unchecked")
        public Object get(Node node) {
            QNameNodeMap props = (QNameNodeMap) node.getProperties();
            String icon = (String) props.getRaw("app:icon");
            return "/images/icons/"
                    + (icon != null ? icon
                    : CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME)
                    + ".gif";
        }
    }

    private static class ResolverMimetype implements NodePropertyResolver {

        private static final long serialVersionUID = -5892550146037635522L;

        public Object get(Node node) {
            ContentData content = (ContentData) node.getProperties().get(
                    ContentModel.PROP_CONTENT.toString());
            return (content != null ? content.getMimetype() : null);
        }
    }

    private static class ResolverSize implements NodePropertyResolver {

        private static final long serialVersionUID = -191591211947393578L;

        public Object get(Node node) {
            ContentData content = (ContentData) node.getProperties().get(
                    ContentModel.PROP_CONTENT.toString());
            return (content != null ? Long.valueOf(content.getSize()) : Long.valueOf(0L));
        }
    }

    private static class ResolverEncoding implements NodePropertyResolver {

        private static final long serialVersionUID = -1594354572323978873L;

        public Object get(Node node) {
            ContentData content = (ContentData) node.getProperties().get(
                    ContentModel.PROP_CONTENT.toString());
            return (content != null ? content.getEncoding() : null);
        }
    }

    private static class ResolverDeletedDate implements NodePropertyResolver {

        private static final long serialVersionUID = 3240286507786251191L;

        public Object get(Node node) {
            return node.getProperties().get(ContentModel.PROP_ARCHIVED_DATE.toString());
        }
    }

    private static class ResolverDeletedBy implements NodePropertyResolver {

        private static final long serialVersionUID = -8678755146743606599L;

        @Override
        public Object get(Node node) {
            return node.getProperties().get(ContentModel.PROP_ARCHIVED_BY.toString());
        }
    }
}
