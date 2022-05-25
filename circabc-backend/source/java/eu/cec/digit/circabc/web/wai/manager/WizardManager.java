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
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.wizard.WaiWizard;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.IWizardBean;
import org.alfresco.web.bean.wizard.WizardState;
import org.alfresco.web.config.WizardsConfigElement.WizardConfig;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Util class to manage a WAI wizard inside the wizard framework. It extends the original
 * WizardManager from Alfresco.
 *
 * @author yanick pignot
 * @see org.alfresco.web.bean.dialog.DialogManager
 */
public class WizardManager implements Serializable {

    public static final String BEAN_NAME = "WaiWizardManager";
    public static final String CANCEL_FROM_ACTION = "#{" + BEAN_NAME + ".cancel}";
    /**
     *
     */
    private static final long serialVersionUID = 2895569605474573545L;
    private static final Log logger = LogFactory.getLog(WizardManager.class);
    private org.alfresco.web.bean.wizard.WizardManager nativeManager = null;
    private transient SecurityService securityService;

    public WizardManager() {
        nativeManager = Application.getWizardManager();
    }

    public static boolean areStatesEquals(WizardState state, Object obj) {
        if (state == null || obj == null || !(obj instanceof WizardState)) {
            return false;
        }

        WizardState state2 = (WizardState) obj;

        return state.getConfig().getName().equals(state2.getConfig().getName());
    }

    /**
     * @return the casted wizard dialog bean
     */
    public WaiWizard getWaiBean() {
        return (WaiWizard) nativeManager.getBean();
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

    public boolean isVisibleActions() {
        return getActionsList() != null;
    }

    public ActionsListWrapper getActionsList() {
        return getWaiBean().getActionList();
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

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#finish()
     */
    public String finish(ActionEvent action) {
        return this.finish();
    }

    ////// Delegate methods

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#back()
     */
    public void back() {
        nativeManager.back();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#cancel()
     */
    public String cancel() {
        return nativeManager.cancel();
    }


    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#finish()
     */
    @SuppressWarnings("unchecked")
    public String finish() {
        final String finishOutcome = nativeManager.finish();

        return finishOutcome;
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getBackButtonDisabled()
     */
    public boolean getBackButtonDisabled() {
        return nativeManager.getBackButtonDisabled();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getBackButtonLabel()
     */
    public String getBackButtonLabel() {
        return nativeManager.getBackButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getBean()
     */
    public IWizardBean getBean() {
        return nativeManager.getBean();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getCancelButtonLabel()
     */
    public String getCancelButtonLabel() {
        return nativeManager.getCancelButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getCurrentStep()
     */
    public int getCurrentStep() {
        return nativeManager.getCurrentStep();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getCurrentStepAsString()
     */
    public String getCurrentStepAsString() {
        return nativeManager.getCurrentStepAsString();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getCurrentStepName()
     */
    public String getCurrentStepName() {
        return nativeManager.getCurrentStepName();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getCurrentWizard()
     */
    public WizardConfig getCurrentWizard() {
        return nativeManager.getCurrentWizard();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#setCurrentWizard(org.alfresco.web.config.WizardsConfigElement.WizardConfig)
     */
    public void setCurrentWizard(WizardConfig config) {
        nativeManager.setCurrentWizard(config);
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getDescription()
     */
    public String getDescription() {
        return getSecurityService().getCleanHTML(nativeManager.getDescription(), true);
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getErrorMessage()
     */
    public String getErrorMessage() {
        return nativeManager.getErrorMessage();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getFinishButtonDisabled()
     */
    public boolean getFinishButtonDisabled() {
        return nativeManager.getFinishButtonDisabled();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getFinishButtonLabel()
     */
    public String getFinishButtonLabel() {
        return nativeManager.getFinishButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getIcon()
     */
    public String getIcon() {
        return nativeManager.getIcon();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getNextButtonDisabled()
     */
    public boolean getNextButtonDisabled() {
        return nativeManager.getNextButtonDisabled();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getNextButtonLabel()
     */
    public String getNextButtonLabel() {
        return nativeManager.getNextButtonLabel();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getPage()
     */
    public String getPage() {
        if (logger.isInfoEnabled()) {
            logger.info("current page :" + nativeManager.getPage());
        }
        return nativeManager.getPage();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getState()
     */
    public WizardState getState() {
        return nativeManager.getState();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getStepDescription()
     */
    public String getStepDescription() {
        return nativeManager.getStepDescription();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getStepInstructions()
     */
    public String getStepInstructions() {
        return nativeManager.getStepInstructions();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getStepItems()
     */
    public List<UIListItem> getStepItems() {
        return nativeManager.getStepItems();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getStepTitle()
     */
    public String getStepTitle() {
        return getSecurityService().getCleanHTML(nativeManager.getStepTitle(), false);
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#getTitle()
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
     * @see org.alfresco.web.bean.wizard.WizardManager#next()
     */
    public void next() {
        nativeManager.next();
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#restoreState(org.alfresco.web.bean.wizard.WizardState)
     */
    public void restoreState(WizardState state) {
        nativeManager.restoreState(state);
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#setupParameters(javax.faces.event.ActionEvent)
     */
    public void setupParameters(ActionEvent event) {
        nativeManager.setupParameters(event);
    }

    /**
     * @see org.alfresco.web.bean.wizard.WizardManager#setupParameters(java.util.Map)
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
