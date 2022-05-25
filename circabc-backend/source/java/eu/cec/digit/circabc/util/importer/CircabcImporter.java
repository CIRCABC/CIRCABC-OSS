package eu.cec.digit.circabc.util.importer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.FileImportPackageHandler;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.view.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.faces.context.FacesContext;
import java.io.File;
import java.io.Reader;
import java.util.List;

/**
 * Implementation of the CIRCABC importer mechanism.
 *
 * @author dtrovato
 */
public class CircabcImporter {

    private static final Log logger = LogFactory.getLog(CircabcImporter.class);

    private final String CAT_MASTERGROUP = "circaCategoryMasterGroup";
    private final String IG_MASTERGROUP = "circaIGRootMasterGroup";
    private final String GROUP_PREFIX = "GROUP_";

    private final QName PROP_CIRCABC_SUBGROUP = QName.createQName(
            "http://www.cc.cec/circabc/model/content/1.0", "circaBCSubsGroup");
    private final QName PROP_CIRCA_CATEGORY_SUBGROUP = QName.createQName(
            "http://www.cc.cec/circabc/model/content/1.0", "circaCategorySubsGroup");

    private NodeService nodeService;
    private SearchService searchService;
    private AuthorityService authorityService;
    private CircabcImporterService circabcImporterService;

    // XML Pull Parser Factory
    private XmlPullParserFactory factory;

