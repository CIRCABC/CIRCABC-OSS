package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * retrieves the group associated with a given repository node.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention:
 * {@code NodesGroupGet} maps to a {@code GET} request whose URL carries the
 * node identifier as the {@code id} template variable (e.g.
 * {@code .../nodes/{id}/group}). It delegates the actual lookup to
 * {@link io.swagger.api.NodesApi#nodesIdGroupGet(String)} and exposes the
 * result to the FreeMarker response template under the {@code "g"} model key.</p>
 *
 * <p>Key behavior:</p>
 * <ul>
 *   <li><b>Inputs:</b> the {@code id} URL template variable identifying the
 *       node, and an optional {@code language} request parameter controlling
 *       localization of multilingual (ML) properties.</li>
 *   <li><b>Authorization:</b> the current user must hold Alfresco read
 *       permission on the node; otherwise an {@code AccessDeniedException} is
 *       raised and the response is set to {@code 403 Forbidden}.</li>
 *   <li><b>Localization:</b> when {@code language} is absent the interceptor is
 *       left ML-aware so raw multilingual values are returned; when supplied,
 *       the content and UI locale are set accordingly and ML awareness is
 *       disabled so localized values are resolved.</li>
 *   <li><b>Error mapping:</b> invalid node references yield
 *       {@code 400 Bad Request} and any other failure yields
 *       {@code 500 Internal Server Error}.</li>
 * </ul>
 */
public class NodesGroupGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesGroupGet.class);

  /** API facade providing node-related operations, including group lookup. */
  @Autowired
  private NodesApi nodesApi;

  /** Service used to verify that the current user may read the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request by resolving the group linked to the node
   * identified by the {@code id} URL template variable.
   *
   * <p>The optional {@code language} request parameter drives localization of
   * multilingual properties: when omitted the interceptor stays ML-aware, and
   * when present the content/UI locale is applied with ML awareness disabled.
   * The original ML-awareness state is always restored before returning.</p>
   *
   * @param req the incoming web script request; supplies the {@code id}
   *            template variable and the optional {@code language} parameter
   * @param status the response status holder, updated to {@code 403},
   *               {@code 400} or {@code 500} when the request cannot be served
   * @param cache the response cache directives (unused by this endpoint)
   * @return a model map containing the resolved group under the {@code "g"}
   *         key on success, or {@code null} when an error occurs and the status
   *         has been set to a redirecting error code
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
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Impossible to access this information. No access on the node"
        );
      }

      model.put("g", this.nodesApi.nodesIdGroupGet(id));
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied: " + ade.getMessage(), ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference: " + inre.getMessage(), inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error: " + e.getMessage(), e);
      }
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
