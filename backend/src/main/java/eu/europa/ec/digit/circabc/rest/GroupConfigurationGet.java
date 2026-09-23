package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that returns the
 * configuration of an Interest Group.
 *
 * <p>The Interest Group is identified by the {@code id} template variable taken from the request
 * URL. When present, the current user's read permission on that node is verified before the group
 * configuration is retrieved through {@link GroupsApi#getInterestGroupConfiguration(String)} and
 * exposed to the response template under the {@code configuration} model key.
 *
 * <p>An optional {@code language} request parameter controls how multilingual (ML) properties are
 * resolved: when it is omitted the script stays ML-aware and returns the full multilingual values;
 * when it is provided the corresponding {@link Locale} is applied so that properties are resolved
 * for that single language. The previous ML-aware state is always restored once processing
 * completes.
 *
 * <p>Error handling maps repository failures to HTTP status codes: an {@link AccessDeniedException}
 * results in {@code 403 Forbidden} and an {@link InvalidNodeRefException} results in
 * {@code 400 Bad Request}.
 */
public class GroupConfigurationGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupConfigurationGet.class);

  /** API used to retrieve the Interest Group configuration. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's Alfresco read permission on the target group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the Interest Group
   * configuration.
   *
   * <p>The group is identified by the {@code id} template variable. If it is provided, the current
   * user's read permission is checked and, on success, the group configuration is placed in the
   * model under the {@code configuration} key. The optional {@code language} request parameter
   * toggles multilingual property resolution as described in the class documentation. In case of a
   * permission or invalid-node error, the appropriate HTTP status is set on {@code status}, a
   * redirect is requested and {@code null} is returned.
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *     {@code language} parameter
   * @param status the response status, updated to {@code 403} or {@code 400} when an error occurs
   * @param cache the cache directives for the response
   * @return a model map containing the {@code configuration} entry, or {@code null} when an error
   *     response (with redirect) has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupIp = templateVars.get("id");

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
      if (groupIp != null) {
        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            groupIp
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot access group, not enough permission"
          );
        }

        model.put(
          "configuration",
          this.groupsApi.getInterestGroupConfiguration(groupIp)
        );
      }
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
