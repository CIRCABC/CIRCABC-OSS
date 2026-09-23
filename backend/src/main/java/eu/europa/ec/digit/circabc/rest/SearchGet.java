package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SearchApi;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Date;
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
 * REST webscript endpoint that performs a search over the CIRCABC repository.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint is
 * bound to the HTTP {@code GET} method. It reads its criteria from request
 * query parameters, delegates the actual search to {@link SearchApi} and
 * returns a paged list of matching nodes rendered by the associated FreeMarker
 * template. The returned model exposes:
 * <ul>
 *   <li>{@code data} &ndash; the list of matching nodes for the requested page;</li>
 *   <li>{@code total} &ndash; the total number of results across all pages.</li>
 * </ul>
 *
 * <p>Key request parameters include:
 * <ul>
 *   <li>{@code q} &ndash; the free-text query string;</li>
 *   <li>{@code node} &ndash; the node reference that scopes the search (read
 *       access on this node is required);</li>
 *   <li>{@code language} &ndash; optional content locale; when omitted the
 *       search runs in multilingual (ML aware) mode;</li>
 *   <li>{@code page} / {@code limit} &ndash; pagination controls;</li>
 *   <li>{@code searchFor} (All, Name, Title, Content),
 *       {@code searchIn} (All, Library, Forums, Information, Agenda),
 *       {@code creator}, {@code keywords} (comma separated);</li>
 *   <li>{@code creationDateFrom} / {@code creationDateTo} and
 *       {@code modifiedDateFrom} / {@code modifiedDateTo} date ranges;</li>
 *   <li>{@code status}, {@code securityRanking}, {@code version};</li>
 *   <li>{@code dynAttr1}..{@code dynAttr20} &ndash; up to 20 dynamic property
 *       criteria;</li>
 *   <li>{@code sort} and {@code order} (asc/desc) for result ordering.</li>
 * </ul>
 *
 * <p>Access is enforced through {@link CurrentUserPermissionCheckerService};
 * missing read permission yields {@code 403 Forbidden}, invalid node references
 * or empty queries yield {@code 400 Bad Request}, and unexpected failures yield
 * {@code 500 Internal Server Error}.
 */
public class SearchGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SearchGet.class);

  /** Default (zero-based) index of the first result page. */
  private static final int START_PAGE = 0;

  /** Default maximum number of results returned when no {@code limit} is supplied. */
  private static final int DEFAULT_NUMBER_RESULTS = 250;

  /** Business service that executes the search against the repository. */
  @Autowired
  private SearchApi searchApi;

  /** Service used to verify that the current user may read the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} search request.
   *
   * <p>Extracts and parses the search criteria from the request query
   * parameters, checks that the current user has read permission on the target
   * node, delegates the search to {@link SearchApi#searchGet}, and populates the
   * response model with the paged results. The multilingual awareness of the
   * property interceptor is toggled based on the presence of the
   * {@code language} parameter and always restored in the {@code finally} block.
   *
   * @param req the web script request carrying the search query parameters
   * @param status the response status, set to an error code (403, 400 or 500)
   *        when the search cannot be completed
   * @param cache the cache control object for the response
   * @return a model map containing the {@code data} (matching nodes) and
   *         {@code total} (overall result count) entries, or {@code null} when
   *         an error status and redirect have been set
   */
  @Override
  @SuppressWarnings({ "squid:S1168", "squid:S3776" })
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

    String q = req.getParameter("q");

    String node = req.getParameter("node");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          node
        )
      ) {
        throw new AccessDeniedException(
          "Cannot do a search, no access on the node"
        );
      }

      String from = req.getParameter("creationDateFrom");
      String to = req.getParameter("creationDateTo");

      Date creationDateFrom = (from != null
        ? Converter.convertStringToSimpleDate(from)
        : null);
      Date creationDateTo = (to != null
        ? Converter.convertStringToSimpleDate(to)
        : null);

      from = req.getParameter("modifiedDateFrom");
      to = req.getParameter("modifiedDateTo");

      Date modifiedDateFrom = (from != null
        ? Converter.convertStringToSimpleDate(from)
        : null);
      Date modifiedDateTo = (to != null
        ? Converter.convertStringToSimpleDate(to)
        : null);

      // SearchFor could be All (default), Name, Title, Content
      String searchFor = req.getParameter("searchFor");

      // SearchIn could be All, Library, Forums, Information or Agenda
      String searchIn = req.getParameter("searchIn");

      String creator = req.getParameter("creator");

      // comma separated list of keywords
      String keywords = req.getParameter("keywords");

      // Dynamic properties
      String[] dynamicProprerties = new String[20];
      for (int i = 0; i < 20; i++) {
        String dynamicProperty = req.getParameter("dynAttr" + (i + 1));
        if (dynamicProperty != null && !dynamicProperty.trim().isEmpty()) {
          dynamicProprerties[i] = dynamicProperty.trim();
        }
      }

      String paramStatus = req.getParameter("status");
      String securityRanking = req.getParameter("securityRanking");
      String version = req.getParameter("version");

      String sort = req.getParameter("sort");
      boolean ascendingOrder = !(req.getParameter("order") != null &&
        "desc".equalsIgnoreCase(req.getParameter("order")));

      PagedSearchNodes nodes = this.searchApi.searchGet(
        q,
        node,
        language,
        nbPage,
        nbLimit,
        searchFor,
        searchIn,
        creator,
        creationDateFrom,
        creationDateTo,
        modifiedDateFrom,
        modifiedDateTo,
        keywords,
        paramStatus,
        securityRanking,
        version,
        dynamicProprerties,
        sort,
        ascendingOrder
      );

      model.put("data", nodes.getData());
      model.put("total", nodes.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when attempting search on node " + node, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | EmptyQueryStringException inre) {
      logger.error(
        "Bad request when performing search. Query: " + q + ", Node: " + node,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error during search execution. Query: " +
          q +
          ", Node: " +
          node,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
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
