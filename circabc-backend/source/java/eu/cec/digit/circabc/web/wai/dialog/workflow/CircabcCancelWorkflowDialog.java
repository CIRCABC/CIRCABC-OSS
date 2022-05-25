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
package eu.cec.digit.circabc.web.wai.dialog.workflow;

import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.bean.workflow.CancelWorkflowDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.Map;

public class CircabcCancelWorkflowDialog extends CancelWorkflowDialog implements
        WaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -2537349657938922452L;
    private static final Log logger = LogFactory.getLog(CircabcCancelWorkflowDialog.class);
    private transient LogService logService;
    private LogRecord logRecord;
    private transient ManagementService managementService;
    private transient MultilingualContentService multilingualContentService;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        CircabcNavigationBean navigator = Beans.getWaiNavigator();
        logRecord = new LogRecord();
        if (navigator != null) {
            logRecord.setService("Workflow");
            logRecord.setActivity("Cancel");
            updateLogDocument(navigator.getCurrentNode().getNodeRef(), logRecord);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Can not get current node when try to cancel workflow");
            }
        }
    }

    private void updateLogDocument(final NodeRef nodeRef, final LogRecord logRecord) {

        logRecord
                .setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
        if (igNodeRef != null) {

            logRecord
                    .setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        } else {
            final NodeRef categoryNodeRef = getManagementService().getCurrentCategory(nodeRef);
            if (categoryNodeRef != null) {
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NAME));
            } else {
                final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));

            }
        }
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        final Path path;
        if (getNodeService().getType(nodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            //tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            final NodeRef tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            path = getNodeService().getPath(tempNodeRef);

        } else {
            path = getNodeService().getPath(nodeRef);
        }

        logRecord.setPath(PathUtils.getCircabcPath(path, true));
        logRecord.addInfo(this.getWorkflowInstance().id);
    }


    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        logRecord.setOK(true);
        getLogService().log(logRecord);
        return super.doPostCommitProcessing(context, outcome);
    }

    protected String getErrorOutcome(Throwable exception) {
        logRecord.setOK(false);
        getLogService().log(logRecord);
        return super.getErrorOutcome(exception);

    }

    @Override
    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionsListWrapper getActionList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCancelButtonVisible() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isFormProvided() {
        // TODO Auto-generated method stub
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
    public final void setLogService(final LogService logService) {
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

    /**
     * @return the multilingualContentService
     */
    public MultilingualContentService getMultilingualContentService() {
        if (multilingualContentService == null) {
            multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
        }
        return multilingualContentService;
    }

}
