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
package eu.cec.digit.circabc.web.wai.dialog;

import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.web.bean.dialog.IDialogBean;

import java.io.Serializable;


/**
 * Base interface of each bean that want to back a dialog of the Circabc WAI webclient
 *
 * @author yanick pignot
 */
public interface WaiDialog extends IDialogBean, Serializable {

    /**
     * @return the alt text attribute of the icon
     */
    String getPageIconAltText();

    /**
     * @return the main title that should be displayed in the top of the browser.
     */
    String getBrowserTitle();

    /**
     * @return an object that wrap an action list for the right menu
     */
    ActionsListWrapper getActionList();


    /**
     * @return true if the cancel button must be displayed
     */
    boolean isCancelButtonVisible();


    boolean isFormProvided();

}
