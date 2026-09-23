package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that creates a new CIRCABC Header.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this endpoint
 * handles the HTTP {@code POST} method for the Header resource. The request must carry a
 * JSON body describing the header to create, containing:
 *
 * <ul>
 *   <li>{@code name} &ndash; the (mandatory, non-empty) name of the header;</li>
 *   <li>{@code description} &ndash; a (mandatory) JSON object holding the i18n
 *       (multilingual) description of the header.</li>
 * </ul>
 *
 * <p>Only CIRCABC administrators or Alfresco administrators are allowed to create a
 * header; any other caller results in an {@link AccessDeniedException}. On success the
 * newly created {@link Header} is returned under the {@code header} key of the model map
 * and its backing node is registered with the {@link CircabcService}.
 *
 * @see CircabcDeclarativeWebScript
 * @see HeadersApi
 */
public class HeaderPost extends CircabcDeclarativeWebScript {

  /** API facade providing the header business operations (creation, lookup, etc.). */
  @Autowired
  private HeadersApi headerApi;

  /** Service used to verify that the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Core CIRCABC service used to register the header node once it has been created. */
  @Autowired
  private CircabcService circabcService;

  /**
   * Injects the {@link CircabcService} dependency.
   *
   * @param circabcService the CIRCABC service to use for header node registration
   */
  public void setCircabcService(CircabcService circabcService) {
    this.circabcService = circabcService;
  }

  /**
   * Handles the POST request that creates a new header.
   *
   * <p>Validates that a request body is present and that the caller is a CIRCABC or
   * Alfresco administrator, parses the JSON payload, checks the mandatory {@code name}
   * and {@code description} fields and delegates the actual creation to
   * {@link #putModel(Status, String, JSONObject)}.
   *
   * @param req the incoming web script request carrying the JSON POST body
   * @param status the response status object, updated to reflect the outcome
   * @param cache the response cache directives
   * @return a model map containing the created header under the {@code header} key, or
   *     {@code null} when an error status (e.g. forbidden or conflict) has been set
   * @throws WebScriptException if the POST body is missing, cannot be parsed or read, or
   *     if a mandatory field is missing
   * @throws AccessDeniedException if the current user is not allowed to create a header
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
        "Missing POST body."
      );
    }

    JSONObject json;
    try {
      if (
        !this.currentUserPermissionCheckerService.isCircabcAdmin() &&
        !this.currentUserPermissionCheckerService.isAlfrescoAdmin()
      ) {
        throw new AccessDeniedException(
          "Not enough rights for craeting a header"
        );
      }

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

      return putModel(status, name, description);
    } catch (JSONException jErr) {
      logger.error(ERROR_OCCURRED, jErr);
      throw new WebScriptException(
        Status.STATUS_BAD_REQUEST,
        "Unable to parse JSON POST body: " + jErr.getMessage()
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
   * Builds the {@link Header} domain object, persists it through the {@link HeadersApi}
   * and prepares the response model.
   *
   * <p>Regardless of the outcome, if a header was created its node reference is
   * registered with the {@link CircabcService} in the {@code finally} block.
   *
   * @param status the response status object, updated to {@code FORBIDDEN} or
   *     {@code CONFLICT} when the corresponding error occurs
   * @param name the mandatory name of the header to create
   * @param description a JSON object holding the i18n description of the header
   * @return a model map containing the created header under the {@code header} key, or
   *     {@code null} if access was denied or a duplicate name was detected
   * @throws JSONException if the description JSON cannot be converted to an i18n property
   */
  private Map<String, Object> putModel(
    Status status,
    String name,
    JSONObject description
  ) throws JSONException {
    Header header = null;
    try {
      Header body = new Header();
      body.setName(name);
      body.setDescription(Converter.toI18NProperty(description));

      header = this.headerApi.postHeader(body);

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
    } finally {
      if (header != null) {
        circabcService.addHeaderNode(
          Converter.createNodeRefFromId(header.getId())
        );
      }
    }
  }
}
