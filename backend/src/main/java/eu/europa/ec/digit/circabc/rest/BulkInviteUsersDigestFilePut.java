package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.BulkImportUserData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script backing the HTTP {@code PUT} endpoint that
 * validates a bulk-invitation file for a given Interest Group.
 *
 * <p>The client uploads a multipart request containing a single file field
 * named {@code fileData}. The endpoint reads the uploaded file, parses it into
 * a list of {@link BulkImportUserData} entries (a "digest" of the users to be
 * invited) and returns that list so the caller can review it before performing
 * the actual bulk invitation.</p>
 *
 * <p>Expected inputs:</p>
 * <ul>
 *   <li>{@code igId} request parameter &ndash; the identifier of the target
 *       Interest Group node; must be non-empty and the current user must hold
 *       Alfresco read permission on it.</li>
 *   <li>A multipart body with exactly one form field named {@code fileData}
 *       that is a file.</li>
 * </ul>
 *
 * <p>The response model exposes the parsed data under the {@code userData}
 * key. On failure the endpoint sets an appropriate HTTP status:
 * {@code 400 Bad Request} for an invalid node reference,
 * {@code 403 Forbidden} when the user lacks read permission, and
 * {@code 406 Not Acceptable} for any other unexpected error.</p>
 */
public class BulkInviteUsersDigestFilePut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    BulkInviteUsersDigestFilePut.class
  );

  /**
   * API providing the user-related business operations, including parsing the
   * uploaded bulk-invitation file into {@link BulkImportUserData} entries.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the current user holds the required Alfresco
   * permissions on the target Interest Group node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request: validates the {@code igId} parameter and
   * the current user's read permission, reads the single uploaded
   * {@code fileData} field, and delegates to
   * {@link UsersApi#bulkInviteUsersDigestFile(String, InputStream, String)} to
   * produce the digest of users to be invited.
   *
   * @param req the web script request; expects an {@code igId} parameter and a
   *            multipart body with a single file field named {@code fileData}
   * @param status the response status, updated with an error code and message
   *               when the request cannot be processed
   * @param cache the cache directives for the response
   * @return a model map containing the parsed {@link BulkImportUserData} list
   *         under the {@code userData} key, or {@code null} when an error
   *         occurs (in which case {@code status} carries the error details)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    MLPropertyInterceptor.setMLAware(false);

    try {
      String igId = req.getParameter("igId");

      if ((igId == null) || igId.trim().isEmpty()) {
        throw new IllegalArgumentException("'igId' cannot be empty.");
      }

      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          igId
        )
      ) {
        throw new AccessDeniedException("No access on node: " + igId);
      }

      FormData form = (FormData) req.parseContent();

      if (form == null) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      if (form.getFields().length != 1) {
        throw new IllegalArgumentException("Wrong number of parameters.");
      }

      InputStream inputStream = null;
      String fileName = null;

      for (FormData.FormField field : form.getFields()) {
        if (field.getName().equals("fileData") && field.getIsFile()) {
          inputStream = field.getInputStream();
          fileName = field.getFilename();
        } else {
          throw new IllegalArgumentException("Incorrect file parameter.");
        }
      }

      List<BulkImportUserData> userData =
        this.usersApi.bulkInviteUsersDigestFile(igId, inputStream, fileName);

      model.put("userData", userData);
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference when bulk inviting users", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when bulk inviting users", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when bulk inviting users", e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
