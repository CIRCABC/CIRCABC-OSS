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

import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.web.bean.workflow.ViewCompletedTaskDialog;

public class CircabcViewCompletedTaskDialog extends ViewCompletedTaskDialog
        implements WaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -3820932579605147338L;


    @Override
    public String getCancelButtonLabel() {
        return WebClientHelper.translate("close");
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

}
