/**
 *
 */
package io.swagger.api;

import io.swagger.model.Node;
import io.swagger.model.Profile;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LibraryApi}.
 *
 * <p>Provides read-only library operations for a given library node (an Interest
 * Group's Library service space) within the Alfresco repository. It resolves the
 * repository path of the supplied node and runs Lucene searches against the
 * workspace store to locate:
 *
 * <ul>
 *   <li>nodes that are currently checked out / locked below the library,</li>
 *   <li>shared spaces (shared-space containers) below the library, and</li>
 *   <li>profiles that are exported (shared) for the library's Interest Group.</li>
 * </ul>
 *
 * <p>Search results are converted into {@link io.swagger.model.Node} and
 * {@link io.swagger.model.Profile} domain models before being returned.
 *
 * @author beaurpi
 */
public class LibraryApiImpl implements LibraryApi {

  /** Alfresco service used to navigate node relationships (e.g. primary parent). */
  @Autowired
  private NodeService nodeService;

  /** Alfresco search service used to run the Lucene queries against the repository. */
  @Autowired
  private SearchService searchService;

  /** Helper providing repository utilities such as resolving a node's search path. */
  @Autowired
  private ApiToolBox apiToolBox;

  /** API used to convert repository {@link NodeRef}s into {@link Node} domain models. */
  @Autowired
  private NodesApi nodesApi;

  /** API used to retrieve the profiles associated with an Interest Group. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Separator used to join clauses when building the Lucene search query. */
  private static final String AND = " AND ";

  /**
   * Retrieves the nodes that are currently locked (checked out) beneath the
   * given library node.
   *
   * <p>Builds a Lucene query restricted to the search path of {@code nodeId}
   * and matching the {@code cm:checkedOut} aspect, then converts each matching
   * repository node into a {@link Node} domain model.
   *
   * @param nodeId the identifier of the library node to search under; when
   *     {@code null} no path/aspect restriction is added to the query
   * @return the list of locked nodes found below the given node; never
   *     {@code null}, possibly empty
   * @see io.swagger.api.LibraryApi#getLockedNodes(String)
   */
  @Override
  public List<Node> getLockedNodes(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    StringBuilder queryBuilder = new StringBuilder();
    List<Node> result;
    if (nodeId != null) {
      String path =
        "(PATH:\"" + apiToolBox.getPathFromSpaceRef(nodeRef, true) + "\")";
      queryBuilder.append(path);
      queryBuilder.append(AND);
      queryBuilder.append("ASPECT:\"cm:checkedOut\"");
    }

    result = new ArrayList<>();

    ResultSet rs = executeSearch(queryBuilder);

    for (NodeRef ref : rs.getNodeRefs()) {
      result.add(nodesApi.getNode(ref));
    }

    return result;
  }

  /**
   * Executes a Lucene search against the workspace (live) store using the query
   * accumulated in the supplied builder.
   *
   * <p>The search is configured with no maximum item limit ({@code -1}) so that
   * all matching nodes are returned.
   *
   * @param queryBuilder the builder holding the Lucene query string to execute
   * @return the {@link ResultSet} produced by the search service
   */
  private ResultSet executeSearch(StringBuilder queryBuilder) {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setMaxItems(-1);
    searchParameters.setQuery(queryBuilder.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    return searchService.query(searchParameters);
  }

  /**
   * Retrieves the shared spaces located beneath the given library node.
   *
   * <p>Builds a Lucene query restricted to the search path of {@code nodeId}
   * and matching the {@code ss:Container} type. For each matching shared-space
   * container the primary parent is resolved and converted into a {@link Node}
   * domain model, so the returned list represents the parent spaces that are
   * shared.
   *
   * @param nodeId the identifier of the library node to search under; when
   *     {@code null} no path/type restriction is added to the query
   * @return the list of shared parent nodes found below the given node; never
   *     {@code null}, possibly empty
   * @see io.swagger.api.LibraryApi#getSharedNodes(String)
   */
  @Override
  public List<Node> getSharedNodes(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    StringBuilder queryBuilder = new StringBuilder();
    List<Node> result;
    if (nodeId != null) {
      String path =
        "(PATH:\"" + apiToolBox.getPathFromSpaceRef(nodeRef, true) + "\")";
      queryBuilder.append(path);
      queryBuilder.append(AND);
      queryBuilder.append("TYPE:\"ss:Container\"");
    }

    result = new ArrayList<>();

    ResultSet rs = executeSearch(queryBuilder);

    for (NodeRef ref : rs.getNodeRefs()) {
      NodeRef parentRef = nodeService.getPrimaryParent(ref).getParentRef();
      result.add(nodesApi.getNode(parentRef));
    }

    return result;
  }

  /**
   * Retrieves the exported (shared) profiles for the Interest Group identified
   * by the given node.
   *
   * <p>Fetches all profiles of the group via {@link ProfilesApi} and filters
   * them to keep only those flagged as exported.
   *
   * @param nodeId the identifier of the Interest Group whose profiles are read
   * @return the list of exported profiles; never {@code null}, possibly empty
   * @see io.swagger.api.LibraryApi#getSharedProfiles(String)
   */
  @Override
  public List<Profile> getSharedProfiles(String nodeId) {
    List<Profile> profiles = profilesApi.groupsIdProfilesGet(
      nodeId,
      null,
      false
    );
    List<Profile> result = new ArrayList<>();
    for (Profile profile : profiles) {
      if (Boolean.TRUE.equals(profile.getExported())) {
        result.add(profile);
      }
    }

    return result;
  }
}
