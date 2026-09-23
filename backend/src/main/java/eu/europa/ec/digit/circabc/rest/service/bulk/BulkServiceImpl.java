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
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecordImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexService;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessageImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.upload.UploadedEntry;
import eu.europa.ec.digit.circabc.rest.service.bulk.upload.UploadedEntryImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import eu.europa.ec.digit.circabc.rest.service.compress.CompressedEntry;
import eu.europa.ec.digit.circabc.rest.service.compress.ZipService;
import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordImpl;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Default implementation of {@link BulkService}, the service that powers CIRCABC's bulk
 * upload/download feature for the document Library.
 *
 * <p>On the upload side it reads a compressed (ZIP) archive together with an optional {@code index}
 * file, extracts the archive content into an Interest Group container, and applies the metadata
 * declared in the index (title, description, keywords, status, dates, references, security ranking,
 * dynamic properties and multilingual relationships) to the newly created nodes. Multilingual
 * documents are handled by processing entries in a strict order: non-multilingual documents first,
 * then pivot (original language) documents, then translations that carry content, and finally empty
 * translations.
 *
 * <p>On the download/metadata side it walks the node tree of a set of {@link NodeRef}s and produces
 * the {@link IndexRecord} entries that describe each content item so they can be exported into an
 * index file.
 *
 * <p>Collaborating Alfresco and CIRCABC services are wired through Spring using setter injection
 * (see the IOC getters/setters at the end of the class).
 *
 * <p>Migration 3.1 -&gt; 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC.
 */
public class BulkServiceImpl implements BulkService {

  /** Message key used when an index entry references a file that is missing from the archive. */
  private static final String FILE_REFERENCED_NOT_PRESENT_IN_ZIP =
    "file_referenced_not_present_in_zip";

  private static final Log logger = LogFactory.getLog(BulkServiceImpl.class);

  /** Formatter used to parse/format the index file dates, based on {@code INDEX_DATE_FORMAT}. */
  private final DateFormat sdf = new SimpleDateFormat(INDEX_DATE_FORMAT);

  private static final String TEMP_FILE_PREFIX = "circabc_";
  private static final String TEMP_FILE_SUFFIX = ".temp";

  /** Cache of resolved type definitions, keyed by type {@link QName}, to avoid repeated lookups. */
  private final Map<QName, TypeDefinition> validTypeMap = new HashMap<>();
  /**
   * IOC
   */
  private FileFolderService fileFolderService;
  private NodeService nodeService;
  private ContentService contentService;
  private ZipService zipService;
  private IndexService indexService;
  private KeywordsService keywordsService;
  private ApiToolBox apiToolBox;
  private ContentFilterLanguagesService contentFilterLanguagesService;
  private MultilingualContentService multilingualContentService;
  private DictionaryService dictionaryService;
  private PermissionService permissionService;

  /**
   * Lists the entries contained in a compressed archive without extracting them into the
   * repository.
   *
   * @param containerNodeRef the target container node (currently unused by this implementation, but
   *     part of the {@link BulkService} contract)
   * @param compressedFile the compressed (ZIP) file to inspect
   * @param messages collector for validation/warning messages produced while reading the archive
   * @return the list of {@link CompressedEntry} found in the archive
   */
  public List<CompressedEntry> getCompressedEntries(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<ValidationMessage> messages
  ) {
    return zipService.getCompressedEntries(compressedFile, messages);
  }

  /**
   * Extract "index" from the compressed and create a List of entries "IndexRecord"
   *
   * <p>Extracts the index file (if present) from the archive into a temporary file, parses it into
   * {@link IndexRecord} entries and then deletes the temporary file. When no index file is present a
   * warning message is added and an empty list is returned.
   *
   * @param compressedFile the compressed (ZIP) file that may contain an index file
   * @param messages collector for validation/warning messages produced while reading the index
   * @return the list of parsed {@link IndexRecord} entries; empty when no index file is present
   * @throws IOException if the temporary index file cannot be created or read
   */
  public List<IndexRecord> getIndexRecords(
    final File compressedFile,
    final List<ValidationMessage> messages
  ) throws IOException {
    final List<IndexRecord> indexRecords = new ArrayList<>();
    File temporaryIndexFile = null;
    try {
      // Step 0: extract index file to temporary file
      // unfortunately a ZIP file can not be read directly from an input
      // stream so we have to create
      // a temporary file first
      temporaryIndexFile = TempFileProvider.createTempFile(
        TEMP_FILE_PREFIX,
        TEMP_FILE_SUFFIX
      );

      // Step 1: Extract index file from the compressed file (if index
      // file exist)
      final boolean exist = zipService.extract(
        compressedFile,
        IndexService.INDEX_FILE,
        temporaryIndexFile,
        messages
      );

      if (exist) {
        // Extract informations from this index file
        indexService.getIndexRecords(
          temporaryIndexFile,
          indexRecords,
          messages
        );
      } else {
        messages.add(
          new ValidationMessageImpl(
            0,
            "",
            "No index file provided",
            ErrorType.Warning
          )
        );
      }
    } finally {
      // now the creationOfIndexEntries is done, delete the temporary file
      if (temporaryIndexFile != null) {
        try {
          java.nio.file.Files.delete(temporaryIndexFile.toPath());
        } catch (IOException e) {
          if (logger.isWarnEnabled()) {
            logger.warn(
              "Unable to delete file : " + temporaryIndexFile.getPath(),
              e
            );
          }
        }
      }
    }
    return indexRecords;
  }

  /**
   * Extracts and uploads the content of a compressed archive into the given container, without
   * applying any index-driven metadata.
   *
   * <p>Convenience overload equivalent to calling {@link #upload(NodeRef, File, List, List)} with an
   * empty list of index records.
   *
   * @param containerNodeRef the container node into which the archive content is uploaded
   * @param compressedFile the compressed (ZIP) file to extract and upload
   * @param messages collector for validation/warning messages produced during the upload
   * @return the list of {@link UploadedEntry} describing the created nodes
   */
  public List<UploadedEntry> upload(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<ValidationMessage> messages
  ) {
    return upload(
      containerNodeRef,
      compressedFile,
      Collections.<IndexRecord>emptyList(),
      messages
    );
  }

