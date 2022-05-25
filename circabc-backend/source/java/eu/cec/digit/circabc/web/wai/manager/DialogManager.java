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
package eu.cec.digit.circabc.web.wai.manager;

import eu.cec.digit.circabc.repo.app.SecurityService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.dialog.IECompatibilityPreference;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialogAsync;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.DialogState;
import org.alfresco.web.bean.dialog.IDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Util class to manage a WAI dialog inside the dialog framework. It extends the original
 * DialogManager from Alfresco.
 *
 * @author yanick pignot
 * @see org.alfresco.web.bean.dialog.DialogManager
 */


public class DialogManager implements Serializable {

    public static final String BEAN_NAME = "WaiDialogManager";
    public static final String CANCEL_FROM_ACTION = "#{" + BEAN_NAME + ".cancel}";
    /**
     *
     */
    private static final long serialVersionUID = -3308185189817256110L;
    private static final Log logger = LogFactory.getLog(DialogManager.class);
    private org.alfresco.web.bean.dialog.DialogManager nativeManager = null;
    private transient SecurityService securityService;

    public DialogManager() {
        nativeManager = Application.getDialogManager();
    }

    public static boolean areStatesEquals(DialogState state, Object obj) {

        if (state == null || obj == null || !(obj instanceof DialogState)) {
            return false;
        }

        DialogState state2 = (DialogState) obj;

        return state.getConfig().getName().equals(state2.getConfig().getName());
    }

    /**
     * @return the casted wai dialog bean
     */
    public WaiDialog getWaiBean() {
        return (WaiDialog) nativeManager.getBean();
    }

    /**
     * @return
     **/
    public String getIconAlt() {
        return getWaiBean().getPageIconAltText();
    }

    /**
     * @return
     */
    public String getBrowserTitle() {
        return getWaiBean().getBrowserTitle();
    }

    /**
     * @return
     */
    public boolean isFormProvided() {
        return getWaiBean().isFormProvided();
    }

    public boolean isNavigationVisible() {
        return getNavigation() != null;
    }

    public List<Node> getNavigation() {
        return WaiApplication.getNavigationManager().getNavigation();
    }

    public boolean isIconVisible() {
        return getIcon() != null;
    }

    public boolean isVisibleOKButton() {
        return isOKButtonVisible();
    }

    public boolean isVisibleCancelButton() {
        return getWaiBean().isCancelButtonVisible();
    }

    public boolean isVisibleRightMenu() {
        return isVisibleCancelButton() || isVisibleOKButton() || isVisibleActions();
    }

    public boolean isVisibleActions() {
        return getActionsList() != null;
    }

