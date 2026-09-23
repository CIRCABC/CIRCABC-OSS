package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.UsersApi;
import io.swagger.exception.AlreadyExistsException;
import io.swagger.model.User;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that creates a new CIRCABC user.
 *
 * <p>This endpoint is invoked via an HTTP {@code POST} (as implied by the
 * {@code CreateUser} class name / mutating nature) and expects a JSON request
 * body describing the user to create. The recognised body properties are:
 * <ul>
 *   <li>{@code currentIgId} - optional Interest Group id used to authorise the
 *       operation for a directory (group) manager;</li>
 *   <li>{@code userId} - the login/identifier of the new user;</li>
 *   <li>{@code firstname}, {@code lastname}, {@code email}, {@code phone} -
 *       core user attributes;</li>
 *   <li>{@code title}, {@code companyId}, {@code fax}, {@code urlAddress},
 *       {@code postalAddress}, {@code description}, {@code password} -
 *       additional user properties stored on the created account.</li>
 * </ul>
 *
 * <p>Before creating the user the endpoint verifies that the current caller is
 * authorised (see {@link #hasCurrentUserPermissionToCreateUser(Object)}). On
 * success it delegates the creation to {@link UsersApi#usersPost(User)} and
 * returns a model containing the created {@link User} under the key
 * {@code "user"}.
 *
 * <p>Error handling:
 * <ul>
 *   <li>{@link AlreadyExistsException} results in HTTP {@code 409 Conflict};</li>
 *   <li>a malformed or unreadable body ({@link IOException} /
 *       {@link ParseException}) results in HTTP {@code 406 Not Acceptable};</li>
 *   <li>insufficient permissions raise an
 *       {@link AccessDeniedException}.</li>
 * </ul>
 *
 * @see CircabcDeclarativeWebScript
 * @see UsersApi
 */
public class CreateUser extends CircabcDeclarativeWebScript {

  /** API facade used to persist the new user in the repository. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to check the current caller's permissions before creating a user. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Parses the JSON request body, validates the caller's permissions and
   * creates the described user.
   *
   * @param req    the web script request; its body must be a JSON object
   *               containing the user attributes described at class level
   * @param status the response status, updated to {@code 409} on conflict or
   *               {@code 406} on a bad/unparseable body
   * @param cache  the response cache directives (unused)
   * @return a model map containing the created {@link User} under the key
   *         {@code "user"}, or {@code null} when an error status has been set
   * @throws AccessDeniedException if the current user is not allowed to create
   *                               a user
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    User user;
    try {
      String cBody = req.getContent().getContent();
      JSONParser parser = new JSONParser();
      JSONObject json = (JSONObject) parser.parse(cBody);

      Object currentIgIdObject = json.get("currentIgId");
      if (!hasCurrentUserPermissionToCreateUser(currentIgIdObject)) {
        throw new AccessDeniedException(
          "Not enough rights for creating a user"
        );
      }

      String userId = String.valueOf(json.get("userId"));

      String firstName = String.valueOf(json.get("firstname"));
      String lastName = String.valueOf(json.get("lastname"));
      String email = String.valueOf(json.get("email"));
      String phone = String.valueOf(json.get("phone"));
      String title = String.valueOf(json.get("title"));
      String companyId = String.valueOf(json.get("companyId"));
      String fax = String.valueOf(json.get("fax"));
      String urlAddress = String.valueOf(json.get("urlAddress"));
      String postalAddress = String.valueOf(json.get("postalAddress"));
      String description = String.valueOf(json.get("description"));
      String password = String.valueOf(json.get("password"));

      user = new User();
      Map<String, String> properties = new HashMap<>();

      user.setUserId(userId);
      user.setFirstname(firstName);
      user.setLastname(lastName);
      user.setEmail(email);
      user.setPhone(phone);
      properties.put("title", title);
      properties.put("fax", fax);
      properties.put("urlAddress", urlAddress);
      properties.put("postalAddress", postalAddress);
      properties.put("description", description);
      properties.put("companyId", companyId);
      properties.put("password", password);

      user.setProperties(properties);

      usersApi.usersPost(user);
    } catch (AlreadyExistsException ex) {
      logger.error(ERROR_OCCURRED, ex);
      status.setCode(Status.STATUS_CONFLICT);
      status.setMessage(ex.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException ex) {
      logger.error(ERROR_OCCURRED, ex);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage("Bad body");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    Map<String, Object> model = new HashMap<>(7, 1.0f);
    model.put("user", user);
    return model;
  }

  /**
   * Determines whether the current user may create a user in the given context.
   *
   * <p>CIRCABC and Alfresco administrators are always allowed. Otherwise, when
   * an Interest Group id is supplied the caller must hold the
   * {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on that group;
   * when no group id is supplied the caller must be a directory, category or
   * CIRCABC administrator.
   *
   * @param currentIgIdObject the raw {@code currentIgId} value from the request
   *                          body; may be {@code null} or empty
   * @return {@code true} if the current user is authorised to create a user,
   *         {@code false} otherwise
   */
  private boolean hasCurrentUserPermissionToCreateUser(
    Object currentIgIdObject
  ) {
    if (
      currentUserPermissionCheckerService.isCircabcAdmin() ||
      currentUserPermissionCheckerService.isAlfrescoAdmin()
    ) {
      return true;
    } else {
      String currentIgId = convertToNonEmptyString(currentIgIdObject);
      if (currentIgId != null) {
        return currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          currentIgId,
          DirectoryPermissions.DIRMANAGEMEMBERS
        );
      } else {
        return currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin();
      }
    }
  }

  /**
   * Converts a raw JSON value to its string representation, mapping absent or
   * empty values to {@code null}.
   *
   * @param jsonObject the raw JSON value; may be {@code null}
   * @return the non-empty string representation of {@code jsonObject}, or
   *         {@code null} if the value is {@code null} or an empty string
   */
  private String convertToNonEmptyString(Object jsonObject) {
    if (jsonObject == null) {
      return null;
    }
    String str = String.valueOf(jsonObject);
    if (str.isEmpty()) {
      return null;
    }

    return str;
  }
}
