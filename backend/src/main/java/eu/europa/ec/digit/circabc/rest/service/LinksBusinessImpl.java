/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.europa.ec.digit.circabc.rest.service;

import eu.europa.ec.digit.circabc.rest.service.helper.MetadataManager;
import eu.europa.ec.digit.circabc.rest.service.helper.NodeTypeManager;
import io.swagger.model.ShareSpaceItem;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SharedSpaceModel;
import io.swagger.model.db.InterestGroupLinkItem;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LinksBusinessSrv}.
 *
 * <p>Provides the business logic for creating and managing links within the Alfresco repository:
 * file links, folder links and shared-space links. It also exposes operations to share a shared
 * space with an Interest Group (granting or revoking library permissions) and to look up the shared
 * spaces and Interest Groups available for sharing.</p>
 *
 * <p>The heavy lifting related to shared spaces is delegated to {@link ShareSpaceService}, while
 * link creation is performed directly against the Alfresco {@link NodeService}.</p>
 *
 * @author Yanick Pignot
 */
public class LinksBusinessImpl implements LinksBusinessSrv {

  /** Name prefix applied to generated link nodes (e.g. {@code "Link to <target name>"}). */
  private static final String LINK_TO_PREFIX = "Link to ";

  /** File extension appended to generated link node names. */
  private static final String LINK_NODE_EXTENSION = ".url";

  /** Alfresco node service used to read/write nodes, properties and associations. */
  @Autowired
  private NodeService nodeService;

  /** Service encapsulating shared-space creation, invitations and lookups. */
  @Autowired
  private ShareSpaceService shareSpaceService;

  /** Utility helper providing common API operations such as category path resolution. */
  @Autowired
  private ApiToolBox apiToolBox;

  /** Helper responsible for computing titles and generating valid, unique node names. */
  @Autowired
  private MetadataManager metadataManager;

  /** Helper used to inspect node types (e.g. to distinguish content from folders). */
  @Autowired
  private NodeTypeManager nodeTypeManager;

  /**
   * Creates a link node pointing to the given target under the specified parent.
   *
   * <p>The created node is a {@code cm:filelink} when the target is a content node, otherwise a
   * {@code cm:folderlink}. The link name is derived from the target name and made unique within the
   * parent.</p>
   *
   * @param parent the existing parent node under which the link is created
   * @param target the node the link points to
   * @return the {@link NodeRef} of the newly created link node
   */
  @Override
  public NodeRef createLink(NodeRef parent, NodeRef target) {
    final String name = getLinkName(parent, target);
    final ChildAssociationRef assocRef = nodeService.getPrimaryParent(target);

    final Map<QName, Serializable> nodeProps = HashMap.newHashMap(2);
    nodeProps.put(ContentModel.PROP_NAME, name);
    nodeProps.put(ContentModel.PROP_LINK_DESTINATION, target);

    final ChildAssociationRef childRef;

    if (nodeTypeManager.isContent(target)) {
      childRef = nodeService.createNode(
        target,
        ContentModel.ASSOC_CONTAINS,
        assocRef.getQName(),
        ApplicationModel.TYPE_FILELINK,
        nodeProps
      );
    } else {
      // create Folder link node
      childRef = nodeService.createNode(
        target,
        ContentModel.ASSOC_CONTAINS,
        assocRef.getQName(),
        ApplicationModel.TYPE_FOLDERLINK,
        nodeProps
      );
    }

    return childRef.getChildRef();
  }

  /**
   * Creates a link to a shared space under the given parent.
   *
   * <p>The link name is derived from the target name and made unique within the parent. Creation is
   * delegated to {@link ShareSpaceService#linkSharedSpace}.</p>
   *
   * @param parent      the existing parent node under which the link is created
   * @param target      the shared-space node the link points to
   * @param title       the title to assign to the link
   * @param description the description to assign to the link
   * @return the {@link NodeRef} of the newly created shared-space link
   */
  @Override
  public NodeRef createSharedSpaceLink(
    NodeRef parent,
    NodeRef target,
    String title,
    String description
  ) {
    final String name = getLinkName(parent, target);

    return shareSpaceService.linkSharedSpace(
      parent,
      target,
      name,
      title,
      description
    );
  }

  /**
   * Shares a shared space with an Interest Group by granting it the given library permission.
   *
   * @param shareSpace        the shared-space node to share
   * @param interestGroup     the Interest Group to invite
   * @param libraryPermission the library permission level to grant to the Interest Group
   */
  @Override
  public void applySharing(
    NodeRef shareSpace,
    NodeRef interestGroup,
    LibraryPermissions libraryPermission
  ) {
    shareSpaceService.inviteInterestGroup(
      shareSpace,
      interestGroup,
      libraryPermission.toString()
    );
  }

  /**
   * Revokes an Interest Group's access to a shared space.
   *
   * @param shareSpace    the shared-space node
   * @param interestGroup the Interest Group whose sharing is to be removed
   */
  @Override
  public void removeSharing(NodeRef shareSpace, NodeRef interestGroup) {
    shareSpaceService.unInviteInterestGroup(shareSpace, interestGroup);
  }

