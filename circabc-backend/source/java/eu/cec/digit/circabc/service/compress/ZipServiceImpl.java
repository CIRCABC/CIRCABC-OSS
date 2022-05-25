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
package eu.cec.digit.circabc.service.compress;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import de.schlichtherle.util.zip.ZipOutputStream;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.*;
import java.util.*;

public class ZipServiceImpl implements ZipService {

    private static final String FILE_ALLREADY_EXIST = "file_allready_exist";
    private static final String SLASH = "/";
    private static final String INDEX_TXT = "index.txt";
    private static final String OVERRIDE_THE_EXISTING_FILE = "Override the existing file:";
    private static final String EXTRACT_FILE = "Extract file:";
    private static final String LIST_CONTENT_OF = "listContent of:";
    private static final String FROM = " from:";
    private static final String UNZIPPING = "unzipping:";
    private static final String TO = " to:";
    private static final String UNCOMPRESS = "uncompress:";
    private static final String CREATE_FILE_INTO_REPOSITORY = "Create file into repository:";
    private static final String ERROR_WHEN_CLOSING_COMPRESSED_FILE =
            "Error when closing compressed file:";
    private static final String ERROR_DURING_UNCOMPRESSING_FILE = "Error during uncompressing file:";
    private static final String ERROR_WHEN_EXTRACTING_BULK_UPLOAD_FILE =
            "Error when extracting bulkUpload file:";
    private static final String ERROR_WHEN_UNCOMPRESSING_BULK_UPLOAD_FILE =
            "Error when uncompressing bulkUpload file:";
    private static final String ERROR_WHEN_UNZIPPING_COMPRESSED_FILE =
            "Error when unzipping compressed file:";
    private static final Log logger = LogFactory.getLog(ZipServiceImpl.class);
    private static final int BUFFER_SIZE = 32 * 1024;
    private final Map<QName, TypeDefinition> validTypeMap = new HashMap<>();
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    // to move
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private ManagementService managementService;

