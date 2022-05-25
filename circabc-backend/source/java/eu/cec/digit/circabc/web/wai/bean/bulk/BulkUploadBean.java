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
package eu.cec.digit.circabc.web.wai.bean.bulk;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.service.bulk.BulkService;
import eu.cec.digit.circabc.service.bulk.indexes.IndexEntry;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.IndexService.Headers;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.upload.UploadedEntry;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.bean.content.AddContentBean;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.*;
import java.util.*;

/**
 * Bean to handle the bulk upload process on the WAI part.
 *
 * @author Stephane Clinckart
 */
public class BulkUploadBean extends AddContentBean {

    private static final long serialVersionUID = 8518953203603149020L;

    private static final Log logger = LogFactory.getLog(BulkUploadBean.class);
    private static final String IS_SECURE_TRANSPORT = "BulkUploadBean.isSecureTransport";
    private static final String ID_CHECK_DISABLE_INDEX = "check-disable-index";
    private final String CLOSE_AND_BROWSE_OUTCOME = CircabcNavigationHandler.CLOSE_DIALOG_OUTCOME
            + CircabcNavigationHandler.OUTCOME_SEPARATOR
            + CircabcNavigationHandler.WAI_PREFIX
            + CircabcBrowseBean.WAI_BROWSE_OUTCOME;
    private List<IndexRecord> indexFileEntries;
    private List<UploadedEntry> uploadedEntries;
    private Map<String, Integer> indexRecordCountCache;
    private List<ValidationMessage> messages;

    private transient BulkService bulkService;
    private transient TransactionService transactionService;
    private transient NotificationService notificationService;
    private transient NotificationSubscriptionService notificationSubscriptionService;

    private BehaviourFilter policyBehaviourFilter;

    private boolean isSecureTransport;

    private boolean indexFileFound = false;
    private boolean errorFound = false;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        // in the restaure mode, the parameters can be null
        if (parameters != null) {
            indexFileFound = false;
            errorFound = false;
            this.indexFileEntries = Collections.emptyList();
            this.uploadedEntries = new ArrayList<>();
            this.indexRecordCountCache = new HashMap<>();
            this.messages = new ArrayList<>();
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        try {
            upload();
        } catch (final BusinessStackError validationErrors) {
            if (logger.isDebugEnabled()) {
                for (final String msg : validationErrors.getI18NMessages()) {
                    logger.debug(msg);
                }
            }
        } catch (final NotSupportedException | HeuristicRollbackException | HeuristicMixedException | RollbackException | IllegalStateException | SecurityException | SystemException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception during upload:" + e.getMessage());
            }
        }

