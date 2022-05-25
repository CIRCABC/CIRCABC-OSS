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
package eu.cec.digit.circabc.web.wai.dialog.content;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.validator.URLValidator;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBeanData;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.generic.EditNodePropertiesDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean responsible for the add URL dialog
 *
 * @author David Ferraz
 */
public class AddUrlDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcAddUrlDialog";
    protected static final String MSG_ERROR_NAME_EMPTY = "library_add_url_name_empty";
    protected static final String MSG_ERROR_URL_INVALID = "library_add_url_url_invalid";
    protected static final String MSG_ERROR_NAME_EXISTS = "library_add_content_error_duplicate_name";
    private static final String LIBRARY_ADD_URL_TITLE = "library_add_url_title";
    private static final String LIBRARY_ADD_URL_ICON_TOOLTIP = "library_add_url_icon_tooltip";
    private static final String LIBRARY_ADD_URL_ACTION = "library_add_url_action";
    private static final String HTML = ".html";
    private static final String PATTERN_HTML_EXTENSION = ".*\\.htm(l)?";
    private static final String AN_URL_CAN_ONLY_BE_CREATED_WITHIN_A_SPACE = "An URL can only be created within a space";
    private static final long serialVersionUID = 8166820634281692712L;
    private String title;
    private String url;
    private boolean disableNotifications = false;
    private BehaviourFilter policyBehaviourFilter;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        disableNotifications = false;

        if (parameters != null) {
            title = null;
            url = null;
        }
        logRecord.setService("Library");
        logRecord.setActivity("Add URL");
    }

    /**
     * Migration 3.1 -> 3.4.6 - 15/12/2011 - Wrapped action inside transaction for Alfresco 3.4.6
     */
    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                return doPostCommitProcessingIntern(context, outcome);
            }
        };
        return txnHelper.doInTransaction(callback, false, true);
    }

    private String doPostCommitProcessingIntern(FacesContext context, String outcome) {
        getPolicyBehaviourFilter().enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        return super.doPostCommitProcessing(context, outcome);
    }


    protected String getErrorOutcome(Throwable exception) {
        getPolicyBehaviourFilter().enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        return super.getErrorOutcome(exception);
    }


    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        getPolicyBehaviourFilter().disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);

        if (!validate()) {
            // if the new URL doesn't pass the validation tests
            return null;
        }

        String name = WebClientHelper.toValidFileName(getTitle());

        if (!name.matches(PATTERN_HTML_EXTENSION)) {
            name = name + HTML;
        }

        Node actionNode = getActionNode();
        final NodeRef parentRef = actionNode.getNodeRef();
        name = WebClientHelper.generateUniqueName(getNodeService(), parentRef, name);

        NodeRef createdNode;

        // 1. tests if the actionNode is a space or a subtype of space
        if (isParentSpace(actionNode)) {
            // the dialog is called from a normal space
            final FileInfo fileInfo = getFileFolderService()
                    .create(parentRef, name, ContentModel.TYPE_CONTENT);
            createdNode = fileInfo.getNodeRef();
        } else if (isParentCategory(actionNode)) {
            // the dialog is called to add a link to the list of categories

            // create the node beneath the circabc root
            final FileInfo fileInfo = getFileFolderService().create(
                    getNavigator().getCircabcHomeNode().getNodeRef(),
                    name,
                    ContentModel.TYPE_CONTENT);
            createdNode = fileInfo.getNodeRef();

            // add the classifiable aspect
            getNodeService().addAspect(createdNode, ContentModel.ASPECT_GEN_CLASSIFIABLE, null);

            // link the created node to the category
            final ArrayList<NodeRef> categories = new ArrayList<>();
            categories.add(actionNode.getNodeRef());
            this.getNodeService().setProperty(createdNode, ContentModel.PROP_CATEGORIES, categories);

        } else {
            throw new IllegalArgumentException(AN_URL_CAN_ONLY_BE_CREATED_WITHIN_A_SPACE);
        }

        if (createdNode == null) {
            // if an unexpected error has occurred
            return null;
        }

        // 3. adds the title aspect and properties
        final Map<QName, Serializable> titledProps = new HashMap<>(3, 1.0f);
        titledProps.put(ContentModel.PROP_TITLE, getTitle());
        this.getNodeService().addAspect(createdNode, ContentModel.ASPECT_TITLED, titledProps);

        // 4. adds the url aspect and properties
        this.getNodeService().addAspect(createdNode, DocumentModel.ASPECT_URLABLE, null);
        this.getNodeService().setProperty(createdNode, DocumentModel.PROP_URL, getUrl());

        // set the current node being the new created node
        getBrowseBean().setDocument(new Node(createdNode));

        // as we were successful, go to the set properties dialog and set the dialog parameters
        final Map<String, String> parameters = new HashMap<>(1);
        parameters.put(BaseWaiDialog.NODE_ID_PARAMETER, createdNode.getId());
        parameters.put(EditNodePropertiesDialog.NOTIFY_AFTER_SETTING_PROPERTIES,
                String.valueOf(!disableNotifications));
        Beans.getEditNodePropertiesDialog().init(parameters);

        if (isParentCategory(actionNode)) {
            //reset cache
            final CategoryHeadersBeanData categoryHeadersBeanData = (CategoryHeadersBeanData) Beans
                    .getBean("CategoryHeadersBeanData");
            categoryHeadersBeanData.reset();
        }
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + EditNodePropertiesDialog.DIALOG_CALL;


    }

    public String getBrowserTitle() {
        return translate(LIBRARY_ADD_URL_TITLE);
    }

    public String getPageIconAltText() {
        return translate(LIBRARY_ADD_URL_ICON_TOOLTIP);
    }

    @Override
    public String getFinishButtonLabel() {
        return translate(LIBRARY_ADD_URL_ACTION);
    }


    /**
     * @param node protected void checkParent(final Node node) { final QName type = node.getType();
     *             <p>
     *             // looks for Space if (!(ContentModel.TYPE_FOLDER.equals(type) ||
     *             getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER))) { throw new
     *             IllegalArgumentException(AN_URL_CAN_ONLY_BE_CREATED_WITHIN_A_SPACE); } }
     */

    protected boolean isParentSpace(final Node node) {
        final QName type = node.getType();
        return ContentModel.TYPE_FOLDER.equals(type) || getDictionaryService()
                .isSubClass(type, ContentModel.TYPE_FOLDER);
    }

    protected boolean isParentCategory(final Node node) {
        return ContentModel.TYPE_CATEGORY.equals(node.getType());
    }

    protected boolean validate() {
        boolean valid = true;

        if (!isCleanHTML(title, false)) {

            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("title")));
            valid = false;
        }
        //	tests if the name is not empty
        if (getTitle() == null || getTitle().trim().length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_NAME_EMPTY));
            valid = false;
        }

        //	tests if the URL is not empty and correct
        try {
            URLValidator.evaluate(getUrl());
        } catch (final ValidatorException ex) {
            final FacesMessage msg = ex.getFacesMessage();

            Utils.addErrorMessage(translate(msg.getSummary()));
            valid = false;
        }

        return valid;
    }


    /**
     * @see org.alfresco.web.bean.spaces.CreateSpaceWizard#getName()
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @see org.alfresco.web.bean.spaces.CreateSpaceWizard#setTitle(String)
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * @return the disableNotifications
     */
    public boolean isDisableNotifications() {
        return disableNotifications;
    }

    /**
     * @param disableNotifications the disableNotifications to set
     */
    public void setDisableNotifications(boolean disableNotifications) {
        this.disableNotifications = disableNotifications;
    }
}
