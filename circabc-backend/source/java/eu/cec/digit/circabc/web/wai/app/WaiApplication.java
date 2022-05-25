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
package eu.cec.digit.circabc.web.wai.app;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.manager.DialogManager;
import eu.cec.digit.circabc.web.wai.manager.NavigationManager;
import eu.cec.digit.circabc.web.wai.manager.WizardManager;
import org.alfresco.web.app.servlet.FacesHelper;

import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Util class to manage the WAI web client.
 *
 * @author yanick pignot
 * @see eu.cec.digit.circabc.web.wai.manager.NavigationManager
 * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator
 */
public class WaiApplication implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3400082766466164726L;

    public static NavigationManager getNavigationManager() {
        return (NavigationManager) FacesHelper
                .getManagedBean(FacesContext.getCurrentInstance(), NavigationManager.BEAN_NAME);
    }

    public static void setNavigationManager(NavigableNodeType type) {
        setNavigationManager(type.getBeanName());
    }

    public static void setNavigationManager(String beanName) {
        getNavigationManager().initNavigation(beanName);
    }

    public static DialogManager getDialogManager() {
        return (DialogManager) FacesHelper
                .getManagedBean(FacesContext.getCurrentInstance(), DialogManager.BEAN_NAME);
    }

    public static WizardManager getWizardManager() {
        return (WizardManager) FacesHelper
                .getManagedBean(FacesContext.getCurrentInstance(), WizardManager.BEAN_NAME);
    }

}
