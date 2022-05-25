/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.customisation;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

import static org.alfresco.model.ContentModel.*;

/**
 * Implementation of the NodePreferencesService.
 *
 * @author Yanick Pignot
 */
public class NodePreferencesServiceImpl implements NodePreferencesService {

    /**
     * The configuration service root container type
     */
    public static final QName TYPE_CUSTOMIZATION_CONTAINER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationContainer");
    /**
     * The configuration service root container type
     */
    public static final QName TYPE_CUSTOMIZATION_FOLDER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationFolder");
    /**
     * The configuration service root container type
     */
    public static final QName TYPE_CUSTOMIZATION_CONTENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationContent");
    /**
     * The configuration service root association name
     */
    public static final QName ASSOC_CUSTOMIZE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customize");
    private static final String DEFAULT = "default.";
    /**
     * The ml root type qname
     */
    private static final QName TYPE_ML_ROOT =
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlRoot");
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(NodePreferencesServiceImpl.class);

    private NodeService nodeService;
    private ManagementService managementService;
    private MultilingualContentService multilingualContentService;
    private ContentService contentService;
    private RootPreferencesUpdater rootPreferencesUpdater;
    private MimetypeService mimetypeService;
    private NodeRef circabcNodeRef = null;
    private NodeRef circabcDictionaryNodeRef = null;

    private SimpleCache<NodeRef, NodeRef> containerCache;
    private SimpleCache<NodeRef, Map<ConfigKey, NodeRef>> customizationFolderCache;

    public void updateRootReference() {
        rootPreferencesUpdater.updateSpace(getCircabcDDNodeRef());
    }

    public boolean isNodeConfigurable(final NodeRef ref) {
        ParameterCheck.mandatory("The node reference", ref);

        return getConfigurationContainer(ref, false) != null;
    }

