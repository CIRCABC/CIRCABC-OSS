package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.model.I18nProperty;
import io.swagger.model.Profile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ProfilesApi} providing the business logic for managing Interest
 * Group (IG) profiles in the CIRCABC content model.
 *
 * <p>A profile groups together the permission sets that an Alfresco authority (group) holds over
 * the different services of an Interest Group: directory ({@value #DIRECTORY_KEY}), library
 * ({@value #LIBRARY_KEY}), newsgroups ({@value #NEWSGROUP_KEY}), events ({@value #EVENT_KEY}),
 * information ({@value #INFORMATION_KEY}) and visibility ({@value #VISIBILITY_KEY}). Each profile
 * is stored as a child node under the IG root node, with one service sub-node per service holding
 * the associated permission set.
 *
 * <p>This class handles listing, reading, creating, updating, importing and deleting profiles. On
 * top of persisting the CIRCABC model nodes and properties, it also creates/removes the backing
 * Alfresco authority (group), applies the corresponding Alfresco permissions on the IG service
 * folders, and keeps the special {@code guest}/{@code EVERYONE} (registered) profiles synchronized
 * with each other.
 *
 * <p>Collaborators are injected by Spring via {@link Autowired} and the corresponding
 * getters/setters.
 *
 * @author beaurpi
 */
public class ProfilesApiImpl implements ProfilesApi {

  public static final String GUEST = "guest";
  private static final String VISIBILITY_PERM = "Visibility";
  private static final String DIRECTORY_KEY = "members";
  private static final String LIBRARY_KEY = "library";
  private static final String NEWSGROUP_KEY = "newsgroups";
  private static final String EVENT_KEY = "events";
  private static final String INFORMATION_KEY = "information";
  private static final String VISIBILITY_KEY = "visibility";
  private static final String INF_ACCESS = "InfAccess";
  private static final String INF_NO_ACCESS = "InfNoAccess";
  private static final String LIB_ACCESS = "LibAccess";
  private static final String LIB_NO_ACCESS = "LibNoAccess";
  private static final String DIR_ACCESS = "DirAccess";
  private static final String DIR_NO_ACCESS = "DirNoAccess";
  private static final String EVE_ACCESS = "EveAccess";
  private static final String EVE_NO_ACCESS = "EveNoAccess";
  private static final String NWS_ACCESS = "NwsAccess";
  private static final String NWS_NO_ACCESS = "NwsNoAccess";
  private static final String PROFILE_PREFIX = "circaIGRoot";
  private static final QName DIRECTORY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "DIRECTORY"
  );
  private static final QName VISIBILITY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "VISIBILITY"
  );
  private static final QName LIBRARY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "LIBRARY"
  );
  private static final QName NEWSGROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "NEWSGROUP"
  );
  private static final QName INFORMATION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "INFORMATION"
  );
  private static final QName EVENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "EVENT"
  );
  private static final QName TYPE_IG_ROOT_SERVICE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootService"
  );
  private static final QName PROP_IG_ROOT_SERVICE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootServiceName"
  );

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private ProfileService profileService;

  /**
   * Lists the profiles defined for a given Interest Group, optionally filtered by a search term and
   * by whether the profile's backing group contains any user.
   *
   * @param id the identifier of the Interest Group node whose profiles are requested
   * @param searchQuery an optional case-insensitive term matched against each profile's name and
   *     localized titles; when empty or {@code null} no filtering is applied
   * @param nonEmptyProfiles when {@code true}, only profiles whose backing Alfresco group contains
   *     at least one user are returned
   * @return the matching profiles of the Interest Group
   */
  @Override
  public List<Profile> groupsIdProfilesGet(
    String id,
    String searchQuery,
    boolean nonEmptyProfiles
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    List<Profile> result = new ArrayList<>();

    List<ChildAssociationRef> listOfProfilesAssoc = nodeService.getChildAssocs(
      groupNodeRef,
      getProfileAssocQName(),
      RegexQNamePattern.MATCH_ALL
    );

    for (ChildAssociationRef caRef : listOfProfilesAssoc) {
      NodeRef profileRefTmp = caRef.getChildRef();

      Profile profileTmp = getProfile(profileRefTmp);

      if (
        StringUtils.isEmpty(searchQuery) || matchQuery(searchQuery, profileTmp)
      ) {
        if (nonEmptyProfiles) {
          List<String> names = apiToolBox.getUsersFromGroup(
            profileTmp.getGroupName()
          );
          if (!names.isEmpty()) {
            result.add(profileTmp);
          }
          continue;
        }

        result.add(profileTmp);
      }
    }

    return result;
  }

  /**
   * Loads and converts the profile stored at the given node.
   *
   * @param profileRefTmp the node reference of the profile to read
   * @return the profile model populated from the node's properties and service sub-nodes
   */
  private Profile getProfile(NodeRef profileRefTmp) {
    Map<QName, Serializable> props = nodeService.getProperties(profileRefTmp);
    return convertProfile(profileRefTmp, props);
  }

  private boolean matchQuery(String searchQuery, Profile profileTmp) {
    String queryTmp = ".*" + searchQuery.toLowerCase().trim() + ".*";
    if (profileTmp.getName().toLowerCase().matches(queryTmp)) {
      return true;
    }

    for (Entry<String, String> title : profileTmp.getTitle().entrySet()) {
      if (
        title.getValue() != null &&
        title.getValue().toLowerCase().matches(queryTmp)
      ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Builds a {@link Profile} model from a profile node and its properties, reading the permission
   * set of every service sub-node (directory, information, library, event, newsgroup and
   * visibility) as well as the imported/exported flags and any exported references.
   *
   * @param profileRefTmp the node reference of the profile
   * @param props the already-loaded properties of the profile node
   * @return the fully populated profile model
   */
  @SuppressWarnings("unchecked")
  private Profile convertProfile(
    NodeRef profileRefTmp,
    Map<QName, Serializable> props
  ) {
    Profile profileTmp = new Profile();
    profileTmp.setId(profileRefTmp.getId());
    profileTmp.setName(
      props.get(CircabcModel.PROP_IG_ROOT_PROFILE_NAME).toString()
    );

    Serializable titleObj = props.get(ContentModel.PROP_TITLE);
    if (titleObj instanceof String str) {
      profileTmp.setTitle(Converter.toI18NProperty(str));
    } else if (titleObj instanceof MLText mlText) {
      profileTmp.setTitle(Converter.toI18NProperty(mlText));
    }

    profileTmp.setGroupName(
      props.get(CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME).toString()
    );

    List<ChildAssociationRef> profilesDefinitionAssoc =
      nodeService.getChildAssocs(
        profileRefTmp,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        DIRECTORY
      );
    NodeRef profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();

    List<String> perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    profileTmp.getPermissions().put(DIRECTORY_KEY, perms.get(0));

    profilesDefinitionAssoc = nodeService.getChildAssocs(
      profileRefTmp,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      INFORMATION
    );
    profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();

    perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    profileTmp.getPermissions().put(INFORMATION_KEY, perms.get(0));

    profilesDefinitionAssoc = nodeService.getChildAssocs(
      profileRefTmp,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      LIBRARY
    );
    profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
    perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    profileTmp.getPermissions().put(LIBRARY_KEY, perms.get(0));

    profilesDefinitionAssoc = nodeService.getChildAssocs(
      profileRefTmp,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      EVENT
    );
    profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
    perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    profileTmp.getPermissions().put(EVENT_KEY, perms.get(0));

    profilesDefinitionAssoc = nodeService.getChildAssocs(
      profileRefTmp,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      NEWSGROUP
    );
    profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
    perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    profileTmp.getPermissions().put(NEWSGROUP_KEY, perms.get(0));

    profilesDefinitionAssoc = nodeService.getChildAssocs(
      profileRefTmp,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      VISIBILITY
    );
    profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
    perms = (ArrayList<String>) nodeService.getProperty(
      profileDefTmp,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET
    );
    if (perms != null) {
      profileTmp.getPermissions().put(VISIBILITY_KEY, perms.get(0));
    } else {
      profileTmp.getPermissions().put(VISIBILITY_KEY, VISIBILITY_PERM);
    }

    if (
      props.containsKey(ProfileModel.PROP_PROFILE_IMPORTED) &&
      props.get(ProfileModel.PROP_PROFILE_IMPORTED) != null
    ) {
      profileTmp.setImported(
        (Boolean) props.get(ProfileModel.PROP_PROFILE_IMPORTED)
      );
    } else {
      profileTmp.setImported(false);
    }

    if (
      props.containsKey(ProfileModel.PROP_PROFILE_EXPORTED) &&
      props.get(ProfileModel.PROP_PROFILE_EXPORTED) != null
    ) {
      profileTmp.setExported(
        (Boolean) props.get(ProfileModel.PROP_PROFILE_EXPORTED)
      );
    } else {
      profileTmp.setExported(false);
    }

    if (Boolean.TRUE.equals(profileTmp.getExported())) {
      List<AssociationRef> lRef = nodeService.getTargetAssocs(
        profileRefTmp,
        ProfileModel.ASSOC_PROFILE_IMPORTED_TO
      );
      for (AssociationRef ar : lRef) {
        profileTmp.getExportedRefs().add(ar.getTargetRef().getId());
      }
    }

    return profileTmp;
  }

  private QName getProfileAssocQName() {
    return QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      PROFILE_PREFIX + "ProfileAssoc"
    );
  }

  /**
   * Creates a new profile under the given Interest Group and synchronizes it in the CIRCABC
   * service database.
   *
   * @param groupNodeRef the node reference of the Interest Group that will own the profile
   * @param body the profile to create, including its name, title and per-service permissions
   * @return the created profile as persisted (with generated name and resolved group name)
   */
  @Override
  public Profile groupsIdProfilesPost(NodeRef groupNodeRef, Profile body) {
    Profile profile = createProfileInternal(groupNodeRef, body);
    circabcService.updateProfile(groupNodeRef, profile.getName(), profile);
    return profile;
  }

  /**
   * Performs the actual creation of a profile node and its service sub-nodes under an Interest
   * Group. A unique name is generated, the backing Alfresco group is created (except for the
   * special {@code guest} and {@code EVERYONE} profiles), and the Alfresco permissions are applied
   * on the IG and its service folders.
   *
   * @param groupNodeRef the node reference of the Interest Group that will own the profile
   * @param body the profile to create
   * @return the created profile
   * @throws InvalidNodeRefException if {@code groupNodeRef} is not an Interest Group root node
   */
  private Profile createProfileInternal(NodeRef groupNodeRef, Profile body) {
    if (!nodeService.hasAspect(groupNodeRef, CircabcModel.ASPECT_IGROOT)) {
      throw new InvalidNodeRefException(
        "The node is not a Interest group",
        groupNodeRef
      );
    }

    List<Profile> existingProfiles = groupsIdProfilesGet(
      groupNodeRef.getId(),
      "",
      false
    );

    int attempt = 0;
    generateName(body, attempt);
    while (profileAlreadyExists(body, existingProfiles)) {
      generateName(body, attempt);
      attempt++;
    }

    body.setImported(false);
    body.setExported(false);

    // profile node
    QName profileQName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      body.getName()
    );
    ChildAssociationRef childAssoc = nodeService.createNode(
      groupNodeRef,
      ProfileModel.ASSOC_IG_ROOT_PROFILE,
      profileQName,
      CircabcModel.TYPE_INTEREST_GROUP_PROFILE
    );
    NodeRef profileRef = childAssoc.getChildRef();
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_IG_ROOT_PROFILE_NAME,
      body.getName()
    );
    nodeService.setProperty(
      profileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(body.getTitle())
    );
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_PROFILE_IMPORTED,
      false
    );
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_PROFILE_EXPORTED,
      false
    );
    nodeService.addAspect(
      profileRef,
      CircabcModel.ASPECT_PROFILE_IMPORTABLE,
      null
    );

    ChildAssociationRef dirAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      DIRECTORY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef directoryRef = dirAssoc.getChildRef();
    ArrayList<String> dirPerms = new ArrayList<>();
    dirPerms.add(body.getPermissions().get(DIRECTORY_KEY));
    nodeService.setProperty(
      directoryRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      dirPerms
    );
    nodeService.setProperty(
      directoryRef,
      PROP_IG_ROOT_SERVICE_NAME,
      DIRECTORY.getLocalName()
    );

    ChildAssociationRef visAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      VISIBILITY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef visibilityRef = visAssoc.getChildRef();
    ArrayList<String> visPerms = new ArrayList<>();
    if (body.getPermissions().containsKey(VISIBILITY_KEY)) {
      String visbilityValue = body.getPermissions().get(VISIBILITY_KEY);
      visPerms.add(visbilityValue);
    } else {
      visPerms.add(VISIBILITY_PERM);
    }
    nodeService.setProperty(
      visibilityRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      visPerms
    );
    nodeService.setProperty(
      visibilityRef,
      PROP_IG_ROOT_SERVICE_NAME,
      VISIBILITY.getLocalName()
    );

    ChildAssociationRef libAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      LIBRARY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef libraryRef = libAssoc.getChildRef();
    ArrayList<String> libPerms = new ArrayList<>();
    libPerms.add(body.getPermissions().get(LIBRARY_KEY));
    nodeService.setProperty(
      libraryRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      libPerms
    );
    nodeService.setProperty(
      libraryRef,
      PROP_IG_ROOT_SERVICE_NAME,
      LIBRARY.getLocalName()
    );

    ChildAssociationRef nwsAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      NEWSGROUP,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef newsgroupRef = nwsAssoc.getChildRef();
    ArrayList<String> nwsPerms = new ArrayList<>();
    nwsPerms.add(body.getPermissions().get(NEWSGROUP_KEY));
    nodeService.setProperty(
      newsgroupRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      nwsPerms
    );
    nodeService.setProperty(
      newsgroupRef,
      PROP_IG_ROOT_SERVICE_NAME,
      NEWSGROUP.getLocalName()
    );

    ChildAssociationRef evtAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      EVENT,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef eventRef = evtAssoc.getChildRef();
    ArrayList<String> evtPerms = new ArrayList<>();
    evtPerms.add(body.getPermissions().get(EVENT_KEY));
    nodeService.setProperty(
      eventRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      evtPerms
    );
    nodeService.setProperty(
      eventRef,
      PROP_IG_ROOT_SERVICE_NAME,
      EVENT.getLocalName()
    );

    ChildAssociationRef infAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      INFORMATION,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef informationRef = infAssoc.getChildRef();
    ArrayList<String> infPerms = new ArrayList<>();
    infPerms.add(body.getPermissions().get(INFORMATION_KEY));
    nodeService.setProperty(
      informationRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      infPerms
    );
    nodeService.setProperty(
      informationRef,
      PROP_IG_ROOT_SERVICE_NAME,
      INFORMATION.getLocalName()
    );

    switch (body.getName()) {
      case GUEST:
        nodeService.setProperty(
          profileRef,
          CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME,
          GUEST
        );
        break;
      case "EVERYONE":
        nodeService.setProperty(
          profileRef,
          CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME,
          "GROUP_EVERYONE"
        );
        break;
      default:
        String igInvitedGroupName =
          "GROUP_" +
          nodeService
            .getProperty(
              groupNodeRef,
              CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP
            )
            .toString();
        String newAlfGroupName = body.getName() + "--" + GUID.generate();

        String finalAlfGroupName = AuthenticationUtil.runAs(
          () ->
            authorityService.createAuthority(
              AuthorityType.GROUP,
              newAlfGroupName
            ),
          AuthenticationUtil.getAdminUserName()
        );
        AuthenticationUtil.runAs(
          () -> {
            authorityService.addAuthority(
              igInvitedGroupName,
              finalAlfGroupName
            );
            return null;
          },
          AuthenticationUtil.getAdminUserName()
        );
        nodeService.setProperty(
          profileRef,
          CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME,
          finalAlfGroupName
        );
        permissionService.setPermission(
          groupNodeRef,
          finalAlfGroupName,
          VISIBILITY_PERM,
          true
        );
        break;
    }

    Profile createdProfile = getProfile(profileRef);
    applyDirectoryPermissions(profileRef, createdProfile);
    applyServicePermissions(profileRef, createdProfile, "Events", EVENT_KEY);
    applyServicePermissions(
      profileRef,
      createdProfile,
      "Information",
      INFORMATION_KEY
    );
    applyServicePermissions(profileRef, createdProfile, "Library", LIBRARY_KEY);
    applyServicePermissions(
      profileRef,
      createdProfile,
      "Newsgroups",
      NEWSGROUP_KEY
    );

    return createdProfile;
  }

  private boolean profileAlreadyExists(
    Profile body,
    List<Profile> existingProfiles
  ) {
    boolean result = false;

    for (Profile p : existingProfiles) {
      if (p.getName().equals(body.getName())) {
        result = true;
        break;
      }
    }

    return result;
  }

  private void generateName(Profile body, int attempt) {
    String candidate = body.getName();

    if ("".equals(body.getName()) || body.getName() == null) {
      I18nProperty title = body.getTitle();
      if (title.containsKey(Locale.ENGLISH.getLanguage())) {
        candidate = title.get(Locale.ENGLISH.getLanguage());
      } else if (!title.values().isEmpty()) {
        candidate = title.values().iterator().next();
      }
    }

    candidate = candidate.trim();

    if (attempt > 0) {
      candidate = candidate + attempt;
    }

    body.setName(candidate);
  }

  /**
   * @return the nodeService
   */
  public NodeService getNodeService() {
    return nodeService;
  }

  /**
   * @param nodeService the nodeService to set
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * @return the authorityService
   */
  public AuthorityService getAuthorityService() {
    return authorityService;
  }

  /**
   * @param authorityService the authorityService to set
   */
  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  /**
   * Deletes a profile node and, unless it is an imported profile, its backing Alfresco authority,
   * then removes it from the CIRCABC service database.
   *
   * @param profileRef the node reference of the profile to delete
   * @return the node reference of the owning Interest Group, to be resynchronized afterwards
   */
  @Override
  public NodeRef profilesIdDelete(NodeRef profileRef) {
    Profile prof = getProfile(profileRef);
    if (Boolean.FALSE.equals(prof.getImported())) {
      this.deleteAuthorityAsAdmin(prof.getGroupName());
    }
    NodeRef igRef = nodeService.getPrimaryParent(profileRef).getParentRef();
    nodeService.deleteNode(profileRef);
    circabcService.deleteProfile(igRef, prof.getName());

    return igRef;
  }

  private void deleteAuthorityAsAdmin(String group) {
    AuthenticationUtil.runAs(
      new RunAsWork<Void>() {
        @Override
        public Void doWork() throws Exception {
          authorityService.deleteAuthority(group, true);
          return null;
        }
      },
      AuthenticationUtil.getAdminUserName()
    );
  }

  /**
   * Updates an existing profile: its title, imported/exported flags and the permission sets of all
   * services (directory, library, newsgroups, events, information and visibility). The matching
   * Alfresco permissions are re-applied on the IG service folders, and the special
   * {@code guest}/{@code EVERYONE} (registered) profiles are kept in sync when one of them is
   * updated. The change is finally synchronized in the CIRCABC service database.
   *
   * @param profileRef the node reference of the profile to update
   * @param body the new profile state (title, flags and per-service permissions)
   * @return the profile as persisted after the update
   */
  @Override
  public Profile profilesIdPut(NodeRef profileRef, Profile body) {
    nodeService.setProperty(
      profileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(body.getTitle())
    );

    if (body.getImported() != null) {
      nodeService.setProperty(
        profileRef,
        ProfileModel.PROP_PROFILE_IMPORTED,
        body.getImported()
      );
    }
    if (body.getExported() != null) {
      nodeService.setProperty(
        profileRef,
        ProfileModel.PROP_PROFILE_EXPORTED,
        body.getExported()
      );
    }

    String groupName = getProfile(profileRef).getGroupName();
    body.setGroupName(groupName);

    updateServicePermission(profileRef, body, DIRECTORY, DIRECTORY_KEY);
    applyDirectoryPermissions(profileRef, body);

    updateServicePermission(profileRef, body, LIBRARY, LIBRARY_KEY);
    applyServicePermissions(profileRef, body, "Library", LIBRARY_KEY);

    updateServicePermission(profileRef, body, NEWSGROUP, NEWSGROUP_KEY);
    applyServicePermissions(profileRef, body, "Newsgroups", NEWSGROUP_KEY);

    updateServicePermission(profileRef, body, EVENT, EVENT_KEY);
    applyServicePermissions(profileRef, body, "Events", EVENT_KEY);

    updateServicePermission(profileRef, body, INFORMATION, INFORMATION_KEY);
    applyServicePermissions(profileRef, body, "Information", INFORMATION_KEY);

    updateServicePermission(profileRef, body, VISIBILITY, VISIBILITY_KEY);

    if (body.getGroupName().equals(GUEST)) {
      synchronizeRegisteredProfile(profileRef, body);
    }

    if (body.getGroupName().equals("GROUP_EVERYONE")) {
      synchronizeGuestProfile(profileRef, body);
    }

    Profile profile = this.getProfile(profileRef);

    circabcService.updateProfile(
      nodeService.getPrimaryParent(profileRef).getParentRef(),
      profile.getName(),
      profile
    );

    return profile;
  }

  private void updateServicePermission(
    NodeRef profileRef,
    Profile body,
    QName serviceName,
    String permissionKey
  ) {
    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      serviceName
    );
    NodeRef serviceRef = assocs.get(0).getChildRef();
    ArrayList<String> perms = new ArrayList<>();
    perms.add(body.getPermissions().get(permissionKey));
    nodeService.setProperty(
      serviceRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      perms
    );
  }

  private void synchronizeRegisteredProfile(NodeRef profileRef, Profile body) {
    NodeRef groupRef = nodeService.getPrimaryParent(profileRef).getParentRef();
    Profile registeredProfile = groupsIdProfilesGet(
      groupRef.getId(),
      "EVERYONE",
      false
    ).get(0);
    boolean mustUpdate = false;

    mustUpdate |= syncPermissionUp(
      body,
      registeredProfile,
      INFORMATION_KEY,
      INF_ACCESS
    );
    mustUpdate |= syncPermissionUp(
      body,
      registeredProfile,
      LIBRARY_KEY,
      LIB_ACCESS
    );
    mustUpdate |= syncPermissionUp(
      body,
      registeredProfile,
      DIRECTORY_KEY,
      DIR_ACCESS
    );
    mustUpdate |= syncPermissionUp(
      body,
      registeredProfile,
      EVENT_KEY,
      EVE_ACCESS
    );
    mustUpdate |= syncPermissionUp(
      body,
      registeredProfile,
      NEWSGROUP_KEY,
      NWS_ACCESS
    );

    if (mustUpdate) {
      NodeRef registeredRef = Converter.createNodeRefFromId(
        registeredProfile.getId()
      );
      profilesIdPut(registeredRef, registeredProfile);
    }
  }

  private boolean syncPermissionUp(
    Profile source,
    Profile target,
    String key,
    String accessValue
  ) {
    if (
      source.getPermissions().get(key).equals(accessValue) &&
      !source.getPermissions().get(key).equals(target.getPermissions().get(key))
    ) {
      target.getPermissions().put(key, accessValue);
      return true;
    }
    return false;
  }

  private void synchronizeGuestProfile(NodeRef profileRef, Profile body) {
    NodeRef groupRef = nodeService.getPrimaryParent(profileRef).getParentRef();
    Profile guestProfile = groupsIdProfilesGet(
      groupRef.getId(),
      GUEST,
      false
    ).get(0);
    boolean mustUpdate = false;

    // Special case: Library losing access also removes Information
    if (
      !body.getPermissions().get(LIBRARY_KEY).equals(LIB_ACCESS) &&
      guestProfile.getPermissions().get(LIBRARY_KEY).equals(LIB_ACCESS)
    ) {
      guestProfile.getPermissions().put(INFORMATION_KEY, INF_NO_ACCESS);
      guestProfile
        .getPermissions()
        .put(LIBRARY_KEY, body.getPermissions().get(LIBRARY_KEY));
      mustUpdate = true;
    } else {
      mustUpdate |= syncPermissionDown(
        body,
        guestProfile,
        LIBRARY_KEY,
        LIB_ACCESS,
        LIB_NO_ACCESS
      );
    }

    mustUpdate |= syncPermissionDown(
      body,
      guestProfile,
      INFORMATION_KEY,
      INF_ACCESS,
      INF_NO_ACCESS
    );
    mustUpdate |= syncPermissionDown(
      body,
      guestProfile,
      DIRECTORY_KEY,
      DIR_ACCESS,
      DIR_NO_ACCESS
    );
    mustUpdate |= syncPermissionDown(
      body,
      guestProfile,
      EVENT_KEY,
      EVE_ACCESS,
      EVE_NO_ACCESS
    );
    mustUpdate |= syncPermissionDown(
      body,
      guestProfile,
      NEWSGROUP_KEY,
      NWS_ACCESS,
      NWS_NO_ACCESS
    );

    if (mustUpdate) {
      NodeRef guestRef = Converter.createNodeRefFromId(guestProfile.getId());
      profilesIdPut(guestRef, guestProfile);
    }
  }

  private boolean syncPermissionDown(
    Profile source,
    Profile target,
    String key,
    String accessValue,
    String noAccessValue
  ) {
    String sourceVal = source.getPermissions().get(key);
    String targetVal = target.getPermissions().get(key);
    if (!sourceVal.equals(accessValue) && targetVal.equals(accessValue)) {
      target.getPermissions().put(key, sourceVal);
      return true;
    }
    if (sourceVal.equals(noAccessValue) && !sourceVal.equals(targetVal)) {
      target.getPermissions().put(key, noAccessValue);
      return true;
    }
    return false;
  }

  /**
   * Imports a profile from another Interest Group into the target Interest Group. A new profile
   * node is created referencing the source profile, initialized with no-access permissions on every
   * service and a name prefixed by the source IG name (made unique if needed).
   *
   * @param nodeRef the node reference of the target Interest Group receiving the imported profile
   * @param body the profile to import; its {@code id} must reference the source profile node
   * @return the newly created imported profile
   * @throws InvalidNodeRefException if {@code nodeRef} is not an Interest Group root node
   */
  @Override
  public Profile groupsIdImportedProfilesPost(NodeRef nodeRef, Profile body) {
    if (!nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      throw new InvalidNodeRefException(
        "The node is not a Interest group",
        nodeRef
      );
    }

    NodeRef sourceProfileRef = Converter.createNodeRefFromId(body.getId());
    NodeRef sourceIgRef = nodeService
      .getPrimaryParent(sourceProfileRef)
      .getParentRef();
    String groupName = nodeService
      .getProperty(sourceIgRef, ContentModel.PROP_NAME)
      .toString();

    Profile newProfile = getProfile(sourceProfileRef);
    newProfile.setName(groupName + "_" + newProfile.getName());
    newProfile.setImportedRef(sourceProfileRef.getId());

    List<Profile> existingProfiles = groupsIdProfilesGet(
      nodeRef.getId(),
      "",
      false
    );

    int attempt = 0;
    generateName(newProfile, attempt);
    while (profileAlreadyExists(newProfile, existingProfiles)) {
      generateName(newProfile, attempt);
      attempt++;
    }

    newProfile.setImported(true);
    newProfile.setExported(false);

    // profile node
    QName profileQName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      newProfile.getName()
    );
    ChildAssociationRef childAssoc = nodeService.createNode(
      nodeRef,
      ProfileModel.ASSOC_IG_ROOT_PROFILE,
      profileQName,
      CircabcModel.TYPE_INTEREST_GROUP_PROFILE
    );
    NodeRef profileRef = childAssoc.getChildRef();
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_IG_ROOT_PROFILE_NAME,
      newProfile.getName()
    );
    nodeService.setProperty(
      profileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(newProfile.getTitle())
    );
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_PROFILE_IMPORTED,
      true
    );
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_PROFILE_EXPORTED,
      false
    );
    nodeService.setProperty(
      profileRef,
      ProfileModel.PROP_PROFILE_IMPORTED_REF,
      sourceIgRef
    );
    nodeService.addAspect(
      profileRef,
      CircabcModel.ASPECT_PROFILE_IMPORTABLE,
      null
    );

    ChildAssociationRef dirAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      DIRECTORY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef directoryRef = dirAssoc.getChildRef();
    ArrayList<String> dirPerms = new ArrayList<>();
    dirPerms.add(DirectoryPermissions.DIRNOACCESS.toString());
    nodeService.setProperty(
      directoryRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      dirPerms
    );
    nodeService.setProperty(
      directoryRef,
      PROP_IG_ROOT_SERVICE_NAME,
      DIRECTORY.getLocalName()
    );

    ChildAssociationRef visAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      VISIBILITY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef visibilityRef = visAssoc.getChildRef();
    ArrayList<String> visPerms = new ArrayList<>();
    visPerms.add(VISIBILITY_PERM);
    nodeService.setProperty(
      visibilityRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      visPerms
    );
    nodeService.setProperty(
      visibilityRef,
      PROP_IG_ROOT_SERVICE_NAME,
      VISIBILITY.getLocalName()
    );

    ChildAssociationRef libAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      LIBRARY,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef libraryRef = libAssoc.getChildRef();
    ArrayList<String> libPerms = new ArrayList<>();
    libPerms.add(LibraryPermissions.LIBNOACCESS.toString());
    nodeService.setProperty(
      libraryRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      libPerms
    );
    nodeService.setProperty(
      libraryRef,
      PROP_IG_ROOT_SERVICE_NAME,
      LIBRARY.getLocalName()
    );

    ChildAssociationRef nwsAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      NEWSGROUP,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef newsgroupRef = nwsAssoc.getChildRef();
    ArrayList<String> nwsPerms = new ArrayList<>();
    nwsPerms.add(NewsGroupPermissions.NWSNOACCESS.toString());
    nodeService.setProperty(
      newsgroupRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      nwsPerms
    );
    nodeService.setProperty(
      newsgroupRef,
      PROP_IG_ROOT_SERVICE_NAME,
      NEWSGROUP.getLocalName()
    );

    ChildAssociationRef evtAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      EVENT,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef eventRef = evtAssoc.getChildRef();
    ArrayList<String> evtPerms = new ArrayList<>();
    evtPerms.add(EventPermissions.EVENOACCESS.toString());
    nodeService.setProperty(
      eventRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      evtPerms
    );
    nodeService.setProperty(
      eventRef,
      PROP_IG_ROOT_SERVICE_NAME,
      EVENT.getLocalName()
    );

    ChildAssociationRef infAssoc = nodeService.createNode(
      profileRef,
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      INFORMATION,
      TYPE_IG_ROOT_SERVICE
    );
    NodeRef informationRef = infAssoc.getChildRef();
    ArrayList<String> infPerms = new ArrayList<>();
    infPerms.add(InformationPermissions.INFNOACCESS.toString());
    nodeService.setProperty(
      informationRef,
      CircabcModel.PROP_IG_ROOT_PERMISSION_SET,
      infPerms
    );
    nodeService.setProperty(
      informationRef,
      PROP_IG_ROOT_SERVICE_NAME,
      INFORMATION.getLocalName()
    );

    nodeService.setProperty(
      profileRef,
      CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME,
      newProfile.getGroupName()
    );

    // set reference to the original source profile
    nodeService.createAssociation(
      sourceProfileRef,
      profileRef,
      ProfileModel.ASSOC_PROFILE_IMPORTED_TO
    );

    return getProfile(profileRef);
  }

  /**
   * Creates a profile without triggering the CIRCABC database synchronization. Used by the CIRCABC
   * method interceptor during Interest Group creation, where synchronization is performed once at
   * the end of the IG creation rather than for each profile.
   *
   * @param nodeRef the node reference of the Interest Group that will own the profile
   * @param body the profile to create
   * @return the created profile
   */
  @Override
  public Profile groupsIdProfilesPostNoSync(NodeRef nodeRef, Profile body) {
    return createProfileInternal(nodeRef, body);
  }

  private void applyServicePermissions(
    NodeRef profileRef,
    Profile body,
    String serviceName,
    String permissionKey
  ) {
    NodeRef igRef = getGroupNodeRef(profileRef);
    NodeRef serviceRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      serviceName
    );
    removeExistingPermission(serviceRef, body);
    permissionService.setPermission(
      serviceRef,
      body.getGroupName(),
      body.getPermissions().get(permissionKey),
      true
    );
  }

  private void removeExistingPermission(NodeRef nodeRef, Profile body) {
    String permToDelete = getPermissionForAuthority(
      nodeRef,
      body.getGroupName()
    );
    if (permToDelete != null) {
      permissionService.deletePermission(
        nodeRef,
        body.getGroupName(),
        permToDelete
      );
    }
  }

  private String getPermissionForAuthority(NodeRef nodeRef, String groupName) {
    Set<AccessPermission> permissions = permissionService.getAllSetPermissions(
      nodeRef
    );
    String perm = null;
    for (AccessPermission accessPermission : permissions) {
      if (
        accessPermission.getAuthority().equals(groupName) &&
        !accessPermission.isInherited()
      ) {
        perm = accessPermission.getPermission();
      }
    }

    return perm;
  }

  private void applyDirectoryPermissions(NodeRef profileRef, Profile body) {
    NodeRef igRef = getGroupNodeRef(profileRef);

    Set<AccessPermission> permissions = permissionService.getAllSetPermissions(
      igRef
    );
    String permToDelete = null;
    for (AccessPermission accessPermission : permissions) {
      if (
        accessPermission.getAuthority().equals(body.getGroupName()) &&
        accessPermission.getPermission().contains("Dir")
      ) {
        permToDelete = accessPermission.getPermission();
      }
    }

    if (permToDelete != null) {
      permissionService.deletePermission(
        igRef,
        body.getGroupName(),
        permToDelete
      );
    }
    permissionService.setPermission(
      igRef,
      body.getGroupName(),
      body.getPermissions().get(DIRECTORY_KEY),
      true
    );
  }

  /**
   * Returns the set of users invited into the Interest Group, resolved from the IG's invited-users
   * Alfresco group.
   *
   * @param nodeRef the node reference of the Interest Group root node
   * @return the user authorities contained in the invited-users group, or an empty set if the group
   *     does not exist
   */
  public final Set<String> getInvitedUsers(NodeRef nodeRef) {
    Set<String> usersSet = Collections.emptySet();
    final String userGroupName = (String) nodeService.getProperty(
      nodeRef,
      QName.createQName(
        CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
        PROFILE_PREFIX + "InvitedUsersGroup"
      )
    );
    final String prefixedUserGroupName = authorityService.getName(
      AuthorityType.GROUP,
      userGroupName
    );

    if (authorityService.authorityExists(prefixedUserGroupName)) {
      usersSet = authorityService.getContainedAuthorities(
        AuthorityType.USER,
        prefixedUserGroupName,
        false
      );
    }

    return usersSet;
  }

  private NodeRef getGroupNodeRef(NodeRef profileRef) {
    return nodeService.getPrimaryParent(profileRef).getParentRef();
  }

  /**
   * Reads a single profile by its identifier.
   *
   * @param profileId the identifier of the profile node
   * @return the corresponding profile model
   */
  @Override
  public Profile profilesIdGet(String profileId) {
    NodeRef profileRef = Converter.createNodeRefFromId(profileId);
    return getProfile(profileRef);
  }

  /**
   * Resolves the name of the profile assigned to a user within an Interest Group.
   *
   * @param nodeRef the node reference of the Interest Group
   * @param userAutority the authority (user name) whose profile is requested
   * @return the name of the user's profile in the Interest Group
   */
  @Override
  public String getPersonProfile(NodeRef nodeRef, String userAutority) {
    return profileService.getPersonProfile(nodeRef, userAutority);
  }

  /**
   * Resolves the backing Alfresco group name of the profile assigned to a user within an Interest
   * Group.
   *
   * @param nodeRef the node reference of the Interest Group
   * @param userAutority the authority (user name) whose profile group is requested
   * @return the Alfresco group name of the user's profile in the Interest Group
   */
  @Override
  public String getPersonProfileGroupName(
    NodeRef nodeRef,
    String userAutority
  ) {
    return circabcService.getPersonProfileGroupName(nodeRef, userAutority);
  }
}
