package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.model.NotifiableUser;
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
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint handling the HTTP {@code PUT} request that updates
 * an existing space (folder) node in the CIRCABC repository.
 *
 * <p>The endpoint expects the target space identifier as the {@code id} template
 * variable in the URL. The request body is a JSON document describing the new
 * state of the space: its {@code name}, multilingual {@code title} and
 * {@code description} maps (keyed by supported language codes), and an optional
 * {@code properties} object that may carry an {@code expiration_date}.
 *
 * <p>Behaviour of {@link #executeImpl(WebScriptRequest, Status, Cache)}:
 * <ul>
 *   <li>Honours the optional {@code language} request parameter to toggle the
 *       multilingual ({@code MLPropertyInterceptor}) awareness and content
 *       locale used while reading/writing properties.</li>
 *   <li>Verifies that the current user holds Alfresco write permission on the
 *       target node before applying any change.</li>
 *   <li>Applies the update via {@link SpacesApi#spacesIdPut(String, Node)} and
 *       returns the refreshed node under the {@code node} model key.</li>
 *   <li>When the optional {@code notify} request parameter is not {@code false}
 *       (default {@code true}), sends an edit notification to the users
 *       subscribed to the node.</li>
 * </ul>
 *
 * <p>Errors are mapped to appropriate HTTP status codes (403 for access denied,
 * 400 for invalid references/types, 409 for duplicate names, 500 for parsing or
 * unexpected errors).
 */
public class SpacePut extends CircabcDeclarativeWebScript {

  /** JSON key for the space name. */
  private static final String SPACE_NAME = "name";
  /** JSON key for the multilingual space title map. */
  private static final String SPACE_TITLE = "title";
  /** JSON key for the multilingual space description map. */
  private static final String SPACE_DESCRIPTION = "description";
  /** JSON key for the optional properties object. */
  private static final String PROPERTIES = "properties";
  /** JSON key (within {@link #PROPERTIES}) for the space expiration date. */
  private static final String EXPIRATION_DATE = "expiration_date";

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacePut.class);

  /** API used to apply the space update in the repository. */
  @Autowired
  private SpacesApi spacesApi;

  /** API used to fetch the refreshed node representation after the update. */
  @Autowired
  private NodesApi nodesApi;

  /** Service used to check that the current user may write to the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Service used to send edit notifications to subscribed users. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to resolve the set of users subscribed to the target node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Parses the JSON request body into a {@link Node} describing the desired
   * space state.
   *
   * <p>Reads the {@code name}, the multilingual {@code title} and
   * {@code description} maps (keeping only the supported language codes) and,
   * when present, the {@code expiration_date} property from the optional
   * {@code properties} object.
   *
   * @param req the webscript request whose content is the JSON body to parse
   * @return a {@link Node} populated with the parsed name, titles, descriptions
   *         and properties
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
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
    if (properties != null && properties.containsKey(EXPIRATION_DATE)) {
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

    return body;
  }

  /**
   * Handles the {@code PUT} request that updates the space identified by the
   * {@code id} URL template variable.
   *
   * <p>The method configures multilingual awareness/locale based on the optional
   * {@code language} request parameter, checks that the current user has write
   * permission on the target node, parses the JSON body into a {@link Node},
   * applies the update through {@link SpacesApi#spacesIdPut(String, Node)} and,
   * unless the {@code notify} request parameter is {@code false}, notifies the
   * subscribed users. The original multilingual awareness flag is always
   * restored before returning.
   *
   * @param req the webscript request, providing the {@code id} template
   *            variable, the JSON body and the {@code language}/{@code notify}
   *            parameters
   * @param status the response status, updated with the appropriate HTTP code
   *               and message when an error occurs
   * @param cache the response cache directives (unused)
   * @return a model map containing the updated {@code node}, or {@code null}
   *         when an error occurred and the status has been set accordingly
   */
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
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      boolean notify = true;
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
      logger.error("Access denied for space with id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for space with id: " + id, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (DuplicateChildNodeNameException dcnne) {
      logger.error("Duplicate child node name for space with id: " + id, dcnne);
      status.setCode(Status.STATUS_CONFLICT);
      status.setMessage("Duplicate node name");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error("Error parsing JSON for space with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid type exception for space with id: " + id, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error occurred for space with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Unexpected error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
