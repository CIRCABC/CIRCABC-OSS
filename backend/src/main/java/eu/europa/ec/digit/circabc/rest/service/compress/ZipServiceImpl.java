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
package eu.europa.ec.digit.circabc.rest.service.compress;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import de.schlichtherle.util.zip.ZipOutputStream;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessageImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import java.io.*;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Default {@link ZipService} implementation providing ZIP compression and decompression
 * services on top of the Alfresco repository.
 *
 * <p>This service supports the bulk-upload / bulk-download features of CIRCABC by:
 *
 * <ul>
 *   <li>Compressing repository nodes (documents and folders, recursively) or plain files into a
 *       ZIP archive, optionally including an {@code index.txt} manifest.
 *   <li>Extracting a single named entry from an archive to a file on disk.
 *   <li>Extracting a whole archive back into the repository, recreating the folder hierarchy and
 *       creating content nodes under a given destination, driven by an optional list of
 *       {@link IndexRecord} entries.
 *   <li>Listing the entries contained in an archive.
 * </ul>
 *
 * <p>The implementation relies on the TrueZIP ({@code de.schlichtherle.util.zip}) library which
 * offers better handling of non-ASCII entry names than the JDK ZIP classes. When compressing
 * repository nodes, only nodes whose security ranking is public or normal are included; document
 * paths are computed relative to (or absolute from) the current library root. Repository access is
 * performed through the injected Alfresco services, and errors are logged and, where applicable,
 * reported through the supplied {@link ValidationMessage} list rather than propagated as
 * exceptions.
 */
public class ZipServiceImpl implements ZipService {

  /** Log prefix used for generic (non-specific) exception messages. */
  private static final String EXCEPTION = "Exception: ";
  private static final String FILE_ALLREADY_EXIST = "file_allready_exist";
  private static final String SLASH = "/";
  private static final String INDEX_TXT = "index.txt";
  private static final String OVERRIDE_THE_EXISTING_FILE =
    "Override the existing file:";

  private static final String ERROR_WHEN_CLOSING_COMPRESSED_FILE =
    "Error when closing compressed file:";
  private static final String ERROR_WHEN_UNZIPPING_COMPRESSED_FILE =
    "Error when unzipping compressed file:";
  private static final String ERROR_WHEN_UNCOMPRESSING_BULK_UPLOAD_FILE =
    "Error when uncompressing bulk upload file: ";
  private static final String ERROR_WHEN_EXTRACTING_BULK_UPLOAD_FILE =
    "Error when extracting bulk upload file: ";
  private static final Log logger = LogFactory.getLog(ZipServiceImpl.class);

  /** Size (in bytes) of the transfer buffer used when unzipping a single entry to disk. */
  private static final int BUFFER_SIZE = 32 * 1024;

  /**
   * Cache of resolved Alfresco type definitions, keyed by type {@link QName}, used to avoid
   * repeated dictionary look-ups while walking a node hierarchy. A {@code null} value indicates a
   * type that could not be resolved.
   */
  private final Map<QName, TypeDefinition> validTypeMap = new HashMap<>();

