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
package eu.cec.digit.circabc.web.ui.common.component.debug;

import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.ui.common.component.debug.BaseDebugComponent;

import javax.faces.context.FacesContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Component which displays the Server configuration properties
 *
 * @author Yanick Pignot
 */
public class UIServerConfiguration extends BaseDebugComponent {

    private transient ServerConfigurationService serverConfigurationService;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.debug.ServerConfiguration";
    }

    /**
     * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
     */
    @SuppressWarnings("unchecked")
    public Map getDebugData() {
        final Map properties = new LinkedHashMap();

        return properties;
    }

    /**
     * @return the serverConfigurationService
     */
    protected final ServerConfigurationService getServerConfigurationService() {
        if (serverConfigurationService == null) {
            serverConfigurationService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getServerConfigurationService();
        }
        return serverConfigurationService;
    }
}
