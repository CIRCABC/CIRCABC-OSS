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
package eu.cec.digit.circabc.web.wai.dialog.content.edit;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.content.Attachement;
import eu.cec.digit.circabc.business.api.content.Attachement.AttachementType;
import eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.servlet.UploadFileServlet;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.SortableModel;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Base class of any edit online dialog implementation
 *
 * @author Yanick Pignot
 */
@SuppressWarnings("serial")
public abstract class CreateContentBaseDialog extends BaseWaiDialog {

    protected static final String MODE_HTML = "Html";
    protected static final String MODE_TEXT = "Text";
    protected static final String TRUE = Boolean.TRUE.toString();
    protected static final String FALSE = Boolean.FALSE.toString();
    private static final String MSG_ERROR_UPLOAD = "create_content_dialog_attachement_error_upload";
    private static final String MSG_LINK_EXISTS = "create_content_dialog_attachement_error_link_exist";
    private static final String MSG_TYPE_FILE = "create_content_dialog_attachement_type_file";
    private static final String MSG_TYPE_LINK = "create_content_dialog_attachement_type_link";
    private static final String MSG_INFO_REMOVE = "create_content_dialog_attachement_remove_info";
    private static final String MSG_YES = "create_content_dialog_attachement_yes";
    private static final String MSG_NO = "create_content_dialog_attachement_no";
    private static final String HTML_EMPTY_CONTENT = "<p><span></span></p>";
    private static final Log logger = LogFactory.getLog(CreateContentBaseDialog.class);
    private List<AttachementWrapper> attachementWrappers;
    private transient DataModel attachmentDataModel;
    private transient AttachementBusinessSrv attachementBusinessSrv;
    private transient ContentService contentService;
    private CircabcRenditionService circabcRenditionService;
    private NodeRef attachLink;

    private String htmlContent;
    private String textContent;
    private String editMode;
    private String openAfterSave;
    private String defaultMode;

    // ------------------------------------------------------------------------------
    // Dialog implementation

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        this.openAfterSave = FALSE;