  @Autowired
  private NodeService nodeService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private DictionaryService dictionaryService;

  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Extracts a single named entry from a ZIP archive and writes its content to the given output
   * file.
   *
   * @param compressedFile the ZIP archive to read from
   * @param fileName the exact name of the entry to extract
   * @param outputFile the file on disk to which the entry content is written
   * @param messages collector to which a fatal {@link ValidationMessage} is added if the archive
   *     cannot be read
   * @return {@code true} if the entry was found and successfully extracted, {@code false} otherwise
   */
  @SuppressWarnings("java:S2093") // ZipFile doesn't implement AutoCloseable
  public boolean extract(
    final File compressedFile,
    final String fileName,
    final File outputFile,
    final List<ValidationMessage> messages
  ) {
    boolean succeed = false;
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(compressedFile);
      ZipEntry zipEntry;
      @SuppressWarnings("unchecked")
      final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
      while (zipEntries.hasMoreElements()) {
        zipEntry = zipEntries.nextElement();
        if (zipEntry.getName().equals(fileName)) {
          unzip(outputFile, zipFile, zipEntry);
          succeed = true;
          break;
        }
      }
    } catch (final IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error(
          ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(),
          ioe
        );
      }
      messages.add(
        new ValidationMessageImpl(
          0,
          compressedFile.getName(),
          ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + ioe.getMessage(),
          ErrorType.Fatal
        )
      );
    } finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (final IOException ioe) {
          if (logger.isErrorEnabled()) {
            logger.error(
              ERROR_WHEN_CLOSING_COMPRESSED_FILE + compressedFile.getName(),
              ioe
            );
          }
        }
      }
    }
    return succeed;
  }

  /**
   * Lists the entries contained in a ZIP archive.
   *
   * @param compressedFile the ZIP archive to inspect
   * @param messages collector to which a fatal {@link ValidationMessage} is added if the archive
   *     cannot be read or its entries cannot be extracted
   * @return the list of {@link CompressedEntry} descriptors for the archive; empty if the archive
   *     could not be read
   */
  @SuppressWarnings("java:S2093") // ZipFile doesn't implement AutoCloseable
  public List<CompressedEntry> getCompressedEntries(
    final File compressedFile,
    final List<ValidationMessage> messages
  ) {
    final List<CompressedEntry> content = new LinkedList<>();
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(compressedFile);
      CompressedEntry compressedEntry;
      ZipEntry zipEntry;
      @SuppressWarnings("unchecked")
      final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

      while (zipEntries.hasMoreElements()) {
        zipEntry = zipEntries.nextElement();
        compressedEntry = new CompressedEntryImpl(zipEntry);
        content.add(compressedEntry);
      }
    } catch (final IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error(
          ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(),
          ioe
        );
      }
      messages.add(
        new ValidationMessageImpl(
          0,
          compressedFile.getName(),
          ERROR_WHEN_UNCOMPRESSING_BULK_UPLOAD_FILE + ioe.getMessage(),
          ErrorType.Fatal
        )
      );
    } catch (final Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(),
          e
        );
      }
      messages.add(
        new ValidationMessageImpl(
          0,
          compressedFile.getName(),
          ERROR_WHEN_EXTRACTING_BULK_UPLOAD_FILE + e.getMessage(),
          ErrorType.Fatal
        )
      );
    } finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (final IOException ioe) {
          if (logger.isErrorEnabled()) {
            logger.error(
              ERROR_WHEN_CLOSING_COMPRESSED_FILE + compressedFile.getName(),
              ioe
            );
          }
        }
      }
    }
    return content;
  }

  /**
   * Extracts a ZIP archive into the repository, recreating its folder hierarchy and content nodes.
   *
   * <p>Convenience overload that extracts every entry (no exclusions).
   *
   * @param libraryNodeRef the library root node used to resolve entries whose path starts with the
   *     library name
   * @param destinationNodeRef the node under which entries not belonging to the library are created
   * @param compressedFile the ZIP archive to extract
   * @param indexRecords records describing how existing files should be handled (e.g. overwrite)
   * @param messages collector to which validation messages (such as "file already exists") are
   *     added
   * @return a map of archive entry path to the created (or resolved) repository {@link NodeRef}
   */
  public Map<String, NodeRef> extract(
    final NodeRef libraryNodeRef,
    final NodeRef destinationNodeRef,
    final File compressedFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  ) {
    return extract(
      libraryNodeRef,
      destinationNodeRef,
      compressedFile,
      Collections.<String>emptyList(),
      indexRecords,
      messages
    );
  }

  /**
   * Compresses a single repository node (and, if it is a folder, its descendants) into a ZIP
   * archive.
   *
   * @param nodeRef the repository node to add to the archive
   * @param compressedFile the target ZIP file to create
   */
  public void addingFileIntoArchive(
    final NodeRef nodeRef,
    final File compressedFile
  ) {
    final List<NodeRef> nodeRefs = new ArrayList<>();
    nodeRefs.add(nodeRef);
    addingFileIntoArchive(nodeRefs, compressedFile);
  }

  /**
   * Compresses a list of repository nodes (and their descendants) into a ZIP archive.
   *
   * @param nodeRefs the repository nodes to add to the archive
   * @param compressedFile the target ZIP file to create
   */
  public void addingFileIntoArchive(
    final List<NodeRef> nodeRefs,
    final File compressedFile
  ) {
    addingFileIntoArchive(nodeRefs, compressedFile, null);
  }

  /**
   * Compresses a list of repository nodes (and their descendants) into a ZIP archive, optionally
   * appending an {@code index.txt} manifest file.
   *
   * <p>Each node is expanded into its full set of readable content and folder descendants; only
   * nodes with an allowed security ranking are included. Entry names are the node paths relative to
   * the current library root. I/O errors are logged and swallowed rather than propagated.
   *
   * @param nodeRefs the repository nodes to add to the archive
   * @param compressedFile the target ZIP file to create
   * @param indexFile an index file to embed as {@code index.txt}, or {@code null} to omit it
   */
  public void addingFileIntoArchive(
    final List<NodeRef> nodeRefs,
    final File compressedFile,
    final File indexFile
  ) {
    final Date now = new Date();
    final byte[] buf = new byte[1024];
    final List<NodeRef> allNodeRefs = new ArrayList<>();
    for (final NodeRef nodeRef : nodeRefs) {
      allNodeRefs.addAll(getAllChildsNodeRefs(nodeRef));
    }

    try (
      ZipOutputStream zipOutputStream = new ZipOutputStream(
        new FileOutputStream(compressedFile)
      )
    ) {
      zipOutputStream.setMethod(ZipEntry.DEFLATED);
      for (final NodeRef nodeRef : allNodeRefs) {
        addNodeToArchive(nodeRef, zipOutputStream, buf, now);
      }
      if (indexFile != null) {
        addIndexFileToArchive(indexFile, zipOutputStream, buf, now);
      }
      zipOutputStream.finish();
    } catch (final IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error(EXCEPTION + e.getMessage(), e);
      }
    }
  }

  private void addNodeToArchive(
    NodeRef nodeRef,
    ZipOutputStream zipOutputStream,
    byte[] buf,
    Date now
  ) {
    String securityRanking = (String) nodeService.getProperty(
      nodeRef,
      DocumentModel.PROP_SECURITY_RANKING
    );
    if (!isAllowedSecurityRanking(securityRanking)) {
      return;
    }
    QName type = nodeService.getType(nodeRef);
    boolean isContent =
      ContentModel.TYPE_CONTENT.equals(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
    ContentReader contentReader = isContent
      ? contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
      : null;
    String filename = getRelativeLibraryPath(nodeRef);

    try {
      ZipEntry zipentry = createZipEntry(filename, contentReader, now);
      zipOutputStream.putNextEntry(zipentry);
      if (contentReader != null) {
        writeContentToZip(contentReader, zipOutputStream, buf);
      }
      zipOutputStream.flush();
      zipOutputStream.closeEntry();
    } catch (final IOException io) {
      if (logger.isErrorEnabled()) {
        logger.error(EXCEPTION + io.getMessage(), io);
      }
    }
  }

  private boolean isAllowedSecurityRanking(String securityRanking) {
    return (
      securityRanking == null ||
      DocumentModel.SECURITY_RANKINGS_PUBLIC.equalsIgnoreCase(
        securityRanking
      ) ||
      DocumentModel.SECURITY_RANKINGS_NORMAL.equalsIgnoreCase(securityRanking)
    );
  }

  private ZipEntry createZipEntry(
    String filename,
    ContentReader contentReader,
    Date now
  ) {
    String entryName = filename;
    if (contentReader != null) {
      if (entryName.startsWith("/")) {
        entryName = entryName.substring(1);
      }
    } else {
      if (!entryName.endsWith(SLASH)) {
        entryName = entryName + SLASH;
      }
    }
    ZipEntry zipentry = new ZipEntry(entryName);
    if (contentReader != null) {
      zipentry.setSize(contentReader.getContentData().getSize());
    }
    zipentry.setMethod(ZipEntry.DEFLATED);
    zipentry.setTime(now.getTime());
    return zipentry;
  }

  private void writeContentToZip(
    ContentReader contentReader,
    ZipOutputStream zipOutputStream,
    byte[] buf
  ) throws IOException {
    try (InputStream inputStream = contentReader.getContentInputStream()) {
      if (inputStream == null) {
        throw new IOException("Input stream is null");
      }
      int len;
      while ((len = inputStream.read(buf)) > 0) {
        zipOutputStream.write(buf, 0, len);
      }
    }
  }

  private void addIndexFileToArchive(
    File indexFile,
    ZipOutputStream zipOutputStream,
    byte[] buf,
    Date now
  ) {
    try (FileInputStream fileStream = new FileInputStream(indexFile)) {
      ZipEntry zipentry = new ZipEntry(INDEX_TXT);
      zipentry.setMethod(ZipEntry.DEFLATED);
      zipentry.setTime(now.getTime());
      zipOutputStream.putNextEntry(zipentry);
      int len;
      while ((len = fileStream.read(buf)) > 0) {
        zipOutputStream.write(buf, 0, len);
      }
      zipOutputStream.flush();
      zipOutputStream.closeEntry();
    } catch (final IOException io) {
      if (logger.isErrorEnabled()) {
        logger.error(EXCEPTION + io.getMessage(), io);
      }
    }
  }

  /**
   * Computes the path of a node relative to its current library root, using the display NAME of
   * each path element and {@code '/'} as the separator.
   *
   * @param nodeRef the node whose relative library path is required
   * @return the relative path (not prefixed with the library name)
   */
  public String getRelativeLibraryPath(final NodeRef nodeRef) {
    final Path path = nodeService.getPath(nodeRef);
    final NodeRef libraryNodeRef = apiToolBox.getCurrentLibraryRoot(nodeRef);
    return getNamePath(path, libraryNodeRef, SLASH, false);
  }

  /**
   * Computes the absolute path of a node within its current library, using the display NAME of each
   * path element and {@code '/'} as the separator.
   *
   * @param nodeRef the node whose absolute library path is required
   * @return the absolute path, prefixed with the library name
   */
  public String getAbsoluteLibraryPath(final NodeRef nodeRef) {
    final Path path = nodeService.getPath(nodeRef);
    final NodeRef libraryNodeRef = apiToolBox.getCurrentLibraryRoot(nodeRef);
    return getNamePath(path, libraryNodeRef, SLASH, true);
  }

  /**
   * Resolve a Path by converting each element into its display NAME attribute
   *
   * @param path         Path to convert
   * @param rootNode     The Node that is considered to be the first node for calculating the path
   * @param separator    Separator for folders
   * @param absolutePath Boolean that indicate if the node should start with "/library"
   * @return Path converted using NAME attribute on each element
   */
  private String getNamePath(
    final Path path,
    final NodeRef rootNode,
    final String separator,
    final boolean absolutePath
  ) {
    final StringBuilder buf = new StringBuilder(128);
    boolean foundRoot = (rootNode == null);

    for (int i = 1; i < path.size(); i++) {
      Path.Element element = path.get(i);
      PathElementResult result = processPathElement(
        element,
        foundRoot,
        rootNode
      );
      foundRoot = result.foundRoot;

      if (result.elementString != null) {
        buf.append(separator).append(result.elementString);
      }
    }

    return buildFinalPath(rootNode, separator, absolutePath, buf);
  }

  private PathElementResult processPathElement(
    Path.Element element,
    boolean foundRoot,
    NodeRef rootNode
  ) {
    String elementString = null;
    boolean newFoundRoot = foundRoot;

    if (element instanceof Path.ChildAssocElement childAssocElement) {
      ChildAssociationRef elementRef = childAssocElement.getRef();
      if (elementRef.getParentRef() != null) {
        if (foundRoot) {
          elementString = getElementName(elementRef.getChildRef(), element);
        }
        newFoundRoot = foundRoot || elementRef.getChildRef().equals(rootNode);
      }
    } else {
      elementString = element.getElementString();
    }

    return new PathElementResult(elementString, newFoundRoot);
  }

  private String getElementName(NodeRef childRef, Path.Element element) {
    Object nameProp = nodeService.getProperty(childRef, ContentModel.PROP_NAME);
    return nameProp != null ? nameProp.toString() : element.getElementString();
  }

  private String buildFinalPath(
    NodeRef rootNode,
    String separator,
    boolean absolutePath,
    StringBuilder buf
  ) {
    if (!absolutePath) {
      return buf.toString();
    }
    Object nameProp = nodeService.getProperty(rootNode, ContentModel.PROP_NAME);
    if (nameProp != null && !nameProp.toString().isEmpty()) {
      return separator + nameProp.toString() + buf.toString();
    }
    return "";
  }

  private static class PathElementResult {

    final String elementString;
    final boolean foundRoot;

    PathElementResult(String elementString, boolean foundRoot) {
      this.elementString = elementString;
      this.foundRoot = foundRoot;
    }
  }

  private Collection<NodeRef> getAllChildsNodeRefs(final NodeRef nodeRef) {
    final List<NodeRef> allNodeRefs = new ArrayList<>();

    if (!canReadNode(nodeRef)) {
      return allNodeRefs;
    }

    final QName type = nodeService.getType(nodeRef);
    TypeDefinition typeDef = getOrCacheTypeDef(type);

    if (typeDef == null) {
      logInvalidObject(nodeRef, type);
      return allNodeRefs;
    }

    processNodeByType(nodeRef, type, allNodeRefs);
    return allNodeRefs;
  }

  private boolean canReadNode(NodeRef nodeRef) {
    return (
      nodeService.exists(nodeRef) &&
      AccessStatus.ALLOWED.equals(
        permissionService.hasPermission(nodeRef, PermissionService.READ)
      )
    );
  }

  private TypeDefinition getOrCacheTypeDef(QName type) {
    if (validTypeMap.containsKey(type)) {
      return validTypeMap.get(type);
    }
    TypeDefinition typeDef = dictionaryService.getType(type);
    validTypeMap.put(type, typeDef);
    return typeDef;
  }

  private void processNodeByType(
    NodeRef nodeRef,
    QName type,
    List<NodeRef> allNodeRefs
  ) {
    if (isContentType(type)) {
      allNodeRefs.add(nodeRef);
    } else if (isFolderType(type)) {
      createFolderRepresentation(nodeRef, allNodeRefs);
    }
  }

  private boolean isContentType(QName type) {
    return (
      ContentModel.TYPE_CONTENT.equals(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)
    );
  }

  private boolean isFolderType(QName type) {
    return (
      ContentModel.TYPE_FOLDER.equals(type) ||
      (dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) &&
        !dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER))
    );
  }

  private void logInvalidObject(NodeRef nodeRef, QName type) {
    if (logger.isWarnEnabled()) {
      logger.warn(
        "Found invalid object in database: id = " + nodeRef + ", type = " + type
      );
    }
  }

  private void createFolderRepresentation(
    final NodeRef nodeRef,
    final List<NodeRef> allNodeRefs
  ) {
    final List<ChildAssociationRef> listChildAssocs =
      nodeService.getChildAssocs(nodeRef);
    allNodeRefs.add(nodeRef);
    for (final ChildAssociationRef childAssoc : listChildAssocs) {
      allNodeRefs.addAll(getAllChildsNodeRefs(childAssoc.getChildRef()));
    }
  }

  /**
   * Compresses a single file (or directory) from disk into a ZIP archive.
   *
   * @param newFile the file or directory to add to the archive
   * @param compressedFile the target ZIP file to create
   */
  public void addingFileIntoArchive(
    final File newFile,
    final File compressedFile
  ) {
    final List<File> newFiles = new ArrayList<>();
    newFiles.add(newFile);
    addingFilesIntoArchive(newFiles, compressedFile);
  }

  /**
   * Compresses a list of files (or directories) from disk into a ZIP archive.
   *
   * @param newFiles the files or directories to add to the archive
   * @param compressedFile the target ZIP file to create
   */
  public void addingFilesIntoArchive(
    final List<File> newFiles,
    final File compressedFile
  ) {
    addingFilesIntoArchive(newFiles, compressedFile, null);
  }

  /**
   * Compresses a list of files (or directories) from disk into a ZIP archive, optionally appending
   * an additional index file.
   *
   * <p>I/O errors are logged and swallowed rather than propagated.
   *
   * @param newFiles the files or directories to add to the archive
   * @param compressedFile the target ZIP file to create
   * @param indexFile an additional file to append to the archive, or {@code null} to omit it
   */
  public void addingFilesIntoArchive(
    final List<File> newFiles,
    final File compressedFile,
    final File indexFile
  ) {
    final Date now = new Date();
    final byte[] buf = new byte[1024];
    try (
      ZipOutputStream zipOutputStream = new ZipOutputStream(
        new FileOutputStream(compressedFile)
      )
    ) {
      for (final File newFile : newFiles) {
        addFileToArchive(newFile, zipOutputStream, buf, now);
      }
      if (indexFile != null) {
        addFileToArchive(indexFile, zipOutputStream, buf, now);
      }
      zipOutputStream.finish();
    } catch (final IOException io) {
      if (logger.isErrorEnabled()) {
        logger.error(EXCEPTION + io.getMessage(), io);
      }
    }
  }

  private void addFileToArchive(
    File file,
    ZipOutputStream zipOutputStream,
    byte[] buf,
    Date now
  ) {
    String entryPath = file.getPath();
    boolean isDirectory = file.isDirectory();
    if (isDirectory && !file.getName().endsWith(SLASH)) {
      entryPath = entryPath + SLASH;
    }
    ZipEntry zipentry = new ZipEntry(entryPath);
    if (!isDirectory) {
      zipentry.setSize(file.length());
    }
    zipentry.setMethod(ZipEntry.DEFLATED);
    zipentry.setTime(now.getTime());

    try {
      zipOutputStream.putNextEntry(zipentry);
      if (!isDirectory) {
        try (FileInputStream fileStream = new FileInputStream(file)) {
          int len;
          while ((len = fileStream.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, len);
          }
        }
      }
      zipOutputStream.flush();
      zipOutputStream.closeEntry();
    } catch (final IOException io) {
      if (logger.isErrorEnabled()) {
        logger.error(EXCEPTION + io.getMessage(), io);
      }
    }
  }

  private boolean isFolderAlreadyExtracted(
    final Set<String> extractedNodesKeys,
    final String filePath
  ) {
    return extractedNodesKeys
      .stream()
      .anyMatch(key -> key.startsWith(filePath));
  }

  /**
   * Extracts a ZIP archive into the repository, recreating its folder hierarchy and content nodes
   * while skipping a given set of entry names.
   *
   * <p>For each archive entry the path is normalized, directories already covered by an extracted
   * entry and explicitly excluded names are skipped, and remaining entries are unzipped either into
   * the library root (when their path starts with the library name) or under the destination node.
   * Existing files are handled according to the supplied {@link IndexRecord} overwrite flags; when
   * a file already exists and is not marked for overwrite, a "file already exists" validation
   * message is recorded. Any thrown exception is logged and the extraction of the current archive
   * stops.
   *
   * @param libraryNodeRef the library root node used to resolve entries whose path starts with the
   *     library name
   * @param destinationNodeRef the node under which entries not belonging to the library are created
   * @param compressedFile the ZIP archive to extract
   * @param excludedFileName the list of normalized entry paths to skip
   * @param indexRecords records describing how existing files should be handled (e.g. overwrite)
   * @param messages collector to which validation messages (such as "file already exists") are
   *     added
   * @return a map of archive entry path to the created (or resolved) repository {@link NodeRef}
   */
  public Map<String, NodeRef> extract(
    final NodeRef libraryNodeRef,
    final NodeRef destinationNodeRef,
    final File compressedFile,
    final List<String> excludedFileName,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  ) {
    final Map<String, NodeRef> extractedNodes = new HashMap<>();
    final String library = (String) nodeService.getProperty(
      libraryNodeRef,
      ContentModel.PROP_NAME
    );
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(compressedFile);
      @SuppressWarnings("unchecked")
      Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
      while (zipEntries.hasMoreElements()) {
        ZipEntry zipEntry = zipEntries.nextElement();
        String filePath = normalizeFilePath(zipEntry.getName());
        if (
          shouldSkipEntry(
            zipEntry,
            filePath,
            extractedNodes.keySet(),
            excludedFileName
          )
        ) {
          continue;
        }
        NodeRef createdNodeRef = unzip(
          destinationNodeRef,
          zipFile,
          zipEntry,
          indexRecords,
          library,
          libraryNodeRef
        );
        if (createdNodeRef != null) {
          extractedNodes.put(filePath, createdNodeRef);
        } else {
          messages.add(
            new ValidationMessageImpl(
              0,
              filePath,
              I18NUtil.getMessage(FILE_ALLREADY_EXIST),
              ErrorType.Fatal
            )
          );
        }
      }
    } catch (final Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(),
          e
        );
      }
    } finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (Exception e) {
          // NOSONAR: Intentionally ignoring close exception as we're in finally block
          // and the main operation has already completed or failed
          if (logger.isDebugEnabled()) {
            logger.debug("Error closing zip file", e);
          }
        }
      }
    }
    return extractedNodes;
  }

  private String normalizeFilePath(String filePath) {
    String normalized =
      File.separatorChar == '/'
        ? filePath.replace('\\', File.separatorChar)
        : filePath.replace('/', File.separatorChar);
    return normalized.startsWith(File.separator)
      ? normalized
      : File.separatorChar + normalized;
  }

  private boolean shouldSkipEntry(
    ZipEntry zipEntry,
    String filePath,
    Set<String> extractedKeys,
    List<String> excludedFileName
  ) {
    if (
      zipEntry.isDirectory() &&
      isFolderAlreadyExtracted(extractedKeys, filePath)
    ) {
      return true;
    }
    return excludedFileName.contains(filePath);
  }

  private void unzip(
    final File outputFile,
    final ZipFile zipFile,
    final ZipEntry zipEntry
  ) {
    byte[] buffer = new byte[BUFFER_SIZE];
    try (
      BufferedInputStream in = new BufferedInputStream(
        zipFile.getInputStream(zipEntry)
      );
      BufferedOutputStream out = new BufferedOutputStream(
        new FileOutputStream(outputFile)
      )
    ) {
      int nbRead;
      while ((nbRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, nbRead);
      }
    } catch (IOException ioex) {
      logger.error("Error when tring to unzip ", ioex);
    }
  }

  private NodeRef createFolder(
    final NodeRef destinationNodeRef,
    final List<String> folderElementsList
  ) {
    @SuppressWarnings("deprecation")
    final FileInfo pathInfo = FileFolderServiceImpl.makeFolders(
      fileFolderService,
      destinationNodeRef,
      folderElementsList,
      ContentModel.TYPE_FOLDER
    );
    return pathInfo.getNodeRef();
  }

  private NodeRef createFile(
    final NodeRef destinationNodeRef,
    final String filePath,
    final List<IndexRecord> indexRecords,
    final String library,
    final NodeRef libraryNodeRef
  ) {
    String[] pathElements;
    if (filePath.startsWith(SLASH)) {
      pathElements = filePath.substring(1, filePath.length()).split(SLASH);
    } else {
      pathElements = filePath.split(SLASH);
    }
    final String[] folderElements = new String[pathElements.length - 1];
    System.arraycopy(
      pathElements,
      0,
      folderElements,
      0,
      pathElements.length - 1
    );
    final List<String> folderElementsList = new LinkedList<>(
      Arrays.asList(folderElements)
    );

    NodeRef folderNodeRef;
    if (!folderElementsList.isEmpty()) {
      // create folder
      if (folderElementsList.get(0).equals(library)) {
        folderElementsList.remove(0);
        folderNodeRef = folderElementsList.isEmpty()
          ? libraryNodeRef
          : createFolder(libraryNodeRef, folderElementsList);
      } else {
        folderNodeRef = createFolder(destinationNodeRef, folderElementsList);
      }
    } else {
      folderNodeRef = destinationNodeRef;
    }
    // create file
    final String fileName = pathElements[pathElements.length - 1];
    NodeRef nodeRef = nodeService.getChildByName(
      folderNodeRef,
      ContentModel.ASSOC_CONTAINS,
      fileName
    );
    if (nodeRef != null) {
      return handleExistingFile(nodeRef, filePath, indexRecords);
    }
    nodeRef = nodeService.getChildByName(
      folderNodeRef,
      ContentModel.ASSOC_CONTAINS,
      fileName
    );
    if (nodeRef == null) {
      FileInfo fileInfo = fileFolderService.create(
        folderNodeRef,
        fileName,
        ContentModel.TYPE_CONTENT
      );
      nodeRef = fileInfo.getNodeRef();
    }
    return nodeRef;
  }

  private NodeRef handleExistingFile(
    NodeRef nodeRef,
    String filePath,
    List<IndexRecord> indexRecords
  ) {
    for (IndexRecord indexRecord : indexRecords) {
      if (indexRecord.getName().equals(filePath)) {
        if (
          indexRecord.getOverwrite() != null &&
          indexRecord.getOverwrite().equals("Y")
        ) {
          if (logger.isTraceEnabled()) {
            logger.trace(OVERRIDE_THE_EXISTING_FILE + filePath);
          }
          return nodeRef;
        }
        if (logger.isErrorEnabled()) {
          logger.error("File allready exist" + filePath);
        }
        return null;
      }
    }
    return null;
  }

  private NodeRef unzip(
    NodeRef destinationNodeRef,
    ZipFile zipFile,
    ZipEntry zipEntry,
    List<IndexRecord> indexRecords,
    String library,
    NodeRef libraryNodeRef
  ) throws IOException {
    String filePath = zipEntry.getName();
    if (zipEntry.isDirectory()) {
      return unzipDirectory(
        destinationNodeRef,
        filePath,
        library,
        libraryNodeRef
      );
    }
    return unzipFile(
      destinationNodeRef,
      zipFile,
      zipEntry,
      filePath,
      indexRecords,
      library,
      libraryNodeRef
    );
  }

  private NodeRef unzipDirectory(
    NodeRef destinationNodeRef,
    String filePath,
    String library,
    NodeRef libraryNodeRef
  ) {
    NodeRef tmpDestNodeRef;
    String adjustedPath = filePath;
    if (adjustedPath.startsWith(File.separatorChar + library)) {
      adjustedPath = adjustedPath.substring(
        (File.separatorChar + library).length()
      );
      tmpDestNodeRef = libraryNodeRef;
    } else if (adjustedPath.startsWith(library)) {
      adjustedPath = adjustedPath.substring(library.length());
      tmpDestNodeRef = libraryNodeRef;
    } else {
      tmpDestNodeRef = destinationNodeRef;
    }
    String[] folderElements = adjustedPath.startsWith(SLASH)
      ? adjustedPath.substring(1).split(SLASH)
      : adjustedPath.split(SLASH);
    if (folderElements.length > 0 && !folderElements[0].isEmpty()) {
      return createFolder(
        tmpDestNodeRef,
        new LinkedList<>(Arrays.asList(folderElements))
      );
    }
    return tmpDestNodeRef;
  }

  private NodeRef unzipFile(
    NodeRef destinationNodeRef,
    ZipFile zipFile,
    ZipEntry zipEntry,
    String filePath,
    List<IndexRecord> indexRecords,
    String library,
    NodeRef libraryNodeRef
  ) throws IOException {
    NodeRef tmpDestNodeRef;
    String adjustedPath = filePath;
    if (adjustedPath.startsWith(File.separatorChar + library)) {
      adjustedPath = adjustedPath.substring(
        (File.separatorChar + library).length()
      );
      tmpDestNodeRef = libraryNodeRef;
    } else if (adjustedPath.startsWith(library)) {
      adjustedPath = adjustedPath.substring(library.length());
      tmpDestNodeRef = libraryNodeRef;
    } else {
      tmpDestNodeRef = destinationNodeRef;
    }
    NodeRef createdNodeRef = createFile(
      tmpDestNodeRef,
      adjustedPath,
      indexRecords,
      library,
      libraryNodeRef
    );
    if (createdNodeRef != null) {
      ContentWriter writer = contentService.getWriter(
        createdNodeRef,
        ContentModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(mimetypeService.guessMimetype(adjustedPath));
      writer.setEncoding("UTF-8");
      writer.putContent(zipFile.getInputStream(zipEntry));
      Map<QName, Serializable> titledProps = new HashMap<>();
      titledProps.put(ContentModel.PROP_TITLE, "");
      titledProps.put(ContentModel.PROP_DESCRIPTION, "");
      nodeService.addAspect(
        createdNodeRef,
        ContentModel.ASPECT_TITLED,
        titledProps
      );
    }
    return createdNodeRef;
  }
}
