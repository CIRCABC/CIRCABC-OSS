package eu.cec.digit.circabc.util.exporter;

import eu.cec.digit.circabc.model.CircabcModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.*;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.ParameterCheck;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Default implementation of the Exporter Service.
 *
 * @author David Caruana
 */
public class ExporterComponent implements CircabcExporterService {

    public static final String APP_SHARE = "APP.SHARE";
    // Logger
    private static final Log logger = LogFactory.getLog(ExporterComponent.class);
    // Supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private DescriptorService descriptorService;
    private AuthenticationService authenticationService;
    private PermissionService permissionService;

    private NodeRef versionStoreRootNoderef = null;

    /**
     * Indent Size
     */
    private int indentSize = 2;

    /**
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService the service to perform path searches
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param contentService the content service
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param dictionaryService the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param descriptorService the descriptor service
     */
    public void setDescriptorService(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    /**
     * @param authenticationService the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @param permissionService the permission service
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(java.io.OutputStream, org.alfresco.service.cmr.view.ExporterCrawlerParameters, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(
            OutputStream viewWriter, ExporterCrawlerParameters parameters, Exporter progress) {
        ParameterCheck.mandatory("View Writer", viewWriter);

        // Construct a basic XML Exporter
        Exporter xmlExporter = createXMLExporter(viewWriter, parameters.getReferenceType());

        // Export
        exportView(xmlExporter, parameters, progress);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(org.alfresco.service.cmr.view.ExportPackageHandler, org.alfresco.service.cmr.view.ExporterCrawlerParameters, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(
            ExportPackageHandler exportHandler, ExporterCrawlerParameters parameters, Exporter progress) {
        ParameterCheck.mandatory("Stream Handler", exportHandler);

        // create exporter around export handler
        exportHandler.startExport();
        OutputStream dataFile = exportHandler.createDataStream();
        Exporter xmlExporter = createXMLExporter(dataFile, parameters.getReferenceType());
        URLExporter urlExporter = new URLExporter(xmlExporter, exportHandler);

        // export
        exportView(urlExporter, parameters, progress);

        // end export
        exportHandler.endExport();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(org.alfresco.service.cmr.view.Exporter, org.alfresco.service.cmr.view.ExporterCrawler, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(
            Exporter exporter, ExporterCrawlerParameters parameters, Exporter progress) {
        ParameterCheck.mandatory("Exporter", exporter);

        // Build the version store reference
        StoreRef versionStore = new StoreRef("workspace", "version2Store");

        // Get the root of the version store
        versionStoreRootNoderef = nodeService.getRootNode(versionStore);

        // Initialize the list of ML nodeRefs that have been visited
        List<String> visitedMLNodeRefs = new ArrayList<>();

        ChainedExporter chainedExporter = new ChainedExporter(new Exporter[]{exporter, progress});
        DefaultCrawler crawler = new DefaultCrawler();
        crawler.export(parameters, chainedExporter, visitedMLNodeRefs);
    }

    /**
     * Create an XML Exporter that exports repository information to the specified output stream in
     * xml format.
     *
     * @param viewWriter    the output stream to write to
     * @param referenceType the format of references to export
     * @return the xml exporter
     */
    private Exporter createXMLExporter(OutputStream viewWriter, ReferenceType referenceType) {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(indentSize);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter
        try {
            XMLWriter writer = new XMLWriter(viewWriter, format);
            ViewXMLExporter exporter =
                    new ViewXMLExporter(
                            namespaceService,
                            nodeService,
                            searchService,
                            dictionaryService,
                            permissionService,
                            writer);
            exporter.setReferenceType(referenceType);
            return exporter;
        } catch (Exception e) {
            throw new ExporterException("Failed to create XML Writer for export", e);
        }
    }

    /**
     * Responsible for navigating the Repository from specified location and invoking the provided
     * exporter call-back for the actual export implementation.
     *
     * @author David Caruana
     */
    private class DefaultCrawler implements ExporterCrawler {

        private ExporterContext context;
        private Map<NodeRef, NodeRef> nodesWithSecondaryLinks = new HashMap<>();
        private Map<NodeRef, NodeRef> nodesWithAssociations = new HashMap<>();
        private Map<QName, Integer> entitiesPerType = new TreeMap<>();

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterCrawler#export(org.alfresco.service.cmr.view.Exporter)
         */
        public void export(
                ExporterCrawlerParameters parameters, Exporter exporter, List<String> visitedMLNodeRefs) {
            // Initialise Crawler
            nodesWithSecondaryLinks.clear();
            nodesWithAssociations.clear();
            entitiesPerType.clear();
            context = new ExporterContextImpl(parameters);
            exporter.start(context);

            //
            // Export Nodes
            //

            while (context.canRetrieve()) {
                // determine if root repository node
                NodeRef nodeRef = context.getExportOf();
                if (parameters.isCrawlSelf()) {
                    // export root node of specified export location
                    walkStartNamespaces(parameters, exporter);
                    boolean rootNode = nodeService.getRootNode(nodeRef.getStoreRef()).equals(nodeRef);
                    walkNode(nodeRef, parameters, exporter, rootNode, visitedMLNodeRefs);
                    walkEndNamespaces(parameters, exporter);
                } else if (parameters.isCrawlChildNodes()) {
                    // export child nodes only
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
                    for (ChildAssociationRef childAssoc : childAssocs) {
                        walkStartNamespaces(parameters, exporter);
                        try {
                            walkNode(childAssoc.getChildRef(), parameters, exporter, false, visitedMLNodeRefs);
                        } catch (RuntimeException e) {
                            logger.error(
                                    "ERROR >> Parent NodeRef: "
                                            + nodeRef
                                            + ", ChildRef (non-existing): "
                                            + childAssoc.getChildRef(),
                                    e);
                            throw e;
                        }
                        walkEndNamespaces(parameters, exporter);
                    }
                }

                //
                // Export Secondary Links between Nodes
                //
                for (NodeRef nodeWithAssociations : nodesWithSecondaryLinks.keySet()) {
                    String authorityName =
                            (String)
                                    nodeService.getProperty(nodeWithAssociations, ContentModel.PROP_AUTHORITY_NAME);
                    // This check needs to be done to stop link export of children of a SUBSGROUP authority
                    // for CIRCABC in case only the root node needs to be exported
                    if (!(parameters.isExportOnlyRoot()
                            && authorityName != null
                            && authorityName.contains(CircabcExporter.SUBSGROUP))) {
                        walkStartNamespaces(parameters, exporter);
                        walkNodeSecondaryLinks(nodeWithAssociations, parameters, exporter);
                        walkEndNamespaces(parameters, exporter);
                    }
                }

                //
                // Export Associations between Nodes
                //
                for (NodeRef nodeWithAssociations : nodesWithAssociations.keySet()) {
                    walkStartNamespaces(parameters, exporter);
                    walkNodeAssociations(nodeWithAssociations, parameters, exporter);
                    walkEndNamespaces(parameters, exporter);
                }

                context.setNextValue();
            }
            exporter.end();

            if (logger.isInfoEnabled()) {
                logger.info("Number of exported entities per type :\n" + displayEntitiesPerType());
            }
        }

        /**
         * Call-backs for start of Namespace scope
         */
        private void walkStartNamespaces(ExporterCrawlerParameters parameters, Exporter exporter) {
            Collection<String> prefixes = namespaceService.getPrefixes();
            for (String prefix : prefixes) {
                if (!prefix.equals("xml")) {
                    String uri = namespaceService.getNamespaceURI(prefix);
                    exporter.startNamespace(prefix, uri);
                }
            }
        }

        /**
         * Call-backs for end of Namespace scope
         */
        private void walkEndNamespaces(ExporterCrawlerParameters parameters, Exporter exporter) {
            Collection<String> prefixes = namespaceService.getPrefixes();
            for (String prefix : prefixes) {
                if (!prefix.equals("xml")) {
                    exporter.endNamespace(prefix);
                }
            }
        }

        /**
         * Navigate a Node.
         *
         * @param nodeRef the node to navigate
         */
        private void walkNode(
                NodeRef nodeRef,
                ExporterCrawlerParameters parameters,
                Exporter exporter,
                boolean exportAsRef,
                List<String> visitedMLNodeRefs) {
            // Export node (but only if it's not excluded from export)
            QName type = nodeService.getType(nodeRef);

            // Added to exclude user authorities
            if (!(parameters.getCrawlOnlyGivenUsers() == null
                    && parameters.getCrawlOnlyGivenGroups() == null)
                    && (ContentModel.TYPE_USER.equals(type)
                    || ContentModel.TYPE_PERSON.equals(type)
                    || ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type))) {

                Path path = nodeService.getPath(nodeRef);

                if (path.size() > 1) {
                    // a child name does not exist for root
                    Path.ChildAssocElement pathElement = (Path.ChildAssocElement) path.last();
                    QName childQName = pathElement.getRef().getQName();

                    if ((ContentModel.TYPE_USER.equals(type) || ContentModel.TYPE_PERSON.equals(type))
                            && !isIncludedUser(parameters.getCrawlOnlyGivenUsers(), childQName.getLocalName())) {
                        return;
                    } else if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)
                            && !isIncludedGroup(
                            parameters.getCrawlOnlyGivenGroups(), childQName.getLocalName())) {
                        return;
                    }
                }
            }

