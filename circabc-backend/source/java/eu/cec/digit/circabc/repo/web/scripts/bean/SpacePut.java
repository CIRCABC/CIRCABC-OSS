package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpacePut extends CircabcDeclarativeWebScript {

    public static final String SPACE_NAME = "name";
    public static final String SPACE_TITLE = "title";
    public static final String SPACE_DESCRIPTION = "description";
    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(SpacePut.class);

    private SpacesApi spacesApi;
    private NodesApi nodesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    /**
     * @param req
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    private static Node parseSpaceJSON(WebScriptRequest req) throws IOException, ParseException {
        Node body = new Node();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        body.setName(String.valueOf(json.get(SPACE_NAME)));

        JSONObject titles = (JSONObject) json.get(SPACE_TITLE);

        for (String code : SupportedLanguages.availableLangCodes) {
            if (titles.containsKey(code)) {
                body.getTitle().put(code, String.valueOf(titles.get(code)));
            }
        }

        JSONObject description = (JSONObject) json.get(SPACE_DESCRIPTION);

        for (String code : SupportedLanguages.availableLangCodes) {
            if (description.containsKey(code)) {
                body.getDescription().put(code, String.valueOf(description.get(code)));
            }
        }

        return body;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)) {
                throw new AccessDeniedException("Cannot update the space, not enough permissions");
            }

            Node body = parseSpaceJSON(req);
            this.spacesApi.spacesIdPut(id, body);
            model.put("node", this.nodesApi.getNodeById(id));

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } catch (DuplicateChildNodeNameException dcnne) {
            status.setCode(HttpServletResponse.SC_CONFLICT);
            status.setMessage("Duplicate node name");
            status.setRedirect(true);
            return null;
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Error");
            status.setRedirect(true);
            return null;
        } catch (InvalidTypeException ite) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad noderef type");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @return the spacesApi
     */
    public SpacesApi getSpacesApi() {
        return this.spacesApi;
    }

    /**
     * @param spacesApi the spacesApi to set
     */
    public void setSpacesApi(SpacesApi spacesApi) {
        this.spacesApi = spacesApi;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return this.nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
