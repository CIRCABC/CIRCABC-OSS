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
package eu.cec.digit.circabc.web.wai.dialog.customization;

import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.bean.navigation.ResolverHelper;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.generic.ManagedNodes;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.clipboard.ClipboardBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.*;

public class LibraryBulkOperationDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -5337175195473365007L;
    private static final Log logger = LogFactory.getLog(LibraryBulkOperationDialog.class);
    String[] selectedContainers;
    String[] selectedContents;
    ArrayList<SelectItem> allContainers;
    ArrayList<SelectItem> allContents;
    private List<LogRecord> deletedLogRecords;
    /**
     * Lists of container nodes for display
     */
    private List<NavigableNode> containers = null;
    /**
     * Lists of content nodes for display
     */
    private List<NavigableNode> contents = null;
    private List<NavigableNode> selectedDataList;
    private Map<Long, Boolean> selectedIds = new HashMap<>();
    private String selectedOperation;

    public String getPageIconAltText() {
        return translate("library_bulk_operation_dialog_icon_tooltip");
    }

    public String getBrowserTitle() {
        return translate("library_bulk_operation_dialog_browser_title");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) {

        deletedLogRecords = new ArrayList<>();
        for (NavigableNode navNode : getContainers()) {
            if (navNode.isSelected()) {
                navNode.getName();
            }
        }

        String bulkOperation = getSelectedOperation();
        ClipboardBean clipboardBean = Beans.getClipboardBean();

        String[] selectedNodes = (String[]) ArrayUtils
                .addAll(this.getSelectedContents(), this.getSelectedContainers());
        Set<String> selectedContentSet = new HashSet<>(Arrays.asList(this.getSelectedContents()));
        Set<String> selectedContainersSet = new HashSet<>(Arrays.asList(this.getSelectedContainers()));

        for (String nodeRef : selectedNodes) {
            if (bulkOperation.equals("delete")) {
                NodeRef tempNodeRef = new NodeRef(nodeRef);
                if (getNodeService().exists(tempNodeRef)) {
                    LogRecord currentLogRecord = createLogRecord(tempNodeRef);
                    if (selectedContentSet.contains(nodeRef)) {
                        currentLogRecord.setActivity("Bulk delete document");
                        String name = (String) getNodeService()
                                .getProperty(tempNodeRef, ContentModel.PROP_NAME);
                        currentLogRecord.addInfo(name);
                    }
                    if (selectedContainersSet.contains(nodeRef)) {
                        currentLogRecord.setActivity("Bulk delete space");
                    }
                    deletedLogRecords.add(currentLogRecord);
                    deleteNode(nodeRef, selectedContentSet);
                }
            } else {
                UIActionLink link = new UIActionLink();
                link.getParameterMap().put("ref", nodeRef);
                ActionEvent ae = new ActionEvent(link);
                if (bulkOperation.equals("copy")) {
                    clipboardBean.copyNode(ae);
                }
                if (bulkOperation.equals("cut")) {
                    clipboardBean.cutNode(ae);
                }
            }
        }

        return outcome;
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        logBulkDeletetedRecords(true);
        return outcome;
    }

    protected String getErrorOutcome(final Throwable exception) {
        logBulkDeletetedRecords(true);
        return null;
    }

    // Init --------------------------------------------------------------------------------------

    private void logBulkDeletetedRecords(boolean isOK) {
        for (LogRecord currentLogRecord : deletedLogRecords) {
            currentLogRecord.setOK(isOK);
            getLogService().log(currentLogRecord);
        }
    }

    // Actions -----------------------------------------------------------------------------------

    private LogRecord createLogRecord(NodeRef nodeRef) {

        LogRecord logDeletedRecord = new LogRecord();
        logDeletedRecord.setService("Library");
        super.updateLogDocument(nodeRef, logDeletedRecord);
        return logDeletedRecord;

    }

    public void init(final Map<String, String> parameters) {
        super.init(parameters);
        this.contents = null;
        this.containers = null;
    }

    private void deleteNode(String nodeRef, Set<String> selectedContentSet) {
        NodeRef currentNodeRef = new NodeRef(nodeRef);
        Node currentNode = new Node(currentNodeRef);
        ManagedNodes nodeType = ManagedNodes.resolve(currentNode);

        if (currentNode != null && nodeType != null) {
            if (ManagedNodes.TRANSLATION.equals(nodeType)) {
                final NodeRef pivotTranslation = getMultilingualContentService()
                        .getPivotTranslation(currentNodeRef);

                if (currentNodeRef.equals(pivotTranslation)) {
                    if (isAllTrnslationSelected(pivotTranslation, selectedContentSet)) {
                        final NodeRef translationContainer = getMultilingualContentService()
                                .getTranslationContainer(currentNodeRef);
                        getMultilingualContentService().deleteTranslationContainer(translationContainer);
                    }
                } else {
                    if (getNodeService().exists(currentNodeRef)) {
                        getMultilingualContentService().unmakeTranslation(currentNodeRef);
                        getNodeService().deleteNode(currentNodeRef);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to delete " + nodeType.toString() + " node: " + currentNode.getId());
                }

                final String nodeName = (String) this.getNodeService()
                        .getProperty(currentNodeRef, ContentModel.PROP_NAME);
                logRecord.setInfo("deleted " + nodeName);
                try {
                    // delete the node
                    this.getNodeService().deleteNode(currentNodeRef);
                } catch (final NodeLockedException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Can't delete the node - node is locked:" + currentNode.getNodeRef());
                    }
                    Utils.addStatusMessage(FacesMessage.SEVERITY_WARN,
                            translate("msg_error_cant_delete_space_that_contains_checkout_documents"));
                }
            }
        }
    }

    // Getters -----------------------------------------------------------------------------------

    private boolean isAllTrnslationSelected(NodeRef pivotTranslation,
                                            Set<String> selectedContentSet) {
        boolean result = true;
        final Map<Locale, NodeRef> translations = getMultilingualContentService()
                .getTranslations(pivotTranslation);
        for (NodeRef nodeRef : translations.values()) {
            if (!selectedContentSet.contains(nodeRef.toString())) {
                result = false;
                break;
            }
        }
        return result;
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

    public void setContainers(List<NavigableNode> containers) {
        this.containers = containers;
    }

    public Map<Long, Boolean> getSelectedIds() {
        return selectedIds;
    }

    public String getSelectedItems() {

        // Get selected items.
        selectedDataList = new ArrayList<>();
        for (NavigableNode dataItem : containers) {
            if (dataItem.isSelected()) {
                selectedDataList.add(dataItem);
                dataItem.setSelected(false); // Reset.
            }
        }

        // Do your thing with the MyData items in List selectedDataList.

        return "selected"; // Navigation case.
    }

    public List<NavigableNode> getSelectedDataList() {
        return selectedDataList;
    }

    public String getSelectedOperation() {
        if (selectedOperation == null) {
            selectedOperation = "copy"; // This will be the default selected item.
        }
        return selectedOperation;
    }

    public void setSelectedOperation(String selectedOperation) {
        this.selectedOperation = selectedOperation;
    }

    public List<SelectItem> getOperations() {
        List<SelectItem> operations = new ArrayList<>();
        operations.add(new SelectItem("copy", translate("library_bulk_operation_dialog_copy")));
        operations.add(new SelectItem("cut", translate("library_bulk_operation_dialog_cut")));
        operations.add(new SelectItem("delete", translate("library_bulk_operation_dialog_delete")));
        return operations;
    }

    public String[] getSelectedContainers() {
        return selectedContainers;
    }

    public void setSelectedContainers(String containers[]) {
        this.selectedContainers = containers.clone();
    }

    public String[] getSelectedContents() {
        return selectedContents;
    }

    public void setSelectedContents(String contents[]) {
        this.selectedContents = contents.clone();
    }

    public List<SelectItem> getAllContainers() {
        allContainers = new ArrayList<>();
        for (NavigableNode node : getContainers()) {
            allContainers.add(new SelectItem(node.getNodeRefAsString(), node.getName()));
        }
        return allContainers;
    }

    public List<SelectItem> getAllContents() {
        allContents = new ArrayList<>();
        for (NavigableNode node : getContents()) {
            allContents.add(new SelectItem(node.getNodeRefAsString(), node.getName()));
        }
        return allContents;
    }


    /**
     * Get list of content nodes for the currentNodeRef
     *
     * @return The list of content nodes for the currentNodeRef
     */
    public List<NavigableNode> getContents() {
        if (this.contents == null || this.contents.isEmpty()) {
            // user performs a simple browsing
            fillBrowseNodes(getNavigator().getCurrentNode());
        }

        return this.contents;
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
                    final List<FileInfo> children = getFileFolderService().list(currentNode.getNodeRef());
                    containers = new ArrayList<>(children.size());
                    contents = new ArrayList<>(children.size());

                    //NodeRef nodeRef;
                    QName type;
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
                        logger.error("Unexpected error:" + err.getMessage(), err);
                    }
                }
                return null;
            }

        };
        txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug("Time to query and build map nodes: " + (endTime - startTime) + "ms");
        }
    }


}
