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
package eu.cec.digit.circabc.web.servlet;

import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Retrieves the list of translations of the given documentId
 *
 * @author schwerr
 */
public class TranslationsJSONServlet extends BaseServlet {

    private static final Log logger = LogFactory.getLog(TranslationsJSONServlet.class);

    private static final long serialVersionUID = -3169831645378335485L;

    private AuthenticationService authenticationService = null;
    private MultilingualContentService multilingualContentService = null;
    private NodeService nodeService = null;
    private PermissionService permissionService = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {

        super.init();

        authenticationService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getAuthenticationService();
        nodeService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getNodeService();
        multilingualContentService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getMultilingualContentService();
        permissionService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getPermissionService();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Authenticate the current user
//		try {
//			String ticket = authenticationService.getCurrentTicket();
//			authenticationService.validate(ticket);
//		} 
//		catch (AuthenticationException e) {
//			logger.error("Error authenticating user.", e);
//			resp.getWriter().write("");
//			return;
//		}

        AuthenticationStatus status =
                AuthenticationHelper.authenticate(getServletContext(), req,
                        resp, false);

        if (status == AuthenticationStatus.Failure) {
            logger.error("Error authenticating user: Failure");
            resp.getWriter().write("");
            return;
        }

        // Get the document id to retrieve translations for
        String documentId = req.getParameter("documentId");

        NodeRef documentNodeRef = new NodeRef(documentId);

        // Check permission to read the node
        if (permissionService.hasPermission(documentNodeRef,
                PermissionService.READ_CONTENT) == AccessStatus.DENIED) {
            logger.error("User '" + authenticationService.getCurrentUserName() +
                    "' has no access to the content of document: " + documentId);
            resp.getWriter().write("");
            return;
        }

        List<Map<String, Object>> options = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();

        // Check if translated
        if (!nodeService.hasAspect(documentNodeRef,
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            logger.info("Document has no translations.");
            resp.setContentType(MimetypeMap.MIMETYPE_JSON);
            try {
                jsonObject.put("options", options);
            } catch (JSONException e) {
                logger.error("Error parsing JSON", e);
                resp.getWriter().write(e.getMessage());
                return;
            }
            resp.getWriter().write(jsonObject.toString());
            return;
        }

        // Get translations and build JSON response
        Map<Locale, NodeRef> translations =
                multilingualContentService.getTranslations(documentNodeRef);

        for (Map.Entry<Locale, NodeRef> translation : translations.entrySet()) {
            Map<String, Object> option = new HashMap<>();
            option.put("documentId", translation.getValue().getId());
            option.put("language", translation.getKey().getDisplayLanguage().
                    substring(0, 1).toUpperCase() +
                    translation.getKey().getDisplayLanguage().substring(1));
            options.add(option);
        }

        try {
            jsonObject.put("options", options);
        } catch (JSONException e) {
            logger.error("Error parsing JSON", e);
            resp.getWriter().write(e.getMessage());
            return;
        }

        resp.setContentType(MimetypeMap.MIMETYPE_JSON);
        resp.getWriter().write(jsonObject.toString());
    }
}
