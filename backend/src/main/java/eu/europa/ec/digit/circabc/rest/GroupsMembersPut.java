package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.GroupsApi;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint handling the HTTP {@code PUT} request for the
 * memberships of an Interest Group.
 *
 * <p>The endpoint updates (adds or modifies) one or more user memberships
 * within the group identified by the {@code igId} URL template variable. Each
 * membership associates a {@link io.swagger.model.User} with a
 * {@link io.swagger.model.Profile} inside the group.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code igId} &mdash; URL template variable identifying the target
 *       Interest Group node.</li>
 *   <li>{@code language} &mdash; optional request parameter selecting the
 *       content/UI locale; when absent the multilingual (ML) aware mode is
 *       enabled instead.</li>
 *   <li>{@code expirationDate} &mdash; optional request parameter setting the
 *       membership expiration date.</li>
 *   <li>Request body &mdash; a JSON document describing the notification flags
 *       ({@code adminNotifications}, {@code userNotifications}) and the list of
 *       {@code memberships} (each with a {@code user} and a {@code profile}).</li>
 * </ul>
 *
 * <p>The caller must hold the
 * {@link io.swagger.model.permissions.DirectoryPermissions#DIRMANAGEMEMBERS}
 * permission on the group; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden} status. Malformed input results in an HTTP
 * {@code 400 Bad Request} or {@code 406 Not Acceptable} status.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see io.swagger.api.GroupsApi#groupsIdMembersPut(NodeRef, MembershipPostDefinition)
 */
public class GroupsMembersPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersPut.class);

  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String GROUP_NAME = "groupName";
  private static final String LASTNAME = "lastname";
  private static final String FIRSTNAME = "firstname";
  private static final String EMAIL = "email";
  private static final String USER_ID = "userId";
  private static final String USER = "user";
  private static final String PROFILE = "profile";
  private static final String MEMBERSHIPS = "memberships";
  private static final String ADMIN_NOTIFICATIONS = "adminNotifications";
  private static final String USER_NOTIFICATIONS = "userNotifications";
  private static final String BAD_BODY = "Bad body";

  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the required directory
   * permissions before any membership modification is applied.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds a {@link Profile} from its JSON representation.
   *
   * @param profileObject the JSON object describing the profile; expected to
   *     contain the {@code id}, {@code name} and {@code groupName} keys
   * @return the populated {@link Profile} instance
   */
  private static Profile parseProfileJSON(JSONObject profileObject) {
    Profile result = new Profile();
    result.setId(String.valueOf(profileObject.get(ID)));
    result.setName(String.valueOf(profileObject.get(NAME)));
    result.setGroupName(String.valueOf(profileObject.get(GROUP_NAME)));

    return result;
  }

  /**
   * Builds a {@link User} from its JSON representation.
   *
   * @param userObject the JSON object describing the user; expected to contain
   *     the {@code userId}, {@code email}, {@code firstname} and
   *     {@code lastname} keys
   * @return the populated {@link User} instance
   */
  private static User parseUserJSON(JSONObject userObject) {
    User result = new User();
    result.setUserId(String.valueOf(userObject.get(USER_ID)));
    result.setEmail(String.valueOf(userObject.get(EMAIL)));
    result.setFirstname(String.valueOf(userObject.get(FIRSTNAME)));
    result.setLastname(String.valueOf(userObject.get(LASTNAME)));

    return result;
  }

  /**
   * Handles the web script execution: validates the caller's permissions,
   * parses the request body and applies the membership update to the target
   * Interest Group.
   *
   * <p>Failures are translated into HTTP status codes rather than propagated:
   * insufficient permissions yield {@code 403 Forbidden}, an invalid group
   * reference yields {@code 400 Bad Request}, and an unreadable or malformed
   * body yields {@code 406 Not Acceptable}. The multilingual-aware state is
   * always restored before returning.</p>
   *
   * @param req the incoming web script request; supplies the {@code igId}
   *     template variable, the optional {@code language} and
   *     {@code expirationDate} parameters and the JSON body
   * @param status the response status, updated with an error code and message
   *     when the request cannot be processed
   * @param cache the response cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error status
   *     has been set and a redirect is requested
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

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

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRMANAGEMEMBERS
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for updating a user(s)"
        );
      }
      MembershipPostDefinition body = this.parseBodyJSON(req);
      if (req.getParameter("expirationDate") != null) {
        body.setExpirationDate(
          Converter.convertStringToDate(req.getParameter("expirationDate"))
        );
      }
      NodeRef groupNodeRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        id
      );
      this.groupsApi.groupsIdMembersPut(groupNodeRef, body);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Bad request", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error(BAD_BODY, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(BAD_BODY);
      status.setRedirect(true);
    } catch (java.text.ParseException | ParseException e) {
      logger.error(BAD_BODY, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(BAD_BODY);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Parses the request body into a {@link MembershipPostDefinition}.
   *
   * <p>Reads the {@code adminNotifications} and {@code userNotifications} flags
   * (defaulting to {@code false} when absent) and iterates over the
   * {@code memberships} array, converting each entry's {@code user} and
   * {@code profile} objects into a {@link UserProfile}.</p>
   *
   * @param req the web script request whose content holds the JSON body
   * @return the populated {@link MembershipPostDefinition}
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the content is not valid JSON
   */
  private MembershipPostDefinition parseBodyJSON(WebScriptRequest req)
    throws IOException, ParseException {
    MembershipPostDefinition body = new MembershipPostDefinition();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);
    body.setAdminNotifications(
      json.get(GroupsMembersPut.ADMIN_NOTIFICATIONS) != null &&
        Boolean.valueOf(json.get(ADMIN_NOTIFICATIONS).toString())
    );
    body.setUserNotifications(
      json.get(GroupsMembersPut.USER_NOTIFICATIONS) != null &&
        Boolean.valueOf(json.get(USER_NOTIFICATIONS).toString())
    );

    JSONArray memberships = (JSONArray) json.get(MEMBERSHIPS);
    for (Object membership1 : memberships) {
      JSONObject membership = (JSONObject) membership1;
      JSONObject user = (JSONObject) membership.get(USER);
      JSONObject profile = (JSONObject) membership.get(PROFILE);

      UserProfile memberTmp = new UserProfile();
      memberTmp.setUser(parseUserJSON(user));
      memberTmp.setProfile(GroupsMembersPut.parseProfileJSON(profile));

      body.getMemberships().add(memberTmp);
    }

    return body;
  }
}
