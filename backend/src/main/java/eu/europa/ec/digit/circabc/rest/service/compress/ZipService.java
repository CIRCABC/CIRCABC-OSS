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

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for creating and expanding compressed (ZIP) archives within CIRCABC.
 *
 * <p>Implementations handle two complementary concerns:
 *
 * <ul>
 *   <li><b>Extraction</b> — reading the contents of a compressed archive, either to plain files on
 *       disk or by importing them as nodes into the Alfresco repository (typically under a Library
 *       service), while collecting any {@link ValidationMessage} raised during the process.
 *   <li><b>Archiving</b> — adding files or repository nodes into an existing compressed archive.
 * </ul>
 *
 * <p>Because ZIP handling is inherently I/O bound, methods generally accumulate problems into a
 * supplied list of {@link ValidationMessage}s rather than throwing checked exceptions for every
 * malformed entry.
 */
public interface ZipService {
  /**
   * Extracts a single named entry from a compressed archive to the given output file.
   *
   * @param compressedFile the source compressed (ZIP) archive to read from
   * @param fileName the name of the entry to extract from the archive
   * @param outputFile the destination file to which the entry's content is written
   * @param messages collector to which validation/warning messages are appended during extraction
   * @return {@code true} if the named entry was found and successfully extracted, {@code false}
   *     otherwise
   */
  boolean extract(
    final File compressedFile,
    final String fileName,
    final File outputFile,
    final List<ValidationMessage> messages
  );

  /**
   * Extracts every entry of a compressed archive and imports them as nodes into the Alfresco
   * repository, preserving the archive's folder hierarchy beneath the destination node.
   *
   * @param libraryNodeRef the Library service node that scopes the import (used for path
   *     resolution and permission context)
   * @param destinationNodeRef the folder node under which the extracted content is created
   * @param compressedFile the source compressed (ZIP) archive to expand
   * @param indexRecords index records describing metadata to apply to the imported entries
   * @param messages collector to which validation/warning messages are appended during extraction
   * @return a map from each extracted entry's (relative) path to the {@link NodeRef} of the node
   *     created for it in the repository
   */
  Map<String, NodeRef> extract(
    final NodeRef libraryNodeRef,
    final NodeRef destinationNodeRef,
    final File compressedFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  );

  /**
   * Extracts a compressed archive into the repository as {@link #extract(NodeRef, NodeRef, File,
   * List, List)} does, but skips any entries whose names appear in the exclusion list.
   *
   * @param libraryNodeRef the Library service node that scopes the import (used for path
   *     resolution and permission context)
   * @param destinationNodeRef the folder node under which the extracted content is created
   * @param compressedFile the source compressed (ZIP) archive to expand
   * @param excludedFileName names of archive entries that must not be imported
   * @param indexRecords index records describing metadata to apply to the imported entries
   * @param messages collector to which validation/warning messages are appended during extraction
   * @return a map from each imported entry's (relative) path to the {@link NodeRef} of the node
   *     created for it in the repository
   */
  Map<String, NodeRef> extract(
    final NodeRef libraryNodeRef,
    final NodeRef destinationNodeRef,
    final File compressedFile,
    final List<String> excludedFileName,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  );

  /**
   * Lists the entries contained in a compressed archive without extracting them.
   *
   * @param compressedFile the compressed (ZIP) archive to inspect
   * @param messages collector to which validation/warning messages are appended while reading the
   *     archive
   * @return the list of {@link CompressedEntry} descriptors found in the archive
   */
  List<CompressedEntry> getCompressedEntries(
    final File compressedFile,
    final List<ValidationMessage> messages
  );

  /**
   * Adds a single file to an existing compressed archive.
   *
   * @param newFile the file to add to the archive
   * @param compressedFile the target compressed (ZIP) archive to update
   */
  void addingFileIntoArchive(final File newFile, final File compressedFile);

  /**
   * Adds several files to an existing compressed archive.
   *
   * @param newFiles the files to add to the archive
   * @param compressedFile the target compressed (ZIP) archive to update
   */
  void addingFilesIntoArchive(
    final List<File> newFiles,
    final File compressedFile
  );

  /**
   * Adds the content of a single repository node to an existing compressed archive.
   *
   * @param nodeRef the repository node whose content is added to the archive
   * @param compressedFile the target compressed (ZIP) archive to update
   */
  void addingFileIntoArchive(final NodeRef nodeRef, final File compressedFile);

  /**
   * Adds the content of several repository nodes to an existing compressed archive.
   *
   * @param nodeRefs the repository nodes whose content is added to the archive
   * @param compressedFile the target compressed (ZIP) archive to update
   */
  void addingFileIntoArchive(
    final List<NodeRef> nodeRefs,
    final File compressedFile
  );

  /**
   * Adds the content of several repository nodes to an existing compressed archive together with an
   * accompanying index file.
   *
   * @param nodeRefs the repository nodes whose content is added to the archive
   * @param compressedFile the target compressed (ZIP) archive to update
   * @param indexFile the index file (describing the archived nodes) to include in the archive
   */
  void addingFileIntoArchive(
    final List<NodeRef> nodeRefs,
    final File compressedFile,
    final File indexFile
  );

  /**
   * Computes the path of a node relative to its containing Library service.
   *
   * @param nodeRef the repository node to resolve
   * @return the node's path relative to the Library root, suitable for use as an archive entry name
   */
  String getRelativeLibraryPath(final NodeRef nodeRef);
}
