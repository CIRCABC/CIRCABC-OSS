/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service;

import eu.europa.ec.digit.circabc.rest.service.report.ReportDaoService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SharedSpaceModel;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.ApiToolBox;
import java.util.*;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ShareSpaceService}.
 *
 * <p>A shared space is a folder that an Interest Group (IG) leader uses to share documents with
 * members of other Interest Groups belonging to the same category. This service manages the whole
 * life-cycle of that mechanism:
 *
 * <ul>
 *   <li>inviting/uninviting an IG to a shared space (marking the space with the {@code
 *       ci:circabcSharedSpace} aspect and maintaining a hidden container that records the invited
 *       IGs and their granted permission);
 *   <li>propagating the appropriate library permissions from the invited IG to the shared space so
 *       that qualified users of the invited IG gain access;
 *   <li>creating folder links that point to a shared space;
 *   <li>querying invited IGs, IGs still available for invitation and shared spaces reachable from a
 *       given space.
 * </ul>
 *
 * <p>Collaborators are wired through Spring using {@link Autowired} on the Alfresco core services
 * ({@link NodeService}, {@link PermissionService}, etc.) and CIRCABC helpers.
 *
 * @author Slobodan Filipovic
 */
public class ShareSpaceServiceImpl implements ShareSpaceService {

  /** Authority (group) name of the CircaBC category administrators. Members of this group are
   * skipped when propagating permissions to a shared space. */
  private static final String GROUP_CIRCA_CATEGORY_ADMIN =
    "GROUP_" + "CircaCategoryAdmin";

  /** Name of the {@code Library} service folder inside an Interest Group root. */
  private static final String LIBRARY = "Library";

  /**
   * Shallow search for nodes with a name pattern
   */
  private static final String XPATH_QUERY_NODE_MATCH =
    "./*[like(@cm:name, $cm:name, false)]";

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private DictionaryService dictionaryService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private ReportDaoService reportDaoService;

  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * {@inheritDoc}
   *
   * <p>Validates the parameters, records the invited interest group under the shared space's
   * container and propagates the library permissions of the invited group to the shared space.
   *
   * @param shareSpace        node ref of the shared space that receives the invitation
   * @param interestGroup     node ref of the interest group being invited
   * @param libraryPermission the library permission granted to qualified users of the invited group
   * @throws IllegalStateException if validation fails (e.g. the target is not an IG root, the space
   *     already belongs to the interest group, they are in different categories, or the interest
   *     group is already invited)
   */
  public void inviteInterestGroup(
    final NodeRef shareSpace,
    final NodeRef interestGroup,
    final String libraryPermission
  ) {
    validateInviteParameters(shareSpace, interestGroup, libraryPermission);
    createNode(shareSpace, interestGroup, libraryPermission);
    copyPermissions(shareSpace, interestGroup, libraryPermission);
  }

  private void copyPermissions(
    NodeRef shareSpace,
    NodeRef interestGroup,
    String libraryPermission
  ) {
    Map<String, String> libPermissionsMap = craetePermissionMap(
      libraryPermission
    );

    NodeRef libraryNodeRef = nodeService.getChildByName(
      interestGroup,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY
    );
    Set<AccessPermission> allSetPermissions =
      permissionService.getAllSetPermissions(libraryNodeRef);

    for (AccessPermission permission : allSetPermissions) {
      final String oldPermission = permission.getPermission();
      if (libPermissionsMap.containsKey(oldPermission)) {
        String newPermission = libPermissionsMap.get(oldPermission);
        final String authority = permission.getAuthority();
        if (
          !authority.contains(GROUP_CIRCA_CATEGORY_ADMIN) &&
          !authority.contains("GROUP_EVERYONE")
        ) {
          permissionService.setPermission(
            shareSpace,
            authority,
            newPermission,
            true
          );
        }
      }
    }
  }

  private Map<String, String> craetePermissionMap(String libraryPermission) {
    Map<String, String> result = new HashMap<>();
    boolean isStrongerPermission = false;
    for (String permission : LibraryPermissions.getOrderedLibraryPermissions()) {
      if (
        !isStrongerPermission && permission.equalsIgnoreCase(libraryPermission)
      ) {
        isStrongerPermission = true;
      }
      if (isStrongerPermission) {
        result.put(permission, libraryPermission);
      } else {
        result.put(permission, permission);
      }
    }

    return result;
  }

