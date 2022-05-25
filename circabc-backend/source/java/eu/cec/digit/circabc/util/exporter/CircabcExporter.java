package eu.cec.digit.circabc.util.exporter;

import eu.cec.digit.circabc.model.CircabcModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.exporter.FileExportPackageHandler;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the CIRCABC exporter mechanism.
 *
 * @author schwerr
 */
public class CircabcExporter {

    public static final String CATEGORY_PREFIX = "CAT-";
    public static final String IG_PREFIX = "IG-";
    public static final String CIRCABC_SUFFIX = "-CIRCABC";
    public static final String SUBSGROUP = "--SubsGroup--";
    private static final Log logger = LogFactory.getLog(CircabcExporter.class);
    private NodeService nodeService = null;
    private MimetypeService mimetypeService = null;
    private SearchService searchService = null;
    private TransactionService transactionService = null;

    private CircabcExporterService circabcExporterService = null;

    private QName PROP_CIRCA_CATEGORY_MASTER_GROUP = QName.createQName(
            "http://www.cc.cec/circabc/model/content/1.0", "circaCategoryMasterGroup");
    private QName PROP_CIRCA_IG_ROOT_MASTER_GROUP = QName.createQName(
            "http://www.cc.cec/circabc/model/content/1.0", "circaIGRootMasterGroup");

    /**
     * Does the CIRCABC export. It exports users, groups and content relative to an IG, Category or
     * folder, etc.
     * <p>
     * If the selected node is not a category or IG it exports only the node tree.
     */
    @SuppressWarnings("unchecked")
    public void export(final NodeRef nodeRefToExport, String exportFileName,
                       String outputFolderPath, boolean exportAuthorities,
                       boolean exportStructure, boolean exportHeaders,
                       boolean exportOnlyRoot) {

        final RetryingTransactionHelper txnHelper = transactionService
                .getRetryingTransactionHelper();

        RetryingTransactionCallback<Object> callback =
                new RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {

                        if (!nodeService.hasAspect(nodeRefToExport,
                                CircabcModel.ASPECT_LOCKED_FOR_ACCESS)) {
                            nodeService.addAspect(nodeRefToExport,
                                    CircabcModel.ASPECT_LOCKED_FOR_ACCESS, null);
                        }

                        return null;
                    }
                };

