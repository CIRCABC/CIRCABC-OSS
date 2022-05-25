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
package eu.cec.digit.circabc.web.wai.dialog.keywords;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.app.context.ICircabcContextListener;
import eu.cec.digit.circabc.web.app.context.UICircabcContextService;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;


/**
 * Bean that backs the "Set Keyword to a document" WAI Dialog.
 *
 * @author Yanick Pignot
 */

public class SetKeywordsDialog extends SelectKeywordsBaseDialog implements ICircabcContextListener {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "SetKeywordsDialog";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + "setKeywordsDialogWai";
    private static final long serialVersionUID = -2381287486464195753L;
    /**
     * Logger (coppepa: logger must be final)
     */
    private static final Log logger = LogFactory.getLog(SetKeywordsDialog.class);
    private Node interestGroup = null;

    public SetKeywordsDialog() {
        super();
        UICircabcContextService.getInstance(FacesContext.getCurrentInstance())
                .registerBean(this);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final List<Keyword> docKeywordsAsList = new ArrayList<>(settedKeywords.size());
        docKeywordsAsList.addAll(settedKeywords.values());
        String info = "Trying to set the keywords to the current document : "
                + "\n   Document: " + getActionNode().getName() + "(" + getActionNode().getNodeRef() + ")"
                + "\n   IG      : " + getInterestGroup().getName() + "(" + getInterestGroup().getNodeRef()
                + ")"
                + "\n   Keywords: " + docKeywordsAsList + "(" + settedKeywords.keySet() + ")";
        logRecord.setInfo(info);
        logRecord.setService("Library");
        logRecord.setActivity("Add keyword to document");

        if (logger.isDebugEnabled()) {
            logger.debug(info);
        }

        getKeywordsService().setKeywordsToNode(getActionNode().getNodeRef(), docKeywordsAsList);

        if (logger.isDebugEnabled()) {
            logger.debug("Keywords successfully updated.");
        }

        // refresh the edit content properties dialog
        Beans.getEditNodePropertiesDialog().setPropetyDefinedOutside(DocumentModel.PROP_KEYWORD);

        return outcome;
    }

    public String getDialogCloseAndLaunchAction() {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR + WAI_DIALOG_CALL;
    }

    @Override
    public String getBrowserTitle() {
        return translate("set_document_keyword_dialog_browser_title");
    }

    @Override
    public String getPageIconAltText() {
        return translate("set_document_keyword_dialog_icon_tooltip");
    }

    @Override
    public Node getInterestGroup() {
        if (this.interestGroup == null) {
            interestGroup = new Node(
                    getManagementService().getCurrentInterestGroup(getActionNode().getNodeRef()));
        }
        return this.interestGroup;
    }

    @Override
    public void circabcLeaved() {
        // not used

    }

    @Override
    public void circabcEntered() {
        // not used

    }

    @Override
    public void categoryHeaderChanged() {
        // not used

    }

    @Override
    public void categoryChanged() {
        // not used

    }

    @Override
    public void igRootChanged() {
        this.interestGroup = null;

    }

    @Override
    public void igServiceChanged() {
        // not used

    }
}
