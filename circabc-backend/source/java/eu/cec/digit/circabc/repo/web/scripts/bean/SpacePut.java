package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
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

public class SpacePut extends CircabcDeclarativeWebScript {

  public static final String SPACE_NAME = "name";
  public static final String SPACE_TITLE = "title";
  public static final String SPACE_DESCRIPTION = "description";
  public static final String PROPERTIES = "properties";
  private static final String EXPIRATION_DATE = "expiration_date";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacePut.class);

  private SpacesApi spacesApi;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * @param req
   * @return
   * @throws IOException
   * @throws ParseException
   * @throws JSONException
   */
  private static Node parseSpaceJSON(WebScriptRequest req)
    throws IOException, ParseException {
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

    JSONObject properties = (JSONObject) json.get(PROPERTIES);
    if (properties != null) {
      if (properties.containsKey(EXPIRATION_DATE)) {
        Object expirationDate = properties.get(EXPIRATION_DATE);
        if (expirationDate == null) {
          body.getProperties().put(EXPIRATION_DATE, null);
        } else {
          body
            .getProperties()
            .put(
              EXPIRATION_DATE,
              String.valueOf(properties.get(EXPIRATION_DATE))
            );
        }
      }
    }

    return body;
  }

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
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
      Boolean notify = true;
      String notifyString = req.getParameter("notify");
      if (notifyString != null) {
        notify = Boolean.parseBoolean(notifyString);
      }
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)
      ) {
        throw new AccessDeniedException(
          "Cannot update the space, not enough permissions"
        );
      }

      //We first Need to update the space and then notify the user(s), not the other way round!
      Node body = parseSpaceJSON(req);
      this.spacesApi.spacesIdPut(id, body);
      model.put("node", this.nodesApi.getNodeById(id));

      //Do we need to notify users?
      if (notify) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        Set<NotifiableUser> users =
          notificationSubscriptionService.getNotifiableUsers(nodeRef);
        final List<NodeRef> nodeRefs = new ArrayList<>();

        nodeRefs.add(nodeRef);

        notificationService.notifyNewFiles(
          nodeRef,
          nodeRefs,
          users,
          MailTemplate.NOTIFY_EDIT_BULK
        );
      }
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
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * @param notificationService the notificationService to set
   */
  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * @param notificationSubscriptionService the notificationSubscriptionService to
   *                                        set
   */
  public void setNotificationSubscriptionService(
    NotificationSubscriptionService notificationSubscriptionService
  ) {
    this.notificationSubscriptionService = notificationSubscriptionService;
  }
}
