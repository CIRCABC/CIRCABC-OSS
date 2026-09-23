package io.swagger.util;

import eu.europa.ec.digit.circabc.rest.template.SimplePath;
import io.swagger.api.CircabcApi;
import io.swagger.exception.PathNotFoundException;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.OccurenceRate;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;
import javax.imageio.ImageIO;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared utility helper for the CIRCABC REST API layer.
 *
 * <p>This Spring-managed bean groups together cross-cutting helpers that are reused by many
 * webscript endpoints and API implementations. Its responsibilities include:
 *
 * <ul>
 *   <li>Streaming uploaded content to disk with a size limit and validating image uploads
 *       ({@link #inputStreamToFile(InputStream, File, long)},
 *       {@link #checkAndGetImageFile(String, InputStream, long)}).</li>
 *   <li>Resolving Alfresco {@link NodeRef}s from string ids and reading common node properties
 *       (name, database id).</li>
 *   <li>Walking the repository hierarchy to locate the enclosing Category, Interest Group,
 *       Library root or Newsgroup root of a node, including archived nodes.</li>
 *   <li>Building XPATH and human-readable CIRCABC/category/interest-group/library paths for a
 *       node.</li>
 *   <li>Translating and formatting event occurrence rates into display strings using a static
 *       message map.</li>
 * </ul>
 *
 * <p>Alfresco services required by these helpers are injected via {@code @Autowired}.
 *
 * @author beaurpi
 */
public class ApiToolBox {

  /**
   * Prefix for the message keys that describe the time unit of an event occurrence (e.g. daily,
   * weekly). A concrete key is built by appending the lower-cased occurrence name to this prefix.
   */
  public static final String MSG_PREFIX_TIMES_OCCURENCE =
    "event_create_meetings_wizard_step1_occurs_";
  private static final String NOT_APPLICABLE =
    "event_view_meetings_details_dialog_notapplicable";
  private static final String OCCUR_ONCE =
    "event_view_meetings_details_dialog_occurence_once";
  private static final String OCCUR_TIME =
    "event_view_meetings_details_dialog_occurence_times";
  private static final String OCCUR_EVERY_TIME =
    "event_view_meetings_details_dialog_occurence_every";
  /**
   * Immutable lookup table mapping message-bundle keys to their English display text for
   * occurrence rates and time units. Populated once in the static initializer.
   */
  private static final Map<String, String> occurrenceMap;

  /** Shared commons-logging logger for this utility. */
  static final Log logger = LogFactory.getLog(ApiToolBox.class);

  static {
    Map<String, String> occMap = new HashMap<>();
    occMap.put(NOT_APPLICABLE, "Not Applicable");
    occMap.put(OCCUR_EVERY_TIME, "Occurs every {0} {1} for {2} times.");
    occMap.put(OCCUR_ONCE, "Only Once");
    occMap.put(OCCUR_TIME, "{0}. Occurs {1} times.");

    occMap.put("event_create_meetings_wizard_step1_occurs_daily", "Daily");
    occMap.put("event_create_meetings_wizard_step1_occurs_days", "Days");
    occMap.put("event_create_meetings_wizard_step1_occurs_every", "Every");
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_everytwoweeks",
      "Every Two Weeks"
    );
    occMap.put("event_create_meetings_wizard_step1_occurs_for", "for");
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_mondaytofriday",
      "Monday to Friday"
    );
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_mondaywednseyfriday",
      "Mon/Wed/Fri"
    );
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_monthlybydate",
      "Monthly by date"
    );
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_monthlybyweekday",
      "Monthly by weekday"
    );
    occMap.put("event_create_meetings_wizard_step1_occurs_months", "Months");
    occMap.put("event_create_meetings_wizard_step1_occurs_once", "Only Once");
    occMap.put("event_create_meetings_wizard_step1_occurs_times", "times");
    occMap.put(
      "event_create_meetings_wizard_step1_occurs_tuesdaythursday",
      "Tue/Thu"
    );
    occMap.put("event_create_meetings_wizard_step1_occurs_weekly", "Weekly");
    occMap.put("event_create_meetings_wizard_step1_occurs_weeks", "Weeks");
    occMap.put("event_create_meetings_wizard_step1_occurs_yearly", "Yearly");
    occMap.put("event_create_meetings_wizard_step1_occurs", "Occurs");
    occurrenceMap = Collections.unmodifiableMap(occMap);
  }

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private MultilingualContentService multilingualContentService;

  /**
   * Reads an input stream into a file with size limit enforcement
   *
   * @param inputStream the input stream to read from (must not be null)
   * @param file the target file to write to (must not be null)
   * @param limit the maximum number of bytes to read (must be positive)
   * @return the number of bytes written to the file
   * @throws IOException if an I/O error occurs during reading or writing
   * @throws IllegalArgumentException if the size limit is exceeded or parameters are invalid
   */
  public static long inputStreamToFile(
    InputStream inputStream,
    File file,
    long limit
  ) throws IOException, IllegalArgumentException {
    // Validate parameters
    if (inputStream == null) {
      throw new IllegalArgumentException("Input stream cannot be null");
    }
    if (file == null) {
      throw new IllegalArgumentException("Target file cannot be null");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("Size limit must be positive");
    }

    boolean success = false;

    try (OutputStream outStream = new FileOutputStream(file)) {
      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      long totalBytesRead = 0;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        totalBytesRead += bytesRead;

        if (totalBytesRead > limit) {
          // Size limit exceeded - throw exception and let finally block clean up
          throw new IllegalArgumentException(
            "Size limit of " + limit + " bytes exceeded"
          );
        }

        outStream.write(buffer, 0, bytesRead);
      }

      success = true;
      return totalBytesRead;
    } finally {
      // Clean up the partial file if operation wasn't successful
      if (!success && file.exists()) {
        try {
          java.nio.file.Files.delete(file.toPath());
        } catch (IOException deleteEx) {
          if (logger.isWarnEnabled()) {
            logger.warn(
              "Failed to delete partial file: " + file.getAbsolutePath()
            );
          }
        }
      }
    }
  }

  /**
   * Checks if the provided image stream is valid and returns a temp file with its
   * contents.
   *
   * <p>The stream is written to a temporary file (bounded by {@code size}) and then decoded with
   * {@link ImageIO} to verify it is a readable image. The supplied input stream is always closed
   * before returning.
   *
   * @param fileName the original file name, used only in error messages
   * @param inputStream the stream containing the image bytes
   * @param size the maximum number of bytes to read from the stream
   * @return a temporary {@link File} holding the validated image content
   * @throws SwaggerRuntimeException if the stream cannot be read or does not contain a valid image
   */
  public static File checkAndGetImageFile(
    String fileName,
    InputStream inputStream,
    long size
  ) {
    File tempFile;

    try {
      tempFile = TempFileProvider.createTempFile("attachment", ".tmp");
      ApiToolBox.inputStreamToFile(inputStream, tempFile, size);

      BufferedImage image = ImageIO.read(tempFile);
      if (image == null) {
        throw new IllegalArgumentException(
          "Not an image, or image corrupted: " + fileName
        );
      }

      return tempFile;
    } catch (Exception e) {
      throw new SwaggerRuntimeException("Exception reading image.", e);
    } finally {
      if (inputStream != null) {
        IOUtils.closeQuietly(inputStream);
      }
    }
  }

  /**
   * Generate a search XPATH pointing to the specified node, optionally return an
   * XPATH that
   * includes the child nodes.
   *
   * @param ref      Of the node to generate path too
   * @param children Whether to include children of the node
   * @return the path
   */
  public String getPathFromSpaceRef(NodeRef ref, boolean children) {
    Path path = nodeService.getPath(ref);

    StringBuilder buf = new StringBuilder(64);
    for (int i = 0; i < path.size(); i++) {
      String elementString = "";
      Path.Element element = path.get(i);
      if (element instanceof Path.ChildAssocElement childAssocElement) {
        ChildAssociationRef elementRef = childAssocElement.getRef();
        if (elementRef.getParentRef() != null) {
          Collection<String> prefixes = namespaceService.getPrefixes(
            elementRef.getQName().getNamespaceURI()
          );
          if (!prefixes.isEmpty()) {
            elementString =
              '/' +
              prefixes.iterator().next() +
              ':' +
              ISO9075.encode(elementRef.getQName().getLocalName());
          }
        }
      }

      buf.append(elementString);
    }
    if (children) {
      // append syntax to get all children of the path
      buf.append("//*");
    } else {
      // append syntax to just represent the path, not the children
      buf.append("/*");
    }

    return buf.toString();
  }

  /**
   * Resolves a {@link NodeRef} from its string id, looking first in the live workspace store and
   * then in the archive (trashcan) store.
   *
   * @param id the node identifier
   * @return the matching {@link NodeRef} if the node exists in the workspace or archive store,
   *     otherwise {@code null}
   */
  public NodeRef getNodeRef(String id) {
    NodeRef result = Converter.createNodeRefFromId(id);
    if (nodeService.exists(result)) {
      return result;
    }
    result = Converter.createArchiveNodeRefFromId(id);
    if (nodeService.exists(result)) {
      return result;
    } else {
      return null;
    }
  }

  /**
   * Returns the internal Alfresco database id of a node.
   *
   * @param nodeRef the node reference
   * @return the value of the {@code cm:node-dbid} property
   */
  public long getDatabaseID(final NodeRef nodeRef) {
    return (long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
  }

  /**
   * Returns the {@code cm:name} property of a node.
   *
   * @param nodeRef the node reference
   * @return the node name
   */
  public String getName(final NodeRef nodeRef) {
    return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
  }

  private NodeRef findAncestorWithAspect(NodeRef nodeRef, QName aspect) {
    while (
      nodeRef != null &&
      nodeRef
        .getStoreRef()
        .getProtocol()
        .equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol()) &&
      nodeService.exists(nodeRef)
    ) {
      if (nodeService.hasAspect(nodeRef, aspect)) {
        return nodeRef;
      }
      nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
    }
    return null;
  }

  /**
   * Finds the Interest Group root that contains the given node by walking up the primary parent
   * chain looking for the {@code circabc:igRoot} aspect.
   *
   * @param currentNodeRef the node to start the search from
   * @return the {@link NodeRef} of the enclosing Interest Group root, or {@code null} if none is
   *     found
   */
  public NodeRef getCurrentInterestGroup(final NodeRef currentNodeRef) {
    return findAncestorWithAspect(currentNodeRef, CircabcModel.ASPECT_IGROOT);
  }

  /**
   * Get all users that belong to a given group. If there are no users in the
   * group, an empty list
   * is returned.
   *
   * @param groupName Name of the group
   * @return the user names of the {@code cm:person} nodes directly contained in the group, or an
   *     empty list if the group has no users or does not resolve to a node (e.g. GROUP_EVERYONE)
   */
  public List<String> getUsersFromGroup(String groupName) {
    List<String> users = new ArrayList<>();

    NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(groupName);

    if (groupNodeRef == null) {
      // comes here for GROUP_EVERYONE
      return users;
    }

    List<ChildAssociationRef> children = nodeService.getChildAssocs(
      groupNodeRef
    );

    for (ChildAssociationRef child : children) {
      if (
        ContentModel.TYPE_PERSON.equals(
          nodeService.getType(child.getChildRef())
        )
      ) {
        String userName = (String) nodeService.getProperty(
          child.getChildRef(),
          ContentModel.PROP_USERNAME
        );

        users.add(userName);
      }
    }

    return users;
  }

  /**
   * Builds the human-readable CIRCABC path (relative to the CIRCABC root) for a node.
   *
   * @param nodeRef the node reference
   * @param includeFirstSlash whether the returned path should start with a leading slash
   * @return the CIRCABC path of the node
   */
  public String getCircabcPath(NodeRef nodeRef, boolean includeFirstSlash) {
    Path path = this.nodeService.getPath(nodeRef);
    return PathUtils.getCircabcPath(path, includeFirstSlash);
  }

  /**
   * Builds the path of a node relative to its enclosing Category.
   *
   * @param nodeRef the node reference
   * @param includeFirstSlash whether the returned path should start with a leading slash
   * @return the category-relative path of the node
   */
  public String getCategoryPath(NodeRef nodeRef, boolean includeFirstSlash) {
    Path path = this.nodeService.getPath(nodeRef);
    return PathUtils.getCategoryPath(path, includeFirstSlash);
  }

  /**
   * Builds the path of a node relative to its enclosing Interest Group.
   *
   * @param nodeRef the node reference
   * @param includeFirstSlash whether the returned path should start with a leading slash
   * @return the interest-group-relative path of the node
   */
  public String getInterestGroupPath(
    NodeRef nodeRef,
    boolean includeFirstSlash
  ) {
    Path path = this.nodeService.getPath(nodeRef);
    return PathUtils.getInterestGroupPath(path, includeFirstSlash);
  }

  /**
   * Builds the path of a node relative to its enclosing Library.
   *
   * @param nodeRef the node reference
   * @param includeFirstSlash whether the returned path should start with a leading slash
   * @return the library-relative path of the node
   */
  public String getLibraryPath(NodeRef nodeRef, boolean includeFirstSlash) {
    Path path = this.nodeService.getPath(nodeRef);
    return PathUtils.getLibraryPath(path, includeFirstSlash);
  }

  /**
   * Resolves the Interest Group of an archived (trashcan) node.
   *
   * <p>It first tries the {@code circabc:igRootNodeIdArchived} property recorded at archive time;
   * if that is absent it falls back to the original parent association and searches for the
   * enclosing Interest Group of that parent.
   *
   * @param nodeRef the archived node reference
   * @return the {@link NodeRef} of the Interest Group the node belonged to, or {@code null} if it
   *     cannot be determined
   */
  public NodeRef getInterestGroupForArchivedNode(NodeRef nodeRef) {
    String id = (String) nodeService.getProperty(
      nodeRef,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED
    );
    if (id != null) {
      return getNodeRef(id);
    } else {
      ChildAssociationRef childRef =
        (ChildAssociationRef) nodeService.getProperty(
          nodeRef,
          ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC
        );
      if (this.nodeService.exists(childRef.getParentRef())) {
        return getCurrentInterestGroup(childRef.getParentRef());
      } else {
        return null;
      }
    }
  }

  /**
   * Builds the CIRCABC path of an archived node based on its original parent association.
   *
   * @param nodeRef the archived node reference
   * @param includeFirstSlash whether the returned path should start with a leading slash
   * @return the CIRCABC path of the node's original parent, or {@code null} if the original parent
   *     no longer exists
   */
  public String getCircabcPathForArchivedNode(
    NodeRef nodeRef,
    boolean includeFirstSlash
  ) {
    ChildAssociationRef childRef =
      (ChildAssociationRef) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC
      );
    if (this.nodeService.exists(childRef.getParentRef())) {
      Path path = nodeService.getPath(childRef.getParentRef());
      return PathUtils.getCircabcPath(path, includeFirstSlash);
    } else {
      return null;
    }
  }

  /**
   * Finds the Library root that contains the given node by walking up the primary parent chain
   * looking for the {@code circabc:libraryRoot} aspect.
   *
   * @param nodeRef the node to start the search from
   * @return the {@link NodeRef} of the enclosing Library root, or {@code null} if none is found
   */
  public NodeRef getCurrentLibraryRoot(NodeRef nodeRef) {
    return findAncestorWithAspect(nodeRef, CircabcModel.ASPECT_LIBRARY_ROOT);
  }

  /**
   * Finds the Newsgroup root that contains the given node by walking up the primary parent chain
   * looking for the {@code circabc:newsgroupRoot} aspect.
   *
   * @param nodeRef the node to start the search from
   * @return the {@link NodeRef} of the enclosing Newsgroup root, or {@code null} if none is found
   */
  public NodeRef getCurrentNewsgroupRoot(NodeRef nodeRef) {
    return findAncestorWithAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT);
  }

  /**
   * Converts a serialized occurrence-rate string into a human-readable description.
   *
   * @param occurenceRateString the encoded occurrence rate; may be {@code null} or empty
   * @return a display string describing how often the event occurs, or the "not applicable" text
   *     when the input is {@code null} or empty
   */
  public String getOccurenceAsString(String occurenceRateString) {
    if (occurenceRateString == null || occurenceRateString.isEmpty()) {
      return occurrenceMap.get(NOT_APPLICABLE);
    }

    String occurStr = null;
    final OccurenceRate occurenceRate = new OccurenceRate(occurenceRateString);

    switch (occurenceRate.getMainOccurence()) {
      case OnlyOnce:
        occurStr = translate(OCCUR_ONCE);
        break;
      case Times:
        occurStr = translate(
          OCCUR_TIME,
          getDisplayTimesOccurence(occurenceRate),
          occurenceRate.getTimes()
        );
        break;
      case EveryTimes:
        occurStr = translate(
          OCCUR_EVERY_TIME,
          occurenceRate.getEvery(),
          getDisplayEveryTimesOccurence(occurenceRate),
          occurenceRate.getTimes()
        );
        break;
    }

    return occurStr;
  }

  /**
   * Formats a display message identified by a key, substituting the given parameters using
   * {@link MessageFormat}.
   *
   * @param key the message key to look up in the occurrence map
   * @param params the values to substitute into the message pattern
   * @return the formatted message
   */
  protected String translate(final String key, final Object... params) {
    MessageFormat form = new MessageFormat(occurrenceMap.get(key));
    return form.format(params);
  }

  /**
   * Returns the display label for the time unit of the "times" occurrence of an event.
   *
   * @param occurenceRate the occurrence rate
   * @return the localized display label for the time unit (e.g. "Days", "Weeks")
   */
  public String getDisplayTimesOccurence(OccurenceRate occurenceRate) {
    return occurrenceMap.get(
      MSG_PREFIX_TIMES_OCCURENCE +
        occurenceRate.getTimesOccurence().name().toLowerCase()
    );
  }

  /**
   * Returns the display label for the time unit of the "every times" occurrence of an event.
   *
   * @param occurenceRate the occurrence rate
   * @return the localized display label for the time unit (e.g. "Days", "Weeks")
   */
  public String getDisplayEveryTimesOccurence(OccurenceRate occurenceRate) {
    return occurrenceMap.get(
      MSG_PREFIX_TIMES_OCCURENCE +
        occurenceRate.getEveryTimesOccurence().name().toLowerCase()
    );
  }

  /**
   * Finds the Category that contains the given node by walking up the primary parent chain looking
   * for the {@code circabc:category} aspect.
   *
   * @param currentNodeRef the node to start the search from; must not be {@code null}
   * @return the {@link NodeRef} of the enclosing Category, or {@code null} if none is found
   * @throws NullPointerException if {@code currentNodeRef} is {@code null}
   */
  public NodeRef getCurrentCategory(NodeRef currentNodeRef) {
    if (currentNodeRef == null) {
      throw new NullPointerException("NodeRef is a mandatory parameter.");
    }

    return findAncestorWithAspect(currentNodeRef, CircabcModel.ASPECT_CATEGORY);
  }

  /**
   * Computes the {@link SimplePath} representation of a node, applying special handling depending
   * on the node type:
   *
   * <ul>
   *   <li>Category headers are rooted at the CIRCABC node.</li>
   *   <li>Multilingual containers are resolved via their pivot translation's parent folder.</li>
   *   <li>Events are resolved relative to their enclosing calendar service.</li>
   *   <li>All other nodes use their natural path.</li>
   * </ul>
   *
   * @param nodeRef the node reference
   * @return the {@link SimplePath} for the node
   * @throws PathNotFoundException if the path cannot be resolved
   */
  public SimplePath getNodePath(final NodeRef nodeRef)
    throws PathNotFoundException {
    final QName type = nodeService.getType(nodeRef);

    if (type.equals(CircabcModel.TYPE_CATEGORY_HEADER)) {
      return new SimplePath(nodeService, circabcApi.getCircabcNodeRef());
    } else if (type.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
      final NodeRef pivotTranslation =
        multilingualContentService.getPivotTranslation(nodeRef);
      final ChildAssociationRef pivotFolder = nodeService.getPrimaryParent(
        pivotTranslation
      );
      return new SimplePath(nodeService, pivotFolder.getParentRef(), nodeRef);
    } else if (EventModel.TYPE_EVENT.equals(type)) {
      final ChildAssociationRef parent = nodeService.getPrimaryParent(nodeRef);
      final ChildAssociationRef parentparent = nodeService.getPrimaryParent(
        parent.getParentRef()
      );
      final ChildAssociationRef calendarService = nodeService.getPrimaryParent(
        parentparent.getParentRef()
      );
      return new SimplePath(
        nodeService,
        calendarService.getParentRef(),
        nodeRef
      );
    } else {
      return new SimplePath(nodeService, nodeRef);
    }
  }

  /**
   * Returns the {@link SimplePath} of each direct child of a node.
   *
   * @param nodeRef the parent node reference
   * @return a list of {@link SimplePath} objects, one per direct child association; empty if the
   *     node has no children
   * @throws PathNotFoundException if a child path cannot be resolved
   */
  public List<SimplePath> getChildsPath(final NodeRef nodeRef)
    throws PathNotFoundException {
    final List<SimplePath> list = new ArrayList<>();
    final List<ChildAssociationRef> childs = nodeService.getChildAssocs(
      nodeRef
    );
    for (final ChildAssociationRef child : childs) {
      list.add(new SimplePath(nodeService, child.getChildRef()));
    }
    return list;
  }
}
