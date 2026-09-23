package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.PagedNodes;
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
 * lists the children of a "restricted" space (folder) identified by its node id.
 *
 * <p>The endpoint returns a paged collection of child nodes for the given space.
 * Callers may control pagination, sorting and node-type filtering through the
 * following request parameters:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) &mdash; the identifier of the space
 *       whose children are requested.</li>
 *   <li>{@code language} (query) &mdash; optional locale code. When supplied, the
 *       content and UI locale are set accordingly and multilingual (ML) awareness
 *       is disabled so that values are resolved for that specific locale; when
 *       omitted, ML awareness is enabled so multilingual properties are returned
 *       as-is.</li>
 *   <li>{@code page} (query) &mdash; optional 0-based/1-based page number; a value
 *       of {@code -1}, empty or missing means "no paging".</li>
 *   <li>{@code limit} (query) &mdash; optional page size; a value of {@code -1},
 *       empty or missing means "no limit".</li>
 *   <li>{@code order} (query) &mdash; optional sort specification.</li>
 *   <li>{@code folderOnly} (query) &mdash; when {@code "true"}, restricts the
 *       result to folder nodes only.</li>
 *   <li>{@code fileOnly} (query) &mdash; when {@code "true"}, restricts the result
 *       to file (content) nodes only.</li>
 * </ul>
 *
 * <p>On success the response model contains the {@code data} (list of child nodes)
 * and {@code total} (overall number of children) entries. Errors are reported via
 * the web script {@link Status}: {@code 403} for {@link AccessDeniedException},
 * {@code 400} for {@link InvalidNodeRefException}, and {@code 500} for any other
 * unexpected failure.</p>
 *
 * <p>The actual retrieval logic is delegated to
 * {@link SpacesApi#restrictedSpaceGetChildren(String, int, int, String, boolean, boolean)}.</p>
 */
public class RestrictedSpacesIdGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(RestrictedSpacesIdGet.class);

  /**
   * Business API used to fetch the paged children of a restricted space.
   * Injected by Spring.
   */
  @Autowired
  private SpacesApi spacesApi;

  /**
   * Handles the {@code GET} request: resolves the target space from the {@code id}
   * URL template variable, applies the locale / multilingual settings, parses the
   * pagination, sorting and filtering parameters, and delegates to
   * {@link SpacesApi#restrictedSpaceGetChildren(String, int, int, String, boolean, boolean)}
   * to obtain the paged children.
   *
   * <p>Multilingual awareness is toggled based on the {@code language} parameter and
   * always restored to its previous value in the {@code finally} block.</p>
   *
   * <p>When an error occurs, the appropriate HTTP status is set on {@code status}
   * (403, 400 or 500), a redirect is requested and {@code null} is returned so that
   * the framework renders the status response instead of the success template.</p>
   *
   * @param req    the web script request; supplies the {@code id} template variable
   *               and the {@code language}, {@code page}, {@code limit}, {@code order},
   *               {@code folderOnly} and {@code fileOnly} query parameters
   * @param status the web script status, updated with an error code, message and
   *               redirect flag when the request cannot be served
   * @param cache  the response cache control (unused)
   * @return a model map containing {@code data} (the child nodes) and {@code total}
   *         (the total number of children) on success, or {@code null} when an error
   *         status has been set
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

    String page = req.getParameter("page");
    String limit = req.getParameter("limit");

    String sort = req.getParameter("order");

    String folderOnlyParam = req.getParameter("folderOnly");
    boolean folderOnly = "true".equals(folderOnlyParam);

    String fileOnlyParam = req.getParameter("fileOnly");
    boolean fileOnly = "true".equals(fileOnlyParam);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    try {
      int pageNumber = (page == null || page.equals("-1") || page.isEmpty())
        ? -1
        : Integer.parseInt(page);
      int limitNumber = (limit == null || limit.equals("-1") || limit.isEmpty())
        ? -1
        : Integer.parseInt(limit);

      PagedNodes pagedNodes = this.spacesApi.restrictedSpaceGetChildren(
        id,
        pageNumber,
        limitNumber,
        sort,
        folderOnly,
        fileOnly
      );

      model.put("data", pagedNodes.getData());
      model.put("total", pagedNodes.getTotal());
    } catch (AccessDeniedException e) {
      logger.error(
        "Access denied when trying to get children of space " + id,
        e
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for space " + id, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting children of space " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
