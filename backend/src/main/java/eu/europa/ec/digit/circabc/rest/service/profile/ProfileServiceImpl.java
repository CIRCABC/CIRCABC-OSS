package eu.europa.ec.digit.circabc.rest.service.profile;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.model.db.Profile;
import io.swagger.model.db.UserWithProfile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ProfileService} backed by the Alfresco repository.
 *
 * <p>A CIRCABC profile represents a named permission role within an Interest Group. In the
 * repository, each Interest Group node holds a set of child "profile" nodes (linked through the
 * {@code circaIGRootProfileAssoc} association). Every profile node in turn holds child "service"
 * nodes (linked through {@code circaIGRootServiceAssoc}) that carry the permission set granted for
 * each CIRCABC service (Directory, Information, Library, Event, Newsgroup and Visibility).
 *
 * <p>This class reads those node structures and their properties to assemble {@link Profile}
 * domain objects, to resolve the members (authorities) belonging to a profile, and to determine
 * the visibility settings of an Interest Group. It also aggregates invited-user profile
 * information through the {@link CircabcService} and {@link CircabcDaoServiceImpl} collaborators.
 */
public class ProfileServiceImpl implements ProfileService {

  /** Reserved profile/group name used to represent the anonymous (guest) user. */
  private static final String GUEST = "guest";

  /**
   * Prefix of the CIRCABC content-model local names used to build the qualified names of the
   * profile/service associations and properties (e.g. {@code circaIGRootProfileAssoc}).
   */
  private static final String CIRCA_IG_ROOT = "circaIGRoot";

  /** Alfresco service used to read node properties and traverse child associations. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to resolve the users contained in a profile's authority group. */
  @Autowired
  private AuthorityService authorityService;

  /** CIRCABC business service used to retrieve the filtered users of an Interest Group. */
  @Autowired
  private CircabcService circabcService;

  /** CIRCABC DAO service used to obtain supporting reference data such as Alfresco locales. */
  @Autowired
  private CircabcDaoServiceImpl circabcDaoService;

  /** Logger for this service. */
  private static final Log logger = LogFactory.getLog(ProfileServiceImpl.class);

  /**
   * {@inheritDoc}
   *
   * <p>Reads every profile child node of the given repository node and converts each one into a
   * {@link Profile} domain object, populating its {@code nodeRef} with the profile node reference.
   */
  @Override
  public List<Profile> getProfiles(NodeRef nodeRef) {
    List<Profile> profiles = new ArrayList<>();
    List<NodeRef> profileNodes = getProfileNodesList(nodeRef);
    Profile profile;
    for (final NodeRef profileNodeRef : profileNodes) {
      profile = createProfile(profileNodeRef);
      profile.setNodeRef(profileNodeRef.toString());
      profiles.add(profile);
    }

    return profiles;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Scans all profile child nodes of the given node and returns the one whose name matches
   * {@code profileName} (case-insensitive). Returns {@code null} when no profile matches.
   */
  @Override
  public Profile getProfile(NodeRef nodeRef, String profileName) {
    Profile result = null;
    List<NodeRef> profileNodes = getProfileNodesList(nodeRef);
    for (final NodeRef profileNodeRef : profileNodes) {
      Profile profile;
      profile = createProfile(profileNodeRef);
      profile.setNodeRef(profileNodeRef.toString());
      if (profile.getName().equalsIgnoreCase(profileName)) {
        result = profile;
      }
    }

    return result;
  }

  private Profile createProfile(final NodeRef profileNodeRef) {
    final Profile profile = new Profile();

    String curentPrefixedProfileGroupName;
    String curentProfileGroupName;
    String profileName;
    boolean isExported = false;
    boolean isImported = false;
    NodeRef importedNodeRef = null;

    Map<String, NodeRef> servicesNodeRef;
    Map<String, Set<String>> servicesPermissions;

    final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(true);
      final Map<QName, Serializable> properties = nodeService.getProperties(
        profileNodeRef
      );

      profile.setId((Long) properties.get(ContentModel.PROP_NODE_DBID));

      profileName = (String) properties.get(getProfileNameQName());

      // get is imported
      try {
        final Serializable value = properties.get(
          ProfileModel.PROP_PROFILE_IMPORTED
        );
        if (value != null) {
          isImported = (Boolean) value;
        }
      } catch (final Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "isImported can't be evaluated on node:" + profileNodeRef,
            e
          );
        }
      }
      profile.setImported(isImported);