        try {

            // Lock node for access
            txnHelper.doInTransaction(callback, false, true);

            String masterGroupName = (String) nodeService.getProperty(
                    nodeRefToExport, PROP_CIRCA_CATEGORY_MASTER_GROUP);

            String exportNamePrefix = CATEGORY_PREFIX;

            if (masterGroupName == null) {
                masterGroupName = (String) nodeService.getProperty(nodeRefToExport,
                        PROP_CIRCA_IG_ROOT_MASTER_GROUP);
                // Exporting IG
                exportNamePrefix = IG_PREFIX;
            }

            List<String> userNames = new ArrayList<>();
            List<String> groupNames = new ArrayList<>();

            StoreRef alfrescoUserStore = new StoreRef("user", "alfrescoUserStore");
            NodeRef userStoreRootNodeRef = nodeService.getRootNode(alfrescoUserStore);

            NodeRef spacesStoreRootNodeRef = nodeService.getRootNode(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

            exportFileName = exportNamePrefix + exportFileName;

            // If there is not master group it means that we are not at the level
            // of an IG nor Category, so we only export the node tree and not
            // users/groups
            if (masterGroupName != null && exportAuthorities) {

                NodeRef masterGroupNodeRef = null;

                masterGroupNodeRef = findMasterGroupNodeRef(masterGroupName);

                if (masterGroupNodeRef == null) {
                    throw new ExporterException("The MasterGroup '" + masterGroupName +
                            "' for the given export could not be found. The system " +
                            "may be corrupt.");
                }

                // Fill the lists of groups/users in, by traversing the CIRCABC
                // permission tree for this IG/Category
                logger.info("Start collecting authorities to migrate...");
                getAuthorityNamesTree(masterGroupNodeRef, userNames, groupNames,
                        exportOnlyRoot);
                logger.info("Collecting Authorities finished!");

                // Export users & groups
                exportFrom(spacesStoreRootNodeRef, "/sys:system", null,
                        exportFileName + CIRCABC_SUFFIX + "Authorities_SpacesStore",
                        outputFolderPath, userNames, groupNames, false, true, true,
                        exportOnlyRoot);

                // Remove these users because they are created by default by CIRCABC
                // and we need only the references to the exported elements and not
                // their info (gathered above)
                if (userNames.contains("<USERNAME>")) {
                    userNames.remove("<USERNAME>");
                }
                if (userNames.contains("admin")) {
                    userNames.remove("admin");
                }
                if (userNames.contains("guest")) {
                    userNames.remove("guest");
                }
                if (userNames.contains("mtuser")) {
                    userNames.remove("mtuser");
                }
                exportFrom(userStoreRootNodeRef, "/sys:system/sys:people", null,
                        exportFileName + CIRCABC_SUFFIX + "Authorities_alfrescoUserStore",
                        outputFolderPath, userNames, groupNames, false, true, true, false);
            }

            if (exportStructure) {
                // Export node tree
                exportFrom(null, null, nodeRefToExport, exportFileName +
                                CIRCABC_SUFFIX + "Nodes_SpacesStore", outputFolderPath,
                        null, null, true, !exportOnlyRoot, false, false);
            }

            // Also check if it is a category to take into account the concerned header
            if (exportHeaders && CATEGORY_PREFIX.equals(exportNamePrefix)) {

                List<NodeRef> alfrescoCategories = (List<NodeRef>) nodeService.getProperty(
                        nodeRefToExport, ContentModel.PROP_CATEGORIES);

                String alfrescoCategoryName = "";
                boolean exportRoot = false;

                if (alfrescoCategories != null && alfrescoCategories.size() == 1) {
                    alfrescoCategoryName = "/cm:" + ISO9075.encode((String)
                            nodeService.getProperty(alfrescoCategories.get(0),
                                    ContentModel.PROP_NAME));
                    exportRoot = true;
                }

                // Export header
                exportFrom(spacesStoreRootNodeRef,
                        "/cm:categoryRoot/cm:generalclassifiable/cm:CircaBCHeader" +
                                alfrescoCategoryName, null, exportFileName +
                                CIRCABC_SUFFIX + "Header_SpacesStore",
                        outputFolderPath, null, null,
                        exportRoot, true, true, false);
            }
        } finally {
            // Unlock node for access
            callback = new RetryingTransactionCallback<Object>() {

                @Override
                public Object execute() throws Throwable {

                    if (nodeService.hasAspect(nodeRefToExport,
                            CircabcModel.ASPECT_LOCKED_FOR_ACCESS)) {
                        nodeService.removeAspect(nodeRefToExport,
                                CircabcModel.ASPECT_LOCKED_FOR_ACCESS);
                    }

                    return null;
                }
            };

            txnHelper.doInTransaction(callback, false, true);
        }
    }