    public ActionsListWrapper getActionsList() {
        return getWaiBean().getActionList();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#finish()
     */
    public String finish(ActionEvent action) {
        return this.finish();
    }

    ////// Delegate methods

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#cancel()
     */
    public String cancel() {
        return nativeManager.cancel();
    }


    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#finish()
     */
    @SuppressWarnings("unchecked")
    public String finish() {
        String finishOutcome = nativeManager.finish();

        if (finishOutcome == null) {
            Stack stack = (Stack) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .get("_alfViewStack");
            //if(stack.lastElement().equals("/jsp/close.jsp"))
            //{
            //stack.pop();
            //}

            if (stack.isEmpty()) {
                stack.push(CircabcNavigationHandler.WAI_DIALOG_PREFIX + getCurrentDialog().getName());
            }

            finishOutcome = CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                    AlfrescoNavigationHandler.OUTCOME_SEPARATOR +
                    CircabcNavigationHandler.WAI_DIALOG_PREFIX
                    + getCurrentDialog().getName();
        }

        return finishOutcome;
    }


    public String getFinishAsynchButtonLabel() {
        IDialogBean dialog = getState().getDialog();
        if (dialog instanceof WaiDialogAsync) {
            return ((WaiDialogAsync) dialog).getFinishAsyncButtonLabel();
        } else {
            return "";
        }
    }

    public boolean isVisibleOKAsynkButton() {
        IDialogBean dialog = getState().getDialog();
        if (dialog instanceof WaiDialogAsync) {
            return ((WaiDialogAsync) dialog).isFinishAsyncButtonVisible();
        } else {
            return false;
        }
    }


    public boolean getFinishAsyncButtonDisabled() {
        IDialogBean dialog = getState().getDialog();
        if (dialog instanceof WaiDialogAsync) {
            return ((WaiDialogAsync) dialog).isFinishAsyncButtonDisabled();
        } else {
            return false;
        }

    }


    public String finishAsync() {
        IDialogBean dialog = getState().getDialog();
        if (dialog instanceof WaiDialogAsync) {
            return ((WaiDialogAsync) dialog).finishAsync();
        } else {
            return "";
        }


    }

    public String getIECompatibilityMode() {
        IDialogBean dialog = getState().getDialog();
        if (dialog instanceof IECompatibilityPreference) {
            return ((IECompatibilityPreference) dialog).getMode();
        } else {
            return IECompatibilityPreference.IE_8;
        }
    }


    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getActions()
     */
    public String getActionsId() {
        return nativeManager.getActionsId();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getActions()
     */
    public String getActions() {
        return getActionsId();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getAdditionalButtons()
     */
    public List<DialogButtonConfig> getAdditionalButtons() {
        return nativeManager.getAdditionalButtons();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getBean()
     */
    public IDialogBean getBean() {
        return nativeManager.getBean();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getCancelButtonLabel()
     */
    public String getCancelButtonLabel() {
        return nativeManager.getCancelButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getCurrentDialog()
     */
    public DialogConfig getCurrentDialog() {
        return nativeManager.getCurrentDialog();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#setCurrentDialog(org.alfresco.web.config.DialogsConfigElement.DialogConfig)
     */
    public void setCurrentDialog(DialogConfig config) {
        nativeManager.setCurrentDialog(config);
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getDescription()
     */
    public String getDescription() {
        return getSecurityService().getCleanHTML(nativeManager.getDescription(), true);
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getErrorMessage()
     */
    public String getErrorMessage() {
        return nativeManager.getErrorMessage();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getFinishButtonDisabled()
     */
    public boolean getFinishButtonDisabled() {
        return nativeManager.getFinishButtonDisabled();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getFinishButtonLabel()
     */
    public String getFinishButtonLabel() {
        return nativeManager.getFinishButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getIcon()
     */
    public String getIcon() {
        return nativeManager.getIcon();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getPage()
     */
    public String getPage() {
        if (logger.isInfoEnabled()) {
            logger.info("current page :" + nativeManager.getPage());
        }
        return nativeManager.getPage();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getState()
     */
    public DialogState getState() {
        return nativeManager.getState();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#getTitle()
     */
    public String getTitle() {
        return getSecurityService().getCleanHTML(nativeManager.getTitle(), false);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return nativeManager.hashCode();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#isOKButtonVisible()
     */
    public boolean isOKButtonVisible() {
        return nativeManager.isOKButtonVisible();
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#restoreState(org.alfresco.web.bean.dialog.DialogState)
     */
    public void restoreState(DialogState state) {
        nativeManager.restoreState(state);
    }

    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#setupParameters(javax.faces.event.ActionEvent)
     */
    public void setupParameters(ActionEvent event) {
        // check the component the event come from was an action link
        final UIComponent component = event.getComponent();

        Map<String, String> parameters = null;

        if (component instanceof UIActionLink) {
            // store the parameters
            parameters = ((UIActionLink) component).getParameterMap();
        } else if (component instanceof UICommand) {
            final UICommand command = (UICommand) component;
            final List childs = command.getChildren();

            if (childs != null) {
                parameters = new HashMap<>(childs.size());
                for (Object child : childs) {
                    if (child instanceof UIParameter) {
                        UIParameter param = (UIParameter) child;
                        parameters.put(param.getName(), param.getValue().toString());
                    }
                }
            }
        }

        if (parameters == null) {
            parameters = new HashMap<>(1);
        }

        setupParameters(parameters);
    }


    /**
     * @see org.alfresco.web.bean.dialog.DialogManager#setupParameters(java.util.Map)
     */
    public void setupParameters(Map<String, String> arg0) {
        nativeManager.setupParameters(arg0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return nativeManager.toString();
    }

    public SecurityService getSecurityService() {
        if (securityService == null) {
            securityService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getSecurityService();
        }
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }


}
