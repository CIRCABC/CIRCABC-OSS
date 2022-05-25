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
package eu.cec.digit.circabc.web.wai.dialog.forums;

import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Base bean that back the signal an abuse dialog
 *
 * @author Yanick Pignot
 */
public class SignalPostDialog extends ModerationDialog {

    /**
     *
     */
    private static final long serialVersionUID = -1607630889160963579L;

    private static final String MSG_SIGNALED_SUCCESS = "post_moderation_signal_success";
    private static final String MSG_SIGNALED_FAILURE = "post_moderation_signal_failure";

    String message;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            message = null;
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        try {
            signalAbuse(message);
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_SIGNALED_SUCCESS));

            if (message != null) {
                logRecord.setInfo(message);
            }

            return outcome;
        } catch (final Exception e) {
            Utils.addErrorMessage(translate(MSG_SIGNALED_FAILURE, e.getMessage()), e);

            isFinished = false;
            return null;
        }
    }

    public String getBrowserTitle() {
        return translate("moderation_signal_abuse_dialog_page_title");
    }

    public String getPageIconAltText() {
        return translate("moderation_signal_abuse_dialog_icon_tooltip");
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(String message) {
        this.message = message;
    }


}
