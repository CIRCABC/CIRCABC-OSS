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

import io.swagger.model.alfresco.CircabcModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.extensions.config.*;

/**
 * Synchronizes an Alfresco folder (a "root space") with a declarative resource list defined in
 * Spring configuration.
 *
 * <p>Given a root {@link NodeRef} and a configured condition, this class reads the associated
 * {@code space-contents} configuration area and recreates the described folder/file hierarchy under
 * the root space. For each configured {@code space} element a folder is created (if missing), and
 * for each {@code file} element a content node is created or refreshed from a war/ear packaged
 * resource. Files can additionally be versioned, marked as inline-editable, assigned a revision
 * number and granted guest/registered permissions, all driven by attributes on the configuration
 * elements.
 *
 * <p>The class is intended to be wired as a Spring bean; its Alfresco service collaborators are
 * injected through the provided setters.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. ConfigException was moved to Spring. ConfigLookupContext was moved to Spring.
 * ConfigService was moved to Spring. This class seems to be developed for CircaBC
 */
public class RootPreferencesUpdater {

  /** Name of the configuration area (evaluator) holding the space/file definitions. */
  private static final String CONFIG_AREA = "space-contents";
  /** Configuration element name describing a folder to create. */
  private static final String ELEM_SPACE = "space";
  /** Configuration element name wrapping the top-level children of the root space. */
  private static final String ELEM_ROOT = "root";
  /** Configuration element name describing a content file to create or update. */
  private static final String ELEM_FILE = "file";
  /** Attribute holding the node name of a space or file element. */
  private static final String ATTR_NAME = "name";
  /** Attribute flagging whether a created file should be made versionable. */
  private static final String ATTR_VERSIONABLE = "versionable";
  /** Attribute flagging whether a created file should be made inline-editable. */
  private static final String ATTR_EDITONLINE = "editOnline";
  /** Attribute forcing the content to be (re)written regardless of the stored revision. */
  private static final String ATTR_FORCEUPDATE = "forceUpdate";
  /** Attribute holding the revision number of the file (note: original spelling kept). */
  private static final String ATTR_REVISION = "revison";
  /** Attribute holding the permission to grant to the {@code guest} authority. */
  private static final String ATTR_GUEST_PERM = "guestPerm";
  /** Attribute holding the permission to grant to the {@code GROUP_EVERYONE} authority. */
  private static final String ATTR_REGISTRED_PERM = "registredPerm";

  /** Alfresco node service used to read/create nodes and manage aspects/properties. */
  private NodeService nodeService;
  /** Alfresco file/folder service used to create folder and content nodes by name. */
  private FileFolderService fileFolderService;
  /** Alfresco content service used to write the resource content onto content nodes. */
  private ContentService contentService;
  /** Alfresco mimetype service used to resolve mimetypes and guess content encoding. */
  private MimetypeService mimetypeService;
  /** Spring config service used to look up the declarative space/file definitions. */
  private ConfigService configService;
  /** Alfresco permission service used to grant configured permissions on created files. */
  private PermissionService permissionService;

  /** Configuration condition selecting which {@code space-contents} definition to apply. */
  private String configCondition;

  /**
   * Synchronizes the given root space with the configured {@code space-contents} definition.
   *
   * <p>Looks up the configuration identified by {@link #configCondition}, reads its {@code root}
   * element and recursively creates the described folders and files beneath {@code rootSpace}.
   *
   * @param rootSpace the folder to synchronize; must not be {@code null}
   * @throws ConfigException if {@code configCondition} is not set, or if the configuration cannot be
   *     resolved or applied to the space
   */
  void updateSpace(final NodeRef rootSpace) {
    ParameterCheck.mandatory("The root folder", rootSpace);
    ParameterCheck.mandatoryString(
      "The config condition (evaluator='string-compare' condition='??')",
      configCondition
    );

    try {
      final ConfigLookupContext clContext = new ConfigLookupContext(
        CONFIG_AREA
      );
      final Config configConditions = configService.getConfig(
        configCondition,
        clContext
      );

      final ConfigElement configElements = configConditions.getConfigElement(
        ELEM_ROOT
      );

      createNodes(rootSpace, configElements.getChildren());
    } catch (Exception e) {
      throw new ConfigException(
        "Impossible to update space: " +
          rootSpace +
          " with the config condition " +
          configCondition +
          ". " +
          e.getMessage(),
        e
      );
    }
  }

