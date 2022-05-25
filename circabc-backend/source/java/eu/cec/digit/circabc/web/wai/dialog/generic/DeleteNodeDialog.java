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
package eu.cec.digit.circabc.web.wai.dialog.generic;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Arrays;
import java.util.Map;

/**
 * Bean to handle the WAI delete content/space/forum/mlcontainer/...  dialog.
 * <p>
 * <p>
 * TODO rename the class and bean name to DeleteNodeDialog
 *
 * @author yanick pignot
 */
public class DeleteNodeDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "DeleteNodeDialog";
    private static final String MSG_ERROR_CANT_DELETE_SPACE_THAT_CONTAINS_CHECKOUT_DOCUMENTS = "msg_error_cant_delete_space_that_contains_checkout_documents";
    private static final long serialVersionUID = 5511047928192880756L;
    private static final Log logger = LogFactory.getLog(DeleteNodeDialog.class);
    protected ManagedNodes actionNodeType;
    private NodeRef parent;

    @Override
    public void init(final Map<String, String> parameters) {
        try {
            super.init(parameters);

            if (parameters != null) {
                actionNodeType = null;

                if (getActionNode() == null) {
                    throw new NullPointerException("The node id is a mandatory parameter");
                }

                actionNodeType = ManagedNodes.resolve(getActionNode());

                if (actionNodeType == null) {
                    throw new IllegalArgumentException(
                            "This kind of node is not managed by this dialog. Node Type : " + getActionNode()
                                    .getType().getLocalName() + " . Expected: " + Arrays
                                    .toString(ManagedNodes.values()));
                }
                setServiceAndActivity();
            }
        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("init failed:" + ex.getMessage(), ex);
            }
        }
    }

    private void setServiceAndActivity() {
        String activity = null;
        String service = null;
        switch (actionNodeType) {
            case CONTENT:
                service = super.getServiceFromActionNode();
                activity = "Delete document";
                break;
            case SPACE:
                service = super.getServiceFromActionNode();
                activity = "Delete space";
                break;
            case DOSSIER:
                service = "Library";
                activity = "Delete dossier";
                break;
            case EMPTY_TRANSLATION:
                service = "Library";
                activity = "Delete empty translation";
                break;
            case FORUM:
                service = super.getServiceFromActionNode();
                activity = "Delete forum";
                break;
            case FORUMS:
                service = "Newsgroup";
                activity = "Delete forums";
                break;
            case LINK:
                service = super.getServiceFromActionNode();
                activity = "Delete link";
                break;
            case ML_CONTAINER:
                service = "Library";
                activity = "Multy lingual container link";
                break;
            case POST:
                service = super.getServiceFromActionNode();
                activity = "Delete post";
                break;
            case TOPIC:
                service = super.getServiceFromActionNode();
                activity = "Delete topic";
                break;
            case TRANSLATION:
                service = "Library";
                activity = "Delete translation";
                break;
            case EXTERNAL_LINK:
                service = "Library";
                activity = "Delete URL";
                break;
            case CATEGORY_HEADER_NODE:
                service = "Administration";
                activity = "Delete category header";
                break;
            case CATEGORY_NODE:
                service = "Administration";
                activity = "Delete category";
                break;
            case CIRCABC_NODE:
                service = "Administration";
                activity = "Delete CIRCABC";
                break;
            case EVENTS_NODE:
                service = "Events";
                activity = "Delete event";
                break;
            case SURVEY_NODE:
                service = "Syrvey";
                activity = "Delete survey";
                break;
            case INFO_SERVICE_NODE:
                service = "Administration";
                activity = "Delete information";
                break;
            case LIBRARY_NODE:
                service = "Administration";
                activity = "Delete library";
                break;
            case NEWSGROUP_NODE:
                service = "Administration";
                activity = "Delete newsgroup";
                break;
            case IG_NODE:
                service = "Administration";
                activity = "Delete interest group";
                break;

            default:
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Not defined logging activity and service for Action Node Type " + actionNodeType
                                    .toString());
                }
        }
        logRecord.setActivity(activity);
        logRecord.setService(service);
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        try {
            parent = null;

            // add property from what interest group node is deleted
            NodeRef igRoot = getManagementService().getCurrentInterestGroup(getActionNode().getNodeRef());
            if (igRoot != null) {
                getNodeService()
                        .setProperty(getActionNode().getNodeRef(), CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED,
                                igRoot.getId());
            }

            if (getActionNode() != null && actionNodeType != null) {
                if (ManagedNodes.ML_CONTAINER.equals(actionNodeType)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Trying to delete multilingual container: " + getActionNode().getId()
                                + " and its translations");
                    }

                    final NodeRef pivot = getMultilingualContentService()
                            .getPivotTranslation(getActionNode().getNodeRef());

                    parent = getNodeService().getPrimaryParent(pivot).getParentRef();

                    // delete the mlContainer and its translations
                    getMultilingualContentService().deleteTranslationContainer(getActionNode().getNodeRef());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Trying to delete " + actionNodeType.toString() + " node: " + getActionNode()
                                        .getId());
                    }

                    parent = getNodeService().getPrimaryParent(getActionNode().getNodeRef()).getParentRef();

                    final String nodeName = (String) this.getNodeService()
                            .getProperty(getActionNode().getNodeRef(), ContentModel.PROP_NAME);
                    logRecord.setInfo("deleted " + nodeName);
                    try {
                        // delete the node
                        this.getNodeService().deleteNode(getActionNode().getNodeRef());
                    } catch (final NodeLockedException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Can't delete the node - node is locked:" + getActionNode().getNodeRef());
                        }
                        Utils.addStatusMessage(FacesMessage.SEVERITY_WARN,
                                translate(MSG_ERROR_CANT_DELETE_SPACE_THAT_CONTAINS_CHECKOUT_DOCUMENTS));
                    }
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Delete called without a document!");
                }
            }
        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("finishImpl:" + ex.getMessage(), ex);
            }
        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    /**
     * @return String The outcome to return to the WAI browse page
     */
    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        try {
            outcome = super.doPostCommitProcessing(context, outcome);
            this.isFinished = true;
            if (parent != null) {
                getBrowseBean().clickWai(parent);
            }
        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("doPostCommitProcessing:" + ex.getMessage(), ex);
            }
        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    /**
     * Returns the confirmation to display to the user before deleting the content.
     *
     * @return The formatted message to display
     */
    public String getConfirmMessage() {
        if (getActionNode() != null && getNodeService().exists(getActionNode().getNodeRef())
                && actionNodeType != null) {
            return translate("delete_" + actionNodeType.toString() + "_dialog_confirmation",
                    getActionNode().getName());
        } else {
            // this method can be called a second time at the restaure time and the node will not longer exists.
            return "";
        }
    }

    public String getBrowserTitle() {
        return translate("delete_" + actionNodeType.toString() + "_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("delete_" + actionNodeType.toString() + "_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate("delete_" + actionNodeType.toString() + "_dialog_title");
    }

    @Override
    public String getContainerDescription() {
        return translate("delete_" + actionNodeType.toString() + "_dialog_description");
    }


}