    public NodeRef makeConfigurable(final NodeRef ref) throws CustomizationException {
        ParameterCheck.mandatory("The node reference", ref);

        if (nodeService.getType(ref).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            throw new CustomizationException("Impossible to make a multilingual container configurable.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to make the node " + ref + " configurable.");
        }

        if (isNodeConfigurable(ref)) {
            throw new CustomizationException("The node " + ref + " is already setted configurable.");
        }

        final NodeRef configContainer =
                nodeService
                        .createNode(
                                ref, ASSOC_CUSTOMIZE, TYPE_CUSTOMIZATION_CONTAINER, TYPE_CUSTOMIZATION_CONTAINER)
                        .getChildRef();

        containerCache.clear();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The node "
                            + ref
                            + " is successfully setted being configurable. "
                            + "\n\tThe configuration container is "
                            + configContainer);
        }

        return configContainer;
    }

    public List<NodeRef> getConfigurationFiles(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement)
            throws CustomizationException {
        checkParamters(nodeRef, configTypeRoot, configSubType, configElement);

        NodeRef recusionRef = null;
        NodeRef container = null;
        List<NodeRef> configItems = null;
        QName recursNodeType = null;

        do {
            if (recusionRef == null) {
                recursNodeType = nodeService.getType(nodeRef);
                if (recursNodeType.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                    recusionRef = multilingualContentService.getPivotTranslation(nodeRef);
                } else if (recursNodeType.equals(CircabcModel.TYPE_CATEGORY_HEADER)
                        || recursNodeType.equals(TYPE_ML_ROOT)) {
                    recusionRef = managementService.getCircabcNodeRef();
                } else {
                    recusionRef = nodeRef;
                }
            } else {
                // get the parent file of the container
                recusionRef = nodeService.getPrimaryParent(container).getParentRef();
                // get the parent for recursion
                recusionRef = nodeService.getPrimaryParent(recusionRef).getParentRef();
            }

            container = getConfigurationContainer(recusionRef, true);
            configItems = getConfigFilesImpl(configTypeRoot, configSubType, configElement, container);
        } while (container != null && configItems == null);

        if (configItems == null || configItems.size() == 0) {
            final String message =
                    "No customization found for data: "
                            + configTypeRoot
                            + "/"
                            + configSubType
                            + "/"
                            + configElement;

            logger.error(message);

            throw new CustomizationException(message);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        configItems.size()
                                + " customization files for "
                                + nodeRef
                                + " and data: "
                                + configTypeRoot
                                + "/"
                                + configSubType
                                + "/"
                                + configElement);
            }

            return configItems;
        }
    }

    public NodeRef getDefaultConfigurationFile(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement)
            throws CustomizationException {
        final List<NodeRef> files =
                getConfigurationFiles(nodeRef, configTypeRoot, configSubType, configElement);

        // the getConfigurationFiles method doesn't allow having no configuration file.

        if (files.size() == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        files.get(0)
                                + " is the default customization file for data: "
                                + configTypeRoot
                                + "/"
                                + configSubType
                                + "/"
                                + configElement
                                + ". Because it is the only one.");
            }

            return files.get(0);
        } else {
            NodeRef defaultFile = null;
            boolean foundWithName = false;

            String defaultFileName = null;
            Date defaultFileCreated = null;
            Map<QName, Serializable> props;
            String name = null;
            Date created = null;
            for (final NodeRef file : files) {
                props = nodeService.getProperties(nodeRef);
                name = (String) props.get(PROP_NAME);
                created = (Date) props.get(PROP_CREATED);
                if (name.startsWith(DEFAULT)) {
                    foundWithName = true;
                    defaultFile = file;
                    break;
                } else if (defaultFile == null || defaultFileCreated.after(created)) {
                    defaultFileCreated = created;
                    defaultFileName = name;
                    defaultFile = file;
                }
            }

            if (logger.isDebugEnabled()) {
                if (foundWithName) {
                    logger.debug(
                            defaultFileName
                                    + " is the default customization file for data: "
                                    + configTypeRoot
                                    + "/"
                                    + configSubType
                                    + "/"
                                    + configElement
                                    + ". Because it is prefixed '"
                                    + DEFAULT
                                    + "'");
                } else {
                    logger.debug(
                            defaultFileName
                                    + " is the default customization file for data: "
                                    + configTypeRoot
                                    + "/"
                                    + configSubType
                                    + "/"
                                    + configElement
                                    + ". Because it is the oldest '"
                                    + defaultFileCreated
                                    + "'");
                }
            }
            return defaultFile;
        }
    }

    public NodeRef addCustomizationFile(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName,
            final InputStream content)
            throws CustomizationException {
        return addCustomizationFileImpl(
                nodeRef, configTypeRoot, configSubType, configElement, fileName, content);
    }

    public NodeRef addCustomizationFile(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName,
            final File content)
            throws CustomizationException {
        return addCustomizationFileImpl(
                nodeRef, configTypeRoot, configSubType, configElement, fileName, content);
    }

    public NodeRef addCustomizationFile(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName,
            final String content)
            throws CustomizationException {
        return addCustomizationFileImpl(
                nodeRef, configTypeRoot, configSubType, configElement, fileName, content);
    }

    public NodeRef addCustomizationFile(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName,
            final Properties content)
            throws CustomizationException {
        return addCustomizationFileImpl(
                nodeRef, configTypeRoot, configSubType, configElement, fileName, content);
    }

    private NodeRef addCustomizationFileImpl(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName,
            final Object contentAsObject)
            throws CustomizationException {
        checkParamters(nodeRef, configTypeRoot, configSubType, configElement);

        ParameterCheck.mandatoryString("The configuration file name", fileName);

        // check the root config  conatiner
        final NodeRef configContainer = getConfigurationContainer(nodeRef, false);
        if (configContainer == null) {
            throw new CustomizationException(
                    "The node "
                            + nodeRef
                            + " must be setted as configurable. Please use the NodePreferencesServiceImpl.makeConfigurable method.");
        }

        // remove container cache
        customizationFolderCache.remove(configContainer);

        // check the root custom container
        NodeRef rootCustomContainer = getChildByName(configContainer, ASSOC_CHILDREN, configTypeRoot);
        if (rootCustomContainer == null) {
            rootCustomContainer = createFolder(configContainer, configTypeRoot);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Root customization container successfully created under the node "
                                + " with ref "
                                + rootCustomContainer);
            }
        }

        // check the configuration space root
        NodeRef subCustomContainer = getChildByName(rootCustomContainer, ASSOC_CHILDREN, configSubType);
        if (subCustomContainer == null) {
            subCustomContainer = createFolder(rootCustomContainer, configSubType);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Sub customization container successfully created under the node "
                                + nodeRef
                                + " with ref "
                                + subCustomContainer);
            }
        }

        // check the configuration element
        NodeRef elementCustomContainer =
                getChildByName(subCustomContainer, ASSOC_CHILDREN, configElement);
        if (elementCustomContainer == null) {
            elementCustomContainer = createFolder(subCustomContainer, configElement);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Element customization container successfully created under the node "
                                + nodeRef
                                + " with ref "
                                + elementCustomContainer);
            }
        }

        NodeRef contentRef = getChildByName(elementCustomContainer, ASSOC_CHILDREN, fileName);
        if (contentRef == null) {
            contentRef = createFile(elementCustomContainer, fileName);

            final Map<QName, Serializable> versionProps =
                    Collections.<QName, Serializable>singletonMap(PROP_AUTO_VERSION, Boolean.TRUE);
            nodeService.addAspect(contentRef, ASPECT_VERSIONABLE, versionProps);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Customization file '"
                                + fileName
                                + "' successfully created under the node "
                                + nodeRef
                                + " with ref "
                                + contentRef);
            }
        }

        //	set the body (in the content)
        final ContentWriter writter =
                contentService.getWriter(contentRef, CircabcModel.PROP_CONTENT, true);
        writter.setMimetype(mimetypeService.guessMimetype(fileName));
        if (contentAsObject == null) {
            writter.putContent("");
        } else if (contentAsObject instanceof InputStream) {
            writter.putContent((InputStream) contentAsObject);
        } else if (contentAsObject instanceof File) {
            writter.putContent((File) contentAsObject);
        } else if (contentAsObject instanceof Properties) {
            final OutputStream contentOutputStream = writter.getContentOutputStream();
            try {
                ((Properties) contentAsObject).store(contentOutputStream, "");
                contentOutputStream.flush();
                contentOutputStream.close();
            } catch (IOException e) {
                throw new CustomizationException(
                        "IO error occurs when update customization content: " + e.getMessage(), e);
            }
        } else {
            writter.putContent(contentAsObject.toString());
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Customization file '"
                            + fileName
                            + "' successfully updated with content "
                            + contentAsObject);
        }

        return contentRef;
    }

    public boolean customizationFileExists(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName) {
        final NodeRef file =
                getCustomizationOrNull(nodeRef, configTypeRoot, configSubType, configElement, fileName);
        final boolean exists = file != null;
        if (logger.isDebugEnabled()) {
            logger.debug("Customization file '" + fileName + "' found: " + exists);
        }

        return exists;
    }

    public NodeRef getCustomization(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName)
            throws CustomizationException {
        final NodeRef file =
                getCustomizationOrNull(nodeRef, configTypeRoot, configSubType, configElement, fileName);

        if (file == null) {
            throw new CustomizationException("Customization " + fileName + " doens't exists.");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Customization file '" + fileName + "' successfully found");
            }

            return file;
        }
    }

    public void removeCustomization(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName)
            throws CustomizationException {
        checkParamters(nodeRef, configTypeRoot, configSubType, configElement);
        ParameterCheck.mandatoryString("The configuration file name", fileName);

        if (nodeRef.equals(getCircabcRootNodeRef())) {
            throw new CustomizationException("Impossible to remove customization of the root folder.");
        }

        final NodeRef file =
                getCustomizationOrNull(nodeRef, configTypeRoot, configSubType, configElement, fileName);
        final NodeRef container = getConfigurationContainer(nodeRef, false);

        containerCache.clear();
        customizationFolderCache.remove(container);

        if (file == null) {
            throw new CustomizationException("Customization " + fileName + " doens't exists.");
        } else {
            NodeRef parent;
            NodeRef fileToDelete = file;

            // clleanup the configuration spaces
            while (fileToDelete != null && fileToDelete.equals(nodeRef) == false) {
                parent = nodeService.getPrimaryParent(fileToDelete).getParentRef();
                nodeService.deleteNode(fileToDelete);
                if (nodeService.getChildAssocs(parent).size() == 0) {
                    fileToDelete = parent;
                } else {
                    fileToDelete = null;
                }
            }
        }
    }

    public NodeRef getCustomizationFromNode(final NodeRef customizationNode) {
        if (customizationNode == null) {
            return null;
        } else if (customizationNode.equals(getCircabcDDNodeRef())) {
            return getCircabcRootNodeRef();
        } else if (nodeService.getType(customizationNode).equals(TYPE_CUSTOMIZATION_CONTAINER)) {
            return nodeService.getPrimaryParent(customizationNode).getParentRef();
        } else {
            return getCustomizationFromNode(
                    nodeService.getPrimaryParent(customizationNode).getParentRef());
        }
    }

    private NodeRef getCustomizationOrNull(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final String fileName) {
        checkParamters(nodeRef, configTypeRoot, configSubType, configElement);
        ParameterCheck.mandatoryString("The configuration file name", fileName);

        // check the root config  conatiner
        final NodeRef configContainer = getConfigurationContainer(nodeRef, false);
        if (configContainer == null) {
            return null;
        }

        // check the root custom container
        NodeRef rootCustomContainer = getChildByName(configContainer, ASSOC_CHILDREN, configTypeRoot);
        if (rootCustomContainer == null) {
            return null;
        }

        // check the configuration space root
        NodeRef subCustomContainer = getChildByName(rootCustomContainer, ASSOC_CHILDREN, configSubType);
        if (subCustomContainer == null) {
            return null;
        }

        // check the configuration element
        NodeRef elementCustomContainer =
                getChildByName(subCustomContainer, ASSOC_CHILDREN, configElement);
        if (elementCustomContainer == null) {
            return null;
        }

        return getChildByName(elementCustomContainer, ASSOC_CHILDREN, fileName);
    }

    // -----------
    //  --  Helpers

    /**
     * @param nodeRef
     * @param configTypeRoot
     * @param configSubType
     * @param configElement
     */
    private void checkParamters(
            final NodeRef nodeRef,
            final String configTypeRoot,
            final String configSubType,
            final String configElement) {
        ParameterCheck.mandatory("The node reference", nodeRef);
        ParameterCheck.mandatory("The config type root (root folder name)", configTypeRoot);
        ParameterCheck.mandatory("The config sub type (sub type folder name)", configSubType);
        ParameterCheck.mandatory("The config element (config element folder name)", configElement);
    }

    /**
     * @param configTypeRoot
     * @param configSubType
     * @param configElement
     * @param container
     * @return
     */
    private List<NodeRef> getConfigFilesImpl(
            final String configTypeRoot,
            final String configSubType,
            final String configElement,
            final NodeRef container) {
        List<NodeRef> configItems = null;

        /* Search in the cache first */

        final ConfigKey key = new ConfigKey(configTypeRoot, configSubType, configElement);
        Map<ConfigKey, NodeRef> folders = customizationFolderCache.get(container);

        NodeRef configElementRef = null;

        if (folders == null) {
            folders = new HashMap<>();
            customizationFolderCache.put(container, folders);
        }

        if (folders.containsKey(key)) {
            configElementRef = folders.get(key);
        } else {
            // in the data dictionary, the assoc type is different.
            final QName childAssoc = (isRootContainer(container)) ? ASSOC_CONTAINS : ASSOC_CHILDREN;

            final NodeRef configTypeRootRef = getChildByName(container, childAssoc, configTypeRoot);
            if (configTypeRootRef != null) {
                final NodeRef configSubTypeRef =
                        getChildByName(configTypeRootRef, childAssoc, configSubType);

                if (configSubTypeRef != null) {
                    configElementRef = getChildByName(configSubTypeRef, childAssoc, configElement);

                    if (configElementRef != null) {
                        folders.put(key, configElementRef);
                    }
                }
            }
        }

        if (configElementRef != null) {
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(configElementRef);
            configItems = new ArrayList<>(childs.size());
            for (final ChildAssociationRef child : childs) {
                configItems.add(child.getChildRef());
            }
        }

        return configItems;
    }

    private NodeRef getChildByName(final NodeRef parent, final QName childAssoc, final String name) {
        if (childAssoc.equals(ASSOC_CONTAINS)) {
            return nodeService.getChildByName(parent, childAssoc, name);
        } else {
            final List<ChildAssociationRef> assocs =
                    nodeService.getChildAssocs(parent, childAssoc, createNameQName(name));

            final int size = (assocs == null) ? 0 : assocs.size();

            if (size == 0) {
                return null;
            } else {
                if (size > 1 && logger.isWarnEnabled()) {
                    logger.warn(
                            size
                                    + " children found with qname "
                                    + createNameQName(name)
                                    + " for customization container/file "
                                    + parent);
                }
                return assocs.get(0).getChildRef();
            }
        }
    }

    private boolean isRootContainer(final NodeRef container) {
        return container != null && container.equals(getCircabcDDNodeRef());
    }

    private NodeRef getConfigurationContainer(final NodeRef originalRef, final boolean recursive) {
        return getConfigurationContainer(originalRef, originalRef, recursive);
    }

    /**
     * @param originalRef For caching purposes
     */
    private NodeRef getConfigurationContainer(
            final NodeRef originalRef, final NodeRef recursionRef, final boolean recursive) {
        if (recursionRef == null) {
            return null;
        }

        // force to search
        if (recursive == false) {
            containerCache.remove(recursionRef);
        }

        if (nodeService.getType(recursionRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            return getConfigurationContainer(
                    originalRef, multilingualContentService.getPivotTranslation(recursionRef), recursive);
        }

        final NodeRef cachedContainer = containerCache.get(recursionRef);
        if (cachedContainer != null) {
            if (nodeService.exists(cachedContainer)) {
                return cachedContainer;
            } else {
                containerCache.remove(recursionRef);
            }
        }

        if (getCircabcRootNodeRef().equals(recursionRef)) {
            final NodeRef circabcDDNodeRef = getCircabcDDNodeRef();
            // Special case: We are on the top of the search, get the Dictionary Container
            containerCache.put(originalRef, circabcDDNodeRef);

            return circabcDDNodeRef;
        } else {

            final List<ChildAssociationRef> associations =
                    nodeService.getChildAssocs(recursionRef, ASSOC_CUSTOMIZE, TYPE_CUSTOMIZATION_CONTAINER);

            if (associations == null || associations.size() < 1) {
                if (recursive) {
                    return getConfigurationContainer(
                            originalRef, nodeService.getPrimaryParent(recursionRef).getParentRef(), recursive);
                } else {
                    return null;
                }
            } else {
                if (associations.size() > 1) {
                    logger.warn("More than one Preference Root Node found for the node " + recursionRef);
                }

                final NodeRef container = associations.get(0).getChildRef();
                containerCache.put(originalRef, container);
                return container;
            }
        }
    }

    /**
     * @return
     */
    private NodeRef getCircabcDDNodeRef() {
        if (circabcDictionaryNodeRef == null) {
            circabcDictionaryNodeRef = managementService.getCircabcDictionaryNodeRef();
        }
        return circabcDictionaryNodeRef;
    }

    /**
     * @return
     */
    private NodeRef getCircabcRootNodeRef() {
        if (circabcNodeRef == null) {
            circabcNodeRef = managementService.getCircabcNodeRef();
        }
        return circabcNodeRef;
    }

    private NodeRef createFolder(final NodeRef parent, final String name) {
        return createNode(parent, name, TYPE_CUSTOMIZATION_FOLDER);
    }

    private NodeRef createFile(final NodeRef parent, final String name) {
        return createNode(parent, name, TYPE_CUSTOMIZATION_CONTENT);
    }

    private NodeRef createNode(final NodeRef parent, final String name, final QName typeQname) {
        final Map<QName, Serializable> properties =
                Collections.<QName, Serializable>singletonMap(PROP_NAME, name);
        final ChildAssociationRef assoc =
                nodeService.createNode(
                        parent, ASSOC_CHILDREN, createNameQName(name), typeQname, properties);

        return assoc.getChildRef();
    }

    private QName createNameQName(final String name) {
        return QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param containerCache the containerCache to set
     */
    public final void setContainerCache(SimpleCache<NodeRef, NodeRef> containerCache) {
        this.containerCache = containerCache;
    }

    /**
     * @param customizationFolderCache the customizationFolderCache to set
     */
    public final void setCustomizationFolderCache(
            SimpleCache<NodeRef, Map<ConfigKey, NodeRef>> customizationFolderCache) {
        this.customizationFolderCache = customizationFolderCache;
    }

    public final void setRootPreferencesUpdater(RootPreferencesUpdater rootPreferencesUpdater) {
        this.rootPreferencesUpdater = rootPreferencesUpdater;
    }

    public final void setMultilingualContentService(
            MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @param mimetypeService the mimetypeService to set
     */
    public final void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Build key with config folder names for caching purposes.
     *
     * @author Yanick Pignot
     */
    static class ConfigKey implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1806250009495739421L;

        private final String configTypeRoot;
        private final String configSubType;
        private final String configElement;

        /**
         * @param configTypeRoot
         * @param configSubType
         * @param configElement
         */
        private ConfigKey(
                final String configTypeRoot, final String configSubType, final String configElement) {
            super();
            this.configTypeRoot = configTypeRoot;
            this.configSubType = configSubType;
            this.configElement = configElement;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((configElement == null) ? 0 : configElement.hashCode());
            result = PRIME * result + ((configSubType == null) ? 0 : configSubType.hashCode());
            result = PRIME * result + ((configTypeRoot == null) ? 0 : configTypeRoot.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ConfigKey other = (ConfigKey) obj;
            if (configElement == null) {
                if (other.configElement != null) {
                    return false;
                }
            } else if (!configElement.equals(other.configElement)) {
                return false;
            }
            if (configSubType == null) {
                if (other.configSubType != null) {
                    return false;
                }
            } else if (!configSubType.equals(other.configSubType)) {
                return false;
            }
            if (configTypeRoot == null) {
                if (other.configTypeRoot != null) {
                    return false;
                }
            } else if (!configTypeRoot.equals(other.configTypeRoot)) {
                return false;
            }
            return true;
        }
    }
}
