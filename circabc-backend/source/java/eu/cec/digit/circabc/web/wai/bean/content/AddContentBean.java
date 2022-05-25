/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.bean.content;


import com.ibm.icu.util.Calendar;
import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.content.ContentBusinessSrv;
import eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.servlet.UploadFileServlet;
import eu.cec.digit.circabc.web.servlet.UploadFileServletConfig;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.generic.EditNodePropertiesDialog;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Bean to handle the upload content process on the WAI part.
 *
 * @author Guillaume
 * @author Yanick Pignot
 * @author Pierre Beauregard
 */
public class AddContentBean extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcAddContentBean";
    protected static final String FILENAME_REGEX = "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)";
    final static Log logger = LogFactory.getLog(AddContentBean.class);
    private static final String FILE_LIMIT_REACHED_TITLE = "file_limit_reached_title";
    private static final String FILE_LIMIT_REACHED = "file_limit_reached";
    private static final String STATUS = "status";
    private static final String DESCRIPTION = "description";
    private static final String TITLE = "title";
    private static final long serialVersionUID = 8518953203603149020L;
    private static final String MSG_ERR_UNEXPECTED_ERROR = "add_content_unexpected_error";
    private static final String MSG_ERR_ONE_ERROR = "add_content_error_one";
    private static final String MSG_UPLOAD_SUCCESS = "library_add_upload_successfull";
    private static final String MSG_CREATE_NOTHING = "add_content_no_submit";
    private static final String MSG_CREATE_ONE = "add_content_submit_one";
    private static final String MSG_INVALID_FILE = "add_content_invalid_file";
    private static final String AUTHOR = "author";
    private static final String VALID_NAME_LOCK = "valid-name-lock";
    private MetadataManager metadataManager;
    /**
     * Where we were at the upload process
     */
    private String locationBeforeUpload = CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE;
    private transient DataModel uploadedFilesDataModel;
    private List<CircabcUploadedFile> uploadedFiles;
    private Map<NodeRef, String> createdNodeRefs;
    private NodeRef toEditPropsNode = null;
    private boolean toNotifyAfterEdit = false;
    private List<SelectItem> possibleStatus;
    private List<SelectItem> possibleSecurityRanking;
    private List<SelectItem> possibleKeywords;
    private Integer startYear;
    private UploadedFile submittedFile;
    private Boolean isMultiLingualDocument;
    private String submittedLanguageDocument;
    private List<SelectItem> pivotDocuments;
    private UploadFileServletConfig fileUploadLimit;
    private KeywordsService keywordsService;
    private MetadataEncryptor metadataEncryptor = null;
    private CircabcRenditionService circabcRenditionService = null;
    private boolean hasPermission;
   

    /**
     * @return the submittedFile
     */
    public UploadedFile getSubmittedFile() {
        return submittedFile;
    }

    /**
     * @param submittedFile the submittedFile to set
     */
    public void setSubmittedFile(UploadedFile submittedFile) {
        this.submittedFile = submittedFile;
    }

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        final FacesContext fc = FacesContext.getCurrentInstance();

        possibleStatus = new ArrayList<>();
        possibleStatus.add(0, new SelectItem(DocumentModel.STATUS_VALUE_DRAFT,
                translate(DocumentModel.STATUS_VALUE_DRAFT.toLowerCase())));
        possibleStatus.add(1, new SelectItem(DocumentModel.STATUS_VALUE_FINAL,
                translate(DocumentModel.STATUS_VALUE_FINAL.toLowerCase())));
        possibleStatus.add(2, new SelectItem(DocumentModel.STATUS_VALUE_RELEASE,
                translate(DocumentModel.STATUS_VALUE_RELEASE.toLowerCase())));

        possibleSecurityRanking = new ArrayList<>();
        possibleSecurityRanking.add(0, new SelectItem(DocumentModel.SECURITY_RANKINGS_NORMAL,
                translate(DocumentModel.SECURITY_RANKINGS_NORMAL.toLowerCase())));
        possibleSecurityRanking.add(1, new SelectItem(DocumentModel.SECURITY_RANKINGS_SENSITIVE,
                translate(DocumentModel.SECURITY_RANKINGS_SENSITIVE.toLowerCase())));
        possibleSecurityRanking.add(2, new SelectItem(DocumentModel.SECURITY_RANKINGS_SPECIAL_HANDLING,
                translate(DocumentModel.SECURITY_RANKINGS_SPECIAL_HANDLING.toLowerCase())));
        possibleSecurityRanking.add(3, new SelectItem(DocumentModel.SECURITY_RANKINGS_PUBLIC,
                translate(DocumentModel.SECURITY_RANKINGS_PUBLIC.toLowerCase())));
        possibleSecurityRanking.add(4, new SelectItem(DocumentModel.SECURITY_RANKINGS_INTERNAL,
                translate(DocumentModel.SECURITY_RANKINGS_INTERNAL.toLowerCase())));
        possibleSecurityRanking.add(5, new SelectItem(DocumentModel.SECURITY_RANKINGS_LIMITED,
                translate(DocumentModel.SECURITY_RANKINGS_LIMITED.toLowerCase())));

        possibleKeywords = new ArrayList<>();
        List<Keyword> lKeyword = keywordsService.getKeywords(
                getManagementService().getCurrentInterestGroup(this.getActionNode().getNodeRef()));
        for (Keyword keyword : lKeyword) {
            if (keyword.getMLValues().containsKey(fc.getViewRoot().getLocale())) {
                possibleKeywords.add(new SelectItem(keyword.getId().toString(),
                        keyword.getMLValues().get(fc.getViewRoot().getLocale())));
            } else {
                possibleKeywords
                        .add(new SelectItem(keyword.getId().toString(), keyword.getMLValues().toString()));
            }
        }

        startYear = new GregorianCalendar().get(Calendar.YEAR) + 20;

        isMultiLingualDocument = false;

        pivotDocuments = new ArrayList<>();

        // in the restaure mode, the parameters can be null
        if (parameters != null) {
            clearUpload();
        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException("Node id parameter is mandatory");
        }
    }

    public /*synchronized*/ String finish() {

        final FacesContext context = FacesContext.getCurrentInstance();
        final String defaultOutcome = getDefaultFinishOutcome();
        String outcome = null;

        // check the isFinished flag to stop the finish button
        // being pressed multiple times
        if (!this.isFinished) {

            this.isFinished = true;

            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
                public String execute() throws Throwable {
                    // call the actual implementation
                    return finishImpl(context, defaultOutcome);
                }
            };


                    outcome = txnHelper.doInTransaction(callback, false, true);
    

                // allow any subclasses to perform post commit processing
                // i.e. resetting state or setting status messages
                outcome = doPostCommitProcessing(context, outcome);

                // remove container variable
                context.getExternalContext().getSessionMap().remove(
                        AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);
    
        } else {
            Utils.addErrorMessage(Application.getMessage(context, "error_wizard_completed_already"));
        }

        return outcome;
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {
        if (uploadedFiles == null || uploadedFiles.size() == 0) {
            // should not appear, just let a message to user
            Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_CREATE_NOTHING));
            return null;
        } else {

            if (translationsAreValid()) {

                try {

                    final NodeRef parent = getParentFolder();

                    createdNodeRefs = new HashMap<>(uploadedFiles.size());

                    for (SelectItem item : pivotDocuments) {
                        CircabcUploadedFile bean = getBeanFromUploadedFiles(item.getValue().toString());
                        createContent(parent, bean);
                    }

                    for (CircabcUploadedFile bean : uploadedFiles) {
                        NodeRef noderRef = createContent(parent, bean);
                        if (noderRef != null) {
                            circabcRenditionService.addRequest(noderRef);
                        }
                    }

                    return outcome
                            + CircabcNavigationHandler.OUTCOME_SEPARATOR
                            + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
                } catch (final BusinessStackError validationErrors) {
                    for (final String msg : validationErrors.getI18NMessages()) {
                        Utils.addErrorMessage(msg);
                    }
                    this.isFinished = false;
                    logger.error(validationErrors.getMessage(), validationErrors);
                    return null;
                } catch (Throwable t) {
                    Utils.addErrorMessage(translate(MSG_ERR_UNEXPECTED_ERROR, t.getMessage()));
                    this.isFinished = false;
                    logger.error(translate(MSG_ERR_UNEXPECTED_ERROR, t.getMessage()), t);
                    return null;
                }
            } else {
                this.isFinished = false;
                return null;
            }
        }
    }

    private boolean translationsAreValid() {

        Boolean result = true;

        for (CircabcUploadedFile file : uploadedFiles) {
            if (file.getIsTranslation()) {
                CircabcUploadedFile pivot = getBeanFromUploadedFiles(file.getIsTranslationOfDocument());

                if (file.getLanguageName().equals(pivot.getLanguageName())) {
                    Utils.addErrorMessage(
                            translate("add_content_translation_pivot_same_language", file.getFileName(),
                                    file.getLanguageTitle(), pivot.getFileName()));
                    result = false;
                }

            }

            if (file.getIsPivotMultilingual()) {
                //<Language, File>
                Map<String, String> usedLanguages = new HashMap<>();
                for (String key : file.getSubmitedProperties().keySet()) {
                    if (key.startsWith(":")) {
                        if (usedLanguages.containsKey(file.getSubmitedProperty(key))) {
                            Utils.addErrorMessage(translate("add_content_translations_same_language",
                                    key.substring(1),
                                    usedLanguages.get(file.getSubmitedProperty(key)),
                                    getFilterLanguage(file.getSubmitedProperty(key)))
                            );
                            result = false;
                        } else {
                            usedLanguages.put(file.getSubmitedProperty(key), key.substring(1));
                        }
                    }
                }
            }
        }

        return result;
    }

    private CircabcUploadedFile getBeanFromUploadedFiles(String value) {
        CircabcUploadedFile result = null;

        for (CircabcUploadedFile file : uploadedFiles) {
            if (file.getFileName().equals(value)) {
                result = file;
                break;
            }
        }
        return result;
    }

    public void uploadFile(ActionEvent event) {
        if (submittedFile != null) {
            final String name = FilenameUtils.getName(submittedFile.getName());
            if (CircabcConfig.ECHA) {
                final List<String> allowedFileExtensions = ESAPI.securityConfiguration()
                        .getAllowedFileExtensions();
                try {
                    ESAPI.validator().getValidFileName("submitted file", name, allowedFileExtensions, false);
                } catch (ValidationException | IntrusionException vex) {
                    Utils.addStatusMessage(FacesMessage.SEVERITY_WARN,
                            translate(MSG_INVALID_FILE, allowedFileExtensions));
                    if (logger.isErrorEnabled()) {
                        logger.error("Invalid file submitted", vex);
                    }
                    return;
                }
            }
            CircabcUploadedFile newFile = new CircabcUploadedFile();

            File tempFile = TempFileProvider.createTempFile("circabcUpload", "tmp");
            FileOutputStream fs = null;
            Boolean failed = false;
            try {

                fs = new FileOutputStream(tempFile);
                fs.write(submittedFile.getBytes());

            } catch (java.io.FileNotFoundException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Problem during upload content, file does not exists", e);
                }
                failed = true;
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Problem during writing to file in content upload", e);
                }
                failed = true;
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Problem when closing temporary file output stream", e);
                        }
                    }
                }

            }

            if (!failed) {
                newFile.setFile(tempFile);

                Map<QName, Serializable> props = null;

                try {
                    //	Try and extract metadata from the file
                    String mimetype = metadataManager.guessMimetype(submittedFile.getName());
                    String encoding = metadataManager
                            .guessEncoding(new BufferedInputStream(new FileInputStream(newFile.getFile())),
                                    mimetype);

                    final ContentReader cr = new FileContentReader(newFile.getFile());
                    cr.setMimetype(mimetype);
                    cr.setEncoding(encoding);

                    props = metadataManager.extractContentMetadata(cr);

                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error during extracting metadata of one file in the Add COntent Page", e);
                    }
                }

                newFile.setFileName(name);
                newFile.setFilePath(tempFile.getPath());
                newFile.addSubmitedProperty(CircabcUploadedFile.NAME, name);

                if (props != null) {
                    if (props.containsKey(ContentModel.PROP_AUTHOR)) {
                        newFile.addSubmitedProperty(CircabcUploadedFile.AUTHOR,
                                props.get(ContentModel.PROP_AUTHOR).toString());
                    }

                    if (props.containsKey(ContentModel.PROP_TITLE)) {
                        newFile.addSubmitedProperty(CircabcUploadedFile.TITLE,
                                cleanTranslatationMetadata(props.get(ContentModel.PROP_TITLE).toString()));
                    } else {
                        newFile.addSubmitedProperty(CircabcUploadedFile.TITLE, name);
                    }

                    if (props.containsKey(ContentModel.PROP_DESCRIPTION)) {
                        newFile.addSubmitedProperty(CircabcUploadedFile.DESCRIPTION,
                                cleanTranslatationMetadata(props.get(ContentModel.PROP_DESCRIPTION).toString()));
                    }
                }

                if (isMultiLingualDocument) {
                    newFile.addSubmitedProperty(CircabcUploadedFile.IS_PIVOT, "true");
                    newFile.addSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION, "false");
                    newFile.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE, submittedLanguageDocument);
                    newFile.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE_TITLE,
                            getFilterLanguage(submittedLanguageDocument));

                    pivotDocuments.add(new SelectItem(newFile.getFileName(), newFile.getFileName()));
                }

                this.uploadedFiles.add(newFile);
            }

            this.uploadedFilesDataModel = null;

            isMultiLingualDocument = false;

        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_CREATE_NOTHING));
        }
    }

    private String cleanTranslatationMetadata(String rawString) {
        String clean = rawString.substring(4);
        clean = clean.substring(0, clean.length() - 1);

        return clean;
    }

    private String getFilterLanguage(String submittedLanguageDocument) {

        SelectItem[] items = getFilterLanguages();
        String result = "";

        for (SelectItem item : items) {
            if (item.getValue().equals(submittedLanguageDocument)) {
                result = item.getLabel();
                break;
            }
        }

        return result;
    }

    public void removeMultilinguism(ActionEvent event) {
        CircabcUploadedFile fileBean = (CircabcUploadedFile) this.uploadedFilesDataModel.getRowData();

        Integer pos = uploadedFiles.indexOf(fileBean);

        deleteMultilinguismProperties(fileBean);

        for (CircabcUploadedFile file : uploadedFiles) {
            if (file.getSubmitedProperties()
                    .containsKey(CircabcUploadedFile.IS_TRANSLATION_OF_DOCUMENT_TITLE)) {
                if (file.getSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION_OF_DOCUMENT_TITLE)
                        .equals(fileBean.getFileName())) {
                    deleteTranslationProperties(file);
                }
            }
        }

        uploadedFiles.set(pos, fileBean);

        int found = 0;

        for (int i = 0; i < pivotDocuments.size(); i++) {
            if (pivotDocuments.get(i).getValue().equals(fileBean.getFileName())) {
                found = i;
                break;
            }
        }

        pivotDocuments.remove(found);

        uploadedFilesDataModel = null;
    }

    private void deleteMultilinguismProperties(CircabcUploadedFile fileBean) {

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.IS_PIVOT);
        fileBean.addSubmitedProperty(CircabcUploadedFile.IS_PIVOT, "false");

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.MAIN_LANGUAGE);
        fileBean.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE, "false");

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.MAIN_LANGUAGE_TITLE);
        fileBean.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE_TITLE, "");

        // if a key start with ":" this is to identify the pivot document reference
        String keyPivotRef = "";

        for (String keyTmp : fileBean.getSubmitedProperties().keySet()) {
            if (keyTmp.startsWith(":")) {
                keyPivotRef = keyTmp;
            }
        }

        if (keyPivotRef.length() > 0) {
            fileBean.removeSubmitedProperty(keyPivotRef);
        }

    }

    public void valiteFileSize(FacesContext context, UIComponent component, Object parameters) {
        final UploadContext requestContext = new ServletRequestContext(
                (HttpServletRequest) context.getExternalContext().getRequest());

        if (requestContext.contentLength()
                > this.getFileUploadLimit().getMaxSizeInMegaBytes() * 1024 * 1024) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(translate(FILE_LIMIT_REACHED_TITLE));
            message.setDetail(
                    translate(FILE_LIMIT_REACHED, this.getFileUploadLimit().getMaxSizeInMegaBytes()));

            throw new ValidatorException(message);
        }
    }

    protected NodeRef getParentFolder() {
        return getActionNode().getNodeRef();
    }

    protected NodeRef createContent(final NodeRef parent, CircabcUploadedFile bean)
            throws FileNotFoundException, IOException {
        NodeRef createdRef = null;

        if (!createdNodeRefs.containsValue(bean.getFileName())) {
            boolean notificationDisabled = bean.isNotificationDisabled();
            if (bean.isEditPropertiesAfter() && !notificationDisabled) {
                // disable notification when creating content
                notificationDisabled = true;
            }

            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.NAME)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.NAME).length() > 0) {
                    createdRef = getContentBusinessSrv()
                            .addContent(parent, bean.getSubmitedProperty(CircabcUploadedFile.NAME),
                                    bean.getFile(), notificationDisabled);
                } else {
                    createdRef = getContentBusinessSrv()
                            .addContent(parent, bean.getFileName(), bean.getFile(), notificationDisabled);
                }
            } else {
                createdRef = getContentBusinessSrv()
                        .addContent(parent, bean.getFileName(), bean.getFile(), notificationDisabled);
            }

            // fix for DIGIT-CIRCABC-2139
            super.getNodeService().setProperty(createdRef, ContentModel.PROP_AUTO_VERSION_PROPS, false);
            //super.getNodeService().setProperty(createdRef, ContentModel.PROP_VERSION_LABEL, "1.0");

            /*
             * Update metadata
             */

            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.TITLE)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.TITLE).length() > 0) {
                    String tmpTitle = getSecurityService()
                            .getCleanHTML(bean.getSubmitedProperties().get(CircabcUploadedFile.TITLE), false);

                    super.getNodeService().setProperty(createdRef, ContentModel.PROP_TITLE, tmpTitle);
                }
            }
            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.DESCRIPTION)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.DESCRIPTION).length() > 0) {
                    String tmpDescription = getSecurityService()
                            .getCleanHTML(bean.getSubmitedProperties().get(CircabcUploadedFile.DESCRIPTION),
                                    true);

                    super.getNodeService()
                            .setProperty(createdRef, ContentModel.PROP_DESCRIPTION, tmpDescription);
                }
            }
            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.STATUS)) {
                if (((String) bean.getSubmitedProperties().get(CircabcUploadedFile.STATUS)).length() > 0) {
                    super.getNodeService().setProperty(createdRef, DocumentModel.PROP_STATUS,
                            bean.getSubmitedProperty(CircabcUploadedFile.STATUS));
                }
            }
            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.AUTHOR)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.AUTHOR).length() > 0) {
                    String tmpAuthor = getSecurityService()
                            .getCleanHTML(bean.getSubmitedProperties().get(CircabcUploadedFile.AUTHOR), false);

                    super.getNodeService().setProperty(createdRef, ContentModel.PROP_AUTHOR, tmpAuthor);
                }
            }
            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.REFERENCE)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.REFERENCE).length() > 0) {
                    super.getNodeService().setProperty(createdRef, DocumentModel.PROP_REFERENCE,
                            bean.getSubmitedProperty(CircabcUploadedFile.REFERENCE));
                }
            }
            if (bean.getSubmitedProperties().containsKey(CircabcUploadedFile.SECURITY_RANKING)) {
                if (bean.getSubmitedProperties().get(CircabcUploadedFile.SECURITY_RANKING).length() > 0) {
                    super.getNodeService().setProperty(createdRef, DocumentModel.PROP_SECURITY_RANKING,
                            bean.getSubmitedProperty(CircabcUploadedFile.SECURITY_RANKING));
                }
            }
            if (bean.getIssueDate() != null) {
                super.getNodeService()
                        .setProperty(createdRef, DocumentModel.PROP_ISSUE_DATE, bean.getIssueDate());
            }
            if (bean.getExpirationDate() != null) {
                super.getNodeService()
                        .setProperty(createdRef, DocumentModel.PROP_EXPIRATION_DATE, bean.getExpirationDate());
            }
            if (bean.getKeywords() != null) {
                for (String k : bean.getKeywords()) {
                    keywordsService.addKeywordToNode(createdRef, keywordsService.buildKeywordWithId(k));
                }
            }
            // Add encrypted aspect to the content
            Object encrypted = bean.getSubmitedProperties().get(CircabcUploadedFile.ENCRYPTED);
            if (encrypted != null && (Boolean) encrypted) {
                Map<QName, Serializable> allProperties = new HashMap<>();
                allProperties.put(DocumentModel.PROP_ENCRYPTEDTEXT1, "");
                allProperties.put(DocumentModel.PROP_ENCRYPTEDTEXT2, "");
                allProperties = metadataEncryptor.encrypt(allProperties);

                getNodeService().addAspect(createdRef, DocumentModel.ASPECT_ENCRYPTABLE, allProperties);
            }

            if (bean.getIsPivotMultilingual()) {
                /*
                 * DO the multilinguism for pivot document
                 */

                final Locale locale = I18NUtil
                        .parseLocale(bean.getSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE));

                getMultilingualContentService().makeTranslation(createdRef, locale);

                final NodeRef mlContainer = getMultilingualContentService()
                        .getTranslationContainer(createdRef);

                // if the author of the node is not set, set it with the default author name of
                // the new ML Container
                String nodeAuthor = (String) getNodeService()
                        .getProperty(createdRef, ContentModel.PROP_AUTHOR);

                if (nodeAuthor == null || nodeAuthor.length() < 1) {
                    getNodeService().setProperty(createdRef, ContentModel.PROP_AUTHOR, "");
                }

                // set properties of the ml container
                getNodeService().setProperty(mlContainer, ContentModel.PROP_AUTHOR, "");
            } else if (bean.getIsTranslation()) {
                /*
                 * DO the multilinguism for translated document
                 */
                getMultilingualContentService().addTranslation(createdRef, getPivotRef(
                        bean.getSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION_OF_DOCUMENT_TITLE)),
                        I18NUtil.parseLocale(bean.getSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE)));
            }

            if (createdRef != null) {
                createdNodeRefs.put(createdRef, bean.getFileName());

                if (bean.isEditPropertiesAfter()) {
                    toEditPropsNode = createdRef;
                    toNotifyAfterEdit = !bean.isNotificationDisabled();
                }

                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, createContenetOKMessage(bean));
            } else {
                Utils.addErrorMessage(createContentErrorMessage(bean));
            }
        }

        return createdRef;
    }

    private NodeRef getPivotRef(String fileName) {

        NodeRef result = null;

        for (NodeRef ref : createdNodeRefs.keySet()) {
            if (createdNodeRefs.get(ref).equals(fileName)) {
                result = ref;
                break;
            }
        }

        return result;
    }

    protected String createContentErrorMessage(CircabcUploadedFile bean) {
        return translate(MSG_ERR_ONE_ERROR, bean.getFileName());
    }

    protected String createContenetOKMessage(CircabcUploadedFile bean) {
        return translate(MSG_CREATE_ONE, bean.getFileName());
    }


    public DataModel getUploadedFilesDataModel() {
        if (this.uploadedFilesDataModel == null) {
            this.uploadedFilesDataModel = new SortableModel();
            if (uploadedFiles == null) {
                this.uploadedFilesDataModel.setWrappedData(Collections.<CircabcUploadedFile>emptySet());
            } else {
                this.uploadedFilesDataModel.setWrappedData(uploadedFiles);

            }
        }

        return uploadedFilesDataModel;
    }

    public void removeSelection(final ActionEvent event) {
        final CircabcUploadedFile fileBean = (CircabcUploadedFile) this.uploadedFilesDataModel
                .getRowData();

        if (fileBean.getIsPivotMultilingual()) {
            this.removeMultilinguism(event);
        }

        this.uploadedFiles.remove(fileBean);

        removeFile(fileBean.getFile());
        uploadedFilesDataModel = null;
    }

    public void addTranslation(ActionEvent event) {
        final CircabcUploadedFile fileBean = (CircabcUploadedFile) this.uploadedFilesDataModel
                .getRowData();

        Integer pos = uploadedFiles.indexOf(fileBean);

        fileBean.addSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION, "true");
        fileBean.addSubmitedProperty(CircabcUploadedFile.IS_PIVOT, "false");
        fileBean.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE_TITLE,
                getFilterLanguage(fileBean.getSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE)));

        uploadedFiles.set(pos, fileBean);

        CircabcUploadedFile pivot = getBeanFromUploadedFiles(fileBean.getIsTranslationOfDocument());
        Integer posPivot = uploadedFiles.indexOf(pivot);
        // ":" + filename to reference the pivot document name
        pivot.addSubmitedProperty(":" + fileBean.getFileName(),
                fileBean.getSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE));
        uploadedFiles.set(posPivot, pivot);

        uploadedFilesDataModel = null;
    }

    public void removeTranslation(ActionEvent event) {
        final CircabcUploadedFile fileBean = (CircabcUploadedFile) this.uploadedFilesDataModel
                .getRowData();

        Integer pos = uploadedFiles.indexOf(fileBean);

        CircabcUploadedFile pivot = getBeanFromUploadedFiles(fileBean.getIsTranslationOfDocument());

        deleteTranslationProperties(fileBean);
        uploadedFiles.set(pos, fileBean);

        Integer posPivot = uploadedFiles.indexOf(pivot);

        pivot.removeSubmitedProperty(":" + fileBean.getFileName());
        uploadedFiles.set(posPivot, pivot);

        uploadedFilesDataModel = null;
    }

    /**
     * @param fileBean
     */
    private void deleteTranslationProperties(final CircabcUploadedFile fileBean) {

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.IS_TRANSLATION);
        fileBean.addSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION, "false");

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.MAIN_LANGUAGE);
        fileBean.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE, "false");

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.MAIN_LANGUAGE_TITLE);
        fileBean.addSubmitedProperty(CircabcUploadedFile.MAIN_LANGUAGE_TITLE, "");

        fileBean.getSubmitedProperties().remove(CircabcUploadedFile.IS_TRANSLATION_OF_DOCUMENT_TITLE);
        fileBean.addSubmitedProperty(CircabcUploadedFile.IS_TRANSLATION_OF_DOCUMENT_TITLE, "");
    }

    public void addFile(final CircabcUploadedFile fileBean) {
        if (fileBean != null && fileBean.getFile() != null) {
            String fileName;
            try {
                fileName = getPropertiesBusinessSrv()
                        .computeValidUniqueName(getActionNode().getNodeRef(), fileBean.getFileName());
                fileBean.setFileName(fileName);
                fileBean.addSubmitedProperty(TITLE, "");
                fileBean.addSubmitedProperty(DESCRIPTION, "");
                fileBean.addSubmitedProperty(STATUS, DocumentModel.STATUS_VALUE_DRAFT);
                fileBean.addSubmitedProperty(AUTHOR, "");

                uploadedFiles.add(fileBean);

                final String message = addFileOKMessage(fileName);
                ErrorsRenderer
                        .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));

                uploadedFilesDataModel = null;
            } catch (final BusinessStackError e) {
                for (final String message : e.getI18NMessages()) {
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
                    removeFile(fileBean.getFile());
                }
            }
        }
    }


    protected String addFileOKMessage(String fileName) {
        return translate(MSG_UPLOAD_SUCCESS, fileName);
    }

    public int getUploadedFileCount() {
        return uploadedFiles == null ? 0 : uploadedFiles.size();
    }


    protected void clearUpload() {
        if (uploadedFiles != null) {
            for (final CircabcUploadedFile bean : uploadedFiles) {
                removeFile(bean.getFile());
            }
        }

        //remove the file upload bean from the session
        final FacesContext fc = FacesContext.getCurrentInstance();
        final HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);

        UploadFileServlet.resetUploadedFiles(session);

        uploadedFiles = new ArrayList<>();
        pivotDocuments = new ArrayList<>();
        uploadedFilesDataModel = null;
        createdNodeRefs = null;
        toEditPropsNode = null;
        toNotifyAfterEdit = false;
    }

    /**
     * @param file
     */
    protected void removeFile(final File file) {
        if (file != null && file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted && logger.isWarnEnabled()) {
                try {
                    logger.warn("Unable to delete file : " + file.getCanonicalPath());
                } catch (IOException e) {
                    logger.warn("Unable to get CanonicalPath for : " + file.getPath(), e);
                }
            }
        }
    }

    /**
     * If the file was uploaded successfully, we open the edit content properties dialog
     *
     * @param context The Face context
     * @param outcome The default outcome - not used
     * @return String The outcome to edit content properties dialog
     */
    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        // Test if previous error
        if (outcome == null) {
            return null;
        }

        Node node = null;
        final String service;
        if (getActionNode().hasAspect(CircabcModel.ASPECT_INFORMATION)) {
            service = "Information";
        } else {
            service = "Library";
        }

        if (createdNodeRefs != null) {
            for (final NodeRef createdNodeRef : createdNodeRefs.keySet()) {
                node = new Node(createdNodeRef);
                setLogRecord(node);
                logRecord.setService(service);
                logRecord.setActivity("Upload document");
                logRecord.setOK(true);
                logRecord.setInfo(node.getName());
                getLogService().log(logRecord);
            }
        }

        final String newOutcome;

        if (toEditPropsNode != null) {
            if (!node.getNodeRef().equals(toEditPropsNode)) {
                node = new Node(toEditPropsNode);
            }
            //set the current node being the new created node
            getBrowseBean().setDocument(node);

            // as we were successful, go to the set properties dialog and set the dialog parameters
            final Map<String, String> parameters = new HashMap<>(4);
            parameters.put(BaseWaiDialog.NODE_ID_PARAMETER, node.getId());
            parameters.put(BaseWaiDialog.SERVICE_PARAMETER, service);
            parameters.put(BaseWaiDialog.ACTIVITY_PARAMETER, "EditDocumentProperties");
            parameters.put(EditNodePropertiesDialog.NOTIFY_AFTER_SETTING_PROPERTIES,
                    String.valueOf(toNotifyAfterEdit));

            Beans.getEditNodePropertiesDialog().init(parameters);

            // We don't close the dialog cause the upload process clear the stack but we fill it with the good thing
            FacesContext.getCurrentInstance().getViewRoot().setViewId(this.locationBeforeUpload);
            newOutcome = CircabcNavigationHandler.CLOSE_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + EditNodePropertiesDialog.DIALOG_CALL;
        } else {
            newOutcome = outcome;
        }

        clearUpload();
        return newOutcome;

    }

    /**
     * Action handler called when the dialog is canceled
     */
    @Override
    public String cancel() {
        super.cancel();

        clearUpload();

        return "wai:browse-wai";
    }

    public void start(final ActionEvent event) {
        // NOTE: this is a temporary solution to allow us to use the new dialog
        // framework beans outside of the dialog framework, we need to do
        // this because the uploading requires a separate non-JSF form, this
        // approach can not be used in the current dialog framework. Until
        // we have a pure JSF upload solution we need this initialisation

        final UIComponent component = event.getComponent();
        final Map<String, String> params = ((UIActionLink) component).getParameterMap();

        init(params);

        // launch the dialog
        final FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().getNavigationHandler()
                .handleNavigation(context, null, getDialogToStart());
    }

    protected String getDialogToStart() {
        return "addContentWai";
    }

    protected void setLogRecord(Node node) {
        logRecord.setDocumentID(
                (Long) getNodeService().getProperty(node.getNodeRef(), ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(node.getNodeRef());
        logRecord.setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
        logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        Path path = node.getNodePath();
        String displayPath = PathUtils.getCircabcPath(path, true);
        displayPath = displayPath.endsWith("contains") ? displayPath
                .substring(0, displayPath.length() - "contains".length()) : displayPath;
        logRecord.setPath(displayPath);
    }

    /**
     * @return the showOtherProperties
     */
    public final boolean isShowOtherProperties() {
        return false;
    }

    /**
     * @return the showOtherProperties
     */
    public final void setShowOtherProperties(boolean ignore) {
        // do nothing, the value is submited in upload for (see UploadedFile.addSubmitedProperty(edit-properties, value)
    }

    /**
     * @return the disableNotification
     */
    public final boolean isDisableNotification() {
        return false;
    }

    /**
     * @return the disableNotification
     */
    public final void setDisableNotification(boolean ignore) {
        // do nothing, the value is submited in upload for (see UploadedFile.addSubmitedProperty(check-disable-notif, value)
    }

    public String getBrowserTitle() {
        // managed in jsp
        return null;
    }

    public String getPageIconAltText() {
        // managed in jsp
        return null;
    }

    /**
     * @return the uploadedFiles
     */
    public final List<CircabcUploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    /**
     * @param uploadedFiles the uploadedFiles to set
     */
    public void setUploadedFiles(List<CircabcUploadedFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }


    /**
     * @return
     */
    protected CircabcUploadedFile getFirstUploadedFile() {
        if (uploadedFiles == null || uploadedFiles.size() < 1) {
            return null;
        } else {
            return getUploadedFiles().get(0);
        }
    }

    /**
     * @return the contentBusinessSrv
     */
    protected final ContentBusinessSrv getContentBusinessSrv() {
        return getBusinessRegistry().getContentBusinessSrv();
    }

    /**
     * @return the propertiesBusinessSrv
     */
    protected final PropertiesBusinessSrv getPropertiesBusinessSrv() {
        return getBusinessRegistry().getPropertiesBusinessSrv();
    }

    protected Map<NodeRef, String> getCreatedNodeRefs() {
        return createdNodeRefs;
    }

    protected void setCreatedNodeRefs(Map<NodeRef, String> createdNodeRefs) {
        this.createdNodeRefs = createdNodeRefs;
    }

    /**
     * @return the possibleStatus
     */
    public List<SelectItem> getPossibleStatus() {
        return possibleStatus;
    }


    /**
     * @param possibleStatus the possibleStatus to set
     */
    public void setPossibleStatus(List<SelectItem> possibleStatus) {
        this.possibleStatus = possibleStatus;
    }

    /**
     * @return the possibleSecurityRanking
     */
    public List<SelectItem> getPossibleSecurityRanking() {
        return possibleSecurityRanking;
    }

    /**
     * @param possibleSecurityRanking the possibleSecurityRanking to set
     */
    public void setPossibleSecurityRanking(List<SelectItem> possibleSecurityRanking) {
        this.possibleSecurityRanking = possibleSecurityRanking;
    }

    /**
     * @return the possibleKeywords
     */
    public List<SelectItem> getPossibleKeywords() {
        return possibleKeywords;
    }

    /**
     * @param possibleKeywords the possibleKeywords to set
     */
    public void setPossibleKeywords(List<SelectItem> possibleKeywords) {
        this.possibleKeywords = possibleKeywords;
    }

    /**
     * @return the fileUploadLimit
     */
    public UploadFileServletConfig getFileUploadLimit() {
        return fileUploadLimit;
    }

    /**
     * @param fileUploadLimit the fileUploadLimit to set
     */
    public void setFileUploadLimit(UploadFileServletConfig fileUploadLimit) {
        this.fileUploadLimit = fileUploadLimit;
    }

    /**
     * @return the isMultiLingualDocument
     */
    public Boolean getIsMultiLingualDocument() {
        return isMultiLingualDocument;
    }

    /**
     * @param isMultiLingualDocument the isMultiLingualDocument to set
     */
    public void setIsMultiLingualDocument(Boolean isMultiLingualDocument) {
        this.isMultiLingualDocument = isMultiLingualDocument;
    }

    /**
     * @return the complete list of available languages for the multilinguism
     */
    public SelectItem[] getFilterLanguages() {
        return getUserPreferencesBean().getContentFilterLanguages(false);
    }

    public List<SelectItem> getPivotDocuments() {
        return this.pivotDocuments;
    }

    /**
     * @param pivotDocuments the pivotDocuments to set
     */
    public void setPivotDocuments(List<SelectItem> pivotDocuments) {
        this.pivotDocuments = pivotDocuments;
    }

    public Boolean getHasPivotDocuments() {
        return this.pivotDocuments.size() > 0;
    }

    /**
     * @return the submittedLanguageDocument
     */
    public String getSubmittedLanguageDocument() {
        return submittedLanguageDocument;
    }

    /**
     * @param submittedLanguageDocument the submittedLanguageDocument to set
     */
    public void setSubmittedLanguageDocument(String submittedLanguageDocument) {
        this.submittedLanguageDocument = submittedLanguageDocument;
    }

    /**
     * @return the metadataManager
     */
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    /**
     * @return the keywordsService
     */
    public KeywordsService getKeywordsService() {
        return keywordsService;
    }

    /**
     * @param keywordsService the keywordsService to set
     */
    public void setKeywordsService(KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
    }

    /**
     * @return the startYear
     */
    public Integer getStartYear() {
        return startYear;
    }

    /**
     * @param startYear the startYear to set
     */
    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    /**
     * @param metadataEncryptor the metadataEncryptor to set
     */
    public void setMetadataEncryptor(MetadataEncryptor metadataEncryptor) {
        this.metadataEncryptor = metadataEncryptor;
    }

    /**
     * Sets the value of the circabcRenditionService
     *
     * @param circabcRenditionService the circabcRenditionService to set.
     */
    public void setCircabcRenditionService(
            CircabcRenditionService circabcRenditionService) {
        this.circabcRenditionService = circabcRenditionService;
    }

    public boolean isHasPermission() {
        final String currentAlfrescoUserName = getNavigator().getCurrentAlfrescoUserName();
        final Node currentNode = getNavigator().getCurrentNode();
        final String permission = "AddChildren";
        final AccessStatus hasPermission = getPermissionService()
                .hasPermission(currentNode.getNodeRef(), permission);
        if (hasPermission == AccessStatus.DENIED) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "User " + currentAlfrescoUserName + " does not have requred permision " + permission
                                + " on node " + currentNode.toString());
            }
            throw new RuntimeException("Piracy warning");
        } else {
            return true;
        }
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

 
    public boolean isEncryptedPropertiesEnabled() {
        return Boolean.parseBoolean(CircabcConfiguration.getProperty(
                CircabcConfiguration.ENCRYPTED_PROPERTIES_ENABLED));
    }
}
