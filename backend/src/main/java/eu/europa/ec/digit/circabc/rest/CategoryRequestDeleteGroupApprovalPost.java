package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupDeletionRequestApproval;
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
 * Alfresco declarative web script that handles the approval (or rejection) of a
 * pending request to delete an interest group within a given category.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint is
 * bound to the HTTP {@code POST} method. It delegates to
 * {@link CategoriesApi#categoriesIdGroupRequestDeleteApprovalPost(String,
 * GroupDeletionRequestApproval, String)} to record the decision on the group
 * deletion request.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &ndash; the category identifier, supplied as a URL template
 *       variable.</li>
 *   <li>{@code language} &ndash; an optional request parameter used to set the
 *       content/UI locale; when absent the web script operates in
 *       multilingual-aware mode.</li>
 *   <li>The request body, parsed into a {@link GroupDeletionRequestApproval},
 *       carrying the approval decision.</li>
 * </ul>
 *
 * <p>Access is restricted to category administrators; callers lacking the
 * required permission receive an HTTP {@code 403 Forbidden} response, while
 * malformed input results in an HTTP {@code 400 Bad Request} response.</p>
 */
public class CategoryRequestDeleteGroupApprovalPost
  extends CircabcDeclarativeWebScript
{

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    CategoryRequestDeleteGroupApprovalPost.class
  );

  /** API used to approve the group deletion request for the target category. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Provides the user name of the currently authenticated caller. */
  @Autowired
  private AuthenticationService authenticationService;

  /** Verifies whether the current user holds category-administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the POST request that approves a pending interest-group deletion
   * request for the category identified in the URL.
   *
   * <p>The method resolves the {@code id} template variable and the optional
   * {@code language} parameter, adjusts the multilingual/locale context
   * accordingly, checks that the caller is a category administrator, parses the
   * {@link GroupDeletionRequestApproval} body and forwards the approval to the
   * {@link CategoriesApi}. The original multilingual-aware state is always
   * restored before returning.</p>
   *
   * @param req    the incoming web script request, providing the category
   *               {@code id} template variable, the optional {@code language}
   *               parameter and the JSON approval body
   * @param status the response status, set to {@code 403} when the caller is not
   *               a category administrator or {@code 400} when the request is
   *               malformed
   * @param cache  the response cache directives
   * @return an empty model map on success, or {@code null} when the request is
   *         rejected and a redirect status has been set
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
      GroupDeletionRequestApproval body =
        InterestGroupJsonParser.parseGroupDeletionRequestApproval(req);
      this.categoriesApi.categoriesIdGroupRequestDeleteApprovalPost(
        categoryId,
        body,
        authenticationService.getCurrentUserName()
      );
    } catch (AccessDeniedException ade) {
      logger.error("Access denied: ", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Bad request: ", inre);
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
