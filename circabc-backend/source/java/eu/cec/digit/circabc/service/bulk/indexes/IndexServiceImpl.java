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
package eu.cec.digit.circabc.service.bulk.indexes;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import eu.cec.digit.circabc.service.bulk.validation.validator.*;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.*;
import java.util.*;

public class IndexServiceImpl implements IndexService {

    static final String[] HEADERS =
            new String[]{
                    Headers.NAME,
                    Headers.TITLE,
                    Headers.DESCRIPTION,
                    Headers.AUTHOR,
                    Headers.KEYWORDS,
                    Headers.STATUS,
                    Headers.ISSUE_DATE,
                    Headers.REFERENCE,
                    Headers.EXPIRATION_DATE,
                    Headers.SECURITY_RANKING,
                    Headers.ATTRI1,
                    Headers.ATTRI2,
                    Headers.ATTRI3,
                    Headers.ATTRI4,
                    Headers.ATTRI5,
                    Headers.ATTRI6,
                    Headers.ATTRI7,
                    Headers.ATTRI8,
                    Headers.ATTRI9,
                    Headers.ATTRI10,
                    Headers.ATTRI11,
                    Headers.ATTRI12,
                    Headers.ATTRI13,
                    Headers.ATTRI14,
                    Headers.ATTRI15,
                    Headers.ATTRI16,
                    Headers.ATTRI17,
                    Headers.ATTRI18,
                    Headers.ATTRI19,
                    Headers.ATTRI20,
                    Headers.TYPE_DOCUMENT,
                    Headers.TRANSLATOR,
                    Headers.DOC_LANG,
                    Headers.NO_CONTENT,
                    Headers.ORI_LANG,
                    Headers.REL_TRANS,
                    Headers.OVERWRITE
            };
    public static final List<String> HEADERS_LIST = Arrays.asList(IndexServiceImpl.HEADERS);
    private static final Log logger = LogFactory.getLog(IndexServiceImpl.class);
    private static final List<IndexValidator> VALIDATORS = new ArrayList<>();
    private final IndexHeaders indexHeaders = new IndexHeadersImpl(HEADERS);
    private ServiceRegistry serviceRegistry;

    public IndexServiceImpl() {
    }

    public IndexHeaders getIndexHeaders() {
        return indexHeaders;
    }

