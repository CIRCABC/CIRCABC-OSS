package io.swagger.util;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Authorization helper service used by the CIRCABC REST layer to answer
 * questions about the <em>currently authenticated user</em>.
 *
 * <p>The service centralizes the permission and role checks that webscript
 * endpoints perform before executing an operation. It combines several
 * sources of truth:
 *
 * <ul>
 *   <li>Alfresco's low-level {@link PermissionService} to test raw ACL
 *       permissions (Read, Write, Delete, CheckIn, ...) on a node.</li>
 *   <li>The CIRCABC domain roles exposed through {@link CircabcService}
 *       (CIRCABC admin, category admin, interest group membership, ...).</li>
 *   <li>The CIRCABC permission enums ({@link LibraryPermissions},
 *       {@link NewsGroupPermissions}, {@link InformationPermissions},
 *       {@link EventPermissions}, {@link DirectoryPermissions}) that map to
 *       named Alfresco permissions per service area.</li>
 *   <li>{@link LdapUserService} / {@link PersonService} to resolve user
 *       identity attributes such as e-mail address or external/internal
 *       domain.</li>
 * </ul>
 *
 * <p>The "current user" is always the fully authenticated user obtained from
 * {@link AuthenticationUtil}. Most methods simply return a boolean answer;
 * the {@code throwIf*} methods instead raise an {@link AccessDeniedException}
 * when the check fails, which lets callers guard an endpoint with a single
 * call.
 */
public class CurrentUserPermissionCheckerService {

  /** Alfresco service used to test raw ACL permissions on nodes. */
  @Autowired
  private PermissionService permissionService;

  /** Alfresco service used to read node types, aspects and properties. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to resolve authorities and admin/guest status. */
  @Autowired
  private AuthorityService authorityService;

  /** CIRCABC domain service exposing role and membership checks. */
  @Autowired
  private CircabcService circabcService;

  /** CIRCABC configuration, e.g. whether LDAP authentication is enabled. */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * User directory service used to resolve LDAP user data. The
   * {@code ldapOrLuceneUserService} qualifier selects the LDAP-backed
   * implementation when LDAP is enabled and a Lucene-backed fallback
   * otherwise.
   */
  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  /** Alfresco service used to resolve the person node of a user. */
  @Autowired
  private PersonService personService;

  /**
   * Check whether the given user is an external (non-internal) user.
   *
   * @param userName the user name to check
   * @return {@code true} if the user is flagged as external, {@code false}
   *         otherwise
   */
  public boolean isExternalUser(String userName) {
    return circabcService.isExternalUser(userName);
  }

  /**
   * if current user is not CIRCABC admin it will throw AccessDenied exception
   *
   * @return true if current user is CIRCABC admin
   * @throws AccessDeniedException if the current user is not a CIRCABC admin
   */
  public boolean throwIfNotCircabcAdmin() {
    return throwAccessDeniedIfFalse(isCircabcAdmin(), "Not Circabc admin");
  }

  /**
   * if current user is not CIRCABC admin it will throw AccessDenied exception
   *
   * @param categoryId id of category
   * @return true if current user is category admin
   * @throws AccessDeniedException if the current user is not a category admin
   */
  public boolean throwIfNotCategoryAdmin(String categoryId) {
    return throwAccessDeniedIfFalse(
      isCategoryAdmin(categoryId),
      "Not Category admin"
    );
  }

  /**
   * Throw AccessDenied exception if current user does not have rights to access
   * interest group
   *
   * @param interestGroupId id of interest group
   * @return tru if user has rights otherwise throw exception
   * @throws AccessDeniedException if the current user cannot access the
   *                              interest group
   */
  public boolean throwIfCanNotAccessInterestGroup(String interestGroupId) {
    return throwAccessDeniedIfFalse(
      canAccessInterestGroup(interestGroupId),
      "Can not access interest group"
    );
  }

  /**
   * Throw exception if current userName is not equal with passed userName This
   * method is useful to
   * check if user that is passed in url is equal to authenticated user
   *
   * @param userName userName that we want to check
   * @return true if exception is not thrown
   * @throws AccessDeniedException if the given userName is not the current user
   */
  public boolean throwIfNotCurrentUser(String userName) {
    return throwAccessDeniedIfFalse(
      isCurrentUserEqualTo(userName),
      userName + "is not current user"
    );
  }

