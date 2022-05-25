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
package eu.cec.digit.circabc.web.wai.dialog.coci;

import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.coci.CCProperties;
import org.alfresco.web.bean.coci.CCUndoCheckoutFileDialog;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.Date;
import java.util.Map;

/**
 * Bean responsible for the undo checkout dialog
 *
 * @author yanick pignot
 */
public class UndoCheckoutDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "UndoCheckoutDialog";
    private static final long serialVersionUID = 8166875319281692712L;
    private CCUndoCheckoutFileDialog undoCheckoutFileDialog;

    private CCProperties ccProperties;

    private Node lastCurrentNode;

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        // in the restaure mode, the parameters can be null
        if (parameters != null) {
            String id = parameters.get(NODE_ID_PARAMETER);
            // test if the application is in right state (id should be equalt to the current node)
            lastCurrentNode = getNavigator().getCurrentNode();
            if (!lastCurrentNode.getId().equals(id)) {
                // the verfication of id parameter will be perfom here
                getNavigator().setCurrentNodeId(id);
                UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();
            }

            ccProperties = new CCProperties();
            ccProperties.setDocument(getActionNode());

            getUndoCheckoutFileDialog().setProperty(ccProperties);

            logRecord.setService(super.getServiceFromActionNode());
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (getActionNode() != null) {
            NodeRef locked = null;

            if (getNavigator().getCurrentNodeType().equals(NavigableNodeType.LIBRARY_CONTENT) ||
                    getNavigator().getCurrentNodeType().equals(NavigableNodeType.INFORMATION_CONTENT)) {
                // the dialog has to be called via the document details page
                locked = getLockedDocument();
            }

            logRecord.setActivity("Undo checkout");
            logRecord.setOK(true);
            logRecord.setDate(new Date());
            logRecord.setInfo("Working copy cancelled from: " + locked.toString());
            logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
            NodeRef currentInterestGroup = getManagementService().getCurrentInterestGroup(locked);
            logRecord.setIgID(
                    (Long) getNodeService().getProperty(currentInterestGroup, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName(
                    (String) getNodeService().getProperty(currentInterestGroup, ContentModel.PROP_NAME));
            Path path = new Node(locked).getNodePath();
            logRecord.setPath(PathUtils.getCircabcPath(path, true));

            final String resultOutcome = undoCheckoutFileDialog
                    .undoCheckoutFile(FacesContext.getCurrentInstance(), "");

            // Handle error
            if (resultOutcome == null) {
                logRecord.setOK(false);

                return null;
            }

            if (wasCurrentnodeAContainer()) {
                // return the the original space
                getBrowseBean().clickWai(lastCurrentNode.getId());
            } else if (locked != null) {
                // don't return to the current node, it is dropped.
                // But to the original one.
                getBrowseBean().clickWai(locked);
            }

        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    public String getBrowserTitle() {
        return translate("title_undocheckout");
    }

    public String getPageIconAltText() {
        return translate("undocheckout_icon_tooltip");
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("undo_checkout");
    }

    @Override
    public String getContainerDescription() {
        return translate("undocheckout_title_desc");
    }

    @Override
    public String getContainerTitle() {
        return translate("undocheckout_title", getActionNode().getName());
    }

    private NodeRef getLockedDocument() {
        return getCheckOutCheckInService().getCheckedOut(getActionNode().getNodeRef());
    }


    public final CCUndoCheckoutFileDialog getUndoCheckoutFileDialog() {
        if (undoCheckoutFileDialog == null) {
            undoCheckoutFileDialog = Beans.getCCUndoCheckoutFileDialog();
        }
        return undoCheckoutFileDialog;
    }

    public final void setUndoCheckoutFileDialog(CCUndoCheckoutFileDialog undoCheckoutFileDialog) {
        this.undoCheckoutFileDialog = undoCheckoutFileDialog;
    }

    public boolean wasCurrentnodeAContainer() {
        final QName modelType = lastCurrentNode.getType();

        // look for Space or File nodes
        if ((ContentModel.TYPE_FOLDER.equals(modelType) || getDictionaryService()
                .isSubClass(modelType, ContentModel.TYPE_CONTAINER))) {
            return true;
        } else {
            return false;
        }
    }
}
