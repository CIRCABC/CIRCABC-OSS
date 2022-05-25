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
package eu.cec.digit.circabc.web.app;

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.manager.DialogManager;
import eu.cec.digit.circabc.web.wai.manager.NavigationManager;
import eu.cec.digit.circabc.web.wai.manager.NavigationState;
import eu.cec.digit.circabc.web.wai.manager.WizardManager;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.DialogState;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.WizardState;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean.CLOSE_NAVIGATION_OUTCOME;
import static eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean.WAI_BROWSE_OUTCOME;

/**
 * Extension of AlfrescoNavigationHandler to handle a global container for circabc.
 *
 * @author patrice.coppens@trasys.lu
 */
public class CircabcNavigationHandler extends AlfrescoNavigationHandler {

    public static final String WAI_PREFIX = "wai" + OUTCOME_SEPARATOR;
    public static final String WAI_CONTAINER_SESSION = "waiContainer";

    public static final String CLOSE_WAI_DIALOG_OUTCOME = WAI_PREFIX + CLOSE_DIALOG_OUTCOME;
    public static final String CLOSE_WAI_WIZARD_OUTCOME = WAI_PREFIX + CLOSE_WIZARD_OUTCOME;

    public static final String WAI_DIALOG_PREFIX = WAI_PREFIX + DIALOG_PREFIX;
    public static final String WAI_WIZARD_PREFIX = WAI_PREFIX + WIZARD_PREFIX;

    public static final String WAI_NAVIGATION_CONTAINER_PAGE = "/jsp/extension/wai/navigation/container.jsp";
    public static final String WAI_DIALOG_CONTAINER_PAGE = "/jsp/extension/wai/dialog/container.jsp";
    public static final String WAI_WIZARD_CONTAINER_PAGE = "/jsp/extension/wai/wizard/container.jsp";
    public static final String VIEW_STACK = "_alfViewStack";
    private static final Log logger = LogFactory.getLog(CircabcNavigationHandler.class);

    public CircabcNavigationHandler(NavigationHandler arg0) {
        super(arg0);
    }

    @SuppressWarnings("unchecked")
    public static Stack getViewStack(FacesContext context) {
        Stack viewStack = (Stack) context.getExternalContext().getSessionMap().get(VIEW_STACK);

        if (viewStack == null) {
            viewStack = new Stack();
            context.getExternalContext().getSessionMap().put(VIEW_STACK, viewStack);
        }

        return viewStack;
    }

    @Override
    public void handleNavigation(final FacesContext context, final String fromAction,
                                 String outcome) {
        if (outcome != null) {
            if (isWai(outcome)) {
                outcome = stripPrefix(outcome);

                if (isNavigation(outcome)) {
                    if (isNavigationClosing(outcome)) {
                        Beans.getWaiBrowseBean().clickParent();
                        return;
                    }
                    if (fromAction != null && !fromAction.startsWith("#{")) {
                        Beans.getWaiBrowseBean().applyWaiBrowsing(fromAction, true);
                        this.addCurrentViewToStack(context);
                    } else {
                        Beans.getWaiBrowseBean().refreshBrowsing();
                        return;
                    }
                } else {
                    // Check lock for webdav and disallow update
                    // Issue https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-2819
                    // Issue https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-2820
                    if ("dialog:updateContentWai".equals(outcome) ||
                            "dialog:editDocumentInlineWai".equals(outcome)) {

                        Node actionNode = getActionNode(context);

                        if (actionNode.isLocked()) {
                            Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                                    Application.getMessage(FacesContext.getCurrentInstance(),
                                            "document_locked"));
                            return;
                        }
                    }

                    useCircabcContainer(context);
                }

            } else {
                removeCircabcContainer(context);
            }
        }

