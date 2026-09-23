package eu.europa.ec.digit.circabc.rest.service.user;

import java.io.File;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for reading and updating CIRCABC user (person) details.
 *
 * <p>Implementations expose the business operations used by the REST layer to query a person's
 * profile information and to manage the person's avatar. This includes:
 *
 * <ul>
 *   <li>Retrieving the full set of user details for a person, either by node reference or by user
 *       name (see {@link #getUserDetails(NodeRef)} and {@link #getUserDetails(String)}).
 *   <li>Managing a person's avatar: uploading a new image, reusing an existing repository file,
 *       removing a custom avatar to fall back to the default, and resolving the effective or
 *       default avatar (see {@link #updateAvatar(NodeRef, String, File)},
 *       {@link #updateAvatar(NodeRef, NodeRef)}, {@link #removeAvatar(NodeRef)},
 *       {@link #getAvatar(NodeRef)} and {@link #getDefaultAvatar()}).
 *   <li>Persisting modified preferences and properties of a person (see
 *       {@link #updateUserDetails(NodeRef, UserDetails)}).
 * </ul>
 *
 * <p>Avatar changes are handled exclusively through the dedicated avatar methods and are not
 * covered by {@link #updateUserDetails(NodeRef, UserDetails)}.
 */
public interface UserDetailsService {
  /**
   * Get all details of the given person.
   *
   * @param person The person to query
   * @return A person POJO
   */
  UserDetails getUserDetails(final NodeRef person);

  /**
   * Get all details of the given person identified by its user name.
   *
   * @param username The username to query
   * @return A person POJO
   */
  UserDetails getUserDetails(final String username);

  /**
   * Upload an image file and set it as avatar for the given person.
   *
   * @param person         The person to update
   * @param avatarFileName The name of the avatar file
   * @param avatarFile     The avatar to upload
   * @return The created avatar node reference
   */
  NodeRef updateAvatar(
    final NodeRef person,
    final String avatarFileName,
    final File avatarFile
  );

  /**
   * Update the avatar of a person with an existing repository file.
   *
   * @param person   The person to update
   * @param imageRef The existing repository file
   */
  void updateAvatar(final NodeRef person, final NodeRef imageRef);

  /**
   * Remove the user defined avatar to use the default one.
   *
   * @param person The person to update
   */
  void removeAvatar(final NodeRef person);

  /**
   * Return the avatar of the given person.
   *
   * @param person The person to query
   * @return The configured or default avatar node reference
   */
  NodeRef getAvatar(final NodeRef person);

  /**
   * Return the default avatar.
   *
   * @return The default avatar node reference
   */
  NodeRef getDefaultAvatar();

  /**
   * Update modified user details (prefernces and properties).
   * <b>Warning:</b>
   * <p>
   * This method doen'st support avatar changes. Use updateAvatar and removeAvatar methods of this
   * same class.	 *
   * </p>
   *
   * @param personRef   The person node reference (Only used for security check, MUST be equals to
   *                    userDetails.getNodeRef())
   * @param userDetails The details of the person to update.
   *     <p>See also {@code UserDetailsBusinessSrv#updateAvatar(NodeRef, String, File)}, {@code
   *     UserDetailsBusinessSrv#updateAvatar(NodeRef, NodeRef)} and {@code
   *     UserDetailsBusinessSrv#removeAvatar(NodeRef)}.
   * <p>

   */
  void updateUserDetails(
    final NodeRef personRef,
    final UserDetails userDetails
  );
}
