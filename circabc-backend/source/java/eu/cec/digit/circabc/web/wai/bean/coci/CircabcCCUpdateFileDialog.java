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
package eu.cec.digit.circabc.web.wai.bean.coci;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.business.api.content.ContentBusinessSrv;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Map;

/**
 * @author Yanick Pignot
 */
public class CircabcCCUpdateFileDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcCCUpdateFileDialog";
    protected static final String CLOSE_AND_BROWSE_OUTCOME =
            CircabcNavigationHandler.CLOSE_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + CircabcNavigationHandler.WAI_PREFIX
                    + CircabcBrowseBean.WAI_BROWSE_OUTCOME;
    private static final Log logger = LogFactory.getLog(CircabcCCUpdateFileDialog.class);
    private static final long serialVersionUID = 3909601469295466806L;
    private static final String MSG_ERROR_UNEXPECTED = "update_doc_unexpected_error";
    private CircabcUploadedFile uploadedFile;

    private boolean hasPermission;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException(
                        "The node id is a madatory parameter that should be passed either via the Wizard/DialogManager.setupParameters(ActionEvent event). Use the actionListener tag of the action component.");
            }

            reset();

            String url = WebClientHelper
                    .getGeneratedWaiFullUrl(getActionNode(), ExtendedURLMode.HTTP_WAI_BROWSE);

            getActionNode().getProperties().put("url", url);
            getActionNode().getProperties()
                    .put("workingCopy", getActionNode().hasAspect(ContentModel.ASPECT_WORKING_COPY));
            getActionNode().getProperties()
                    .put("fileType32", FileTypeImageUtils.getFileTypeImage(getActionNode().getName(), false));
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        try {
            final NodeRef document = getActionNode().getNodeRef();
            getCociContentBusinessSrv()
                    .update(document, this.uploadedFile.getFile(), this.uploadedFile.isNotificationDisabled(),
                            this.uploadedFile.getFileName());

            return CLOSE_AND_BROWSE_OUTCOME;
        } catch (final BusinessStackError validationErrors) {
            for (final String msg : validationErrors.getI18NMessages()) {
                Utils.addErrorMessage(msg);
            }
            this.isFinished = false;
            return null;
        } catch (Throwable t) {
            if (logger.isErrorEnabled()) {
                logger.error("Error", t);
            }
            isFinished = false;
            Utils.addErrorMessage(
                    translate(MSG_ERROR_UNEXPECTED, getActionNode().getName(), t.getMessage()));
            return null;
        }
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
    }

    public String getBrowserTitle() {
        return translate("update_doc_browser_title");
    }

    public String getPageIconAltText() {
        return translate("update_doc_icon_tooltip");
    }

    public boolean isFileUploaded() {
        return uploadedFile != null && uploadedFile.getFile() != null;
    }

    /**
     * @return the uploadedFile
     */
    protected final CircabcUploadedFile getUploadedFile() {
        return uploadedFile;
    }

    protected void reset() {
        if (this.uploadedFile != null && this.uploadedFile.getFile() != null) {
            try {
                this.uploadedFile.getFile().delete();
            } catch (Throwable ignore) {
            }
        }

        uploadedFile = null;
    }

    public void addFile(CircabcUploadedFile fileBean) {
        if (fileBean != null && fileBean.getFile() != null) {
            this.uploadedFile = fileBean;
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate("file_upload_success", fileBean.getFileName()));
        }
    }

    @Override
    public String cancel() {
        reset();
        return super.cancel();
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        reset();
        return super.doPostCommitProcessing(context, outcome);
    }

    /**
     * @return the disableNotification
     */
    public final boolean isDisableNotification() {
        return false;
    }

    /**
     * @param disableNotification the disableNotification to set
     */
    public final void setDisableNotification(boolean disableNotification) {

    }

    /**
     * @return the cociContentBusinessSrv
     */
    protected final CociContentBusinessSrv getCociContentBusinessSrv() {
        return getBusinessRegistry().getCociContentBusinessSrv();
    }

    /**
     * @return the contentBusinessSrv
     */
    protected final ContentBusinessSrv getContentBusinessSrv() {
        return getBusinessRegistry().getContentBusinessSrv();
    }

    public boolean isHasPermission() {
        final String currentAlfrescoUserName = getNavigator().getCurrentAlfrescoUserName();
        if (getActionNode() != null) {
            final NodeRef document = getActionNode().getNodeRef();
            final String permission = "Write";
            final AccessStatus hasPermission = getPermissionService().hasPermission(document, permission);
            if (hasPermission == AccessStatus.DENIED) {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "User " + currentAlfrescoUserName + " does not have requred permision " + permission
                                    + " on node " + document.toString());
                }
                throw new RuntimeException("Piracy warning");
            } else {
                return true;
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("For user " + currentAlfrescoUserName + " no current action node");
            }
            throw new RuntimeException("Piracy warning");
        }

    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
