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
package eu.cec.digit.circabc.myfaces.application.jsp;

import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.jsp.JspStateManagerImpl;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * Circabc StateManager implementation for use when views are defined via tags in JSP pages. We need
 * other action when a state is not found in session
 *
 * @author Guillaume
 */
public class CircabcJspStateManagerImpl extends JspStateManagerImpl {

    private static final Log logger = LogFactory.getLog(CircabcJspStateManagerImpl.class);

    /**
     * The circabc management service
     */
    private ManagementService managementService;

    public CircabcJspStateManagerImpl() {
        if (logger.isTraceEnabled()) {
            logger.trace("New CircabcJspStateManagerImpl instance created");
        }
    }

    @Override
    public UIViewRoot restoreView(final FacesContext facescontext, final String viewId,
                                  final String renderKitId) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering restoreView");
        }

        Object obj = facescontext.getExternalContext().getSessionMap()
                .get(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);

        if (obj != null && obj instanceof Boolean && (Boolean) obj) {
            facescontext.getExternalContext().getSessionMap().put(
                    AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.FALSE);
            return null;
        }

        UIViewRoot uiViewRoot = null;

        try {
            uiViewRoot = restoreTreeStructure(facescontext, viewId, renderKitId);
        } catch (FacesException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage() + " -> viewId: " + viewId + ", renderKitId: " + renderKitId);
            }
            return null;
        }

        if (uiViewRoot != null) {
            uiViewRoot.setViewId(viewId);
            restoreComponentState(facescontext, uiViewRoot, renderKitId);
            final String restoredViewId = uiViewRoot.getViewId();
            if (restoredViewId == null || !(restoredViewId.equals(viewId))) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Exiting restoreView - restored view is null.");
                }
                return null;
            }
        } else {
            //We permit only one other outter access - Original login page for alfresco user
            if (logger.isInfoEnabled()) {
                logger.info("Alfresco login page wanted");
            }
            return null;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Exiting restoreView");
        }

        return uiViewRoot;
    }


    /**
     * @return the managementService
     */
    public ManagementService getManagementService() {
        if (managementService == null) {
            managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
        }

        return managementService;
    }

}
