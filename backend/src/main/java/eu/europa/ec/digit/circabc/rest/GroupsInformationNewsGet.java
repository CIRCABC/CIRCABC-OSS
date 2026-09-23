package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.InformationApi;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint that handles the HTTP {@code GET} request for
 * retrieving the list of news items published in the Information service of an
 * Interest Group (IG).
 *
 * <p>The endpoint resolves the {@code Information} container of the IG
 * identified by the {@code igId} URL template variable, verifies that the
 * current user holds at least the {@link InformationPermissions#INFACCESS}
 * permission on it, and then delegates to {@link InformationApi} to fetch a
 * paginated collection of news entries.</p>
 *
 * <p>Supported request inputs:</p>
 * <ul>
 *   <li>{@code igId} (URL template variable) &ndash; the identifier of the
 *       Interest Group whose news are requested.</li>
 *   <li>{@code page} (query parameter, optional) &ndash; the 1-based page
 *       number; converted internally to a 0-based index. Defaults to the first
 *       page.</li>
 *   <li>{@code limit} (query parameter, optional) &ndash; the maximum number of
 *       news items to return per page. Defaults to
 *       {@link #DEFAULT_NUMBER_RESULTS}.</li>
 *   <li>{@code language} (query parameter, optional) &ndash; the locale used to
 *       render multilingual content. When omitted, the response is
 *       multilingual-aware; when supplied, content is resolved for the given
 *       locale.</li>
 * </ul>
 *
 * <p>The resulting news collection is placed in the response model under the
 * {@code news} key for the associated FreeMarker template to render as JSON.</p>
 */
public class GroupsInformationNewsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsInformationNewsGet.class);

  /** Default 0-based page index used when no {@code page} parameter is supplied. */
  private static final int START_PAGE = 0;

  /** Default number of news items returned when no {@code limit} parameter is supplied. */
  private static final int DEFAULT_NUMBER_RESULTS = 25;

  /** API providing the Information service business operations, including news retrieval. */
  @Autowired
  private InformationApi informationApi;

  /** Alfresco node service used to resolve the Information container of the Interest Group. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user holds the required Information permission. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the news retrieval logic for the requested Interest Group.
   *
   * <p>Reads the {@code igId} template variable together with the optional
   * {@code page}, {@code limit} and {@code language} request parameters,
   * resolves the IG's {@code Information} container, enforces the
   * {@link InformationPermissions#INFACCESS} permission, and populates the
   * model with the paginated news list under the {@code news} key.</p>
   *
   * @param req the incoming web script request; provides the {@code igId}
   *            template variable and the {@code page}, {@code limit} and
   *            {@code language} query parameters
   * @param status the response status used to signal error conditions (for
   *               example {@code 403 Forbidden} on access denial or
   *               {@code 400 Bad Request} on an invalid node reference)
   * @param cache the cache directives for the response
   * @return a model map containing the {@code news} collection, or
   *         {@code null} when an error status is set and a redirect response is
   *         returned
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

    String page = req.getParameter("page");
    int nbPage = START_PAGE;
    if (page != null) {
      nbPage = ((Integer.parseInt(page) == 0)
        ? 0
        : (Integer.parseInt(page) - 1));
    }

    String limit = req.getParameter("limit");
    int nbLimit = DEFAULT_NUMBER_RESULTS;
    if (limit != null) {
      nbLimit = Integer.parseInt(limit);
    }

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
      NodeRef infRef = this.nodeService.getChildByName(
        Converter.createNodeRefFromId(id),
        ContentModel.ASSOC_CONTAINS,
        "Information"
      );

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
          infRef.getId(),
          InformationPermissions.INFACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Not enought permissions to get the list of News"
        );
      }

      model.put(
        "news",
        this.informationApi.groupsIdInformationNewsGet(id, nbLimit, nbPage)
      );
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
