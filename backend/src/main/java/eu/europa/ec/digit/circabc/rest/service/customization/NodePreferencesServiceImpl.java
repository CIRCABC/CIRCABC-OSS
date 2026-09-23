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
package eu.europa.ec.digit.circabc.rest.service.customization;

import static org.alfresco.model.ContentModel.*;

import io.swagger.api.CircabcApi;
import io.swagger.exception.CustomizationException;
import io.swagger.model.alfresco.CircabcModel;
import java.io.*;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link NodePreferencesService}.
 *
 * <p>This service manages per-node customization ("preference") files stored in the Alfresco
 * repository. Any node can be made <em>configurable</em>, which attaches a hidden customization
 * container to it. Customization files are then organized inside that container following a
 * three-level folder hierarchy: {@code configTypeRoot / configSubType / configElement / fileName}.
 *
 * <p>When a customization is requested for a node, the lookup walks up the node hierarchy
 * (including multilingual pivots and the CIRCABC root/dictionary) until a matching configuration is
 * found, allowing customizations to be inherited from ancestor nodes and to fall back to global
 * defaults stored under the CIRCABC dictionary.
 *
 * <p>Two Alfresco {@link org.alfresco.repo.cache.SimpleCache caches} are used to speed up repeated
 * lookups: one mapping a node to its configuration container and one mapping a container to its
 * resolved configuration folders.
 *
 * @author Yanick Pignot
 */
public class NodePreferencesServiceImpl implements NodePreferencesService {

