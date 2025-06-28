package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.SearchApi;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SearchGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SearchGet.class);

  private static final int START_PAGE = 0;
  private static final int DEFAULT_NUMBER_RESULTS = 250;
  private SearchApi searchApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

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
      Locale locale = new Locale(language);
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

      Date creationDateFrom =
        (from != null ? Converter.convertStringToSimpleDate(from) : null);
      Date creationDateTo =
        (to != null ? Converter.convertStringToSimpleDate(to) : null);

      from = req.getParameter("modifiedDateFrom");
      to = req.getParameter("modifiedDateTo");

      Date modifiedDateFrom =
        (from != null ? Converter.convertStringToSimpleDate(from) : null);
      Date modifiedDateTo =
        (to != null ? Converter.convertStringToSimpleDate(to) : null);

      // SearchFor could be All (default), Name, Title, Content
      String searchFor = req.getParameter("searchFor");

      // SearchIn could be All (default), Library, Forums, Information or Agenda
      String searchIn = req.getParameter("searchIn");

      String creator = req.getParameter("creator");

      // comma separated list of keywords
      String keywords = req.getParameter("keywords");

      // Dynamic properties
      String[] dynamicProprerties = new String[20];
      for (int i = 0; i < 20; i++) {
        String dynamicProperty = req.getParameter("dynAttr" + (i + 1));
        if (dynamicProperty != null && dynamicProperty.trim().length() > 0) {
          dynamicProprerties[i] = dynamicProperty.trim();
        }
      }
      String paramStatus = req.getParameter("status");
      String securityRanking = req.getParameter("securityRanking");
      String version = req.getParameter("version");

      String sort = req.getParameter("sort");
      boolean ascendingOrder = (req.getParameter("order") != null &&
          "desc".equalsIgnoreCase(req.getParameter("order")))
        ? false
        : true;

      PagedSearchNodes nodes =
        this.searchApi.searchGet(
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
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException | EmptyQueryStringException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  public SearchApi getSearchApi() {
    return this.searchApi;
  }

  public void setSearchApi(SearchApi searchApi) {
    this.searchApi = searchApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
