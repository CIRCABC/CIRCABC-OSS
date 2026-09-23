package io.swagger.api;

import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;
import java.util.Date;

/**
 * Business operations for searching content nodes in the CIRCABC repository.
 *
 * <p>Implementations back the search REST endpoint, translating the query and
 * filter parameters received from the web layer into a repository search and
 * returning a paginated set of matching nodes. Filtering supports full-text
 * queries scoped to a starting node, plus a range of metadata criteria such as
 * creator, creation/modification date ranges, keywords, status, security
 * ranking, version and arbitrary dynamic properties.
 *
 * @author beaurpi
 */
public interface SearchApi {
  /**
   * Executes a search over the repository and returns a page of matching nodes.
   *
   * @param q the full-text query string to match against node content and metadata
   * @param node the identifier of the node under which the search is scoped
   * @param language the language used to interpret/localise the query
   * @param page the zero- or one-based index of the result page to return
   * @param limit the maximum number of results per page
   * @param searchFor the type of items to search for (e.g. content type filter)
   * @param searchIn the scope or area within which to search
   * @param creator filter restricting results to nodes created by this user
   * @param creationDateFrom lower bound (inclusive) of the node creation date range
   * @param creationDateTo upper bound (inclusive) of the node creation date range
   * @param modifiedDateFrom lower bound (inclusive) of the node modification date range
   * @param modifiedDateTo upper bound (inclusive) of the node modification date range
   * @param keywords keywords used to further filter matching nodes
   * @param status filter restricting results to nodes in the given status
   * @param securityRanking filter restricting results by security ranking
   * @param version filter restricting results by version
   * @param dynamicProperties additional dynamic property filters to apply
   * @param sort the field by which results should be sorted
   * @param order the sort direction; {@code true} for one direction (e.g. ascending)
   *     and {@code false} for the other (e.g. descending)
   * @return a {@link PagedSearchNodes} holding the matching nodes for the requested page
   * @throws EmptyQueryStringException if the required query string is missing or empty
   */
  @SuppressWarnings("java:S107") // Search filter parameters from REST endpoint
  PagedSearchNodes searchGet(
    String q,
    String node,
    String language,
    Integer page,
    Integer limit,
    String searchFor,
    String searchIn,
    String creator,
    Date creationDateFrom,
    Date creationDateTo,
    Date modifiedDateFrom,
    Date modifiedDateTo,
    String keywords,
    String status,
    String securityRanking,
    String version,
    String[] dynamicProperties,
    String sort,
    boolean order
  ) throws EmptyQueryStringException;
}
