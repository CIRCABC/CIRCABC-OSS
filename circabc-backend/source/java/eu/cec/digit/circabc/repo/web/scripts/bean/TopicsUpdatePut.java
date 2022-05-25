package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Update a topic
 *
 * @author schwerr
 */
public class TopicsUpdatePut extends CircabcDeclarativeWebScript {

    public static final String TOPIC_NAME = "name";
    public static final String TOPIC_TITLE = "title";
    public static final String TOPIC_DESCRIPTION = "description";
    public static final String TOPIC_PROPERTIES = "properties";
    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(TopicsUpdatePut.class);

    private TopicsApi topicsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    /**
     * @param req
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    private static Node parseForumJSON(WebScriptRequest req) throws IOException, ParseException {
        Node body = new Node();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        body.setName(String.valueOf(json.get(TOPIC_NAME)));

        Object descriptions = json.get(TOPIC_DESCRIPTION);

        if (descriptions instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) descriptions).containsKey(code)) {
                    body.getDescription().put(code, String.valueOf(((JSONObject) descriptions).get(code)));
                }
            }
        }

        Object titles = json.get(TOPIC_TITLE);

        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Map<String, String> properties = (Map<String, String>) json.get(TOPIC_PROPERTIES);

        body.getProperties().put("security_ranking", properties.get("security_ranking"));
        body.getProperties().put("expiration_date", properties.get("expiration_date"));

        return body;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)) {
                throw new AccessDeniedException("Cannot update the topic, not enough permissions");
            }

            Node body = parseForumJSON(req);

            this.topicsApi.updateTopic(id, body);

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
        }

        return model;
    }

    /**
     * @param topicsApi the topicsApi to set
     */
    public void setTopicsApi(TopicsApi topicsApi) {
        this.topicsApi = topicsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
