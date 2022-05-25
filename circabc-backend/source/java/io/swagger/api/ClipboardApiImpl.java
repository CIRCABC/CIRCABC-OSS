package io.swagger.api;

import eu.cec.digit.circabc.aspect.DisableNotificationThreadLocal;
import eu.cec.digit.circabc.service.notification.NotificationManagerService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the api to copy, move and link nodes.
 *
 * @author schwerr
 */
public class ClipboardApiImpl implements ClipboardApi {

    private static final String LINK_NODE_EXTENSION = ".url";
    /**
     * Shallow search for nodes with a name pattern
     */
    private static final String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";

    private final Log logger = LogFactory.getLog(ClipboardApiImpl.class);
    private DictionaryService dictionaryService = null;
    private NodeService nodeService = null;
    private FileFolderService fileFolderService = null;
    private CopyService copyService = null;
    private MultilingualContentService multilingualContentService = null;
    private SearchService searchService = null;
    private NamespaceService namespaceService = null;
    private ManagementService managementService;
    private NotificationManagerService notificationManagerService;

    /**
     * Pastes a list of nodes given as a comma separated string into the destination folder. According
     * to the action, the node is copied, linked or moved.
     */
    public void paste(final String[] nodeIds, final NodeRef destRef, final int action)
            throws Exception {

        if (nodeIds.length > 0) {
            NodeRef firstNodeRef = Converter.createNodeRefFromId(nodeIds[0]);
            boolean shouldNotify = getNotificationStatus(firstNodeRef, true);
            DisableNotificationThreadLocal disableNotificationThreadLocal =
                    new DisableNotificationThreadLocal();
            if (!shouldNotify) {
                disableNotificationThreadLocal.set(true);
            }
        }

        for (String nodeId : nodeIds) {
            NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

            if (!nodeService.exists(nodeRef)) {
                // if the item does not exist or has been deleted, return
                return;
            }

            paste(nodeRef, destRef, action, false);
        }
    }

    /**
     * @return the notificationManagerService
     */
    public NotificationManagerService getNotificationManagerService() {
        return notificationManagerService;
    }

    /**
     * @param notificationManagerService the notificationManagerService to set
     */
    public void setNotificationManagerService(NotificationManagerService notificationManagerService) {
        this.notificationManagerService = notificationManagerService;
    }

    /**
     * Pastes a node into the destination folder. According to the action, the node is copied, linked
     * or moved.
     */
    public void paste(final NodeRef nodeRef, final NodeRef destRef, final int action)
            throws Exception {
        paste(nodeRef, destRef, action, true);
    }

