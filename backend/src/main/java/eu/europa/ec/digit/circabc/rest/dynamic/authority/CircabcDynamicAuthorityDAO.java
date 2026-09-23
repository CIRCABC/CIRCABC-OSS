package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import java.util.List;

/**
 * Data Access Object for resolving the permissions and administrative role that a given user
 * holds within a CIRCABC group (Interest Group / Category).
 *
 * <p>Implementations query the underlying database (via MyBatis/iBATIS mappings) and are used by
 * the dynamic authority layer to determine, at runtime, which permissions apply to a user for the
 * Library, Newsgroup and Information services of a group.
 */
public interface CircabcDynamicAuthorityDAO {
  /**
   * Retrieves the permissions granted to a user for the services (Library, Newsgroup, Information)
   * of the specified group.
   *
   * @param groupNodeRef the Alfresco node reference of the group whose permissions are queried
   * @param userName the identifier of the user whose permissions are resolved
   * @return the list of {@link CircabcPermission} entries applicable to the user for the group;
   *     may be empty if no permission is granted
   */
  List<CircabcPermission> getGroupPermission(
    String groupNodeRef,
    String userName
  );

  /**
   * Determines whether the given user is a category administrator for the specified group.
   *
   * @param groupNodeRef the Alfresco node reference of the group to check against
   * @param userName the identifier of the user whose administrative role is checked
   * @return {@code true} if the user is a category administrator for the group, {@code false}
   *     otherwise
   */
  boolean isCategoryAdmin(String groupNodeRef, String userName);
}
