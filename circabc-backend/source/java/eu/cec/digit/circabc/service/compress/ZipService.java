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

import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ZipService {

    boolean extract(
            final File compressedFile,
            final String fileName,
            final File outputFile,
            final List<ValidationMessage> messages);

    Map<String, NodeRef> extract(
            final NodeRef libraryNodeRef,
            final NodeRef destinationNodeRef,
            final File compressedFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages);

    Map<String, NodeRef> extract(
            final NodeRef libraryNodeRef,
            final NodeRef destinationNodeRef,
            final File compressedFile,
            final List<String> excludedFileName,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages);

    List<CompressedEntry> getCompressedEntries(
            final File compressedFile, final List<ValidationMessage> messages);

    void addingFileIntoArchive(final File newFile, final File compressedFile);

    void addingFilesIntoArchive(final List<File> newFiles, final File compressedFile);

    void addingFileIntoArchive(final NodeRef nodeRef, final File compressedFile);

    void addingFileIntoArchive(final List<NodeRef> nodeRefs, final File compressedFile);

    void addingFileIntoArchive(
            final List<NodeRef> nodeRefs, final File compressedFile, final File indexFile);

    String getRelativeLibraryPath(final NodeRef nodeRef);
}
