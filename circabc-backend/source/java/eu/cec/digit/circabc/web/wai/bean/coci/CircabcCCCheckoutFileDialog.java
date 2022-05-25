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
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Date;
import java.util.Map;

/**
 * @author Yanick Pignot
 */
public class CircabcCCCheckoutFileDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcCCCheckoutFileDialog";
    private static final long serialVersionUID = 3909601469295466806L;
    private static final String MSG_SUCCESS = "checkout_action_success";
    private static final String MSG_FAILURE = "checkout_action_failure";


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException(
                        "The node id is a madatory parameter that should be passed either via the Wizard/DialogManager.setupParameters(ActionEvent event). Use the actionListener tag of the action component.");
            }

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
        return null;
    }

    public String getBrowserTitle() {
        return null;
    }

    public String getPageIconAltText() {
        return null;
    }

    /**
     * Action called upon completion of the Check Out file page without going into dialog
     *
     * @param event The action event
     * @return outcome Depends on calling page
     */
    public void checkoutFileDirect(ActionEvent event) {
        // setup the action
        init(((UIActionLink) event.getComponent()).getParameterMap());

        LogRecord logRecord = new LogRecord();
        logRecord.setActivity("Checkout");
        logRecord.setDate(new Date());
        logRecord.addInfo("Checking out the file: ");
        logRecord.addInfo(
                getNodeService().getProperty(getActionNode().getNodeRef(), ContentModel.PROP_NAME)
                        .toString());
        logRecord.setService("Library");
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        logRecord.setDocumentID((Long) getNodeService()
                .getProperty(getActionNode().getNodeRef(), ContentModel.PROP_NODE_DBID));

        Path path = getNodeService().getPath(getActionNode().getNodeRef());
        String displayPath = PathUtils.getCircabcPath(path, true);
        displayPath = displayPath.endsWith("contains") ? displayPath
                .substring(0, displayPath.length() - "contains".length()) : displayPath;
        logRecord.setPath(displayPath);

        NodeRef igRef = getManagementService().getCurrentInterestGroup(getActionNode().getNodeRef());
        logRecord.setIgID((Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID));
        logRecord.setIgName(getNodeService().getProperty(igRef, ContentModel.PROP_NAME).toString());

        try {

            getCociContentBusinessSrv().checkOut(getActionNode().getNodeRef());
            logRecord.setOK(true);
            // refresh and click
            getBrowseBean().refreshBrowsing();

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(MSG_SUCCESS, getActionNode().getName()));
            getLogService().log(logRecord);

        } catch (final BusinessStackError e) {
            logRecord.setOK(false);
            for (final String err : e.getI18NMessages()) {
                Utils.addErrorMessage(err);
            }
            getLogService().log(logRecord);
        } catch (final Throwable t) {
            logRecord.setOK(false);
            Utils.addErrorMessage(translate(MSG_FAILURE, getActionNode().getName(), t.getMessage()));
            getLogService().log(logRecord);
        }


    }


    /**
     * @return the cociContentBusinessSrv
     */
    protected final CociContentBusinessSrv getCociContentBusinessSrv() {
        return getBusinessRegistry().getCociContentBusinessSrv();
    }
}
