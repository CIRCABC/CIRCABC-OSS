package io.swagger.api;

import io.swagger.model.*;
import java.util.List;

/**
 * Business API for managing CIRCABC spaces (folders) within the Alfresco repository.
 *
 * <p>A "space" is an Alfresco container node (a folder) that may hold sub-spaces, files, URL
 * links and other content items. This interface groups the operations exposed to the REST
 * layer for browsing space contents, creating and updating spaces, deleting spaces, and
 * managing the sharing of a space with other Interest Groups (IGs).
 *
 * <p>Implementations contain the actual business logic and are typically injected into the
 * webscript endpoint classes that back the {@code /spaces/**} REST routes.
 *
 * @author beaurpi
 */
public interface SpacesApi {
  /**
   * Returns the immediate children of a space.
   *
   * <p>Backs {@code GET /spaces/{id}/children}. The returned list may contain any kind of child
   * node (sub-spaces, files, URLs, etc.).
   *
   * @param id the identifier of the parent space whose children are requested
   * @param folderOnly if {@code true}, only folder/space children are returned; otherwise all
   *     child node types are included
   * @return the list of child nodes of the given space
   */
  List<Node> spaceGetChildren(String id, boolean folderOnly);

  /**
   * Creates a new space (folder) inside an existing space.
   *
   * <p>Backs {@code POST /spaces/{id}/spaces}.
   *
   * @param id the identifier of the parent space in which the new space is created
   * @param body the definition of the space to create (e.g. name, title, description)
   * @return the newly created space node
   */
  Node spacesIdSpacesPost(String id, Node body);

  /**
   * Deletes the referenced space (folder).
   *
   * <p>Backs {@code DELETE /spaces/{id}}.
   *
   * @param id the identifier of the space to delete
   */
  void spaceDelete(String id);

  /**
   * Returns the children of a space with paging, sorting and filtering support.
   *
   * <p>Backs the paged variant of {@code GET /spaces/{id}/children}. The result may contain any
   * kind of child node (sub-spaces, files, URLs, etc.), constrained by the supplied filters.
   *
   * @param id the identifier of the parent space whose children are requested
   * @param nbPage the (1-based) page number to retrieve
   * @param nbLimit the maximum number of items per page
   * @param sort the sort criterion applied to the children
   * @param folderOnly if {@code true}, restrict the result to folder/space children
   * @param fileOnly if {@code true}, restrict the result to file children
   * @param skipExpiredItems if {@code true}, items whose expiration date has passed are excluded
   * @return a paged collection of the matching child nodes
   */
  PagedNodes spaceGetChildren(
    String id,
    int nbPage,
    int nbLimit,
    String sort,
    boolean folderOnly,
    boolean fileOnly,
    boolean skipExpiredItems
  );

  /**
   * Updates the definition (metadata) of an existing space.
   *
   * <p>Backs {@code PUT /spaces/{id}}.
   *
   * @param id the identifier of the space to update
   * @param body the new definition to apply to the space
   */
  void spacesIdPut(String id, Node body);

  /**
   * Creates a new URL link node inside a space.
   *
   * <p>Backs {@code POST /spaces/{id}/url}.
   *
   * @param id the identifier of the space in which the URL link is created
   * @param body the definition of the URL node to create
   * @return the newly created URL node
   */
  Node spacesIdUrlPost(String id, Node body);

  // Space sharing related methods

  /**
   * Lists the Interest Groups that a space has been shared with.
   *
   * @param spaceId the identifier of the shared space
   * @param page the (1-based) page number to retrieve
   * @param limit the maximum number of shares per page
   * @return a paged collection of the shares granted on the space
   */
  PagedShares getInvitedInterestGroups(String spaceId, int page, int limit);

  /**
   * Removes a sharing grant, revoking an Interest Group's access to a shared space.
   *
   * @param spaceId the identifier of the shared space
   * @param sharedIGId the identifier of the Interest Group whose share is removed
   */
  void deleteShare(String spaceId, String sharedIGId);

  /**
   * Shares a space with an Interest Group.
   *
   * @param spaceId the identifier of the space to share
   * @param share the sharing definition (target Interest Group and granted permission)
   * @param notifyLeaders if {@code true}, the leaders of the target Interest Group are notified
   */
  void addShare(String spaceId, Share share, boolean notifyLeaders);

  /**
   * Changes the permission granted to an Interest Group on a shared space.
   *
   * @param sharedSpaceId the identifier of the shared space
   * @param igId the identifier of the Interest Group whose permission is changed
   * @param newPermission the new permission to assign
   * @param notifyLeaders if {@code true}, the leaders of the Interest Group are notified
   */
  void changeSharePermission(
    String sharedSpaceId,
    String igId,
    String newPermission,
    boolean notifyLeaders
  );

  /**
   * Returns the Interest Groups a space is shared with together with their permissions.
   *
   * @param spaceId the identifier of the shared space
   * @return the Interest Groups and their associated permissions for the space
   */
  ShareIGsAndPermissions getShareIGsAndPermissions(String spaceId);

  /**
   * Lists the spaces that are available to be shared as links for the given space.
   *
   * @param spaceId the identifier of the space for which candidate shared spaces are listed
   * @return the list of shareable space items
   */
  List<ShareSpaceItem> getAvailableSharedSpaces(String spaceId);

  /**
   * Creates a link to a shared space under the given parent space.
   *
   * @param spaceId the identifier of the shared space being linked to
   * @param parentId the identifier of the parent space where the link is created
   * @param title the title of the shared space link
   * @param description the description of the shared space link
   */
  void createSharedSpaceLink(
    String spaceId,
    String parentId,
    String title,
    String description
  );

  /**
   * Returns the children of a restricted space with paging, sorting and filtering support.
   *
   * <p>Behaves like {@link #spaceGetChildren(String, int, int, String, boolean, boolean, boolean)}
   * but applies restricted-access rules when resolving the visible children.
   *
   * @param id the identifier of the parent space whose children are requested
   * @param nbPage the (1-based) page number to retrieve
   * @param nbLimit the maximum number of items per page
   * @param sort the sort criterion applied to the children
   * @param folderOnly if {@code true}, restrict the result to folder/space children
   * @param fileOnly if {@code true}, restrict the result to file children
   * @return a paged collection of the matching child nodes
   */
  PagedNodes restrictedSpaceGetChildren(
    String id,
    int nbPage,
    int nbLimit,
    String sort,
    boolean folderOnly,
    boolean fileOnly
  );

  /**
   * Computes the total size of a space (folder).
   *
   * @param id the identifier of the space whose size is computed
   * @return the aggregated size of the space, in bytes
   */
  int getFolderSize(String id);
}