    public CircabcImporter() {
        try {
            // Construct Xml Pull Parser Factory
            factory = XmlPullParserFactory
                    .newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), this.getClass());
            factory.setNamespaceAware(true);
        } catch (XmlPullParserException e) {
            throw new ImporterException("Failed to initialise view importer", e);
        }
    }

    /**
     * Does the CIRCABC import. It imports users, groups and content relative to an IG, Category or
     * folder, etc.
     * <p>
     * You can import a whole Category inside the CIRCABC Root NodeRef or a IG inside a selected
     * Category.
     */
    public void importTo(final NodeRef nodeRefWhereToImport, final List<String> packages,
                         final String importFolderPath, final boolean importAuthorities,
                         final boolean importStructure, final boolean importHeaders,
                         final boolean keepUUIDs) {
        RetryingTransactionHelper txnHelper = Repository
                .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                // call the actual implementation
                importPackages(nodeRefWhereToImport, packages, importFolderPath, importAuthorities,
                        importStructure, importHeaders, keepUUIDs);
                return "success";
            }
        };
        try {
            // Execute
            txnHelper.doInTransaction(callback, false, true);
        } catch (Throwable e) {
            logger.error("Failed to import using RetryingTransactionHelper : " + e.getMessage());
            //throw e;
            ReportedException.throwIfNecessary(e);
        }
    }

    private void importPackages(final NodeRef nodeRefWhereToImport, final List<String> packages,
                                final String importFolderPath, final boolean importAuthorities,
                                final boolean importStructure, final boolean importHeaders,
                                final boolean keepUUIDs) {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {
                NodeRef spacesStoreRootNodeRef = nodeService
                        .getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

                if (importAuthorities) {
                    StoreRef alfrescoUserStore = new StoreRef("user", "alfrescoUserStore");
                    NodeRef userStoreRootNodeRef = nodeService.getRootNode(alfrescoUserStore);

                    // -- exportFileName + CIRCABC_SUFFIX + "Authorities_alfrescoUserStore"
                    // users
                    doImport(userStoreRootNodeRef, "/sys:system/sys:people", null,
                            importFolderPath, getPackage(packages, "Authorities_alfrescoUserStore"), keepUUIDs,
                            true);

                    // -- exportFileName + CIRCABC_SUFFIX + "Authorities_SpacesStore"
                    // users & groups
                    doImport(spacesStoreRootNodeRef, "/sys:system", null,
                            importFolderPath, getPackage(packages, "Authorities_SpacesStore"), keepUUIDs, true);
                }

                if (importHeaders) {
                    // -- exportFileName + CIRCABC_SUFFIX + "Header_SpacesStore"
                    // category headers
                    doImport(spacesStoreRootNodeRef,
                            "/cm:categoryRoot/cm:generalclassifiable/cm:CircaBCHeader",
                            null, importFolderPath, getPackage(packages, "Header_SpacesStore"), keepUUIDs, true);
                }

                if (importStructure) {
                    // -- exportFileName + CIRCABC_SUFFIX + "Nodes_SpacesStore"
                    // nodes
                    doImport(null, null, nodeRefWhereToImport, importFolderPath,
                            getPackage(packages, "Nodes_SpacesStore"), keepUUIDs, false);

                    // authorities : we need to add the 'member' association between the MasterGroup of the child node we just imported
                    // and the SubsGroup of the parent node (where we imported)
                    String entityMasterGroupName = getEntityMasterGroupName(importFolderPath,
                            getPackage(packages, "Nodes_SpacesStore"));
                    addAuthority(nodeRefWhereToImport, entityMasterGroupName);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private String getEntityMasterGroupName(String importFolderPath, String packageFileName) {
        File sourceDir = new File(importFolderPath + "/" + packageFileName);
        File dataFile = new File(packageFileName + ".xml");
        FileImportPackageHandler importHandler = new FileImportPackageHandler(sourceDir, dataFile,
                "UTF-8");
        importHandler.startImport();
        Reader dataFileReader = importHandler.getDataStream();

        String entityMasterGroup = null;
        try {
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(dataFileReader);
            try {
                for (int eventType = xpp.getEventType(); eventType != XmlPullParser.END_DOCUMENT;
                     eventType = xpp.next()) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (CAT_MASTERGROUP.equals(xpp.getName()) || IG_MASTERGROUP.equals(xpp.getName())) {
                            eventType = xpp.next();
                            // For CIRCABC 3.8 and later export
                            String possibleMasterGroup = xpp.getText().trim();
                            if (eventType == XmlPullParser.TEXT && !possibleMasterGroup.isEmpty()) {
                                entityMasterGroup = xpp.getText();
                                break;
                            }
                            eventType = xpp.next();
                            eventType = xpp.next();
                            // For CIRCABC 3.7 and previous export
                            if (eventType == XmlPullParser.TEXT) {
                                entityMasterGroup = xpp.getText();
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Failed to parse at line " + xpp.getLineNumber() + "; column " + xpp.getColumnNumber()
                                    + " due to error: ", e);
                }
                throw new ImporterException(
                        "Failed to parse at line " + xpp.getLineNumber() + "; column " + xpp.getColumnNumber()
                                + " due to error: " + e.getMessage(), e);
            }
        } catch (XmlPullParserException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to parse", e);
            }
            throw new ImporterException("Failed to parse", e);
        }

        importHandler.endImport();

        if (entityMasterGroup == null) {
            throw new ImporterException(
                    "The MasterGroup authority of the exported entity was not found in " + packageFileName);
        }

        return entityMasterGroup;
    }

    private void addAuthority(NodeRef parentNode, final String childNodeMasterGoupAuthName) {
        final String parentNodeSubsGroupAuthName;
        if (nodeService.getProperty(parentNode, PROP_CIRCABC_SUBGROUP) != null) {
            parentNodeSubsGroupAuthName = (String) nodeService
                    .getProperty(parentNode, PROP_CIRCABC_SUBGROUP);
        } else if (nodeService.getProperty(parentNode, PROP_CIRCA_CATEGORY_SUBGROUP) != null) {
            parentNodeSubsGroupAuthName = (String) nodeService
                    .getProperty(parentNode, PROP_CIRCA_CATEGORY_SUBGROUP);
        } else {
            throw new ImporterException("The SubsGroup authority of " + parentNode + " was not found.");
        }

        NodeRef authoritiesNodeRef = null;
        NodeRef parentNodeSubsGroup = null;
        NodeRef childNodeMasterGroup = null;
        ResultSet resultSet = null;
        try {
            resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_XPATH,
                    "/sys:system/sys:authorities");

            authoritiesNodeRef = resultSet.getNodeRef(0);

            // find the SubGroup authority parent node
            List<ChildAssociationRef> children = nodeService
                    .getChildAssocs(authoritiesNodeRef, ContentModel.ASSOC_CHILDREN, new QNamePattern() {

                        @Override
                        public boolean isMatch(QName qName) {
                            return qName.getLocalName().startsWith(GROUP_PREFIX + parentNodeSubsGroupAuthName);
                        }
                    }, true);

            if (children != null && children.size() == 1) {
                parentNodeSubsGroup = children.get(0).getChildRef();
            } else {
                throw new ImporterException(
                        parentNodeSubsGroupAuthName + " doesn't exist or it has been found more than once.");
            }

            // find the MasterGroup authority child node
            children = nodeService
                    .getChildAssocs(authoritiesNodeRef, ContentModel.ASSOC_CHILDREN, new QNamePattern() {

                        @Override
                        public boolean isMatch(QName qName) {
                            return qName.getLocalName().startsWith(GROUP_PREFIX + childNodeMasterGoupAuthName);
                        }
                    }, true);

            if (children != null && children.size() == 1) {
                childNodeMasterGroup = children.get(0).getChildRef();
            } else {
                throw new ImporterException(
                        childNodeMasterGoupAuthName + " doesn't exist or it has been found more than once.");
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }

        // add the authority if it doesn't exist
        String childNodeMasterGoupName = (String) nodeService
                .getProperty(childNodeMasterGroup, ContentModel.PROP_NAME);
        if (nodeService
                .getChildByName(parentNodeSubsGroup, ContentModel.ASSOC_MEMBER, childNodeMasterGoupName)
                == null) {
            authorityService.addAuthority(GROUP_PREFIX + parentNodeSubsGroupAuthName,
                    GROUP_PREFIX + childNodeMasterGoupAuthName);

            if (logger.isDebugEnabled()) {
                logger.debug("Authority has been created : " + getAuthorityInfo(ContentModel.ASSOC_MEMBER,
                        parentNodeSubsGroup, childNodeMasterGroup));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Authority already created : " + getAuthorityInfo(ContentModel.ASSOC_MEMBER,
                        parentNodeSubsGroup, childNodeMasterGroup));
            }
        }
    }

    private String getAuthorityInfo(QName assocType, NodeRef parentRef, NodeRef childRef) {
        StringBuilder log = new StringBuilder("Authority[type=").append(assocType.getLocalName());
        log.append(", parentRef=").append(parentRef);
        log.append(", childRef=").append(childRef);
        log.append("]");
        return log.toString();
    }

    private String getPackage(List<String> packages, String identifier) {
        for (String acp : packages) {
            if (acp.contains(identifier)) {
                return acp;
            }
        }
        throw new ImporterException("The ACP file '" + identifier + "' was not found.");
    }


    /**
     * Does the actual import of ACP(s) Sets the parameters for the import and calls the importer.
     */
    private void doImport(final NodeRef storeRef, final String path,
                          final NodeRef nodeRefWhereToImport,
                          final String importFolderPath,
                          final String packageFileName,
                          final boolean keepUUIDs,
                          final boolean useACP) {
        Location location;
        if (nodeRefWhereToImport != null) {
            location = new Location(nodeRefWhereToImport);
        } else {
            location = new Location(storeRef);
            location.setPath(path);
        }

        ImportPackageHandler importHandler;
        if (useACP) {
            importHandler = new ACPImportPackageHandler(
                    new File(importFolderPath + "/" + packageFileName), "UTF-8");
        } else {
            File sourceDir = new File(importFolderPath + "/" + packageFileName);
            File dataFile = new File(packageFileName + ".xml");
            importHandler = new FileImportPackageHandler(sourceDir, dataFile, "UTF-8");
        }
        try {
            circabcImporterService
                    .importView(importHandler, location, new CircabcImportBinding(keepUUIDs), null);
        } catch (ImporterException e) {
            throw e;
        } finally {
            // Close package handler to release open files
            importHandler.endImport();
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
     * Sets the value of the circabcImporterService
     *
     * @param circabcImporterService the circabcImporterService to set.
     */
    public void setCircabcImporterService(
            CircabcImporterService circabcImporterService) {
        this.circabcImporterService = circabcImporterService;
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
     * Sets the value of the authorityService
     *
     * @param authorityService the authorityService to set.
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * CIRCABC Import Binding
     */
    private class CircabcImportBinding implements ImporterBinding {

        private ImporterBinding.UUID_BINDING uuidBinding;

        /**
         * Construct
         */
        private CircabcImportBinding(boolean keepUUIDs) {
            if (keepUUIDs) {
                uuidBinding = ImporterBinding.UUID_BINDING.CREATE_NEW_WITH_UUID;
            } else {
                uuidBinding = ImporterBinding.UUID_BINDING.CREATE_NEW;
            }
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getUUIDBinding()
         */
        public UUID_BINDING getUUIDBinding() {
            return uuidBinding;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getValue(java.lang.String)
         */
        public String getValue(String key) {
            return null;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#searchWithinTransaction()
         */
        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getExcludedClasses()
         */
        public QName[] getExcludedClasses() {
            return new QName[]{};
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
