package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ProfileModel;
import eu.cec.digit.circabc.service.profile.permissions.*;
import io.swagger.model.I18nProperty;
import io.swagger.model.Profile;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import static eu.cec.digit.circabc.model.CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI;

/**
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
    private static final String PROFILE_PREFIX = "circaIGRoot";
    private static final QName DIRECTORY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "DIRECTORY");
    private static final QName VISIBILITY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "VISIBILITY");
    private static final QName LIBRARY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "LIBRARY");
    private static final QName NEWSGROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "NEWSGROUP");
    private static final QName INFORMATION =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "INFORMATION");
    private static final QName EVENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "EVENT");
    private static final QName TYPE_IG_ROOT_SERVICE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootService");
    private static final QName PROP_IG_ROOT_SERVICE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootServiceName");
    private NodeService nodeService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private ApiToolBox apiToolBox;

    @Override
    public List<Profile> groupsIdProfilesGet(
            String id, String searchQuery, boolean nonEmptyProfiles) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        List<Profile> result = new ArrayList<>();

        List<ChildAssociationRef> listOfProfilesAssoc =
                nodeService.getChildAssocs(
                        groupNodeRef, getProfileAssocQName(), RegexQNamePattern.MATCH_ALL);

        for (ChildAssociationRef caRef : listOfProfilesAssoc) {

            NodeRef profileRefTmp = caRef.getChildRef();

            Profile profileTmp = getProfile(profileRefTmp);

            if (StringUtils.isEmpty(searchQuery) || matchQuery(searchQuery, profileTmp)) {

                if (nonEmptyProfiles) {
                    List<String> names = apiToolBox.getUsersFromGroup(profileTmp.getGroupName());
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
     * @param profileRefTmp
     * @return
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
            if (title.getValue() != null && title.getValue().toLowerCase().matches(queryTmp)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param profileRefTmp
     * @param props
     * @return
     */
    @SuppressWarnings("unchecked")
    private Profile convertProfile(NodeRef profileRefTmp, Map<QName, Serializable> props) {
        Profile profileTmp = new Profile();
        profileTmp.setId(profileRefTmp.getId());
        profileTmp.setName(props.get(CircabcModel.PROP_IG_ROOT_PROFILE_NAME).toString());

        Serializable titleObj = props.get(ContentModel.PROP_TITLE);
        if (titleObj instanceof String) {
            profileTmp.setTitle(Converter.toI18NProperty((String) titleObj));
        } else if (titleObj instanceof MLText) {
            profileTmp.setTitle(Converter.toI18NProperty((MLText) titleObj));
        }

        profileTmp.setGroupName(props.get(CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME).toString());

        List<ChildAssociationRef> profilesDefinitionAssoc =
                nodeService.getChildAssocs(
                        profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, DIRECTORY);
        NodeRef profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();

        List<String> perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        profileTmp.getPermissions().put(DIRECTORY_KEY, perms.get(0));

        profilesDefinitionAssoc =
                nodeService.getChildAssocs(
                        profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, INFORMATION);
        profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();

        perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        profileTmp.getPermissions().put(INFORMATION_KEY, perms.get(0));

        profilesDefinitionAssoc =
                nodeService.getChildAssocs(profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, LIBRARY);
        profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
        perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        profileTmp.getPermissions().put(LIBRARY_KEY, perms.get(0));

        profilesDefinitionAssoc =
                nodeService.getChildAssocs(profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, EVENT);
        profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
        perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        profileTmp.getPermissions().put(EVENT_KEY, perms.get(0));

        profilesDefinitionAssoc =
                nodeService.getChildAssocs(
                        profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, NEWSGROUP);
        profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
        perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        profileTmp.getPermissions().put(NEWSGROUP_KEY, perms.get(0));

        profilesDefinitionAssoc =
                nodeService.getChildAssocs(
                        profileRefTmp, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, VISIBILITY);
        profileDefTmp = profilesDefinitionAssoc.get(0).getChildRef();
        perms =
                (ArrayList<String>)
                        nodeService.getProperty(profileDefTmp, CircabcModel.PROP_IG_ROOT_PERMISSION_SET);
        if (perms != null) {
            profileTmp.getPermissions().put(VISIBILITY_KEY, perms.get(0));
        } else {
            profileTmp.getPermissions().put(VISIBILITY_KEY, VISIBILITY_PERM);
        }

        if (props.containsKey(ProfileModel.PROP_PROFILE_IMPORTED)
                && props.get(ProfileModel.PROP_PROFILE_IMPORTED) != null) {
            profileTmp.setImported((Boolean) props.get(ProfileModel.PROP_PROFILE_IMPORTED));
        } else {
            profileTmp.setImported(false);
        }

        if (props.containsKey(ProfileModel.PROP_PROFILE_EXPORTED)
                && props.get(ProfileModel.PROP_PROFILE_EXPORTED) != null) {
            profileTmp.setExported((Boolean) props.get(ProfileModel.PROP_PROFILE_EXPORTED));
        } else {
            profileTmp.setExported(false);
        }

        if (Boolean.TRUE.equals(profileTmp.getExported())) {
            List<AssociationRef> lRef =
                    nodeService.getTargetAssocs(profileRefTmp, ProfileModel.ASSOC_PROFILE_IMPORTED_TO);
            for (AssociationRef ar : lRef) {
                profileTmp.getExportedRefs().add(ar.getTargetRef().getId());
            }
        }

        return profileTmp;
    }

    private QName getProfileAssocQName() {
        return QName.createQName(
                CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, PROFILE_PREFIX + "ProfileAssoc");
    }

    @Override
    public Profile groupsIdProfilesPost(NodeRef groupNodeRef, Profile body) {

        return createProfileInternal(groupNodeRef, body);
    }

    /**
     * @param groupNodeRef
     * @param body
     * @return
     */
    private Profile createProfileInternal(NodeRef groupNodeRef, Profile body) {
        if (!nodeService.hasAspect(groupNodeRef, CircabcModel.ASPECT_IGROOT)) {
            throw new InvalidNodeRefException("The node is not a Interest group", groupNodeRef);
        }

        List<Profile> existingProfiles = groupsIdProfilesGet(groupNodeRef.getId(), "", false);

        int attempt = 0;
        generateName(body, attempt);
        while (profileAlreadyExists(body, existingProfiles)) {
            generateName(body, attempt);
            attempt++;
        }

        body.setImported(false);
        body.setExported(false);

        // profile node
        QName profileQName =
                QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, body.getName());
        ChildAssociationRef childAssoc =
                nodeService.createNode(
                        groupNodeRef,
                        ProfileModel.ASSOC_IG_ROOT_PROFILE,
                        profileQName,
                        CircabcModel.TYPE_INTEREST_GROUP_PROFILE);
        NodeRef profileRef = childAssoc.getChildRef();
        nodeService.setProperty(profileRef, ProfileModel.PROP_IG_ROOT_PROFILE_NAME, body.getName());
        nodeService.setProperty(
                profileRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
        nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_IMPORTED, false);
        nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_EXPORTED, false);
        nodeService.addAspect(profileRef, CircabcModel.ASPECT_PROFILE_IMPORTABLE, null);

        ChildAssociationRef dirAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, DIRECTORY, TYPE_IG_ROOT_SERVICE);
        NodeRef directoryRef = dirAssoc.getChildRef();
        ArrayList<String> dirPerms = new ArrayList<>();
        dirPerms.add(body.getPermissions().get(DIRECTORY_KEY));
        nodeService.setProperty(directoryRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, dirPerms);
        nodeService.setProperty(directoryRef, PROP_IG_ROOT_SERVICE_NAME, DIRECTORY.getLocalName());

        ChildAssociationRef visAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, VISIBILITY, TYPE_IG_ROOT_SERVICE);
        NodeRef visibilityRef = visAssoc.getChildRef();
        ArrayList<String> visPerms = new ArrayList<>();
        if (body.getPermissions().containsKey(VISIBILITY_KEY)) {
            String visbilityValue = body.getPermissions().get(VISIBILITY_KEY);
            visPerms.add(visbilityValue);
        } else {
            visPerms.add(VISIBILITY_PERM);
        }
        nodeService.setProperty(visibilityRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, visPerms);
        nodeService.setProperty(visibilityRef, PROP_IG_ROOT_SERVICE_NAME, VISIBILITY.getLocalName());

        ChildAssociationRef libAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, LIBRARY, TYPE_IG_ROOT_SERVICE);
        NodeRef libraryRef = libAssoc.getChildRef();
        ArrayList<String> libPerms = new ArrayList<>();
        libPerms.add(body.getPermissions().get(LIBRARY_KEY));
        nodeService.setProperty(libraryRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, libPerms);
        nodeService.setProperty(libraryRef, PROP_IG_ROOT_SERVICE_NAME, LIBRARY.getLocalName());

        ChildAssociationRef nwsAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, NEWSGROUP, TYPE_IG_ROOT_SERVICE);
        NodeRef newsgroupRef = nwsAssoc.getChildRef();
        ArrayList<String> nwsPerms = new ArrayList<>();
        nwsPerms.add(body.getPermissions().get(NEWSGROUP_KEY));
        nodeService.setProperty(newsgroupRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, nwsPerms);
        nodeService.setProperty(newsgroupRef, PROP_IG_ROOT_SERVICE_NAME, NEWSGROUP.getLocalName());

        ChildAssociationRef evtAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, EVENT, TYPE_IG_ROOT_SERVICE);
        NodeRef eventRef = evtAssoc.getChildRef();
        ArrayList<String> evtPerms = new ArrayList<>();
        evtPerms.add(body.getPermissions().get(EVENT_KEY));
        nodeService.setProperty(eventRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, evtPerms);
        nodeService.setProperty(eventRef, PROP_IG_ROOT_SERVICE_NAME, EVENT.getLocalName());

        ChildAssociationRef infAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, INFORMATION, TYPE_IG_ROOT_SERVICE);
        NodeRef informationRef = infAssoc.getChildRef();
        ArrayList<String> infPerms = new ArrayList<>();
        infPerms.add(body.getPermissions().get(INFORMATION_KEY));
        nodeService.setProperty(informationRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, infPerms);
        nodeService.setProperty(informationRef, PROP_IG_ROOT_SERVICE_NAME, INFORMATION.getLocalName());

        switch (body.getName()) {
            case GUEST:
                nodeService.setProperty(profileRef, CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME, GUEST);
                break;
            case "EVERYONE":
                nodeService.setProperty(
                        profileRef, CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME, "GROUP_EVERYONE");
                break;
            default:
                String igInvitedGroupName =
                        "GROUP_"
                                + nodeService
                                .getProperty(groupNodeRef, CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP)
                                .toString();
                String newAlfGroupName = body.getName() + "--" + GUID.generate();

                String finalAlfGroupName =
                        authorityService.createAuthority(
                                AuthorityType.GROUP,
                                newAlfGroupName,
                                "GROUP_" + newAlfGroupName,
                                authorityService.getDefaultZones());
                authorityService.addAuthority(igInvitedGroupName, finalAlfGroupName);
                nodeService.setProperty(
                        profileRef, CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME, finalAlfGroupName);
                permissionService.setPermission(groupNodeRef, finalAlfGroupName, VISIBILITY_PERM, true);
                break;
        }

        Profile createdProfile = getProfile(profileRef);
        applyDirectoryPermissions(profileRef, createdProfile);
        applyEventsPermissions(profileRef, createdProfile);
        applyInformationPermissions(profileRef, createdProfile);
        applyLibraryPermissions(profileRef, createdProfile);
        applyNewsgroupsPermissions(profileRef, createdProfile);

        return createdProfile;
    }

    private boolean profileAlreadyExists(Profile body, List<Profile> existingProfiles) {
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

    @Override
    public NodeRef profilesIdDelete(NodeRef profileRef) {

        Profile prof = getProfile(profileRef);
        if (Boolean.FALSE.equals(prof.getImported())) {
            authorityService.deleteAuthority(prof.getGroupName());
        }
        NodeRef igRef = nodeService.getPrimaryParent(profileRef).getParentRef();
        nodeService.deleteNode(profileRef);

        return igRef;
    }

    @Override
    public Profile profilesIdPut(NodeRef profileRef, Profile body) {

        nodeService.setProperty(
                profileRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));

        if (body.getImported() != null) {
            nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_IMPORTED, body.getImported());
        }
        if (body.getExported() != null) {
            nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_EXPORTED, body.getExported());
        }

        String groupName = getProfile(profileRef).getGroupName();
        body.setGroupName(groupName);

        List<ChildAssociationRef> dirRefAssoc =
                nodeService.getChildAssocs(profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, DIRECTORY);
        NodeRef dirRef = dirRefAssoc.get(0).getChildRef();
        ArrayList<String> dirPerms = new ArrayList<>();
        dirPerms.add(body.getPermissions().get(DIRECTORY_KEY));
        nodeService.setProperty(dirRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, dirPerms);
        applyDirectoryPermissions(profileRef, body);

        List<ChildAssociationRef> libRefAssoc =
                nodeService.getChildAssocs(profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, LIBRARY);
        NodeRef libraryRef = libRefAssoc.get(0).getChildRef();
        ArrayList<String> libPerms = new ArrayList<>();
        libPerms.add(body.getPermissions().get(LIBRARY_KEY));
        nodeService.setProperty(libraryRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, libPerms);
        applyLibraryPermissions(profileRef, body);

        List<ChildAssociationRef> nwsRefAssoc =
                nodeService.getChildAssocs(profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, NEWSGROUP);
        NodeRef newsgroupRef = nwsRefAssoc.get(0).getChildRef();
        ArrayList<String> nwsPerms = new ArrayList<>();
        nwsPerms.add(body.getPermissions().get(NEWSGROUP_KEY));
        nodeService.setProperty(newsgroupRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, nwsPerms);
        applyNewsgroupsPermissions(profileRef, body);

        List<ChildAssociationRef> evtsRefAssoc =
                nodeService.getChildAssocs(profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, EVENT);
        NodeRef eventRef = evtsRefAssoc.get(0).getChildRef();
        ArrayList<String> evtPerms = new ArrayList<>();
        evtPerms.add(body.getPermissions().get(EVENT_KEY));
        nodeService.setProperty(eventRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, evtPerms);
        applyEventsPermissions(profileRef, body);

        List<ChildAssociationRef> infRefAssoc =
                nodeService.getChildAssocs(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, INFORMATION);
        NodeRef informationRef = infRefAssoc.get(0).getChildRef();
        ArrayList<String> infPerms = new ArrayList<>();
        infPerms.add(body.getPermissions().get(INFORMATION_KEY));
        nodeService.setProperty(informationRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, infPerms);
        applyInformationPermissions(profileRef, body);

        List<ChildAssociationRef> visRefAssoc =
                nodeService.getChildAssocs(profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, VISIBILITY);
        NodeRef visRef = visRefAssoc.get(0).getChildRef();
        ArrayList<String> visPerms = new ArrayList<>();
        visPerms.add(body.getPermissions().get(VISIBILITY_KEY));
        nodeService.setProperty(visRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, visPerms);

        if (body.getGroupName().equals(GUEST)
                && body.getPermissions().get(VISIBILITY_KEY).equals(VISIBILITY_PERM)) {
            NodeRef groupRef = nodeService.getPrimaryParent(profileRef).getParentRef();
            Profile registeredProfile = groupsIdProfilesGet(groupRef.getId(), "EVERYONE", false).get(0);
            Boolean mustUpdate = false;
            if (body.getPermissions().get(INFORMATION_KEY).equals("InfAccess")
                    && !body.getPermissions()
                    .get(INFORMATION_KEY)
                    .equals(registeredProfile.getPermissions().get(INFORMATION_KEY))) {
                registeredProfile.getPermissions().put(INFORMATION_KEY, "InfAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(LIBRARY_KEY).equals("LibAccess")
                    && !body.getPermissions()
                    .get(LIBRARY_KEY)
                    .equals(registeredProfile.getPermissions().get(LIBRARY_KEY))) {
                registeredProfile.getPermissions().put(LIBRARY_KEY, "LibAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(DIRECTORY_KEY).equals("DirAccess")
                    && !body.getPermissions()
                    .get(DIRECTORY_KEY)
                    .equals(registeredProfile.getPermissions().get(DIRECTORY_KEY))) {
                registeredProfile.getPermissions().put(DIRECTORY_KEY, "DirAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(EVENT_KEY).equals("EveAccess")
                    && !body.getPermissions()
                    .get(EVENT_KEY)
                    .equals(registeredProfile.getPermissions().get(EVENT_KEY))) {
                registeredProfile.getPermissions().put(EVENT_KEY, "EveAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(NEWSGROUP_KEY).equals("NwsAccess")
                    && !body.getPermissions()
                    .get(NEWSGROUP_KEY)
                    .equals(registeredProfile.getPermissions().get(NEWSGROUP_KEY))) {
                registeredProfile.getPermissions().put(NEWSGROUP_KEY, "NwsAccess");
                mustUpdate = true;
            }

            if (Boolean.TRUE.equals(mustUpdate)) {
                NodeRef registeredRef = Converter.createNodeRefFromId(registeredProfile.getId());
                profilesIdPut(registeredRef, registeredProfile);
            }
        }

        if (body.getGroupName().equals("GROUP_EVERYONE")
                && body.getPermissions().get(VISIBILITY_KEY).equals(VISIBILITY_PERM)) {
            NodeRef groupRef = nodeService.getPrimaryParent(profileRef).getParentRef();
            Profile guestProfile = groupsIdProfilesGet(groupRef.getId(), GUEST, false).get(0);
            boolean mustUpdate = false;
            if (body.getPermissions().get(INFORMATION_KEY).equals("InfNoAccess")
                    && !body.getPermissions()
                    .get(INFORMATION_KEY)
                    .equals(guestProfile.getPermissions().get(INFORMATION_KEY))) {
                guestProfile.getPermissions().put(INFORMATION_KEY, "InfNoAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(LIBRARY_KEY).equals("LibNoAccess")
                    && !body.getPermissions()
                    .get(LIBRARY_KEY)
                    .equals(guestProfile.getPermissions().get(LIBRARY_KEY))) {
                guestProfile.getPermissions().put(LIBRARY_KEY, "LibNoAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(DIRECTORY_KEY).equals("DirNoAccess")
                    && !body.getPermissions()
                    .get(DIRECTORY_KEY)
                    .equals(guestProfile.getPermissions().get(DIRECTORY_KEY))) {
                guestProfile.getPermissions().put(DIRECTORY_KEY, "DirNoAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(EVENT_KEY).equals("EveNoAccess")
                    && !body.getPermissions()
                    .get(EVENT_KEY)
                    .equals(guestProfile.getPermissions().get(EVENT_KEY))) {
                guestProfile.getPermissions().put(EVENT_KEY, "EveNoAccess");
                mustUpdate = true;
            }
            if (body.getPermissions().get(NEWSGROUP_KEY).equals("NwsNoAccess")
                    && !body.getPermissions()
                    .get(NEWSGROUP_KEY)
                    .equals(guestProfile.getPermissions().get(NEWSGROUP_KEY))) {
                guestProfile.getPermissions().put(NEWSGROUP_KEY, "NwsNoAccess");
                mustUpdate = true;
            }

            if (mustUpdate) {
                NodeRef guestRef = Converter.createNodeRefFromId(guestProfile.getId());
                profilesIdPut(guestRef, guestProfile);
            }
        }

        return this.getProfile(profileRef);
    }

    @Override
    public Profile groupsIdImportedProfilesPost(NodeRef nodeRef, Profile body) {

        if (!nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            throw new InvalidNodeRefException("The node is not a Interest group", nodeRef);
        }

        NodeRef sourceProfileRef = Converter.createNodeRefFromId(body.getId());
        NodeRef sourceIgRef = nodeService.getPrimaryParent(sourceProfileRef).getParentRef();
        String groupName = nodeService.getProperty(sourceIgRef, ContentModel.PROP_NAME).toString();

        Profile newProfile = getProfile(sourceProfileRef);
        newProfile.setName(groupName + "_" + newProfile.getName());
        newProfile.setImportedRef(sourceProfileRef.getId());

        List<Profile> existingProfiles = groupsIdProfilesGet(nodeRef.getId(), "", false);

        int attempt = 0;
        generateName(newProfile, attempt);
        while (profileAlreadyExists(newProfile, existingProfiles)) {
            generateName(newProfile, attempt);
            attempt++;
        }

        newProfile.setImported(true);
        newProfile.setExported(false);

        // profile node
        QName profileQName =
                QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, newProfile.getName());
        ChildAssociationRef childAssoc =
                nodeService.createNode(
                        nodeRef,
                        ProfileModel.ASSOC_IG_ROOT_PROFILE,
                        profileQName,
                        CircabcModel.TYPE_INTEREST_GROUP_PROFILE);
        NodeRef profileRef = childAssoc.getChildRef();
        nodeService.setProperty(
                profileRef, ProfileModel.PROP_IG_ROOT_PROFILE_NAME, newProfile.getName());
        nodeService.setProperty(
                profileRef, ContentModel.PROP_TITLE, Converter.toMLText(newProfile.getTitle()));
        nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_IMPORTED, true);
        nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_EXPORTED, false);
        nodeService.setProperty(profileRef, ProfileModel.PROP_PROFILE_IMPORTED_REF, sourceIgRef);
        nodeService.addAspect(profileRef, CircabcModel.ASPECT_PROFILE_IMPORTABLE, null);

        ChildAssociationRef dirAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, DIRECTORY, TYPE_IG_ROOT_SERVICE);
        NodeRef directoryRef = dirAssoc.getChildRef();
        ArrayList<String> dirPerms = new ArrayList<>();
        dirPerms.add(DirectoryPermissions.DIRNOACCESS.toString());
        nodeService.setProperty(directoryRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, dirPerms);
        nodeService.setProperty(directoryRef, PROP_IG_ROOT_SERVICE_NAME, DIRECTORY.getLocalName());

        ChildAssociationRef visAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, VISIBILITY, TYPE_IG_ROOT_SERVICE);
        NodeRef visibilityRef = visAssoc.getChildRef();
        ArrayList<String> visPerms = new ArrayList<>();
        visPerms.add(VISIBILITY_PERM);
        nodeService.setProperty(visibilityRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, visPerms);
        nodeService.setProperty(visibilityRef, PROP_IG_ROOT_SERVICE_NAME, VISIBILITY.getLocalName());

        ChildAssociationRef libAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, LIBRARY, TYPE_IG_ROOT_SERVICE);
        NodeRef libraryRef = libAssoc.getChildRef();
        ArrayList<String> libPerms = new ArrayList<>();
        libPerms.add(LibraryPermissions.LIBNOACCESS.toString());
        nodeService.setProperty(libraryRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, libPerms);
        nodeService.setProperty(libraryRef, PROP_IG_ROOT_SERVICE_NAME, LIBRARY.getLocalName());

        ChildAssociationRef nwsAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, NEWSGROUP, TYPE_IG_ROOT_SERVICE);
        NodeRef newsgroupRef = nwsAssoc.getChildRef();
        ArrayList<String> nwsPerms = new ArrayList<>();
        nwsPerms.add(NewsGroupPermissions.NWSNOACCESS.toString());
        nodeService.setProperty(newsgroupRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, nwsPerms);
        nodeService.setProperty(newsgroupRef, PROP_IG_ROOT_SERVICE_NAME, NEWSGROUP.getLocalName());

        ChildAssociationRef evtAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, EVENT, TYPE_IG_ROOT_SERVICE);
        NodeRef eventRef = evtAssoc.getChildRef();
        ArrayList<String> evtPerms = new ArrayList<>();
        evtPerms.add(EventPermissions.EVENOACCESS.toString());
        nodeService.setProperty(eventRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, evtPerms);
        nodeService.setProperty(eventRef, PROP_IG_ROOT_SERVICE_NAME, EVENT.getLocalName());

        ChildAssociationRef infAssoc =
                nodeService.createNode(
                        profileRef, CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC, INFORMATION, TYPE_IG_ROOT_SERVICE);
        NodeRef informationRef = infAssoc.getChildRef();
        ArrayList<String> infPerms = new ArrayList<>();
        infPerms.add(InformationPermissions.INFNOACCESS.toString());
        nodeService.setProperty(informationRef, CircabcModel.PROP_IG_ROOT_PERMISSION_SET, infPerms);
        nodeService.setProperty(informationRef, PROP_IG_ROOT_SERVICE_NAME, INFORMATION.getLocalName());

        nodeService.setProperty(
                profileRef, CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME, newProfile.getGroupName());

        // set reference to the original source profile
        nodeService.createAssociation(
                sourceProfileRef, profileRef, ProfileModel.ASSOC_PROFILE_IMPORTED_TO);

        return getProfile(profileRef);
    }

    @Override
    public Profile groupsIdProfilesPostNoSync(NodeRef nodeRef, Profile body) {
        return createProfileInternal(nodeRef, body);
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    private void applyLibraryPermissions(NodeRef profileRef, Profile body) {
        NodeRef igRef = getGroupNodeRef(profileRef);
        NodeRef serviceRef = nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Library");
        removeExistingPermission(serviceRef, body);
        permissionService.setPermission(
                serviceRef, body.getGroupName(), body.getPermissions().get(LIBRARY_KEY), true);
    }

    private void removeExistingPermission(NodeRef nodeRef, Profile body) {
        String permToDelete = getPermissionForAuthority(nodeRef, body.getGroupName());
        if (permToDelete != null) {
            permissionService.deletePermission(nodeRef, body.getGroupName(), permToDelete);
        }
    }

    private String getPermissionForAuthority(NodeRef nodeRef, String groupName) {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        String perm = null;
        for (AccessPermission accessPermission : permissions) {
            if (accessPermission.getAuthority().equals(groupName) && !accessPermission.isInherited()) {
                perm = accessPermission.getPermission();
            }
        }

        return perm;
    }

    private void applyInformationPermissions(NodeRef profileRef, Profile body) {
        NodeRef igRef = getGroupNodeRef(profileRef);
        NodeRef serviceRef =
                nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Information");
        removeExistingPermission(serviceRef, body);
        permissionService.setPermission(
                serviceRef, body.getGroupName(), body.getPermissions().get(INFORMATION_KEY), true);
    }

    private void applyNewsgroupsPermissions(NodeRef profileRef, Profile body) {
        NodeRef igRef = getGroupNodeRef(profileRef);
        NodeRef serviceRef =
                nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Newsgroups");
        removeExistingPermission(serviceRef, body);
        permissionService.setPermission(
                serviceRef, body.getGroupName(), body.getPermissions().get(NEWSGROUP_KEY), true);
    }

    private void applyEventsPermissions(NodeRef profileRef, Profile body) {
        NodeRef igRef = getGroupNodeRef(profileRef);
        NodeRef serviceRef = nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Events");
        removeExistingPermission(serviceRef, body);
        permissionService.setPermission(
                serviceRef, body.getGroupName(), body.getPermissions().get(EVENT_KEY), true);
    }

    private void applyDirectoryPermissions(NodeRef profileRef, Profile body) {
        NodeRef igRef = getGroupNodeRef(profileRef);

        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(igRef);
        String permToDelete = null;
        for (AccessPermission accessPermission : permissions) {
            if (accessPermission.getAuthority().equals(body.getGroupName())
                    && accessPermission.getPermission().contains("Dir")) {
                permToDelete = accessPermission.getPermission();
            }
        }

        if (permToDelete != null) {
            permissionService.deletePermission(igRef, body.getGroupName(), permToDelete);
        }
        permissionService.setPermission(
                igRef, body.getGroupName(), body.getPermissions().get(DIRECTORY_KEY), true);
    }

    public final Set<String> getInvitedUsers(NodeRef nodeRef, String profilePrefix) {

        Set<String> usersSet = Collections.emptySet();
        final String userGroupName =
                (String)
                        nodeService.getProperty(
                                nodeRef,
                                QName.createQName(
                                        CIRCABC_CONTENT_MODEL_1_0_URI, profilePrefix + "InvitedUsersGroup"));
        final String prefixedUserGroupName =
                authorityService.getName(AuthorityType.GROUP, userGroupName);

        if (authorityService.authorityExists(prefixedUserGroupName)) {
            usersSet =
                    authorityService.getContainedAuthorities(
                            AuthorityType.USER, prefixedUserGroupName, false);
        }

        return usersSet;
    }

    private NodeRef getGroupNodeRef(NodeRef profileRef) {
        return nodeService.getPrimaryParent(profileRef).getParentRef();
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public Profile profilesIdGet(String profileId) {
        NodeRef profileRef = Converter.createNodeRefFromId(profileId);
        return getProfile(profileRef);
    }
}
