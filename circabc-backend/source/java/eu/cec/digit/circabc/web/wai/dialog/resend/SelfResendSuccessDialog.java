/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or – as soon they
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


package eu.cec.digit.circabc.web.wai.dialog.resend;

import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

import javax.faces.context.FacesContext;

/**
 * @author yanick pignot
 */
public class SelfResendSuccessDialog extends BaseWaiDialog {

    private static final long serialVersionUID = -685695045385990641L;

    public static String BEAN_NAME = "SelfResendSuccessDialog";

    public SelfResendSuccessDialog() {
        super();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    public String getBrowserTitle() {
        return translate("resend_congratulation_page_title");
    }

    public String getPageIconAltText() {
        return translate("resend_congratulation_page_title");
    }

    @Override
    public boolean isCancelButtonVisible() {
        return false;
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("ok");
    }

}