    private void paste(
            final NodeRef nodeRef, final NodeRef destRef, final int action, boolean checkShouldNotify)
            throws Exception {
        DisableNotificationThreadLocal disableNotificationThreadLocal =
                new DisableNotificationThreadLocal();
        disableNotificationThreadLocal.set(false);

        if (checkShouldNotify) {
            boolean shouldNotify = getNotificationStatus(nodeRef, false);

            if (!shouldNotify) {
                disableNotificationThreadLocal.set(true);
            }
        }

        final boolean isPrimaryParent;

        final ChildAssociationRef assocRef;

        if (getParent(nodeRef) == null) {
            assocRef = nodeService.getPrimaryParent(nodeRef);
            isPrimaryParent = true;
        } else {
            NodeRef parentNodeRef = getParent(nodeRef);
            List<ChildAssociationRef> assocList = nodeService.getParentAssocs(nodeRef);
            ChildAssociationRef foundRef = null;
            if (assocList != null) {
                for (ChildAssociationRef assocListEntry : assocList) {
                    if (parentNodeRef.equals(assocListEntry.getParentRef())) {
                        foundRef = assocListEntry;
                        break;
                    }
                }
            }
            assocRef = foundRef;
            isPrimaryParent = parentNodeRef.equals(nodeService.getPrimaryParent(nodeRef).getParentRef());
        }
        if (assocRef == null) {
            throw new Exception("Can not find assocRef for: " + nodeRef.toString());
        }

        // initial name to attempt the copy of the item with
        String originalName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String name = originalName;

        String translationPrefix = "";

        if (action == ClipboardAction.LINK.getValue()) {
            // copy as link was specifically requested by the user
            String linkTo = "Link_";
            name = linkTo + name;
        }

        QName type = nodeService.getType(nodeRef);

        // Loop until we find a target name that doesn't exist
        for (; ; ) {

            try {

                if (action == ClipboardAction.LINK.getValue()) {

                    // LINK operation
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Attempting to link node ID: " + nodeRef + " into node: " + destRef.toString());
                    }

                    // we create a special Link Object node that has a property to reference the original
                    // create the node using the nodeService (can only use FileFolderService for content)
                    if (notExists(name + LINK_NODE_EXTENSION, destRef)) {
                        Map<QName, Serializable> props = new HashMap<>(2, 1.0f);
                        String newName = name + LINK_NODE_EXTENSION;
                        props.put(ContentModel.PROP_NAME, newName);
                        props.put(ContentModel.PROP_LINK_DESTINATION, nodeRef);
                        if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                            // create File Link node
                            ChildAssociationRef childRef =
                                    nodeService.createNode(
                                            destRef,
                                            ContentModel.ASSOC_CONTAINS,
                                            QName.createQName(assocRef.getQName().getNamespaceURI(), newName),
                                            ApplicationModel.TYPE_FILELINK,
                                            props);

                            // apply the titled aspect - title and description
                            Map<QName, Serializable> titledProps = new HashMap<>(2, 1.0f);
                            titledProps.put(ContentModel.PROP_TITLE, name);
                            titledProps.put(ContentModel.PROP_DESCRIPTION, name);
                            nodeService.addAspect(
                                    childRef.getChildRef(), ContentModel.ASPECT_TITLED, titledProps);
                        } else {
                            // create Folder link node
                            ChildAssociationRef childRef =
                                    nodeService.createNode(
                                            destRef,
                                            ContentModel.ASSOC_CONTAINS,
                                            assocRef.getQName(),
                                            ApplicationModel.TYPE_FOLDERLINK,
                                            props);

                            // apply the uifacets aspect - icon, title and description props
                            Map<QName, Serializable> uiFacetsProps = new HashMap<>(4, 1.0f);
                            uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
                            uiFacetsProps.put(ContentModel.PROP_TITLE, name);
                            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, name);
                            nodeService.addAspect(
                                    childRef.getChildRef(), ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
                        }
                    }
                } else if (action == ClipboardAction.COPY.getValue()) {

                    // COPY operation
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Attempting to copy node: " + nodeRef + " into node ID: " + destRef.toString());
                    }

                    // check that we are not attempting to copy a duplicate into the same parent
                    if (destRef.equals(assocRef.getParentRef()) && name.equals(originalName)) {
                        // manually change the name if this occurs
                        throw new FileExistsException(destRef, name);
                    }

                    // check that the file name doesn't exist in the listing
                    List<FileInfo> files = fileFolderService.list(destRef);

                    for (FileInfo file : files) {
                        if (name.equals(file.getName())) {
                            throw new FileExistsException(destRef, name);
                        }
                    }

                    if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)
                            || dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)) {
                        // copy the file/folder
                        fileFolderService.copy(nodeRef, destRef, name);
                    } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                        // copy the mlContainer and its translations
                        multilingualContentService.copyTranslationContainer(
                                nodeRef, destRef, translationPrefix);
                    } else {
                        // copy the node
                        if (notExists(name, destRef)) {
                            copyService.copyAndRename(
                                    nodeRef, destRef, ContentModel.ASSOC_CONTAINS, assocRef.getQName(), true);
                        }
                    }
                } else if (action == ClipboardAction.MOVE.getValue()) {
                    disableNotificationThreadLocal.set(true);

                    // MOVE operation
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Attempting to move node: " + nodeRef + " into node ID: " + destRef.toString());
                    }

                    // same folder
                    if (destRef.equals(assocRef.getParentRef())) {
                        break;
                    }

                    if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)
                            || dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)) {
                        // move the file/folder
                        fileFolderService.moveFrom(nodeRef, getParent(nodeRef), destRef, name);
                    } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                        // copy the mlContainer and its translations
                        multilingualContentService.moveTranslationContainer(nodeRef, destRef);
                    } else {
                        if (isPrimaryParent) {
                            // move the node
                            nodeService.moveNode(
                                    nodeRef, destRef, ContentModel.ASSOC_CONTAINS, assocRef.getQName());
                        } else {
                            nodeService.removeChild(getParent(nodeRef), nodeRef);
                            nodeService.addChild(destRef, nodeRef, assocRef.getTypeQName(), assocRef.getQName());
                        }
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Invalid action code, must be 0 = COPY, 1 = MOVE or 2 = LINK");
                }

                // We got here without error, so no need to loop with a new name
                break;
            } catch (FileExistsException fileExistsErr) {
                // If mode is COPY, have another go around the loop with a new name
                if (action == ClipboardAction.COPY.getValue()) {
                    String copyOf = "Copy_";
                    name = copyOf + name;
                    translationPrefix = copyOf + ' ' + translationPrefix;
                } else {
                    // we should not rename an item when it is being moved - so exit
                    throw fileExistsErr;
                }
            }
        }
    }

    private boolean getNotificationStatus(NodeRef nodeRef, boolean pasteAll) {
        boolean result = false;
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
        if (igNodeRef != null) {
            if (pasteAll) {
                result = getNotificationManagerService().isPasteAllNotificationEnabled(igNodeRef);
            } else {
                result = getNotificationManagerService().isPasteNotificationEnabled(igNodeRef);
            }
        }
        return result;
    }

    private NodeRef getParent(NodeRef nodeRef) {
        return nodeService.getPrimaryParent(nodeRef).getParentRef();
    }

    private boolean notExists(String name, NodeRef parent) {

        QueryParameterDefinition[] params = new QueryParameterDefinition[1];
        params[0] =
                new QueryParameterDefImpl(
                        ContentModel.PROP_NAME,
                        dictionaryService.getDataType(DataTypeDefinition.TEXT),
                        true,
                        name);

        // execute the query
        List<NodeRef> nodeRefs =
                searchService.selectNodes(parent, XPATH_QUERY_NODE_MATCH, params, namespaceService, false);

        return nodeRefs.isEmpty();
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param copyService the copyService to set
     */
    public void setCopyService(CopyService copyService) {
        this.copyService = copyService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the managementService
     */
    public ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }
}