    public void getIndexRecords(
            final File indexFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages)
            throws IOException {

        final CSVReader reader = new CSVReader(new FileReader(indexFile), '\t');
        String[] headerRow;
        String[] nextRow;
        int rowNumber = 0;

        final Map<Integer, String> fileHeaderPositions = new HashMap<>();

        if ((headerRow = reader.readNext()) != null) {
            rowNumber++;
            if (logger.isDebugEnabled()) {
                logger.debug("IndexFile Header Configuration:");
            }
            for (int i = 0; i < headerRow.length; i++) {
                fileHeaderPositions.put(i, headerRow[i]);
                if (logger.isDebugEnabled()) {
                    logger.debug("Line:" + rowNumber + " Column:" + (i + 1) + " HeaderName:" + headerRow[i]);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("IndexFile Records:");
            }
            IndexEntry indexEntry;
            IndexRecord indexRecord;
            while ((nextRow = reader.readNext()) != null) {
                rowNumber++;

                if (isEmptyLine(nextRow)) {
                    final String ERROR_DESCRIPTION = "bulk_upload_empty_row";
                    final String fileName = IndexService.INDEX_FILE;
                    messages.add(
                            new ValidationMessageImpl(
                                    rowNumber, fileName, I18NUtil.getMessage(ERROR_DESCRIPTION), ErrorType.Warning));
                    continue;
                }
                indexRecord = new IndexRecordImpl(rowNumber);
                // nextRow[] is an array of values from the line
                for (int i = 0; i < nextRow.length; i++) {

                    // new IndexEntryImpl(HeaderName, value);
                    if (nextRow[i] != null && nextRow[i].length() != 0) {
                        if (Headers.NAME.equals(fileHeaderPositions.get(i))) {
                            String path = nextRow[i];
                            if (File.separatorChar == '/') {
                                path = path.replace('\\', File.separatorChar);
                            } else {
                                path = path.replace('/', File.separatorChar);
                            }
                            if (!path.startsWith(File.separator)) {
                                path = File.separator + path;
                            }
                            indexEntry = new IndexEntryImpl(Headers.NAME, path);
                        } else {
                            indexEntry = new IndexEntryImpl(fileHeaderPositions.get(i), nextRow[i]);

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Line:"
                                                + rowNumber
                                                + " Column:"
                                                + (i + 1)
                                                + " HeaderName:"
                                                + headerRow[i]
                                                + " Value:"
                                                + nextRow[i]);
                            }
                        }
                        indexRecord.addIndexEntry(indexEntry);
                    }
                }
                validate(rowNumber, indexRecord, messages);
                indexRecords.add(indexRecord);
            }
        }
    }

    private boolean isEmptyLine(final String[] nextRow) {
        for (String aNextRow : nextRow) {
            if (aNextRow != null && aNextRow.length() > 0) {
                return false;
            }
        }
        return true;
    }

    private IndexHeaders getAllHeaders() {
        final IndexHeaders localIndexHeaders = new IndexHeadersImpl();
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.NAME));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.TITLE));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.DESCRIPTION));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.AUTHOR));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.KEYWORDS));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.STATUS));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.ISSUE_DATE));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.REFERENCE));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.EXPIRATION_DATE));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.SECURITY_RANKING));

        for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
            localIndexHeaders.addHeader(
                    indexHeaders.getHeader(Headers.ATTRIPREFIX + String.valueOf(i + 1)));
        }
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.TYPE_DOCUMENT));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.TRANSLATOR));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.DOC_LANG));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.NO_CONTENT));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.ORI_LANG));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.REL_TRANS));
        localIndexHeaders.addHeader(indexHeaders.getHeader(Headers.OVERWRITE));
        return localIndexHeaders;
    }

    public void generateIndexRecords(final File indexFile, final List<IndexRecord> indexRecords)
            throws IOException {

        CSVWriter writer = null;
        try {
            // final IndexHeaders indexHeaders = getHeaders(indexRecords);
            // Correction of bug DIGIT-CIRCABC-747
            final IndexHeaders indexHeaders = getAllHeaders();

            final FileOutputStream fos = new FileOutputStream(indexFile, false /* append */);
            final OutputStreamWriter eosw = new OutputStreamWriter(fos, "UTF-8");
            final BufferedWriter bw = new BufferedWriter(eosw, 4096 /* buffsize in chars */);
            writer =
                    new CSVWriter(
                            bw, '\t', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, "\r\n");

            final String[] headers = new String[indexHeaders.getHeaders().size()];

            int i = 0;
            final StringBuilder allHeaders = new StringBuilder();
            for (final IndexHeader indexHeader : indexHeaders.getHeaders()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Headers:" + i + " " + indexHeader.getHeaderName());
                }
                headers[i++] = indexHeader.getHeaderName();
            }
            if (logger.isDebugEnabled()) {
                for (int j = 0; j < headers.length; j++) {
                    if (j != 0) {
                        allHeaders.append("|");
                    }
                    allHeaders.append(headers[j]);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("IndexFile Headers:" + allHeaders.toString());
                }
            }
            writer.writeNext(headers);

            IndexEntry indexEntry;
            final String[] indexEntriesValues = new String[indexHeaders.getHeaders().size()];
            for (final IndexRecord indexRecord : indexRecords) {
                final StringBuilder allValues = new StringBuilder();
                i = 0;
                for (final IndexHeader indexHeader : indexHeaders.getHeaders()) {
                    indexEntry = indexRecord.getEntry(indexHeader.getHeaderName());
                    if (indexEntry == null
                            || indexEntry.getValue() == null
                            || indexEntry.getValue().length() == 0) {
                        indexEntriesValues[i++] = "";
                        if (logger.isDebugEnabled()) {
                            logger.debug("No value found for header:" + indexHeader.getHeaderName());
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Value "
                                            + indexEntry.getValue()
                                            + " found for header:"
                                            + indexHeader.getHeaderName());
                        }
                        indexEntriesValues[i++] = indexEntry.getValue();
                    }
                }
                if (logger.isDebugEnabled()) {
                    for (int j = 0; j < indexEntriesValues.length; j++) {
                        if (j != 0) {
                            allValues.append("|");
                        }
                        allValues.append(indexEntriesValues[j]);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("IndexFile line(" + i + "):" + allValues.toString());
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
            final int rowNumber, final IndexRecord indexRecord, final List<ValidationMessage> messages) {
        if (VALIDATORS.size() == 0) {
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
