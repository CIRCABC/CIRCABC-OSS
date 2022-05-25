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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.web.app.context.IContextListener;

import java.io.Serializable;
import java.util.Map;

/**
 * Base interface of each bean that want to back a navigation part of the Circabc WAI webclient
 *
 * @author yanick pignot
 */
public interface WaiNavigator extends IContextListener, Serializable {

    /**
     * Initialises the WaiNavigator bean
     *
     * @param parameters Map of parameters for the dialog
     */
    void init(Map<String, String> parameters);

    /**
     * Initialises the parameters bean to apply
     *
     * @param parameters Map of parameters
     */
    void setParamsToApply(Map<String, String> parameters);

    /**
     * Initialises the WaiNavigator bean especially for a circa displayer
     *
     * @return whether the content of the displayer must be displayed or not
     * @see eu.cec.digit.circabc.web.ui.component.UIDisplayer
     */
    boolean getInit();

    /**
     * @return the unique node type managed by the implementation of the bean
     */
    NavigableNodeType getManagedNodeType();

    /**
     * @return the unique jsp that must be backed by the implementation of the bean
     */
    String getRelatedJsp();

    /**
     * @return the translated I18N message for the the title of the page
     */
    String getPageTitle();

    /**
     * @return the translated I18N message for the the description of the page
     */
    String getPageDescription();

    /**
     * @return the icon near the title of the page if required or null
     */
    String getPageIcon();


    /**
     * @return the alt text attribute of the icon
     */
    String getPageIconAltText();

    /**
     * @return the main title that should be displayed in the top of the browser.
     */
    String getBrowserTitle();

    /**
     * @return the number of element that contains the lists
     */
    int getListPageSize();

    /**
     * Called when the dialog is restored after a nested dialog is closed
     */
    void restored();


    /**
     * @return the Http url of the current node. Null if not applicable.
     */
    String getHttpUrl();

    /**
     * @return the Http url of the current node. Null if not applicable.
     */
    String getWebdavUrl();

    /**
     * @return the Http url of the previous node. Null if not applicable.
     */
    String getPreviousHttpUrl();

}
