package eu.europa.ec.digit.circabc.rest.service.profile;

import io.swagger.model.db.Profile;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service that resolves profile (permission role) information for CIRCABC Interest
 * Groups.
 *
 * <p>Profiles represent the named permission roles defined within an Interest Group and control
 * what members are allowed to do. Implementations of this service look up the profiles associated
 * with a given repository node, the members assigned to a profile, and the visibility settings
 * (guest access, all-CIRCABC-users access) configured for an Interest Group.
 */
public interface ProfileService {
  /**
   * Returns all profiles defined for the given node.
   *
   * @param nodeRef the repository node (typically an Interest Group) whose profiles are requested
   * @return the list of profiles associated with the node; empty if none are defined
   */
  List<Profile> getProfiles(NodeRef nodeRef);

  /**
   * Returns a single profile identified by its name within the given node.
   *
   * @param nodeRef the repository node (typically an Interest Group) that owns the profile
   * @param profileName the name of the profile to retrieve
   * @return the matching profile, or {@code null} if no profile with that name exists
   */
  Profile getProfile(NodeRef nodeRef, String profileName);

  /**
   * Returns the authorities (users and/or groups) assigned to the named profile within an Interest
   * Group.
   *
   * @param interestGroup the Interest Group node to inspect
   * @param name the profile name whose members are requested
   * @return the set of authority identifiers assigned to the profile; empty if none
   */
  Set<String> getPersonInProfile(NodeRef interestGroup, String name);

  /**
   * Indicates whether the given Interest Group grants visibility to the guest (anonymous) user.
   *
   * @param interestGroup the Interest Group node to check
   * @return {@code true} if guest visibility is enabled, {@code false} otherwise
   */
  Boolean hasGuestVisibility(NodeRef interestGroup);

  /**
   * Indicates whether the given Interest Group grants visibility to all registered CIRCABC users.
   *
   * @param interestGroup the Interest Group node to check
   * @return {@code true} if all-CIRCABC-users visibility is enabled, {@code false} otherwise
   */
  Boolean hasAllCircabcUsersVisibility(NodeRef interestGroup);

  /**
   * Returns the profiles of users invited to the given Interest Group, keyed by user authority.
   *
   * @param igRef the Interest Group node whose invited users are requested
   * @return a map of user authority to the profile assigned to that invited user
   */
  Map<String, io.swagger.model.db.Profile> getInvitedUsersProfiles(
    NodeRef igRef
  );

  /**
   * Returns the profile name assigned to a specific authority within the given node.
   *
   * @param nodeRef the repository node (typically an Interest Group) to inspect
   * @param userAutority the authority (user or group identifier) whose profile is requested
   * @return the name of the profile assigned to the authority, or {@code null} if none applies
   */
  String getPersonProfile(NodeRef nodeRef, String userAutority);
}