            if (isExcludedURI(parameters.getExcludeNamespaceURIs(), type.getNamespaceURI())) {
                return;
            }

            // explicitly included ?
            if (parameters.getIncludedPaths() != null) {
                String nodePathPrefixString = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
                if (!(isIncludedPath(parameters.getIncludedPaths(), nodePathPrefixString))) {
                    return;
                }
            }

            // export node as reference to node, or as the actual node
            if (exportAsRef) {
                exporter.startReference(nodeRef, null);
            } else {
                exporter.startNode(nodeRef);
            }

            boolean hasVersionableAspect = false;
            boolean hasMLlDocumentAspect = false;
            boolean hasCircaCategoryAspect = false;

            // Export node aspects
            exporter.startAspects(nodeRef);
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects) {
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), aspect.getNamespaceURI())) {
                    continue;
                } else if (isExcludedAspect(parameters.getExcludeAspects(), aspect)) {
                    continue;
                } else {
                    if (ContentModel.ASPECT_VERSIONABLE.equals(aspect)) {
                        hasVersionableAspect = true;
                    }
                    if (ContentModel.ASPECT_MULTILINGUAL_DOCUMENT.equals(aspect)) {
                        hasMLlDocumentAspect = true;
                    }
                    if (CircabcModel.ASPECT_CATEGORY.equals(aspect)) {
                        hasCircaCategoryAspect = true;
                    }

                    exporter.startAspect(nodeRef, aspect);
                    exporter.endAspect(nodeRef, aspect);
                }
            }
            exporter.endAspects(nodeRef);

            // Export node permissions
            AccessStatus readPermission =
                    permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS);
            if (authenticationService.isCurrentUserTheSystemUser()
                    || readPermission.equals(AccessStatus.ALLOWED)) {
                Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
                boolean inheritPermissions = permissionService.getInheritParentPermissions(nodeRef);
                if (permissions.size() > 0 || !inheritPermissions) {
                    exporter.startACL(nodeRef);
                    for (AccessPermission permission : permissions) {
                        if (permission.isSetDirectly()) {
                            exporter.permission(nodeRef, permission);
                        }
                    }
                    exporter.endACL(nodeRef);
                }
            }

            // Export node properties
            exporter.startProperties(nodeRef);
            boolean aware = MLPropertyInterceptor.setMLAware(true);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            MLPropertyInterceptor.setMLAware(aware);
            for (QName property : properties.keySet()) {
                // filter out properties whose namespace is excluded
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), property.getNamespaceURI())) {
                    continue;
                }
                if (isExcludedAspectProperty(parameters.getExcludeAspects(), property)) {
                    continue;
                }

                // filter out properties whose value is null, if not required
                Object value = properties.get(property);
                if (!parameters.isCrawlNullProperties() && value == null) {
                    continue;
                }

                // start export of property
                exporter.startProperty(nodeRef, property);

                if (value instanceof Collection) {
                    exporter.startValueCollection(nodeRef, property);
                    int index = 0;
                    for (Object valueInCollection : (Collection) value) {
                        walkProperty(nodeRef, property, valueInCollection, index, parameters, exporter);
                        index++;
                    }
                    exporter.endValueCollection(nodeRef, property);
                } else {
                    if (value instanceof MLText) {
                        MLText valueMLT = (MLText) value;
                        Set<Locale> locales = valueMLT.getLocales();
                        for (Locale locale : locales) {
                            String localeValue = valueMLT.getValue(locale);
                            if (localeValue == null) {
                                walkProperty(nodeRef, property, localeValue, -1, parameters, exporter);
                                continue;
                            }
                            exporter.startValueMLText(nodeRef, locale);
                            walkProperty(nodeRef, property, localeValue, -1, parameters, exporter);
                            exporter.endValueMLText(nodeRef);
                        }
                    } else {
                        walkProperty(nodeRef, property, value, -1, parameters, exporter);
                    }
                }

                // end export of property
                exporter.endProperty(nodeRef, property);
            }
            exporter.endProperties(nodeRef);

            // Export node children
            if (parameters.isCrawlChildNodes() || hasCircaCategoryAspect) {
                // sort associations into assoc type buckets filtering out unneccessary associations
                Map<QName, List<ChildAssociationRef>> assocTypes = new HashMap<>();
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
                for (ChildAssociationRef childAssoc : childAssocs) {
                    QName childAssocType = childAssoc.getTypeQName();
                    if (isExcludedURI(
                            parameters.getExcludeNamespaceURIs(), childAssocType.getNamespaceURI())) {
                        continue;
                    }
                    if (isExcludedChildAssoc(parameters.getExcludeChildAssocs(), childAssocType)) {
                        continue;
                    }
                    if (isExcludedAspectAssociation(parameters.getExcludeAspects(), childAssocType)) {
                        continue;
                    }
                    if (childAssoc.isPrimary() == false) {
                        nodesWithSecondaryLinks.put(nodeRef, nodeRef);
                        continue;
                    }
                    if (isExcludedURI(
                            parameters.getExcludeNamespaceURIs(), childAssoc.getQName().getNamespaceURI())) {
                        continue;
                    }

                    List<ChildAssociationRef> assocRefs = assocTypes.get(childAssocType);
                    if (assocRefs == null) {
                        assocRefs = new ArrayList<>();
                        assocTypes.put(childAssocType, assocRefs);
                    }
                    // Added the case to export only "circaCategoryProfileAssoc" in case only the root node of
                    // the Category has been selected for export
                    if ((!parameters.isCrawlChildNodes()
                            && hasCircaCategoryAspect
                            && CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE.equals(childAssocType))
                            || parameters.isCrawlChildNodes()) {
                        assocRefs.add(childAssoc);
                    }
                }

                // output each association type bucket
                if (assocTypes.size() > 0) {
                    exporter.startAssocs(nodeRef);
                    for (Map.Entry<QName, List<ChildAssociationRef>> assocType : assocTypes.entrySet()) {
                        List<ChildAssociationRef> assocRefs = assocType.getValue();
                        if (assocRefs.size() > 0) {
                            exporter.startAssoc(nodeRef, assocType.getKey());
                            for (ChildAssociationRef assocRef : assocRefs) {
                                walkNode(assocRef.getChildRef(), parameters, exporter, false, visitedMLNodeRefs);
                            }
                            exporter.endAssoc(nodeRef, assocType.getKey());
                        }
                    }
                    exporter.endAssocs(nodeRef);
                }
            }

            // Export node target associations
            if (parameters.isCrawlAssociations()) {
                List<AssociationRef> associations =
                        nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
                if (associations.size() > 0) {
                    nodesWithAssociations.put(nodeRef, nodeRef);
                }
            }

            // Export node versions (exclude recursively nested versions inside ML)
            // Exclude the version of the category root that comes from an old
            // version of CIRCABC
            if (hasVersionableAspect
                    && !ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(type)
                    && !hasCircaCategoryAspect) {

                String nodeId = nodeRef.getId();

                // Attempt to retrieve the current nodeRefs versions
                NodeRef versionedContainerNodeRef =
                        nodeService.getChildByName(
                                versionStoreRootNoderef, Version2Model.TYPE_QNAME_VERSION_HISTORY, nodeId);

                if (versionedContainerNodeRef != null) {

                    // Versions found
                    exporter.startVersions(versionedContainerNodeRef);

                    // Export the version nodes
                    walkNode(versionedContainerNodeRef, parameters, exporter, exportAsRef, visitedMLNodeRefs);

                    exporter.endVersions(versionedContainerNodeRef);
                }
            }

            // Export node multilingual translations
            if (hasMLlDocumentAspect) {

                List<ChildAssociationRef> mlContainers =
                        nodeService.getParentAssocs(
                                nodeRef, ContentModel.ASSOC_MULTILINGUAL_CHILD, RegexQNamePattern.MATCH_ALL);

                if (mlContainers.size() > 1) {
                    throw new ExporterException(
                            "There should be only one container to store the translations of a content nodeRef. Offending nodeRef: "
                                    + nodeRef.toString());
                } else if (mlContainers.size() == 1) {

                    NodeRef mlContainerNodeRef = mlContainers.get(0).getParentRef();

                    String mlContainerNodeId = mlContainerNodeRef.getId();

                    if (!visitedMLNodeRefs.contains(mlContainerNodeId)) {

                        visitedMLNodeRefs.add(mlContainerNodeId);

                        exporter.startMLTranslations(mlContainerNodeRef);

                        walkNode(mlContainerNodeRef, parameters, exporter, exportAsRef, visitedMLNodeRefs);

                        exporter.endMLTranslations(mlContainerNodeRef);
                    }
                }
            }

            // Signal end of node
            // export node as reference to node, or as the actual node
            if (exportAsRef) {
                exporter.endReference(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "End reference to node (signal end of node): "
                                    + getNodeInfo(nodeRef, type, getNodeName(properties)));
                }
            } else {
                exporter.endNode(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug("Exporting node : " + getNodeInfo(nodeRef, type, getNodeName(properties)));
                }
                // Track the number of entities we export per type
                addType(type);
            }
        }

        private void addType(QName type) {
            if (entitiesPerType.containsKey(type)) {
                Integer newValue = entitiesPerType.get(type) + 1;
                entitiesPerType.put(type, newValue);
            } else {
                entitiesPerType.put(type, 1);
            }
        }

        private String displayEntitiesPerType() {
            StringBuilder log = new StringBuilder();

            for (Map.Entry<QName, Integer> entry : entitiesPerType.entrySet()) {
                log.append(entry.getKey()).append(";").append(entry.getValue()).append("\n");
            }

            return log.toString();
        }

        private String getNodeName(Map<QName, Serializable> properties) {
            if (properties.containsKey(ContentModel.PROP_NAME)) {
                return (String) properties.get(ContentModel.PROP_NAME);
            }

            return null;
        }

        private String getNodeInfo(NodeRef nodeRef, QName type, String nodeName) {
            String log =
                    "Node[ref="
                            + nodeRef
                            + ", name="
                            + (nodeName != null ? nodeName : "<No cm:name was defined for this node>")
                            + ", type="
                            + type.getLocalName()
                            + "]";

            return log;
        }

        /**
         * Export Property
         */
        private void walkProperty(
                NodeRef nodeRef,
                QName property,
                Object value,
                int index,
                ExporterCrawlerParameters parameters,
                Exporter exporter) {
            // determine data type of value
            PropertyDefinition propDef = dictionaryService.getProperty(property);
            DataTypeDefinition dataTypeDef = (propDef == null) ? null : propDef.getDataType();
            QName valueDataType = null;
            if (dataTypeDef == null || dataTypeDef.getName().equals(DataTypeDefinition.ANY)) {
                dataTypeDef = (value == null) ? null : dictionaryService.getDataType(value.getClass());
                if (dataTypeDef != null) {
                    valueDataType = dataTypeDef.getName();
                }
            } else {
                valueDataType = dataTypeDef.getName();
            }

            if (valueDataType == null || !valueDataType.equals(DataTypeDefinition.CONTENT)) {
                // Export non content data types
                try {
                    exporter.value(nodeRef, property, value, index);
                } catch (TypeConversionException e) {
                    String err = "Value of property " + property + " could not be converted to xml string";
                    exporter.warning(err);
                    //                    exporter.value(nodeRef, property, (value == null ? null :
                    // value.toString()), index);
                    throw new ExporterException(err, e);
                }
            } else {
                // export property of datatype CONTENT
                ContentReader reader = contentService.getReader(nodeRef, property);
                if (!parameters.isCrawlContent() || reader == null || reader.exists() == false) {
                    // export an empty url for the content
                    ContentData contentData = (ContentData) value;
                    ContentData noContentURL = null;
                    if (contentData == null) {
                        noContentURL = new ContentData("", null, 0L, "UTF-8");
                    } else {
                        noContentURL =
                                new ContentData(
                                        "",
                                        contentData.getMimetype(),
                                        contentData.getSize(),
                                        contentData.getEncoding());
                    }
                    exporter.content(nodeRef, property, null, noContentURL, index);
                    exporter.warning("Skipped content for property " + property + " on node " + nodeRef);
                } else {
                    InputStream inputStream = reader.getContentInputStream();
                    try {
                        exporter.content(nodeRef, property, inputStream, reader.getContentData(), index);
                    } catch (Exception e) {
                        throw new ExporterException("Failed to export node content for node " + nodeRef, e);
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Failed to close stream  for node " + nodeRef, e);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Export Secondary Links
         */
        private void walkNodeSecondaryLinks(
                NodeRef nodeRef, ExporterCrawlerParameters parameters, Exporter exporter) {
            // sort associations into assoc type buckets filtering out unneccessary associations
            Map<QName, List<ChildAssociationRef>> assocTypes = new HashMap<>();
            List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssoc : childAssocs) {
                // determine if child association should be exported
                QName childAssocType = childAssoc.getTypeQName();
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), childAssocType.getNamespaceURI())) {
                    continue;
                }
                if (isExcludedChildAssoc(parameters.getExcludeChildAssocs(), childAssocType)) {
                    continue;
                }
                if (isExcludedAspectAssociation(parameters.getExcludeAspects(), childAssocType)) {
                    continue;
                }
                if (childAssoc.isPrimary()) {
                    continue;
                }
                if (!isWithinExport(childAssoc.getChildRef(), parameters)) {
                    continue;
                }

                List<ChildAssociationRef> assocRefs = assocTypes.get(childAssocType);
                if (assocRefs == null) {
                    assocRefs = new ArrayList<>();
                    assocTypes.put(childAssocType, assocRefs);
                }
                assocRefs.add(childAssoc);
            }

            // output each association type bucket
            if (assocTypes.size() > 0) {
                // If it is the share association, discard it (only present in CIRCABC 3.8 onwards)
                String assocName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                if (APP_SHARE.equals(assocName)) {
                    return;
                }

                exporter.startReference(nodeRef, null);
                exporter.startAssocs(nodeRef);
                for (Map.Entry<QName, List<ChildAssociationRef>> assocType : assocTypes.entrySet()) {
                    // Include only cm:inZone that refer to the selected authorities
                    if (!(parameters.getCrawlOnlyGivenUsers() == null
                            && parameters.getCrawlOnlyGivenGroups() == null)
                            && ContentModel.ASSOC_IN_ZONE.equals(assocType.getKey())) {

                        List<ChildAssociationRef> assocRefs = assocType.getValue();
                        if (assocRefs.size() > 0) {

                            exporter.startAssoc(nodeRef, assocType.getKey());
                            for (ChildAssociationRef assocRef : assocRefs) {

                                Path path = nodeService.getPath(assocRef.getChildRef());

                                if (path.size() > 1) {

                                    // a child name does not exist for root
                                    Path.ChildAssocElement pathElement = (Path.ChildAssocElement) path.last();
                                    QName childQName = pathElement.getRef().getQName();

                                    if (!(isIncludedUser(
                                            parameters.getCrawlOnlyGivenUsers(), childQName.getLocalName())
                                            || isIncludedGroup(
                                            parameters.getCrawlOnlyGivenGroups(), childQName.getLocalName()))) {
                                        continue;
                                    }
                                }

                                exporter.startReference(assocRef.getChildRef(), assocRef.getQName());
                                exporter.endReference(assocRef.getChildRef());
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "End cm:inZone reference: "
                                                    + getNodeInfo(
                                                    nodeRef,
                                                    nodeService.getType(nodeRef),
                                                    getNodeName(nodeService.getProperties(nodeRef))));
                                }
                            }
                            exporter.endAssoc(nodeRef, assocType.getKey());
                        }
                    } else {
                        // Process other assocs
                        List<ChildAssociationRef> assocRefs = assocType.getValue();
                        if (assocRefs.size() > 0) {
                            exporter.startAssoc(nodeRef, assocType.getKey());
                            for (ChildAssociationRef assocRef : assocRefs) {
                                exporter.startReference(assocRef.getChildRef(), assocRef.getQName());
                                exporter.endReference(assocRef.getChildRef());
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "End assoc secondary reference: "
                                                    + getNodeInfo(
                                                    nodeRef,
                                                    nodeService.getType(nodeRef),
                                                    getNodeName(nodeService.getProperties(nodeRef))));
                                }
                            }
                            exporter.endAssoc(nodeRef, assocType.getKey());
                        }
                    }
                }
                exporter.endAssocs(nodeRef);
                exporter.endReference(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "End assocs reference: "
                                    + getNodeInfo(
                                    nodeRef,
                                    nodeService.getType(nodeRef),
                                    getNodeName(nodeService.getProperties(nodeRef))));
                }
            }
        }

        /**
         * Export Node Associations
         */
        private void walkNodeAssociations(
                NodeRef nodeRef, ExporterCrawlerParameters parameters, Exporter exporter) {
            // sort associations into assoc type buckets filtering out unneccessary associations
            Map<QName, List<AssociationRef>> assocTypes = new HashMap<>();
            List<AssociationRef> assocs =
                    nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef assoc : assocs) {
                QName assocType = assoc.getTypeQName();
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), assocType.getNamespaceURI())) {
                    continue;
                }
                if (!isWithinExport(assoc.getTargetRef(), parameters)
                        && !Version2Model.ASSOC_ROOT_VERSION.equals(assocType)) {
                    continue;
                }

                List<AssociationRef> assocRefs = assocTypes.get(assocType);
                if (assocRefs == null) {
                    assocRefs = new ArrayList<>();
                    assocTypes.put(assocType, assocRefs);
                }
                assocRefs.add(assoc);
            }

            // output each association type bucket
            if (assocTypes.size() > 0) {
                exporter.startReference(nodeRef, null);
                exporter.startAssocs(nodeRef);
                for (Map.Entry<QName, List<AssociationRef>> assocType : assocTypes.entrySet()) {
                    List<AssociationRef> assocRefs = assocType.getValue();
                    if (assocRefs.size() > 0) {
                        exporter.startAssoc(nodeRef, assocType.getKey());
                        for (AssociationRef assocRef : assocRefs) {
                            exporter.startReference(assocRef.getTargetRef(), null);
                            exporter.endReference(assocRef.getTargetRef());
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "End assoc reference: "
                                                + getNodeInfo(
                                                nodeRef,
                                                nodeService.getType(nodeRef),
                                                getNodeName(nodeService.getProperties(nodeRef))));
                            }
                        }
                        exporter.endAssoc(nodeRef, assocType.getKey());
                    }
                }
                exporter.endAssocs(nodeRef);
                exporter.endReference(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "End assocs reference: "
                                    + getNodeInfo(
                                    nodeRef,
                                    nodeService.getType(nodeRef),
                                    getNodeName(nodeService.getProperties(nodeRef))));
                }
            }
        }

        /**
         * Is the specified URI an excluded URI?
         *
         * @param uri the URI to test
         * @return true => it's excluded from the export
         */
        private boolean isExcludedURI(String[] excludeNamespaceURIs, String uri) {
            for (String excludedURI : excludeNamespaceURIs) {
                if (uri.equals(excludedURI)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isIncludedUser(String[] excludeUserNames, String userName) {
            if (excludeUserNames == null) {
                return false;
            }

            for (String excludedUserName : excludeUserNames) {
                if (userName.equals(excludedUserName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isIncludedGroup(String[] excludeGroupNames, String groupName) {
            if (excludeGroupNames == null) {
                return false;
            }

            for (String excludedGroupName : excludeGroupNames) {
                if (groupName.equals(excludedGroupName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isIncludedPath(String[] includedPaths, String path) {
            for (String includePath : includedPaths) {
                // note: allow parents or children - e.g. if included path is /a/b/c then /, /a, /a/b,
                // /a/b/c, /a/b/c/d, /a/b/c/d/e are all included
                if (includePath.startsWith(path) || path.startsWith(includePath)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Is the aspect unexportable?
         *
         * @param aspectQName the aspect name
         * @return <tt>true</tt> if the aspect can't be exported
         */
        private boolean isExcludedAspect(QName[] excludeAspects, QName aspectQName) {
            //            if (aspectQName.equals(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) ||
            //                    aspectQName.equals(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            //            {
            //                return true;
            //            }
            //            else
            //            {
            for (QName excludeAspect : excludeAspects) {
                if (aspectQName.equals(excludeAspect)) {
                    return true;
                }
            }
            //            }
            return false;
        }

        /**
         * Is the child association unexportable?
         *
         * @param childAssocQName the child assoc name
         * @return <tt>true</tt> if the aspect can't be exported
         */
        private boolean isExcludedChildAssoc(QName[] excludeChildAssocs, QName childAssocQName) {
            for (QName excludeChildAssoc : excludeChildAssocs) {
                if (childAssocQName.equals(excludeChildAssoc)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is the property unexportable?
         */
        private boolean isExcludedAspectProperty(QName[] excludeAspects, QName propertyQName) {
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            if (propDef == null) {
                return false;
            }

            ClassDefinition classDef = propDef.getContainerClass();
            if (classDef == null || !classDef.isAspect()) {
                return false;
            }

            return isExcludedAspect(excludeAspects, classDef.getName());
        }

        /**
         * Is the association unexportable?
         */
        private boolean isExcludedAspectAssociation(QName[] excludeAspects, QName associationQName) {
            AssociationDefinition assocDef = dictionaryService.getAssociation(associationQName);
            if (assocDef == null) {
                return false;
            }

            ClassDefinition classDef = assocDef.getSourceClass();
            if (classDef == null || !classDef.isAspect()) {
                return false;
            }

            return isExcludedAspect(excludeAspects, classDef.getName());
        }

        /**
         * Determine if specified Node Reference is within the set of nodes to be exported
         *
         * @param nodeRef node reference to check
         * @return true => node reference is within export set
         */
        private boolean isWithinExport(NodeRef nodeRef, ExporterCrawlerParameters parameters) {
            boolean isWithin = false;

            // Current strategy is to determine if node is a child of the root exported node
            for (NodeRef exportRoot : context.getExportList()) {
                if (nodeRef.equals(exportRoot) && parameters.isCrawlSelf() == true) {
                    // node to export is the root export node (and root is to be exported)
                    isWithin = true;
                } else {
                    // locate export root in primary parent path of node
                    Path nodePath = nodeService.getPath(nodeRef);
                    for (int i = nodePath.size() - 1; i >= 0; i--) {
                        Path.ChildAssocElement pathElement = (Path.ChildAssocElement) nodePath.get(i);
                        if (pathElement.getRef().getChildRef().equals(exportRoot)) {
                            isWithin = true;
                            break;
                        }
                    }
                }
            }
            return isWithin;
        }
    }

    /**
     * Exporter Context
     */
    private class ExporterContextImpl implements ExporterContext {

        private NodeRef[] exportList;
        private NodeRef[] parentList;
        private String exportedBy;
        private Date exportedDate;
        private String exporterVersion;

        private int index;

        /**
         * Construct
         *
         * @param parameters exporter crawler parameters
         */
        public ExporterContextImpl(ExporterCrawlerParameters parameters) {
            index = 0;

            // get current user performing export
            String currentUserName = authenticationService.getCurrentUserName();
            exportedBy = (currentUserName == null) ? "unknown" : currentUserName;

            // get current date
            exportedDate = new Date(System.currentTimeMillis());

            // get list of exported nodes
            exportList =
                    (parameters.getExportFrom() == null) ? null : parameters.getExportFrom().getNodeRefs();
            if (exportList == null) {
                // multi-node export
                exportList = new NodeRef[1];
                NodeRef exportOf = getNodeRef(parameters.getExportFrom());
                exportList[0] = exportOf;
            }
            parentList = new NodeRef[exportList.length];
            for (int i = 0; i < exportList.length; i++) {
                parentList[i] = getParent(exportList[i], parameters.isCrawlSelf());
            }

            // get exporter version
            exporterVersion = descriptorService.getServerDescriptor().getVersion();
        }

        public boolean canRetrieve() {
            return index < exportList.length;
        }

        public int setNextValue() {
            return ++index;
        }

        public void resetContext() {
            index = 0;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportedBy()
         */
        public String getExportedBy() {
            return exportedBy;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportedDate()
         */
        public Date getExportedDate() {
            return exportedDate;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExporterVersion()
         */
        public String getExporterVersion() {
            return exporterVersion;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportOf()
         */
        public NodeRef getExportOf() {
            if (canRetrieve()) {
                return exportList[index];
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportParent()
         */
        public NodeRef getExportParent() {
            if (canRetrieve()) {
                return parentList[index];
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportList()
         */
        public NodeRef[] getExportList() {
            return exportList;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportParentList()
         */
        public NodeRef[] getExportParentList() {
            return parentList;
        }

        /**
         * Get the Node Ref from the specified Location
         *
         * @param location the location
         * @return the node reference
         */
        private NodeRef getNodeRef(Location location) {
            ParameterCheck.mandatory("Location", location);

            // Establish node to export from
            NodeRef nodeRef = (location == null) ? null : location.getNodeRef();
            if (nodeRef == null) {
                // If a specific node has not been provided, default to the root
                nodeRef = nodeService.getRootNode(location.getStoreRef());
            }

            // Resolve to path within node, if one specified
            String path = (location == null) ? null : location.getPath();
            if (path != null && path.length() > 0) {
                // Create a valid path and search
                List<NodeRef> nodeRefs =
                        searchService.selectNodes(nodeRef, path, null, namespaceService, false);
                if (nodeRefs.size() == 0) {
                    throw new ImporterException(
                            "Path "
                                    + path
                                    + " within node "
                                    + nodeRef
                                    + " does not exist - the path must resolve to a valid location");
                }
                if (nodeRefs.size() > 1) {
                    throw new ImporterException(
                            "Path "
                                    + path
                                    + " within node "
                                    + nodeRef
                                    + " found too many locations - the path must resolve to one location");
                }
                nodeRef = nodeRefs.get(0);
            }

            // TODO: Check Node actually exists

            return nodeRef;
        }

        /**
         * Gets the parent node of the items to be exported
         */
        private NodeRef getParent(NodeRef exportOf, boolean exportSelf) {
            NodeRef parent = null;

            if (exportSelf) {
                NodeRef rootNode = nodeService.getRootNode(exportOf.getStoreRef());
                if (rootNode.equals(exportOf)) {
                    parent = exportOf;
                } else {
                    ChildAssociationRef parentRef = nodeService.getPrimaryParent(exportOf);
                    parent = parentRef.getParentRef();
                }
            } else {
                parent = exportOf;
            }

            return parent;
        }
    }
}