        return outcome;
    }

    /**
     * Ask if a file has been uploaded
     *
     * @return boolean True if a file has been uploaded
     */
    public boolean getHasBeenUploaded() {
        return getFirstUploadedFile() != null;
    }

    @Override
    protected String getDialogToStart() {
        return "bulkUploadWai";
    }

    @Override
    public void addFile(CircabcUploadedFile fileBean) {
        super.addFile(fileBean);
        simulateUpload();
    }

    private boolean isSubmitedSkipIndexfile(final CircabcUploadedFile uploadedFile) {
        final String value =
                (uploadedFile == null) ? null : uploadedFile.getSubmitedProperty(ID_CHECK_DISABLE_INDEX);

        return Boolean.parseBoolean(value);
    }

    /**
     * Action handler called when the dialog is cancelled
     */
    @Override
    public String cancel() {
        super.cancel();
        return "wai:browse-wai";
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        super.doPostCommitProcessing(context, outcome);
        return CLOSE_AND_BROWSE_OUTCOME;
    }

    @Override
    protected void clearUpload() {
        super.clearUpload();

        this.indexFileEntries = null;
        this.uploadedEntries = null;
        this.indexRecordCountCache = null;
        this.messages = null;
    }

    public void resetUpload(final ActionEvent event) {
        clearUpload();
    }

    private void simulateUpload() {

        final UserTransaction txn = getTransactionService().getNonPropagatingUserTransaction(false);
        try {
            txn.begin();
            final CircabcUploadedFile fileBean = getFirstUploadedFile();
            if (fileBean != null) {
                fileBean.getFile();
                final CircabcUploadedFile uploadedFile = getFirstUploadedFile();
                try {
                    //Reset messages
                    messages = new ArrayList<>();
                    //Reset the cache
                    indexRecordCountCache = new HashMap<>();

                    if (isSubmitedSkipIndexfile(fileBean)) {
                        indexFileEntries = Collections.emptyList();
                    } else {
                        indexFileEntries = getBulkService().getIndexRecords(uploadedFile.getFile(), messages);
                    }

                    uploadedEntries = getBulkService()
                            .upload(getNavigator().getCurrentNode().getNodeRef(), fileBean.getFile(),
                                    indexFileEntries, messages);
                    getBulkService().validateEntries(indexFileEntries, uploadedEntries, messages);

                    this.errorFound = false;
                    for (final ValidationMessage message : messages) {
                        if (message.getErrorType().equals(ErrorType.Fatal)) {
                            errorFound = true;
                            break;
                        }
                    }

                    this.indexFileFound = indexFileEntries.size() > 0;

                } catch (final Exception ex) {
                    boolean foundFatal = false;
                    for (ValidationMessage message : messages) {
                        if (ErrorType.Fatal.equals(message.getErrorType())) {
                            foundFatal = true;
                            break;
                        }
                    }
                    if (!foundFatal) {
                        messages.add(new ValidationMessageImpl(-1 /* TODO */, uploadedFile.getFileName(),
                                "Error while uploading the file:" + uploadedFile.getFileName() + " caused by:" + ex
                                        .getMessage() + " Please contact the Circabc Support Team", ErrorType.Fatal));
                    }
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception during upload:" + ex.getMessage(), ex);
                    }
                }
            }
        } catch (final NotSupportedException | SystemException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception during upload:" + e.getMessage());
            }
        } finally {
            try {
                txn.rollback();
            } catch (final IllegalStateException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Exception during rollback:" + e.getMessage());
                }
            } catch (final SecurityException | SystemException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Exception during upload:" + e.getMessage());
                }
            }
        }
    }

    public boolean getIsSecureTransport() {
        final HttpServletRequest httpReq = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        if (httpReq.getAttribute(IS_SECURE_TRANSPORT) == null) {
            String fullURL = httpReq.getHeader("full-url");
            if (fullURL == null) {
                fullURL = httpReq.getRequestURL().toString();
            }
            this.isSecureTransport = fullURL.startsWith("https:");
            httpReq.setAttribute(IS_SECURE_TRANSPORT, this.isSecureTransport);
        } else {
            this.isSecureTransport = (Boolean) httpReq.getAttribute(IS_SECURE_TRANSPORT);
        }
        return this.isSecureTransport;
    }

    private void upload()
            throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {

        final UserTransaction txn = getTransactionService().getNonPropagatingUserTransaction(false);
        txn.begin();

        final CircabcUploadedFile fileBean = getFirstUploadedFile();
        if (fileBean != null) {
            if (fileBean.isNotificationDisabled()) {
                // Disable the notifications.
                // The change applies ONLY to the current transaction.
                // It is automatically reverted after the commit.
                this.policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }

            fileBean.getFile();
            final CircabcUploadedFile uploadedFile = getFirstUploadedFile();
            try {
                //Reset messages
                messages = new ArrayList<>();
                //Reset the cache
                indexRecordCountCache = new HashMap<>();
                if (isSubmitedSkipIndexfile(fileBean)) {
                    indexFileEntries = Collections.emptyList();
                } else {
                    indexFileEntries = getBulkService().getIndexRecords(uploadedFile.getFile(), messages);
                }
                uploadedEntries = getBulkService()
                        .upload(getNavigator().getCurrentNode().getNodeRef(), fileBean.getFile(),
                                indexFileEntries, messages);
            } catch (final Exception ex) {
                messages.add(new ValidationMessageImpl(-1 /* TODO */, uploadedFile.getFileName(),
                        "Error while uploading the file:" + uploadedFile.getFileName() + " caused by:" + ex
                                .getMessage(), ErrorType.Fatal));
                if (logger.isErrorEnabled()) {
                    logger.error("Exception during upload:" + ex.getMessage());
                }
            }
        }

        txn.commit();
    }

    /* Index Entry List */
    public List<IndexRecord> getIndexRecords() {
        return indexFileEntries;
    }

    public List<UploadedEntry> getUploadedEntries() {
        return uploadedEntries;
    }


    public List<ValidationMessage> getErrorMessages() {
        return messages;
    }

    public int getUploadedEntryPageSize() {
        return uploadedEntries.size();
    }

    public int getIndexRecordPageSize() {
        return indexFileEntries.size();
    }

    private int getGenericCount(final String headerName) {
        int count = 0;
        IndexEntry indexEntry;
        for (final IndexRecord indexRecord : indexFileEntries) {
            indexEntry = indexRecord.getEntry(headerName);
            if (indexEntry != null &&
                    indexEntry.getHeaderName().equals(headerName) &&
                    indexEntry.getValue() != null &&
                    !indexEntry.getValue().equals("")) {
                count++;
            }
        }
        return count;
    }

    private int getGenericCountCache(final String headerName) {

        int count = -1;
        if (indexRecordCountCache.containsKey(headerName)) {
            final Integer countCache = indexRecordCountCache.get(headerName);
            if (countCache != null) {
                count = countCache;
            }
        }
        if (count == -1) {
            count = getGenericCount(headerName);
            indexRecordCountCache.put(headerName, count);
        }
        return count;
    }

    /* Columns availabilty */
    public int getIndexRecordNameCount() {
        return getGenericCountCache(Headers.NAME);
    }

    public int getIndexRecordTitleCount() {
        return getGenericCountCache(Headers.TITLE);
    }

    public int getIndexRecordDescriptionCount() {
        return getGenericCountCache(Headers.DESCRIPTION);
    }

    public int getIndexRecordAuthorCount() {
        return getGenericCountCache(Headers.AUTHOR);
    }

    public int getIndexRecordKeywordsCount() {
        return getGenericCountCache(Headers.KEYWORDS);
    }

    public int getIndexRecordStatusCount() {
        return getGenericCountCache(Headers.STATUS);
    }

    public int getIndexRecordIssueDateCount() {
        return getGenericCountCache(Headers.ISSUE_DATE);
    }

    public int getIndexRecordReferenceCount() {
        return getGenericCountCache(Headers.REFERENCE);
    }

    public int getIndexRecordExpirationDateCount() {
        return getGenericCountCache(Headers.EXPIRATION_DATE);
    }

    public int getIndexRecordSecurityRankingCount() {
        return getGenericCountCache(Headers.SECURITY_RANKING);
    }


    public int getIndexRecordAttri1Count() {
        return getGenericCountCache(Headers.ATTRI1);
    }

    public int getIndexRecordAttri2Count() {
        return getGenericCountCache(Headers.ATTRI2);
    }

    public int getIndexRecordAttri3Count() {
        return getGenericCountCache(Headers.ATTRI3);
    }

    public int getIndexRecordAttri4Count() {
        return getGenericCountCache(Headers.ATTRI4);
    }

    public int getIndexRecordAttri5Count() {
        return getGenericCountCache(Headers.ATTRI5);
    }

    public int getIndexRecordAttri6Count() {
        return getGenericCountCache(Headers.ATTRI6);
    }

    public int getIndexRecordAttri7Count() {
        return getGenericCountCache(Headers.ATTRI7);
    }

    public int getIndexRecordAttri8Count() {
        return getGenericCountCache(Headers.ATTRI8);
    }

    public int getIndexRecordAttri9Count() {
        return getGenericCountCache(Headers.ATTRI9);
    }

    public int getIndexRecordAttri10Count() {
        return getGenericCountCache(Headers.ATTRI10);
    }

    public int getIndexRecordAttri11Count() {
        return getGenericCountCache(Headers.ATTRI11);
    }

    public int getIndexRecordAttri12Count() {
        return getGenericCountCache(Headers.ATTRI12);
    }

    public int getIndexRecordAttri13Count() {
        return getGenericCountCache(Headers.ATTRI13);
    }

    public int getIndexRecordAttri14Count() {
        return getGenericCountCache(Headers.ATTRI14);
    }

    public int getIndexRecordAttri15Count() {
        return getGenericCountCache(Headers.ATTRI15);
    }

    public int getIndexRecordAttri16Count() {
        return getGenericCountCache(Headers.ATTRI16);
    }

    public int getIndexRecordAttri17Count() {
        return getGenericCountCache(Headers.ATTRI17);
    }

    public int getIndexRecordAttri18Count() {
        return getGenericCountCache(Headers.ATTRI18);
    }

    public int getIndexRecordAttri19Count() {
        return getGenericCountCache(Headers.ATTRI19);
    }

    public int getIndexRecordAttri20Count() {
        return getGenericCountCache(Headers.ATTRI20);
    }


    public int getIndexRecordTypeDocumentCount() {
        return getGenericCountCache(Headers.TYPE_DOCUMENT);
    }

    public int getIndexRecordTranslatorCount() {
        return getGenericCountCache(Headers.TRANSLATOR);
    }

    public int getIndexRecordDocLangCount() {
        return getGenericCountCache(Headers.DOC_LANG);
    }

    public int getIndexRecordNoContentCount() {
        return getGenericCountCache(Headers.NO_CONTENT);
    }

    public int getIndexRecordOriLangCount() {
        return getGenericCountCache(Headers.ORI_LANG);
    }

    public int getIndexRecordRelTransCount() {
        return getGenericCountCache(Headers.REL_TRANS);
    }

    public int getIndexRecordOverwriteCount() {
        return getGenericCountCache(Headers.OVERWRITE);
    }

    public int getErrorRecordPageSize() {
        return messages.size();
    }

    /* WIZARD Button */

    public boolean getNextButtonDisabled() {
        boolean disabled = false;

        for (final ValidationMessage message : messages) {
            if (message.getErrorType().equals(ErrorType.Fatal)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Found an error in file:" + message.getFileName() + " description:" + message
                            .getErrorDescription());
                }
                disabled = true;
                break;
            }
        }
        return disabled;
    }


    public boolean getFinishButtonDisabled() {

        if (getHasBeenUploaded() == false) {
            return true;
        } else {
            return errorFound;
        }

    }


    public String getBrowserTitle() {
        // managed by the jsp
        return "";
    }

    public String getPageIconAltText() {
        // managed by the jsp
        return "";
    }

