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
package eu.cec.digit.circabc.web.wai.dialog;

import eu.cec.digit.circabc.action.CircabcImporterActionExecuter;
import eu.cec.digit.circabc.action.config.ImportConfig;
import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import eu.cec.digit.circabc.web.wai.bean.content.AddContentBean;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ImportDialogWai extends AddContentBean implements WaiDialog {


    protected static final Serializable ENCODING = "UTF-8";
    /**
     *
     */
    private static final long serialVersionUID = 1688168394578086333L;
    private static final Log logger = LogFactory.getLog(ImportDialogWai.class);
    private static final String MSG_IMPORT_UPLOAD_SUCCESS = "import_upload_success";
    private static final String MSG_IMPORT_STARTED = "import_started";
    private static final String MSG_IMPORT_MAX_FILE_SIZE_IN_MEGA_BYTES = "import_max_file_size_in_mega_bytes";
    private static final String MSG_ERR_INVALID_FILE_EXTENSION = "import_invalid_file_extension";
    private static final String MSG_ERR_FILE_IS_TOO_LARGE = "import_file_is_too_large";
    private static final String MSG_ERR_IMPORT_UNEXPECTED_ERROR = "import_unexpected_error";
    private static final String IMPORT_ONLY_ONE_FILE = "import_only_one_file";
    private static final String MSG_ERR_FIRST_LOCATE_FILE_TO_UPLAD = "import_first_locate_file";

    private String disabledInput = "";


    private ActionService actionService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private BehaviourFilter policyBehaviourFilter;
    private ImportConfig config;

    private String selectedEncoding = "CP437";
    private Boolean notifyUser;
    private Boolean deleteFileAfterImport;
    private Boolean disableFileNotification;
    private String maxFileSizeMessage;
    private long maxSizeInBytes;

    public void reset(ActionEvent event) {
        this.init(null);
    }

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
        if (parameters != null) {
            notifyUser = true;
            deleteFileAfterImport = true;
            disableFileNotification = true;

        }
        maxFileSizeMessage = translate(MSG_IMPORT_MAX_FILE_SIZE_IN_MEGA_BYTES,
                config.getMaxSizeInMegaBytes());
        maxSizeInBytes = this.config.getMaxSizeInMegaBytes() * 1024L * 1024L;
        disabledInput = "";

    }

    @Override
    protected String getDialogToStart() {
        return "importDialogWai";
    }

    public void addFile(final CircabcUploadedFile fileBean) {
        if (fileBean != null) {
            if (fileBean.getFile() == null) {
                if (getUploadedFileCount() == 1) {
                    String message = translate(IMPORT_ONLY_ONE_FILE);
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
                } else {
                    String message = translate(MSG_ERR_FIRST_LOCATE_FILE_TO_UPLAD);
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
                }
            } else {
                final String fileName = fileBean.getFileName();
                long fileLength = fileBean.getFile().length();
                if (!fileName.endsWith(".zip") && !fileName.endsWith(".acp")) {
                    String message = translate(MSG_ERR_INVALID_FILE_EXTENSION);
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));

                } else if (fileLength > maxSizeInBytes) {
                    String message = translate(MSG_ERR_FILE_IS_TOO_LARGE);
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));

                } else if (getUploadedFileCount() == 1) {
                    String message = translate(IMPORT_ONLY_ONE_FILE);
                    ErrorsRenderer
                            .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
                } else {
                    super.addFile(fileBean);
                    // disable input submit after successful upload
                    disabledInput = "disabled=\"disabled\"";
                }
            }
        }
    }


    @Override
    protected String addFileOKMessage(String fileName) {
        return translate(MSG_IMPORT_UPLOAD_SUCCESS, fileName);
    }

    @Override
    protected String createContenetOKMessage(CircabcUploadedFile bean) {
        return translate(MSG_IMPORT_STARTED, bean.getFileName());
    }


    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {
        // todo walidate zip and acp
        if (getUploadedFileCount() == 1) {
            if (disableFileNotification) {
                policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }
            final String dialogOutcome = super.finishImpl(context, outcome);
            outcome = dialogOutcome;
            if (getCreatedNodeRefs().size() == 1) {

                final NodeRef acpNodeRef = getCreatedNodeRefs().keySet().iterator().next();
                try {

                    RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
                    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                        public Object execute() throws Throwable {
                            // build the action params map based on the bean's current state
                            Map<String, Serializable> params = new HashMap<>(2, 1.0f);
                            params.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER,
                                    getActionNode().getNodeRef());
                            params.put(ImporterActionExecuter.PARAM_ENCODING, selectedEncoding);
                            params.put(CircabcImporterActionExecuter.PARAM_DELETE_FILE, deleteFileAfterImport);
                            params.put(CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION,
                                    disableFileNotification);
                            String userName = "";
                            if (notifyUser) {
                                userName = AuthenticationUtil.getFullyAuthenticatedUser();

                            }
                            params.put(CircabcImporterActionExecuter.PARAM_NOTIFY_USER, userName);
                            // build the action to execute
                            Action action = getActionService()
                                    .createAction(CircabcImporterActionExecuter.NAME, params);
                            action.setExecuteAsynchronously(true);

                            // execute the action on the ACP file
                            getActionService().executeAction(action, acpNodeRef);

                            if (logger.isDebugEnabled()) {
                                logger.debug("Executed import action with action params of " + params);
                            }
                            return dialogOutcome;
                        }
                    };
                    txnHelper.doInTransaction(callback);

                    // reset the bean
                    reset(null);
                } catch (Throwable e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error during import.", e);
                    }
                    Utils.addErrorMessage(translate(MSG_ERR_IMPORT_UNEXPECTED_ERROR, e.getMessage()));
                    this.isFinished = false;
                    return null;
                }

            }
        }
        return outcome;
    }


    public void notifyUserChanged(ValueChangeEvent event) {
        Boolean newValue = (Boolean) event.getNewValue();
        notifyUser = newValue;
    }

    public void deleteFileAfterImportChanged(ValueChangeEvent event) {
        Boolean newValue = (Boolean) event.getNewValue();
        deleteFileAfterImport = newValue;
    }


    public void disableFileNotificationChanged(ValueChangeEvent event) {
        Boolean newValue = (Boolean) event.getNewValue();
        disableFileNotification = newValue;
    }

    @Override
    public void removeSelection(ActionEvent event) {
        // clear all uploaded file
        clearUpload();
    }


    @Override
    public String doPostCommitProcessing(final FacesContext context, final String outcome) {

        setLogRecord(getActionNode());
        logRecord.setService("Library");
        logRecord.setActivity("Import started");
        logRecord.setOK(true);
        logRecord.setInfo(getActionNode().getName());
        getLogService().log(logRecord);
        return outcome;
    }

    public String generateEmptyTemplate(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        ServletOutputStream outStream = null;

        try {

            response.setHeader("Content-Disposition", "attachment;filename=index.txt");
            response.setContentType("text/csv;charset=UTF-8");

            outStream = response.getOutputStream();

            OutputStreamWriter outStreamWriter = null;
            try {
                outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

                // write byte order mark
                outStream.write(0xEF);
                outStream.write(0xBB);
                outStream.write(0xBF);

                outStreamWriter.write("NAME");
                outStreamWriter.write("\t");
                outStreamWriter.write("TITLE");
                outStreamWriter.write("\t");
                outStreamWriter.write("DESCRIPTION");
                outStreamWriter.write("\t");
                outStreamWriter.write("AUTHOR");
                outStreamWriter.write("\t");
                outStreamWriter.write("KEYWORDS");
                outStreamWriter.write("\t");
                outStreamWriter.write("STATUS");
                outStreamWriter.write("\t");
                outStreamWriter.write("ISSUE DATE");
                outStreamWriter.write("\t");
                outStreamWriter.write("REFERENCE");
                outStreamWriter.write("\t");
                outStreamWriter.write("EXPIRDATE");
                outStreamWriter.write("\t");
                outStreamWriter.write("SECRANK");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI1");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI2");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI3");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI4");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI5");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI6");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI7");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI8");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI9");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI10");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI11");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI12");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI13");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI14");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI15");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI16");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI17");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI18");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI19");
                outStreamWriter.write("\t");
                outStreamWriter.write("ATTRI20");
                outStreamWriter.write("\t");
                outStreamWriter.write("TYPE");
                outStreamWriter.write("\t");
                outStreamWriter.write("TRANSLATOR");
                outStreamWriter.write("\t");
                outStreamWriter.write("LANG");
                outStreamWriter.write("\t");
                outStreamWriter.write("NOCONTENT");
                outStreamWriter.write("\t");
                outStreamWriter.write("ORILANG");
                outStreamWriter.write("\t");
                outStreamWriter.write("RELTRANS");
                outStreamWriter.write("\t");
                outStreamWriter.write("OVERWRITE");
                outStreamWriter.write('\n');
            } finally {
                if (outStreamWriter != null) {
                    try {
                        outStreamWriter.flush();
                    } finally {
                        outStreamWriter.close();
                    }
                }
            }

            context.responseComplete();

        } catch (Exception ex) {
            logger.error("Error during generating empty template of index file", ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

        return null;

    }

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public Boolean getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(Boolean notifyUser) {
        this.notifyUser = notifyUser;
    }

    public Boolean getDeleteFileAfterImport() {
        return deleteFileAfterImport;
    }

    public void setDeleteFileAfterImport(Boolean deleteFileAfterImport) {
        this.deleteFileAfterImport = deleteFileAfterImport;
    }

    public Boolean getDisableFileNotification() {
        return disableFileNotification;
    }

    public void setDisableFileNotification(Boolean disableFileNotification) {
        this.disableFileNotification = disableFileNotification;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public String getMaxFileSizeMessage() {
        return maxFileSizeMessage;
    }

    public void setMaxFileSizeMessage(String maxFileSizeMessage) {
        this.maxFileSizeMessage = maxFileSizeMessage;
    }

    public ImportConfig getConfig() {
        return config;
    }

    public void setConfig(ImportConfig config) {
        this.config = config;
    }

    /**
     * @return the disabledInput
     */
    public String getDisabledInput() {
        return disabledInput;
    }

    /**
     * @param disabledInput the disabledInput to set
     */
    public void setDisabledInput(String disabledInput) {
        this.disabledInput = disabledInput;
    }

    public String getSelectedEncoding() {
        return selectedEncoding;
    }

    public void setSelectedEncoding(String selectedEncoding) {
        this.selectedEncoding = selectedEncoding;
    }
}