        super.handleNavigation(context, fromAction, outcome);
    }

    /**
     * Retrieves the action node from the faces context that holds the triggered event.
     */
    protected Node getActionNode(final FacesContext context) {

        UIViewRoot viewRoot = context.getViewRoot();

        Class<?> clazz = viewRoot.getClass();

        List<?> events = null;

        try {
            Field declaredField = clazz.getDeclaredField("_events");
            declaredField.setAccessible(true);
            events = (List<?>) declaredField.get(viewRoot);
        } catch (Exception e) {
        }

        ActionEvent event = (ActionEvent) events.get(0);

        UIActionLink link = (UIActionLink) event.getComponent();

        Map<String, String> parameters = link.getParameterMap();

        String id = parameters.get(BaseWaiDialog.NODE_ID_PARAMETER);

        Node actionNode = new MapNode(new NodeRef(Repository.getStoreRef(), id));

        return actionNode;
    }

    /**
     * Determines whether the given outcome is wai related
     *
     * @param outcome The outcome to test
     * @return true if outcome is wai related
     */
    protected boolean isWai(String outcome) {
        boolean wai = false;

        if (outcome != null && outcome.startsWith(WAI_PREFIX)) {
            wai = true;
        }

        return wai;
    }

    /**
     * Determines whether the given outcome is navigation related
     *
     * @param outcome The outcome to test
     * @return true if outcome is navigation related
     */
    protected boolean isNavigation(String outcome) {
        boolean nav = false;

        if (outcome != null && outcome.startsWith(WAI_BROWSE_OUTCOME)) {
            nav = true;
        }

        return nav;
    }

    /**
     * Determines whether the given outcome represents a navigation closing
     *
     * @param outcome The outcome to test
     * @return true if the outcome represents a closing navigation
     */
    protected boolean isNavigationClosing(String outcome) {
        boolean closing = false;

        if (outcome != null && outcome.startsWith(CLOSE_NAVIGATION_OUTCOME)) {
            closing = true;
        }

        return closing;
    }

    /**
     * Retrieves the configured dialog container page.
     *
     * @param context FacesContext
     * @return The container page
     */
    @Override
    protected String getDialogContainer(FacesContext context) {
        if (isCircabcContainer(context)) {
            return WAI_DIALOG_CONTAINER_PAGE;
        } else {
            return super.getDialogContainer(context);
        }
    }

    /**
     * Retrieves the configured wizard container page.
     *
     * @param context FacesContext
     * @return The container page
     */
    @Override
    protected String getWizardContainer(FacesContext context) {
        if (isCircabcContainer(context)) {
            return WAI_WIZARD_CONTAINER_PAGE;
        } else {
            return super.getWizardContainer(context);
        }
    }

    protected String getNavigationContainer(FacesContext context) {
        return WAI_NAVIGATION_CONTAINER_PAGE;
    }

    @Override
    protected String getViewIdFromStackObject(FacesContext context, Object topOfStack) {
        String viewId = context.getViewRoot().getViewId();

        if (viewId.equals("/jsp/close.jsp") || (topOfStack instanceof String && ((String) topOfStack)
                .equals("/jsp/close.jsp"))) {
            return WAI_NAVIGATION_CONTAINER_PAGE;
        }
        if (topOfStack instanceof StackElement) {
            final Object stateObject = ((StackElement) topOfStack).getState();
            final String container = ((StackElement) topOfStack).getContainer();

            if (stateObject instanceof NavigationState) {
                final NavigationState state = (NavigationState) stateObject;

                if (!Beans.getWaiBrowseBean().isNodeSecure(state.getNode())) {
                    // the node has certainly be deleted
                    final Stack stack = getViewStack(context);
                    if (stack.isEmpty()) {
                        return WAI_NAVIGATION_CONTAINER_PAGE;
                    } else {
                        return getViewIdFromStackObject(context, getViewStack(context).pop());
                    }
                }

                // restore the dialog state and get the dialog container viewId
                WaiApplication.getNavigationManager().restoreState(state);
            } else if (stateObject instanceof DialogState) {
                // restore the dialog state and get the dialog container viewId
                WaiApplication.getDialogManager().restoreState((DialogState) stateObject);
            } else if (stateObject instanceof WizardState) {
                // restore the wizard state and get the wizard container viewId
                WaiApplication.getWizardManager().restoreState((WizardState) stateObject);
            }

            return container;
        }

        return super.getViewIdFromStackObject(context, topOfStack);

    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addCurrentViewToStack(final FacesContext context) {
        // work out what to add to the stack
        final String viewId = context.getViewRoot().getViewId();

        if (viewId == null || viewId.equalsIgnoreCase("/jsp/close.jsp")) {
            return;
        }

        final Stack stack = getViewStack(context);

        Object objectForStack = null;
        Object topOfStack = null;
        boolean newEqualsTop = false;

        if (!stack.empty()) {
            topOfStack = stack.peek();
        }

        if (viewId.equals(getNavigationContainer(context))) {
            NavigationManager navMgr = WaiApplication.getNavigationManager();
            objectForStack = new StackElement(navMgr.getState(), viewId);

            newEqualsTop = NavigationManager.areStatesEquals(
                    navMgr.getState(),
                    topOfStack instanceof StackElement ? ((StackElement) topOfStack).getState() : topOfStack);
        } else if (isViewAnyDialogContainer(context, viewId)) {
            DialogManager dlgMgr = WaiApplication.getDialogManager();
            objectForStack = new StackElement(dlgMgr.getState(), viewId);

            newEqualsTop = DialogManager.areStatesEquals(
                    dlgMgr.getState(),
                    topOfStack instanceof StackElement ? ((StackElement) topOfStack).getState() : topOfStack);
        } else if (isViewAnyWizardContainer(context, viewId)) {
            WizardManager wizMgr = WaiApplication.getWizardManager();
            objectForStack = new StackElement(wizMgr.getState(), viewId);

            newEqualsTop = WizardManager.areStatesEquals(
                    wizMgr.getState(),
                    topOfStack instanceof StackElement ? ((StackElement) topOfStack).getState() : topOfStack);
        } else {
            objectForStack = viewId;

            newEqualsTop = objectForStack.equals(topOfStack);
        }

        if (stack.empty() || newEqualsTop == false) {
            stack.push(objectForStack);

            if (logger.isDebugEnabled()) {
                logger.debug("Pushed item to view stack: " + objectForStack);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("current view is already top of the view stack!");
            }
        }
    }

    protected boolean isViewAnyWizardContainer(FacesContext context, String viewId) {
        if (viewId == null) {
            return false;
        } else {
            return viewId.equals(getWizardContainer(context)) || WAI_WIZARD_CONTAINER_PAGE.equals(viewId);
        }
    }

    protected boolean isViewAnyDialogContainer(FacesContext context, String viewId) {
        if (viewId == null) {
            return false;
        } else {
            return viewId.equals(getDialogContainer(context)) || WAI_DIALOG_CONTAINER_PAGE.equals(viewId);
        }
    }

    @Override
    protected void handleDialogOrWizardClose(FacesContext context, String fromAction, String outcome,
                                             boolean dialog) {
        // if we allow Alfresco to manage this case, it redirects the user to the native browse page.
        if (getViewStack(context).empty() == true && (
                DialogManager.CANCEL_FROM_ACTION.equals(fromAction) || WizardManager.CANCEL_FROM_ACTION
                        .equals(fromAction))) {
            this.handleNavigation(context, null, CircabcBrowseBean.WAI_BROWSE_OUTCOME);
        } else {
            String closingItem = dialog ? "dialog" : "wizard";

            // if we are closing a wizard or dialog take the view off the
            // top of the stack then decide whether to use the view
            // or any overridden outcome that may be present
            if (getViewStack(context).empty() == false) {
                // is there an overidden outcome?
                String overriddenOutcome = getOutcomeOverride(outcome);
                if (overriddenOutcome == null) {
//            	 there isn't an overidden outcome so go back to the previous view
                    if (logger.isDebugEnabled()) {
                        logger.debug("Closing " + closingItem);
                    }

                    // determine how many levels of dialog we need to close
                    int numberToClose = getNumberToClose(outcome);

                    Object stackObject = null;
                    if (numberToClose == 1) {
                        // just closing one dialog so get the item from the top of the stack
                        stackObject = getViewStack(context).pop();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Popped item from the top of the view stack: " + stackObject);
                        }
                    } else {
                        // check there are enough items on the stack, if there
                        // isn't just get the last one (effectively going back
                        // to the beginning)
                        Stack viewStack = getViewStack(context);
                        int itemsOnStack = viewStack.size();
                        if (itemsOnStack < numberToClose) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Returning to first item on the view stack as there aren't " +
                                        numberToClose + " containers to close!");
                            }

                            numberToClose = itemsOnStack;
                        }

                        // pop the right object from the stack
                        for (int x = 1; x <= numberToClose; x++) {
                            stackObject = viewStack.pop();
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Popped item from the stack: " + stackObject);
                        }
                    }

                    // this fix is implemented to correct a wrong redirection when closing a dialog after a bulkdownload.
                    // bulk download is forcing call from Alfresco UI, we have to detect it thanks to the following IF.
                    if (stackObject.equals("/jsp/browse/browse.jsp") && !context.getViewRoot().getViewId()
                            .equals("/jsp/dialog/container.jsp")) {
                        // back to CIRCABC extension
                        stackObject = "/jsp/extension/wai/navigation/container.jsp";
                    }

                    // get the appropriate view id for the stack object
                    String newViewId = getViewIdFromStackObject(context, stackObject);

                    // go to the appropraite page
                    goToView(context, newViewId);
                } else {
                    // we also need to empty the dialog stack if we have been given
                    // an overidden outcome as we could be going anywhere in the app.
                    // grab the current top item first though in case we need to open
                    // another dialog or wizard
                    String previousViewId = getViewIdFromStackObject(context, getViewStack(context).peek());
                    //getViewStack(context).clear();

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Closing " + closingItem + " with an overridden outcome of '" + overriddenOutcome
                                        + "'");
                    }

                    // if the override is calling another dialog or wizard come back through
                    // the navigation handler from the beginning
                    if (isDialog(overriddenOutcome) || isWizard(overriddenOutcome)) {
                        // set the view id to the page at the top of the stack so when
                        // the new dialog or wizard closes it goes back to the correct page
                        context.getViewRoot().setViewId(previousViewId);

                        if (logger.isDebugEnabled()) {
                            logger.debug("view stack: " + getViewStack(context));
                            logger.debug("Opening '" + overriddenOutcome + "' after " + closingItem +
                                    " close using view id: " + previousViewId);
                        }

                        this.handleNavigation(context, fromAction, overriddenOutcome);
                    } else {
                        navigate(context, fromAction, overriddenOutcome);
                    }
                }
            } else {
                // we are trying to close a dialog when one hasn't been opened!
                // return to the main page of the app (print warning if debug is enabled)
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to close a " + closingItem
                            + " with an empty view stack, returning 'browse' outcome");
                }

                navigate(context, fromAction, "browse");
            }
        }
    }

    /**
     * Navigates to the appropriate page using the original navigation handler
     *
     * @param context    FacesContext
     * @param fromAction The fromAction
     * @param outcome    The outcome
     */
    private void navigate(FacesContext context, String fromAction, String outcome) {
        if (logger.isDebugEnabled()) {
            logger.debug("Passing outcome '" + outcome + "' to original navigation handler");
        }

        super.handleNavigation(context, fromAction, outcome);
    }

    /**
     * Dispatches to the given view id
     *
     * @param context Faces context
     * @param viewId  The view id to go to
     */
    private void goToView(FacesContext context, String viewId) {
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, viewId);
        viewRoot.setViewId(viewId);
        context.setViewRoot(viewRoot);
        context.renderResponse();
    }

    @Override
    protected boolean isDialog(String outcome) {
        if (isWai(outcome)) {
            outcome = stripPrefix(outcome);
        }

        if (super.isDialog(outcome)) {
            return true;
        } else if (isNavigation(outcome)) {
            // workaround to force the AlfrescoNavigationHandler.handleDialogOrWizardClose method to manage
            // the navigation outcome as a dialog when we have an overriden outcome
            // (ie:  dialog:close:wai:browse-wai)

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isWizard(String outcome) {
        if (isWai(outcome)) {
            outcome = stripPrefix(outcome);
        }
        return super.isWizard(outcome);
    }

    @Override
    protected boolean isWizardStep(String fromAction) {
        boolean wizardStep = false;

        if (fromAction != null &&
                (fromAction.equals("#{WaiWizardManager.next}") || fromAction
                        .equals("#{WaiWizardManager.back}"))) {
            wizardStep = true;
        } else {
            wizardStep = super.isWizardStep(fromAction);
        }

        return wizardStep;
    }

    /** !!! END OF OVERRIDEN FROM THE SUPER CLASS !!! */

    /**
     * get if the param to use wai container (for dialog and/or wizard) is setted .
     */
    @SuppressWarnings("unchecked")
    public boolean isCircabcContainer(FacesContext context) {
        Object obj = context.getExternalContext().getSessionMap().get(WAI_CONTAINER_SESSION);

        return obj != null && obj instanceof Boolean && (Boolean) obj;
    }

    /**
     * add param to use wai container (for dialog and/or wizard) instead the original container.
     */
    @SuppressWarnings("unchecked")
    public void useCircabcContainer(FacesContext context) {
        context.getExternalContext().getSessionMap().put(WAI_CONTAINER_SESSION, Boolean.TRUE);
    }

    /**
     * remove param to use wai container (for dialog and/or wizard) instead the original container.
     */
    public void removeCircabcContainer(FacesContext context) {
        context.getExternalContext().getSessionMap().remove(WAI_CONTAINER_SESSION);
    }


    public static class StackElement implements Serializable {

        private static final long serialVersionUID = -2357072473640593852L;

        private Object state;
        private String container;

        /**
         * @param state
         * @param container
         */
        public StackElement(Object state, String container) {
            super();

            ParameterCheck.mandatory("State", state);
            ParameterCheck.mandatoryString("Container", container);

            this.state = state;
            this.container = container;
        }

        /**
         * @return the container
         */
        public String getContainer() {
            return container;
        }

        /**
         * @return the state
         */
        public Object getState() {
            return state;
        }
    }

}



