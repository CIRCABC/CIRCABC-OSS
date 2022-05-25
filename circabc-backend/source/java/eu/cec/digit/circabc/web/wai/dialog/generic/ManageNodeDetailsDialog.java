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

import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean to handle the display of the space details on the WAI part.
 *
 * @author yanick pignot
 */
public class ManageNodeDetailsDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "ManageNodeDetailsDialog";
    private static final long serialVersionUID = -5362799998894071426L;
    private static final Log logger = LogFactory.getLog(ManageNodeDetailsDialog.class);
    private static final String MSG_SUCCESS_OWNERSHIP = "success_take_ownership";
    private static final String MSG_SUCCESS_VERSION = "success_allow_versionning";

    private transient OwnableService ownableService;

    private ManagedNodes actionNodeType;

    @Override
    public void init(Map<String, String> parameters) {
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
            } else if (ManagedNodes.CONTENT.equals(actionNodeType) || ManagedNodes.TRANSLATION
                    .equals(actionNodeType) ||
                    ManagedNodes.EMPTY_TRANSLATION.equals(actionNodeType) || ManagedNodes.ML_CONTAINER
                    .equals(actionNodeType)) {
                throw new IllegalArgumentException(
                        "This kind of node is not managed by this dialog but the wai navigation framework. Node Type : "
                                + actionNodeType);
            }
            setServiceAndActivity();

        }

    }

    private void setServiceAndActivity() {
        String activity = null;
        String service = null;
        switch (actionNodeType) {
            case CONTENT:
                service = "Library";
                activity = "Manage Document";

                break;
            case DOSSIER:
                service = "Library";
                activity = "Manage Dossier";

                break;
            case EMPTY_TRANSLATION:
                service = "Library";
                activity = "Manage  Empty Translation";

                break;
            case FORUM:
                service = "Newsgroup";
                activity = "Manage Forum";

                break;
            case FORUMS:
                service = "Newsgroup";
                activity = "Manage Forums";

                break;
            case LINK:
                service = "Library";
                activity = "Manage link";

                break;
            case ML_CONTAINER:

                service = "Library";
                activity = "Manage Multi Lingual Container Link";

                break;
            case POST:

                service = "Newsgroup";
                activity = "Manage  Post";

                break;
            case SPACE:
                service = "Library";
                activity = "Manage space";

                break;
            case TOPIC:
                service = "Newsgroup";
                activity = "Manage Topic";

                break;
            case TRANSLATION:
                service = "Library";
                activity = "Manage transaltion";

                break;

            case IG_NODE:
                service = "Administration";
                activity = "Manage interest group";
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
    public ActionsListWrapper getActionList() {
        return new ActionsListWrapper(getActionNode(),
                actionNodeType.toString() + "_details_actions_wai");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public boolean isLocked() {
        return getActionNode().isLocked();
    }

    /**
     * @return true if the current space is a dossier
     */
    public boolean isEventsNotifiables() {
        return !ManagedNodes.DOSSIER.equals(actionNodeType) && !ManagedNodes.LINK
                .equals(actionNodeType);
    }


    /**
     * Action Handler to take Ownership of the current node
     */
    public void applyVersionable(final ActionEvent event) {
        final Map<String, String> params = getActionParams(event);
        final NodeRef ref = getParamNodeRef(params);
        setActivityOk(params, ref);

        try {
            RetryingTransactionHelper txnHelper = Repository
                    .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                public Object execute() throws Throwable {
                    getNodeService().addAspect(ref, ContentModel.ASPECT_VERSIONABLE,
                            Collections.<QName, Serializable>singletonMap(ContentModel.PROP_AUTO_VERSION,
                                    Boolean.TRUE));

                    String msg = translate(MSG_SUCCESS_VERSION);
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, msg);

                    getBrowseBean().refreshBrowsing();
                    return null;
                }
            };
            txnHelper.doInTransaction(callback);
        } catch (Throwable e) {
            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, e.getMessage()), e);
            logRecord.setOK(false);
        } finally {
            logRecord.setInfo(
                    "took ownership of  " + getNodeService().getProperty(ref, ContentModel.PROP_NAME));
            getLogService().log(logRecord);
        }

    }

    /**
     * Action Handler to take Ownership of the current node
     */
    public void takeOwnership(final ActionEvent event) {
        final Map<String, String> params = getActionParams(event);
        final NodeRef ref = getParamNodeRef(params);
        setActivityOk(params, ref);

        try {
            RetryingTransactionHelper txnHelper = Repository
                    .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                public Object execute() throws Throwable {
                    getOwnableService().takeOwnership(ref);

                    String msg = translate(MSG_SUCCESS_OWNERSHIP);
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, msg);

                    getBrowseBean().refreshBrowsing();

                    return null;
                }
            };
            txnHelper.doInTransaction(callback);
        } catch (Throwable e) {

            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, e.getMessage()), e);
            logRecord.setOK(false);
        } finally {
            logRecord.setInfo(
                    "took ownership of  " + getNodeService().getProperty(ref, ContentModel.PROP_NAME));
            getLogService().log(logRecord);
        }

    }

    private Map<String, String> getActionParams(final ActionEvent event) {
        final UIComponent component = event.getComponent();
        final UIActionLink link = (UIActionLink) component;
        return link.getParameterMap();
    }


    private NodeRef getParamNodeRef(final Map<String, String> params) {
        final String id = params.get(NODE_ID_PARAMETER);

        if (id == null) {
            throw new NullPointerException("The node id is a mandatory parameter");
        }

        return new NodeRef(Repository.getStoreRef(), id);
    }

    private void setActivityOk(final Map<String, String> params, final NodeRef ref) {
        ParameterCheck.mandatoryString("activity parameter", params.get(ACTIVITY_PARAMETER));
        ParameterCheck.mandatoryString("service parameter", params.get(SERVICE_PARAMETER));

        super.updateLogDocument(ref, logRecord);
        logRecord.setActivity(params.get(ACTIVITY_PARAMETER));
        logRecord.setService(params.get(SERVICE_PARAMETER));
        logRecord.setOK(true);
    }

    @Override
    public void restored() {
        final Map<String, String> parameters = new HashMap<>(1);
        parameters.put(NODE_ID_PARAMETER, getActionNode().getId());
        this.init(parameters);
    }

    public String getPanelTooltip() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_panel_tooltip");
    }

    public String getNodeIconAltText() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_node_icon_tooltip",
                getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_icon_tooltip");
    }

    public String getIcon() {
        return (String) getActionNode().getProperties().get("icon");
    }

    @Override
    public String getContainerTitle() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_title",
                getActionNode().getName());
    }

    @Override
    public String getContainerDescription() {
        return translate("manage_" + actionNodeType.toString() + "_details_dialog_description");
    }

    protected final OwnableService getOwnableService() {
        if (ownableService == null) {
            ownableService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getOwnableService();
        }
        return ownableService;
    }

    public final void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }
}