  private Boolean throwAccessDeniedIfFalse(Boolean value, String message) {
    if (Boolean.FALSE.equals(value)) {
      throw new AccessDeniedException(message);
    }
    return true;
  }

  /**
   * Check if current user is guest
   *
   * @return true if user is alfresco guest false otherwise
   */
  public boolean isGuest() {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    return authorityService.isGuestAuthority(userName);
  }

  /**
   * Check if current user name is equal to passed parameter
   *
   * @param userName user name to check
   * @return true if userNames are equal otherwise false
   */
  public boolean isCurrentUserEqualTo(String userName) {
    return AuthenticationUtil.getFullyAuthenticatedUser().equals(userName);
  }

  /**
   * Check if current user has rights to access interest group
   *
   * @param interestGroupId interest group id (uuid of node ref)
   */
  public boolean canAccessInterestGroup(String interestGroupId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(interestGroupId);
    return canUserAccessInterestGroup(nodeRef);
  }

  /**
   * ,Check if current user is category admin
   *
   * @param categoryId id of category
   * @return true is current user is category admin otherwise false
   */
  public boolean isCategoryAdmin(String categoryId) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    return isCategoryAdmin(categoryRef);
  }

  private boolean isCategoryAdmin(NodeRef categoryNodeRef) {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    return circabcService.isCategoryAdmin(categoryNodeRef, userName);
  }

  /**
   * Check if current user is circabc admin
   *
   * @return true if current user is circabc admin otherwise false
   */
  public boolean isCircabcAdmin() {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    return circabcService.isCircabcAdmin(userName);
  }

  /**
   * Check if current user is alfresco admin
   *
   * @return true if current user is alfresco admin
   */
  public boolean isAlfrescoAdmin() {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    return authorityService.isAdminAuthority(userName);
  }

  /**
   * Check whether the current user is a directory admin, a category admin or
   * a CIRCABC admin.
   *
   * @return {@code true} if the current user holds any of those admin roles,
   *         {@code false} otherwise
   */
  public boolean isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin() {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    return circabcService.isUserDirAdminOrCategoryAdminOrCircabcAdmin(userName);
  }

  private boolean canUserAccessInterestGroup(NodeRef interestGroupNodeRef) {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();

    InterestGroupResult interestGroup = circabcService.getInterestGroup(
      interestGroupNodeRef
    );
    if (userName.equals("guest")) {
      return interestGroup.getIsPublic();
    } else if (Boolean.TRUE.equals(interestGroup.getIsRegistered())) {
      return true;
    } else if (circabcService.isUserMember(interestGroupNodeRef, userName)) {
      return true;
    } else {
      return circabcService.isCategoryAdminOfInterestGroup(
        interestGroupNodeRef,
        userName
      );
    }
  }

  /**
   * Check if current user has any of alfresco permissions
   *
   * @param nodeId              id of node to check
   * @param alfrescoPermissions alfresco permissions to check for example
   *                            Read,Write,Delete,AddChildren
   * @return true if user has any of Alfresco permission on given node otherwise
   *         false
   */
  public boolean hasAnyOfAlfrescoPermission(
    String nodeId,
    String... alfrescoPermissions
  ) {
    boolean result = false;
    for (String alfrescoPermission : alfrescoPermissions) {
      result = hasAlfrescoPermission(nodeId, alfrescoPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Return tru if current user has read permission on node
   */
  public boolean hasAlfrescoReadPermission(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, PermissionService.READ)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Return true if current user has write permission on node
   */
  public boolean hasAlfrescoWritePermission(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, PermissionService.WRITE)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Return tru if current user has delete permission on node
   */
  public boolean hasAlfrescoDeletePermission(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, PermissionService.DELETE)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Return tru if current user has add children permission on node
   */
  public boolean hasAlfrescoAddChildrenPermission(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, PermissionService.ADD_CHILDREN)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check if current user has any of library permissions
   *
   * @param nodeId             id of node to check
   * @param libraryPermissions library permissions to check for example
   *                           Read,Write,Delete,AddChildren
   * @return true if user has any of library permission on given node otherwise
   *         false
   */
  public boolean hasAnyOfLibraryPermission(
    String nodeId,
    LibraryPermissions... libraryPermissions
  ) {
    boolean result = false;
    for (LibraryPermissions libraryPermission : libraryPermissions) {
      result = hasLibraryPermission(nodeId, libraryPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Check if the current id is a post of a document located in the Library
   *
   * @param id node id
   */
  public boolean isDocumentPost(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return (
      ForumModel.TYPE_POST.equals(nodeService.getType(nodeRef)) &&
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    );
  }

  /**
   * Check if current user has any of newsgroups permissions
   *
   * @param nodeId               id of node to check
   * @param newsGroupPermissions newsgroups permissions to check for example
   *                             Read,Write,Delete,AddChildren
   * @return true if user has any of library permission on given node otherwise
   *         false
   */
  public boolean hasAnyOfNewsGroupPermission(
    String nodeId,
    NewsGroupPermissions... newsGroupPermissions
  ) {
    boolean result = false;
    for (NewsGroupPermissions newsGroupPermission : newsGroupPermissions) {
      result = hasNewsGroupPermission(nodeId, newsGroupPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Check if current user has any of information permissions
   *
   * @param nodeId                 id of node to check
   * @param informationPermissions information permissions to check f
   * @return true if user has any of information permission on given node
   *         otherwise false
   */
  public boolean hasAnyOfInformationPermission(
    String nodeId,
    InformationPermissions... informationPermissions
  ) {
    boolean result = false;
    for (InformationPermissions informationPermission : informationPermissions) {
      result = hasInformationPermission(nodeId, informationPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Check if current user has any of event permissions
   *
   * @param nodeId           id of node to check
   * @param eventPermissions event permissions to check
   * @return true if user has any of event permission on given node otherwise
   *         false
   */
  public boolean hasAnyOfEventPermission(
    String nodeId,
    EventPermissions... eventPermissions
  ) {
    boolean result = false;
    for (EventPermissions eventPermission : eventPermissions) {
      result = hasEventPermission(nodeId, eventPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Check if current user has any of directory permissions
   *
   * @param nodeId               id of node to check
   * @param directoryPermissions directory permissions to check
   * @return true if user has any of directory permission on given node otherwise
   *         false
   */
  public boolean hasAnyOfDirectoryPermission(
    String nodeId,
    DirectoryPermissions... directoryPermissions
  ) {
    boolean result = false;
    for (DirectoryPermissions directoryPermission : directoryPermissions) {
      result = hasDirectoryPermission(nodeId, directoryPermission);
      if (result) {
        break;
      }
    }
    return result;
  }

  private boolean hasLibraryPermission(
    String nodeId,
    LibraryPermissions libraryPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, libraryPermission.toString())
      .equals(AccessStatus.ALLOWED);
  }

  private boolean hasInformationPermission(
    String nodeId,
    InformationPermissions informationPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, informationPermission.toString())
      .equals(AccessStatus.ALLOWED);
  }

  private boolean hasEventPermission(
    String nodeId,
    EventPermissions eventPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, eventPermission.toString())
      .equals(AccessStatus.ALLOWED);
  }

  private boolean hasNewsGroupPermission(
    String nodeId,
    NewsGroupPermissions newsGroupPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, newsGroupPermission.toString())
      .equals(AccessStatus.ALLOWED);
  }

  private boolean hasDirectoryPermission(
    String nodeId,
    DirectoryPermissions directoryPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, directoryPermission.toString())
      .equals(AccessStatus.ALLOWED);
  }

  private boolean hasAlfrescoPermission(
    String nodeId,
    String alfrescoPermission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    return permissionService
      .hasPermission(nodeRef, alfrescoPermission)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check whether the current user is a Library administrator of the given
   * interest group. The node must be an interest group root and the current
   * user must hold the {@link LibraryPermissions#LIBADMIN} permission on its
   * "Library" child.
   *
   * @param igId id of the interest group node
   * @return {@code true} if the current user is Library admin of the interest
   *         group, {@code false} otherwise
   */
  public boolean isInterestGroupLibAdmin(String igId) {
    NodeRef igRef = Converter.createNodeRefFromId(igId);

    if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
      NodeRef libRef = nodeService.getChildByName(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      );
      return (
        libRef != null &&
        hasAnyOfLibraryPermission(libRef.getId(), LibraryPermissions.LIBADMIN)
      );
    }

    return false;
  }

  /**
   * Check whether the current user is a Newsgroups administrator of the given
   * interest group. The node must be an interest group root and the current
   * user must hold the {@link NewsGroupPermissions#NWSADMIN} permission on its
   * "Newsgroups" child.
   *
   * @param igId id of the interest group node
   * @return {@code true} if the current user is Newsgroups admin of the
   *         interest group, {@code false} otherwise
   */
  public boolean isInterestGroupNewsAdmin(String igId) {
    NodeRef igRef = Converter.createNodeRefFromId(igId);

    if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
      NodeRef newsRef = nodeService.getChildByName(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      );
      return (
        newsRef != null &&
        hasAnyOfNewsGroupPermission(
          newsRef.getId(),
          NewsGroupPermissions.NWSADMIN
        )
      );
    }

    return false;
  }

  /**
   * Check whether the current user is a Directory administrator of the given
   * interest group. The node must be an interest group root and the current
   * user must hold the {@link DirectoryPermissions#DIRADMIN} permission on it.
   *
   * @param igId id of the interest group node
   * @return {@code true} if the current user is Directory admin of the
   *         interest group, {@code false} otherwise
   */
  public boolean isInterestGroupDirAdmin(String igId) {
    NodeRef igRef = Converter.createNodeRefFromId(igId);

    if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
      return hasAnyOfDirectoryPermission(
        igRef.getId(),
        DirectoryPermissions.DIRADMIN
      );
    }

    return false;
  }

  /**
   * Check whether the current user owns the working copy of the given node.
   *
   * @param id id of the node to check
   * @return {@code true} if the node has the working-copy aspect and its
   *         working-copy owner matches the current run-as user, {@code false}
   *         otherwise
   */
  public boolean isWorkingCopyOwner(String id) {
    boolean result = false;
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    boolean isWorkingCopy = nodeService.hasAspect(
      nodeRef,
      ContentModel.ASPECT_WORKING_COPY
    );
    if (isWorkingCopy) {
      String workingCopyOwner = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_WORKING_COPY_OWNER
      );
      result = workingCopyOwner.equals(AuthenticationUtil.getRunAsUser());
    }
    return result;
  }

  /**
   * Check whether the current user may cancel the checkout of the given node.
   *
   * @param id id of the node to check
   * @return {@code true} if the {@link PermissionService#CANCEL_CHECK_OUT}
   *         permission is allowed, {@code false} otherwise
   */
  public boolean hasAlfCancelCheckoutPermission(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return permissionService
      .hasPermission(nodeRef, PermissionService.CANCEL_CHECK_OUT)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check whether the current user may check in the given node.
   *
   * @param id id of the node to check
   * @return {@code true} if the {@link PermissionService#CHECK_IN} permission
   *         is allowed, {@code false} otherwise
   */
  public boolean hasAlfCheckinPermission(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return permissionService
      .hasPermission(nodeRef, PermissionService.CHECK_IN)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check whether the current user may check out the given node.
   *
   * @param id id of the node to check
   * @return {@code true} if the {@link PermissionService#CHECK_OUT} permission
   *         is allowed, {@code false} otherwise
   */
  public boolean hasAlfCheckoutPermission(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return permissionService
      .hasPermission(nodeRef, PermissionService.CHECK_OUT)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check whether the current user is an administrator of the given Interest
   * Group.
   *
   * <p>An Interest Group administrator holds every service admin permission at
   * once. These permissions are <em>not</em> all granted on the IG root node:
   * only the directory admin permission ({@code DirAdmin}) is set on the IG
   * root itself, while the library, events, information and newsgroup admin
   * permissions are granted on the respective service root nodes ("Library",
   * "Events", "Information" and "Newsgroups"), which are direct children of the
   * IG root. This mirrors how profile permissions are applied in
   * {@code ProfilesApiImpl}. Each service permission is therefore checked on
   * the node where it is actually granted. A missing service node is skipped
   * so IGs that do not expose a given service are not wrongly rejected.
   *
   * @param id id of the Interest Group root node to check
   * @return {@code true} if the node exists and the current user holds the
   *         directory admin permission on the IG root and the corresponding
   *         admin permission on every present service root, {@code false}
   *         otherwise
   */
  public boolean isGroupAdmin(String id) {
    NodeRef igRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(igRef)) {
      return false;
    }

    // Directory admin is granted on the IG root node itself.
    if (
      !hasAnyOfDirectoryPermission(igRef.getId(), DirectoryPermissions.DIRADMIN)
    ) {
      return false;
    }

    // The library, events, information and newsgroup admin permissions are
    // granted on the respective service root nodes (children of the IG root),
    // not on the IG root itself.
    NodeRef libraryRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Library"
    );
    NodeRef eventsRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Events"
    );
    NodeRef informationRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Information"
    );
    NodeRef newsgroupsRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Newsgroups"
    );

    return (
      (libraryRef == null ||
        hasAnyOfLibraryPermission(
          libraryRef.getId(),
          LibraryPermissions.LIBADMIN
        )) &&
      (eventsRef == null ||
        hasAnyOfEventPermission(
          eventsRef.getId(),
          EventPermissions.EVEADMIN
        )) &&
      (informationRef == null ||
        hasAnyOfInformationPermission(
          informationRef.getId(),
          InformationPermissions.INFADMIN
        )) &&
      (newsgroupsRef == null ||
        hasAnyOfNewsGroupPermission(
          newsgroupsRef.getId(),
          NewsGroupPermissions.NWSADMIN
        ))
    );
  }

  /**
   * Check whether the current user may take ownership of the given node.
   *
   * @param id id of the node to check
   * @return {@code true} if the {@link PermissionService#TAKE_OWNERSHIP}
   *         permission is allowed, {@code false} otherwise
   */
  public boolean hasTakeOwnershipPermission(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return permissionService
      .hasPermission(nodeRef, PermissionService.TAKE_OWNERSHIP)
      .equals(AccessStatus.ALLOWED);
  }

  /**
   * Check whether the current user is granted the given permission on a node,
   * either directly, through one of the groups (authorities) the user belongs
   * to, or by holding the "Write" permission as a fallback.
   *
   * @param id         id of the node to check
   * @param permission name of the permission to look for in the node's set
   *                   permissions
   * @return {@code true} if the node exists and the permission is granted to
   *         the user or one of its groups (or the user has Write access),
   *         {@code false} otherwise
   */
  public boolean verifyMemberPermission(String id, String permission) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(nodeRef)) {
      return false;
    }

    String userName = AuthenticationUtil.getRunAsUser();
    Set<String> authorities = authorityService.getAuthorities();
    for (AccessPermission ac : permissionService.getAllSetPermissions(
      nodeRef
    )) {
      if (ac.getAuthorityType() == AuthorityType.USER) {
        if (
          ac.getAuthority().equals(userName) &&
          ac.getPermission().equals(permission) &&
          ac.getAccessStatus().name().equals("ALLOWED")
        ) {
          return true;
        }
      } else if (
        ac.getAuthorityType() == AuthorityType.GROUP &&
        authorities.contains(ac.getAuthority()) &&
        ac.getPermission().equals(permission)
      ) {
        return true;
      }
    }
    return (
      permissionService
        .hasPermission(nodeRef, "Write")
        .equals(AccessStatus.ALLOWED)
    );
  }

  /**
   * Check whether the current user is an external user, based on their LDAP
   * domain. Only meaningful when LDAP authentication is enabled and the user
   * is not the guest user.
   *
   * @return {@code true} if LDAP is enabled, the user is not guest, and the
   *         resolved LDAP domain equals "external"; {@code false} otherwise
   */
  public boolean isExternalUser() {
    if (!circabcConfig.isUseLDAP() || isGuest()) {
      return false;
    }
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    CircabcUserDataBean user = ldapUserService.getLDAPUserDataByUid(userName);
    return user != null && "external".equalsIgnoreCase(user.getDomain());
  }

  /**
   * Check whether the current user's e-mail address matches the given address.
   *
   * @param emailAddress the e-mail address to compare against, case-insensitive
   * @return {@code true} if the current user's e-mail equals the given address,
   *         {@code false} otherwise
   */
  public boolean isCurrentUserEmailEqualTo(String emailAddress) {
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    NodeRef personRef = personService.getPerson(userName);
    String email = String.valueOf(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    );
    return email.equalsIgnoreCase(emailAddress);
  }
}