    public boolean extract(
            final File compressedFile,
            final String fileName,
            final File outputFile,
            final List<ValidationMessage> messages) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    UNZIPPING + fileName + FROM + compressedFile.getName() + TO + outputFile.getPath());
        }
        boolean succeed = false;
        ZipFile zipFile = null;
        try {

            zipFile = new ZipFile(compressedFile);
            ZipEntry zipEntry;
            @SuppressWarnings("unchecked")
            // final Enumeration<ZipEntry> zipEntries = zipFile.getEntries();
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
            succeed = false;
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(), ioe);
            }
            messages.add(
                    new ValidationMessageImpl(
                            0,
                            compressedFile.getName(),
                            ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + ioe.getMessage(),
                            ErrorType.Fatal));
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException ioe) {
                    if (logger.isErrorEnabled()) {
                        logger.error(ERROR_WHEN_CLOSING_COMPRESSED_FILE + compressedFile.getName(), ioe);
                    }
                }
            }
        }
        return succeed;
    }

    public List<CompressedEntry> getCompressedEntries(
            final File compressedFile, final List<ValidationMessage> messages) {
        if (logger.isDebugEnabled()) {
            logger.debug(LIST_CONTENT_OF + compressedFile.getPath());
        }
        final List<CompressedEntry> content = new LinkedList<>();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(compressedFile);
            CompressedEntry compressedEntry;
            ZipEntry zipEntry;
            @SuppressWarnings("unchecked")
            // final Enumeration<ZipEntry> zipEntries = zipFile.getEntries();
            final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                compressedEntry = new CompressedEntryImpl(zipEntry);
                content.add(compressedEntry);
            }
        } catch (final IOException ioe) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(), ioe);
            }
            messages.add(
                    new ValidationMessageImpl(
                            0,
                            compressedFile.getName(),
                            ERROR_WHEN_UNCOMPRESSING_BULK_UPLOAD_FILE + ioe.getMessage(),
                            ErrorType.Fatal));
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(), e);
            }
            messages.add(
                    new ValidationMessageImpl(
                            0,
                            compressedFile.getName(),
                            ERROR_WHEN_EXTRACTING_BULK_UPLOAD_FILE + e.getMessage(),
                            ErrorType.Fatal));
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException ioe) {
                    if (logger.isErrorEnabled()) {
                        logger.error(ERROR_WHEN_CLOSING_COMPRESSED_FILE + compressedFile.getName(), ioe);
                    }
                }
            }
        }
        return content;
    }

    public Map<String, NodeRef> extract(
            final NodeRef libraryNodeRef,
            final NodeRef destinationNodeRef,
            final File compressedFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages) {
        return extract(
                libraryNodeRef,
                destinationNodeRef,
                compressedFile,
                Collections.<String>emptyList(),
                indexRecords,
                messages);
    }

    public void addingFileIntoArchive(final NodeRef nodeRef, final File compressedFile) {
        final List<NodeRef> nodeRefs = new ArrayList<>();
        nodeRefs.add(nodeRef);
        addingFileIntoArchive(nodeRefs, compressedFile);
    }

    public void addingFileIntoArchive(final List<NodeRef> nodeRefs, final File compressedFile) {
        addingFileIntoArchive(nodeRefs, compressedFile, null);
    }

    public void addingFileIntoArchive(
            final List<NodeRef> nodeRefs, final File compressedFile, final File indexFile) {
        final Date now = new Date();
        ContentReader contentReader;
        String filename;

        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(compressedFile));
            zipOutputStream.setMethod(ZipEntry.DEFLATED);
            ZipEntry zipentry;

            // Create a buffer for reading the files
            final byte[] buf = new byte[1024];
            InputStream inputStream = null;

            final List<NodeRef> allNodeRefs = new ArrayList<>();

            for (final NodeRef nodeRef : nodeRefs) {
                allNodeRefs.addAll(getAllChildsNodeRefs(nodeRef));
            }

            String securityRanking;
            QName type;
            for (final NodeRef nodeRef : allNodeRefs) {
                securityRanking =
                        (String) nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING);
                // TODO check why securityRanking is null with translated document
                // FIX BUG DIGITCIRCABC-4692
                if (securityRanking != null
                        && !(DocumentModel.SECURITY_RANKINGS_PUBLIC.equalsIgnoreCase(securityRanking)
                                || DocumentModel.SECURITY_RANKINGS_NORMAL.equalsIgnoreCase(securityRanking))) {
                    // ignore not public and normal security ranking
                    continue;
                }
                type = nodeService.getType(nodeRef);
                if (ContentModel.TYPE_CONTENT.equals(type)
                        || dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                    contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                } else {
                    contentReader = null;
                }
                // Get the file name
                filename = getRelativeLibraryPath(nodeRef);
                inputStream = null;
                try {
                    // reading new File
                    if (contentReader != null) {
                        if (filename.startsWith("/")) {
                            filename = filename.substring(1);
                        }
                        zipentry = new ZipEntry(filename);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Adding new File: " + filename);
                        }
                    } else {
                        if (filename.endsWith(SLASH)) {
                            zipentry = new ZipEntry(filename);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Adding new Directory: " + filename);
                            }
                        } else {
                            zipentry = new ZipEntry(filename + SLASH);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Adding new Directory: " + filename + SLASH);
                            }
                        }
                    }
                    if (contentReader != null) {
                        inputStream = contentReader.getContentInputStream();
                        zipentry.setSize(contentReader.getContentData().getSize());
                    }

                    zipentry.setMethod(ZipEntry.DEFLATED);
                    zipentry.setTime(now.getTime());

                    // final long crc32 = ChecksumCRC32.getChecksum(contentReader.getContentInputStream(),
                    // filename, contentReader.getContentData().getSize());
                    // zipentry.setCrc(crc32);
                    zipOutputStream.putNextEntry(zipentry);
                    if (contentReader != null) {
                        int len;
                        // Transfer bytes from the file to the ZIP file
                        while ((len = inputStream.read(buf)) > 0) {
                            zipOutputStream.write(buf, 0, len);
                        }
                    }
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                } catch (final IOException io) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception: " + io.getMessage(), io);
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (final IOException io) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Exception: " + io.getMessage(), io);
                            }
                        }
                    }
                }
            }
            if (indexFile != null) {
                FileInputStream fileStream = null;
                try {
                    // reading new File
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding indexFile: " + indexFile.getPath());
                    }
                    zipentry = new ZipEntry(INDEX_TXT);
                    zipentry.setMethod(ZipEntry.DEFLATED);

                    zipentry.setTime(now.getTime());
                    zipOutputStream.putNextEntry(zipentry);
                    fileStream = new FileInputStream(indexFile);
                    int len;

                    while ((len = fileStream.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                } catch (final IOException io) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception: " + io.getMessage(), io);
                    }
                } finally {
                    if (fileStream != null) {
                        try {
                            fileStream.close();
                        } catch (final IOException io) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Exception: " + io.getMessage(), io);
                            }
                        }
                    }
                }
            }
        } catch (final FileNotFoundException fnfe) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception: " + fnfe.getMessage(), fnfe);
            }
        } finally {
            if (zipOutputStream != null) {
                // finished
                try {
                    zipOutputStream.finish();
                } catch (final IOException io) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception: " + io.getMessage(), io);
                    }
                } finally {
                    try {
                        zipOutputStream.close();
                    } catch (final IOException io) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Exception: " + io.getMessage(), io);
                        }
                    }
                }
            }
        }
    }

    /**
     * TODO Move this method to a utility class
     */
    public String getRelativeLibraryPath(final NodeRef nodeRef) {
        final Path path = nodeService.getPath(nodeRef);
        final NodeRef libraryNodeRef = managementService.getCurrentLibrary(nodeRef);
        final String relativePath = getNamePath(path, libraryNodeRef, SLASH, false);
        return relativePath;
    }

    /**
     * TODO Move this method to a utility class
     */
    public String getAbsoluteLibraryPath(final NodeRef nodeRef) {
        final Path path = nodeService.getPath(nodeRef);
        final NodeRef libraryNodeRef = managementService.getCurrentLibrary(nodeRef);
        final String relativePath = getNamePath(path, libraryNodeRef, SLASH, true);
        return relativePath;
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
            final Path path, final NodeRef rootNode, final String separator, final boolean absolutePath) {
        final StringBuilder buf = new StringBuilder(128);

        // ignore root node check if not passed in
        boolean foundRoot = (rootNode == null);

        // skip first element as it represents repo root '/'
        Path.Element element;
        String elementString = null;
        ChildAssociationRef elementRef;
        Object nameProp;
        String thePath = "";
        for (int i = 1; i < path.size(); i++) {
            element = path.get(i);
            elementString = null;
            if (element instanceof Path.ChildAssocElement) {
                elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    // only append if we've found the root already
                    if (foundRoot == true) {
                        nameProp = nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME);
                        if (nameProp != null) {
                            elementString = nameProp.toString();
                        } else {
                            elementString = element.getElementString();
                        }
                    }

                    // either we've found root already or may have now
                    // check after as we want to skip the root as it represents the CIFS share name
                    foundRoot = (foundRoot || elementRef.getChildRef().equals(rootNode));
                }
            } else {
                elementString = element.getElementString();
            }

            if (elementString != null) {
                buf.append(separator);
                buf.append(elementString);
            }
        }
        if (absolutePath) {
            nameProp = nodeService.getProperty(rootNode, ContentModel.PROP_NAME);
            if (nameProp != null) {
                elementString = nameProp.toString();
            }

            if (elementString != null && elementString.length() != 0) {
                thePath = separator + elementString + buf.toString();
            }
        } else {
            thePath = buf.toString();
        }

        return thePath;
    }

    private Collection<? extends NodeRef> getAllChildsNodeRefs(final NodeRef nodeRef) {
        final List<NodeRef> allNodeRefs = new ArrayList<>();

        TypeDefinition typeDef;

        if (nodeService.exists(nodeRef)
                && AccessStatus.ALLOWED.equals(
                permissionService.hasPermission(nodeRef, PermissionService.READ))) {
            final QName type = nodeService.getType(nodeRef);

            // Trick to optimise the efficiency of the code
            if (validTypeMap.containsKey(type)) {
                typeDef = validTypeMap.get(type);
            } else {
                // make sure the type is defined in the data dictionary
                typeDef = dictionaryService.getType(type);
                validTypeMap.put(type, typeDef);
            }

            if (typeDef != null) {
                /** Optimistic optimisationfor type evaluation */

                // look for File content node
                if (ContentModel.TYPE_CONTENT.equals(type)) {
                    allNodeRefs.add(nodeRef);
                }

                /** Optimistic optimisationfor type evaluation */

                // look for Space folder node
                else if (ContentModel.TYPE_FOLDER.equals(type)) {
                    createFolderRepresentation(nodeRef, allNodeRefs);
                }

                // look for File Link object node
                else if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                    allNodeRefs.add(nodeRef);
                } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true
                        && dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                    createFolderRepresentation(nodeRef, allNodeRefs);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
                }
            }
        }
        return allNodeRefs;
    }

    private void createFileLinkRepresentation(
            final NodeRef nodeRef, final List<NodeRef> allNodeRefs) {
        // only display the user has the permissions to navigate to the target of the link
        final NodeRef destRef =
                (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            allNodeRefs.add(destRef);
        }
    }

    private void createFolderLinkRepresentation(
            final NodeRef nodeRef, final List<NodeRef> allNodeRefs) {
        // only display the user has the permissions to navigate to the target of the link
        final NodeRef destRef =
                (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            final List<ChildAssociationRef> listChildAssocs = nodeService.getChildAssocs(destRef);
            allNodeRefs.add(nodeRef);
            for (final ChildAssociationRef childAssoc : listChildAssocs) {
                allNodeRefs.addAll(getAllChildsNodeRefs(childAssoc.getChildRef()));
            }
        }
    }

    private void createFolderRepresentation(final NodeRef nodeRef, final List<NodeRef> allNodeRefs) {
        final List<ChildAssociationRef> listChildAssocs = nodeService.getChildAssocs(nodeRef);
        allNodeRefs.add(nodeRef);
        for (final ChildAssociationRef childAssoc : listChildAssocs) {
            allNodeRefs.addAll(getAllChildsNodeRefs(childAssoc.getChildRef()));
        }
    }

    public void addingFileIntoArchive(final File newFile, final File compressedFile) {
        final List<File> newFiles = new ArrayList<>();
        newFiles.add(newFile);
        addingFilesIntoArchive(newFiles, compressedFile);
    }

    public void addingFilesIntoArchive(final List<File> newFiles, final File compressedFile) {
        addingFilesIntoArchive(newFiles, compressedFile, null);
    }

    public void addingFilesIntoArchive(
            final List<File> newFiles, final File compressedFile, final File indexFile) {
        final Date now = new Date();
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(compressedFile));
            // zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            // Create a buffer for reading the files
            final byte[] buf = new byte[1024];
            ZipEntry zipentry;
            for (final File newFile : newFiles) {
                FileInputStream fileStream = null;
                try {
                    // reading new File
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding new File: " + newFile.getPath());
                    }

                    if (!newFile.isDirectory()) {
                        zipentry = new ZipEntry(newFile.getPath());
                        zipentry.setSize(newFile.length());
                    } else {
                        if (newFile.getName().endsWith(SLASH)) {
                            zipentry = new ZipEntry(newFile.getPath());
                        } else {
                            zipentry = new ZipEntry(newFile.getPath() + SLASH);
                        }
                    }
                    zipentry.setMethod(ZipEntry.DEFLATED);
                    zipentry.setTime(now.getTime());

                    // final long crc32 = ChecksumCRC32.getChecksum(newFile.getPath());
                    // zipentry.setCrc(crc32);

                    // adding to the new jarOutputStream
                    zipOutputStream.putNextEntry(zipentry);
                    if (!newFile.isDirectory()) {
                        fileStream = new FileInputStream(newFile);
                        // Transfer bytes from the file to the ZIP file
                        int len;
                        while ((len = fileStream.read(buf)) > 0) {
                            zipOutputStream.write(buf, 0, len);
                        }
                    }
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                } finally {
                    if (fileStream != null) {
                        try {
                            fileStream.close();
                        } catch (final IOException io) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Exception: " + io.getMessage(), io);
                            }
                        }
                    }
                }
            }
            if (indexFile != null) {
                FileInputStream fileStream = null;
                try {
                    // reading new File
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding indexFile: " + indexFile.getPath());
                    }
                    zipentry = new ZipEntry(indexFile.getPath());
                    zipOutputStream.putNextEntry(zipentry);
                    fileStream = new FileInputStream(indexFile);
                    int len;
                    // Create a buffer for reading the files
                    while ((len = fileStream.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                } finally {
                    if (fileStream != null) {
                        try {
                            fileStream.close();
                        } catch (final IOException io) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Exception: " + io.getMessage(), io);
                            }
                        }
                    }
                }
            }

        } catch (final IOException io) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception: " + io.getMessage(), io);
            }
        } finally {
            if (zipOutputStream != null) {
                // finished
                try {
                    zipOutputStream.finish();
                } catch (final IOException io) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception: " + io.getMessage(), io);
                    }
                } finally {
                    try {
                        zipOutputStream.close();
                    } catch (final IOException io) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Exception: " + io.getMessage(), io);
                        }
                    }
                }
            }
        }
    }

    private boolean isFolderAlreadyExtracted(
            final Set<String> extractedNodesKeys, final String filePath) {
        boolean found = false;
        for (final String extractedNodesKey : extractedNodesKeys) {
            if (extractedNodesKey.startsWith(filePath)) {
                found = true;
                break;
            }
        }

        return found;
    }

    public Map<String, NodeRef> extract(
            final NodeRef libraryNodeRef,
            final NodeRef destinationNodeRef,
            final File compressedFile,
            final List<String> excludedFileName,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages) {
        if (logger.isDebugEnabled()) {
            logger.debug(UNCOMPRESS + compressedFile.getName() + TO + destinationNodeRef);
        }
        final Map<String, NodeRef> extractedNodes = new HashMap<>();
        ZipFile zipFile = null;
        NodeRef createdNodeRef = null;

        final String library = (String) nodeService.getProperty(libraryNodeRef, ContentModel.PROP_NAME);

        try {
            zipFile = new ZipFile(compressedFile);
            ZipEntry zipEntry;
            @SuppressWarnings("unchecked")
            // final Enumeration<ZipEntry> zipEntries = zipFile.getEntries();
            final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            String filePath;
            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                filePath = zipEntry.getName();

                if (File.separatorChar == '/') {
                    filePath = filePath.replace('\\', File.separatorChar);
                } else {
                    filePath = filePath.replace('/', File.separatorChar);
                }
                if (!filePath.startsWith(File.separator)) {
                    filePath = File.separatorChar + filePath;
                }

                if (zipEntry.isDirectory()) {
                    if (isFolderAlreadyExtracted(extractedNodes.keySet(), filePath)) {
                        continue;
                    }
                }
                if (!excludedFileName.contains(filePath)) {
                    createdNodeRef =
                            unzip(destinationNodeRef, zipFile, zipEntry, indexRecords, library, libraryNodeRef);
                    if (createdNodeRef != null) {
                        extractedNodes.put(filePath, createdNodeRef);
                        if (logger.isDebugEnabled()) {
                            logger.debug(EXTRACT_FILE + filePath + TO + createdNodeRef);
                        }
                    } else {
                        final String fileAllreadyExist = I18NUtil.getMessage(FILE_ALLREADY_EXIST);
                        if (logger.isDebugEnabled()) {
                            logger.debug(fileAllreadyExist + filePath);
                        }
                        messages.add(
                                new ValidationMessageImpl(0, filePath, fileAllreadyExist, ErrorType.Fatal));
                    }
                }
            }
        } catch (final IOException ioe) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_WHEN_UNZIPPING_COMPRESSED_FILE + compressedFile.getName(), ioe);
            }
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_DURING_UNCOMPRESSING_FILE + compressedFile.getName(), e);
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ioe) {
                    if (logger.isErrorEnabled()) {
                        logger.error(ERROR_WHEN_CLOSING_COMPRESSED_FILE + compressedFile.getName(), ioe);
                    }
                }
            }
        }
        return extractedNodes;
    }

    private void unzip(final File outputFile, final ZipFile zipFile, final ZipEntry zipEntry)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            int nbRead = 0;
            while ((nbRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, nbRead);
            }
        } catch (IOException ioex) {
            logger.error("Error when tring to unzip ", ioex);
        } finally {
            try {
                if (out != null) {
                    try {
                        out.flush();
                    } finally {
                        out.close();
                    }
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    private NodeRef createFolder(
            final NodeRef destinationNodeRef, final List<String> folderElementsList) {
        final FileInfo pathInfo =
                FileFolderServiceImpl.makeFolders(
                        fileFolderService, destinationNodeRef, folderElementsList, ContentModel.TYPE_FOLDER);
        return pathInfo.getNodeRef();
    }

    private NodeRef createFile(
            final NodeRef destinationNodeRef,
            final String filePath,
            final List<IndexRecord> indexRecords,
            final String library,
            final NodeRef libraryNodeRef) {
        String[] pathElements;
        if (filePath.startsWith(SLASH)) {
            pathElements = filePath.substring(1, filePath.length()).split(SLASH);
        } else {
            pathElements = filePath.split(SLASH);
        }
        final String[] folderElements = new String[pathElements.length - 1];
        System.arraycopy(pathElements, 0, folderElements, 0, pathElements.length - 1);
        final List<String> folderElementsList = new LinkedList<>(Arrays.asList(folderElements));

        NodeRef folderNodeRef;
        if (folderElementsList.size() > 0) {
            // create folder
            if (folderElementsList.get(0).equals(library)) {
                folderElementsList.remove(0);
                if (folderElementsList.size() == 0) {
                    folderNodeRef = libraryNodeRef;
                } else {
                    folderNodeRef = createFolder(libraryNodeRef, folderElementsList);
                }
            } else {
                folderNodeRef = createFolder(destinationNodeRef, folderElementsList);
            }
        } else {
            folderNodeRef = destinationNodeRef;
        }
        // create file
        final String fileName = pathElements[pathElements.length - 1];
        NodeRef nodeRef =
                nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
        // NodeRef nodeRef = fileFolderService.searchSimple(folderNodeRef, fileName);
        if (nodeRef != null) {
            // File exist in the repository
            boolean found = false;
            for (final IndexRecord indexRecord : indexRecords) {
                if (indexRecord.getName().equals(filePath)) {
                    found = true;
                    if (indexRecord.getOverwrite() != null && indexRecord.getOverwrite().equals("Y")) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(OVERRIDE_THE_EXISTING_FILE + filePath);
                        }
                        break;
                    } else {
                        // TODO Raise Exception here
                        if (logger.isErrorEnabled()) {
                            logger.error("File allready exist" + filePath);
                        }
                        nodeRef = null;
                        break;
                    }
                }
            }
            if (!found) {
                // No OVERIDE specification in the index for this file.
                nodeRef = null;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(CREATE_FILE_INTO_REPOSITORY + filePath);
            }
            nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
            if (nodeRef == null) {
                final FileInfo fileInfo =
                        fileFolderService.create(folderNodeRef, fileName, ContentModel.TYPE_CONTENT);
                nodeRef = fileInfo.getNodeRef();
            }
        }
        return nodeRef;
    }

    private NodeRef unzip(
            final NodeRef destinationNodeRef,
            final ZipFile zipFile,
            final ZipEntry zipEntry,
            final List<IndexRecord> indexRecords,
            final String library,
            final NodeRef libraryNodeRef)
            throws IOException {
        String filePath = zipEntry.getName();
        final String fileTitle = "";
        final String fileDescription = "";
        boolean isDirectory = zipEntry.isDirectory();
        NodeRef createdNodeRef;
        NodeRef tmpDestNodeRef;
        if (isDirectory) {
            String[] folderElements;
            if (filePath.startsWith(File.separatorChar + library)) {
                filePath = filePath.substring((File.separatorChar + library).length(), filePath.length());
                folderElements = filePath.substring(1, filePath.length()).split(SLASH);
                tmpDestNodeRef = libraryNodeRef;
            } else if (filePath.startsWith(library)) {
                filePath = filePath.substring(library.length(), filePath.length());
                folderElements = filePath.substring(1, filePath.length()).split(SLASH);
                tmpDestNodeRef = libraryNodeRef;
            } else if (filePath.startsWith(SLASH)) {
                folderElements = filePath.substring(1, filePath.length()).split(SLASH);
                tmpDestNodeRef = destinationNodeRef;
            } else {
                folderElements = filePath.split(SLASH);
                tmpDestNodeRef = destinationNodeRef;
            }
            final List<String> folderElementsList = new LinkedList<>(Arrays.asList(folderElements));
            if (folderElements.length > 0 && folderElements[0].length() > 0) {
                createdNodeRef = createFolder(tmpDestNodeRef, folderElementsList);
            } else {
                createdNodeRef = tmpDestNodeRef;
            }
        } else {
            if (filePath.startsWith(File.separatorChar + library)) {
                filePath = filePath.substring((File.separatorChar + library).length(), filePath.length());
                tmpDestNodeRef = libraryNodeRef;
            } else if (filePath.startsWith(library)) {
                filePath = filePath.substring(library.length(), filePath.length());
                tmpDestNodeRef = libraryNodeRef;
            } else {
                tmpDestNodeRef = destinationNodeRef;
            }
            createdNodeRef = createFile(tmpDestNodeRef, filePath, indexRecords, library, libraryNodeRef);
            if (createdNodeRef != null) {
                final ContentWriter writer =
                        contentService.getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetypeService.guessMimetype(filePath));
                writer.setEncoding("UTF-8");
                writer.putContent(zipFile.getInputStream(zipEntry));

                // TODO Put this in the Bussiness layer
                // add titled aspect (for Web Client display)
                final Map<QName, Serializable> titledProps = new HashMap<>();
                titledProps.put(ContentModel.PROP_TITLE, fileTitle);
                titledProps.put(ContentModel.PROP_DESCRIPTION, fileDescription);
                nodeService.addAspect(createdNodeRef, ContentModel.ASPECT_TITLED, titledProps);
            }
        }
        return createdNodeRef;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    public void setMimetypeService(final MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    public void setFileFolderService(final FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    public void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the dictionaryService
     */
    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(final DictionaryService dictionaryService) {
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
    public void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }
}
