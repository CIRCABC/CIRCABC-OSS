package io.swagger.api;

import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;

/**
 * Business-logic contract for the CIRCABC "guards" endpoints.
 *
 * <p>A guard resolves, for the currently authenticated user, whether a given
 * action or view is permitted on a specific node of the Alfresco repository.
 * Each method inspects the node's aspects and the permissions set on it and
 * returns a {@link GuardAuthorization} whose {@code granted} flag indicates
 * whether access is allowed. These operations are typically consumed by the
 * frontend to decide whether UI features should be shown or enabled.
 *
 * @author beaurpi
 */
public interface GuardsApi {
  /**
   * Determines whether the current user may view the given Interest Group.
   *
   * <p>Access is granted when the group is public (visible to guests), when it
   * is visible to registered users and the caller is authenticated (i.e. not
   * the guest user), or when the caller has explicit {@code Read} permission on
   * a non-registered group.
   *
   * @param id the identifier of the Interest Group node
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the group may be viewed by the current user
   */
  GuardAuthorization guardsGroupIdGet(String id);

  /**
   * Determines whether the current user may access the service that owns the
   * given node.
   *
   * <p>The relevant access permission is chosen based on the node's aspect
   * (Library, Information, Newsgroup or Event).
   *
   * @param id the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when access is allowed
   * @throws NonExistingNodeException if no node exists for the given
   *                                  identifier
   */
  GuardAuthorization guardsAccessIdGet(String id)
    throws NonExistingNodeException;

  /**
   * Determines whether the current user may edit the given node.
   *
   * <p>Editing is granted when the user (or one of the groups it belongs to)
   * holds an editing-capable library permission on the node, or when the user
   * has {@code Write} permission on it.
   *
   * @param id the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the node may be edited
   * @throws NonExistingNodeException if no node exists for the given
   *                                  identifier
   */
  GuardAuthorization guardsEditionIdGet(String id)
    throws NonExistingNodeException;

  /**
   * Determines whether the current user may access a specific service of an
   * Interest Group.
   *
   * <p>Supported service names are {@code members}, {@code applicants} and
   * {@code information}, each of which is evaluated against the corresponding
   * directory or content permissions.
   *
   * @param groupIp     the identifier of the Interest Group node
   * @param serviceName the name of the service to check ({@code members},
   *                    {@code applicants} or {@code information})
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the service may be accessed
   */
  GuardAuthorization guardsGroupIdServiceNameGet(
    String groupIp,
    String serviceName
  );

  /**
   * Determines whether the current user may administer the members of the
   * given Interest Group.
   *
   * <p>Access is granted only for an Interest Group root node on which the
   * caller holds the directory administration permission.
   *
   * @param groupIp the identifier of the Interest Group node
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when member administration is allowed
   */
  GuardAuthorization guardsGroupIdMembersAdminGet(String groupIp);

  /**
   * Determines whether the current user may administer the given node.
   *
   * <p>The required administration permission depends on the node's aspect:
   * an Interest Group root requires admin rights on all of its services and
   * on the directory, while Information, Library, Newsgroup, Event and
   * Category nodes are each checked against their own administration
   * permission.
   *
   * @param nodeId the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the node may be administered
   */
  GuardAuthorization guardsAdministrationIdGet(String nodeId);
}