//  ------------------------------------------------------------------------------
    // Helper methods

    /**
     * @return the BulkService
     */
    public BulkService getBulkService() {
        if (this.bulkService == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            this.bulkService = (BulkService) Services.getCircabcServiceRegistry(fc).getBulkService();
        }
        return bulkService;
    }

    /**
     * set the BulkService
     */
    public void setBulkService(final BulkService bulkService) {
        this.bulkService = bulkService;
    }

    /**
     * @return the transactionService
     */
    public TransactionService getTransactionService() {
        if (this.transactionService == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            this.transactionService = (TransactionService) Services.getAlfrescoServiceRegistry(fc)
                    .getTransactionService();
        }
        return this.transactionService;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @return the NotificationService
     */
    public NotificationService getNotificationService() {
        if (this.notificationService == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            this.notificationService = (NotificationService) Services.getCircabcServiceRegistry(fc)
                    .getNotificationService();
        }
        return notificationService;
    }

    /**
     * set the NotificationService
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    /**
     * @return the NotificationSubscriptionService
     */
    public NotificationSubscriptionService getNotificationSubscriptionService() {
        if (this.notificationSubscriptionService == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            this.notificationSubscriptionService = (NotificationSubscriptionService) Services
                    .getCircabcServiceRegistry(fc).getNotificationSubscriptionService();
        }
        return notificationSubscriptionService;
    }

    /**
     * set the NotificationSubscriptionService
     */
    public void setNotificationSubscriptionService(
            final NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }


    /**
     * @param policyBehaviourFilter the policyBehaviourFilter to set
     */
    public final void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * @return the errorFound
     */
    public final boolean isErrorFound() {
        return errorFound;
    }

    /**
     * @return the indexFileFound
     */
    public final boolean isIndexFileFound() {
        return indexFileFound;
    }

    /**
     * @return the disableIndexFile
     */
    public final boolean isDisableIndexFile() {
        return false;
    }

    /**
     * @param disableIndexFile the disableIndexFile to set
     */
    public final void setDisableIndexFile(boolean disableIndexFile) {
    }

}