  /**
   * Extracts a compressed archive into the given container and applies the metadata declared in the
   * supplied index records to the uploaded nodes.
   *
   * <p>The index file itself is excluded from extraction. Extracted files are processed in a strict
   * order so that multilingual relationships can be resolved correctly: non-multilingual documents
   * first, then pivot (original language) documents, then translations carrying content, and finally
   * empty translations declared only in the index.
   *
   * @param containerNodeRef the container node into which the archive content is uploaded
   * @param compressedFile the compressed (ZIP) file to extract and upload
   * @param indexRecords the index records describing the metadata to apply to each file
   * @param messages collector for validation/warning messages produced during the upload
   * @return the list of {@link UploadedEntry} describing the created nodes
   */
  public List<UploadedEntry> upload(
    final NodeRef containerNodeRef,
    final File compressedFile,
    final List<IndexRecord> indexRecords,
    final List<ValidationMessage> messages
  ) {
    final List<UploadedEntry> uploadedEntries = new ArrayList<>();
    final List<String> excludedFileName = new ArrayList<>(1);
    excludedFileName.add(File.separatorChar + IndexService.INDEX_FILE);
    final NodeRef libraryNodeRef = apiToolBox.getCurrentLibraryRoot(
      containerNodeRef
    );
    final Map<String, NodeRef> extractedFiles = zipService.extract(
      libraryNodeRef,
      containerNodeRef,
      compressedFile,
      excludedFileName,
      indexRecords,
      messages
    );

    // Process in order: non-ML docs, pivot docs, translations, empty translations
    processNonMultilingualDocs(
      extractedFiles,
      indexRecords,
      libraryNodeRef,
      containerNodeRef,
      messages,
      uploadedEntries
    );
    processPivotDocs(
      extractedFiles,
      indexRecords,
      libraryNodeRef,
      containerNodeRef,
      messages,
      uploadedEntries
    );
    processTranslationsWithContent(
      extractedFiles,
      indexRecords,
      libraryNodeRef,
      containerNodeRef,
      messages,
      uploadedEntries
    );
    processEmptyTranslations(
      indexRecords,
      libraryNodeRef,
      containerNodeRef,
      messages,
      uploadedEntries
    );

    return uploadedEntries;
  }

