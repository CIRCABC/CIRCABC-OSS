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
package eu.cec.digit.circabc.web.wai.dialog.profile;

/**
 * Baked bean for access profiles creation
 *
 * @author Yanick Pignot
 */
public class CreateAccessProfileDialog extends EditAccessProfileDialog {

    private static final long serialVersionUID = -457333397844453605L;

    private static final String MSG_CONTAINER_TITLE = "create_access_profile_dialog_page_title";

    //private static final Log logger = LogFactory.getLog(CreateAccessProfileDialog.class);

    @Override
    public String getBrowserTitle() {
        return translate("create_access_profile_dialog_browser_title");
    }

    @Override
    public String getPageIconAltText() {
        return translate("create_access_profile_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate(MSG_CONTAINER_TITLE);
    }
}
