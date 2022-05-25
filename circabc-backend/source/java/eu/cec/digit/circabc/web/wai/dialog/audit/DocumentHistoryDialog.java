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
package eu.cec.digit.circabc.web.wai.dialog.audit;

import eu.cec.digit.circabc.repo.log.LogSearchResultDAO;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;

import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;

public class DocumentHistoryDialog extends BaseWaiDialog {

    private static final String CONTAINER_TITLE = "history_dialog_title";
    private static final String CONTAINER_DESC = "history_dialog_description";
    private static final long serialVersionUID = 6196990576366355844L;
    private List<LogSearchResultDAO> auditList;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        Long documentID = (Long) getNodeService()
                .getProperty(getActionNode().getNodeRef(), ContentModel.PROP_NODE_DBID);
        setAuditList(getLogService().getHistory(documentID,getActionNode().getNodeRef().getId()));

    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        // nothing to do
        return null;
    }

    public String getBrowserTitle() {
        return translate("history_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("history_dialog_icon_tooltip");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    @Override
    public String getContainerDescription() {
        return translate(CONTAINER_DESC, getActionNode().getName());
    }

    @Override
    public String getContainerTitle() {
        return translate(CONTAINER_TITLE);
    }

    /**
     * @return the auditList
     */
    public List<LogSearchResultDAO> getAuditList() {
        return auditList;
    }

    /**
     * @param auditList the auditList to set
     */
    public void setAuditList(List<LogSearchResultDAO> auditList) {
        this.auditList = auditList;
    }


}
