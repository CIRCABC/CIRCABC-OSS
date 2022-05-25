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
package eu.cec.digit.circabc.service.bulk;

import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.upload.UploadedEntry;
import eu.cec.digit.circabc.service.compress.CompressedEntry;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BulkService {

    String INDEX_DATE_FORMAT = "dd/MM/yyyy";

    List<UploadedEntry> upload(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<ValidationMessage> messages);

    List<UploadedEntry> upload(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages);

    List<IndexRecord> getIndexRecords(
            final File compressedFile, final List<ValidationMessage> messages) throws IOException;

    List<CompressedEntry> getCompressedEntries(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<ValidationMessage> messages);

    List<IndexRecord> getMetaData(final List<NodeRef> nodeRefs);

    void validateEntries(
            final List<IndexRecord> indexFileEntries,
            final List<UploadedEntry> uploadedEntries,
            final List<ValidationMessage> messages);
}