  private void createNodes(
    final NodeRef parentSpace,
    final Collection<ConfigElement> configElements
  ) throws ConfigException, IOException {
    if (configElements == null) return;
    for (final ConfigElement element : configElements) {
      switch (element.getName()) {
        case ELEM_SPACE:
          processSpaceElement(parentSpace, element);
          break;
        case ELEM_FILE:
          processFileElement(parentSpace, element);
          break;
        default:
          throw new ConfigException("Unknow element name " + element.getName());
      }
    }
  }

  private void processSpaceElement(NodeRef parentSpace, ConfigElement element)
    throws ConfigException, IOException {
    String name = element.getAttribute(ATTR_NAME);
    if (!isNameValid(name)) throw new ConfigException(
      "The space name attribute is mandatory"
    );
    NodeRef spaceRef = nodeService.getChildByName(
      parentSpace,
      ContentModel.ASSOC_CONTAINS,
      name
    );
    if (spaceRef == null) {
      spaceRef = fileFolderService
        .create(parentSpace, name, ContentModel.TYPE_FOLDER)
        .getNodeRef();
    }
    createNodes(spaceRef, element.getChildren());
  }

  private void processFileElement(NodeRef parentSpace, ConfigElement element)
    throws ConfigException, IOException {
    String name = element.getAttribute(ATTR_NAME);
    if (!isNameValid(name)) throw new ConfigException(
      "The content name attribute is mandatory"
    );
    NodeRef contentRef = nodeService.getChildByName(
      parentSpace,
      ContentModel.ASSOC_CONTAINS,
      name
    );
    boolean newContent = contentRef == null;
    if (newContent) {
      contentRef = fileFolderService
        .create(parentSpace, name, ContentModel.TYPE_CONTENT)
        .getNodeRef();
    }
    Integer revision = getInteger(element.getAttribute(ATTR_REVISION));
    boolean forceUpdate = isTrue(element.getAttribute(ATTR_FORCEUPDATE));
    validateRevision(forceUpdate, revision);
    if (newContent || forceUpdate || updateContent(contentRef, revision)) {
      writeContentAndSetPermissions(contentRef, element, name, revision);
    }
    applyVersionable(element, contentRef);
    applyInlineEditable(element, contentRef);
  }

  private void validateRevision(boolean forceUpdate, Integer revision)
    throws ConfigException {
    if (!forceUpdate) {
      if (revision == null) throw new ConfigException(
        "In force update is null or false, a revision number is mandatory (attribute 'revision')"
      );
      if (revision < 1) throw new ConfigException(
        "The revision number is invalid. Found: " +
          revision +
          " and any strictly positive number expected."
      );
    }
  }

  private void writeContentAndSetPermissions(
    NodeRef contentRef,
    ConfigElement element,
    String name,
    Integer revision
  ) throws IOException {
    InputStream inputStream = getInpuStreamFromResource(element.getValue());
    ContentWriter writer = contentService.getWriter(
      contentRef,
      ContentModel.PROP_CONTENT,
      true
    );
    String mimetype = getMimeTypeForFileName(name);
    writer.setMimetype(mimetype);
    writer.setEncoding(guessEncoding(inputStream, mimetype));
    writer.putContent(inputStream);
    updateRevision(revision, contentRef);
    String guestPerm = element.getAttribute(ATTR_GUEST_PERM);
    String registredPerm = element.getAttribute(ATTR_REGISTRED_PERM);
    if (isNameValid(guestPerm)) {
      permissionService.setPermission(contentRef, "guest", guestPerm, true);
    }
    if (isNameValid(registredPerm)) {
      permissionService.setPermission(
        contentRef,
        "GROUP_EVERYONE",
        registredPerm,
        true
      );
    }
  }

