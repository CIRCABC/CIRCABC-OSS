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

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.keywords.KeywordImpl;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecordImpl;
import eu.cec.digit.circabc.service.bulk.indexes.IndexService;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.upload.UploadedEntry;
import eu.cec.digit.circabc.service.bulk.upload.UploadedEntryImpl;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import eu.cec.digit.circabc.service.compress.CompressedEntry;
import eu.cec.digit.circabc.service.compress.ZipService;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.service.struct.ManagementService;
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
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.jcr.PathNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class BulkServiceImpl implements BulkService {

    private static final String FILE_REFERENCED_NOT_PRESENT_IN_ZIP =
            "file_referenced_not_present_in_zip";

    private static final Log logger = LogFactory.getLog(BulkServiceImpl.class);
    private static final ThreadLocal<DateFormat> sdf =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    return new SimpleDateFormat(INDEX_DATE_FORMAT);
                }
            };
    private static final String TEMP_FILE_PREFIX = "circabc_";
    private static final String TEMP_FILE_SUFFIX = ".temp";
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
    private ManagementService managementService;
    private ContentFilterLanguagesService contentFilterLanguagesService;
    private MultilingualContentService multilingualContentService;
    private DictionaryService dictionaryService;
    private PermissionService permissionService;

    public List<CompressedEntry> getCompressedEntries(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<ValidationMessage> messages) {
        if (logger.isDebugEnabled()) {
            logger.debug("upload file:" + compressedFile.getPath() + " to:" + containerNodeRef);
        }
        final List<CompressedEntry> compressedEntries =
                zipService.getCompressedEntries(compressedFile, messages);
        return compressedEntries;
    }

    /**
     * Extract "index" from the compressed and create a List of entries "IndexRecord"
     */
    public List<IndexRecord> getIndexRecords(
            final File compressedFile, final List<ValidationMessage> messages) throws IOException {
        final List<IndexRecord> indexRecords = new ArrayList<>();
        File temporaryIndexFile = null;
        try {
            // Step 0: extract index file to temporary file
            // unfortunately a ZIP file can not be read directly from an input
            // stream so we have to create
            // a temporary file first
            temporaryIndexFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            if (logger.isDebugEnabled()) {
                logger.debug("Create temporary file:" + temporaryIndexFile);
            }

            // Step 1: Extract index file from the compressed file (if index
            // file exist)
            final boolean exist =
                    zipService.extract(compressedFile, IndexService.INDEX_FILE, temporaryIndexFile, messages);

            if (exist) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The zip contains an index file");
                }
                // Extract informations from this index file
                indexService.getIndexRecords(temporaryIndexFile, indexRecords, messages);
            } else {
                messages.add(new ValidationMessageImpl(0, "", "No index file provided", ErrorType.Warning));
            }
        } finally {
            // now the creationOfIndexEntries is done, delete the temporary file
            if (temporaryIndexFile != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Delete temporary file:" + temporaryIndexFile);
                }
                boolean isDeleted = temporaryIndexFile.delete();
                if (!isDeleted && logger.isWarnEnabled()) {
                    try {
                        logger.warn("Unable to delete file : " + temporaryIndexFile.getCanonicalPath());
                    } catch (IOException e) {
                        logger.warn("Unable to get getCanonicalPath for : " + temporaryIndexFile.getPath(), e);
                    }
                }
            }
        }
        return indexRecords;
    }

    public List<UploadedEntry> upload(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<ValidationMessage> messages) {
        return upload(containerNodeRef, compressedFile, Collections.<IndexRecord>emptyList(), messages);
    }

    public List<UploadedEntry> upload(
            final NodeRef containerNodeRef,
            final File compressedFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages) {
        final List<UploadedEntry> uploadedEntries = new ArrayList<>();
        if (logger.isDebugEnabled()) {
            logger.debug("upload file:" + compressedFile.getPath() + " to:" + containerNodeRef);
        }
        final List<String> excludedFileName = new ArrayList<>(1);
        excludedFileName.add(File.separatorChar + IndexService.INDEX_FILE);
        final NodeRef libraryNodeRef = managementService.getCurrentLibrary(containerNodeRef);
        final Map<String, NodeRef> extractedFiles =
                zipService.extract(
                        libraryNodeRef,
                        containerNodeRef,
                        compressedFile,
                        excludedFileName,
                        indexRecords,
                        messages);

        IndexRecord indexRecord;
        UploadedEntry uploadedEntry;

        // Work arround for problem of original Multilingal document (have to be uploaded first
        // Set properties extracted from index file
        String fileName;

        // Set properties for non MultiLingual Documents.
        for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
            indexRecord = getIndexRecord(indexRecords, entry.getKey());
            if (indexRecord != null && indexRecord.getOriLang() == null) {
                fileName = (String) nodeService.getProperty(entry.getValue(), ContentModel.PROP_NAME);
                setMetaDatas(
                        libraryNodeRef, containerNodeRef, entry.getValue(), indexRecord, fileName, messages);
                uploadedEntry = new UploadedEntryImpl(fileName, entry.getKey());
                uploadedEntries.add(uploadedEntry);
            }
        }

        // Set Properties for Original Multilinguals documents. (Pivot Document)
        for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
            indexRecord = getIndexRecord(indexRecords, entry.getKey());
            if (indexRecord != null
                    && indexRecord.getOriLang() != null
                    && indexRecord.getOriLang().equalsIgnoreCase("Y")) {
                fileName = (String) nodeService.getProperty(entry.getValue(), ContentModel.PROP_NAME);
                setMetaDatas(
                        libraryNodeRef, containerNodeRef, entry.getValue(), indexRecord, fileName, messages);
                uploadedEntry = new UploadedEntryImpl(fileName, entry.getKey());
                uploadedEntries.add(uploadedEntry);
            }
        }

        // Set properties for all Translations with Content
        for (final Map.Entry<String, NodeRef> entry : extractedFiles.entrySet()) {
            indexRecord = getIndexRecord(indexRecords, entry.getKey());
            if (indexRecord != null
                    && indexRecord.getOriLang() != null
                    && !indexRecord.getOriLang().equalsIgnoreCase("Y")) {
                fileName = (String) nodeService.getProperty(entry.getValue(), ContentModel.PROP_NAME);
                setMetaDatas(
                        libraryNodeRef, containerNodeRef, entry.getValue(), indexRecord, fileName, messages);
                uploadedEntry = new UploadedEntryImpl(fileName, entry.getKey());
                uploadedEntries.add(uploadedEntry);
            }
        }

        // Set properties for all Translations with Content
        String filePath;
        File file;
        for (final IndexRecord indexRecord2 : indexRecords) {
            if (indexRecord2 != null
                    && indexRecord2.getNoContent() != null
                    && indexRecord2.getNoContent().equalsIgnoreCase("Y")) {
                filePath = indexRecord2.getName();
                file = new File(filePath);
                fileName = file.getName();
                setMetaDatas(libraryNodeRef, containerNodeRef, null, indexRecord2, fileName, messages);
                uploadedEntry = new UploadedEntryImpl(fileName, filePath);
                uploadedEntries.add(uploadedEntry);
            }
        }
        return uploadedEntries;
    }

    private void setMetaDatas(
            final NodeRef libraryNodeRef,
            final NodeRef containerNodeRef,
            final NodeRef nodeRef,
            final IndexRecord indexRecord,
            final String fileName,
            final List<ValidationMessage> messages) {

        NodeRef currentNode = nodeRef;
        NodeRef pivotNode = null;

        if (logger.isDebugEnabled()) {
            logger.debug("setMetaDatas for file:" + fileName);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(indexRecord);
        }

        Map<QName, Serializable> nodeProperties = null;

        if (indexRecord != null) {
            // Special case for empty Translation && file existing in repository but not in zip!!!!
            if (nodeRef == null) {
                final String library =
                        (String) nodeService.getProperty(libraryNodeRef, ContentModel.PROP_NAME);
                if (indexRecord.getNoContent() != null
                        && indexRecord.getNoContent().equalsIgnoreCase("Y")) {
                    NodeRef relatedTranslationNodeRef = null;
                    if (indexRecord.getRelTrans() != null && indexRecord.getRelTrans().length() != 0) {
                        // Lien vers pivot
                        final String relatedTranslation = indexRecord.getRelTrans();
                        if (relatedTranslation.startsWith(library)
                                || relatedTranslation.startsWith(File.separatorChar + library)) {
                            relatedTranslationNodeRef = searchFile(libraryNodeRef, relatedTranslation);
                        } else {
                            relatedTranslationNodeRef = searchFile(containerNodeRef, relatedTranslation);
                        }
                        if (relatedTranslationNodeRef != null) {
                            final String translationName = indexRecord.getName();
                            try {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("addEmptyTranslation with DocLang:" + indexRecord.getDocLang());
                                }
                                final String emptyTranslationName = getFileName(translationName);
                                currentNode =
                                        multilingualContentService.addEmptyTranslation(
                                                relatedTranslationNodeRef,
                                                emptyTranslationName,
                                                I18NUtil.parseLocale(indexRecord.getDocLang()));
                            } catch (final FileExistsException ex) {
                                final String ERROR_DESCRIPTION = "bulk_upload_existing_empty_translation";
                                messages.add(
                                        new ValidationMessageImpl(
                                                indexRecord.getRowNumber(),
                                                fileName,
                                                I18NUtil.getMessage(ERROR_DESCRIPTION),
                                                ErrorType.Fatal));
                            }
                        } else {
                            final String ERROR_DESCRIPTION =
                                    "bulk_upload_invalid_translation_empty_multi_lingual_doc";
                            messages.add(
                                    new ValidationMessageImpl(
                                            indexRecord.getRowNumber(),
                                            fileName,
                                            I18NUtil.getMessage(ERROR_DESCRIPTION),
                                            ErrorType.Fatal));
                        }
                    } else {
                        final String ERROR_DESCRIPTION = "bulk_upload_invalid_translation_multi_lingual_doc";
                        messages.add(
                                new ValidationMessageImpl(
                                        indexRecord.getRowNumber(),
                                        fileName,
                                        I18NUtil.getMessage(ERROR_DESCRIPTION),
                                        ErrorType.Fatal));
                    }
                } else {
                    // Special Case for file not existing in the zip but present in the repository
                    final String path = indexRecord.getName();
                    if (path.startsWith(library) || path.startsWith(File.separatorChar + library)) {
                        currentNode = searchFile(libraryNodeRef, path);
                    } else {
                        currentNode = searchFile(containerNodeRef, path);
                    }
                    if (currentNode == null) {
                        final String ERROR_DESCRIPTION =
                                "bulk_upload_file_not_exist_either_in_zip_and_in_repository";
                        messages.add(
                                new ValidationMessageImpl(
                                        indexRecord.getRowNumber(),
                                        fileName,
                                        I18NUtil.getMessage(ERROR_DESCRIPTION),
                                        ErrorType.Fatal));
                    }
                }
            }

            nodeProperties = nodeService.getProperties(currentNode);

            // Title
            if (indexRecord.getTitle() != null && indexRecord.getTitle().length() > 0) {
                setPropertiesGeneric(
                        currentNode,
                        fileName,
                        nodeProperties,
                        ContentModel.PROP_TITLE,
                        indexRecord.getTitle(),
                        indexRecord,
                        messages);
            }
            // Description
            if (indexRecord.getDescription() != null && indexRecord.getDescription().length() > 0) {
                setPropertiesGeneric(
                        currentNode,
                        fileName,
                        nodeProperties,
                        ContentModel.PROP_DESCRIPTION,
                        indexRecord.getDescription(),
                        indexRecord,
                        messages);
            }

            // Keywords
            if (indexRecord.getKeywords() != null && indexRecord.getKeywords().length() > 0) {
                final String keywords[] = indexRecord.getKeywords().split(";");
                Keyword keyword;
                final NodeRef igNodeRef = managementService.getCurrentInterestGroup(currentNode);
                Locale locale;
                String localeValue;
                String keywordValue;
                int open = 0;
                int close = 0;
                for (final String langKeywordValue : keywords) {
                    open = langKeywordValue.indexOf("(");
                    close = langKeywordValue.indexOf(")");
                    if (open == -1 || close == -1) {
                        final String ERROR_DESCRIPTION = "bulk_upload_param_keyword";
                        messages.add(
                                new ValidationMessageImpl(
                                        indexRecord.getRowNumber(),
                                        fileName,
                                        I18NUtil.getMessage(ERROR_DESCRIPTION) + " " + langKeywordValue,
                                        ErrorType.Warning));
                    } else {
                        localeValue = langKeywordValue.substring(open + 1, open + 1 + 2);
                        keywordValue = langKeywordValue.substring(close + 1);
                        if (logger.isDebugEnabled()) {
                            logger.debug("locale: " + localeValue + " keyword: " + keywordValue);
                        }
                        locale = I18NUtil.parseLocale(localeValue);
                        keyword = new KeywordImpl(locale, keywordValue);

                        List<Keyword> igKeywords = keywordsService.getKeywords(igNodeRef);
                        for (Keyword item : igKeywords) {
                            if (item.exists(locale, keywordValue)) {
                                keyword = item;
                                break;
                            }
                        }
                        if (!keywordsService.exists(keyword)) {
                            keyword = keywordsService.createKeyword(igNodeRef, keyword);
                        }
                        if (!nodeService.hasAspect(currentNode, DocumentModel.ASPECT_CPROPERTIES)) {
                            // add CProperties Aspect
                            nodeService.addAspect(currentNode, DocumentModel.ASPECT_CPROPERTIES, null);
                        }
                        keywordsService.addKeywordToNode(currentNode, keyword);
                    }
                }
            }

            // Status
            if (indexRecord.getStatus() != null && indexRecord.getStatus().length() > 0) {
                boolean valid = false;
                for (final String status : DocumentModel.STATUS_VALUES) {
                    if (status.equals(indexRecord.getStatus())) {
                        valid = true;
                        break;
                    }
                }
                if (valid) {
                    setPropertiesGeneric(
                            currentNode,
                            fileName,
                            nodeProperties,
                            DocumentModel.PROP_STATUS,
                            indexRecord.getStatus(),
                            indexRecord,
                            messages);
                } else {
                    final String ERROR_DESCRIPTION = "bulk_upload_param_doc_status";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Fatal));
                }
            }

            // Issue Date
            if (indexRecord.getIssueDate() != null && indexRecord.getIssueDate().length() > 0) {
                try {
                    final Date parsed = sdf.get().parse(indexRecord.getIssueDate());
                    setPropertiesGeneric(
                            currentNode,
                            fileName,
                            nodeProperties,
                            DocumentModel.PROP_ISSUE_DATE,
                            parsed,
                            indexRecord,
                            messages);
                } catch (final ParseException pe) {
                    final String ERROR_DESCRIPTION = "bulk_upload_issue_date_error";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Warning));
                }
            }

            // Reference
            if (indexRecord.getReference() != null && indexRecord.getReference().length() > 0) {
                setPropertiesGeneric(
                        currentNode,
                        fileName,
                        nodeProperties,
                        DocumentModel.PROP_REFERENCE,
                        indexRecord.getReference(),
                        indexRecord,
                        messages);
            }

            // SecurityRanking
            if (indexRecord.getSecurityRanking() != null
                    && indexRecord.getSecurityRanking().length() > 0) {
                boolean valid = false;
                for (final String securityRanking : DocumentModel.SECURITY_RANKINGS) {
                    if (securityRanking.equals(indexRecord.getSecurityRanking())) {
                        valid = true;
                        break;
                    }
                }
                if (valid) {
                    setPropertiesGeneric(
                            currentNode,
                            fileName,
                            nodeProperties,
                            DocumentModel.PROP_SECURITY_RANKING,
                            indexRecord.getSecurityRanking(),
                            indexRecord,
                            messages);
                } else {
                    final String ERROR_DESCRIPTION = "bulk_upload_security_ranking";
                    final String error_message = I18NUtil.getMessage(ERROR_DESCRIPTION);
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(), fileName, error_message, ErrorType.Fatal));
                    if (logger.isErrorEnabled()) {
                        logger.error(error_message + " value:" + indexRecord.getSecurityRanking());
                    }
                }
            }

            for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
                QName item = DocumentModel.ALL_DYN_PROPS.get(i);
                final String dynamicProperty = indexRecord.getDynamicProperty(i + 1);
                if (dynamicProperty != null && dynamicProperty.length() > 0) {
                    setPropertiesGeneric(
                            currentNode, fileName, nodeProperties, item, dynamicProperty, indexRecord, messages);
                }
            }

            if (indexRecord.getDocLang() != null && indexRecord.getDocLang().length() > 0) {
                if (contentFilterLanguagesService.getFilterLanguages().contains(indexRecord.getDocLang())) {
                    if (indexRecord.getOriLang() != null
                            && indexRecord.getOriLang().length() != 0
                            && indexRecord.getOriLang().equalsIgnoreCase("Y")) {
                        // Langue PIVOT
                        multilingualContentService.makeTranslation(
                                currentNode, I18NUtil.parseLocale(indexRecord.getDocLang()));
                        pivotNode = currentNode;
                    } else {
                        // il s'agit surement d'une traduction
                        if (indexRecord.getRelTrans() == null || indexRecord.getRelTrans().length() == 0) {
                            // Ben non... alors on met un Warning
                            final String ERROR_DESCRIPTION =
                                    "bulk_upload_param_doc_lang_on_a_document_not_translated";
                            messages.add(
                                    new ValidationMessageImpl(
                                            indexRecord.getRowNumber(),
                                            fileName,
                                            I18NUtil.getMessage(ERROR_DESCRIPTION),
                                            ErrorType.Warning));
                        }
                    }
                } else {
                    final String ERROR_DESCRIPTION = "bulk_upload_param_doc_lang";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Fatal));
                }
            }

            NodeRef relatedTranslationNodeRef = null;
            if (indexRecord.getRelTrans() != null && indexRecord.getRelTrans().length() != 0) {
                // Lien vers pivot
                final String library =
                        (String) nodeService.getProperty(libraryNodeRef, ContentModel.PROP_NAME);
                final String relatedTranslation = indexRecord.getRelTrans();
                if (relatedTranslation.startsWith(library)
                        || relatedTranslation.startsWith(File.separatorChar + library)) {
                    relatedTranslationNodeRef = searchFile(libraryNodeRef, relatedTranslation);
                } else {
                    relatedTranslationNodeRef = searchFile(containerNodeRef, relatedTranslation);
                }
                if (relatedTranslationNodeRef != null) {
                    // final String relatedTranslationName =
                    // (String)nodeService.getProperty(relatedTranslationNodeRef, ContentModel.PROP_NAME);
                    if (indexRecord.getNoContent() != null && indexRecord.getNoContent().equals("Y")) {
                        // Empty Translation allready created
                    } else {
                        if (currentNode != null) {
                            multilingualContentService.addTranslation(
                                    currentNode,
                                    relatedTranslationNodeRef,
                                    I18NUtil.parseLocale(indexRecord.getDocLang()));
                        }
                    }
                } else {
                    final String ERROR_DESCRIPTION = "bulk_upload_invalid_translation_multi_lingual_doc";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Fatal));
                }
            }

            //			 Author
            if (indexRecord.getAuthor() != null && indexRecord.getAuthor().length() > 0) {
                if (currentNode == pivotNode && pivotNode != null) {
                    // Set the Author value on the logical document if the current document (currentNode) is
                    // the pivot language
                    final NodeRef logicalMlDocument =
                            multilingualContentService.getTranslationContainer(pivotNode);
                    setPropertiesGeneric(
                            logicalMlDocument,
                            fileName,
                            nodeProperties,
                            ContentModel.PROP_AUTHOR,
                            indexRecord.getAuthor(),
                            indexRecord,
                            messages);
                } else {
                    // Set the Author on the current Document
                    setPropertiesGeneric(
                            currentNode,
                            fileName,
                            nodeProperties,
                            ContentModel.PROP_AUTHOR,
                            indexRecord.getAuthor(),
                            indexRecord,
                            messages);
                }
            }

            if (indexRecord.getTranslator() != null && indexRecord.getTranslator().length() != 0) {
                if (relatedTranslationNodeRef != null) {
                    if (currentNode != null) {
                        if (nodeService.hasAspect(
                                relatedTranslationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
                                || nodeService.hasAspect(
                                relatedTranslationNodeRef,
                                ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
                            nodeService.setProperty(
                                    currentNode, ContentModel.PROP_AUTHOR, indexRecord.getTranslator());
                        } else {
                            final String ERROR_DESCRIPTION = "bulk_upload_add_translator_non_multi_lingual_doc";

                            messages.add(
                                    new ValidationMessageImpl(
                                            indexRecord.getRowNumber(),
                                            fileName,
                                            I18NUtil.getMessage(ERROR_DESCRIPTION),
                                            ErrorType.Fatal));
                        }
                    } else {
                        // Should normaly be a empty translation... let check it.
                        logger.debug("Oooppsssss");
                    }
                } else {
                    if (indexRecord.getOriLang() != null && indexRecord.getOriLang().equalsIgnoreCase("Y")) {
                        // Document PIVOT
                        // Inutile de setter le translator sur un document PIVOT... puisque c'est l'auteur.
                    } else {
                        // Dans les autre cas...
                        final String ERROR_DESCRIPTION =
                                "bulk_upload_add_translator_with_no_related_translation";

                        messages.add(
                                new ValidationMessageImpl(
                                        indexRecord.getRowNumber(),
                                        fileName,
                                        I18NUtil.getMessage(ERROR_DESCRIPTION),
                                        ErrorType.Fatal));
                    }
                }
            }

            // ExpirationDate
            if (indexRecord.getExpirationDate() != null && indexRecord.getExpirationDate().length() > 0) {
                try {
                    final Date parsed = sdf.get().parse(indexRecord.getExpirationDate());
                    if (currentNode == pivotNode && pivotNode != null) {
                        // Set the Expiration Date value on the logical document
                        final NodeRef logicalMlDocument =
                                multilingualContentService.getTranslationContainer(pivotNode);
                        setPropertiesGeneric(
                                logicalMlDocument,
                                fileName,
                                nodeProperties,
                                DocumentModel.PROP_EXPIRATION_DATE,
                                parsed,
                                indexRecord,
                                messages);
                    } else {
                        if (indexRecord.getRelTrans() != null && indexRecord.getRelTrans().length() > 0) {
                            final String ERROR_DESCRIPTION = "bulk_upload_expir_date_error_on_translation";
                            messages.add(
                                    new ValidationMessageImpl(
                                            indexRecord.getRowNumber(),
                                            fileName,
                                            I18NUtil.getMessage(ERROR_DESCRIPTION),
                                            ErrorType.Warning));
                        } else {
                            setPropertiesGeneric(
                                    currentNode,
                                    fileName,
                                    nodeProperties,
                                    DocumentModel.PROP_EXPIRATION_DATE,
                                    parsed,
                                    indexRecord,
                                    messages);
                        }
                    }
                } catch (final ParseException pe) {
                    final String ERROR_DESCRIPTION = "bulk_upload_expir_date_error";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Warning));
                }
            } else {
                // Validate if ORIG_LANG == Y
                if (indexRecord.getOriLang() != null && indexRecord.getOriLang().equalsIgnoreCase("Y")) {
                    final String ERROR_DESCRIPTION = "bulk_upload_mandatory_expir_date_error";
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    fileName,
                                    I18NUtil.getMessage(ERROR_DESCRIPTION),
                                    ErrorType.Warning));
                }
            }
        }
    }

    private void setPropertiesGeneric(
            final NodeRef nodeRef,
            final String fileName,
            final Map<QName, Serializable> nodeProperties,
            final QName key,
            final Serializable value,
            final IndexRecord indexRecord,
            final List<ValidationMessage> messages) {
        if (value != null && !value.equals("")) {
            final Serializable oldValue = nodeProperties.get(key);
            if (oldValue != null && oldValue.toString().length() > 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("oldValue:" + oldValue + " new value:" + value);
                }
                if (oldValue.equals(value)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("value are similar");
                    }
                }
            }
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "set new value:"
                                    + value
                                    + " on noderef:"
                                    + nodeRef
                                    + " that represent the file:"
                                    + fileName);
                }
                nodeService.setProperty(nodeRef, key, value);
            } catch (final Exception ex) {
                messages.add(
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(),
                                fileName,
                                "Error while setting property "
                                        + key
                                        + " with value:"
                                        + value
                                        + " on file:"
                                        + fileName
                                        + " cause:"
                                        + ex.getMessage(),
                                ErrorType.Fatal));
            }
        }
    }

    private List<NodeRef> getAllChildsNodeRefs(
            final List<NodeRef> allNodeRefs, final NodeRef nodeRef) {
        final List<NodeRef> childsNodeRefs = new ArrayList<>();

        TypeDefinition typeDef;

        if (nodeService.exists(nodeRef)
                && AccessStatus.ALLOWED.equals(
                permissionService.hasPermission(nodeRef, PermissionService.READ))) {
            final QName type = nodeService.getType(nodeRef);

            // Trick to optimize the efficiency of the code
            if (validTypeMap.containsKey(type)) {
                typeDef = validTypeMap.get(type);
            } else {
                // make sure the type is defined in the data dictionary
                typeDef = dictionaryService.getType(type);
                validTypeMap.put(type, typeDef);
            }

            if (typeDef != null) {
                /** Optimistic optimization for type evaluation */
                // look for File content node
                if (ContentModel.TYPE_CONTENT.equals(type)
                        || dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                    if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
                        if (multilingualContentService.isTranslation(nodeRef)) {
                            final NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
                            if (nodeRef.equals(pivot)) {

                                // Case 1: Add all translation binded.
                                // childsNodeRefs.addAll(multilingualContentService.getTranslations(nodeRef).values());

                                // Case2:
                                // Add the pivot
                                if (!childsNodeRefs.contains(pivot) && !allNodeRefs.contains(pivot)) {
                                    childsNodeRefs.add(pivot);
                                }

                            } else {
                                // Add only the pivot document
                                if (!childsNodeRefs.contains(pivot) && !allNodeRefs.contains(pivot)) {
                                    childsNodeRefs.add(pivot);
                                }
                                // Add the translation document
                                if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
                                    childsNodeRefs.add(nodeRef);
                                }
                            }
                        } else {
                            if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
                                childsNodeRefs.add(nodeRef);
                            }
                        }
                    }
                }

                /** Optimistic optimisationfor type evaluation */

                // look for Space folder node
                else if (ContentModel.TYPE_FOLDER.equals(type)) {
                    createFolderRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                } else if (ApplicationModel.TYPE_FOLDERLINK.equals(type)) {
                    // to avoid infinite recursion ignore folder links
                    // createFolderLinkRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                } else if (ApplicationModel.TYPE_FILELINK.equals(type)) {
                    createFileLinkRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true
                        && dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                    createFolderRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                } else if (dictionaryService.isSubClass(type, ApplicationModel.TYPE_FOLDERLINK)) {
                    // to avoid infinite recursion ignore folder links
                    // createFolderLinkRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                } else if (dictionaryService.isSubClass(type, ApplicationModel.TYPE_FILELINK)) {
                    createFileLinkRepresentation(nodeRef, childsNodeRefs, allNodeRefs);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
                }
            }
        }
        return childsNodeRefs;
    }

    private void createFileLinkRepresentation(
            final NodeRef nodeRef, final List<NodeRef> childsNodeRefs, final List<NodeRef> allNodeRefs) {
        // only display the user has the permissions to navigate to
        // the target of the link
        final NodeRef destRef =
                (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
                childsNodeRefs.add(destRef);
            }
        }
    }

    /*
    private void createFolderLinkRepresentation(
            final NodeRef nodeRef, final List<NodeRef> childsNodeRefs, final List<NodeRef> allNodeRefs) {
        // only display the user has the permissions to navigate to
        // the target of the link
        final NodeRef destRef =
                (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            final List<ChildAssociationRef> listChildAssocs = nodeService.getChildAssocs(destRef);
            if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
                childsNodeRefs.add(nodeRef);
            }
            List<NodeRef> tempNodeRefs;
            for (final ChildAssociationRef childAssoc : listChildAssocs) {
                tempNodeRefs = (List<NodeRef>) getAllChildsNodeRefs(allNodeRefs, childAssoc.getChildRef());
                // remove duplicated
                childsNodeRefs.removeAll(tempNodeRefs);
                tempNodeRefs.removeAll(allNodeRefs);
                childsNodeRefs.addAll(childsNodeRefs);
            }
        }
    }
    */
    private void createFolderRepresentation(
            final NodeRef nodeRef, final List<NodeRef> childsNodeRefs, final List<NodeRef> allNodeRefs) {
        final List<ChildAssociationRef> listChildAssocs = nodeService.getChildAssocs(nodeRef);
        if (!childsNodeRefs.contains(nodeRef) && !allNodeRefs.contains(nodeRef)) {
            childsNodeRefs.add(nodeRef);
        }
        List<NodeRef> tempNodeRefs;
        for (final ChildAssociationRef childAssoc : listChildAssocs) {
            tempNodeRefs = getAllChildsNodeRefs(allNodeRefs, childAssoc.getChildRef());
            // remove duplicate
            tempNodeRefs.removeAll(childsNodeRefs);
            tempNodeRefs.removeAll(allNodeRefs);
            childsNodeRefs.addAll(tempNodeRefs);
        }
    }

    private boolean isContent(final NodeRef nodeRef) {
        boolean isContent = false;
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
                if (ContentModel.TYPE_CONTENT.equals(type)
                        || dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                    isContent = true;
                }
            }
        }
        return isContent;
    }

    public List<IndexRecord> getMetaData(final List<NodeRef> nodeRefs) {
        final List<IndexRecord> indexRecords = new LinkedList<>();
        IndexRecord indexRecord;
        int rowNumber = 0;
        final List<NodeRef> allNodeRefs = new ArrayList<>();
        for (final NodeRef nodeRef : nodeRefs) {
            allNodeRefs.addAll(getAllChildsNodeRefs(allNodeRefs, nodeRef));
        }

        String securityRanking;
        boolean found = false;
        for (final NodeRef nodeRef : allNodeRefs) {

            if (isContent(nodeRef)) {
                // ALLOW only document with low security ranking
                // SecurityRanking
                if (multilingualContentService.isTranslation(nodeRef)) {
                    final NodeRef translationContainer =
                            multilingualContentService.getTranslationContainer(nodeRef);
                    securityRanking =
                            (String)
                                    nodeService.getProperty(
                                            translationContainer, DocumentModel.PROP_SECURITY_RANKING);
                } else {
                    securityRanking =
                            (String) nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING);
                }
                // FIX BUG DIGITCIRCABC-4692
                // TODO check why securityRanking is null with translated document
                if (securityRanking != null
                        && !(DocumentModel.SECURITY_RANKINGS_PUBLIC.equalsIgnoreCase(securityRanking)
                                || DocumentModel.SECURITY_RANKINGS_NORMAL.equalsIgnoreCase(securityRanking))) {
                    // ignore not public and normal security ranking
                    continue;
                }
                // Name (with relative path)
                final String relativePath = zipService.getRelativeLibraryPath(nodeRef);
                found = false;
                for (final IndexRecord indexRecordTemp : indexRecords) {
                    if (indexRecordTemp.getName().equals(relativePath)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }

                rowNumber++;
                indexRecord = new IndexRecordImpl(rowNumber);

                if (indexRecord != null) {

                    indexRecord.setName(relativePath);

                    // Title
                    final String title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
                    if (title != null && title.length() > 0) {
                        indexRecord.setTitle(title);
                    }

                    // Description
                    final String description =
                            (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
                    if (description != null && description.length() > 0) {
                        indexRecord.setDescription(description);
                    }

                    // DOC LANG
                    final Locale docLang =
                            (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
                    if (docLang != null) {
                        indexRecord.setDocLang(docLang.toString());
                    }

                    // Author
                    final String author = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR);
                    if (author != null && author.length() > 0) {
                        indexRecord.setAuthor(author);
                    }

                    // Keywords
                    final List<Keyword> keywords = keywordsService.getKeywordsForNode(nodeRef);
                    String keywordString = "";
                    StringBuilder keywordBuilder = new StringBuilder();
                    Set<Locale> locales;
                    for (final Keyword keyword : keywords) {
                        if (keyword.isKeywordTranslated()) {
                            locales = keyword.getMLValues().getLocales();
                            for (final Locale locale : locales) {
                                keywordBuilder.append("(").append(locale.toString()).append(")");
                                keywordBuilder
                                        .append(" ")
                                        .append(keyword.getMLValues().getValue(locale))
                                        .append(";");
                            }
                        } else {
                            if (keywordBuilder.length() != 0) {
                                keywordBuilder.append(";").append(keyword.getString());
                            } else {
                                keywordBuilder.append(keyword.getString());
                            }
                        }
                    }
                    keywordString = keywordBuilder.toString();
                    if (keywordString != null && keywordString.length() > 0) {
                        indexRecord.setKeywords(keywordString);
                    }

                    // Status
                    final String status =
                            (String) nodeService.getProperty(nodeRef, DocumentModel.PROP_STATUS);
                    if (status != null && status.length() > 0) {
                        indexRecord.setStatus(status);
                    }

                    // Issue Date
                    final Date issueDate =
                            (Date) nodeService.getProperty(nodeRef, DocumentModel.PROP_ISSUE_DATE);
                    if (issueDate != null) {
                        indexRecord.setIssueDate(sdf.get().format(issueDate));
                    }

                    // Reference
                    final String reference =
                            (String) nodeService.getProperty(nodeRef, DocumentModel.PROP_REFERENCE);
                    if (reference != null && reference.length() > 0) {
                        indexRecord.setReference(reference);
                    }

                    // ExpirationDate
                    // Issue Date
                    Date expirationDate = null;
                    if (multilingualContentService.isTranslation(nodeRef)) {
                        final NodeRef pivotNodeRef = multilingualContentService.getPivotTranslation(nodeRef);
                        if (pivotNodeRef == nodeRef) {
                            // Expiration date only for Pivot language
                            final NodeRef translationContainer =
                                    multilingualContentService.getTranslationContainer(nodeRef);
                            expirationDate =
                                    (Date)
                                            nodeService.getProperty(
                                                    translationContainer, DocumentModel.PROP_EXPIRATION_DATE);
                        }
                    } else {
                        expirationDate =
                                (Date) nodeService.getProperty(nodeRef, DocumentModel.PROP_EXPIRATION_DATE);
                    }
                    if (expirationDate != null) {
                        indexRecord.setExpirationDate(sdf.get().format(expirationDate));
                    }

                    // SecurityRanking
                    if (securityRanking != null) {
                        if (multilingualContentService.isTranslation(nodeRef)) {
                            final NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
                            if (nodeRef.equals(pivot)) {
                                // Set SecurityRanking only for pivot.
                                indexRecord.setSecurityRanking(securityRanking);
                            }
                        } else {
                            // normal situation, it's not a translated document
                            indexRecord.setSecurityRanking(securityRanking);
                        }
                    }

                    for (int i = 0; i < DocumentModel.ALL_DYN_PROPS.size(); i++) {
                        QName item = DocumentModel.ALL_DYN_PROPS.get(i);
                        final String dynamicProperty = (String) nodeService.getProperty(nodeRef, item);
                        if (dynamicProperty != null && dynamicProperty.length() > 0) {
                            indexRecord.setDynamicProperty(i + 1, dynamicProperty);
                        }
                    }

                    // TODO not defined yet
                    // indexRecord.getTypeDocument();

                    if (multilingualContentService.isTranslation(nodeRef)) {
                        final NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
                        if (nodeRef.equals(pivot)) {
                            indexRecord.setOriLang("Y");
                        } else {
                            indexRecord.setOriLang("");
                            // Lien vers pivot
                            //	Name (with relative path)
                            final String pivotRelativePath = zipService.getRelativeLibraryPath(pivot);
                            indexRecord.setRelTrans(pivotRelativePath);

                            // Translator
                            if (nodeService.hasAspect(
                                    nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
                                final String translator =
                                        (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR);
                                if (translator != null && translator.length() > 0) {
                                    indexRecord.setTranslator(translator);
                                }
                                indexRecord.setNoContent("Y");
                            } else {
                                indexRecord.setNoContent("N");
                            }
                        }
                    }
                }
                indexRecords.add(indexRecord);
            }
        }
        return indexRecords;
    }

    public void validateEntries(
            final List<IndexRecord> indexFileEntries,
            final List<UploadedEntry> uploadedEntries,
            final List<ValidationMessage> messages) {
        String indexEntryPath;
        String uploadPath;
        boolean found = false;
        for (final IndexRecord indexRecord : indexFileEntries) {
            found = false;
            if (indexRecord.getName().startsWith(File.separator)) {
                indexEntryPath = indexRecord.getName();
            } else {
                indexEntryPath = File.separatorChar + indexRecord.getName();
            }

            if (logger.isTraceEnabled()) {
                logger.trace("indexPath:" + indexEntryPath);
            }
            for (final UploadedEntry uploadedEntry : uploadedEntries) {
                uploadPath = uploadedEntry.getFilePath();
                if (logger.isTraceEnabled()) {
                    logger.trace("uploaded document:" + uploadPath);
                }
                if (uploadPath.equals(indexEntryPath)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("index entry is uploaded:" + uploadPath);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (indexRecord.getNoContent() != null
                        && indexRecord.getNoContent().equalsIgnoreCase("Y")) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("index entry for an empty translation" + indexRecord.getName());
                    }
                    continue;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("index entry is NOT uploaded !!!" + indexRecord.getName());
                    }
                    messages.add(
                            new ValidationMessageImpl(
                                    indexRecord.getRowNumber(),
                                    indexRecord.getName(),
                                    I18NUtil.getMessage(FILE_REFERENCED_NOT_PRESENT_IN_ZIP),
                                    ErrorType.Fatal));
                }
            }
        }
    }

    private IndexRecord getIndexRecord(final List<IndexRecord> indexRecords, final String fileName) {
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
            System.arraycopy(pathElements, 0, folderElements, 0, pathElements.length - 1);
            // fileName
            fileName = pathElements[pathElements.length - 1];
        } else {
            // Reference to the document directly
            fileName = relativePath;
        }
        return fileName;
    }

    private NodeRef searchFile(final NodeRef parentNodeRef, final String relativePath) {
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
            System.arraycopy(pathElements, 0, folderElements, 0, pathElements.length - 1);
            final List<String> folderElementsList = Arrays.asList(folderElements);
            // fileName
            fileName = pathElements[pathElements.length - 1];
            destinationFolderNodeRef = searchFolder(parentNodeRef, folderElementsList);
        } else {
            // Reference to the document directly
            fileName = relativePath;
            destinationFolderNodeRef = parentNodeRef;
        }
        NodeRef fileNodeRef = null;
        if (destinationFolderNodeRef != null) {
            if (logger.isDebugEnabled()) {
                try {
                    final SimplePath simplePath = managementService.getNodePath(destinationFolderNodeRef);
                    logger.debug("Search file:" + fileName + " in:" + simplePath.toString());
                    if (logger.isTraceEnabled()) {
                        final List<SimplePath> childs =
                                managementService.getChildsPath(destinationFolderNodeRef);
                        logger.trace("Childs of the node:");
                        for (final SimplePath child : childs) {
                            logger.trace(child);
                        }
                    }
                } catch (final PathNotFoundException e) {
                    logger.error("Error when geting node path for " + destinationFolderNodeRef.toString(), e);
                }
            }
            fileNodeRef =
                    nodeService.getChildByName(
                            destinationFolderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
        }
        return fileNodeRef;
    }

    private NodeRef searchFolder(final NodeRef parentNodeRef, final List<String> folderElementsList) {
        NodeRef folder = parentNodeRef;

        for (final String folderName : folderElementsList) {
            if (folder != null) {
                if (logger.isDebugEnabled()) {
                    try {
                        final SimplePath simplePath = managementService.getNodePath(folder);
                        logger.debug("Search:" + folderName + " in:" + simplePath.toString());
                        if (logger.isTraceEnabled()) {
                            final List<SimplePath> childs = managementService.getChildsPath(folder);
                            logger.trace("Childs of the node:");
                            for (final SimplePath child : childs) {
                                logger.trace(child.toString());
                            }
                        }
                    } catch (final PathNotFoundException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Path not found:", e);
                        }
                    }
                }
                folder = nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, folderName);
                // folder = fileFolderService.searchSimple(folder, folderName);
            }
        }
        return folder;
    }

    /* IOC */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    public void setFileFolderService(final FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
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

    public ZipService getZipService() {
        return zipService;
    }

    public void setZipService(final ZipService zipService) {
        this.zipService = zipService;
    }

    public IndexService getIndexService() {
        return indexService;
    }

    public void setIndexService(final IndexService indexService) {
        this.indexService = indexService;
    }

    public KeywordsService getKeywordsService() {
        return keywordsService;
    }

    public void setKeywordsService(final KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
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
            final ContentFilterLanguagesService contentFilterLanguagesService) {
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
            final MultilingualContentService multilingualContentService) {
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