  /**
   * Returns the shared spaces available for the given node's Interest group, Interest group child
   * or category.
   *
   * <p>Only shared spaces that have at least one still-existing invited Interest Group are
   * returned.</p>
   *
   * @param nodeRef any node located at or under a category
   * @return the list of available {@link ShareSpaceItem}; never {@code null}
   */
  @Override
  public List<ShareSpaceItem> getAvailableSharedSpaces(NodeRef nodeRef) {
    final List<NodeRef> allSharedSpaces =
      shareSpaceService.getAvailableShareSpaces(nodeRef);

    if (allSharedSpaces == null || allSharedSpaces.isEmpty()) {
      return Collections.emptyList();
    } else {
      return buildSharedSpaceItems(allSharedSpaces);
    }
  }

  /**
   * Wraps the given shared-space nodes into {@link ShareSpaceItem} instances, keeping only those
   * that have at least one still-existing invited Interest Group and enriching each with its
   * category path.
   *
   * @param allSharedSpaces the shared-space nodes to wrap
   * @return the list of built {@link ShareSpaceItem}; never {@code null}
   */
  private List<ShareSpaceItem> buildSharedSpaceItems(
    final List<NodeRef> allSharedSpaces
  ) {
    final List<ShareSpaceItem> wrappers = new ArrayList<>(
      allSharedSpaces.size()
    );

    for (final NodeRef ref : allSharedSpaces) {
      if (checkIfOneInvitedInterestGroupExists(ref)) {
        wrappers.add(
          new ShareSpaceItem(ref, apiToolBox.getCategoryPath(ref, true))
        );
      }
    }

    return wrappers;
  }

  /**
   * Checks whether the given shared space has at least one invited Interest Group whose backing
   * Interest Group node still exists in the repository.
   *
   * @param shareSpace the shared-space node to inspect
   * @return {@code true} if at least one still-existing invited Interest Group is found, otherwise
   *     {@code false}
   */
  private boolean checkIfOneInvitedInterestGroupExists(NodeRef shareSpace) {
    boolean result = false;
    if (nodeService.hasAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE)) {
      List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
        shareSpace,
        SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
        RegexQNamePattern.MATCH_ALL
      );
      final ChildAssociationRef assocRef;
      if (!childAssocs.isEmpty()) {
        assocRef = childAssocs.get(0);
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
          if ((igNodeRef != null) && nodeService.exists(igNodeRef)) {
            result = true;
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns all shared spaces defined recursively within the Interest Group of the given node.
   *
   * <p>Only shared spaces that have at least one still-existing invited Interest Group are
   * returned.</p>
   *
   * @param nodeRef any node located at or under a category
   * @return the list of matching {@link ShareSpaceItem}; never {@code null}
   */
  @Override
  public List<ShareSpaceItem> findSharedSpaces(NodeRef nodeRef) {
    final List<NodeRef> allSharedSpaces =
      shareSpaceService.getAllSharedSpaceInInterestGroup(nodeRef);

    if (allSharedSpaces == null || allSharedSpaces.isEmpty()) {
      return Collections.emptyList();
    } else {
      return buildSharedSpaceItems(allSharedSpaces);
    }
  }

  /**
   * Returns the Interest Groups that are available to share the given node with.
   *
   * @param nodeRef the library space for which candidate Interest Groups are resolved
   * @return the list of available {@link InterestGroupLinkItem}; never {@code null}
   */
  @Override
  public List<InterestGroupLinkItem> getInterestGroupForSharing(
    final NodeRef nodeRef
  ) {
    final List<NodeRef> interestGroups =
      shareSpaceService.getAvailableInterestGroups(nodeRef);

    if (interestGroups == null || interestGroups.isEmpty()) {
      return Collections.emptyList();
    } else {
      final List<InterestGroupLinkItem> items = new ArrayList<>(
        interestGroups.size()
      );
      for (NodeRef ref : interestGroups) {
        items.add(buildInterestGroupItem(ref, null));
      }

      return items;
    }
  }

  /**
   * Builds an {@link InterestGroupLinkItem} from an Interest Group node, resolving its name and
   * computed title.
   *
   * @param interestGroup the Interest Group node
   * @param permission    the permission associated with the Interest Group, may be {@code null}
   * @return the built {@link InterestGroupLinkItem}
   */
  private InterestGroupLinkItem buildInterestGroupItem(
    final NodeRef interestGroup,
    final String permission
  ) {
    final Map<QName, Serializable> props = nodeService.getProperties(
      interestGroup
    );

    return new InterestGroupLinkItem(
      interestGroup,
      (String) props.get(ContentModel.PROP_NAME),
      permission,
      metadataManager.computeTitle(props)
    );
  }

  /**
   * Computes a valid, unique link node name for the given target under the given parent, of the
   * form {@code "Link to <target name>.url"}.
   *
   * @param parent the parent node under which the link will be created
   * @param target the target node the link points to
   * @return a valid name that is unique within the parent
   */
  private String getLinkName(final NodeRef parent, final NodeRef target) {
    final String targetName = (String) nodeService.getProperty(
      target,
      ContentModel.PROP_NAME
    );

    return metadataManager.getValidUniqueName(
      parent,
      LINK_TO_PREFIX + targetName + LINK_NODE_EXTENSION
    );
  }
}
