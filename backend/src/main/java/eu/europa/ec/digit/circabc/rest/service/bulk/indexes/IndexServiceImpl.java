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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessageImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.ExpirDateValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.IndexValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.IssueDateValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.LangValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.NameValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.NoContentValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.OriLangValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.OverwriteValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.SecurityRankingValidator;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator.StatusValidator;
import io.swagger.model.alfresco.DocumentModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Default implementation of {@link IndexService}.
 *
 * <p>Handles the tab-delimited {@code index.txt} metadata file used during CIRCABC bulk document
 * uploads. It is responsible for:
 *
 * <ul>
 *   <li>Reading an existing index file into {@link IndexRecord} instances while collecting
 *       {@link ValidationMessage} entries produced by the registered {@link IndexValidator}s.
 *   <li>Generating (writing) an index file from a list of {@link IndexRecord}s using the full,
 *       canonical column layout.
 *   <li>Exposing the canonical set of {@link IndexHeaders} recognised by the platform.
 * </ul>
 *
 * <p>The index file uses a tab ({@code \t}) as the column separator and UTF-8 encoding. Each data
 * row describes one document to be imported, keyed by the header columns declared in
 * {@link #HEADERS}.
 */
public class IndexServiceImpl implements IndexService {

  /**
   * Canonical, ordered list of column names recognised in the index file. This defines both the
   * expected columns when reading and the columns produced when writing an index file, including
   * the fixed metadata columns and the dynamic attribute columns ({@code ATTRI1}..{@code ATTRI20}).
   */
  static final String[] HEADERS = new String[] {
    IndexHeaderColumn.NAME,
    IndexHeaderColumn.TITLE,
    IndexHeaderColumn.DESCRIPTION,
    IndexHeaderColumn.AUTHOR,
    IndexHeaderColumn.KEYWORDS,
    IndexHeaderColumn.STATUS,
    IndexHeaderColumn.ISSUE_DATE,
    IndexHeaderColumn.REFERENCE,
    IndexHeaderColumn.EXPIRATION_DATE,
    IndexHeaderColumn.SECURITY_RANKING,
    IndexHeaderColumn.ATTRI1,
    IndexHeaderColumn.ATTRI2,
    IndexHeaderColumn.ATTRI3,
    IndexHeaderColumn.ATTRI4,
    IndexHeaderColumn.ATTRI5,
    IndexHeaderColumn.ATTRI6,
    IndexHeaderColumn.ATTRI7,
    IndexHeaderColumn.ATTRI8,
    IndexHeaderColumn.ATTRI9,
    IndexHeaderColumn.ATTRI10,
    IndexHeaderColumn.ATTRI11,
    IndexHeaderColumn.ATTRI12,
    IndexHeaderColumn.ATTRI13,
    IndexHeaderColumn.ATTRI14,
    IndexHeaderColumn.ATTRI15,
    IndexHeaderColumn.ATTRI16,
    IndexHeaderColumn.ATTRI17,
    IndexHeaderColumn.ATTRI18,
    IndexHeaderColumn.ATTRI19,
    IndexHeaderColumn.ATTRI20,
    IndexHeaderColumn.TYPE_DOCUMENT,
    IndexHeaderColumn.TRANSLATOR,
    IndexHeaderColumn.DOC_LANG,
    IndexHeaderColumn.NO_CONTENT,
    IndexHeaderColumn.ORI_LANG,
    IndexHeaderColumn.REL_TRANS,
    IndexHeaderColumn.OVERWRITE,
  };

  /**
   * Lazily initialised, shared list of validators applied to every parsed record. Populated on the
   * first call to {@link #validate(IndexRecord, List)}.
   */
  private static final List<IndexValidator> VALIDATORS = new ArrayList<>();

  /** The canonical headers instance built from {@link #HEADERS}. */
  private final IndexHeaders indexHeaders = new IndexHeadersImpl(HEADERS);

  /** Alfresco service registry, injected by Spring and passed to the validators. */
  private ServiceRegistry serviceRegistry;

  /**
   * Returns the canonical set of headers recognised by this service.
   *
   * @return the {@link IndexHeaders} describing all supported index columns
   */
  public IndexHeaders getIndexHeaders() {
    return indexHeaders;
  }

  /**
   * Reads the given tab-delimited index file, parsing each data row into an {@link IndexRecord} and
   * validating it. The first row is treated as the header row and is used to map column positions
   * to header names. Empty rows are skipped and reported as warnings.
   *
   * @param indexFile the tab-delimited index file to read
   * @param indexRecords output list that will be populated with the parsed records
   * @param messages output list that will be populated with validation and warning messages
   * @throws IOException if the file cannot be read
   */
  public void getIndexRecords(
    final File indexFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  ) throws IOException {
    final CSVReader reader = new CSVReader(new FileReader(indexFile), '\t');
    String[] headerRow;
    int rowNumber = 0;

    if ((headerRow = reader.readNext()) != null) {
      rowNumber++;
      Map<Integer, String> fileHeaderPositions = buildHeaderPositions(
        headerRow
      );
      processDataRows(
        reader,
        rowNumber,
        fileHeaderPositions,
        indexRecords,
        messages
      );
    }
  }

  private Map<Integer, String> buildHeaderPositions(String[] headerRow) {
    Map<Integer, String> positions = new HashMap<>();
    for (int i = 0; i < headerRow.length; i++) {
      positions.put(i, headerRow[i]);
    }
    return positions;
  }

  private int processDataRows(
    CSVReader reader,
    int rowNumber,
    Map<Integer, String> fileHeaderPositions,
    List<IndexRecord> indexRecords,
    List<ValidationMessage> messages
  ) throws IOException {
    String[] nextRow;
    while ((nextRow = reader.readNext()) != null) {
      rowNumber++;
      if (isEmptyLine(nextRow)) {
        messages.add(
          new ValidationMessageImpl(
            rowNumber,
            IndexService.INDEX_FILE,
            I18NUtil.getMessage("bulk_upload_empty_row"),
            ErrorType.Warning
          )
        );
        continue;
      }
      IndexRecord indexRecord = parseRow(
        nextRow,
        rowNumber,
        fileHeaderPositions
      );
      validate(indexRecord, messages);
      indexRecords.add(indexRecord);
    }
    return rowNumber;
  }

  private IndexRecord parseRow(
    String[] nextRow,
    int rowNumber,
    Map<Integer, String> fileHeaderPositions
  ) {
    IndexRecord indexRecord = new IndexRecordImpl(rowNumber);
    for (int i = 0; i < nextRow.length; i++) {
      if (nextRow[i] != null && !nextRow[i].isEmpty()) {
        IndexEntry indexEntry = createIndexEntry(
          fileHeaderPositions.get(i),
          nextRow[i]
        );
        indexRecord.addIndexEntry(indexEntry);
      }
    }
    return indexRecord;
  }

  private IndexEntry createIndexEntry(String headerColumn, String value) {
    if (IndexHeaderColumn.NAME.equals(headerColumn)) {
      String path = normalizePath(value);
      return new IndexEntryImpl(IndexHeaderColumn.NAME, path);
    }
    return new IndexEntryImpl(headerColumn, value);
  }

  private String normalizePath(String path) {
    if (File.separatorChar == '/') {
      path = path.replace('\\', File.separatorChar);
    } else {
      path = path.replace('/', File.separatorChar);
    }
    if (!path.startsWith(File.separator)) {
      path = File.separator + path;
    }
    return path;
  }

  private boolean isEmptyLine(final String[] nextRow) {
    for (String aNextRow : nextRow) {
      if (aNextRow != null && !aNextRow.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private IndexHeaders getAllHeaders() {
    final IndexHeaders localIndexHeaders = new IndexHeadersImpl();
    localIndexHeaders.addHeader(indexHeaders.getHeader(IndexHeaderColumn.NAME));
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.TITLE)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.DESCRIPTION)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.AUTHOR)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.KEYWORDS)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.STATUS)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.ISSUE_DATE)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.REFERENCE)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.EXPIRATION_DATE)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.SECURITY_RANKING)
    );

    for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
      localIndexHeaders.addHeader(
        indexHeaders.getHeader(
          IndexHeaderColumn.ATTRIPREFIX + String.valueOf(i + 1)
        )
      );
    }
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.TYPE_DOCUMENT)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.TRANSLATOR)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.DOC_LANG)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.NO_CONTENT)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.ORI_LANG)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.REL_TRANS)
    );
    localIndexHeaders.addHeader(
      indexHeaders.getHeader(IndexHeaderColumn.OVERWRITE)
    );
    return localIndexHeaders;
  }

  /**
   * Writes the given records to the specified index file as a tab-delimited, UTF-8 encoded file
   * using the full canonical column layout (see {@link #getAllHeaders()}). Any existing content of
   * the file is overwritten. Missing or empty entry values are written as empty cells.
   *
   * @param indexFile the destination file to (over)write
   * @param indexRecords the records to serialise into the index file
   * @throws IOException if the file cannot be written
   */
  @SuppressWarnings("java:S2093") // CSVWriter doesn't implement AutoCloseable
  public void generateIndexRecords(
    final File indexFile,
    final List<IndexRecord> indexRecords
  ) throws IOException {
    CSVWriter writer = null;
    try {
      final IndexHeaders localIndexHeaders = getAllHeaders();

      final FileOutputStream fos = new FileOutputStream(indexFile, false);
      final OutputStreamWriter eosw = new OutputStreamWriter(
        fos,
        StandardCharsets.UTF_8
      );
      final BufferedWriter bw = new BufferedWriter(eosw, 4096);
      writer = new CSVWriter(
        bw,
        '\t',
        CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.NO_ESCAPE_CHARACTER,
        "\r\n"
      );

      final String[] headers = new String[localIndexHeaders
        .getHeaders()
        .size()];

      int i = 0;
      for (final IndexHeader indexHeader : localIndexHeaders.getHeaders()) {
        headers[i++] = indexHeader.getHeaderName();
      }

      writer.writeNext(headers);

      IndexEntry indexEntry;
      final String[] indexEntriesValues = new String[localIndexHeaders
        .getHeaders()
        .size()];
      for (final IndexRecord indexRecord : indexRecords) {
        i = 0;
        for (final IndexHeader indexHeader : localIndexHeaders.getHeaders()) {
          indexEntry = indexRecord.getEntry(indexHeader.getHeaderName());
          if (
            indexEntry == null ||
            indexEntry.getValue() == null ||
            indexEntry.getValue().isEmpty()
          ) {
            indexEntriesValues[i++] = "";
          } else {
            indexEntriesValues[i++] = indexEntry.getValue();
          }
        }
        writer.writeNext(indexEntriesValues);
      }
    } finally {
      if (writer != null) {
        writer.flush();
        writer.close();
      }
    }
  }

  private void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (VALIDATORS.isEmpty()) {
      VALIDATORS.add(new NameValidator(serviceRegistry));
      VALIDATORS.add(new StatusValidator(serviceRegistry));
      VALIDATORS.add(new IssueDateValidator(serviceRegistry));
      VALIDATORS.add(new ExpirDateValidator(serviceRegistry));
      VALIDATORS.add(new SecurityRankingValidator(serviceRegistry));
      VALIDATORS.add(new LangValidator(serviceRegistry));
      VALIDATORS.add(new NoContentValidator(serviceRegistry));
      VALIDATORS.add(new OriLangValidator(serviceRegistry));
      VALIDATORS.add(new OverwriteValidator(serviceRegistry));
    }
    for (final IndexValidator indexValidator : VALIDATORS) {
      indexValidator.validate(indexRecord, messages);
    }
  }

  /**
   * @return the serviceRegistry
   */
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  /**
   * @param serviceRegistry the serviceRegistry to set
   */
  public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
