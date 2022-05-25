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

import eu.cec.digit.circabc.repo.admin.debug.ServerConfigurationServiceImpl;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.ui.common.component.debug.BaseDebugComponent;
import org.dom4j.DocumentException;

import javax.faces.context.FacesContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Component which displays the Circabc property files (in a static way)
 *
 * @author Yanick Pignot
 */
public class UIConfigFileReader extends BaseDebugComponent {

    private static final File[] PROPERTY_FILES_TO_READ =
            {
                    ServerConfigurationServiceImpl.CIRCABC_VERSION_PROPS,
                    ServerConfigurationServiceImpl.ALFRESCO_REPO_PROPS,
                    ServerConfigurationServiceImpl.ALFRESCO_CACHE_STATEGIES,
                    ServerConfigurationServiceImpl.ALFRESCO_TRANSACTION_PROPS,
                    ServerConfigurationServiceImpl.ALFRESCO_EMAIN_CONFIGURATION,
                    ServerConfigurationServiceImpl.CIRCABC_SHARED_REPO_PROPS,
                    ServerConfigurationServiceImpl.CIRCABC_REPO_PROPS,
                    ServerConfigurationServiceImpl.CIRCABC_SETTINGS,
                    ServerConfigurationServiceImpl.CIRCABC_BUILD_CONF,
                    ServerConfigurationServiceImpl.CIRCABC_SHARED_HIBERNATE_CONFIG,
                    ServerConfigurationServiceImpl.CIRCABC_QUARTZ_PROPS,
                    ServerConfigurationServiceImpl.CIRCABC_DB_POOL,
                    ServerConfigurationServiceImpl.CIRCABC_RMI,
                    ServerConfigurationServiceImpl.CIRCABC_SHARED_PROPS,
                    ServerConfigurationServiceImpl.CIRCABC_FILE_SERVER_CONF,
                    ServerConfigurationServiceImpl.ECAS_PROPS
            };
    private transient ServerConfigurationService serverConfigurationService;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.debug.ConfigFileReader";
    }

    /**
     * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
     */
    @SuppressWarnings("unchecked")
    public Map getDebugData() {
        final Map properties = new LinkedHashMap();

        for (final File file : PROPERTY_FILES_TO_READ) {
            final String displayPath = "<b><i>" + file.getPath() + "</i></b>";

            try {
                // if it will be not the case, the key will be overriden ...
                properties.put(displayPath, "<b><font color=\"blue\">Successfully read</font></b>");

                putAllKeyAsc(properties, getServerConfigurationService().getConfigurationFileResume(file));
            } catch (FileNotFoundException ex) {
                properties.put(displayPath, "<b><font color=\"green\">Not exists</font></b>");
            } catch (IOException | DocumentException ex) {
                properties.put(displayPath,
                        "<b><font color=\"red\">Can't be opened due to: " + ex.getMessage() + "</font></b>");
            }
        }

        return properties;
    }


    protected void putAllKeyAsc(final Map<String, String> destination,
                                final Map<String, String> toSort) {
        final Set<String> keys = toSort.keySet();
        final List<String> keyAsList = new ArrayList<>(keys.size());

        keyAsList.addAll(keys);
        Collections.sort(keyAsList);

        for (String key : keyAsList) {
            if (destination.containsKey(key)) {
                destination.put(key + " [override]", toSort.get(key));
            } else {
                destination.put(key, toSort.get(key));
            }

        }
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