  private static final String THE_CONFIGURATION_FILE_NAME =
    "The configuration file name";
  private static final String THE_NODE_REFERENCE = "The node reference";
  /**
   * The configuration service root container type
   */
  public static final QName TYPE_CUSTOMIZATION_CONTAINER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationContainer"
  );
  /**
   * The configuration service root container type
   */
  public static final QName TYPE_CUSTOMIZATION_FOLDER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationFolder"
  );
  /**
   * The configuration service root container type
   */
  public static final QName TYPE_CUSTOMIZATION_CONTENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationContent"
  );
  /**
   * The configuration service root association name
   */
  public static final QName ASSOC_CUSTOMIZE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customize"
  );
  private static final String DEFAULT = "default.";
  /**
   * The ml root type qname
   */
  private static final QName TYPE_ML_ROOT = QName.createQName(
    NamespaceService.CONTENT_MODEL_1_0_URI,
    "mlRoot"
  );
  /**
   * Logger
   */
  private static final Log logger = LogFactory.getLog(
    NodePreferencesServiceImpl.class
  );

  @Autowired
  private NodeService nodeService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private MultilingualContentService multilingualContentService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private RootPreferencesUpdater rootPreferencesUpdater;

  @Autowired
  private MimetypeService mimetypeService;

  private NodeRef circabcNodeRef = null;

  private NodeRef circabcDictionaryNodeRef = null;

  private SimpleCache<NodeRef, NodeRef> containerCache;

  private SimpleCache<
    NodeRef,
    Map<ConfigKey, NodeRef>
  > customizationFolderCache;

  /**
   * Refreshes the customization root reference by (re)initializing the CIRCABC dictionary space that
   * holds the default/global customization files.
   */
  public void updateRootReference() {
    rootPreferencesUpdater.updateSpace(getCircabcDDNodeRef());
  }

  /**
   * Indicates whether the given node has been made configurable, i.e. whether a customization
   * container is currently attached to it.
   *
   * @param ref the node to test; must not be {@code null}
   * @return {@code true} if the node has an associated customization container, {@code false}
   *     otherwise
   */
  public boolean isNodeConfigurable(final NodeRef ref) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);

    return getConfigurationContainer(ref, false) != null;
  }

  /**
   * Makes the given node configurable by creating and attaching a customization container to it.
   *
   * @param ref the node to make configurable; must not be {@code null} and must not be a
   *     multilingual container
   * @return the {@link NodeRef} of the newly created customization container
   * @throws CustomizationException if the node is a multilingual container or is already
   *     configurable
   */
  public NodeRef makeConfigurable(final NodeRef ref)
    throws CustomizationException {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);

    if (
      nodeService.getType(ref).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)
    ) {
      throw new CustomizationException(
        "Impossible to make a multilingual container configurable."
      );
    }

    if (isNodeConfigurable(ref)) {
      throw new CustomizationException(
        "The node " + ref + " is already setted configurable."
      );
    }

    final NodeRef configContainer = nodeService
      .createNode(
        ref,
        ASSOC_CUSTOMIZE,
        TYPE_CUSTOMIZATION_CONTAINER,
        TYPE_CUSTOMIZATION_CONTAINER
      )
      .getChildRef();

    containerCache.clear();

    return configContainer;
  }

  /**
   * Resolves the list of customization files matching the given configuration coordinates for a
   * node, walking up the node hierarchy until a matching configuration is found.
   *
   * <p>Starting from {@code nodeRef} (or its multilingual pivot / the CIRCABC root for special
   * types), the lookup ascends through parent configuration containers until customization files
   * are located.
   *
   * @param nodeRef the node for which customization files are requested; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @return the non-empty list of matching configuration files
   * @throws CustomizationException if no customization is found for the given coordinates
   */
  public List<NodeRef> getConfigurationFiles(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement
  ) throws CustomizationException {
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
        } else if (
          recursNodeType.equals(CircabcModel.TYPE_CATEGORY_HEADER) ||
          recursNodeType.equals(TYPE_ML_ROOT)
        ) {
          recusionRef = circabcApi.getCircabcNodeRef();
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
      configItems = getConfigFilesImpl(
        configTypeRoot,
        configSubType,
        configElement,
        container
      );
    } while (container != null && configItems == null);

    if (configItems == null || configItems.isEmpty()) {
      final String message =
        "No customization found for data: " +
        configTypeRoot +
        "/" +
        configSubType +
        "/" +
        configElement;

      logger.error(message);

      throw new CustomizationException(message);
    } else {
      return configItems;
    }
  }

  /**
   * Returns the single default configuration file for the given configuration coordinates.
   *
   * <p>If several matching files exist, the one whose name starts with {@code "default."} is
   * preferred; otherwise the oldest (earliest created) file is returned.
   *
   * @param nodeRef the node for which the default customization file is requested; must not be
   *     {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @return the {@link NodeRef} of the resolved default configuration file
   * @throws CustomizationException if no customization is found for the given coordinates
   */
  @Override
  public NodeRef getDefaultConfigurationFile(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement
  ) throws CustomizationException {
    final List<NodeRef> files = getConfigurationFiles(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement
    );

    // the getConfigurationFiles method doesn't allow having no configuration file.

    if (files.size() == 1) {
      return files.get(0);
    } else {
      NodeRef defaultFile = null;
      Date defaultFileCreated = null;
      Map<QName, Serializable> props;
      String name = null;
      Date created = null;
      for (final NodeRef file : files) {
        props = nodeService.getProperties(nodeRef);
        name = (String) props.get(PROP_NAME);
        created = (Date) props.get(PROP_CREATED);
        if (name.startsWith(DEFAULT)) {
          defaultFile = file;
          break;
        } else if (
          defaultFile == null ||
          defaultFileCreated == null ||
          defaultFileCreated.after(created)
        ) {
          defaultFileCreated = created;
          defaultFile = file;
        }
      }

      return defaultFile;
    }
  }

  /**
   * Creates or updates a customization file for the given configuration coordinates, taking the file
   * body from an {@link InputStream}.
   *
   * @param nodeRef the configurable node under which the file is stored; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @param content the file content as a stream
   * @return the {@link NodeRef} of the created or updated customization file
   * @throws CustomizationException if the node is not configurable or the content cannot be written
   */
  public NodeRef addCustomizationFile(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName,
    final InputStream content
  ) throws CustomizationException {
    return addCustomizationFileImpl(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName,
      content
    );
  }

  /**
   * Creates or updates a customization file for the given configuration coordinates, taking the file
   * body from a {@link File}.
   *
   * @param nodeRef the configurable node under which the file is stored; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @param content the file content as a {@link File}
   * @return the {@link NodeRef} of the created or updated customization file
   * @throws CustomizationException if the node is not configurable or the content cannot be written
   */
  public NodeRef addCustomizationFile(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName,
    final File content
  ) throws CustomizationException {
    return addCustomizationFileImpl(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName,
      content
    );
  }

  /**
   * Creates or updates a customization file for the given configuration coordinates, taking the file
   * body from a {@link String}.
   *
   * @param nodeRef the configurable node under which the file is stored; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @param content the file content as a string
   * @return the {@link NodeRef} of the created or updated customization file
   * @throws CustomizationException if the node is not configurable or the content cannot be written
   */
  public NodeRef addCustomizationFile(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName,
    final String content
  ) throws CustomizationException {
    return addCustomizationFileImpl(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName,
      content
    );
  }

  /**
   * Creates or updates a customization file for the given configuration coordinates, serializing a
   * {@link Properties} object as the file body.
   *
   * @param nodeRef the configurable node under which the file is stored; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @param content the properties to store as the file content
   * @return the {@link NodeRef} of the created or updated customization file
   * @throws CustomizationException if the node is not configurable or the content cannot be written
   */
  public NodeRef addCustomizationFile(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName,
    final Properties content
  ) throws CustomizationException {
    return addCustomizationFileImpl(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName,
      content
    );
  }

  /**
   * Shared implementation backing the {@code addCustomizationFile} overloads. Ensures the node is
   * configurable, lazily creates the {@code configTypeRoot/configSubType/configElement} folder
   * hierarchy, then creates (with versioning enabled) or updates the target file and writes the
   * supplied content according to its runtime type.
   *
   * @param nodeRef the configurable node under which the file is stored; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @param contentAsObject the content to write; an {@link InputStream}, {@link File},
   *     {@link Properties}, {@code null} (empty content) or any other object (written via
   *     {@link Object#toString()})
   * @return the {@link NodeRef} of the created or updated customization file
   * @throws CustomizationException if the node is not configurable or an I/O error occurs while
   *     writing properties content
   */
  private NodeRef addCustomizationFileImpl(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName,
    final Object contentAsObject
  ) throws CustomizationException {
    checkParamters(nodeRef, configTypeRoot, configSubType, configElement);

    ParameterCheck.mandatoryString(THE_CONFIGURATION_FILE_NAME, fileName);

    // check the root config  conatiner
    final NodeRef configContainer = getConfigurationContainer(nodeRef, false);
    if (configContainer == null) {
      throw new CustomizationException(
        "The node " +
          nodeRef +
          " must be setted as configurable. Please use the NodePreferencesServiceImpl.makeConfigurable method."
      );
    }

    // remove container cache
    customizationFolderCache.remove(configContainer);

    // check the root custom container
    NodeRef rootCustomContainer = getChildByName(
      configContainer,
      ASSOC_CHILDREN,
      configTypeRoot
    );
    if (rootCustomContainer == null) {
      rootCustomContainer = createFolder(configContainer, configTypeRoot);
    }

    // check the configuration space root
    NodeRef subCustomContainer = getChildByName(
      rootCustomContainer,
      ASSOC_CHILDREN,
      configSubType
    );
    if (subCustomContainer == null) {
      subCustomContainer = createFolder(rootCustomContainer, configSubType);
    }

    // check the configuration element
    NodeRef elementCustomContainer = getChildByName(
      subCustomContainer,
      ASSOC_CHILDREN,
      configElement
    );
    if (elementCustomContainer == null) {
      elementCustomContainer = createFolder(subCustomContainer, configElement);
    }

    NodeRef contentRef = getChildByName(
      elementCustomContainer,
      ASSOC_CHILDREN,
      fileName
    );
    if (contentRef == null) {
      contentRef = createFile(elementCustomContainer, fileName);

      final Map<QName, Serializable> versionProps = Collections.<
          QName,
          Serializable
        >singletonMap(PROP_AUTO_VERSION, Boolean.TRUE);
      nodeService.addAspect(contentRef, ASPECT_VERSIONABLE, versionProps);
    }

    //	set the body (in the content)
    final ContentWriter writter = contentService.getWriter(
      contentRef,
      CircabcModel.PROP_CONTENT,
      true
    );
    writter.setMimetype(mimetypeService.guessMimetype(fileName));
    if (contentAsObject == null) {
      writter.putContent("");
    } else if (contentAsObject instanceof InputStream inputStream) {
      writter.putContent(inputStream);
    } else if (contentAsObject instanceof File file) {
      writter.putContent(file);
    } else if (contentAsObject instanceof Properties properties) {
      final OutputStream contentOutputStream = writter.getContentOutputStream();
      try {
        properties.store(contentOutputStream, "");
        contentOutputStream.flush();
        contentOutputStream.close();
      } catch (IOException e) {
        throw new CustomizationException(
          "IO error occurs when update customization content: " +
            e.getMessage(),
          e
        );
      }
    } else {
      writter.putContent(contentAsObject.toString());
    }

    return contentRef;
  }

  /**
   * Tests whether a customization file with the given name exists for the specified configuration
   * coordinates on the node.
   *
   * @param nodeRef the node to inspect; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @return {@code true} if the file exists, {@code false} otherwise
   */
  public boolean customizationFileExists(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName
  ) {
    final NodeRef file = getCustomizationOrNull(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName
    );
    return file != null;
  }

  /**
   * Returns the customization file with the given name for the specified configuration coordinates.
   *
   * @param nodeRef the node to inspect; must not be {@code null}
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file; must not be {@code null} or empty
   * @return the {@link NodeRef} of the customization file
   * @throws CustomizationException if no such file exists
   */
  public NodeRef getCustomization(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName
  ) throws CustomizationException {
    final NodeRef file = getCustomizationOrNull(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName
    );

    if (file == null) {
      throw new CustomizationException(
        "Customization " + fileName + " doens't exists."
      );
    } else {
      return file;
    }
  }

  /**
   * Removes the customization file with the given name for the specified configuration coordinates,
   * then cleans up any now-empty parent configuration folders up to the node itself.
   *
   * @param nodeRef the node owning the customization; must not be {@code null} and must not be the
   *     CIRCABC root node
   * @param configTypeRoot the top-level configuration folder name; must not be {@code null}
   * @param configSubType the second-level configuration folder name; must not be {@code null}
   * @param configElement the third-level configuration folder name; must not be {@code null}
   * @param fileName the name of the customization file to remove; must not be {@code null} or empty
   * @throws CustomizationException if the node is the root folder or the customization does not exist
   */
  public void removeCustomization(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName
  ) throws CustomizationException {
    checkParamters(nodeRef, configTypeRoot, configSubType, configElement);
    ParameterCheck.mandatoryString(THE_CONFIGURATION_FILE_NAME, fileName);

    if (nodeRef.equals(getCircabcRootNodeRef())) {
      throw new CustomizationException(
        "Impossible to remove customization of the root folder."
      );
    }

    final NodeRef file = getCustomizationOrNull(
      nodeRef,
      configTypeRoot,
      configSubType,
      configElement,
      fileName
    );
    final NodeRef container = getConfigurationContainer(nodeRef, false);

    containerCache.clear();
    customizationFolderCache.remove(container);

    if (file == null) {
      throw new CustomizationException(
        "Customization " + fileName + " doens't exists."
      );
    } else {
      NodeRef parent;
      NodeRef fileToDelete = file;

      // clleanup the configuration spaces
      while (fileToDelete != null && !fileToDelete.equals(nodeRef)) {
        parent = nodeService.getPrimaryParent(fileToDelete).getParentRef();
        nodeService.deleteNode(fileToDelete);
        if (nodeService.getChildAssocs(parent).isEmpty()) {
          fileToDelete = parent;
        } else {
          fileToDelete = null;
        }
      }
    }
  }

  /**
   * Resolves the "owning" content node for a given customization node.
   *
   * <p>Given a node located somewhere inside a customization hierarchy (a customization container,
   * folder or file), this walks up the primary parent chain until it reaches the customization
   * container and returns the node that was made configurable. The CIRCABC dictionary node maps back
   * to the CIRCABC root node.
   *
   * @param customizationNode a node within a customization hierarchy; may be {@code null}
   * @return the configurable node owning the customization, or {@code null} if
   *     {@code customizationNode} is {@code null}
   */
  public NodeRef getCustomizationFromNode(final NodeRef customizationNode) {
    if (customizationNode == null) {
      return null;
    } else if (customizationNode.equals(getCircabcDDNodeRef())) {
      return getCircabcRootNodeRef();
    } else if (
      nodeService
        .getType(customizationNode)
        .equals(TYPE_CUSTOMIZATION_CONTAINER)
    ) {
      return nodeService.getPrimaryParent(customizationNode).getParentRef();
    } else {
      return getCustomizationFromNode(
        nodeService.getPrimaryParent(customizationNode).getParentRef()
      );
    }
  }

  private NodeRef getCustomizationOrNull(
    final NodeRef nodeRef,
    final String configTypeRoot,
    final String configSubType,
    final String configElement,
    final String fileName
  ) {
    checkParamters(nodeRef, configTypeRoot, configSubType, configElement);
    ParameterCheck.mandatoryString(THE_CONFIGURATION_FILE_NAME, fileName);

    // check the root config  conatiner
    final NodeRef configContainer = getConfigurationContainer(nodeRef, false);
    if (configContainer == null) {
      return null;
    }

    // check the root custom container
    NodeRef rootCustomContainer = getChildByName(
      configContainer,
      ASSOC_CHILDREN,
      configTypeRoot
    );
    if (rootCustomContainer == null) {
      return null;
    }

    // check the configuration space root
    NodeRef subCustomContainer = getChildByName(
      rootCustomContainer,
      ASSOC_CHILDREN,
      configSubType
    );
    if (subCustomContainer == null) {
      return null;
    }

    // check the configuration element
    NodeRef elementCustomContainer = getChildByName(
      subCustomContainer,
      ASSOC_CHILDREN,
      configElement
    );
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
    final String configElement
  ) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, nodeRef);
    ParameterCheck.mandatory(
      "The config type root (root folder name)",
      configTypeRoot
    );
    ParameterCheck.mandatory(
      "The config sub type (sub type folder name)",
      configSubType
    );
    ParameterCheck.mandatory(
      "The config element (config element folder name)",
      configElement
    );
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
    final NodeRef container
  ) {
    final ConfigKey key = new ConfigKey(
      configTypeRoot,
      configSubType,
      configElement
    );
    Map<ConfigKey, NodeRef> folders = customizationFolderCache.get(container);
    if (folders == null) {
      folders = new HashMap<>();
      customizationFolderCache.put(container, folders);
    }
    NodeRef configElementRef = folders.containsKey(key)
      ? folders.get(key)
      : findAndCacheConfigElement(
          folders,
          key,
          container,
          configTypeRoot,
          configSubType,
          configElement
        );
    return getChildNodeRefs(configElementRef);
  }

  private NodeRef findAndCacheConfigElement(
    Map<ConfigKey, NodeRef> folders,
    ConfigKey key,
    NodeRef container,
    String configTypeRoot,
    String configSubType,
    String configElement
  ) {
    QName childAssoc = isRootContainer(container)
      ? ASSOC_CONTAINS
      : ASSOC_CHILDREN;
    NodeRef configTypeRootRef = getChildByName(
      container,
      childAssoc,
      configTypeRoot
    );
    if (configTypeRootRef == null) return null;
    NodeRef configSubTypeRef = getChildByName(
      configTypeRootRef,
      childAssoc,
      configSubType
    );
    if (configSubTypeRef == null) return null;
    NodeRef configElementRef = getChildByName(
      configSubTypeRef,
      childAssoc,
      configElement
    );
    if (configElementRef != null) {
      folders.put(key, configElementRef);
    }
    return configElementRef;
  }

  private List<NodeRef> getChildNodeRefs(NodeRef parentRef) {
    if (parentRef == null) return Collections.emptyList();
    List<ChildAssociationRef> childs = nodeService.getChildAssocs(parentRef);
    List<NodeRef> result = new ArrayList<>(childs.size());
    for (ChildAssociationRef child : childs) {
      result.add(child.getChildRef());
    }
    return result;
  }

  private NodeRef getChildByName(
    final NodeRef parent,
    final QName childAssoc,
    final String name
  ) {
    if (childAssoc.equals(ASSOC_CONTAINS)) {
      return nodeService.getChildByName(parent, childAssoc, name);
    } else {
      final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(
        parent,
        childAssoc,
        createNameQName(name)
      );

      if (assocs == null || assocs.isEmpty()) {
        return null;
      } else {
        if (assocs.size() > 1 && logger.isWarnEnabled()) {
          logger.warn(
            assocs.size() +
              " children found with qname " +
              createNameQName(name) +
              " for customization container/file " +
              parent
          );
        }
        return assocs.get(0).getChildRef();
      }
    }
  }

  private boolean isRootContainer(final NodeRef container) {
    return container != null && container.equals(getCircabcDDNodeRef());
  }

  private NodeRef getConfigurationContainer(
    final NodeRef originalRef,
    final boolean recursive
  ) {
    return getConfigurationContainer(originalRef, originalRef, recursive);
  }

  /**
   * @param originalRef For caching purposes
   */
  private NodeRef getConfigurationContainer(
    final NodeRef originalRef,
    final NodeRef recursionRef,
    final boolean recursive
  ) {
    if (recursionRef == null) return null;
    if (!recursive) containerCache.remove(recursionRef);

    if (
      nodeService
        .getType(recursionRef)
        .equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)
    ) {
      return getConfigurationContainer(
        originalRef,
        multilingualContentService.getPivotTranslation(recursionRef),
        recursive
      );
    }

    NodeRef cachedContainer = containerCache.get(recursionRef);
    if (cachedContainer != null) {
      if (nodeService.exists(cachedContainer)) return cachedContainer;
      containerCache.remove(recursionRef);
    }

    if (getCircabcRootNodeRef().equals(recursionRef)) {
      NodeRef circabcDDNodeRef = getCircabcDDNodeRef();
      containerCache.put(originalRef, circabcDDNodeRef);
      return circabcDDNodeRef;
    }

    return findOrRecurseContainer(originalRef, recursionRef, recursive);
  }

  private NodeRef findOrRecurseContainer(
    NodeRef originalRef,
    NodeRef recursionRef,
    boolean recursive
  ) {
    List<ChildAssociationRef> associations = nodeService.getChildAssocs(
      recursionRef,
      ASSOC_CUSTOMIZE,
      TYPE_CUSTOMIZATION_CONTAINER
    );
    if (associations == null || associations.isEmpty()) {
      return recursive
        ? getConfigurationContainer(
            originalRef,
            nodeService.getPrimaryParent(recursionRef).getParentRef(),
            recursive
          )
        : null;
    }
    if (associations.size() > 1) {
      logger.warn(
        "More than one Preference Root Node found for the node " + recursionRef
      );
    }
    NodeRef container = associations.get(0).getChildRef();
    containerCache.put(originalRef, container);
    return container;
  }

  /**
   * @return
   */
  private NodeRef getCircabcDDNodeRef() {
    if (circabcDictionaryNodeRef == null) {
      circabcDictionaryNodeRef = circabcApi.getCircabcDictionaryNodeRef();
    }
    return circabcDictionaryNodeRef;
  }

  /**
   * @return
   */
  private NodeRef getCircabcRootNodeRef() {
    if (circabcNodeRef == null) {
      circabcNodeRef = circabcApi.getCircabcNodeRef();
    }
    return circabcNodeRef;
  }

  private NodeRef createFolder(final NodeRef parent, final String name) {
    return createNode(parent, name, TYPE_CUSTOMIZATION_FOLDER);
  }

  private NodeRef createFile(final NodeRef parent, final String name) {
    return createNode(parent, name, TYPE_CUSTOMIZATION_CONTENT);
  }

  private NodeRef createNode(
    final NodeRef parent,
    final String name,
    final QName typeQname
  ) {
    final Map<QName, Serializable> properties = Collections.<
        QName,
        Serializable
      >singletonMap(PROP_NAME, name);
    final ChildAssociationRef assoc = nodeService.createNode(
      parent,
      ASSOC_CHILDREN,
      createNameQName(name),
      typeQname,
      properties
    );

    return assoc.getChildRef();
  }

  private QName createNameQName(final String name) {
    return QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      QName.createValidLocalName(name)
    );
  }

  /**
   * @param nodeService the nodeService to set
   */
  public final void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
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
  public final void setContainerCache(
    SimpleCache<NodeRef, NodeRef> containerCache
  ) {
    this.containerCache = containerCache;
  }

  /**
   * @param customizationFolderCache the customizationFolderCache to set
   */
  public final void setCustomizationFolderCache(
    SimpleCache<NodeRef, Map<ConfigKey, NodeRef>> customizationFolderCache
  ) {
    this.customizationFolderCache = customizationFolderCache;
  }

  /**
   * @param rootPreferencesUpdater the rootPreferencesUpdater to set
   */
  public final void setRootPreferencesUpdater(
    RootPreferencesUpdater rootPreferencesUpdater
  ) {
    this.rootPreferencesUpdater = rootPreferencesUpdater;
  }

  /**
   * @param multilingualContentService the multilingualContentService to set
   */
  public final void setMultilingualContentService(
    MultilingualContentService multilingualContentService
  ) {
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
      final String configTypeRoot,
      final String configSubType,
      final String configElement
    ) {
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
      result =
        PRIME * result +
        ((configElement == null) ? 0 : configElement.hashCode());
      result =
        PRIME * result +
        ((configSubType == null) ? 0 : configSubType.hashCode());
      result =
        PRIME * result +
        ((configTypeRoot == null) ? 0 : configTypeRoot.hashCode());
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
