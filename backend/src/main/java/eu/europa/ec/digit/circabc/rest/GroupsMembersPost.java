package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

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
 * Alfresco Web Script endpoint handling the HTTP {@code POST} request that adds one or more
 * members to an Interest Group (IG).
 *
 * <p>The endpoint invites users into an IG by associating each supplied {@link User} with a
 * {@link Profile}, optionally notifying administrators and/or the invited users. The target IG is
 * identified by the {@code igId} path template variable, which is resolved to a
 * {@link NodeRef} in the workspace/SpacesStore.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li><b>Path variable</b> {@code igId} &ndash; the identifier of the group node to add members to.</li>
 *   <li><b>Request parameter</b> {@code language} &ndash; optional locale used to control multilingual
 *       (ML) property handling and the content/UI locale.</li>
 *   <li><b>Request parameter</b> {@code expirationDate} &ndash; optional membership expiration date.</li>
 *   <li><b>Request body</b> &ndash; a JSON payload describing the memberships to create along with
 *       notification flags and an optional notification text (see {@link MembershipPostDefinition}).</li>
 * </ul>
 *
 * <p>The caller must hold the {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the target
 * group; otherwise the request is rejected with an {@code HTTP 403 Forbidden}. Malformed node
 * references yield {@code HTTP 400}, and invalid request bodies or expiration dates yield
 * {@code HTTP 406}.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see GroupsApi#groupsIdMembersPost(NodeRef, MembershipPostDefinition)
 */
public class GroupsMembersPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersPost.class);

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
  private static final String NOTIFY_TEXT = "notifyText";

  /**
   * API providing the group membership business operations invoked by this endpoint.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the required directory permissions before
   * members are added.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds a {@link Profile} from its JSON representation.
   *
   * @param profileObject the JSON object holding the profile's {@code id}, {@code name} and
   *                      {@code groupName} fields
   * @return a {@link Profile} populated from the supplied JSON object
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
   * @param userObject the JSON object holding the user's {@code userId}, {@code email},
   *                   {@code firstname} and {@code lastname} fields
   * @return a {@link User} populated from the supplied JSON object
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
   * Handles the {@code POST} request that adds members to an Interest Group.
   *
   * <p>Resolves the target group from the {@code igId} path variable, configures the multilingual
   * (ML) handling based on the optional {@code language} request parameter, verifies that the
   * current user has the {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission, parses the
   * request body and delegates the actual membership creation to
   * {@link GroupsApi#groupsIdMembersPost(NodeRef, MembershipPostDefinition)}. An optional
   * {@code expirationDate} request parameter is applied to the parsed body when present.</p>
   *
   * <p>On failure, the response {@link Status} is set accordingly and {@code null} is returned:
   * {@code 403} when the user lacks permission, {@code 400} for an invalid node reference and
   * {@code 406} for a malformed body or expiration date. The previous ML-aware state is always
   * restored before returning.</p>
   *
   * @param req    the incoming web script request, providing the path variables, parameters and body
   * @param status the response status to populate on error
   * @param cache  the response cache directives
   * @return an empty model map on success, or {@code null} when an error status has been set
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
          "Not enough rights for inviting a user(s)"
        );
      }
      MembershipPostDefinition body = this.parseBodyJSON(req);
      NodeRef groupNodeRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        id
      );

      if (req.getParameter("expirationDate") != null) {
        body.setExpirationDate(
          Converter.convertStringToDate(req.getParameter("expirationDate"))
        );
      }

      this.groupsApi.groupsIdMembersPost(groupNodeRef, body);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage("Bad body");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (java.text.ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage("Bad expiration date");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Parses the JSON request body into a {@link MembershipPostDefinition}.
   *
   * <p>Reads the {@code adminNotifications} and {@code userNotifications} boolean flags (defaulting
   * to {@code false} when absent), the optional {@code notifyText} (defaulting to an empty string)
   * and the {@code memberships} array, converting each entry into a {@link UserProfile} pairing a
   * {@link User} with a {@link Profile}.</p>
   *
   * @param req the web script request whose content holds the JSON payload
   * @return the parsed {@link MembershipPostDefinition}
   * @throws IOException    if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  private MembershipPostDefinition parseBodyJSON(WebScriptRequest req)
    throws IOException, ParseException {
    MembershipPostDefinition body = new MembershipPostDefinition();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);
    body.setAdminNotifications(
      json.get(GroupsMembersPost.ADMIN_NOTIFICATIONS) != null &&
        Boolean.valueOf(json.get(ADMIN_NOTIFICATIONS).toString())
    );
    body.setUserNotifications(
      json.get(GroupsMembersPost.USER_NOTIFICATIONS) != null &&
        Boolean.valueOf(json.get(USER_NOTIFICATIONS).toString())
    );

    body.setNotifyText(
      (json.get(GroupsMembersPost.NOTIFY_TEXT) == null)
        ? ""
        : json.get(GroupsMembersPost.NOTIFY_TEXT).toString()
    );

    JSONArray memberships = (JSONArray) json.get(MEMBERSHIPS);
    for (Object membership1 : memberships) {
      JSONObject membership = (JSONObject) membership1;
      JSONObject user = (JSONObject) membership.get(USER);
      JSONObject profile = (JSONObject) membership.get(PROFILE);

      UserProfile memberTmp = new UserProfile();
      memberTmp.setUser(parseUserJSON(user));
      memberTmp.setProfile(GroupsMembersPost.parseProfileJSON(profile));

      body.getMemberships().add(memberTmp);
    }

    return body;
  }
}
