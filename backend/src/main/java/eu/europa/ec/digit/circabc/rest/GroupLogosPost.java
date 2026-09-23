package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.*;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * REST webscript endpoint that handles the upload of a logo image for a group.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint responds to HTTP
 * {@code POST} requests. It expects a multipart form request whose first file field contains the
 * logo image to associate with the target group. The group is identified by the {@code id}
 * template variable extracted from the request URL.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) — the identifier of the group to upload the logo for.</li>
 *   <li>{@code language} (request parameter, optional) — when provided, the content and UI locale
 *       are set accordingly and multilingual (ML) awareness is disabled; otherwise ML awareness is
 *       enabled so the upload is performed in a language-neutral manner.</li>
 *   <li>Multipart body — the uploaded file field carrying the logo image and its filename.</li>
 * </ul>
 *
 * <p>Only group administrators are allowed to upload a logo; unauthorized requests result in a
 * {@code 403 Forbidden} response, while invalid node references result in a {@code 400 Bad Request}
 * response. On success, the model exposes the uploaded logo(s) under the {@code logos} key.</p>
 */
public class GroupLogosPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupLogosPost.class);

  /** API providing group-related business operations, including logo upload. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user has group administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the logo upload request for the group identified by the {@code id} template variable.
   *
   * <p>Validates that the current user is a group administrator, parses the multipart request,
   * uploads the first file field found as the group logo and places the resulting node(s) in the
   * response model under the {@code logos} key. The multilingual awareness state is adjusted based
   * on the optional {@code language} request parameter and always restored before returning.</p>
   *
   * @param req the web script request, providing the {@code id} template variable, the optional
   *     {@code language} parameter and the multipart form content
   * @param status the response status object, updated to {@code 403} on access denial or {@code 400}
   *     on an invalid node reference
   * @param cache the cache control object for the response
   * @return a model map containing the uploaded logo node(s) under the {@code logos} key on success,
   *     or {@code null} when an error status (forbidden or bad request) has been set
   * @throws IllegalArgumentException if the request is not a multipart request
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("id");

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
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
        throw new AccessDeniedException(
          "Cannot upload a new logo, not enough permissions"
        );
      }

      FormData form = (FormData) req.parseContent();

      if ((form == null) || !form.getIsMultiPart()) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      List<Node> uploadedImages = new ArrayList<>();

      if (groupId != null) {
        for (FormData.FormField field : form.getFields()) {
          if (field.getIsFile()) {
            InputStream inputStream = field.getInputStream();
            uploadedImages.add(
              this.groupsApi.postGroupLogoByGroupId(
                groupId,
                inputStream,
                field.getFilename()
              )
            );
            break;
          }
        }
      }

      model.put("logos", uploadedImages);
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
