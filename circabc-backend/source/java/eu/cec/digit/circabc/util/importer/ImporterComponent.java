package eu.cec.digit.circabc.util.importer;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.util.CommonUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.*;
import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.*;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.*;

/**
 * Default implementation of the Importer Service
 *
 * @author David Caruana
 */
public class ImporterComponent
        implements CircabcImporterService {

    // Logger
    private static final Log logger = LogFactory.getLog(ImporterComponent.class);
    // binding markers
    private static final String START_BINDING_MARKER = "${";
    private static final String END_BINDING_MARKER = "}";
    // a list of nodes already created by the system so we will skip their creation and return their reference
    private static final List<String> EXCLUDED_NODE_NAMES = Arrays
            .asList("sys:people", "sys:workflow", "cm:packages", "sys:authorities",
                    "sys:zones", "cm:AUTH.ALF", "cm:APP.DEFAULT", "cm:APP.SHARE");
    // default importer
    // TODO: Allow registration of plug-in parsers (by namespace)
    private Parser viewParser;
    // supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private BehaviourFilter behaviourFilter;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private RuleService ruleService;
    private PermissionService permissionService;
    private AuthenticationContext authenticationContext;
    private MultilingualContentService multilingualContentService;
    // reference to the version2Store root node
    private NodeRef versionStoreRootNoderef = null;

    /**
     * @param viewParser the default parser
     */
    public void setViewParser(Parser viewParser) {
        this.viewParser = viewParser;
    }

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
     * @param behaviourFilter policy behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * TODO: Remove this in favour of appropriate rule disabling
     *
     * @param ruleService rule service
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * @param permissionService permissionService
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param authenticationContext authenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * @param multilingualContentService multilingualContentService
     */
    public void setMultilingualContentService(
            MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterService#importView(java.io.InputStreamReader, org.alfresco.service.cmr.view.Location, java.util.Properties, org.alfresco.service.cmr.view.ImporterProgress)
     */
    public void importView(Reader viewReader, Location location, ImporterBinding binding,
                           ImporterProgress progress) {
        NodeRef nodeRef = getNodeRef(location, binding);
        parserImport(nodeRef, location.getChildAssocType(), viewReader, new DefaultStreamHandler(),
                binding, progress);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterService#importView(org.alfresco.service.cmr.view.ImportPackageHandler, org.alfresco.service.cmr.view.Location, org.alfresco.service.cmr.view.ImporterBinding, org.alfresco.service.cmr.view.ImporterProgress)
     */
    public void importView(ImportPackageHandler importHandler, Location location,
                           ImporterBinding binding, ImporterProgress progress) throws ImporterException {
        importHandler.startImport();
        Reader dataFileReader = importHandler.getDataStream();
        NodeRef nodeRef = getNodeRef(location, binding);
        parserImport(nodeRef, location.getChildAssocType(), dataFileReader, importHandler, binding,
                progress);
        importHandler.endImport();
    }

    /**
     * Get Node Reference from Location
     *
     * @param location the location to extract node reference from
     * @param binding  import configuration
     * @return node reference
     */
    private NodeRef getNodeRef(Location location, ImporterBinding binding) {
        ParameterCheck.mandatory("Location", location);

        // Establish node to import within
        NodeRef nodeRef = location.getNodeRef();
        if (nodeRef == null) {
            // If a specific node has not been provided, default to the root
            nodeRef = nodeService.getRootNode(location.getStoreRef());
        }

        // Resolve to path within node, if one specified
        String path = location.getPath();
        if (path != null && path.length() > 0) {
            // Create a valid path and search
            path = bindPlaceHolder(path, binding);
            path = createValidPath(path);
            List<NodeRef> nodeRefs = searchService
                    .selectNodes(nodeRef, path, null, namespaceService, false);
            if (nodeRefs.size() == 0) {
                throw new ImporterException("Path " + path + " within node " + nodeRef
                        + " does not exist - the path must resolve to a valid location");
            }
            if (nodeRefs.size() > 1) {
                throw new ImporterException("Path " + path + " within node " + nodeRef
                        + " found too many locations - the path must resolve to one location");
            }
            nodeRef = nodeRefs.get(0);
        }

        // TODO: Check Node actually exists

        return nodeRef;
    }

    /**
     * Bind the specified value to the passed configuration values if it is a place holder
     *
     * @param value   the value to bind
     * @param binding the configuration properties to bind to
     * @return the bound value
     */
    private String bindPlaceHolder(String value, ImporterBinding binding) {
        if (binding != null) {
            int iStartBinding = value.indexOf(START_BINDING_MARKER);
            while (iStartBinding != -1) {
                int iEndBinding = value
                        .indexOf(END_BINDING_MARKER, iStartBinding + START_BINDING_MARKER.length());
                if (iEndBinding == -1) {
                    throw new ImporterException(
                            "Cannot find end marker " + END_BINDING_MARKER + " within value " + value);
                }

                String key = value.substring(iStartBinding + START_BINDING_MARKER.length(), iEndBinding);
                String keyValue = binding.getValue(key);
                if (keyValue == null) {
                    logger.warn("No binding value for placeholder (will default to empty string): " + value);
                }
                value = StringUtils.replace(value, START_BINDING_MARKER + key + END_BINDING_MARKER,
                        keyValue == null ? "" : keyValue);
                iStartBinding = value.indexOf(START_BINDING_MARKER);
            }
        }
        return value;
    }

    /**
     * Create a valid qname-based xpath
     * <p>
     * Note: - the localname will be truncated to 100 chars - the localname should already be encoded
     * for ISO 9075 (in case of MT bootstrap, the @ sign will be auto-encoded, see below)
     * <p>
     * Some examples: / sys:people/cm:admin /app:company_home/app:dictionary
     * ../../cm:people_x0020_folder sys:people/cm:admin_x0040_test
     */
    private String createValidPath(String path) {
        StringBuilder validPath = new StringBuilder(path.length());
        String[] segments = StringUtils.delimitedListToStringArray(path, "/");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] != null && segments[i].length() > 0) {
                int colonIndex = segments[i].indexOf(QName.NAMESPACE_PREFIX);
                if (colonIndex == -1) {
                    // eg. ".."
                    validPath.append(segments[i]);
                } else {
                    String[] qnameComponents = QName.splitPrefixedQName(segments[i]);

                    String localName = QName.createValidLocalName(qnameComponents[1]);

                    // MT: bootstrap of "alfrescoUserStore.xml" requires 'sys:people/cm:admin@tenant' to be encoded as 'sys:people/cm:admin_x0040_tenant' (for XPath)
                    localName = localName.replace("@", "_x0040_");

                    QName segmentQName = QName.createQName(qnameComponents[0], localName, namespaceService);
                    validPath.append(segmentQName.toPrefixString());
                }
            }
            if (i < (segments.length - 1)) {
                validPath.append("/");
            }
        }
        return validPath.toString();
    }

    /**
     * Perform Import via Parser
     *
     * @param nodeRef        node reference to import under
     * @param childAssocType the child association type to import under
     * @param inputStream    the input stream to import from
     * @param streamHandler  the content property import stream handler
     * @param binding        import configuration
     * @param progress       import progress
     */
    public void parserImport(NodeRef nodeRef, QName childAssocType, Reader viewReader,
                             ImportPackageHandler streamHandler, ImporterBinding binding, ImporterProgress progress) {
        ParameterCheck.mandatory("Node Reference", nodeRef);
        ParameterCheck.mandatory("View Reader", viewReader);
        ParameterCheck.mandatory("Stream Handler", streamHandler);

        Importer nodeImporter = new NodeImporter(nodeRef, childAssocType, binding, streamHandler,
                progress);
        try {
            nodeImporter.start();
            viewParser.parse(viewReader, nodeImporter);
            nodeImporter.end();

            if (logger.isInfoEnabled()) {
                logger.info("Number of imported entities per type :\n" + ((NodeImporter) nodeImporter)
                        .displayEntitiesPerType());
            }
            if (viewParser instanceof ViewParser && ((ViewParser) viewParser).hasIncompleteReferences()) {
                logger.warn("Incomplete(s) reference(s):\n" + ((ViewParser) viewParser)
                        .displayIncompleteReferences());
            }
        } catch (RuntimeException e) {
            nodeImporter.error(e);
            throw e;
        }
    }

    /**
     * Perform import via Content Handler
     *
     * @param nodeRef        node reference to import under
     * @param childAssocType the child association type to import under
     * @param handler        the import content handler
     * @param binding        import configuration
     * @param progress       import progress
     * @return content handler to interact with
     */
    public ContentHandler handlerImport(NodeRef nodeRef, QName childAssocType,
                                        ImportContentHandler handler, ImporterBinding binding, ImporterProgress progress) {
        ParameterCheck.mandatory("Node Reference", nodeRef);

        DefaultContentHandler defaultHandler = new DefaultContentHandler(handler);
        ImportPackageHandler streamHandler = new ContentHandlerStreamHandler(defaultHandler);
        Importer nodeImporter = new NodeImporter(nodeRef, childAssocType, binding, streamHandler,
                progress);
        defaultHandler.setImporter(nodeImporter);
        return defaultHandler;
    }

    /**
     * Encapsulate how a node is imported into the repository
     */
    public interface NodeImporterStrategy {

        /**
         * Import a node
         *
         * @param node to import
         */
        NodeRef importNode(ImportNode node);
    }

    /**
     * Imported Node Reference
     *
     * @author David Caruana
     */
    private static class ImportedNodeRef {

        private ImportNode context;
        private QName property;
        private Serializable value;
        /**
         * Construct
         */
        private ImportedNodeRef(ImportNode context, QName property, Serializable value) {
            this.context = context;
            this.property = property;
            this.value = value;
        }
    }

    /**
     * Default Import Stream Handler
     *
     * @author David Caruana
     */
    private static class DefaultStreamHandler
            implements ImportPackageHandler {

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
         */
        public void startImport() {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
         */
        public InputStream importStream(String content) {
            ResourceLoader loader = new DefaultResourceLoader();
            Resource resource = loader.getResource(content);
            if (resource.exists() == false) {
                throw new ImporterException("Content URL " + content + " does not exist.");
            }

            try {
                return resource.getInputStream();
            } catch (IOException e) {
                throw new ImporterException("Failed to retrieve input stream for content URL " + content);
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
         */
        public Reader getDataStream() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#endImport()
         */
        public void endImport() {
        }
    }

    /**
     * Default Import Stream Handler
     *
     * @author David Caruana
     */
    private static class ContentHandlerStreamHandler
            implements ImportPackageHandler {

        private ImportContentHandler handler;

        /**
         * Construct
         */
        private ContentHandlerStreamHandler(ImportContentHandler handler) {
            this.handler = handler;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
         */
        public void startImport() {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
         */
        public InputStream importStream(String content) {
            return handler.importStream(content);
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
         */
        public Reader getDataStream() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#endImport()
         */
        public void endImport() {
        }
    }

    /**
     * Default Importer strategy
     *
     * @author David Caruana
     */
    public class NodeImporter
            implements Importer {

        private static final String VER2_PREFIX = "/ver2:";
        private NodeRef rootRef;
        private QName rootAssocType;
        private ImporterBinding binding;
        private ImporterProgress progress;
        private ImportPackageHandler streamHandler;
        private NodeImporterStrategy importStrategy;
        private UpdateExistingNodeImporterStrategy updateStrategy;
        private QName[] excludedClasses;
        // Import tracking
        private List<ImportedNodeRef> nodeRefs = new ArrayList<>();
        // Track the number of entities per type
        private Map<QName, Integer> entitiesPerType = new TreeMap<>();

        /**
         * Construct
         */
        private NodeImporter(NodeRef rootRef, QName rootAssocType, ImporterBinding binding,
                             ImportPackageHandler streamHandler, ImporterProgress progress) {
            this.rootRef = rootRef;
            this.rootAssocType = rootAssocType;
            this.binding = binding;
            this.progress = progress;
            this.streamHandler = streamHandler;
            this.importStrategy = createNodeImporterStrategy(
                    binding == null ? null : binding.getUUIDBinding());
            this.updateStrategy = new UpdateExistingNodeImporterStrategy();

            entitiesPerType.clear();

            // initialise list of content models to exclude from import
            if (binding == null || binding.getExcludedClasses() == null) {
                this.excludedClasses = new QName[]{ContentModel.ASPECT_REFERENCEABLE,
                        ContentModel.ASPECT_VERSIONABLE};
            } else {
                this.excludedClasses = binding.getExcludedClasses();
            }
        }

        /**
         * Create Node Importer Strategy
         *
         * @param uuidBinding UUID Binding
         * @return Node Importer Strategy
         */
        private NodeImporterStrategy createNodeImporterStrategy(
                ImporterBinding.UUID_BINDING uuidBinding) {
            if (uuidBinding == null) {
                return new CreateNewNodeImporterStrategy(true);
            } else if (uuidBinding.equals(UUID_BINDING.CREATE_NEW)) {
                return new CreateNewNodeImporterStrategy(true);
            } else if (uuidBinding.equals(UUID_BINDING.CREATE_NEW_WITH_UUID)) {
                return new CreateNewNodeImporterStrategy(false);
            } else if (uuidBinding.equals(UUID_BINDING.REMOVE_EXISTING)) {
                return new RemoveExistingNodeImporterStrategy();
            } else if (uuidBinding.equals(UUID_BINDING.REPLACE_EXISTING)) {
                return new ReplaceExistingNodeImporterStrategy();
            } else if (uuidBinding.equals(UUID_BINDING.UPDATE_EXISTING)) {
                return new UpdateExistingNodeImporterStrategy();
            } else if (uuidBinding.equals(UUID_BINDING.THROW_ON_COLLISION)) {
                return new ThrowOnCollisionNodeImporterStrategy();
            } else {
                return new CreateNewNodeImporterStrategy(true);
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#getRootRef()
         */
        public NodeRef getRootRef() {
            return rootRef;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#getRootAssocType()
         */
        public QName getRootAssocType() {
            return rootAssocType;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#start()
         */
        public void start() {
            reportStarted();
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#importMetaData(java.util.Map)
         */
        public void importMetaData(Map<QName, String> properties) {
            // Determine if we're importing a complete repository
            String complexPath = properties
                    .get(QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "exportOf"));
            for (String path : complexPath.split(",")) {
                if (path != null && path.equals("/")) {
                    // Only allow complete repository import into root
                    NodeRef storeRootRef = nodeService.getRootNode(rootRef.getStoreRef());
                    if (!storeRootRef.equals(rootRef)) {
                        throw new ImporterException("A complete repository package cannot be imported here");
                    }
                }
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

        public String displayEntitiesPerType() {
            StringBuilder log = new StringBuilder();

            for (Map.Entry<QName, Integer> entry : entitiesPerType.entrySet()) {
                log.append(entry.getKey()).append(";").append(entry.getValue()).append("\n");
            }

            return log.toString();
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#importNode(org.alfresco.repo.importer.ImportNode)
         */
        @SuppressWarnings("unchecked")
        public NodeRef importNode(ImportNode context) {
            // import node
            NodeRef nodeRef;
            if (context.isReference()) {
                nodeRef = linkNode(context);
            } else {
                nodeRef = importStrategy.importNode(context);
                // Track the number of entities we import per type
                addType(context.getTypeDefinition().getName());
            }

            // apply aspects
            for (QName aspect : context.getNodeAspects()) {
                if (nodeService.hasAspect(nodeRef, aspect) == false) {
                    nodeService.addAspect(nodeRef, aspect, null);   // all properties previously added
                    reportAspectAdded(nodeRef, aspect);
                }
            }

            // import content, if applicable
            for (Map.Entry<QName, Serializable> property : context.getProperties().entrySet()) {
                // filter out content properties (they're imported later)
                DataTypeDefinition valueDataType = context.getPropertyDataType(property.getKey());
                if (valueDataType != null && valueDataType.getName().equals(DataTypeDefinition.CONTENT)) {
                    // the property may be a single value or a collection - handle both
                    Object objVal = property.getValue();
                    if (objVal instanceof String) {
                        importContent(nodeRef, property.getKey(), (String) objVal);
                    } else if (objVal instanceof Collection) {
                        for (String value : (Collection<String>) objVal) {
                            importContent(nodeRef, property.getKey(), value);
                        }
                    }
                }
            }

            return nodeRef;
        }

        /**
         * Link an existing Node
         *
         * @param context node to link in
         * @return node reference of child linked in
         */
        private NodeRef linkNode(ImportNode context) {
            ImportParent parentContext = context.getParentContext();
            NodeRef parentRef = parentContext.getParentRef();

            // determine the node reference to link to
            String uuid = context.getUUID();
            if (uuid == null || uuid.length() == 0) {
                throw new ImporterException("Node reference does not specify a reference to follow.");
            }

            // get the referenced node
            NodeRef referencedRef;
            if (Version2Model.TYPE_QNAME_VERSION_HISTORY.equals(context.getTypeDefinition().getName())) {
                // node from the version2Store - it has been previously created so we return it directly
                referencedRef = new NodeRef(getVersion2StoreNodeRef().getStoreRef(), uuid);
                return referencedRef;
            } else if (Version2Model.ASSOC_ROOT_VERSION
                    .equals(context.getParentContext().getAssocType())) {
                // node from the version2Store - children for this association will be linked
                referencedRef = new NodeRef(getVersion2StoreNodeRef().getStoreRef(), uuid);
            } else {
                // node which belongs to same store than the root node where we imported
                referencedRef = new NodeRef(rootRef.getStoreRef(), uuid);
            }

            // Note: do not link references that are defined in the root of the import
            if (!parentRef.equals(getRootRef())) {
                // determine child assoc type
                final QName assocType = getAssocType(context);
                AssociationDefinition assocDef = dictionaryService.getAssociation(assocType);
                if (assocDef.isChild()) {
                    // determine child name
                    QName childQName = getChildName(context);
                    if (childQName == null) {
                        String name = (String) nodeService.getProperty(referencedRef, ContentModel.PROP_NAME);
                        if (name == null || name.length() == 0) {
                            throw new ImporterException("Cannot determine node reference child name");
                        }
                        String localName = QName.createValidLocalName(name);
                        childQName = QName.createQName(assocType.getNamespaceURI(), localName);
                    }

                    // check if the association already exists
                    boolean found = false;
                    if (ContentModel.ASSOC_MULTILINGUAL_CHILD.equals(assocType)) {
                        Map<Locale, NodeRef> translations = multilingualContentService
                                .getTranslations(parentRef);
                        for (NodeRef translation : translations.values()) {
                            if (translation.equals(referencedRef)) {
                                found = true;
                                break;
                            }
                        }
                    } else {
                        String nodeName = (String) nodeService
                                .getProperty(referencedRef, ContentModel.PROP_NAME);

                        if (nodeService.getChildByName(parentRef, assocType, nodeName) != null) {
                            found = true;
                        }
                    }

                    if (!found) {
                        // create the secondary link
                        nodeService.addChild(parentRef, referencedRef, assocType, childQName);
                        reportNodeLinked(referencedRef, parentRef, assocType, childQName);

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Association has been created : " + getAssociationInfo(assocType, true, parentRef,
                                            referencedRef));
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Association already created : " + getAssociationInfo(assocType, true, parentRef,
                                            referencedRef));
                        }
                    }
                } else {
                    // check if the association already exists
                    List<AssociationRef> children = nodeService
                            .getTargetAssocs(parentRef, new QNamePattern() {

                                @Override
                                public boolean isMatch(QName qname) {
                                    return assocType.equals(qname);
                                }
                            });

                    boolean found = false;
                    if (children != null && children.size() > 0) {
                        for (AssociationRef associationRef : children) {
                            if (associationRef.getTargetRef().equals(referencedRef)) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        nodeService.createAssociation(parentRef, referencedRef, assocType);
                        reportNodeLinked(parentRef, referencedRef, assocType, null);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Association has been created : " + getAssociationInfo(assocType, false,
                                    parentRef, referencedRef));
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Association already created : " + getAssociationInfo(assocType, false, parentRef,
                                            referencedRef));
                        }
                    }
                }
            }

            // second, perform any specified udpates to the node
            updateStrategy.importNode(context);
            return referencedRef;
        }

        private String getAssociationInfo(QName assocType, boolean isChildAssoc, NodeRef parentRef,
                                          NodeRef referencedRef) {
            StringBuilder log = new StringBuilder("Association[type=").append(assocType.getLocalName());
            if (isChildAssoc) {
                log.append(", parentRef=").append(parentRef);
                log.append(", childRef=").append(referencedRef);
            } else {
                log.append(", sourceRef=").append(parentRef);
                log.append(", targetRef=").append(referencedRef);
            }
            log.append("]");
            return log.toString();
        }

        /**
         * Import Node Content.
         * <p>
         * The content URL, if present, will be a local URL.  This import copies the content from the
         * local URL to a server-assigned location.
         *
         * @param nodeRef      containing node
         * @param propertyName the name of the content-type property
         * @param contentData  the identifier of the content to import
         */
        private void importContent(NodeRef nodeRef, QName propertyName, String importContentData) {
            // bind import content data description
            importContentData = bindPlaceHolder(importContentData, binding);
            if (importContentData != null && importContentData.length() > 0) {
                DataTypeDefinition dataTypeDef = dictionaryService.getDataType(DataTypeDefinition.CONTENT);
                ContentData contentData = (ContentData) DefaultTypeConverter.INSTANCE
                        .convert(dataTypeDef, importContentData);
                String contentUrl = contentData.getContentUrl();
                if (contentUrl != null && contentUrl.length() > 0) {
                    // import the content from the url
                    InputStream contentStream = streamHandler.importStream(contentUrl);
                    ContentWriter writer = contentService.getWriter(nodeRef, propertyName, true);
                    writer.setEncoding(contentData.getEncoding());
                    writer.setMimetype(contentData.getMimetype());
                    writer.putContent(contentStream);
                    reportContentCreated(nodeRef, contentUrl);
                }
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#childrenImported(org.alfresco.service.cmr.repository.NodeRef)
         */
        public void childrenImported(NodeRef nodeRef) {
            behaviourFilter.enableBehaviours(nodeRef);
            ruleService.enableRules(nodeRef);
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#resolvePath(java.lang.String)
         */
        public NodeRef resolvePath(String path) {
            NodeRef referencedRef = null;
            if (path != null && path.length() > 0) {
                if (path.startsWith(VER2_PREFIX)) {
                    referencedRef = resolveImportedNodeRef(getVersion2StoreNodeRef(), path);
                } else {
                    referencedRef = resolveImportedNodeRef(rootRef, path);
                }
            }
            return referencedRef;
        }

        private NodeRef getVersion2StoreNodeRef() {
            if (versionStoreRootNoderef == null) {
                StoreRef versionStore = new StoreRef("workspace", "version2Store");
                // Get the root of the version store
                versionStoreRootNoderef = nodeService.getRootNode(versionStore);
            }
            return versionStoreRootNoderef;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#isExcludedClass(org.alfresco.service.namespace.QName)
         */
        public boolean isExcludedClass(QName className) {
            for (QName excludedClass : excludedClasses) {
                if (excludedClass.equals(className)) {
                    return true;
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#end()
         */
        @SuppressWarnings("unchecked")
        public void end() {
            // Bind all node references to destination space
            for (ImportedNodeRef importedRef : nodeRefs) {
                Serializable refProperty = null;
                if (importedRef.value != null) {
                    if (importedRef.value instanceof Collection) {
                        Collection<String> unresolvedRefs = (Collection<String>) importedRef.value;
                        List<NodeRef> resolvedRefs = new ArrayList<>(unresolvedRefs.size());
                        for (String unresolvedRef : unresolvedRefs) {
                            if (unresolvedRef != null) {
                                NodeRef nodeRef = resolveImportedNodeRef(importedRef.context.getNodeRef(),
                                        unresolvedRef);
                                // TODO: Provide a better mechanism for invalid references? e.g. report warning
                                if (nodeRef != null) {
                                    resolvedRefs.add(nodeRef);
                                }
                            }
                        }
                        refProperty = (Serializable) resolvedRefs;
                    } else {
                        refProperty = resolveImportedNodeRef(importedRef.context.getNodeRef(),
                                (String) importedRef.value);
                        // TODO: Provide a better mechanism for invalid references? e.g. report warning
                    }
                }

                // Set node reference on source node
                Set<QName> disabledBehaviours = getDisabledBehaviours(importedRef.context);
                try {
                    for (QName disabledBehaviour : disabledBehaviours) {
                        behaviourFilter.disableBehaviour(importedRef.context.getNodeRef(), disabledBehaviour);
                    }
                    nodeService
                            .setProperty(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                    if (progress != null) {
                        progress
                                .propertySet(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                    }
                } finally {
                    behaviourFilter.enableBehaviours(importedRef.context.getNodeRef());
                }
            }

            reportCompleted();
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#error(java.lang.Throwable)
         */
        public void error(Throwable e) {
            behaviourFilter.enableAllBehaviours();
            reportError(e);
        }

        /**
         * Get the child name to import node under
         *
         * @param context the node
         * @return the child name
         */
        private QName getChildName(ImportNode context) {
            QName assocType = getAssocType(context);
            QName childQName = null;

            // Determine child name
            String childName = context.getChildName();
            if (childName != null) {
                childName = bindPlaceHolder(childName, binding);
                // <Fix for ETHREEOH-2299>
                if (ContentModel.TYPE_PERSON.equals(context.getTypeDefinition().getName())) {
                    childName = childName.toLowerCase();
                }
                // </Fix for ETHREEOH-2299>
                String[] qnameComponents = QName.splitPrefixedQName(childName);
                childQName = QName
                        .createQName(qnameComponents[0], QName.createValidLocalName(qnameComponents[1]),
                                namespaceService);
            } else {
                Map<QName, Serializable> typeProperties = context.getProperties();

                Serializable nameValue = typeProperties.get(ContentModel.PROP_NAME);

                if (nameValue != null && !String.class.isAssignableFrom(nameValue.getClass())) {
                    throw new ImporterException(
                            "Unable to use childName property: " + ContentModel.PROP_NAME + " is not a string");
                }

                String name = (String) nameValue;

                if (name != null && name.length() > 0) {
                    name = bindPlaceHolder(name, binding);
                    String localName = QName.createValidLocalName(name);
                    childQName = QName.createQName(assocType.getNamespaceURI(), localName);
                }
            }

            return childQName;
        }

        /**
         * Get appropriate child association type for node to import under
         *
         * @param context node to import
         * @return child association type name
         */
        private QName getAssocType(ImportNode context) {
            QName assocType = context.getParentContext().getAssocType();
            if (assocType != null) {
                // return explicitly set association type
                return assocType;
            }

            //
            // Derive association type
            //

            // build type and aspect list for node
            List<QName> nodeTypes = new ArrayList<>();
            nodeTypes.add(context.getTypeDefinition().getName());
            for (QName aspect : context.getNodeAspects()) {
                nodeTypes.add(aspect);
            }

            // build target class types for parent
            Map<QName, QName> targetTypes = new HashMap<>();
            QName parentType = nodeService.getType(context.getParentContext().getParentRef());
            ClassDefinition classDef = dictionaryService.getClass(parentType);
            Map<QName, ChildAssociationDefinition> childAssocDefs = classDef.getChildAssociations();
            for (ChildAssociationDefinition childAssocDef : childAssocDefs.values()) {
                targetTypes.put(childAssocDef.getTargetClass().getName(), childAssocDef.getName());
            }
            Set<QName> parentAspects = nodeService.getAspects(context.getParentContext().getParentRef());
            for (QName parentAspect : parentAspects) {
                classDef = dictionaryService.getClass(parentAspect);
                childAssocDefs = classDef.getChildAssociations();
                for (ChildAssociationDefinition childAssocDef : childAssocDefs.values()) {
                    targetTypes.put(childAssocDef.getTargetClass().getName(), childAssocDef.getName());
                }
            }

            // find target class that is closest to node type or aspects
            QName closestAssocType = null;
            int closestHit = 1;
            for (QName nodeType : nodeTypes) {
                for (QName targetType : targetTypes.keySet()) {
                    QName testType = nodeType;
                    int howClose = 1;
                    while (testType != null) {
                        howClose--;
                        if (targetType.equals(testType) && howClose < closestHit) {
                            closestAssocType = targetTypes.get(targetType);
                            closestHit = howClose;
                            break;
                        }
                        ClassDefinition testTypeDef = dictionaryService.getClass(testType);
                        testType = (testTypeDef == null) ? null : testTypeDef.getParentName();
                    }
                }
            }

            return closestAssocType;
        }

        /**
         * For the given import node, return the behaviours to disable during import
         *
         * @param context import node
         * @return the disabled behaviours
         */
        private Set<QName> getDisabledBehaviours(ImportNode context) {
            Set<QName> classNames = new HashSet<>();

            // disable the type
            TypeDefinition typeDef = context.getTypeDefinition();
            classNames.add(typeDef.getName());

            // disable the aspects imported on the node
            classNames.addAll(context.getNodeAspects());

            // note: do not disable default aspects that are not imported on the node.
            //       this means they'll be added on import

            return classNames;
        }

        /**
         * Bind properties
         */
        @SuppressWarnings("unchecked")
        private Map<QName, Serializable> bindProperties(ImportNode context) {
            Map<QName, Serializable> properties = context.getProperties();
            Map<QName, Serializable> boundProperties = new HashMap<>(properties.size());
            for (QName property : properties.keySet()) {
                // get property datatype
                DataTypeDefinition valueDataType = context.getPropertyDataType(property);

                // filter out content properties (they're imported later)
                if (valueDataType != null && valueDataType.getName().equals(DataTypeDefinition.CONTENT)) {
                    continue;
                }

                // get property value
                Serializable value = properties.get(property);

                // bind property value to configuration and convert to appropriate type
                if (value instanceof Collection) {
                    List<Serializable> boundCollection = new ArrayList<>();
                    for (Serializable collectionValue : (Collection<Serializable>) value) {
                        Serializable objValue = bindValue(context, property, valueDataType, collectionValue);
                        boundCollection.add(objValue);
                    }
                    value = (Serializable) boundCollection;
                } else {
                    value = bindValue(context, property, valueDataType, value);
                }

                // choose to provide property on node creation or at end of import for lazy binding
                if (valueDataType != null && (valueDataType.getName().equals(DataTypeDefinition.NODE_REF)
                        || valueDataType.getName().equals(DataTypeDefinition.CATEGORY))) {
                    // record node reference for end-of-import binding
                    ImportedNodeRef importedRef = new ImportedNodeRef(context, property, value);
                    nodeRefs.add(importedRef);
                } else {
                    // property ready to be set on Node creation / update
                    boundProperties.put(property, value);
                }
            }

            return boundProperties;
        }

        /**
         * Bind permissions - binds authorities
         */
        private List<AccessPermission> bindPermissions(List<AccessPermission> permissions) {
            List<AccessPermission> boundPermissions = new ArrayList<>(permissions.size());

            for (AccessPermission permission : permissions) {
                AccessPermission ace = new NodeContext.ACE(permission.getAccessStatus(),
                        bindPlaceHolder(permission.getAuthority(), binding),
                        permission.getPermission());
                boundPermissions.add(ace);
            }

            return boundPermissions;
        }

        /**
         * Bind property value
         *
         * @param valueType value type
         * @param value     string form of value
         * @return the bound value
         */
        private Serializable bindValue(ImportNode context, QName property, DataTypeDefinition valueType,
                                       Serializable value) {
            Serializable objValue = null;
            // special behavior to treat base64 encoded properties
            if ((ModerationModel.PROP_ABUSE_MESSAGES.equals(property) ||
                    CircabcModel.PROP_APPLICANTS.equals(property)) && isNotEmpty(value)) {
                try {
                    objValue = (Serializable) CommonUtils.fromBase64String((String) value);
                } catch (ClassNotFoundException | IOException e) {
                    logger.error("Cannot convert value for property " + property, e);
                    throw new ImporterException("Cannot convert value for property " + property, e);
                }
            } else if (value != null && valueType != null) {
                if (value instanceof String) {
                    value = bindPlaceHolder(value.toString(), binding);
                }
                if ((valueType.getName().equals(DataTypeDefinition.NODE_REF) || valueType.getName()
                        .equals(DataTypeDefinition.CATEGORY))) {
                    objValue = value;
                } else {
                    objValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(valueType, value);
                }

            }
            return objValue;
        }

        private boolean isNotEmpty(Serializable value) {
            return value != null && value.toString().trim().length() > 0;
        }

        /**
         * Resolve imported reference relative to specified node
         *
         * @param sourceNodeRef context to resolve within
         * @param importedRef   reference to resolve
         */
        private NodeRef resolveImportedNodeRef(NodeRef sourceNodeRef, String importedRef) {
            // Resolve path to node reference
            NodeRef nodeRef = null;
            importedRef = bindPlaceHolder(importedRef, binding);

            if (importedRef.equals("/")) {
                nodeRef = sourceNodeRef;
            } else if (importedRef.startsWith("/")) {
                String path = createValidPath(importedRef);
                List<NodeRef> nodeRefs = searchService
                        .selectNodes(sourceNodeRef, path, null, namespaceService, false);
                if (nodeRefs.size() > 0) {
                    nodeRef = nodeRefs.get(0);
                }
            } else {
                // determine if node reference
                if (NodeRef.isNodeRef(importedRef)) {
                    nodeRef = new NodeRef(importedRef);
                } else {
                    // resolve relative path
                    try {
                        String path = createValidPath(importedRef);
                        List<NodeRef> nodeRefs = searchService
                                .selectNodes(sourceNodeRef, path, null, namespaceService, false);
                        if (nodeRefs.size() > 0) {
                            nodeRef = nodeRefs.get(0);
                        }
                    } catch (XPathException e) {
                        nodeRef = new NodeRef(importedRef);
                    } catch (AlfrescoRuntimeException e1) {
                        // Note: Invalid reference format - try path search instead
                    }
                }
            }

            return nodeRef;
        }

        /**
         * Helper to report start of import
         */
        private void reportStarted() {
            if (progress != null) {
                progress.started();
            }
        }

        /**
         * Helper to report end of import
         */
        private void reportCompleted() {
            if (progress != null) {
                progress.completed();
            }
        }

        /**
         * Helper to report error
         */
        private void reportError(Throwable e) {
            if (progress != null) {
                progress.error(e);
            }
        }

        /**
         * Helper to report node created progress
         */
        private void reportNodeCreated(ChildAssociationRef childAssocRef) {
            if (progress != null) {
                progress.nodeCreated(childAssocRef.getChildRef(), childAssocRef.getParentRef(),
                        childAssocRef.getTypeQName(), childAssocRef.getQName());
            }
        }

        /**
         * Helper to report node linked progress
         */
        private void reportNodeLinked(NodeRef childRef, NodeRef parentRef, QName assocType,
                                      QName childName) {
            if (progress != null) {
                progress.nodeLinked(childRef, parentRef, assocType, childName);
            }
        }

        /**
         * Helper to report content created progress
         */
        private void reportContentCreated(NodeRef nodeRef, String sourceUrl) {
            if (progress != null) {
                progress.contentCreated(nodeRef, sourceUrl);
            }
        }

        /**
         * Helper to report aspect added progress
         */
        private void reportAspectAdded(NodeRef nodeRef, QName aspect) {
            if (progress != null) {
                progress.aspectAdded(nodeRef, aspect);
            }
        }

        /**
         * Helper to report property set progress
         */
        private void reportPropertySet(NodeRef nodeRef, Map<QName, Serializable> properties) {
            if (progress != null && properties != null) {
                for (QName property : properties.keySet()) {
                    progress.propertySet(nodeRef, property, properties.get(property));
                }
            }
        }

        /**
         * Helper to report permission set progress
         */
        private void reportPermissionSet(NodeRef nodeRef, List<AccessPermission> permissions) {
            if (progress != null && permissions != null) {
                for (AccessPermission permission : permissions) {
                    progress.permissionSet(nodeRef, permission);
                }
            }
        }

        private StoreRef getStoreRef(String protocol, String identifier) {
            return new StoreRef(protocol, identifier);
        }

        private String getNodeInfo(ImportNode node, NodeRef nodeRef, QName childQName) {
            TypeDefinition nodeType = node.getTypeDefinition();
            NodeRef parentRef = node.getParentContext().getParentRef();
            QName assocType = getAssocType(node);

            String log = "Node[ref=" + (node.getNodeRef() != null ? node.getNodeRef() : nodeRef) +
                    ", name=" + childQName.getLocalName() +
                    ", type=" + nodeType.getName().getLocalName() +
                    ", assocType=" + assocType.getLocalName() +
                    ", parentRef=" + parentRef +
                    "]";

            return log;
        }

        /**
         * Import strategy where imported nodes are always created regardless of whether a node of the
         * same UUID already exists in the repository
         */
        private class CreateNewNodeImporterStrategy implements NodeImporterStrategy {

            // force allocation of new UUID, even if one already specified
            private boolean assignNewUUID;
            // sets of persons / configurations already existing in the system
            private Set<NodeRef> existingPersons;
            private Set<NodeRef> existingConfigurations;

            /**
             * Construct
             *
             * @param newUUID force allocation of new UUID
             */
            public CreateNewNodeImporterStrategy(boolean assignNewUUID) {
                this.assignNewUUID = assignNewUUID;
                this.existingPersons = new HashSet<>();
                this.existingConfigurations = new HashSet<>();
            }

            private boolean isUserOrPerson(ImportNode node) {
                return (ContentModel.TYPE_USER.equals(node.getTypeDefinition().getName()) ||
                        isPerson(node));
            }

            private boolean isPerson(ImportNode node) {
                return ContentModel.TYPE_PERSON.equals(node.getTypeDefinition().getName());
            }

            private boolean isConfigurations(ImportNode node) {
                return ApplicationModel.TYPE_CONFIGURATIONS.equals(node.getTypeDefinition().getName());
            }

            private boolean isPreferences(ImportNode node, QName childQName) {
                return ContentModel.TYPE_CMOBJECT.equals(node.getTypeDefinition().getName()) &&
                        "app:preferences".equals(childQName.getPrefixString());
            }

            private NodeRef doesChildAlreadyExist(NodeRef parentRef, final QName childQName,
                                                  final QName assocType) {
                List<ChildAssociationRef> children = nodeService
                        .getChildAssocs(parentRef, assocType, new QNamePattern() {

                            @Override
                            public boolean isMatch(QName qname) {
                                return childQName.equals(qname);
                            }
                        }, true);

                if (children != null && children.size() == 1) {
                    NodeRef childNodeRef = children.get(0).getChildRef();

                    if (childNodeRef != null) {
                        // found it, we return its reference
                        return childNodeRef;
                    }
                } else if (children.size() > 1) {
                    throw new RuntimeException(new ImporterException(
                            "There is more than one child node matching " + childQName
                                    + ". There must be only one."));
                }
                return null;
            }

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node) {
                TypeDefinition nodeType = node.getTypeDefinition();
                NodeRef parentRef = node.getParentContext().getParentRef();
                QName assocType = getAssocType(node);
                final QName childQName = getChildName(node);
                if (childQName == null) {
                    throw new ImporterException(
                            "Cannot determine child name of node (type: " + nodeType.getName() + ")");
                } else if (isNodeExcluded(childQName.getPrefixString(), EXCLUDED_NODE_NAMES) ||
                        // a node already created by the system
                        isUserOrPerson(node)) {
                    NodeRef childNodeRef = doesChildAlreadyExist(parentRef, childQName,
                            ContentModel.ASSOC_CHILDREN);
                    if (childNodeRef != null) {
                        if (isPerson(node)) {
                            existingPersons.add(childNodeRef);
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Node already created : " + getNodeInfo(node, childNodeRef, childQName));
                        }
                        return childNodeRef;
                    }
                } else if (isConfigurations(node) && existingPersons.contains(parentRef)) {
                    NodeRef childNodeRef = doesChildAlreadyExist(parentRef, childQName,
                            ApplicationModel.ASSOC_CONFIGURATIONS);
                    if (childNodeRef != null) {
                        existingConfigurations.add(childNodeRef);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Node already created : " + getNodeInfo(node, childNodeRef, childQName));
                        }
                        return childNodeRef;
                    }
                } else if (isPreferences(node, childQName) && existingConfigurations.contains(parentRef)) {
                    NodeRef childNodeRef = doesChildAlreadyExist(parentRef, childQName,
                            ContentModel.ASSOC_CONTAINS);
                    if (childNodeRef != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Node already created : " + getNodeInfo(node, childNodeRef, childQName));
                        }
                        return childNodeRef;
                    }
                } else {
                    // if a node already exists with the same UUID, we don't create it
                    String uuid = node.getUUID();
                    if (uuid != null && uuid.length() > 0) {
                        if (node.getProperties() != null
                                && node.getProperties().get(ContentModel.PROP_STORE_IDENTIFIER) != null) {
                            String protocol = (String) node.getProperties().get(ContentModel.PROP_STORE_PROTOCOL);
                            String identifier = (String) node.getProperties()
                                    .get(ContentModel.PROP_STORE_IDENTIFIER);
                            NodeRef existingNodeRef = new NodeRef(getStoreRef(protocol, identifier), uuid);
                            if (nodeService.exists(existingNodeRef)) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Node already created : " + getNodeInfo(node, existingNodeRef, childQName));
                                }
                                return existingNodeRef;
                            }
                        }
                    }
                }

                // Create initial node (but, first disable behaviour for the node to be created)
                Set<QName> disabledBehaviours = getDisabledBehaviours(node);
                List<QName> alreadyDisabledBehaviours = new ArrayList<>();
                for (QName disabledBehaviour : disabledBehaviours) {
                    boolean alreadyDisabled = !behaviourFilter.isEnabled(disabledBehaviour);
                    behaviourFilter.disableBehaviour(disabledBehaviour);
                    if (alreadyDisabled) {
                        alreadyDisabledBehaviours.add(disabledBehaviour);
                    }
                }
                disabledBehaviours.removeAll(alreadyDisabledBehaviours);

                // Build initial map of properties
                Map<QName, Serializable> initialProperties = bindProperties(node);

                // Bind a mandatory property of cm:person if not set
                if (ContentModel.TYPE_PERSON.equals(node.getTypeDefinition().getName())) {
                    if (!initialProperties.containsKey(ContentModel.PROP_SIZE_CURRENT)) {
                        initialProperties.put(ContentModel.PROP_SIZE_CURRENT, 0);
                    }
                }

                // Assign UUID if already specified on imported node
                if (!assignNewUUID && node.getUUID() != null) {
                    initialProperties.put(ContentModel.PROP_NODE_UUID, node.getUUID());
                } else if (initialProperties.containsKey(ContentModel.PROP_NODE_UUID)) {
                    initialProperties.remove(ContentModel.PROP_NODE_UUID);
                }

                // Create Node
                ChildAssociationRef assocRef = nodeService
                        .createNode(parentRef, assocType, childQName, nodeType.getName(), initialProperties);
                NodeRef nodeRef = assocRef.getChildRef();

                // apply permissions
                List<AccessPermission> permissions = null;
                AccessStatus writePermission = permissionService
                        .hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS);
                if (authenticationContext.isCurrentUserTheSystemUser() || writePermission
                        .equals(AccessStatus.ALLOWED)) {
                    permissions = bindPermissions(node.getAccessControlEntries());

                    for (AccessPermission permission : permissions) {
                        permissionService
                                .setPermission(nodeRef, permission.getAuthority(), permission.getPermission(),
                                        permission.getAccessStatus().equals(AccessStatus.ALLOWED));
                    }
                    // note: apply inheritance after setting permissions as this may affect whether you can apply permissions
                    boolean inheritPermissions = node.getInheritPermissions();
                    if (!inheritPermissions) {
                        permissionService.setInheritParentPermissions(nodeRef, false);
                    }
                }

                // Disable behaviour for the node until the complete node (and its children have been imported)
                for (QName disabledBehaviour : disabledBehaviours) {
                    behaviourFilter.enableBehaviour(disabledBehaviour);
                }
                for (QName disabledBehaviour : disabledBehaviours) {
                    behaviourFilter.disableBehaviour(nodeRef, disabledBehaviour);
                }
                // TODO: Replace this with appropriate rule/action import handling
                ruleService.disableRules(nodeRef);

                // Report creation
                reportNodeCreated(assocRef);
                reportPropertySet(nodeRef, initialProperties);
                reportPermissionSet(nodeRef, permissions);

                // return newly created node reference
                if (logger.isDebugEnabled()) {
                    logger.debug("Node has been created : " + getNodeInfo(node, nodeRef, childQName));
                }
                return nodeRef;
            }

            private boolean isNodeExcluded(String childQName, List<String> excludedNodesNames) {
                if (excludedNodesNames.contains(childQName)) {
                    return true;
                }
                return false;
            }
        }

        /**
         * Importer strategy where an existing node (one with the same UUID) as a node being imported is
         * first removed.  The imported node is placed in the location specified at import time.
         */
        private class RemoveExistingNodeImporterStrategy implements NodeImporterStrategy {

            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node) {
                // remove existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0) {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef)) {
                        // remove primary parent link forcing deletion
                        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(existingNodeRef);

                        // TODO: Check for root node
                        nodeService.removeChild(childAssocRef.getParentRef(), childAssocRef.getChildRef());
                    }
                }

                // import as if a new node into current import parent location
                return createNewStrategy.importNode(node);
            }
        }

        /**
         * Importer strategy where an existing node (one with the same UUID) as a node being imported is
         * first removed.  The imported node is placed under the parent of the removed node.
         */
        private class ReplaceExistingNodeImporterStrategy implements NodeImporterStrategy {

            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node) {
                // replace existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0) {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef)) {
                        // remove primary parent link forcing deletion
                        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(existingNodeRef);
                        nodeService.removeChild(childAssocRef.getParentRef(), childAssocRef.getChildRef());

                        // update the parent context of the node being imported to the parent of the node just deleted
                        node.getParentContext().setParentRef(childAssocRef.getParentRef());
                        node.getParentContext().setAssocType(childAssocRef.getTypeQName());
                    }
                }

                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }

        /**
         * Import strategy where an error is thrown when importing a node that has the same UUID of an
         * existing node in the repository.
         */
        private class ThrowOnCollisionNodeImporterStrategy implements NodeImporterStrategy {

            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node) {
                // if node to import has a UUID and an existing node of the same uuid already exists
                // then throw an error
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0) {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef)) {
                        throw new InvalidNodeRefException("Node " + existingNodeRef + " already exists",
                                existingNodeRef);
                    }
                }

                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }

        /**
         * Import strategy where imported nodes are updated if a node with the same UUID already exists
         * in the repository.
         * <p>
         * Note: this will only allow incremental update of an existing node - it does not delete
         * properties or associations.
         */
        private class UpdateExistingNodeImporterStrategy implements NodeImporterStrategy {

            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node) {
                // replace existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0) {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (!nodeService.exists(existingNodeRef)) {
                        existingNodeRef = new NodeRef(getVersion2StoreNodeRef().getStoreRef(), uuid);
                    }

                    if (nodeService.exists(existingNodeRef)) {
                        // do the update
                        Map<QName, Serializable> existingProperties = nodeService
                                .getProperties(existingNodeRef);
                        Map<QName, Serializable> updateProperties = bindProperties(node);
                        if (updateProperties != null && updateProperties.size() > 0) {
                            existingProperties.putAll(updateProperties);
                            nodeService.setProperties(existingNodeRef, existingProperties);
                        }

                        // Apply permissions
                        List<AccessPermission> permissions = null;
                        AccessStatus writePermission = permissionService
                                .hasPermission(existingNodeRef, PermissionService.CHANGE_PERMISSIONS);
                        if (authenticationContext.isCurrentUserTheSystemUser() || writePermission
                                .equals(AccessStatus.ALLOWED)) {
                            boolean inheritPermissions = node.getInheritPermissions();
                            if (!inheritPermissions) {
                                permissionService.setInheritParentPermissions(existingNodeRef, false);
                            }

                            permissions = bindPermissions(node.getAccessControlEntries());

                            for (AccessPermission permission : permissions) {
                                permissionService.setPermission(existingNodeRef, permission.getAuthority(),
                                        permission.getPermission(),
                                        permission.getAccessStatus().equals(AccessStatus.ALLOWED));
                            }
                        }

                        // report update
                        reportPropertySet(existingNodeRef, updateProperties);
                        reportPermissionSet(existingNodeRef, permissions);

                        return existingNodeRef;
                    }
                }

                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }

    }
}