    /**
     * Finds the node reference of the associated master group.
     */
    protected NodeRef findMasterGroupNodeRef(String masterGroupName) {

        ResultSet resultSet = null;

        try {
            resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_XPATH,
                    "/sys:system/sys:authorities");

            if (resultSet.length() == 0) {
                throw new ExporterException("The authorities container " +
                        "/sys:system/sys:authorities could not be found. " +
                        "The system may be corrupt.");
            }

            NodeRef parentNodeRef = resultSet.getNodeRef(0);

            List<ChildAssociationRef> children =
                    nodeService.getChildAssocsByPropertyValue(parentNodeRef,
                            ContentModel.PROP_AUTHORITY_NAME,
                            "GROUP_" + masterGroupName);

            if (children.size() != 1) {
                throw new ExporterException("The number of authorities with " +
                        "the given name is not one. Given name: " + "GROUP_" +
                        masterGroupName + "- Number of authorities: " +
                        children.size());
            }

            return children.get(0).getChildRef();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    /**
     * Does the actual export as ACP Sets the parameters for the export and calls the exporter.
     */
    private void exportFrom(final NodeRef storeRef, final String path,
                            final NodeRef nodeRefToExport, final String fileName,
                            final String outputFolderPath, final List<String> userNames,
                            final List<String> groupNames, final boolean exportRootNode,
                            final boolean exportChildNodes, final boolean createAcp,
                            final boolean exportOnlyRoot) {

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

            public Object doWork() {

                // Create export package handler
                ExportPackageHandler exportHandler = null;

                if (createAcp) {
                    exportHandler = new ACPExportPackageHandler(
                            new File(outputFolderPath), new File(fileName),
                            new File(fileName), new File("content"), true,
                            mimetypeService);
                } else {
                    File destDir = new File(outputFolderPath + "/" + fileName);
                    destDir.mkdirs();
                    exportHandler = new FileExportPackageHandler(destDir,
                            new File(fileName + ".xml"), new File("content"),
                            true, mimetypeService);
                }

                // export Repository content to export package
                ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();

                Location location = null;

                if (nodeRefToExport != null) {
                    location = new Location(nodeRefToExport);
                } else {
                    location = new Location(storeRef);
                    location.setPath(path);
                }

                parameters.setExportFrom(location);
                parameters.setCrawlSelf(exportRootNode);
                parameters.setCrawlChildNodes(exportChildNodes);
                parameters.setCrawlNullProperties(false);
                parameters.setExportOnlyRoot(exportOnlyRoot);

                // exclude ci:lockForAccess
                parameters.setExcludeAspects(new QName[]{CircabcModel.ASPECT_LOCKED_FOR_ACCESS});

                if (userNames != null) {
                    // For users export (will be fed in by
                    // groups/users/authorities export results from the SpacesStore)
                    parameters.setCrawlOnlyGivenUsers(userNames.toArray(new String[userNames.size()]));
                }

                if (groupNames != null) {
                    // For group export
                    parameters.setCrawlOnlyGivenGroups(groupNames.toArray(new String[groupNames.size()]));
                }

                try {
                    circabcExporterService.exportView(exportHandler, parameters, new ExportProgress());
                } catch (ExporterException e) {
                    throw new ExporterException("Exception when exporting.", e);
                }

                return null;
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Traverses the authority tree to determine which groups/users belog to the selected Category/IG
     * <p>
     * This is necessary to filter from the user/groups export the authorities that make no sense for
     * the IG/Category we are exporting.
     */
    private void getAuthorityNamesTree(NodeRef rootNodeRef, List<String> userNames,
                                       List<String> groupNames, boolean exportOnlyRoot) {

        // If person, then it is a "leaf" in the tree
        if (ContentModel.TYPE_PERSON.equals(nodeService.getType(rootNodeRef))) {
            String userName = (String) nodeService.getProperty(rootNodeRef, ContentModel.PROP_USERNAME);
            if (!userNames.contains(userName)) {
                userNames.add(userName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Collecting person name: " + userName);
                }
            }
            return;
        }

        String groupName = (String) nodeService
                .getProperty(rootNodeRef, ContentModel.PROP_AUTHORITY_NAME);
        if (!groupNames.contains(groupName)) {
            groupNames.add(groupName);
            if (logger.isDebugEnabled()) {
                logger.debug("Collecting group name: " + groupName);
            }
        }
        // If we must export only the root node and we-re about to traverse
        // the 'SubsGroup', I return because we're getting into IGs authorities
        if (exportOnlyRoot && groupName != null && groupName.contains(SUBSGROUP)) {
            return;
        }

        List<ChildAssociationRef> childrenRefs = nodeService.getChildAssocs(rootNodeRef);

        for (ChildAssociationRef childRef : childrenRefs) {

            NodeRef childNodeRef = childRef.getChildRef();

            getAuthorityNamesTree(childNodeRef, userNames, groupNames, exportOnlyRoot);
        }
    }

    /**
     * Sets the value of the nodeService
     *
     * @param nodeService the nodeService to set.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Sets the value of the mimetypeService
     *
     * @param mimetypeService the mimetypeService to set.
     */
    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Sets the value of the searchService
     *
     * @param searchService the searchService to set.
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Sets the value of the transactionService
     *
     * @param transactionService the transactionService to set.
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Sets the value of the circabcExporterService
     *
     * @param circabcExporterService the circabcExporterService to set.
     */
    public void setCircabcExporterService(
            CircabcExporterService circabcExporterService) {
        this.circabcExporterService = circabcExporterService;
    }
}
