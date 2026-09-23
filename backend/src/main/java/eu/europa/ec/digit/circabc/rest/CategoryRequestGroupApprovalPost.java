package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupCreationRequestApproval;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint handling the HTTP {@code POST} request used to
 * approve (or reject) a pending group creation request within a category.
 *
 * <p>The endpoint is bound to a URL carrying the category identifier as the
 * {@code id} template variable. It optionally accepts a {@code language}
 * request parameter that controls the content/locale handling: when it is
 * absent the interceptor is set to be ML aware, otherwise the supplied locale
 * is applied and ML awareness is disabled.</p>
 *
 * <p>Only a category administrator is allowed to invoke this endpoint; the
 * permission is enforced through {@link CurrentUserPermissionCheckerService}.
 * The JSON request body is parsed into a {@link GroupCreationRequestApproval}
 * and delegated to {@link CategoriesApi#categoriesIdGroupRequestApprovalPost}
 * on behalf of the currently authenticated user.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see CategoriesApi
 */
public class CategoryRequestGroupApprovalPost
  extends CircabcDeclarativeWebScript
{

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    CategoryRequestGroupApprovalPost.class
  );

  /**
   * API used to delegate the group creation request approval to the business
   * logic layer.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service providing the name of the currently authenticated user, forwarded
   * as the approving actor.
   */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Service used to verify that the caller has category administrator rights
   * before the approval is processed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the group creation request approval POST request.
   *
   * <p>Reads the category identifier from the {@code id} template variable and
   * the optional {@code language} request parameter to configure locale/ML
   * handling. After confirming the caller is a category administrator, it
   * parses the JSON body into a {@link GroupCreationRequestApproval} and hands
   * it over to the {@link CategoriesApi}. The original ML-aware state of the
   * interceptor is always restored before returning.</p>
   *
   * @param req the incoming web script request, providing the {@code id}
   *            template variable, the optional {@code language} parameter and
   *            the JSON request body
   * @param status the response status, updated to {@code 403 FORBIDDEN} when
   *               the caller lacks permission or to {@code 400 BAD REQUEST}
   *               when the request cannot be parsed
   * @param cache the response cache directives (unused by this endpoint)
   * @return the (empty) model map on success, or {@code null} when an error
   *         status and redirect have been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String categoryId = templateVars.get("id");

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
      // verification if the user is category admin

      if (!currentUserPermissionCheckerService.isCategoryAdmin(categoryId)) {
        throw new AccessDeniedException(
          "Impossible to list the category group requests. Not enough permissions"
        );
      }

      // no special permission check required here.
      GroupCreationRequestApproval body =
        InterestGroupJsonParser.parseGroupCreationRequestApproval(req);
      this.categoriesApi.categoriesIdGroupRequestApprovalPost(
        categoryId,
        body,
        authenticationService.getCurrentUserName()
      );
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Bad request", inre);
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