  private void processNonMultilingualDocs(
    final Map<String, NodeRef> extractedFiles,
    final List<IndexRecord> indexRecords,
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final List<ValidationMessage> messages,
    final List<UploadedEntry> uploadedEntries
  ) {
    for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
      IndexRecord indexRecord = getIndexRecord(indexRecords, entry.getKey());
      if (indexRecord != null && indexRecord.getOriLang() == null) {
        processExtractedFile(
          entry,
          indexRecord,
          libraryNodeRef,
          containerNodeRef,
          messages,
          uploadedEntries
        );
      }
    }
  }

  private void processPivotDocs(
    final Map<String, NodeRef> extractedFiles,
    final List<IndexRecord> indexRecords,
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final List<ValidationMessage> messages,
    final List<UploadedEntry> uploadedEntries
  ) {
    for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
      IndexRecord indexRecord = getIndexRecord(indexRecords, entry.getKey());
      if (
        indexRecord != null && "Y".equalsIgnoreCase(indexRecord.getOriLang())
      ) {
        processExtractedFile(
          entry,
          indexRecord,
          libraryNodeRef,
          containerNodeRef,
          messages,
          uploadedEntries
        );
      }
    }
  }

  private void processTranslationsWithContent(
    final Map<String, NodeRef> extractedFiles,
    final List<IndexRecord> indexRecords,
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final List<ValidationMessage> messages,
    final List<UploadedEntry> uploadedEntries
  ) {
    for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
      IndexRecord indexRecord = getIndexRecord(indexRecords, entry.getKey());
      if (
        indexRecord != null &&
        indexRecord.getOriLang() != null &&
        !"Y".equalsIgnoreCase(indexRecord.getOriLang())
      ) {
        processExtractedFile(
          entry,
          indexRecord,
          libraryNodeRef,
          containerNodeRef,
          messages,
          uploadedEntries
        );
      }
    }
  }

  private void processExtractedFile(
    final Map.Entry<String, NodeRef> entry,
    final IndexRecord indexRecord,
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final List<ValidationMessage> messages,
    final List<UploadedEntry> uploadedEntries
  ) {
    String fileName = (String) nodeService.getProperty(
      entry.getValue(),
      ContentModel.PROP_NAME
    );
    setMetaDatas(
      libraryNodeRef,
      containerNodeRef,
      entry.getValue(),
      indexRecord,
      fileName,
      messages
    );
    uploadedEntries.add(new UploadedEntryImpl(fileName, entry.getKey()));
  }

  private void processEmptyTranslations(
    final List<IndexRecord> indexRecords,
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final List<ValidationMessage> messages,
    final List<UploadedEntry> uploadedEntries
  ) {
    for (final IndexRecord indexRecord : indexRecords) {
      if (
        indexRecord != null && "Y".equalsIgnoreCase(indexRecord.getNoContent())
      ) {
        String filePath = indexRecord.getName();
        String fileName = new File(filePath).getName();
        setMetaDatas(
          libraryNodeRef,
          containerNodeRef,
          null,
          indexRecord,
          fileName,
          messages
        );
        uploadedEntries.add(new UploadedEntryImpl(fileName, filePath));
      }
    }
  }

  private void setMetaDatas(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final NodeRef nodeRef,
    final IndexRecord indexRecord,
    final String fileName,
    final List<ValidationMessage> messages
  ) {
    if (indexRecord == null) {
      return;
    }

    NodeRef currentNode = nodeRef;
    if (nodeRef == null) {
      currentNode = resolveNodeForNullRef(
        libraryNodeRef,
        containerNodeRef,
        indexRecord,
        fileName,
        messages
      );
    }

    if (currentNode == null) {
      return;
    }

    Map<QName, Serializable> nodeProperties = nodeService.getProperties(
      currentNode
    );
    applyAllMetadata(
      libraryNodeRef,
      containerNodeRef,
      currentNode,
      nodeProperties,
      indexRecord,
      fileName,
      messages
    );
  }

  private NodeRef resolveNodeForNullRef(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final IndexRecord indexRecord,
    final String fileName,
    final List<ValidationMessage> messages
  ) {
    final String library = (String) nodeService.getProperty(
      libraryNodeRef,
      ContentModel.PROP_NAME
    );

    if ("Y".equalsIgnoreCase(indexRecord.getNoContent())) {
      return handleEmptyTranslation(
        libraryNodeRef,
        containerNodeRef,
        library,
        indexRecord,
        fileName,
        messages
      );
    }
    return findExistingNode(
      libraryNodeRef,
      containerNodeRef,
      library,
      indexRecord,
      fileName,
      messages
    );
  }

  private NodeRef handleEmptyTranslation(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final String library,
    final IndexRecord indexRecord,
    final String fileName,
    final List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getRelTrans() == null || indexRecord.getRelTrans().isEmpty()
    ) {
      addValidationError(
        messages,
        indexRecord,
        fileName,
        "bulk_upload_invalid_translation_multi_lingual_doc"
      );
      return null;
    }

    NodeRef relatedTranslationNodeRef = findRelatedTranslation(
      libraryNodeRef,
      containerNodeRef,
      library,
      indexRecord.getRelTrans()
    );
    if (relatedTranslationNodeRef == null) {
      addValidationError(
        messages,
        indexRecord,
        fileName,
        "bulk_upload_invalid_translation_empty_multi_lingual_doc"
      );
      return null;
    }

    try {
      return multilingualContentService.addEmptyTranslation(
        relatedTranslationNodeRef,
        getFileName(indexRecord.getName()),
        I18NUtil.parseLocale(indexRecord.getDocLang())
      );
    } catch (final FileExistsException ex) {
      addValidationError(
        messages,
        indexRecord,
        fileName,
        "bulk_upload_existing_empty_translation"
      );
      return null;
    }
  }

  private NodeRef findRelatedTranslation(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final String library,
    final String relatedTranslation
  ) {
    if (
      relatedTranslation.startsWith(library) ||
      relatedTranslation.startsWith(File.separatorChar + library)
    ) {
      return searchFile(libraryNodeRef, relatedTranslation);
    }
    return searchFile(containerNodeRef, relatedTranslation);
  }

  private NodeRef findExistingNode(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final String library,
    final IndexRecord indexRecord,
    final String fileName,
    final List<ValidationMessage> messages
  ) {
    final String path = indexRecord.getName();
    NodeRef currentNode;
    if (
      path.startsWith(library) || path.startsWith(File.separatorChar + library)
    ) {
      currentNode = searchFile(libraryNodeRef, path);
    } else {
      currentNode = searchFile(containerNodeRef, path);
    }
    if (currentNode == null) {
      addValidationError(
        messages,
        indexRecord,
        fileName,
        "bulk_upload_file_not_exist_either_in_zip_and_in_repository"
      );
    }
    return currentNode;
  }

  private void addValidationError(
    final List<ValidationMessage> messages,
    final IndexRecord indexRecord,
    final String fileName,
    final String errorKey
  ) {
    messages.add(
      new ValidationMessageImpl(
        indexRecord.getRowNumber(),
        fileName,
        I18NUtil.getMessage(errorKey),
        ErrorType.Fatal
      )
    );
  }

  private void applyAllMetadata(
    final NodeRef libraryNodeRef,
    final NodeRef containerNodeRef,
    final NodeRef currentNode,
    final Map<QName, Serializable> nodeProperties,
    final IndexRecord indexRecord,
    final String fileName,
    final List<ValidationMessage> messages
  ) {
    setPropertyIfNotEmpty(
      currentNode,
      fileName,
      nodeProperties,
      ContentModel.PROP_TITLE,
      indexRecord.getTitle(),
      indexRecord,
      messages
    );
    setPropertyIfNotEmpty(
      currentNode,
      fileName,
      nodeProperties,
      ContentModel.PROP_DESCRIPTION,
      indexRecord.getDescription(),
      indexRecord,
      messages
    );
    processKeywords(currentNode, fileName, indexRecord, messages);
    processStatus(currentNode, fileName, nodeProperties, indexRecord, messages);
    processIssueDate(
      currentNode,
      fileName,
      nodeProperties,
      indexRecord,
      messages
    );
    setPropertyIfNotEmpty(
      currentNode,
      fileName,
      nodeProperties,
      DocumentModel.PROP_REFERENCE,
      indexRecord.getReference(),
      indexRecord,
      messages
    );
    processSecurityRanking(
      currentNode,
      fileName,
      nodeProperties,
      indexRecord,
      messages
    );
    processDynamicProperties(
      currentNode,
      fileName,
      nodeProperties,
      indexRecord,
      messages
    );

    NodeRef pivotNode = processDocLang(
      currentNode,
      fileName,
      indexRecord,
      messages
    );
    NodeRef relatedTranslationNodeRef = processRelatedTranslation(
      libraryNodeRef,
      containerNodeRef,
      currentNode,
      fileName,
      indexRecord,
      messages
    );

    processAuthor(
      currentNode,
      pivotNode,
      fileName,
      nodeProperties,
      indexRecord,
      messages
    );
    processTranslator(
      currentNode,
      relatedTranslationNodeRef,
      fileName,
      indexRecord,
      messages
    );
    processExpirationDate(
      currentNode,
      pivotNode,
      fileName,
      nodeProperties,
      indexRecord,
      messages
    );
  }

  private void setPropertiesGeneric(
    final NodeRef nodeRef,
    final String fileName,
    final Map<QName, Serializable> nodeProperties,
    final QName key,
    final Serializable value,
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (value == null || value.equals("")) {
      return;
    }
    final Serializable oldValue = nodeProperties.get(key);
    if (
      oldValue != null &&
      !oldValue.toString().isEmpty() &&
      logger.isTraceEnabled()
    ) {
      logger.trace("oldValue:" + oldValue + " new value:" + value);
      if (oldValue.equals(value)) {
        logger.trace("value are similar");
      }
    }
    try {
      if (logger.isTraceEnabled()) {
        logger.trace(
          "set new value:" +
            value +
            " on noderef:" +
            nodeRef +
            " that represent the file:" +
            fileName
        );
      }
      nodeService.setProperty(nodeRef, key, value);
    } catch (final Exception ex) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          "Error while setting property " +
            key +
            " with value:" +
            value +
            " on file:" +
            fileName +
            " cause:" +
            ex.getMessage(),
          ErrorType.Fatal
        )
      );
    }
  }

  private void setPropertyIfNotEmpty(
    NodeRef nodeRef,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    QName prop,
    String value,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (value != null && !value.isEmpty()) {
      setPropertiesGeneric(
        nodeRef,
        fileName,
        nodeProperties,
        prop,
        value,
        indexRecord,
        messages
      );
    }
  }

  private void processKeywords(
    NodeRef currentNode,
    String fileName,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getKeywords() == null || indexRecord.getKeywords().isEmpty()
    ) {
      return;
    }
    final String[] keywords = indexRecord.getKeywords().split(";");
    final NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(currentNode);
    for (final String langKeywordValue : keywords) {
      int open = langKeywordValue.indexOf("(");
      int close = langKeywordValue.indexOf(")");
      if (open == -1 || close == -1) {
        messages.add(
          new ValidationMessageImpl(
            indexRecord.getRowNumber(),
            fileName,
            I18NUtil.getMessage("bulk_upload_param_keyword") +
              " " +
              langKeywordValue,
            ErrorType.Warning
          )
        );
        continue;
      }
      String localeValue = langKeywordValue.substring(open + 1, open + 3);
      String keywordValue = langKeywordValue.substring(close + 1);
      Locale locale = I18NUtil.parseLocale(localeValue);
      Keyword keyword = new KeywordImpl(locale, keywordValue);
      for (Keyword item : keywordsService.getKeywords(igNodeRef)) {
        if (item.exists(locale, keywordValue)) {
          keyword = item;
          break;
        }
      }
      if (!keywordsService.exists(keyword)) {
        keyword = keywordsService.createKeyword(igNodeRef, keyword);
      }
      if (
        !nodeService.hasAspect(currentNode, DocumentModel.ASPECT_CPROPERTIES)
      ) {
        nodeService.addAspect(
          currentNode,
          DocumentModel.ASPECT_CPROPERTIES,
          null
        );
      }
      keywordsService.addKeywordToNode(currentNode, keyword);
    }
  }

  private void processStatus(
    NodeRef currentNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (indexRecord.getStatus() == null || indexRecord.getStatus().isEmpty()) {
      return;
    }
    boolean valid = DocumentModel.STATUS_VALUES.contains(
      indexRecord.getStatus()
    );
    if (valid) {
      setPropertiesGeneric(
        currentNode,
        fileName,
        nodeProperties,
        DocumentModel.PROP_STATUS,
        indexRecord.getStatus(),
        indexRecord,
        messages
      );
    } else {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage("bulk_upload_param_doc_status"),
          ErrorType.Fatal
        )
      );
    }
  }

  private void processIssueDate(
    NodeRef currentNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getIssueDate() == null || indexRecord.getIssueDate().isEmpty()
    ) {
      return;
    }
    try {
      final Date parsed = sdf.parse(indexRecord.getIssueDate());
      setPropertiesGeneric(
        currentNode,
        fileName,
        nodeProperties,
        DocumentModel.PROP_ISSUE_DATE,
        parsed,
        indexRecord,
        messages
      );
    } catch (final ParseException pe) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage("bulk_upload_issue_date_error"),
          ErrorType.Warning
        )
      );
    }
  }

  private void processSecurityRanking(
    NodeRef currentNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getSecurityRanking() == null ||
      indexRecord.getSecurityRanking().isEmpty()
    ) {
      return;
    }
    boolean valid = DocumentModel.SECURITY_RANKINGS.contains(
      indexRecord.getSecurityRanking()
    );
    if (valid) {
      setPropertiesGeneric(
        currentNode,
        fileName,
        nodeProperties,
        DocumentModel.PROP_SECURITY_RANKING,
        indexRecord.getSecurityRanking(),
        indexRecord,
        messages
      );
    } else {
      String errorMsg = I18NUtil.getMessage("bulk_upload_security_ranking");
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          errorMsg,
          ErrorType.Fatal
        )
      );
      if (logger.isErrorEnabled()) {
        logger.error(errorMsg + " value:" + indexRecord.getSecurityRanking());
      }
    }
  }

  private void processDynamicProperties(
    NodeRef currentNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
      QName item = DocumentModel.ALL_DYN_PROPS.get(i);
      String dynamicProperty = indexRecord.getDynamicProperty(i + 1);
      if (dynamicProperty != null && !dynamicProperty.isEmpty()) {
        setPropertiesGeneric(
          currentNode,
          fileName,
          nodeProperties,
          item,
          dynamicProperty,
          indexRecord,
          messages
        );
      }
    }
  }

  private NodeRef processDocLang(
    NodeRef currentNode,
    String fileName,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getDocLang() == null || indexRecord.getDocLang().isEmpty()
    ) {
      return null;
    }
    if (
      !contentFilterLanguagesService
        .getFilterLanguages()
        .contains(indexRecord.getDocLang())
    ) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage("bulk_upload_param_doc_lang"),
          ErrorType.Fatal
        )
      );
      return null;
    }
    if (
      indexRecord.getOriLang() != null &&
      indexRecord.getOriLang().equalsIgnoreCase("Y")
    ) {
      multilingualContentService.makeTranslation(
        currentNode,
        I18NUtil.parseLocale(indexRecord.getDocLang())
      );
      return currentNode;
    }
    if (
      indexRecord.getRelTrans() == null || indexRecord.getRelTrans().isEmpty()
    ) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage(
            "bulk_upload_param_doc_lang_on_a_document_not_translated"
          ),
          ErrorType.Warning
        )
      );
    }
    return null;
  }

  private NodeRef processRelatedTranslation(
    NodeRef libraryNodeRef,
    NodeRef containerNodeRef,
    NodeRef currentNode,
    String fileName,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getRelTrans() == null || indexRecord.getRelTrans().isEmpty()
    ) {
      return null;
    }
    String library = (String) nodeService.getProperty(
      libraryNodeRef,
      ContentModel.PROP_NAME
    );
    String relatedTranslation = indexRecord.getRelTrans();
    NodeRef relatedTranslationNodeRef =
      relatedTranslation.startsWith(library) ||
      relatedTranslation.startsWith(File.separatorChar + library)
        ? searchFile(libraryNodeRef, relatedTranslation)
        : searchFile(containerNodeRef, relatedTranslation);
    if (relatedTranslationNodeRef == null) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage(
            "bulk_upload_invalid_translation_multi_lingual_doc"
          ),
          ErrorType.Fatal
        )
      );
      return null;
    }
    boolean isNoContent =
      indexRecord.getNoContent() != null &&
      indexRecord.getNoContent().equals("Y");
    if (!isNoContent && currentNode != null) {
      multilingualContentService.addTranslation(
        currentNode,
        relatedTranslationNodeRef,
        I18NUtil.parseLocale(indexRecord.getDocLang())
      );
    }
    return relatedTranslationNodeRef;
  }

  private void processAuthor(
    NodeRef currentNode,
    NodeRef pivotNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (indexRecord.getAuthor() == null || indexRecord.getAuthor().isEmpty()) {
      return;
    }
    if (currentNode == pivotNode && pivotNode != null) {
      NodeRef logicalMlDocument =
        multilingualContentService.getTranslationContainer(pivotNode);
      setPropertiesGeneric(
        logicalMlDocument,
        fileName,
        nodeProperties,
        ContentModel.PROP_AUTHOR,
        indexRecord.getAuthor(),
        indexRecord,
        messages
      );
    } else {
      setPropertiesGeneric(
        currentNode,
        fileName,
        nodeProperties,
        ContentModel.PROP_AUTHOR,
        indexRecord.getAuthor(),
        indexRecord,
        messages
      );
    }
  }

  private void processTranslator(
    NodeRef currentNode,
    NodeRef relatedTranslationNodeRef,
    String fileName,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getTranslator() == null ||
      indexRecord.getTranslator().isEmpty()
    ) {
      return;
    }
    if (relatedTranslationNodeRef != null) {
      if (currentNode == null) {
        logger.debug("Empty translation case");
        return;
      }
      if (
        nodeService.hasAspect(
          relatedTranslationNodeRef,
          ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
        ) ||
        nodeService.hasAspect(
          relatedTranslationNodeRef,
          ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
        )
      ) {
        nodeService.setProperty(
          currentNode,
          ContentModel.PROP_AUTHOR,
          indexRecord.getTranslator()
        );
      } else {
        messages.add(
          new ValidationMessageImpl(
            indexRecord.getRowNumber(),
            fileName,
            I18NUtil.getMessage(
              "bulk_upload_add_translator_non_multi_lingual_doc"
            ),
            ErrorType.Fatal
          )
        );
      }
    } else if (
      indexRecord.getOriLang() == null ||
      !indexRecord.getOriLang().equalsIgnoreCase("Y")
    ) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage(
            "bulk_upload_add_translator_with_no_related_translation"
          ),
          ErrorType.Fatal
        )
      );
    }
  }

  private void processExpirationDate(
    NodeRef currentNode,
    NodeRef pivotNode,
    String fileName,
    Map<QName, Serializable> nodeProperties,
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getExpirationDate() == null ||
      indexRecord.getExpirationDate().isEmpty()
    ) {
      if (
        indexRecord.getOriLang() != null &&
        indexRecord.getOriLang().equalsIgnoreCase("Y")
      ) {
        messages.add(
          new ValidationMessageImpl(
            indexRecord.getRowNumber(),
            fileName,
            I18NUtil.getMessage("bulk_upload_mandatory_expir_date_error"),
            ErrorType.Warning
          )
        );
      }
      return;
    }
    try {
      Date parsed = sdf.parse(indexRecord.getExpirationDate());
      if (currentNode == pivotNode && pivotNode != null) {
        NodeRef logicalMlDocument =
          multilingualContentService.getTranslationContainer(pivotNode);
        setPropertiesGeneric(
          logicalMlDocument,
          fileName,
          nodeProperties,
          DocumentModel.PROP_EXPIRATION_DATE,
          parsed,
          indexRecord,
          messages
        );
      } else if (
        indexRecord.getRelTrans() != null &&
        !indexRecord.getRelTrans().isEmpty()
      ) {
        messages.add(
          new ValidationMessageImpl(
            indexRecord.getRowNumber(),
            fileName,
            I18NUtil.getMessage("bulk_upload_expir_date_error_on_translation"),
            ErrorType.Warning
          )
        );
      } else {
        setPropertiesGeneric(
          currentNode,
          fileName,
          nodeProperties,
          DocumentModel.PROP_EXPIRATION_DATE,
          parsed,
          indexRecord,
          messages
        );
      }
    } catch (ParseException pe) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          fileName,
          I18NUtil.getMessage("bulk_upload_expir_date_error"),
          ErrorType.Warning
        )
      );
    }
  }

  private List<NodeRef> getAllChildsNodeRefs(
    final List<NodeRef> allNodeRefs,
    final NodeRef nodeRef
  ) {
    final List<NodeRef> childsNodeRefs = new ArrayList<>();
    if (
      !nodeService.exists(nodeRef) ||
      !AccessStatus.ALLOWED.equals(
        permissionService.hasPermission(nodeRef, PermissionService.READ)
      )
    ) {
      return childsNodeRefs;
    }
    final QName type = nodeService.getType(nodeRef);
    TypeDefinition typeDef = validTypeMap.computeIfAbsent(
      type,
      dictionaryService::getType
    );
    if (typeDef == null) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Found invalid object in database: id = " +
            nodeRef +
            ", type = " +
            type
        );
      }
      return childsNodeRefs;
    }
    processNodeByType(nodeRef, type, childsNodeRefs, allNodeRefs);
    return childsNodeRefs;
  }

  private void processNodeByType(
    NodeRef nodeRef,
    QName type,
    List<NodeRef> childsNodeRefs,
    List<NodeRef> allNodeRefs
  ) {
    if (
      ContentModel.TYPE_CONTENT.equals(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)
    ) {
      processContentNode(nodeRef, childsNodeRefs, allNodeRefs);
    } else if (
      ContentModel.TYPE_FOLDER.equals(type) ||
      (dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) &&
        !dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER))
    ) {
      createFolderRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
    } else if (
      ApplicationModel.TYPE_FILELINK.equals(type) ||
      dictionaryService.isSubClass(type, ApplicationModel.TYPE_FILELINK)
    ) {
      createFileLinkRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
    }
    // Folder links are ignored to avoid infinite recursion
  }

  private void processContentNode(
    NodeRef nodeRef,
    List<NodeRef> childsNodeRefs,
    List<NodeRef> allNodeRefs
  ) {
    if (childsNodeRefs.contains(nodeRef) || allNodeRefs.contains(nodeRef)) {
      return;
    }
    if (!multilingualContentService.isTranslation(nodeRef)) {
      childsNodeRefs.add(nodeRef);
      return;
    }
    NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
    addIfNotPresent(pivot, childsNodeRefs, allNodeRefs);
    if (!nodeRef.equals(pivot)) {
      addIfNotPresent(nodeRef, childsNodeRefs, allNodeRefs);
    }
  }

  private void addIfNotPresent(
    NodeRef nodeRef,
    List<NodeRef> childsNodeRefs,
    List<NodeRef> allNodeRefs
  ) {
    if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
      childsNodeRefs.add(nodeRef);
    }
  }

  private void createFileLinkRepresentation(
    final NodeRef nodeRef,
    final List<NodeRef> childsNodeRefs,
    final List<NodeRef> allNodeRefs
  ) {
    // only display the user has the permissions to navigate to
    // the target of the link
    final NodeRef destRef = (NodeRef) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_LINK_DESTINATION
    );

    if (
      destRef != null &&
      permissionService.hasPermission(destRef, PermissionService.READ) ==
      AccessStatus.ALLOWED &&
      !childsNodeRefs.contains(nodeRef) &&
      !allNodeRefs.contains(nodeRef)
    ) {
      childsNodeRefs.add(destRef);
    }
  }

  private void createFolderRepresentation(
    final NodeRef nodeRef,
    final List<NodeRef> childsNodeRefs,
    final List<NodeRef> allNodeRefs
  ) {
    final List<ChildAssociationRef> listChildAssocs =
      nodeService.getChildAssocs(nodeRef);
    if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
      childsNodeRefs.add(nodeRef);
    }
    List<NodeRef> tempNodeRefs;
    for (final ChildAssociationRef childAssoc : listChildAssocs) {
      tempNodeRefs = getAllChildsNodeRefs(
        allNodeRefs,
        childAssoc.getChildRef()
      );
      // remove duplicate
      tempNodeRefs.removeAll(childsNodeRefs);
      tempNodeRefs.removeAll(allNodeRefs);
      childsNodeRefs.addAll(tempNodeRefs);
    }
  }

  private boolean isContent(final NodeRef nodeRef) {
    boolean isContent = false;
    TypeDefinition typeDef;

    if (
      nodeService.exists(nodeRef) &&
      AccessStatus.ALLOWED.equals(
        permissionService.hasPermission(nodeRef, PermissionService.READ)
      )
    ) {
      final QName type = nodeService.getType(nodeRef);

      // Trick to optimise the efficiency of the code
      if (validTypeMap.containsKey(type)) {
        typeDef = validTypeMap.get(type);
      } else {
        // make sure the type is defined in the data dictionary
        typeDef = dictionaryService.getType(type);
        validTypeMap.put(type, typeDef);
      }

      if (
        typeDef != null &&
        (ContentModel.TYPE_CONTENT.equals(type) ||
          dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
      ) {
        isContent = true;
      }
    }
    return isContent;
  }

  /**
   * Builds the index records describing the content items reachable from the given nodes, for
   * export into an index file.
   *
   * <p>The node tree under each supplied {@link NodeRef} is walked (folders, content and file links
   * are followed, folder links are skipped to avoid infinite recursion). Only readable content
   * nodes whose security ranking is public or normal are included, duplicates by relative path are
   * ignored, and each retained node is described by a fully populated {@link IndexRecord} with an
   * incrementing row number.
   *
   * @param nodeRefs the root nodes to explore
   * @return the list of {@link IndexRecord} describing the eligible content items
   */
  public List<IndexRecord> getMetaData(final List<NodeRef> nodeRefs) {
    final List<IndexRecord> indexRecords = new LinkedList<>();
    int rowNumber = 0;
    final List<NodeRef> allNodeRefs = new ArrayList<>();
    for (final NodeRef nodeRef : nodeRefs) {
      allNodeRefs.addAll(getAllChildsNodeRefs(allNodeRefs, nodeRef));
    }

    for (final NodeRef nodeRef : allNodeRefs) {
      if (!isContent(nodeRef)) {
        continue;
      }
      String securityRanking = getSecurityRankingForNode(nodeRef);
      if (isAllowedSecurityRanking(securityRanking)) {
        String relativePath = zipService.getRelativeLibraryPath(nodeRef);
        if (
          indexRecords.stream().noneMatch(r -> r.getName().equals(relativePath))
        ) {
          rowNumber++;
          IndexRecord indexRecord = new IndexRecordImpl(rowNumber);
          populateIndexRecord(
            indexRecord,
            nodeRef,
            relativePath,
            securityRanking
          );
          indexRecords.add(indexRecord);
        }
      }
    }
    return indexRecords;
  }

  private String getSecurityRankingForNode(NodeRef nodeRef) {
    if (multilingualContentService.isTranslation(nodeRef)) {
      NodeRef translationContainer =
        multilingualContentService.getTranslationContainer(nodeRef);
      return (String) nodeService.getProperty(
        translationContainer,
        DocumentModel.PROP_SECURITY_RANKING
      );
    }
    return (String) nodeService.getProperty(
      nodeRef,
      DocumentModel.PROP_SECURITY_RANKING
    );
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

  private void populateIndexRecord(
    IndexRecord indexRecord,
    NodeRef nodeRef,
    String relativePath,
    String securityRanking
  ) {
    indexRecord.setName(relativePath);
    setStringPropertyIfNotEmpty(
      indexRecord::setTitle,
      nodeRef,
      ContentModel.PROP_TITLE
    );
    setStringPropertyIfNotEmpty(
      indexRecord::setDescription,
      nodeRef,
      ContentModel.PROP_DESCRIPTION
    );
    Locale docLang = (Locale) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_LOCALE
    );
    if (docLang != null) {
      indexRecord.setDocLang(docLang.toString());
    }
    setStringPropertyIfNotEmpty(
      indexRecord::setAuthor,
      nodeRef,
      ContentModel.PROP_AUTHOR
    );
    indexRecord.setKeywords(buildKeywordString(nodeRef));
    setStringPropertyIfNotEmpty(
      indexRecord::setStatus,
      nodeRef,
      DocumentModel.PROP_STATUS
    );
    Date issueDate = (Date) nodeService.getProperty(
      nodeRef,
      DocumentModel.PROP_ISSUE_DATE
    );
    if (issueDate != null) {
      indexRecord.setIssueDate(sdf.format(issueDate));
    }
    setStringPropertyIfNotEmpty(
      indexRecord::setReference,
      nodeRef,
      DocumentModel.PROP_REFERENCE
    );
    setExpirationDate(indexRecord, nodeRef);
    setSecurityRankingOnRecord(indexRecord, nodeRef, securityRanking);
    setDynamicPropertiesOnRecord(indexRecord, nodeRef);
    setMultilingualProperties(indexRecord, nodeRef);
  }

  private void setStringPropertyIfNotEmpty(
    java.util.function.Consumer<String> setter,
    NodeRef nodeRef,
    QName prop
  ) {
    String value = (String) nodeService.getProperty(nodeRef, prop);
    if (value != null && !value.isEmpty()) {
      setter.accept(value);
    }
  }

  private String buildKeywordString(NodeRef nodeRef) {
    List<Keyword> keywords = keywordsService.getKeywordsForNode(nodeRef);
    StringBuilder keywordBuilder = new StringBuilder();
    for (Keyword keyword : keywords) {
      if (keyword.isKeywordTranslated()) {
        for (Locale locale : keyword.getMLValues().getLocales()) {
          keywordBuilder
            .append("(")
            .append(locale)
            .append(") ")
            .append(keyword.getMLValues().getValue(locale))
            .append(";");
        }
      } else {
        if (!keywordBuilder.isEmpty()) {
          keywordBuilder.append(";");
        }
        keywordBuilder.append(keyword.getString());
      }
    }
    String result = keywordBuilder.toString();
    return result.isEmpty() ? null : result;
  }

  private void setExpirationDate(IndexRecord indexRecord, NodeRef nodeRef) {
    Date expirationDate = null;
    if (multilingualContentService.isTranslation(nodeRef)) {
      NodeRef pivotNodeRef = multilingualContentService.getPivotTranslation(
        nodeRef
      );
      if (pivotNodeRef == nodeRef) {
        NodeRef translationContainer =
          multilingualContentService.getTranslationContainer(nodeRef);
        expirationDate = (Date) nodeService.getProperty(
          translationContainer,
          DocumentModel.PROP_EXPIRATION_DATE
        );
      }
    } else {
      expirationDate = (Date) nodeService.getProperty(
        nodeRef,
        DocumentModel.PROP_EXPIRATION_DATE
      );
    }
    if (expirationDate != null) {
      indexRecord.setExpirationDate(sdf.format(expirationDate));
    }
  }

  private void setSecurityRankingOnRecord(
    IndexRecord indexRecord,
    NodeRef nodeRef,
    String securityRanking
  ) {
    if (securityRanking == null) {
      return;
    }
    if (
      !multilingualContentService.isTranslation(nodeRef) ||
      nodeRef.equals(multilingualContentService.getPivotTranslation(nodeRef))
    ) {
      indexRecord.setSecurityRanking(securityRanking);
    }
  }

  private void setDynamicPropertiesOnRecord(
    IndexRecord indexRecord,
    NodeRef nodeRef
  ) {
    for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
      String dynamicProperty = (String) nodeService.getProperty(
        nodeRef,
        DocumentModel.ALL_DYN_PROPS.get(i)
      );
      if (dynamicProperty != null && !dynamicProperty.isEmpty()) {
        indexRecord.setDynamicProperty(i + 1, dynamicProperty);
      }
    }
  }

  private void setMultilingualProperties(
    IndexRecord indexRecord,
    NodeRef nodeRef
  ) {
    if (!multilingualContentService.isTranslation(nodeRef)) {
      return;
    }
    NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
    if (nodeRef.equals(pivot)) {
      indexRecord.setOriLang("Y");
      return;
    }
    indexRecord.setOriLang("");
    indexRecord.setRelTrans(zipService.getRelativeLibraryPath(pivot));
    if (
      nodeService.hasAspect(
        nodeRef,
        ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
      )
    ) {
      String translator = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_AUTHOR
      );
      if (translator != null && !translator.isEmpty()) {
        indexRecord.setTranslator(translator);
      }
      indexRecord.setNoContent("Y");
    } else {
      indexRecord.setNoContent("N");
    }
  }

  /**
   * Validates that every index entry has a matching uploaded file.
   *
   * <p>For each index record whose path is not found among the uploaded entries a fatal validation
   * message is added, except for entries declared as empty translations ({@code noContent = "Y"}),
   * which are expected to have no corresponding file in the archive.
   *
   * @param indexFileEntries the index records to validate
   * @param uploadedEntries the entries that were actually uploaded from the archive
   * @param messages collector to which validation messages for missing entries are added
   */
  public void validateEntries(
    final List<IndexRecord> indexFileEntries,
    final List<UploadedEntry> uploadedEntries,
    final List<ValidationMessage> messages
  ) {
    for (final IndexRecord indexRecord : indexFileEntries) {
      String indexEntryPath = normalizeIndexPath(indexRecord.getName());
      logTrace("indexPath:" + indexEntryPath);

      boolean found = isEntryUploaded(indexEntryPath, uploadedEntries);
      if (!found) {
        handleMissingEntry(indexRecord, messages);
      }
    }
  }

  private String normalizeIndexPath(String name) {
    return name.startsWith(File.separator) ? name : File.separatorChar + name;
  }

  private boolean isEntryUploaded(
    String indexEntryPath,
    List<UploadedEntry> uploadedEntries
  ) {
    for (final UploadedEntry uploadedEntry : uploadedEntries) {
      String uploadPath = uploadedEntry.getFilePath();
      logTrace("uploaded document:" + uploadPath);
      if (uploadPath.equals(indexEntryPath)) {
        logTrace("index entry is uploaded:" + uploadPath);
        return true;
      }
    }
    return false;
  }

  private void handleMissingEntry(
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if ("Y".equalsIgnoreCase(indexRecord.getNoContent())) {
      logTrace("index entry for an empty translation" + indexRecord.getName());
      return;
    }
    logTrace("index entry is NOT uploaded !!!" + indexRecord.getName());
    messages.add(
      new ValidationMessageImpl(
        indexRecord.getRowNumber(),
        indexRecord.getName(),
        I18NUtil.getMessage(FILE_REFERENCED_NOT_PRESENT_IN_ZIP),
        ErrorType.Fatal
      )
    );
  }

  private void logTrace(String message) {
    if (logger.isTraceEnabled()) {
      logger.trace(message);
    }
  }

  private IndexRecord getIndexRecord(
    final List<IndexRecord> indexRecords,
    final String fileName
  ) {
    for (final IndexRecord indexRecord : indexRecords) {
      if (logger.isTraceEnabled()) {
        logger.trace(indexRecord.getName());
      }
      if (indexRecord.getName().equals(fileName)) {
        return indexRecord;
      }
    }
    return null;
  }

  private String getFileName(final String relativePath) {
    String path;
    if (File.separatorChar == '/') {
      path = relativePath.replace('\\', File.separatorChar);
    } else {
      path = relativePath.replace('/', File.separatorChar);
    }
    if (path.startsWith(File.separator)) {
      path = path.substring(1, path.length());
    }
    String[] pathElements;
    if (File.separatorChar == '/') {
      pathElements = path.split("/");
    } else {
      pathElements = path.replace('\\', '/').split("/");
    }
    String fileName = null;
    if (pathElements.length > 1) {
      // Contains subfolder
      final String[] folderElements = new String[pathElements.length - 1];
      System.arraycopy(
        pathElements,
        0,
        folderElements,
        0,
        pathElements.length - 1
      );
      // fileName
      fileName = pathElements[pathElements.length - 1];
    } else {
      // Reference to the document directly
      fileName = relativePath;
    }
    return fileName;
  }

  private NodeRef searchFile(
    final NodeRef parentNodeRef,
    final String relativePath
  ) {
    String path;
    if (File.separatorChar == '/') {
      path = relativePath.replace('\\', File.separatorChar);
    } else {
      path = relativePath.replace('/', File.separatorChar);
    }
    if (path.startsWith(File.separator)) {
      path = path.substring(1, path.length());
    }
    String[] pathElements;
    if (File.separatorChar == '/') {
      pathElements = path.split("/");
    } else {
      pathElements = path.replace('\\', '/').split("/");
    }
    String fileName = null;
    NodeRef destinationFolderNodeRef = null;
    if (pathElements.length > 1) {
      // Contains subfolder
      final String[] folderElements = new String[pathElements.length - 1];
      System.arraycopy(
        pathElements,
        0,
        folderElements,
        0,
        pathElements.length - 1
      );
      final List<String> folderElementsList = Arrays.asList(folderElements);
      // fileName
      fileName = pathElements[pathElements.length - 1];
      destinationFolderNodeRef = searchFolder(
        parentNodeRef,
        folderElementsList
      );
    } else {
      // Reference to the document directly
      fileName = relativePath;
      destinationFolderNodeRef = parentNodeRef;
    }
    NodeRef fileNodeRef = null;
    if (destinationFolderNodeRef != null) {
      fileNodeRef = nodeService.getChildByName(
        destinationFolderNodeRef,
        ContentModel.ASSOC_CONTAINS,
        fileName
      );
    }
    return fileNodeRef;
  }

  private NodeRef searchFolder(
    final NodeRef parentNodeRef,
    final List<String> folderElementsList
  ) {
    NodeRef folder = parentNodeRef;

    for (final String folderName : folderElementsList) {
      if (folder != null) {
        folder = nodeService.getChildByName(
          folder,
          ContentModel.ASSOC_CONTAINS,
          folderName
        );
      }
    }
    return folder;
  }

  /* IOC */
  /**
   * @return the injected {@link FileFolderService}
   */
  public FileFolderService getFileFolderService() {
    return fileFolderService;
  }

  /**
   * @param fileFolderService the {@link FileFolderService} to inject
   */
  public void setFileFolderService(final FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * @return the injected {@link NodeService}
   */
  public NodeService getNodeService() {
    return nodeService;
  }

  /**
   * @param nodeService the {@link NodeService} to inject
   */
  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * @return the injected {@link ContentService}
   */
  public ContentService getContentService() {
    return contentService;
  }

  /**
   * @param contentService the {@link ContentService} to inject
   */
  public void setContentService(final ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * @return the injected {@link ZipService}
   */
  public ZipService getZipService() {
    return zipService;
  }

  /**
   * @param zipService the {@link ZipService} to inject
   */
  public void setZipService(final ZipService zipService) {
    this.zipService = zipService;
  }

  /**
   * @return the injected {@link IndexService}
   */
  public IndexService getIndexService() {
    return indexService;
  }

  /**
   * @param indexService the {@link IndexService} to inject
   */
  public void setIndexService(final IndexService indexService) {
    this.indexService = indexService;
  }

  /**
   * @return the injected {@link KeywordsService}
   */
  public KeywordsService getKeywordsService() {
    return keywordsService;
  }

  /**
   * @param keywordsService the {@link KeywordsService} to inject
   */
  public void setKeywordsService(final KeywordsService keywordsService) {
    this.keywordsService = keywordsService;
  }

  /**
   * @return the contentFilterLanguagesService
   */
  public ContentFilterLanguagesService getContentFilterLanguagesService() {
    return contentFilterLanguagesService;
  }

  /**
   * @param contentFilterLanguagesService the contentFilterLanguagesService to set
   */
  public void setContentFilterLanguagesService(
    final ContentFilterLanguagesService contentFilterLanguagesService
  ) {
    this.contentFilterLanguagesService = contentFilterLanguagesService;
  }

  /**
   * @return the multilingualContentService
   */
  public MultilingualContentService getMultilingualContentService() {
    return multilingualContentService;
  }

  /**
   * @param multilingualContentService the multilingualContentService to set
   */
  public void setMultilingualContentService(
    final MultilingualContentService multilingualContentService
  ) {
    this.multilingualContentService = multilingualContentService;
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
}
