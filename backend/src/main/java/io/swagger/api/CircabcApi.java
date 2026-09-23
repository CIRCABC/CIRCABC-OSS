package io.swagger.api;

import io.swagger.model.User;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service contract for CIRCABC-wide (root level) operations.
 *
 * <p>This interface defines the operations backing the top-level CIRCABC REST
 * endpoints, such as managing the platform administrators and resolving the key
 * structural {@link NodeRef}s used throughout the application (the CIRCABC root
 * folder, the CIRCABC data dictionary, the machine-translation folder, Company
 * Home, Guest Home and the root category header).
 *
 * <p>Implementations are responsible for lazily bootstrapping the CIRCABC root
 * node and its associated Alfresco authority groups (master, subscribers,
 * invited users and administrators) when they do not yet exist.
 */
public interface CircabcApi {
  /**
   * Grants CIRCABC administrator rights to the supplied users.
   *
   * <p>Each user is added to the CIRCABC administrators authority group and
   * registered as a CIRCABC administrator. Users that are unknown to the
   * repository are provisioned (created from LDAP data) before being added.
   *
   * @param userIds the identifiers (user names) of the users to promote to
   *     CIRCABC administrators
   */
  void circabcAdminsPost(List<String> userIds);

  /**
   * Revokes CIRCABC administrator rights from the given user.
   *
   * <p>The user is removed from the CIRCABC administrators authority group and,
   * if currently registered as a CIRCABC administrator, is unregistered.
   *
   * @param userId the identifier (user name) of the user whose administrator
   *     rights are to be revoked
   */
  void circabcAdminsUserIdDelete(String userId);

  /**
   * Returns the list of current CIRCABC administrators.
   *
   * @return the users belonging to the CIRCABC administrators group; an empty
   *     list if there are none
   */
  List<User> getCircabcAdmins();

  /**
   * Returns the {@link NodeRef} of the CIRCABC root folder, resolving it if it
   * has not yet been located.
   *
   * @return the node reference of the CIRCABC root folder
   */
  NodeRef getCircabcNodeRef();

  /**
   * Returns the {@link NodeRef} of the CIRCABC data dictionary folder, creating
   * it under the Alfresco data dictionary if it does not already exist.
   *
   * @return the node reference of the CIRCABC data dictionary folder
   */
  NodeRef getCircabcDictionaryNodeRef();

  /**
   * Returns the {@link NodeRef} of the machine-translation (MT) folder.
   *
   * @return the node reference of the machine-translation folder
   */
  NodeRef getMTNodeRef();

  /**
   * Returns the {@link NodeRef} of the repository Company Home folder.
   *
   * @return the node reference of Company Home
   */
  NodeRef getCompanyHomeNodeRef();

  /**
   * Returns the {@link NodeRef} of the repository Guest Home folder.
   *
   * @return the node reference of Guest Home
   */
  NodeRef getGuestHomeNodeRef();

  /**
   * Returns the {@link NodeRef} of the root category header ("CircaBCHeader")
   * under which the CIRCABC category hierarchy is organized.
   *
   * @return the node reference of the root category header, or {@code null} if
   *     it cannot be found
   */
  NodeRef getRootCategoryHeader();

  /**
   * Returns the name of the Alfresco authority group holding all invited
   * CIRCABC users.
   *
   * @return the invited-users group name
   */
  String getInvitedUsersGroupName();
}