  private void createNode(
    final NodeRef shareSpace,
    final NodeRef interestGroup,
    final String libraryPermission
  ) {
    if (!nodeService.hasAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE)) {
      nodeService.addAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE, null);
    }

    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      shareSpace,
      SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
      RegexQNamePattern.MATCH_ALL
    );
    final ChildAssociationRef assocRef;
    if (childAssocs.isEmpty()) {
      assocRef = nodeService.createNode(
        shareSpace,
        SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
        SharedSpaceModel.TYPE_CONTAINER,
        SharedSpaceModel.TYPE_CONTAINER,
        new PropertyMap()
      );
    } else {
      assocRef = childAssocs.get(0);
    }

    NodeRef container = assocRef.getChildRef();
    PropertyMap properties = new PropertyMap();
    properties.put(
      SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF,
      interestGroup
    );
    properties.put(SharedSpaceModel.PROP_PERMISSION, libraryPermission);

    boolean nodeExists = false;
    List<ChildAssociationRef> igChildAssocs = nodeService.getChildAssocs(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      RegexQNamePattern.MATCH_ALL
    );
    for (ChildAssociationRef ref : igChildAssocs) {
      NodeRef childRef = ref.getChildRef();
      NodeRef igNodeRef = (NodeRef) nodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      );
      if (igNodeRef.equals(interestGroup)) {
        nodeExists = true;
        break;
      }
    }
    if (nodeExists) {
      throw new IllegalStateException(
        "Interestgroup (" +
          interestGroup +
          ") is already invited to space (" +
          shareSpace +
          ")"
      );
    }

    nodeService.createNode(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      SharedSpaceModel.TYPE_INVITED_INTEREST_GROUP,
      SharedSpaceModel.TYPE_INVITED_INTEREST_GROUP,
      properties
    );
  }

  private void validateInviteParameters(
    final NodeRef shareSpace,
    final NodeRef interestGroup,
    final String libraryPermission
  ) {
    ParameterCheck.mandatory(
      "shareSpaceNodeRef is mandatory param",
      shareSpace
    );
    ParameterCheck.mandatory(
      "The targetInterestGroupNodeRef is mandatory param ",
      interestGroup
    );
    ParameterCheck.mandatory(
      "The libraryPermission is mandatory param ",
      libraryPermission
    );

    if (!nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalStateException(
        "This node is not ig root (" + interestGroup + ")"
      );
    }

    final NodeRef currentInterestGroup = apiToolBox.getCurrentInterestGroup(
      shareSpace
    );
    if (currentInterestGroup.equals(interestGroup)) {
      throw new IllegalStateException(
        "Shared space(" +
          shareSpace +
          ") already belong to Interestgroup (" +
          interestGroup +
          ")"
      );
    }

    final NodeRef shareSpaceCategory = apiToolBox.getCurrentCategory(
      shareSpace
    );
    final NodeRef interestGroupCategory = apiToolBox.getCurrentCategory(
      interestGroup
    );
    if (!shareSpaceCategory.equals(interestGroupCategory)) {
      throw new IllegalStateException(
        "Shared space(" +
          shareSpace +
          ") and  Interestgroup (" +
          interestGroup +
          ") shoul belong to same category"
      );
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Validates the parameters, removes the record of the invited interest group and clears the
   * permissions that had been propagated from the interest group's library to the shared space.
   *
   * @param shareSpace    node ref of the shared space the interest group is removed from
   * @param interestGroup node ref of the interest group being uninvited
   * @throws IllegalStateException if the shared space does not carry the shared-space aspect or the
   *     expected container association is missing
   */
  public void unInviteInterestGroup(
    final NodeRef shareSpace,
    final NodeRef interestGroup
  ) {
    validateUnInviteParameters(shareSpace, interestGroup);

    deleteNode(shareSpace, interestGroup);

    removePermissions(shareSpace, interestGroup);
  }

  private void removePermissions(NodeRef shareSpace, NodeRef interestGroup) {
    NodeRef libraryNodeRef = nodeService.getChildByName(
      interestGroup,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY
    );
    Set<AccessPermission> libraryPermissions =
      permissionService.getAllSetPermissions(libraryNodeRef);
    for (AccessPermission permission : libraryPermissions) {
      permissionService.clearPermission(shareSpace, permission.getAuthority());
    }
  }

  private void deleteNode(
    final NodeRef shareSpace,
    final NodeRef interestGroup
  ) {
    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      shareSpace,
      SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
      RegexQNamePattern.MATCH_ALL
    );
    final ChildAssociationRef assocRef;
    if (childAssocs.isEmpty()) {
      throw new IllegalStateException(
        "Space is missing association " +
          SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER
      );
    } else {
      assocRef = childAssocs.get(0);
    }

    NodeRef container = assocRef.getChildRef();

    List<ChildAssociationRef> igChildAssocs = nodeService.getChildAssocs(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      RegexQNamePattern.MATCH_ALL
    );
    for (ChildAssociationRef ref : igChildAssocs) {
      NodeRef childRef = ref.getChildRef();
      NodeRef igNodeRef = (NodeRef) nodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      );
      if (igNodeRef.equals(interestGroup)) {
        nodeService.deleteNode(childRef);
        break;
      }
    }
    List<ChildAssociationRef> restChildAssocs = nodeService.getChildAssocs(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      RegexQNamePattern.MATCH_ALL
    );
    if (restChildAssocs.isEmpty()) {
      nodeService.deleteNode(container);
      nodeService.removeAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE);
    }
  }

  private void validateUnInviteParameters(
    final NodeRef shareSpaceNodeRef,
    final NodeRef interestGroupNodeRef
  ) {
    ParameterCheck.mandatory(
      "shareSpaceNodeRef is mandatory param",
      shareSpaceNodeRef
    );
    ParameterCheck.mandatory(
      "The targetInterestGroupNodeRef is mandatory param ",
      interestGroupNodeRef
    );

    if (
      !nodeService.hasAspect(
        shareSpaceNodeRef,
        CircabcModel.ASPECT_SHARED_SPACE
      )
    ) {
      throw new IllegalStateException(
        "Space is missing aspect " + CircabcModel.ASPECT_SHARED_SPACE
      );
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a folder-link node (type {@code app:folderlink}) under {@code currentSpace} whose
   * destination is {@code sharedSpace}. The link name must be unique within {@code currentSpace}.
   *
   * @param currentSpace node ref of the space where the link is created
   * @param sharedSpace  node ref of the shared space the link points to
   * @param name         name of the link; must be unique within {@code currentSpace}
   * @param title        optional title of the link (ignored when {@code null} or empty)
   * @param description  optional description of the link (ignored when {@code null} or empty)
   * @return node ref of the newly created folder-link node
   * @throws IllegalStateException if a node with the same name already exists in {@code
   *     currentSpace}
   */
  public NodeRef linkSharedSpace(
    NodeRef currentSpace,
    NodeRef sharedSpace,
    String name,
    String title,
    String description
  ) {
    if (checkExists(name, currentSpace)) {
      throw new IllegalStateException("Name should be unique " + name);
    }
    ChildAssociationRef assocRef = nodeService.getPrimaryParent(sharedSpace);
    PropertyMap props = new PropertyMap(4, 1.0f);
    props.put(ContentModel.PROP_NAME, name);
    props.put(ContentModel.PROP_LINK_DESTINATION, sharedSpace);
    if (title != null && !title.isEmpty()) {
      props.put(ContentModel.PROP_TITLE, title);
    }
    if (description != null && !description.isEmpty()) {
      props.put(ContentModel.PROP_DESCRIPTION, description);
    }
    // create Folder link node
    ChildAssociationRef childRef = nodeService.createNode(
      currentSpace,
      ContentModel.ASSOC_CONTAINS,
      assocRef.getQName(),
      ApplicationModel.TYPE_FOLDERLINK,
      props
    );

    return childRef.getChildRef();
  }

  private boolean checkExists(String name, NodeRef parent) {
    QueryParameterDefinition[] params = new QueryParameterDefinition[1];
    params[0] = new QueryParameterDefImpl(
      ContentModel.PROP_NAME,
      dictionaryService.getDataType(DataTypeDefinition.TEXT),
      true,
      name
    );

    // execute the query
    List<NodeRef> nodeRefs = searchService.selectNodes(
      parent,
      XPATH_QUERY_NODE_MATCH,
      params,
      namespaceService,
      false
    );

    return (!nodeRefs.isEmpty());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads the shared space's hidden container and returns, for every still existing invited
   * interest group, the group node ref together with the permission granted to it.
   *
   * @param shareSpace node ref of the shared space to inspect
   * @return list of pairs holding the invited interest group node ref and its granted permission;
   *     an empty list when the space has no container or no invitations
   */
  public List<Pair<NodeRef, String>> getInvitedInterestGroups(
    NodeRef shareSpace
  ) {
    ArrayList<Pair<NodeRef, String>> result = new ArrayList<>();

    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      shareSpace,
      SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
      RegexQNamePattern.MATCH_ALL
    );
    final ChildAssociationRef assocRef;
    if (childAssocs.isEmpty()) {
      return result;
    } else {
      assocRef = childAssocs.get(0);
    }

    NodeRef container = assocRef.getChildRef();

    List<ChildAssociationRef> igChildAssocs = nodeService.getChildAssocs(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      RegexQNamePattern.MATCH_ALL
    );
    for (ChildAssociationRef ref : igChildAssocs) {
      NodeRef childRef = ref.getChildRef();
      final NodeRef igNodeRef = (NodeRef) nodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      );

      if ((igNodeRef != null) && nodeService.exists(igNodeRef)) {
        final String permission = (String) nodeService.getProperty(
          childRef,
          SharedSpaceModel.PROP_PERMISSION
        );
        result.add(new Pair<>(igNodeRef, permission));
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Collects all interest groups in the shared space's category and removes the space's own
   * interest group and any already invited group, leaving the groups that can still be invited.
   *
   * @param shareSpace node ref of the shared space
   * @return list of node refs of the interest groups that can still be invited
   */
  public List<NodeRef> getAvailableInterestGroups(NodeRef shareSpace) {
    final NodeRef interestGroup = apiToolBox.getCurrentInterestGroup(
      shareSpace
    );
    final NodeRef shareSpaceCategory = apiToolBox.getCurrentCategory(
      interestGroup
    );

    List<Pair<NodeRef, String>> invitedIgNodes = getInvitedInterestGroups(
      shareSpace
    );
    List<NodeRef> candidates = getAllIgNodes(shareSpaceCategory);
    candidates.remove(interestGroup);
    for (final Pair<NodeRef, String> invited : invitedIgNodes) {
      candidates.remove(invited.getFirst());
    }

    return candidates;
  }

  private List<NodeRef> getAllIgNodes(NodeRef categoryNode) {
    List<NodeRef> result = new ArrayList<>();

    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      categoryNode,
      ContentModel.ASSOC_CONTAINS,
      RegexQNamePattern.MATCH_ALL
    );

    if (childAssocs != null) {
      List<ChildAssociationRef> igChildAssocs = nodeService.getChildAssocs(
        categoryNode,
        ContentModel.ASSOC_CONTAINS,
        RegexQNamePattern.MATCH_ALL
      );
      for (ChildAssociationRef ref : igChildAssocs) {
        NodeRef childRef = ref.getChildRef();
        result.add(childRef);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Resolves the space's interest group, asks the report DAO for the reachable shared spaces and
   * keeps only the ancestors that actually carry the shared-space aspect.
   *
   * @param space node ref of the space from which shared spaces are reachable
   * @return list of node refs of shared spaces available from the given space
   */
  public List<NodeRef> getAvailableShareSpaces(NodeRef space) {
    final NodeRef interestGroup = apiToolBox.getCurrentInterestGroup(space);

    List<NodeRef> result = new ArrayList<>();
    final List<NodeRef> availibleShareSpaces =
      reportDaoService.getAvailibleShareSpaces(interestGroup);
    for (final NodeRef nodeRef : availibleShareSpaces) {
      final ChildAssociationRef primaryParent = nodeService.getPrimaryParent(
        nodeRef
      );
      final ChildAssociationRef parentOfParent = nodeService.getPrimaryParent(
        primaryParent.getParentRef()
      );
      final NodeRef shareSpaceRef = parentOfParent.getParentRef();
      if (
        nodeService.hasAspect(shareSpaceRef, CircabcModel.ASPECT_SHARED_SPACE)
      ) {
        result.add(shareSpaceRef);
      }
    }

    return result;
  }

  private String buildLuceneQueryInvitedIG(NodeRef interestGroup) {
    return (
      "((PATH:\"" +
      apiToolBox.getPathFromSpaceRef(interestGroup, true) +
      "\") AND (ASPECT: \"ci:circabcSharedSpace\"))"
    );
  }

  private ResultSet executeLuceneQuery(final String query) {
    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(query);
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    return searchService.query(sp);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Runs a Lucene query for nodes carrying the {@code ci:circabcSharedSpace} aspect under the
   * given interest group's path.
   *
   * @param interestGroup node ref of the interest group to inspect
   * @return list of node refs of the shared spaces belonging to the interest group
   */
  public List<NodeRef> getAllSharedSpaceInInterestGroup(NodeRef interestGroup) {
    List<NodeRef> result = new ArrayList<>();
    String luceneQuery = buildLuceneQueryInvitedIG(interestGroup);
    ResultSet resultSet = null;
    try {
      resultSet = executeLuceneQuery(luceneQuery);
      for (final ResultSetRow row : resultSet) {
        result.add(row.getNodeRef());
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
    }
    return result;
  }
}
