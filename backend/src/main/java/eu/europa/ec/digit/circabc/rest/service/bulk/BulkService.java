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
package eu.europa.ec.digit.circabc.rest.service.bulk;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.upload.UploadedEntry;
import eu.europa.ec.digit.circabc.rest.service.compress.CompressedEntry;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service that orchestrates the bulk upload of content into the Alfresco repository.
 *
 * <p>A bulk upload takes a single compressed archive (typically a ZIP), optionally accompanied by an
 * index file describing metadata for the archived entries, and imports its contents as nodes under a
 * given container node. The service is responsible for inspecting the archive, parsing and validating
 * the associated index metadata, cross-checking that the declared index entries match the actual
 * uploaded content, and finally creating the corresponding repository nodes.
 *
 * <p>Validation problems encountered during any step are not thrown as exceptions but instead
 * collected into a caller-supplied {@link ValidationMessage} list, allowing the caller to report all
 * issues back to the end user at once.
 */
public interface BulkService {
  /** Date pattern ({@code dd/MM/yyyy}) used to parse and format date values in bulk index files. */
  String INDEX_DATE_FORMAT = "dd/MM/yyyy";

  /**
   * Uploads the contents of a compressed archive as nodes under the given container, without any
   * externally supplied index metadata.
   *
   * @param containerNodeRef the repository node under which the archive content is imported
   * @param compressedFile the compressed archive whose entries are to be uploaded
   * @param messages a mutable list to which validation messages produced during the upload are added
   * @return the list of entries that were uploaded to the repository
   */
  List<UploadedEntry> upload(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<ValidationMessage> messages
  );

  /**
   * Uploads the contents of a compressed archive as nodes under the given container, applying the
   * supplied index records as metadata for the matching archive entries.
   *
   * @param containerNodeRef the repository node under which the archive content is imported
   * @param compressedFile the compressed archive whose entries are to be uploaded
   * @param indexRecords the index metadata records describing the archive entries
   * @param messages a mutable list to which validation messages produced during the upload are added
   * @return the list of entries that were uploaded to the repository
   */
  List<UploadedEntry> upload(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  );

  /**
   * Reads and parses the index file contained in the given compressed archive into index records.
   *
   * @param compressedFile the compressed archive expected to contain an index file
   * @param messages a mutable list to which validation messages produced during parsing are added
   * @return the list of parsed index records
   * @throws IOException if the archive or its index file cannot be read
   */
  List<IndexRecord> getIndexRecords(
    final File compressedFile,
    final List<ValidationMessage> messages
  ) throws IOException;

  /**
   * Inspects the given compressed archive and returns a description of each of its entries, resolved
   * against the target container.
   *
   * @param containerNodeRef the repository node under which the archive would be imported
   * @param compressedFile the compressed archive to inspect
   * @param messages a mutable list to which validation messages produced during inspection are added
   * @return the list of entries contained in the archive
   */
  List<CompressedEntry> getCompressedEntries(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<ValidationMessage> messages
  );

  /**
   * Extracts index metadata from a set of already existing repository nodes.
   *
   * @param nodeRefs the repository nodes whose metadata is to be read
   * @return the list of index records built from the given nodes
   */
  List<IndexRecord> getMetaData(final List<NodeRef> nodeRefs);

  /**
   * Validates that the entries declared in an index file are consistent with the entries actually
   * present in the uploaded archive.
   *
   * @param indexFileEntries the index records declared in the index file
   * @param uploadedEntries the entries actually found in the uploaded archive
   * @param messages a mutable list to which any detected validation problems are added
   */
  void validateEntries(
    final List<IndexRecord> indexFileEntries,
    final List<UploadedEntry> uploadedEntries,
    final List<ValidationMessage> messages
  );
}
