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
package eu.cec.digit.circabc.web.wai.bean.ml;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.generic.EditNodePropertiesDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Dialog bean to upload a new translation without content and to add it to an existing
 * MLContainer.
 *
 * @author David Ferraz
 */
public class AddTranslationWithoutContentDialog extends
        org.alfresco.web.bean.ml.AddTranslationWithoutContentDialog implements WaiDialog {

    public static final String BEAN_NAME = "CircabcAddTranslationWithoutContentDialog";
    private static final long serialVersionUID = 1835522719203535L;
    protected LogRecord logRecord = new LogRecord();
    private transient LogService logService;
    private transient ManagementService managementService;

    /*
     * (non-Javadoc)
     *
     * @see org.alfresco.web.bean.content.AddTranslationWithoutContentDialog#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters) {
        // the previous call was in error
        if (!this.isFinished && this.newTranslation != null) {
            return;
        }

        // store the parameters, create empty map if necessary
        this.parameters = parameters;

        if (this.parameters == null) {
            this.parameters = Collections.emptyMap();
        }
        this.isFinished = false;

        // get the id of the node we are creating the translation for
        String id = this.parameters.get("id");
        if (id != null && id.length() > 0) {
            try {
                this.newTranslation = new NodeRef(Repository.getStoreRef(), id);

                // handle special folder link node case
                if (ApplicationModel.TYPE_FOLDERLINK
                        .equals(getNodeService().getType(this.newTranslation))) {
                    this.newTranslation = (NodeRef) getNodeService()
                            .getProperty(this.newTranslation, ContentModel.PROP_LINK_DESTINATION);
                }
            } catch (InvalidNodeRefException refErr) {
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF),
                        id));
            }
        } else {
            this.newTranslation = this.navigator.getCurrentNode().getNodeRef();
        }

        // propose the author and the language of the content for the properties of the MLContainer
        if (getNodeService().hasAspect(this.newTranslation, ContentModel.ASPECT_AUTHOR)
                && getNodeService().getProperty(this.newTranslation, ContentModel.PROP_AUTHOR) != null) {
            setAuthor(
                    (String) getNodeService().getProperty(this.newTranslation, ContentModel.PROP_AUTHOR));
        } else {
            setAuthor("");
        }

        if (getNodeService().hasAspect(this.newTranslation, ContentModel.ASPECT_LOCALIZED)
                && getNodeService().getProperty(this.newTranslation, ContentModel.PROP_LOCALE) != null) {
            setLanguage(
                    ((Locale) getNodeService().getProperty(this.newTranslation, ContentModel.PROP_LOCALE))
                            .toString());
        } else {
            setLanguage(null);
        }

        // We do this to use
        this.navigator.setCurrentNodeId(
                getNodeService().getPrimaryParent(this.newTranslation).getParentRef().getId());
        this.browseBean.setDocument(new Node(this.newTranslation));

        super.init(parameters);
    }

    /**
     * Validates the information given for the translation
     *
     * @return boolean Is the translation valid
     */
    protected boolean validate() {
        boolean valid = true;

        //	test if the language is not empty
        if (getLanguage().equalsIgnoreCase("null")) {
            Utils.addErrorMessage(Application
                    .getMessage(FacesContext.getCurrentInstance(), "make_multilingual_error_language"));
            valid = false;
        }

        //	test if the author is not empty
        if (getAuthor() == null || getAuthor().trim().length() < 1) {
            Utils.addErrorMessage(Application
                    .getMessage(FacesContext.getCurrentInstance(), "make_multilingual_error_author"));
            valid = false;
        }

        return valid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.alfresco.web.bean.content.AddTranslationWithoutContentDialog#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (!validate()) {
            this.isFinished = false;
            return null;
        } else {
            final String superOutcome = super.finishImpl(context, outcome);

            getNodeService().addAspect(newTranslation, ContentModel.ASPECT_TITLED, null);
            getNodeService().addAspect(newTranslation, CircabcModel.ASPECT_CIRCABC_MANAGEMENT, null);
            getNodeService().addAspect(newTranslation, CircabcModel.ASPECT_LIBRARY, null);

            return superOutcome;
        }
    }

    /**
     * If the file was uploaded succesfully, we open the edit content properties dialog
     *
     * @param context The Face context
     * @param outcome The default outcome - not used
     * @return String The outcome to edit content properties dialog
     */
//	@Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (outcome == null) {
            // Previous error -> redisplay
            isFinished = false;
            return null;
        }

        Node node = new Node(newTranslation);
        setLogRecord(node);
        logRecord.setService("Library");
        logRecord.setActivity("Add translation without content");
        logRecord.setOK(true);
        logRecord.setInfo(" Only added content properties in" + super.getLanguage());
        getLogService().log(logRecord);

        isFinished = true;
        this.browseBean.contextUpdated();
        this.browseBean.getDocument().reset();

        // redirect the user according the value of (show other properties)
        if (isShowOtherProperties()) {
            this.browseBean.setDocument(new Node(this.newTranslation));
            final Map<String, String> parameters = new HashMap<>(1);
            parameters.put(BaseWaiDialog.NODE_ID_PARAMETER, this.newTranslation.getId());
            Beans.getEditNodePropertiesDialog().init(parameters);
            FacesContext.getCurrentInstance().getViewRoot()
                    .setViewId(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE);
            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR + EditNodePropertiesDialog.DIALOG_CALL;
        } else {
            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
        }
    }

    /**
     * Indicate if the finish button must be disbled or not
     *
     * @return false all the time
     */
    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    /**
     * Action handler called when the dialog is cancelled
     */
    @Override
    public String cancel() {
        super.cancel();

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public String getBrowserTitle() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                "add_translation_without_content_action_wai");
    }

    public String getPageIconAltText() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                "add_translation_without_content_icon_tooltip");
    }

    public boolean isCancelButtonVisible() {
        return true;
    }

    public boolean isFormProvided() {
        return false;
    }

    /**
     * @return the logService
     */
    protected final LogService getLogService() {
        if (logService == null) {
            logService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getLogService();
        }
        return logService;
    }

    /**
     * @param logService the logService to set
     */
    public final void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * @return the ManagementService
     */
    protected final ManagementService getManagementService() {
        if (managementService == null) {
            managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
        }
        return managementService;
    }

    private void setLogRecord(Node node) {
        logRecord.setDocumentID(
                (Long) getNodeService().getProperty(node.getNodeRef(), ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(node.getNodeRef());
        logRecord.setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
        logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        Path path = node.getNodePath();
        logRecord.setPath(PathUtils.getCircabcPath(path, true));
    }
}
