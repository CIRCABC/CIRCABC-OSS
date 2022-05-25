package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HeadersApi;
import io.swagger.model.Header;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HeaderPost extends CircabcDeclarativeWebScript {

    private HeadersApi headerApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Content c = req.getContent();
        if (c == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing POST body.");
        }

        JSONObject json;
        try {

            if (!this.currentUserPermissionCheckerService.isCircabcAdmin()
                    && !this.currentUserPermissionCheckerService.isAlfrescoAdmin()) {
                throw new AccessDeniedException("Not enough rights for craeting a header");
            }

            json = new JSONObject(c.getContent());
            String name = json.getString("name");
            JSONObject description = json.getJSONObject("description");

            if ((name == null) || name.isEmpty()) {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Name not specified");
            }

            if (description == null) {
                throw new WebScriptException(
                        HttpServletResponse.SC_BAD_REQUEST, "Description not specified");
            }

            return putModel(status, name, description);
        } catch (JSONException jErr) {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST, "Unable to parse JSON POST body: " + jErr.getMessage());
        } catch (IOException ioErr) {
            throw new WebScriptException(
                    Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }
    }

    private Map<String, Object> putModel(Status status, String name, JSONObject description) throws JSONException {
        try {

            Header body = new Header();
            body.setName(name);
            body.setDescription(Converter.toI18NProperty(description));

            Header header = this.headerApi.postHeader(body);

            Map<String, Object> model = new HashMap<>(7, 1.0f);
            model.put("header", header);
            return model;
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (DuplicateChildNodeNameException dce) {
            status.setCode(HttpServletResponse.SC_CONFLICT);
            status.setMessage(dce.getMessage());
            status.setRedirect(true);
            return null;
        }
    }

    public HeadersApi getHeaderApi() {
        return this.headerApi;
    }

    public void setHeaderApi(HeadersApi headerApi) {
        this.headerApi = headerApi;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return this.currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