      // get imported node ref
      try {
        final Serializable value = properties.get(
          ProfileModel.PROP_PROFILE_IMPORTED_REF
        );
        if (value != null) {
          importedNodeRef = (NodeRef) value;
          profile.setIgFromNodeRef(importedNodeRef.toString());
        }
      } catch (final Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "importedNodeRef can't be evaluated on node:" + profileNodeRef,
            e
          );
        }
      }

      try {
        final Serializable value = properties.get(
          ProfileModel.PROP_PROFILE_EXPORTED
        );
        if (value != null) {
          isExported = (Boolean) value;
        }
      } catch (final Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "isExported can't be evaluated on node:" + profileNodeRef,
            e
          );
        }
      }
      profile.setExported(isExported);

      profile.setName(profileName);

      if (logger.isTraceEnabled()) {
        logger.trace("get profileGroupName on Node:" + profileNodeRef);
      }
      curentPrefixedProfileGroupName = (String) properties.get(
        getProfileGroupNameQName()
      );

      if (!GUEST.equals(curentPrefixedProfileGroupName)) {
        curentProfileGroupName = curentPrefixedProfileGroupName.substring(
          "GROUP_".length(),
          curentPrefixedProfileGroupName.length()
        );
        profile.setAlfrescoGroup(curentProfileGroupName);
      } else {
        profile.setAlfrescoGroup(curentPrefixedProfileGroupName);
      }

      servicesNodeRef = getServiceNodes(profileNodeRef);
      servicesPermissions = getServicePermissionSet(
        profileNodeRef,
        servicesNodeRef
      );

      profile.setDirectoryPermission(directoryPermision(servicesPermissions));
      profile.setInformationPermission(
        informationPermision(servicesPermissions)
      );
      profile.setLibraryPermission(libraryPermision(servicesPermissions));
      profile.setEventPermission(eventPermision(servicesPermissions));
      profile.setNewsgroupPermission(newsGroupPermision(servicesPermissions));
      profile.setVisible(visible(servicesPermissions));
    } finally {
      MLPropertyInterceptor.setMLAware(wasMLAware);
    }

    return profile;
  }

  private boolean visible(Map<String, Set<String>> servicesPermissions) {
    return servicesPermissions.get("VISIBILITY").contains("Visibility");
  }

  private String newsGroupPermision(
    Map<String, Set<String>> servicesPermissions
  ) {
    return servicesPermissions.get("NEWSGROUP").iterator().next();
  }

  private String eventPermision(Map<String, Set<String>> servicesPermissions) {
    return servicesPermissions.get("EVENT").iterator().next();
  }

  private String libraryPermision(
    Map<String, Set<String>> servicesPermissions
  ) {
    return servicesPermissions.get("LIBRARY").iterator().next();
  }

  private String informationPermision(
    Map<String, Set<String>> servicesPermissions
  ) {
    return servicesPermissions.get("INFORMATION").iterator().next();
  }

  private String directoryPermision(
    Map<String, Set<String>> servicesPermissions
  ) {
    return servicesPermissions.get("DIRECTORY").iterator().next();
  }

  private List<NodeRef> getProfileNodesList(final NodeRef nodeRef) {
    final List<NodeRef> profileNodes = new ArrayList<>();
    final List<ChildAssociationRef> listOfProfilesAssoc =
      nodeService.getChildAssocs(
        nodeRef,
        getProfileAssocQName(),
        RegexQNamePattern.MATCH_ALL
      );
    NodeRef profileNodeRef;
    for (final ChildAssociationRef profileAssoc : listOfProfilesAssoc) {
      profileNodeRef = profileAssoc.getChildRef();
      // Get profileName
      if (logger.isTraceEnabled()) {
        logger.trace("get ProfileName on node:" + profileNodeRef);
      }
      profileNodes.add(profileNodeRef);
    }
    return profileNodes;
  }

  private QName getProfileAssocQName() {
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "ProfileAssoc"
    );
  }

  private QName getProfileNameQName() {
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "ProfileName"
    );
  }

  private QName getProfileGroupNameQName() {
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "ProfileGroupName"
    );
  }

  /**
   * Returns the users assigned to the named profile within the given Interest Group.
   *
   * <p>The profile is located by name among the Interest Group's profile nodes; the users contained
   * in its associated authority group are then resolved recursively. The reserved
   * {@code GROUP_EVERYONE} and {@code guest} groups carry no explicit member list and therefore
   * yield an empty result.
   *
   * @param nodeRef the Interest Group node whose profile is inspected
   * @param profileName the name of the profile whose members are requested
   * @return the set of user authorities assigned to the profile; empty if none or if the profile is
   *     not found
   */
  public final Set<String> getPersonInProfile(
    final NodeRef nodeRef,
    final String profileName
  ) {
    final Set<String> personsInProfile = new HashSet<>();
    final List<NodeRef> profileNodes = getProfileNodesList(nodeRef);
    for (NodeRef profileNodeRef : profileNodes) {
      final String currentProfileName = (String) nodeService.getProperty(
        profileNodeRef,
        getProfileNameQName()
      );
      if (currentProfileName.equals(profileName)) {
        String currentProfileGroupName = (String) nodeService.getProperty(
          profileNodeRef,
          getProfileGroupNameQName()
        );
        if (
          !"GROUP_EVERYONE".equals(currentProfileGroupName) &&
          !GUEST.equals(currentProfileGroupName)
        ) {
          personsInProfile.addAll(
            authorityService.getContainedAuthorities(
              AuthorityType.USER,
              currentProfileGroupName,
              false
            )
          );
        }
        break;
      }
    }
    return personsInProfile;
  }

  private Boolean hasVisibility(NodeRef interestGroup, String groupName) {
    final List<Profile> profiles = getProfiles(interestGroup);
    boolean result = false;

    for (final Profile profileItem : profiles) {
      if (profileItem.getAlfrescoGroup().equals(groupName)) {
        result = profileItem.isVisible();
        break;
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to the visibility lookup using the reserved {@code guest} group name.
   */
  @Override
  public Boolean hasGuestVisibility(NodeRef interestGroup) {
    return hasVisibility(interestGroup, GUEST);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to the visibility lookup using the reserved {@code GROUP_EVERYONE} group name.
   */
  @Override
  public Boolean hasAllCircabcUsersVisibility(NodeRef interestGroup) {
    return hasVisibility(interestGroup, "GROUP_EVERYONE");
  }

  /**
   * Relation name between profile and the service
   */
  private QName getServiceAssocQName() {
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "ServiceAssoc"
    );
  }

  private QName getServiceNameQName() {
    /** type for the serviceName */
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "ServiceName"
    );
  }

  private QName getPermissionSetQName() {
    /** type for the permissionSetQName */
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      CIRCA_IG_ROOT + "PermissionSet"
    );
  }

  private Map<String, NodeRef> getServiceNodes(final NodeRef profileNodeRef) {
    final Map<String, NodeRef> serviceNodes = new HashMap<>();
    final List<ChildAssociationRef> listOfServicesAssoc =
      nodeService.getChildAssocs(
        profileNodeRef,
        getServiceAssocQName(),
        RegexQNamePattern.MATCH_ALL
      );
    NodeRef serviceNodeRef;
    String serviceName;
    for (final ChildAssociationRef serviceAssoc : listOfServicesAssoc) {
      serviceNodeRef = serviceAssoc.getChildRef();
      // Get serviceName
      if (logger.isTraceEnabled()) {
        logger.trace("get ServiceName on ServiceNode:" + serviceNodeRef);
      }
      serviceName = (String) nodeService.getProperty(
        serviceNodeRef,
        getServiceNameQName()
      );
      serviceNodes.put(serviceName, serviceNodeRef);
    }
    return serviceNodes;
  }

  private Map<String, Set<String>> getServicePermissionSet(
    final NodeRef profileNodeRef,
    final Map<String, NodeRef> servicesNodeRef
  ) {
    final Map<String, Set<String>> servicePermissionSet = new HashMap<>();
    Set<String> permissionSet;
    for (final String service : servicesNodeRef.keySet()) {
      permissionSet = getServicePermissionSet(profileNodeRef, service);
      servicePermissionSet.put(service, permissionSet);
    }
    return servicePermissionSet;
  }

  @SuppressWarnings("unchecked")
  private Set<String> getServicePermissionSet(
    final NodeRef profileNodeRef,
    final String serviceName
  ) {
    final NodeRef serviceNodeRef = getServiceNode(profileNodeRef, serviceName);
    if (logger.isTraceEnabled()) {
      logger.trace("get permissionSet on ServiceNode:" + serviceNodeRef);
    }
    Serializable property = nodeService.getProperty(
      serviceNodeRef,
      getPermissionSetQName()
    );
    ArrayList<String> permissionList = null;
    if (property instanceof String) {
      permissionList = new ArrayList<>();
      permissionList.add(property.toString());
    } else {
      permissionList = (ArrayList<String>) property;
    }
    if (permissionList == null) {
      return new HashSet<>();
    }
    return new HashSet<>(permissionList);
  }

  private NodeRef getServiceNode(
    final NodeRef profileNodeRef,
    final String serviceName
  ) {
    final List<ChildAssociationRef> listOfServiceAssoc =
      nodeService.getChildAssocs(
        profileNodeRef,
        getServiceAssocQName(),
        RegexQNamePattern.MATCH_ALL
      );
    NodeRef serviceNodeRef;
    String currentServiceName;
    for (final ChildAssociationRef profileAssoc : listOfServiceAssoc) {
      serviceNodeRef = profileAssoc.getChildRef();
      // Get serviceName
      if (logger.isTraceEnabled()) {
        logger.trace("get ServiceName on ServiceNode:" + serviceNodeRef);
      }
      currentServiceName = (String) nodeService.getProperty(
        serviceNodeRef,
        getServiceNameQName()
      );
      if (currentServiceName.equals(serviceName)) {
        return serviceNodeRef;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Retrieves the filtered users of the Interest Group through the CIRCABC service and builds a
   * {@link Profile} for each one, keyed by the user's login name.
   */
  @Override
  public Map<String, io.swagger.model.db.Profile> getInvitedUsersProfiles(
    NodeRef igRef
  ) {
    List<UserWithProfile> users = circabcService.getFilteredUsers(
      igRef,
      circabcDaoService.getAllAlfrescoLocale().get("en_"),
      "",
      "",
      ""
    );

    long interestGroupID = (long) nodeService.getProperty(
      igRef,
      ContentModel.PROP_NODE_DBID
    );
    Map<String, io.swagger.model.db.Profile> result = new HashMap<>();
    for (UserWithProfile userWithProfile : users) {
      result.put(
        userWithProfile.getUserName(),
        new Profile(
          interestGroupID,
          userWithProfile.getProfileId(),
          userWithProfile.getAlfrescoGroup(),
          userWithProfile.getProfileName(),
          userWithProfile.getProfileTitle(),
          userWithProfile.getDirectoryPermission(),
          userWithProfile.getInformationPermission(),
          userWithProfile.getLibraryPermission(),
          userWithProfile.getNewsgroupPermission(),
          userWithProfile.getEventPermission(),
          userWithProfile.isExported(),
          userWithProfile.isImported(),
          userWithProfile.isVisible(),
          userWithProfile.getProfileNodeRef(),
          userWithProfile.getProfileIgFromNodeRef()
        )
      );
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Iterates over the node's profiles, skipping the reserved {@code guest} and {@code EVERYONE}
   * groups, and returns the name of the first profile whose authority group contains the given
   * authority.
   */
  @Override
  public String getPersonProfile(NodeRef nodeRef, String userAutority) {
    // Get all profiles for the node
    List<Profile> profiles = getProfiles(nodeRef);

    // Iterate through each profile
    for (Profile profile : profiles) {
      String groupName = profile.getAlfrescoGroup();

      // Skip "guest" and "EVERYONE" groups
      if (GUEST.equals(groupName) || "EVERYONE".equals(groupName)) {
        continue;
      }

      // Handle group name prefixing - add "GROUP_" prefix
      String prefixedGroupName = "GROUP_" + groupName;

      // Get all users for this profile group
      Set<String> usersInGroup = authorityService.getContainedAuthorities(
        AuthorityType.USER,
        prefixedGroupName,
        false
      );

      // Check if the specified user is in this group
      if (usersInGroup.contains(userAutority)) {
        return profile.getName();
      }
    }

    // User not found in any profile
    return null;
  }
}