  private void updateRevision(Integer revision, NodeRef contentRef) {
    if (revision != null) {
      final Map<QName, Serializable> revisionProps = Collections.singletonMap(
        CircabcModel.PROP_REVISION_NUMBER,
        (Serializable) revision
      );

      if (nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)) {
        nodeService.addProperties(contentRef, revisionProps);
      } else {
        nodeService.addAspect(
          contentRef,
          CircabcModel.ASPECT_REVISIONABLE,
          revisionProps
        );
      }
    }
  }

  private boolean updateContent(
    final NodeRef contentRef,
    final Integer revision
  ) {
    if (
      revision == null ||
      revision < 1 ||
      !nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)
    ) {
      return true;
    } else {
      final Integer oldRevision = (Integer) nodeService.getProperty(
        contentRef,
        CircabcModel.PROP_REVISION_NUMBER
      );

      return oldRevision == null || revision > oldRevision;
    }
  }

  /**
   * @param element
   * @param name
   * @param contentRef
   * @throws InvalidNodeRefException
   * @throws InvalidAspectException
   */
  private void applyInlineEditable(
    final ConfigElement element,
    NodeRef contentRef
  ) throws InvalidNodeRefException, InvalidAspectException {
    if (
      isTrue(element.getAttribute(ATTR_EDITONLINE)) &&
      !nodeService.hasAspect(contentRef, ApplicationModel.ASPECT_INLINEEDITABLE)
    ) {
      nodeService.addAspect(
        contentRef,
        ApplicationModel.ASPECT_INLINEEDITABLE,
        Collections.singletonMap(
          ApplicationModel.PROP_EDITINLINE,
          (Serializable) Boolean.TRUE
        )
      );
    }
  }

  /**
   * @param element
   * @param name
   * @param contentRef
   * @throws InvalidNodeRefException
   * @throws InvalidAspectException
   */
  private void applyVersionable(final ConfigElement element, NodeRef contentRef)
    throws InvalidNodeRefException, InvalidAspectException {
    if (
      isTrue(element.getAttribute(ATTR_VERSIONABLE)) &&
      !nodeService.hasAspect(contentRef, ContentModel.ASPECT_VERSIONABLE)
    ) {
      nodeService.addAspect(
        contentRef,
        ContentModel.ASPECT_VERSIONABLE,
        Collections.singletonMap(
          ContentModel.PROP_AUTO_VERSION,
          (Serializable) Boolean.TRUE
        )
      );
    }
  }

  /**
   * @param name
   * @return
   */
  private boolean isNameValid(final String name) {
    return name != null && !name.trim().isEmpty();
  }

  /**
   * @param booleanStr
   * @return
   */
  private boolean isTrue(final String booleanStr) {
    return booleanStr != null && Boolean.parseBoolean(booleanStr);
  }

  /**
   * @param intStr
   * @return
   */
  private Integer getInteger(final String intStr) {
    return intStr == null ? null : Integer.parseInt(intStr);
  }

  private String getMimeTypeForFileName(final String filename) {
    // fall back to binary mimetype if no match found
    String mimetype = MimetypeMap.MIMETYPE_BINARY;
    int extIndex = filename.lastIndexOf('.');
    if (extIndex != -1) {
      String ext = filename.substring(extIndex + 1).toLowerCase();
      String mt = mimetypeService.getMimetypesByExtension().get(ext);
      if (mt != null) {
        mimetype = mt;
      }
    }

    return mimetype;
  }

  private String guessEncoding(InputStream is, String mimetype) {
    try {
      final ContentCharsetFinder charsetFinder =
        mimetypeService.getContentCharsetFinder();
      final Charset charset = charsetFinder.getCharset(is, mimetype);
      return charset.name();
    } catch (Exception t) {
      return Charset.defaultCharset().name();
    }
  }

  private InputStream getInpuStreamFromResource(final String contentUri)
    throws IOException {
    final PathMatchingResourcePatternResolver resolver =
      new PathMatchingResourcePatternResolver();
    final Resource resource = resolver.getResource(contentUri);
    return resource.getInputStream();
  }

  /**
   * Returns the configuration condition selecting the {@code space-contents} definition to apply.
   *
   * @return the configured condition, or {@code null} if none has been set
   */
  public final String getConfigCondition() {
    return configCondition;
  }

  /**
   * Sets the configuration condition selecting the {@code space-contents} definition to apply.
   *
   * @param configCondition the condition value matched against the config evaluator
   */
  public final void setConfigCondition(String configCondition) {
    this.configCondition = configCondition;
  }

  /**
   * Injects the Alfresco content service used to write resource content onto content nodes.
   *
   * @param contentService the content service to use
   */
  public final void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Injects the Alfresco file/folder service used to create folders and content nodes.
   *
   * @param fileFolderService the file/folder service to use
   */
  public final void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Injects the Spring config service used to look up the declarative definitions.
   *
   * @param configService the config service to use
   */
  public final void setConfigService(ConfigService configService) {
    this.configService = configService;
  }

  /**
   * Injects the Alfresco mimetype service used to resolve mimetypes and guess encoding.
   *
   * @param mimetypeService the mimetype service to use
   */
  public final void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Injects the Alfresco node service used to read/create nodes and manage aspects.
   *
   * @param nodeService the node service to use
   */
  public final void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Injects the Alfresco permission service used to grant configured permissions.
   *
   * @param permissionService the permission service to use
   */
  public final void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }
}