        if (parameters != null) {
            clearUpload();

            this.editMode = MODE_TEXT;
            this.htmlContent = null;
            this.textContent = null;
        }
    }

    public String getDefaultMode() {
        try {
            Object mode = FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .get("EDIT_MODE");
            if (mode != null && mode instanceof String) {
                defaultMode = (String) mode;
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when getting EDIT_MODE from session ", e);
            }
        }
        return defaultMode;
    }

    public void setDefaultMode(final String value) {
        defaultMode = value;
    }

    public abstract boolean isAttachementAllowed();

    protected abstract String getDialogName();

    protected abstract NodeRef getEditablePost();

    protected String getSelectedContent() {
        if (MODE_HTML.equals(editMode)) {
            return computeContent(this.htmlContent);
        } else if (MODE_TEXT.equals(editMode)) {
            return computeContent(this.textContent);
        } else {
            throw new IllegalStateException(
                    "Unknow mode: " + editMode + ". One of these expected: " + MODE_TEXT + ", " + MODE_HTML);
        }
    }

    protected boolean reopen() {
        return TRUE.equals(getOpenAfterSave());
    }

    protected boolean isDialog() {
        return true;
    }

    protected void attachFiles(final NodeRef referer) {
        if (referer != null && attachementWrappers != null) {
            for (final AttachementWrapper wrapper : attachementWrappers) {
                if (wrapper.isCreated() == false) {
                    getAttachementBusinessSrv().addAttachement(getEditablePost(), wrapper.getAttachRef());

                    if (circabcRenditionService != null) {
                        circabcRenditionService.addRequest(wrapper.getAttachRef());
                    }
                }
                // else already created and not modified...
            }
        }

        clearUpload();
        getAttachmentDataModel();
    }

    protected String computeContent(final String content) {
        // workaroud: since the jsp define several component, one of these return nothing !
        if (content != null && content.length() > 0 && content.equals(HTML_EMPTY_CONTENT) == false) {
            return content;
        } else {
            return null;
        }
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("save");
    }

    public boolean isAttachementFound() {
        // ensure the construction of the list of attachements
        getAttachmentDataModel();

        return this.attachementWrappers != null && this.attachementWrappers.size() > 0;
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        outcome = super.doPostCommitProcessing(context, outcome);
        if (outcome == null) {
            return null;
        } else if (reopen()) {
            setOpenAfterSave(FALSE);

            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + (isDialog() ? CircabcNavigationHandler.WAI_DIALOG_PREFIX
                    : CircabcNavigationHandler.WAI_WIZARD_PREFIX)
                    + getDialogName();
        } else {
            clearUpload();
            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;

        }
    }

    /**
     * @return the contentService
     */
    protected final ContentService getContentService() {
        if (contentService == null) {
            contentService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentService();
        }
        return contentService;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @return the editMode
     */
    public final String getEditMode() {
        try {
            Object mode = FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .get("EDIT_MODE");
            if (mode != null && mode instanceof String) {
                editMode = (String) mode;
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when getting EDIT_MODE from session ", e);
            }
        }
        return editMode;
    }

    /**
     * @param editMode the editMode to set
     */
    public final void setEditMode(String editMode) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .put("EDIT_MODE", editMode);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when put EDIT_MODE into session ", e);
            }
        }
        this.editMode = editMode;
    }

    /**
     * @return the htmlContent
     */
    public String getHtmlContent() {
        if (htmlContent == null) {
            final NodeRef nodeRef = getEditablePost();
            if (nodeRef != null && getNodeService().exists(nodeRef)) {
                htmlContent = getContent(nodeRef);
            } else {
                htmlContent = "";
            }
        }

        return htmlContent;
    }

    /**
     * @param htmlContent the htmlContent to set
     */
    public final void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    protected String getContent(final NodeRef nodeRef)
            throws InvalidNodeRefException, InvalidTypeException, ContentIOException {
        final ContentReader reader = getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
        return reader.getContentString();
    }

    /**
     * @return the textContent
     */
    public String getTextContent() {
        if (textContent == null) {
            textContent = getHtmlContent();
        }

        return textContent;
    }

    /**
     * @param textContent the textContent to set
     */
    public final void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    /**
     * @return the openAfterSave
     */
    public final String getOpenAfterSave() {
        return openAfterSave;
    }

    /**
     * @param openAfterSave the openAfterSave to set
     */
    public final void setOpenAfterSave(String openAfterSave) {
        this.openAfterSave = openAfterSave;
    }

    public String getRootId() {
        final InterestGroupNode currentIGRoot = (InterestGroupNode) getNavigator().getCurrentIGRoot();
        if (currentIGRoot != null) {
            if (currentIGRoot.getLibrary() != null) {
                return currentIGRoot.getLibrary().getId();
            } else {
                return currentIGRoot.getId();
            }
        } else {
            return getNavigator().getCircabcHomeNode().getId();
        }
    }

    public DataModel getAttachmentDataModel() {
        if (this.attachmentDataModel == null) {
            this.attachmentDataModel = new SortableModel();

            if (isAttachementAllowed()) {
                if (getEditablePost() != null) {
                    final AttachementBusinessSrv serv = getAttachementBusinessSrv();
                    final List<Attachement> attachments = serv.getAttachements(getEditablePost());

                    for (final Attachement att : attachments) {
                        attachementWrappers.add(new AttachementWrapper(att));
                    }
                }

                this.attachmentDataModel.setWrappedData(attachementWrappers);
            }
        }
        return this.attachmentDataModel;
    }

    public List<AttachementWrapper> getAttachementWrappers() {
        return attachementWrappers;
    }

    public void removeAttachement(final ActionEvent event) {
        try {
            final AttachementWrapper wrapper = (AttachementWrapper) this.attachmentDataModel.getRowData();

            if (wrapper.isCreated()) {

                getAttachementBusinessSrv()
                        .removeAttachement(getEditablePost(), wrapper.getAttachement().getNodeRef());

                Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_INFO_REMOVE));
            } else {
                if (wrapper.isLink == false) {
                    // it s a temporary node ref
                    final ChildAssociationRef tempRoot = getNodeService()
                            .getPrimaryParent(wrapper.getAttachRef());
                    AuthenticationUtil.setRunAsUserSystem();
                    getAttachementBusinessSrv()
                            .removeAttachement(tempRoot.getParentRef(), wrapper.getAttachRef());
                    AuthenticationUtil.clearCurrentSecurityContext();
                }

                this.attachementWrappers.remove(wrapper);
            }
        } catch (BusinessStackError error) {
            for (final String message : error.getI18NMessages()) {
                Utils.addErrorMessage(message);
            }
        }

    }

    public String getAttachFile() {
        return "";
    }

    public void setAttachFile(String name) {
        if (name == null || name.length() < 1) {
            // the dialog is submited, but not with
            return;
        }

        // we also need to keep the file upload bean in sync
        final FacesContext ctx = FacesContext.getCurrentInstance();
        final FileUploadBean fileBean = (FileUploadBean) ctx.getExternalContext().getSessionMap()
                .get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);

        if (fileBean != null && fileBean.getFile() != null) {
            final NodeRef tempAttach = getAttachementBusinessSrv()
                    .addTempAttachement(name, fileBean.getFile());

            try {
                fileBean.getFile().delete();
            } catch (final Throwable ignore) {
            }

            this.attachementWrappers.add(new AttachementWrapper(tempAttach, name, false));
        } else {
            Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD));
        }
    }

    public NodeRef getAttachLink() {
        return attachLink;
    }

    public final void setAttachLink(NodeRef refLink) {
        attachLink = refLink;

        if (refLink != null) {
            final String name = (String) getNodeService().getProperty(refLink, ContentModel.PROP_NAME);

            // check if reference already exists
            boolean exists = false;
            for (final AttachementWrapper wrapper : this.attachementWrappers) {
                if (refLink.equals(wrapper.getAttachRef())) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                Utils.addErrorMessage(translate(MSG_LINK_EXISTS, name));
            } else {
                this.attachementWrappers.add(new AttachementWrapper(refLink, name, true));
            }
        }
        attachLink = null;
    }

    /**
     * Action handler called when the dialog is cancelled
     */
    @Override
    public String cancel() {
        clearUpload();
        return super.cancel();
    }

    protected void clearUpload() {
        if (this.attachementWrappers != null && this.attachementWrappers.size() > 0) {
            // remove the file upload bean from the session
            final FacesContext fc = FacesContext.getCurrentInstance();
            final HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);

            UploadFileServlet.resetUploadedFiles(session);
        }

        attachLink = null;
        attachmentDataModel = null;
        attachementWrappers = new ArrayList<>();
    }

    /**
     * @return the attachementBusinessSrv
     */
    protected final AttachementBusinessSrv getAttachementBusinessSrv() {
        if (attachementBusinessSrv == null) {
            attachementBusinessSrv = getBusinessRegistry().getAttachementBusinessSrv();
        }
        return attachementBusinessSrv;
    }

    /**
     * @param circabcRenditionService the circabcRenditionService to set
     */
    public void setCircabcRenditionService(
            CircabcRenditionService circabcRenditionService) {
        this.circabcRenditionService = circabcRenditionService;
    }

    public static class AttachementWrapper implements Serializable {

        public final boolean isLink;
        private final Attachement attachement;
        private final NodeRef attachRef;
        private final String name;


        public AttachementWrapper(final NodeRef attachRef, final String name, boolean isLink) {
            super();
            this.name = name;
            this.attachRef = attachRef;
            this.attachement = null;
            this.isLink = isLink;
        }

        private AttachementWrapper(final Attachement attachement) {
            super();
            this.name = attachement.getTitle();
            this.attachRef = attachement.getNodeRef();
            this.attachement = attachement;
            this.isLink = AttachementType.REPO_LINK.equals(attachement.geType());
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            if (isLink) {
                return WebClientHelper.translate(MSG_TYPE_LINK);
            } else {
                return WebClientHelper.translate(MSG_TYPE_FILE);
            }
        }

        public final boolean isCreated() {
            return attachement != null;
        }

        public final String getCreatedStr() {
            return translateBool(isCreated());
        }

        private String translateBool(final boolean bool) {
            if (bool) {
                return WebClientHelper.translate(MSG_YES);
            } else {
                return WebClientHelper.translate(MSG_NO);
            }
        }

        public final NodeRef getAttachRef() {
            return attachRef;
        }

        /**
         * @return the attachement
         */
        protected final Attachement getAttachement() {
            return attachement;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((attachRef == null) ? 0 : attachRef.hashCode());
            result = PRIME * result + ((attachement == null) ? 0 : attachement.hashCode());
            result = PRIME * result + (isLink ? 1231 : 1237);
            result = PRIME * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AttachementWrapper other = (AttachementWrapper) obj;

            if (attachRef == null) {
                if (other.attachRef != null) {
                    return false;
                }
            } else if (!attachRef.equals(other.attachRef)) {
                return false;
            }
            if (attachement == null) {
                if (other.attachement != null) {
                    return false;
                }
            } else if (!attachement.equals(other.attachement)) {
                return false;
            }
            if (isLink != other.isLink) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

    }
}
