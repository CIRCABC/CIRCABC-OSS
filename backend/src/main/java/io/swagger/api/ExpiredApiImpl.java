package io.swagger.api;

import io.swagger.model.PagedNodes;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link ExpiredApi}.
 *
 * <p>Provides the business logic for retrieving documents whose expiration date has passed
 * within a given interest group. It builds a Lucene search query scoped to the group's node
 * and its path, filtering on the {@code expirationDate} property being in the past
 * ({@code [MIN TO NOW]}), then resolves each matching node into a document model.
 *
 * <p>The search runs under the system user via
 * {@link AuthenticationUtil#runAs(AuthenticationUtil.RunAsWork, String)} so that expired items
 * can be discovered regardless of the caller's permissions.
 *
 * @author beaurpi
 */
public class ExpiredApiImpl implements ExpiredApi {

  /** Escaped qualified name of the document expiration-date property, used in the search query. */
  private static final String PROP_EXPIRATION_DATE = Converter.escapeQName(
    DocumentModel.PROP_EXPIRATION_DATE
  );

  /** Lucene query template selecting all nodes directly parented by a given node reference. */
  private static final String QUERY_SEARCH_ALL = "PARENT:\"%s\"";

  /** Closing fragment for a grouped Lucene clause. */
  private static final String CLOSE_QUERY = " )";

  /** Opening fragment for a grouped Lucene clause. */
  private static final String OPEN_QUERY = "( ";

  /** Lucene field prefix used to constrain the query by repository path. */
  private static final String PATH = "PATH:";

  /** Quote-and-space fragment used to delimit the path value inside the Lucene query. */
  private static final String ESCAPE_QUOTES = "\" ";

  /** Alfresco search service used to execute the Lucene query for expired documents. */
  @Autowired
  @Qualifier("searchService")
  private SearchService internalSearchService;

  /** API used to convert matching node references into document models. */
  @Autowired
  private NodesApi nodesApi;

  /** Helper providing repository utilities, such as resolving a node's repository path. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Retrieves the expired documents contained within the given interest group.
   *
   * <p>Backs the HTTP GET endpoint for listing expired documents of a group. It executes a
   * Lucene query (built by {@link #buildSearchQuery(NodeRef)}) against the workspace store to
   * find documents whose expiration date is in the past, running as the system user, and returns
   * them as a paged collection.
   *
   * @param id the identifier of the group node whose expired documents are requested
   * @param limit the maximum number of items to return (pagination hint)
   * @param page the page index to return (pagination hint)
   * @param order the ordering to apply to the results
   * @return a {@link PagedNodes} instance containing the total number of expired documents found
   *     and the resolved document models
   */
  @Override
  public PagedNodes groupsIdDocumentsExpiredGet(
    String id,
    Integer limit,
    Integer page,
    String order
  ) {
    final NodeRef nodeRef = Converter.createNodeRefFromId(id);

    return AuthenticationUtil.runAs(
      () -> {
        PagedNodes expiredNodes = new PagedNodes();

        ResultSet results;
        // fire the query to find the items
        final String query = buildSearchQuery(nodeRef);
        results = internalSearchService.query(
          StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
          SearchService.LANGUAGE_LUCENE,
          query
        );

        expiredNodes.setTotal(results.getNumberFound());

        if (results.length() != 0) {
          for (final ResultSetRow row : results) {
            NodeRef expiredRef = row.getNodeRef();
            expiredNodes.getData().add(nodesApi.getNode(expiredRef));
          }
        }

        return expiredNodes;
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * Builds the Lucene search query used to list the expired documents of a group.
   *
   * <p>The query matches nodes parented by the given node, constrained to the node's repository
   * path, and filtered so that the expiration-date property falls within {@code [MIN TO NOW]}
   * (i.e. already expired).
   *
   * @param nodeRef the reference of the group node whose expired documents are searched
   * @return the Lucene query string to use when displaying the list of expired items
   */
  private String buildSearchQuery(NodeRef nodeRef) {
    String query = String.format(QUERY_SEARCH_ALL, nodeRef.toString());

    query +=
      OPEN_QUERY +
      PATH +
      ESCAPE_QUOTES +
      apiToolBox.getPathFromSpaceRef(nodeRef, true) +
      ESCAPE_QUOTES +
      CLOSE_QUERY;

    String buf = " AND @" + PROP_EXPIRATION_DATE + ":[MIN TO NOW]";
    query += buf;

    return query;
  }
}
