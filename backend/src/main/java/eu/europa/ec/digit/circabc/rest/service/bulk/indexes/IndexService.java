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
package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service contract for handling the index file used during bulk content operations.
 *
 * <p>A bulk import/export archive carries an index file (see {@link #INDEX_FILE}) that lists the
 * content items together with their metadata. Implementations of this interface are responsible for
 * describing the expected columns of that file, parsing it into structured {@link IndexRecord}
 * entries while collecting any {@link ValidationMessage validation feedback}, and generating an
 * index file back from a set of records.
 */
public interface IndexService {
  /** Conventional name of the index file located inside a bulk operation archive. */
  String INDEX_FILE = "index.txt";

  /**
   * Returns the definition of the index file headers, i.e. the ordered set of columns expected in
   * the index file.
   *
   * @return the index headers describing the structure of the index file
   */
  IndexHeaders getIndexHeaders();

  /**
   * Parses the given index file into a list of {@link IndexRecord} entries.
   *
   * <p>Parsed records are appended to {@code indexRecords} and any problem encountered while reading
   * or validating the file is reported through {@code messages}.
   *
   * @param indexFile the index file to read
   * @param indexRecords the list to populate with the records parsed from the file
   * @param messages the list to populate with validation messages produced during parsing
   * @throws IOException if the index file cannot be read
   */
  void getIndexRecords(
    final File indexFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  ) throws IOException;

  /**
   * Writes the given {@link IndexRecord} entries to the specified index file.
   *
   * @param indexFile the index file to generate or overwrite
   * @param indexRecords the records to serialize into the index file
   * @throws IOException if the index file cannot be written
   */
  void generateIndexRecords(
    final File indexFile,
    final List<IndexRecord> indexRecords
  ) throws IOException;
}
