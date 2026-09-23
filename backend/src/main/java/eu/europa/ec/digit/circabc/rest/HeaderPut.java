package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HeadersApi;
import io.swagger.model.Header;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code PUT} endpoint used to
 * update an existing CIRCABC Header.
 *
 * <p>The endpoint expects the target Header identifier as the {@code id}
 * template variable in the request URL and a JSON request body describing the
 * new Header state. The body must contain:
 *
 * <ul>
 *   <li>{@code name} - the (non-empty) Header name;</li>
 *   <li>{@code description} - a JSON object holding the internationalized
 *       (i18n) description property values.</li>
 * </ul>
 *
 * <p>Only users identified as CIRCABC administrators or Alfresco administrators
 * are allowed to modify a Header; any other caller is rejected with an access
 * denied error. On success the updated {@link Header} is returned in the model
 * under the {@code header} key.
 */
public class HeaderPut extends CircabcDeclarativeWebScript {

  /** API façade providing the Header business operations (e.g. update). */
  private HeadersApi headerApi;

  /**
   * Service used to check the permissions of the currently authenticated user,
   * in particular whether they hold CIRCABC or Alfresco administrator rights.
   */
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates an existing Header.
   *
   * <p>Validates the presence of the request body, enforces that the caller has
   * administrator rights, parses and validates the {@code name} and
   * {@code description} JSON fields, and delegates the update to
   * {@link #putModel(Status, String, String, JSONObject)}.
   *
   * @param req the web script request; carries the PUT body and the {@code id}
   *     template variable identifying the Header to update
   * @param status the web script response status, updated to reflect the
   *     outcome of the operation
   * @param cache the web script cache directives for the response
   * @return a model map containing the updated {@link Header} under the
   *     {@code header} key, or {@code null} when an error status (e.g. forbidden
   *     or conflict) has been set on the response
   * @throws WebScriptException if the body is missing, cannot be read, contains
   *     invalid JSON, or is missing the required {@code name}/{@code description}
   *     fields
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Content c = req.getContent();
    if (c == null) {
      throw new WebScriptException(
        Status.STATUS_BAD_REQUEST,
        "Missing PUT body."
      );
    }

    JSONObject json;
    try {
      if (
        !this.currentUserPermissionCheckerService.isCircabcAdmin() &&
        !this.currentUserPermissionCheckerService.isAlfrescoAdmin()
      ) {
        throw new AccessDeniedException(
          "Not enough rights to form modifing a header"
        );
      }

      Map<String, String> templateVars = req
        .getServiceMatch()
        .getTemplateVars();
      String id = templateVars.get("id");

      json = new JSONObject(c.getContent());

      String name = json.getString("name");
      JSONObject description = json.getJSONObject("description");

      if ((name == null) || name.isEmpty()) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Name not specified"
        );
      }

      if (description == null) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Description not specified"
        );
      }

      return putModel(status, id, name, description);
    } catch (JSONException jsonErr) {
      logger.error(ERROR_OCCURRED, jsonErr);
      throw new WebScriptException(
        Status.STATUS_BAD_REQUEST,
        "Unable to parse JSON POST body: " + jsonErr.getMessage()
      );
    } catch (IOException ioErr) {
      logger.error(ERROR_OCCURRED, ioErr);
      throw new WebScriptException(
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Unable to retrieve POST body: " + ioErr.getMessage()
      );
    }
  }

  /**
   * Builds the response model by applying the update to the target Header.
   *
   * <p>Creates a {@link Header} from the supplied name and internationalized
   * description and persists it through {@link HeadersApi#putHeader(String,
   * Header)}. Access and naming conflicts are translated into the appropriate
   * HTTP status on the response rather than propagated as exceptions.
   *
   * @param status the web script response status, updated to
   *     {@link Status#STATUS_FORBIDDEN} on access denial or
   *     {@link Status#STATUS_CONFLICT} on a duplicate name
   * @param id the identifier of the Header to update
   * @param name the new Header name
   * @param description the JSON object holding the i18n description values
   * @return a model map containing the updated {@link Header} under the
   *     {@code header} key, or {@code null} if an error status was set
   * @throws JSONException if the description cannot be converted to an i18n
   *     property
   */
  private Map<String, Object> putModel(
    Status status,
    String id,
    String name,
    JSONObject description
  ) throws JSONException {
    try {
      Header body = new Header();
      body.setName(name);
      body.setDescription(Converter.toI18NProperty(description));

      Header header = this.headerApi.putHeader(id, body);
      Map<String, Object> model = new HashMap<>(7, 1.0f);
      model.put("header", header);
      return model;
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (DuplicateChildNodeNameException dce) {
      logger.error(ERROR_OCCURRED, dce);
      status.setCode(Status.STATUS_CONFLICT);
      status.setMessage(dce.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    }
  }

  /**
   * Returns the Header API façade used by this endpoint.
   *
   * @return the configured {@link HeadersApi}
   */
  public HeadersApi getHeaderApi() {
    return this.headerApi;
  }

  /**
   * Sets the Header API façade (injected by the Spring container).
   *
   * @param headerApi the {@link HeadersApi} to use
   */
  public void setHeaderApi(HeadersApi headerApi) {
    this.headerApi = headerApi;
  }

  /**
   * Returns the service used to check the current user's permissions.
   *
   * @return the configured {@link CurrentUserPermissionCheckerService}
   */
  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return this.currentUserPermissionCheckerService;
  }

  /**
   * Sets the service used to check the current user's permissions (injected by
   * the Spring container).
   *
   * @param currentUserPermissionCheckerService the
   *     {@link CurrentUserPermissionCheckerService} to use
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
