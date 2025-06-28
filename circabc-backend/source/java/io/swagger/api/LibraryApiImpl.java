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
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @author beaurpi */
public class LibraryApiImpl implements LibraryApi {

  static final Log logger = LogFactory.getLog(LibraryApiImpl.class);

  private NodeService nodeService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;
  private NodesApi nodesApi;
  private ProfilesApi profilesApi;

  private String AND = " AND ";

  /* (non-Javadoc)
   * @see io.swagger.api.LibraryApi#getLockedNodes()
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
      if (nodeService.exists(ref)) {
        result.add(nodesApi.getNode(ref));
      } else {
        if (logger.isErrorEnabled()) {
          logger.error(
            "getLockedNodes-node does not exists: " + ref.toString()
          );
        }
      }
    }

    return result;
  }

  private ResultSet executeSearch(StringBuilder queryBuilder) {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setMaxItems(-1);
    searchParameters.setQuery(queryBuilder.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.addStore(Repository.getStoreRef());
    ResultSet rs = searchService.query(searchParameters);
    return rs;
  }

  /* (non-Javadoc)
   * @see io.swagger.api.LibraryApi#getSharedNodes()
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
      if (nodeService.exists(ref)) {
        NodeRef parentRef = nodeService.getPrimaryParent(ref).getParentRef();
        result.add(nodesApi.getNode(parentRef));
      } else {
        if (logger.isErrorEnabled()) {
          logger.error(
            "getSharedNodes-node does not exists: " + ref.toString()
          );
        }
      }
    }

    return result;
  }

  /* (non-Javadoc)
   * @see io.swagger.api.LibraryApi#getSharedProfiles()
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
      if (profile.getExported()) {
        result.add(profile);
      }
    }

    return result;
  }

  /** @return the nodeService */
  public NodeService getNodeService() {
    return nodeService;
  }

  /** @param nodeService the nodeService to set */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /** @return the searchService */
  public SearchService getSearchService() {
    return searchService;
  }

  /** @param searchService the searchService to set */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /** @return the apiToolBox */
  public ApiToolBox getApiToolBox() {
    return apiToolBox;
  }

  /** @param apiToolBox the apiToolBox to set */
  public void setApiToolBox(ApiToolBox apiToolBox) {
    this.apiToolBox = apiToolBox;
  }

  /** @return the nodesApi */
  public NodesApi getNodesApi() {
    return nodesApi;
  }

  /** @param nodesApi the nodesApi to set */
  public void setNodesApi(NodesApi nodesApi) {
    this.nodesApi = nodesApi;
  }

  /** @return the profilesApi */
  public ProfilesApi getProfilesApi() {
    return profilesApi;
  }

  /** @param profilesApi the profilesApi to set */
  public void setProfilesApi(ProfilesApi profilesApi) {
    this.profilesApi = profilesApi;
  }
}
