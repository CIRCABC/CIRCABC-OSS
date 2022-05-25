/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.profile;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ProfileModel;
import eu.cec.digit.circabc.repo.applicant.Applicant;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.profile.permissions.VisibilityPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.PermissionUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

import static eu.cec.digit.circabc.model.CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI;

public abstract class ProfileManagerServiceImpl implements ProfileManagerService {

    private static final String GROUP_UNDESCORE = "GROUP_";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(ProfileManagerServiceImpl.class);
    protected SimpleCache<String, Map<String, Profile>> profileMapCache;
    /**
     * ProfileManagerServiceFactory
     */
    protected ProfileManagerServiceFactory profileManagerServiceFactory;

    // IOC parameters
    protected NodeService nodeService;
    protected AuthorityService authorityService;
    protected PermissionService permissionService;
    protected ServiceRegistry serviceRegistry;
    protected FileFolderService fileFolderService;
    protected OwnableService ownableService;
    protected ManagementService managementService;
    /**
     * The authentification Service reference
     */
    protected MutableAuthenticationService authenticationService;
    // cache for invited  users
    private SimpleCache<String, Set<String>> invitedUsersCache;
    private SimpleCache<Serializable, Object> circabcDynamicAuthorityCache = null;

    public void setOwnableService(final OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(final FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setAuthorityService(final AuthorityService authService) {
        this.authorityService = authService;
    }

    /**
     * @param ManagementService the ManagementService to set
     */
    public void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param profileMapCache the profileMapCache to set
     */
    public final void setProfileMapCache(
            final SimpleCache<String, Map<String, Profile>> profileMapCache) {
        this.profileMapCache = profileMapCache;
    }

    private QName getCircaHomeNodeAspect() {
        return CircabcModel.ASPECT_CIRCABC_ROOT;
    }

    /**
     * Return the Aspect QualifiedName. This is used in order to verify that Service is called on
     * right node
     */
    public abstract QName getNodeAspect();

    public abstract QName getNodeRootAspect();

    /**
     * Relation name between node and the profile
     */
    private QName getProfileAssocQName() {
        final QName profileAssocQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ProfileAssoc");
        return profileAssocQName;
    }

    private QName getProfileQName() {
        /** node type for the profile */
        final QName profileQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "Profile");
        return profileQName;
    }

    private QName getProfileNameQName() {
        /** type for the profileName */
        final QName profileQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ProfileName");
        return profileQName;
    }

    private QName getProfileGroupNameQName() {
        /** type for the profileGroupName */
        final QName profileGroupNameQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ProfileGroupName");
        return profileGroupNameQName;
    }

    /**
     * Relation name between profile and the service
     */
    private QName getServiceAssocQName() {
        final QName serviceAssocQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ServiceAssoc");
        return serviceAssocQName;
    }

    private QName getServiceQName() {
        /** node type for the profile */
        final QName serviceQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "Service");
        return serviceQName;
    }

    private QName getServiceNameQName() {
        /** type for the serviceName */
        final QName serviceQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ServiceName");
        return serviceQName;
    }

    private QName getPermissionSetQName() {
        /** type for the permissionSetQName */
        final QName servicePermissionSetQName =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "PermissionSet");
        return servicePermissionSetQName;
    }

    // Profile
    private NodeRef createProfileNode(final NodeRef nodeRef, final Profile profile) {
        final Map<QName, Serializable> contentProps = new HashMap<>();
        contentProps.put(getProfileNameQName(), profile.getProfileName());
        // the stored group name is the prefixed name with GROUP_
        if (!CircabcConstant.GUEST_AUTHORITY.equalsIgnoreCase(profile.getProfileName())) {
            final String prefixedProfileGroupName;

            if (profile.getPrefixedAlfrescoGroupName() == null) {
                prefixedProfileGroupName =
                        authorityService.getName(AuthorityType.GROUP, profile.getAlfrescoGroupName());
            } else {
                prefixedProfileGroupName = profile.getPrefixedAlfrescoGroupName();
            }

            // the prefixed group name is stored in the node representing the profile
            contentProps.put(getProfileGroupNameQName(), prefixedProfileGroupName);
        } else {
            contentProps.put(getProfileGroupNameQName(), profile.getProfileName());
        }

        contentProps.put(ContentModel.PROP_TITLE, profile.getTitle());
        contentProps.put(ContentModel.PROP_DESCRIPTION, profile.getDescription());
        contentProps.put(ProfileModel.PROP_PROFILE_IMPORTED, profile.isImported());
        contentProps.put(ProfileModel.PROP_PROFILE_EXPORTED, profile.isExported());
        contentProps.put(ProfileModel.PROP_PROFILE_IMPORTED_REF, profile.getImportedNodeRef());

        // create profile node
        if (logger.isTraceEnabled()) {
            logger.trace("createNode Profile on node:" + nodeRef);
        }
        final ChildAssociationRef profileAssociation =
                nodeService.createNode(
                        nodeRef,
                        getProfileAssocQName(),
                        QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, profile.getProfileName()),
                        getProfileQName(),
                        contentProps);
        return profileAssociation.getChildRef();
    }

    private NodeRef updateProfileNode(final NodeRef nodeRef, final Profile profile) {
        final NodeRef profileNodeRef = getProfileNode(nodeRef, profile.getProfileName());
        if (profileNodeRef == null) {
            return createProfileNode(nodeRef, profile);
        } else {
            final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

            try {
                MLPropertyInterceptor.setMLAware(true);

                final Map<QName, Serializable> contentProps = new HashMap<>();
                contentProps.put(ContentModel.PROP_TITLE, profile.getTitle());
                contentProps.put(ContentModel.PROP_DESCRIPTION, profile.getDescription());
                contentProps.put(ProfileModel.PROP_PROFILE_IMPORTED, profile.isImported());
                contentProps.put(ProfileModel.PROP_PROFILE_EXPORTED, profile.isExported());
                contentProps.put(ProfileModel.PROP_PROFILE_IMPORTED_REF, profile.getImportedNodeRef());

                nodeService.addProperties(profileNodeRef, contentProps);

                String prefixedProfileGroupName;
                if (CircabcConstant.GUEST_AUTHORITY.equalsIgnoreCase(profile.getProfileName())) {
                    // the stored group name is the prefixed name with GROUP_
                    // prefixedProfileGroupName = authorityService.getName(AuthorityType.GROUP,
                    // profile.getProfileName());
                    prefixedProfileGroupName = CircabcConstant.GUEST_AUTHORITY;
                } else {
                    prefixedProfileGroupName = profile.getPrefixedAlfrescoGroupName();
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("get profileGroupName on node Profile:" + nodeRef);
                }

                final String curentPrefixedProfileGroupName =
                        (String) nodeService.getProperty(profileNodeRef, getProfileGroupNameQName());

                if (curentPrefixedProfileGroupName != null
                        && prefixedProfileGroupName.equals(curentPrefixedProfileGroupName)) {
                    // do nothing - no change
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("update profileGroupName on node Profile:" + nodeRef);
                    }
                    nodeService.setProperty(
                            profileNodeRef, getProfileGroupNameQName(), prefixedProfileGroupName);
                }

                return profileNodeRef;
            } finally {
                MLPropertyInterceptor.setMLAware(wasMLAware);
            }
        }
    }

    private void deleteProfileNode(final NodeRef nodeRef, final String profileName) {
        final NodeRef profileNodeRef = getProfileNode(nodeRef, profileName);
        if (profileNodeRef != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("delete ProfileNode :" + profileNodeRef);
            }
            nodeService.deleteNode(profileNodeRef);
        }
    }

    private NodeRef getProfileNode(final NodeRef nodeRef, final String profileName) {
        final List<ChildAssociationRef> listOfProfilesAssoc =
                nodeService.getChildAssocs(nodeRef, getProfileAssocQName(), RegexQNamePattern.MATCH_ALL);
        NodeRef profileNodeRef;
        String currentProfileName;
        for (final ChildAssociationRef profileAssoc : listOfProfilesAssoc) {
            profileNodeRef = profileAssoc.getChildRef();
            // Get profileName
            if (logger.isTraceEnabled()) {
                logger.trace("get ProfileName on node:" + profileNodeRef);
            }
            currentProfileName = (String) nodeService.getProperty(profileNodeRef, getProfileNameQName());
            if (currentProfileName.equals(profileName)) {
                return profileNodeRef;
            }
        }
        return null;
    }

    private Map<String, NodeRef> getProfileNodesMap(final NodeRef nodeRef) {
        final Map<String, NodeRef> profileNodes = new HashMap<>();
        final List<ChildAssociationRef> listOfProfilesAssoc =
                nodeService.getChildAssocs(nodeRef, getProfileAssocQName(), RegexQNamePattern.MATCH_ALL);
        NodeRef profileNodeRef;
        String profileName;
        for (final ChildAssociationRef profileAssoc : listOfProfilesAssoc) {
            profileNodeRef = profileAssoc.getChildRef();
            // Get profileName
            if (logger.isTraceEnabled()) {
                logger.trace("get ProfileName on node:" + profileNodeRef);
            }
            profileName = (String) nodeService.getProperty(profileNodeRef, getProfileNameQName());
            profileNodes.put(profileName, profileNodeRef);
        }
        return profileNodes;
    }

    private List<NodeRef> getProfileNodesList(final NodeRef nodeRef) {
        final List<NodeRef> profileNodes = new ArrayList<>();
        final List<ChildAssociationRef> listOfProfilesAssoc =
                nodeService.getChildAssocs(nodeRef, getProfileAssocQName(), RegexQNamePattern.MATCH_ALL);
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

    // Service
    private NodeRef createServiceNode(
            final NodeRef profileNodeRef, final String serviceName, final Set<String> permissionSet) {
        final Map<QName, Serializable> contentProps = new HashMap<>();
        contentProps.put(getServiceNameQName(), serviceName);
        final ArrayList<String> permissionList = new ArrayList<>(permissionSet);
        contentProps.put(getPermissionSetQName(), permissionList);
        // create service node
        if (logger.isTraceEnabled()) {
            logger.trace("Create ServiceNode on node:" + profileNodeRef);
        }
        final ChildAssociationRef serviceAssociation =
                nodeService.createNode(
                        profileNodeRef,
                        getServiceAssocQName(),
                        QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, serviceName),
                        getServiceQName(),
                        contentProps);
        return serviceAssociation.getChildRef();
    }

    private NodeRef updateServiceNode(
            final NodeRef nodeRef, final String serviceName, final Set<String> permissionSet) {
        final NodeRef serviceNodeRef = getServiceNode(nodeRef, serviceName);
        if (serviceNodeRef != null) {
            final ArrayList<String> permissionList = new ArrayList<>(permissionSet);
            if (logger.isTraceEnabled()) {
                logger.trace("Update permissionSet on ServiceNode:" + serviceNodeRef);
            }
            nodeService.setProperty(serviceNodeRef, getPermissionSetQName(), permissionList);
            return serviceNodeRef;
        }
        return null;
    }

    private NodeRef getServiceNode(final NodeRef profileNodeRef, final String serviceName) {
        final List<ChildAssociationRef> listOfServiceAssoc =
                nodeService.getChildAssocs(
                        profileNodeRef, getServiceAssocQName(), RegexQNamePattern.MATCH_ALL);
        NodeRef serviceNodeRef;
        String currentServiceName;
        for (final ChildAssociationRef profileAssoc : listOfServiceAssoc) {
            serviceNodeRef = profileAssoc.getChildRef();
            // Get serviceName
            if (logger.isTraceEnabled()) {
                logger.trace("get ServiceName on ServiceNode:" + serviceNodeRef);
            }
            currentServiceName = (String) nodeService.getProperty(serviceNodeRef, getServiceNameQName());
            if (currentServiceName.equals(serviceName)) {
                return serviceNodeRef;
            }
        }
        return null;
    }

    private Map<String, NodeRef> getServiceNodes(final NodeRef profileNodeRef) {
        final Map<String, NodeRef> serviceNodes = new HashMap<>();
        final List<ChildAssociationRef> listOfServicesAssoc =
                nodeService.getChildAssocs(
                        profileNodeRef, getServiceAssocQName(), RegexQNamePattern.MATCH_ALL);
        NodeRef serviceNodeRef;
        String serviceName;
        for (final ChildAssociationRef serviceAssoc : listOfServicesAssoc) {
            serviceNodeRef = serviceAssoc.getChildRef();
            // Get serviceName
            if (logger.isTraceEnabled()) {
                logger.trace("get ServiceName on ServiceNode:" + serviceNodeRef);
            }
            serviceName = (String) nodeService.getProperty(serviceNodeRef, getServiceNameQName());
            serviceNodes.put(serviceName, serviceNodeRef);
        }
        return serviceNodes;
    }

    private Set<String> getServicePermissionSet(
            final NodeRef profileNodeRef, final String serviceName) {
        final NodeRef serviceNodeRef = getServiceNode(profileNodeRef, serviceName);
        if (logger.isTraceEnabled()) {
            logger.trace("get permissionSet on ServiceNode:" + serviceNodeRef);
        }
        Serializable property = nodeService.getProperty(serviceNodeRef, getPermissionSetQName());
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
        final Set<String> permissionSet = new HashSet<>(permissionList);
        return permissionSet;
    }

    private Map<String, Set<String>> getServicePermissionSet(
            final NodeRef profileNodeRef, final Map<String, NodeRef> servicesNodeRef) {
        final Map<String, Set<String>> servicePermissionSet = new HashMap<>();
        // NodeRef serviceNodeRef;
        Set<String> permissionSet;
        for (final String service : servicesNodeRef.keySet()) {
            // serviceNodeRef = servicesNodeRef.get(service);
            permissionSet = getServicePermissionSet(profileNodeRef, service);
            servicePermissionSet.put(service, permissionSet);
        }
        return servicePermissionSet;
    }

    public Map<String, Profile> getProfileMap(final NodeRef nodeRef) {
        final String key = nodeRef.toString();
        final Map<String, Profile> result = profileMapCache.get(key);
        final Map<String, NodeRef> profileNodes = getProfileNodesMap(nodeRef);

        if (result != null) {
            if (result.size() != 0 && result.size() == profileNodes.size()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("CACHED PROFILE MAP FOUND for node:" + nodeRef);
                }

                return result;
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("WORK ARROUND: REMOVE EMPTY CACHE PROFILE MAP for node:" + key);
                }
                profileMapCache.remove(key);
            }
        }

        final Map<String, Profile> profileMap = new HashMap<>();

        Profile profile;

        for (final Map.Entry<String, NodeRef> entry : profileNodes.entrySet()) {
            profile = createProfile(entry.getValue());
            if (logger.isTraceEnabled()) {
                logger.trace("get profileGroupName on Node:" + entry.getValue());
            }
            profile.setNodeRef(entry.getValue());
            profileMap.put(entry.getKey(), profile);
        }

        if (profileMap.size() != 0) {
            profileMapCache.put(key, profileMap);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("NO PROFILE MAP VALUES FOUND for node:" + nodeRef);
            }
        }

        Collections.unmodifiableMap(profileMap);

        return profileMap;
    }

    private void deleteProfileMap(final NodeRef nodeRef, final String profileName) {
        deleteProfileNode(nodeRef, profileName);
    }

    public Profile updateProfile(
            final NodeRef nodeRef,
            final String profileName,
            final Map<String, Set<String>> servicesPermissions,
            final boolean updateProfileMap) {
        profileMapCache.remove(nodeRef.toString());

        Profile profile = null;
        if (getProfilePrefix() == null) {
            throw new ProfileException(profileName, "This node has not Circabc Aspect");
        }

        if (!getProfileMap(nodeRef).containsKey(profileName)) {
            throw new ProfileException(profileName, "Profile not exist");
        } else {
            final Map<String, Set<String>> filteredServicesPermissions =
                    filterServicesForNode(servicesPermissions);
            // get the Profile
            profile = getProfileMap(nodeRef).get(profileName);
            profile.clearNodeServicesPermissions(
                    nodeRef, filteredServicesPermissions.keySet(), serviceRegistry);
            profile.setServicesPermissions(filteredServicesPermissions);
            profile.setNodeServicesPermissions(nodeRef, filteredServicesPermissions, serviceRegistry);

            if (updateProfileMap) {
                updateProfileMap(nodeRef, profile);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateProfile(profile:" + profileName + ", node:" + nodeRef + ")");
        }
        return profile;
    }

    public void updateProfileMap(final NodeRef nodeRef, final Profile newProfile) {
        final NodeRef profileNodeRef = updateProfileNode(nodeRef, newProfile);
        Set<String> permissionSet;
        NodeRef serviceNodeRef;
        final Map<String, Set<String>> servicesPermissions = newProfile.getServicesPermissions();
        for (final Map.Entry<String, Set<String>> entry : servicesPermissions.entrySet()) {
            permissionSet = entry.getValue();
            serviceNodeRef = getServiceNode(profileNodeRef, entry.getKey());
            if (serviceNodeRef != null) {
                updateServiceNode(profileNodeRef, entry.getKey(), permissionSet);
            } else {
                createServiceNode(profileNodeRef, entry.getKey(), permissionSet);
            }
        }

        profileMapCache.remove(nodeRef.toString());
    }

    /**
     *
     */
    public void addProfile(
            final NodeRef nodeRef,
            final String profileName,
            final String authorityGroupName,
            final MLText title,
            final MLText description,
            final Map<String, Set<String>> servicesPermissions)
            throws ProfileException {
        addProfileImpl(
                nodeRef,
                profileName,
                authorityGroupName,
                title,
                description,
                servicesPermissions,
                false,
                null);
    }

    /**
     * @param fromIgNoderef
     * @param imported
     */
    private Profile addProfileImpl(
            final NodeRef nodeRef,
            final String profileName,
            final String authorityGroupName,
            final MLText title,
            final MLText description,
            final Map<String, Set<String>> servicesPermissions,
            final boolean imported,
            final NodeRef fromIgNoderef)
            throws ProfileException {
        profileMapCache.remove(nodeRef.toString());
        Profile profile = null;

        if (getProfilePrefix() == null) {
            throw new ProfileException(profileName, "This node has not Circabc Aspect");
        }

        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(profileName, "Node has not the specified aspect");
        }

        if (getProfileMap(nodeRef) != null && getProfileMap(nodeRef).containsKey(profileName)) {
            throw new ProfileException(profileName, "Profile already exist");
        } else {
            final CircabcRootProfileManagerService circabcRootProfileManagerService =
                    profileManagerServiceFactory.getCircabcRootProfileManagerService();

            // create a new Profile
            profile =
                    new ProfileImpl(
                            nodeRef,
                            profileName,
                            authorityGroupName,
                            this.getInvitedUsersGroupName(nodeRef),
                            title,
                            description,
                            serviceRegistry,
                            circabcRootProfileManagerService);

            if (imported) {
                profile.setImported(true);
                profile.setImportedNodeRef(fromIgNoderef);
                profile.setImportedNodeName(
                        (String) nodeService.getProperty(fromIgNoderef, ContentModel.PROP_NAME));
            }

            final Map<String, Set<String>> filteredServicesPermissions =
                    filterServicesForNode(servicesPermissions);
            profile.setNodeServicesPermissions(nodeRef, filteredServicesPermissions, serviceRegistry);
            profile.getServicesPermissions().putAll(servicesPermissions);
            updateProfileMap(nodeRef, profile);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("addProfile(profile:" + profileName + ", node:" + nodeRef + ")");
        }

        return profile;
    }

    /**
     *
     */
    public void addProfile(
            final NodeRef nodeRef,
            final String profileName,
            final String authorithyGroupName,
            final Map<String, Set<String>> servicesPermissions)
            throws ProfileException {
        addProfile(nodeRef, profileName, authorithyGroupName, null, null, servicesPermissions);
    }

    /**
     *
     */
    public void addProfile(
            final NodeRef nodeRef,
            final String profileName,
            final Map<String, Set<String>> servicesPermissions)
            throws ProfileException {
        addProfile(nodeRef, profileName, null, null, null, servicesPermissions);
    }

    protected Map<String, Set<String>> filterServicesForNode(
            final Map<String, Set<String>> servicesPermissions) {
        final Map<String, Set<String>> filteredServicesPermissions = new HashMap<>();

        for (final Map.Entry<String, Set<String>> entry : servicesPermissions.entrySet()) {
            if (getServices().contains(entry.getKey())) {
                filteredServicesPermissions.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredServicesPermissions;
    }

    /**
     *
     */
    public void deleteProfile(
            final NodeRef nodeRef, final String profileName, final Boolean cleanPermission)
            throws ProfileException {
        final Profile profile = getProfileMap(nodeRef).get(profileName);
        if (profile == null) {
            throw new ProfileException(profileName, "Profile not exist");
        } else {
            final String alfrescoGroupName = profile.getPrefixedAlfrescoGroupName();
            if (authorityService.authorityExists(alfrescoGroupName)) {
                final Set<String> authorithyContainedInProfile =
                        authorityService.getContainedAuthorities(null, alfrescoGroupName, true);

                if (!profile.isImported()) {
                    if (!authorithyContainedInProfile.isEmpty()) {
                        throw new ProfileException(profileName, "Tried to delete non empty profile!!");
                    }
                    // Remove the authority
                    authorityService.deleteAuthority(alfrescoGroupName);
                }
                // clear all permissions for that profile recursivly
                // TODO Verify if alfresco don't do that already
                if (cleanPermission) {
                    clearPermissionRecursively(nodeRef, alfrescoGroupName);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Authority " + alfrescoGroupName + " does not  exists.");
                }
            }
            deleteProfileMap(nodeRef, profileName);

            final String key = nodeRef.toString();
            profileMapCache.remove(key);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("deleteProfile(profile:" + profileName + ", node:" + nodeRef);
        }
    }

    public Profile getProfile(final NodeRef nodeRef, final String profileName) {
        if (getProfilePrefix() == null) {
            throw new ProfileException("renameProfile", "This node has not Circabc Aspect");
        }
        // Remark: even if the associated profile is renamed, the associated
        // group name is left unchanged

        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(profileName, "Node has not the specified aspect");
        }

        final Map<String, Profile> profileMap = getProfileMap(nodeRef);
        final Profile profile = profileMap.get(profileName);
        if (profile == null) {
            throw new ProfileException(
                    profileName, "Profile not exist " + " for profiles: " + profileMap);
        } else {
            return profile;
        }
    }

    public Profile getProfileNoCache(final NodeRef nodeRef, final String profileName) {
        profileMapCache.remove(nodeRef.toString());
        return getProfile(nodeRef, profileName);
    }

    public abstract List<Profile> getProfilesRecursivly(final NodeRef nodeRef);

    public List<Profile> getProfiles(final NodeRef nodeRef) {
        final List<Profile> profileList = new ArrayList<>();
        final List<NodeRef> profileNodes = getProfileNodesList(nodeRef);

        Profile profile;
        for (final NodeRef profileNodeRef : profileNodes) {
            profile = createProfile(profileNodeRef);
            profileList.add(profile);
        }

        return profileList;
    }

    private Profile createProfile(final NodeRef profileNodeRef) {
        final Profile profile = new ProfileImpl();
        profile.setNodeRef(profileNodeRef);

        Map<String, NodeRef> servicesNodeRef;
        String curentPrefixedProfileGroupName;
        String curentProfileGroupName;
        String profileName;
        boolean isExported = false;
        boolean isImported = false;
        NodeRef importedNodeRef = null;
        Map<String, Set<String>> servicesPermissions;

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            final Map<QName, Serializable> properties = nodeService.getProperties(profileNodeRef);

            profileName = (String) properties.get(getProfileNameQName());

            // get is imported
            try {
                final Serializable value = properties.get(ProfileModel.PROP_PROFILE_IMPORTED);
                if (value != null) {
                    isImported = (Boolean) value;
                }
            } catch (final Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("isImported can't be evaluated on node:" + profileNodeRef, e);
                }
            }
            profile.setImported(isImported);

            // get imported node ref
            try {
                final Serializable value = properties.get(ProfileModel.PROP_PROFILE_IMPORTED_REF);
                if (value != null) {
                    importedNodeRef = (NodeRef) value;
                    profile.setImportedNodeRef(importedNodeRef);
                    profile.setImportedNodeName(
                            (String) nodeService.getProperty(importedNodeRef, ContentModel.PROP_NAME));
                }
            } catch (final Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("importedNodeRef can't be evaluated on node:" + profileNodeRef, e);
                }
            }

            try {
                final Serializable value = properties.get(ProfileModel.PROP_PROFILE_EXPORTED);
                if (value != null) {
                    isExported = (Boolean) value;
                }
            } catch (final Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("isExported can't be evaluated on node:" + profileNodeRef, e);
                }
            }
            profile.setExported(isExported);

            profile.setProfileName(profileName);

            if (logger.isTraceEnabled()) {
                logger.trace("get profileGroupName on Node:" + profileNodeRef);
            }
            curentPrefixedProfileGroupName = (String) properties.get(getProfileGroupNameQName());

            if (!CircabcConstant.GUEST_AUTHORITY.equals(curentPrefixedProfileGroupName)) {
                curentProfileGroupName =
                        curentPrefixedProfileGroupName.substring(
                                GROUP_UNDESCORE.length(), curentPrefixedProfileGroupName.length());
                profile.setAlfrescoGroupName(curentProfileGroupName);
            } else {
                profile.setAlfrescoGroupName(curentPrefixedProfileGroupName);
            }

            profile.setTitles((MLText) properties.get(ContentModel.PROP_TITLE));
            profile.setDescriptions((MLText) properties.get(ContentModel.PROP_DESCRIPTION));

            profile.setPrefixedAlfrescoGroupName(curentPrefixedProfileGroupName);
            servicesNodeRef = getServiceNodes(profileNodeRef);
            servicesPermissions = getServicePermissionSet(profileNodeRef, servicesNodeRef);
            profile.setServicesPermissions(servicesPermissions);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return profile;
    }

    public void renameProfile(
            final NodeRef nodeRef, final String oldProfileName, final String newProfileName)
            throws ProfileException {
        profileMapCache.remove(nodeRef.toString());

        if (getProfilePrefix() == null) {
            throw new ProfileException("renameProfile", "This node has not Circabc Aspect");
        }

        if (oldProfileName.contains(":") || newProfileName.contains(":")) {
            throw new ProfileException("renameProfile", "profileName should not contains semicolon");
        }
        // Remark: even if the associated profile is renamed, the associated
        // group name is left unchanged

        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(oldProfileName, "Node has not the specified aspect");
        }

        final Profile profile = getProfileMap(nodeRef).remove(oldProfileName);
        if (profile == null) {
            throw new ProfileException(oldProfileName, "Profile not exist");
        }
        if (profile.isImported()) {
            throw new ProfileException(oldProfileName, "Impossible rename an imported profile");
        } else {
            profile.setProfileName(newProfileName);
            getProfileMap(nodeRef).put(newProfileName, profile);
            updateProfileMap(nodeRef, profile);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "renameProfile(oldName:"
                            + oldProfileName
                            + ", newName:"
                            + newProfileName
                            + ", node:"
                            + nodeRef);
        }
    }

    /**
     * recursively clear all permissions for an authority
     */
    private void clearPermissionRecursively(final NodeRef nodeRef, final String authority) {
        // permissionService.clearPermission(nodeRef, authority);
        final Set<AccessPermission> accessPermissions = permissionService.getAllSetPermissions(nodeRef);
        for (final AccessPermission accessPermission : accessPermissions) {
            if (accessPermission.getAuthority().equals(authority)) {
                permissionService.clearPermission(nodeRef, authority);
                break;
            }
        }

        // clear permissions on files, discussions, ...
        final List<FileInfo> inList = fileFolderService.list(nodeRef);
        for (final FileInfo fileInfo : inList) {
            clearPermissionRecursively(fileInfo.getNodeRef(), authority);
        }
    }

    public String getRegistredGroupName(final NodeRef nodeRef) {
        if (hasParentSubsGroup()) {
            final NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
            final ProfileManagerService profileManagerService =
                    profileManagerServiceFactory.getProfileManagerService(parentNodeRef);
            final String subsGroupName = profileManagerService.getSubsGroupName(parentNodeRef);
            return subsGroupName;
        } else {
            return null;
        }
    }

    public void addApplicantPerson(
            final NodeRef nodeRef,
            final String userID,
            final String message,
            final String firstName,
            final String lastName)
            throws ProfileException {
        addApplicantPerson(nodeRef, userID, message, firstName, lastName, new Date());
    }

    public void addApplicantPerson(
            final NodeRef nodeRef,
            final String userID,
            final String message,
            final String firstName,
            final String lastName,
            final Date date)
            throws ProfileException {
        if (hasApplicantFeature()) {
            // get the applicantMap and add the user in it
            Map<String, Applicant> applicantMap =
                    (Map<String, Applicant>) nodeService.getProperty(nodeRef, getApplicantUsersMapQName());
            if (applicantMap == null) {
                applicantMap = new HashMap<>();
            }
            applicantMap.put(userID, new Applicant(userID, date, message, firstName, lastName));
            nodeService.setProperty(nodeRef, getApplicantUsersMapQName(), (Serializable) applicantMap);

            if (logger.isDebugEnabled()) {
                final String igName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

                logger.debug(
                        "The user "
                                + userID
                                + " has requested to become a member of the interest group '"
                                + igName
                                + "'");
            }
        }
    }

    public void removeApplicantPerson(final NodeRef nodeRef, final String userID)
            throws ProfileException {
        if (hasApplicantFeature()) {
            // get the applicantMap and remove the user
            final Map<String, Applicant> applicantMap =
                    (Map<String, Applicant>) nodeService.getProperty(nodeRef, getApplicantUsersMapQName());
            if (applicantMap.containsKey(userID)) {
                applicantMap.remove(userID);
                nodeService.setProperty(nodeRef, getApplicantUsersMapQName(), (Serializable) applicantMap);
            }

            if (logger.isDebugEnabled()) {
                final String igName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

                logger.debug(
                        "The application of "
                                + userID
                                + " has been refused to become a member of '"
                                + igName
                                + "'");
            }
        }
    }

    public final void addPersonToProfile(
            final NodeRef nodeRef, final String userID, final String profileName)
            throws ProfileException {
        invitedUsersCache.remove(nodeRef.toString());
        if (getProfilePrefix() == null) {
            throw new ProfileException(profileName, "This node has not Circabc Aspect");
        }
        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(profileName, "Node has not the specified aspect");
        }

        String personProfile = this.getPersonProfile(nodeRef, userID);
        final CircabcRootProfileManagerService circabcRootProfileManagerService =
                profileManagerServiceFactory.getCircabcRootProfileManagerService();
        if (personProfile != null
                && !personProfile.equalsIgnoreCase(
                circabcRootProfileManagerService.getAllCircaUsersGroupName())) {

            throw new ProfileException(
                    profileName, "User " + userID + " already has a profile in the group");
        }

        if (hasApplicantFeature()) {
            // get the applicantMap and remove the user
            final Map<String, Applicant> applicantMap =
                    (Map<String, Applicant>) nodeService.getProperty(nodeRef, getApplicantUsersMapQName());
            if (applicantMap != null && applicantMap.containsKey(userID)) {
                applicantMap.remove(userID);
            }
        }

        final Profile profile = getProfileMap(nodeRef).get(profileName);
        if (profile != null) {
            final String profileGroupName = profile.getPrefixedAlfrescoGroupName();
            authorityService.addAuthority(profileGroupName, userID);
        } else {
            throw new ProfileException(
                    profileName, "addPersonToProfile failed because profile name doesn't exist on the node");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "addPersonToProfile(userID:"
                            + userID
                            + ", profileName:"
                            + profileName
                            + " node:"
                            + nodeRef);
        }
    }

    public final void changePersonProfile(
            final NodeRef nodeRef, final String userID, final String targetProfileName)
            throws ProfileException {
        if (getProfilePrefix() == null) {
            throw new ProfileException(targetProfileName, "This node has not Circabc Aspect");
        }
        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(targetProfileName, "Node has not the specified aspect");
        }

        final Profile profile = getProfileMap(nodeRef).get(targetProfileName);

        if (profile != null) {
            // scan the existing profiles and determine the original one
            final String originalProfileName = this.getPersonProfile(nodeRef, userID);
            if (originalProfileName == null) {
                // that mean that the user can not be moved becaus he wasn't in
                // any
                // profile
                throw new ProfileException(
                        targetProfileName,
                        "movePerson failed because user "
                                + userID
                                + " is not invited yet on the node:"
                                + nodeRef);
            }
            final String originalProfileGroupName =
                    this.getProfile(nodeRef, originalProfileName).getPrefixedAlfrescoGroupName();
            if (originalProfileGroupName == null) {
                // that mean that the user can not be moved becaus he wasn't in
                // any
                // profile
                throw new ProfileException(
                        targetProfileName, "movePerson failed because original group wasn't found");
            }
            // remove the user from the his original group
            authorityService.removeAuthority(originalProfileGroupName, userID);

            // Set authorithy to targetProfile
            final String profileGroupName = profile.getPrefixedAlfrescoGroupName();
            authorityService.addAuthority(profileGroupName, userID);
        } else {
            throw new ProfileException(
                    targetProfileName,
                    "addPersonToProfile failed because profile name doesn't exist on the node");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "changePersonProfile(userID:"
                            + userID
                            + ", targetProfileName"
                            + targetProfileName
                            + " node:"
                            + nodeRef);
        }
    }

    public void uninvitePerson(
            final NodeRef nodeRef, final String userID, final Boolean cleanPermission)
            throws ProfileException {
        invitedUsersCache.remove(nodeRef.toString());
        if (getProfilePrefix() == null) {
            throw new ProfileException(userID, "This node has not Circa Aspect");
        }
        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(userID, "Node has not the specified aspect");
        }
        final String userProfileGroup = getPersonProfileGroup(nodeRef, userID);

        if (userProfileGroup == null) {
            throw new ProfileException(userID, "User doesn't belong to the user group");
        }

        // remove the user from his profile group
        authorityService.removeAuthority(userProfileGroup, userID);

        if (cleanPermission) {
            clearPermissionRecursively(nodeRef, userID);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("uninvitePerson(userID:" + userID + " node:" + nodeRef);
        }
    }

    public final String getPersonProfile(final NodeRef nodeRef, final String userID) {
        if (getProfilePrefix() == null) {
            throw new ProfileException(userID, "This node has not Circabc Aspect");
        }
        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(userID, "Node has not the specified aspect");
        }

        // retrieve what was the original profile of the user
        final Set<String> containingAuthoritiesForUser =
                authorityService.getContainingAuthorities(AuthorityType.GROUP, userID, true);

        final Map<String, Profile> profileMap = getProfileMap(nodeRef);
        final CircabcRootProfileManagerService circabcRootProfileManagerService =
                profileManagerServiceFactory.getCircabcRootProfileManagerService();
        final String allCircaUsersGroupName =
                circabcRootProfileManagerService.getAllCircaUsersGroupName();

        String personProfile = null;

        for (final Profile profile : profileMap.values()) {
            if (profile.getAlfrescoGroupName().equals(allCircaUsersGroupName)) {
                // we don't search in ALL_USERS group
            } else if (containingAuthoritiesForUser.contains(profile.getPrefixedAlfrescoGroupName())) {
                personProfile = profile.getProfileName();

                if (profile.isImported() == false) {
                    break;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "return: " + personProfile + " getPersonProfile(userID:" + userID + " node:" + nodeRef);
        }
        return personProfile;
    }

    public final String getPersonProfileGroup(final NodeRef nodeRef, final String userID) {
        if (getProfilePrefix() == null) {
            throw new ProfileException(userID, "This node has not Circabc Aspect");
        }
        // first check if rootNodeRef has specified aspect
        if (!nodeService.hasAspect(nodeRef, getNodeAspect())) {
            throw new ProfileException(userID, "Node has not the specified aspect");
        }

        // retrieve what was the original profile of the user
        final Set<String> containingAuthoritiesForUser =
                authorityService.getContainingAuthorities(AuthorityType.GROUP, userID, true);

        containingAuthoritiesForUser.remove(GROUP_UNDESCORE + ALL_CIRCA_USERS_PROFILE_NAME);

        final Map<String, Profile> profileMap = getProfileMap(nodeRef);

        for (final Profile profile : profileMap.values()) {
            if (profile.isImported()) {
                continue;
            }
            if (containingAuthoritiesForUser.contains(profile.getPrefixedAlfrescoGroupName())) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "return: "
                                    + profile.getPrefixedAlfrescoGroupName()
                                    + " getPersonProfileGroup(userID:"
                                    + userID
                                    + " node:"
                                    + nodeRef);
                }
                return profile.getPrefixedAlfrescoGroupName();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "return: " + null + " getPersonProfileGroup(userID:" + userID + " node:" + nodeRef);
        }
        return null;
    }

    /**
     * try to find the parent node (recursivly) who has the current Aspect Current Aspect depend on
     * the Implementation of this class (IGRoot, Category, CircaBC, ...)
     */
    public final NodeRef getCurrentAspectRoot(final NodeRef nodeRef) {
        if (getProfilePrefix() == null) {
            throw new ProfileException(nodeRef.toString(), "This node has not Circabc Aspect");
        }
        if (nodeRef == null) {
            throw new ProfileException("", "node null in getRoot");
        }
        NodeRef node = nodeRef;
        ChildAssociationRef ca;
        while (node != null) {
            if (nodeService.hasAspect(node, getNodeRootAspect())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("return node: " + node + " getCurrentAspectRoot(node:" + nodeRef + ")");
                }
                return node;
            }

            ca = nodeService.getPrimaryParent(node);
            if (ca == null) {
                return null;
            }
            node = ca.getParentRef();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("return null: getCurrentAspectRoot(node:" + nodeRef + ")");
        }
        return null;
    }

    /**
     * TODO ceci ne devrait pas etre ici
     *
     * @deprecated
     */
    @Deprecated
    public final NodeRef getCircaHome(final NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new ProfileException("", "node null in getCircaHome");
        }
        NodeRef node = nodeRef;
        ChildAssociationRef ca;
        while (node != null) {
            if (nodeService.hasAspect(node, getCircaHomeNodeAspect())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("return node: " + node + " getCurrentAspectRoot(node:" + nodeRef + ")");
                }
                return node;
            }

            ca = nodeService.getPrimaryParent(node);
            if (ca == null) {
                return null;
            }
            node = ca.getParentRef();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("return null: getCurrentAspectRoot(node:" + nodeRef + ")");
        }
        return null;
    }

    public final Set<String> getPersonInProfile(final NodeRef nodeRef, final String profileName) {
        if (getProfilePrefix() == null) {
            throw new ProfileException(nodeRef.toString(), "This node has not Circabc Aspect");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getPersonInProfile(profileName:" + profileName + ", node:" + nodeRef);
        }
        final Map<String, Profile> profileMap = getProfileMap(nodeRef);

        Set<String> personsInProfile = new HashSet<>();
        for (final Profile profile : profileMap.values()) {
            if (profileName.equals(profile.getProfileName())) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "find: "
                                    + profile.getProfileName()
                                    + " group: "
                                    + profile.getPrefixedAlfrescoGroupName()
                                    + " on node:"
                                    + nodeRef);
                }
                if (profileManagerServiceFactory
                        .getCircabcRootProfileManagerService()
                        .getPrefixedAllCircaUsersGroupName()
                        .equals(profile.getPrefixedAlfrescoGroupName())
                        || CircabcConstant.GUEST_AUTHORITY.equals(profile.getPrefixedAlfrescoGroupName())) {
                    break;
                }

                if (authorityService.authorityExists(profile.getPrefixedAlfrescoGroupName())) {
                    personsInProfile =
                            authorityService.getContainedAuthorities(
                                    AuthorityType.USER, profile.getPrefixedAlfrescoGroupName(), true);
                    break;
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("This group don't exist anymore:" + profile.getPrefixedAlfrescoGroupName());
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            for (final String personne : personsInProfile) {
                logger.debug("personne:" + personne);
            }
        }
        return personsInProfile;
    }

    /**
     * Create a "AllUsers" for the node
     */
    public String createMasterGroup(final NodeRef nodeRef) {

        String parentSubsGroupName = null;
        String prefixedParentSubsGroupName = null;
        if (hasMasterParentGroup()) {
            final NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
            final ProfileManagerService parentProfileManagerService =
                    getProfileManagerServiceFactory().getProfileManagerService(parentNodeRef);
            parentSubsGroupName = parentProfileManagerService.getSubsGroupName(parentNodeRef);
            if (parentSubsGroupName != null) {
                // Full AuthorityIdentifier
                prefixedParentSubsGroupName =
                        authorityService.getName(AuthorityType.GROUP, parentSubsGroupName);
            }
        }

        // get name of the folder
        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replaceAll(" ", "");
        // create the group introducing folder name in the group name
        final String masterGroupName = folderName + "--MasterGroup--" + GUID.generate();

        // create the group as a root authority
        // Migration 3.1 -> 3.4.6 - 09/12/2011
        // createAuthority() method changed for version 3.4
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        masterGroupName,
                        masterGroupName,
                        authorityService.getDefaultZones());

        if (prefixedParentSubsGroupName != null && createdAuthority != null) {
            String prefixedMasterGroupName =
                    authorityService.getName(AuthorityType.GROUP, masterGroupName);
            authorityService.addAuthority(prefixedParentSubsGroupName, prefixedMasterGroupName);
        }

        nodeService.setProperty(nodeRef, getMasterGroupQName(), masterGroupName);

        return masterGroupName;
    }

    /**
     * Create a new GROUP of InvitedUsers inside the "Parent" RegistredUsers (if exist)
     */
    public String createInvitedUsersGroup(final NodeRef nodeRef) {

        String prefixedMasterGroupName = null;
        final String masterGroupName = this.getMasterInvitedGroupName(nodeRef);
        if (masterGroupName != null) {
            // Full AuthorityIdentifier
            prefixedMasterGroupName = authorityService.getName(AuthorityType.GROUP, masterGroupName);
        }

        // get name of the folder
        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replaceAll(" ", "");
        // create the group introducing folder name in the group name
        final String invitedUsersGroupName = folderName + "--InvitedUsersGroup--" + GUID.generate();

        // Migration 3.1 -> 3.4.6 - 09/12/2011
        // createAuthority() method changed for version 3.4
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        invitedUsersGroupName,
                        invitedUsersGroupName,
                        authorityService.getDefaultZones());

        if (createdAuthority != null) {
            String prefixedInvitedUsersGroupName =
                    authorityService.getName(AuthorityType.GROUP, invitedUsersGroupName);
            authorityService.addAuthority(prefixedMasterGroupName, prefixedInvitedUsersGroupName);
        }

        nodeService.setProperty(nodeRef, getInvitedUsersGroupQName(), invitedUsersGroupName);

        return invitedUsersGroupName;
    }

    /**
     * Create a new GROUP of RegistredUsers inside the "Parent" RegistredUsers (if exist)
     */
    public String createSubsGroup(final NodeRef nodeRef) {

        String prefixedMasterGroupName = null;
        final String masterGroupName = this.getMasterInvitedGroupName(nodeRef);
        if (masterGroupName != null) {
            // Full AuthorityIdentifier
            prefixedMasterGroupName = authorityService.getName(AuthorityType.GROUP, masterGroupName);
        }

        // get name of the folder
        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replaceAll(" ", "");
        // create the group introducing folder name in the group name
        final String subsGroupName = folderName + "--SubsGroup--" + GUID.generate();

        // Migration 3.1 -> 3.4.6 - 09/12/2011
        // createAuthority() method changed for version 3.4
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP, subsGroupName, subsGroupName, authorityService.getDefaultZones());

        if (createdAuthority != null) {
            String prefixedInvitedUsersGroupName =
                    authorityService.getName(AuthorityType.GROUP, subsGroupName);
            authorityService.addAuthority(prefixedMasterGroupName, prefixedInvitedUsersGroupName);
        }

        nodeService.setProperty(nodeRef, getSubsGroupQName(), subsGroupName);

        return subsGroupName;
    }

    /**
     * Return a Set with users that are related to this node Invited + SubsGroup Users
     */
    public final Set<String> getMasterUsers(final NodeRef nodeRef) {
        final String userGroupName = getMasterInvitedGroupName(nodeRef);

        final String prefixedUserGroupName =
                authorityService.getName(AuthorityType.GROUP, userGroupName);
        Set<String> usersSet = Collections.emptySet();

        if (authorityService.authorityExists(prefixedUserGroupName)) {
            usersSet =
                    authorityService.getContainedAuthorities(
                            AuthorityType.USER, prefixedUserGroupName, false);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Authority does not exists: " + prefixedUserGroupName);
            }
        }
        return usersSet;
    }

    /**
     * Return a Set with users that are invited to this node
     */
    public final Set<String> getInvitedUsers(final NodeRef nodeRef) {

        String nodeRefAsString = nodeRef.toString();

        Set<String> cachedUserSet = invitedUsersCache.get(nodeRefAsString);
        Set<String> usersSet = Collections.emptySet();
        if (cachedUserSet == null) {
            final String userGroupName = getInvitedUsersGroupName(nodeRef);
            final String prefixedUserGroupName =
                    authorityService.getName(AuthorityType.GROUP, userGroupName);
            if (authorityService.authorityExists(prefixedUserGroupName)) {
                usersSet =
                        authorityService.getContainedAuthorities(
                                AuthorityType.USER, prefixedUserGroupName, false);
                invitedUsersCache.put(nodeRefAsString, usersSet);

            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Error get contained authorities for group " + prefixedUserGroupName);
                }
            }
        } else {
            usersSet = new TreeSet<>(cachedUserSet);
        }
        return usersSet;
    }

    public Map<String, Profile> getInvitedUsersProfiles(NodeRef rootNode) {
        Map<String, Profile> resultMap = new HashMap<>();
        final Map<String, Profile> profileMap = getProfileMap(rootNode);

        final String userGroupName = getInvitedUsersGroupName(rootNode);
        final String prefixedUserGroupName =
                authorityService.getName(AuthorityType.GROUP, userGroupName);
        Set<String> groupSet = Collections.emptySet();
        if (authorityService.authorityExists(prefixedUserGroupName)) {
            groupSet =
                    authorityService.getContainedAuthorities(
                            AuthorityType.GROUP, prefixedUserGroupName, true);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error get contained authorities for group "
                                + prefixedUserGroupName
                                + " authority does not exists");
            }
        }
        for (String group : groupSet) {
            if (authorityService.authorityExists(prefixedUserGroupName)) {
                Set<String> usersSet =
                        authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
                for (String user : usersSet) {
                    final String profileKey = getProfileFromAlfrescoPrefixedGroupName(group);
                    final Profile profile = profileMap.get(profileKey);
                    resultMap.put(user, profile);
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Error get contained authorities for group " + group);
                }
            }
        }
        final List<Profile> importedProfiles = getImportedProfilesRecursivly(rootNode);
        for (Profile profile : importedProfiles) {
            String group = profile.getPrefixedAlfrescoGroupName();
            if (authorityService.authorityExists(group)) {
                Set<String> usersSet =
                        authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
                for (String user : usersSet) {
                    if (resultMap.containsKey(user)) {
                        if (logger.isInfoEnabled()) {
                            logger.info(
                                    "User "
                                            + user
                                            + " is members of "
                                            + resultMap.get(user).getProfileName()
                                            + " and "
                                            + profile.getProfileName()
                                            + " only "
                                            + resultMap.get(user).getProfileName()
                                            + " will be returned");
                        }
                    } else {
                        resultMap.put(user, profile);
                    }
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Authority does not exists: " + group);
                }
            }
        }

        return resultMap;
    }

    /**
     * @param group
     * @return
     */
    private String getProfileFromAlfrescoPrefixedGroupName(String group) {
        // GROUP_
        // --bc9fe7c1-ff45-4b6f-a44b-d424c6d01796
        final String profileKey = group.substring(6, group.length() - 38);
        return profileKey;
    }

    /**
     * Return all user that are invited in a subService
     */
    public final Set<String> getAllSubUsers(final NodeRef nodeRef) {
        final NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getChildRef();
        final ProfileManagerService parentProfileManagerService =
                getProfileManagerServiceFactory().getProfileManagerService(parentNodeRef);
        final String userGroupName = parentProfileManagerService.getSubsGroupName(parentNodeRef);

        final String prefixedUserGroupName =
                authorityService.getName(AuthorityType.GROUP, userGroupName);
        Set<String> parentSubsGroupUsersSet = Collections.emptySet();
        if (authorityService.authorityExists(prefixedUserGroupName)) {
            parentSubsGroupUsersSet =
                    authorityService.getContainedAuthorities(
                            AuthorityType.USER, prefixedUserGroupName, false);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Authority does not exists: " + prefixedUserGroupName);
            }
        }

        return parentSubsGroupUsersSet;
    }

    /**
     * Return a Set with users that are registred to this node
     */
    public final Set<String> getRegistredUsers(final NodeRef nodeRef) {
        if (hasParentSubsGroup()) {
            final Set<String> parentSubsGroupUsersSet = getAllSubUsers(nodeRef);

            final Set<String> invitedUsersSet = getInvitedUsers(nodeRef);

            parentSubsGroupUsersSet.removeAll(invitedUsersSet);

            return parentSubsGroupUsersSet;
        }
        return Collections.emptySet();
    }

    /**
     * Return a Set with users that are applicant to this node
     */
    public final Map<String, Applicant> getApplicantUsers(final NodeRef nodeRef) {
        // get the applicantMap and add the user in it
        Map<String, Applicant> applicantMap =
                (Map<String, Applicant>) nodeService.getProperty(nodeRef, getApplicantUsersMapQName());
        if (applicantMap == null) {
            applicantMap = new HashMap<>();
        }
        return applicantMap;
    }

    public String getMasterInvitedGroupName(final NodeRef nodeRef) {
        final String allUsersGroupName =
                (String) nodeService.getProperty(nodeRef, getMasterGroupQName());

        if (logger.isDebugEnabled()) {
            logger.debug("return:" + allUsersGroupName + " + getMasterInvitedGroupName(node:" + nodeRef);
        }
        return allUsersGroupName;
    }

    public final String getInvitedUsersGroupName(final NodeRef nodeRef) {
        final String invitedUsersGroupName =
                (String) nodeService.getProperty(nodeRef, getInvitedUsersGroupQName());

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "return:" + invitedUsersGroupName + " + getInvitedUsersGroupName(node:" + nodeRef);
        }
        return invitedUsersGroupName;
    }

    public String getSubsGroupName(final NodeRef nodeRef) {
        final String subsGroupName = (String) nodeService.getProperty(nodeRef, getSubsGroupQName());

        if (logger.isDebugEnabled()) {
            logger.debug("return:" + subsGroupName + " + getSubsGroupName(node:" + nodeRef);
        }
        return subsGroupName;
    }

    public String getApplicantUsersMapName(final NodeRef nodeRef) {
        final String applicantGroupName =
                (String) nodeService.getProperty(nodeRef, getApplicantUsersMapQName());

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "return:" + applicantGroupName + " + getApplicantUsersGroupName(node:" + nodeRef);
        }
        return applicantGroupName;
    }

    public final QName getMasterGroupQName() {
        /** RegistredGroup: Name of the AllUsersGroup */
        final QName allUsersGroup =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "MasterGroup");
        return allUsersGroup;
    }

    public final QName getInvitedUsersGroupQName() {
        /** MembersGroup: Name of the group of Invited Users */
        final QName invitedUsersGroup =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "InvitedUsersGroup");
        return invitedUsersGroup;
    }

    public final QName getSubsGroupQName() {
        /** SubsGroup: Name of the group of SubsGroup */
        final QName subsGroup =
                QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "SubsGroup");
        return subsGroup;
    }

    public final QName getApplicantUsersMapQName() {
        /** ApplicantGroup: Name of the group of Applicant Users */
        final QName applicantUsersGroup =
                QName.createQName(
                        CIRCABC_CONTENT_MODEL_1_0_URI, getProfilePrefix() + "ApplicantUsersProperty");
        return applicantUsersGroup;
    }

    public final QName getProfileMapQName() {
        /** ProfileMap for the profile */
        final QName profileQName = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "profileMap");
        return profileQName;
    }

    public Profile getProfileFromGroup(final NodeRef nodeRef, final String groupId) {
        final Map<String, Profile> profMap = getProfileMap(nodeRef);

        for (final Profile profile : profMap.values()) {
            if (profile.getPrefixedAlfrescoGroupName().equals(groupId)) {
                return profile;
            }
        }

        return null;
    }

    public abstract String getProfilePrefix();

    /**
     * @return the profileManagerServiceFactory
     */
    public final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public abstract boolean hasApplicantFeature();

    public abstract boolean hasMasterParentGroup();

    public abstract boolean hasParentSubsGroup();

    /**
     * @return the serviceRegistry
     */
    public final ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public final void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @return the services
     */
    public abstract Set<String> getServices();

    public void setAuthenticationService(final MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public List<Profile> getExportedProfiles(final NodeRef nodeRef) {
        final List<Profile> allProfiles = getProfiles(nodeRef);
        final List<Profile> exportedProfiles = new ArrayList<>();
        for (final Profile profile : allProfiles) {
            if (profile.isExported()) {
                exportedProfiles.add(profile);
            }
        }
        return exportedProfiles;
    }

    private List<Profile> getImportedProfilesRecursivly(final NodeRef nodeRef) {
        final List<Profile> allProfiles = getProfilesRecursivly(nodeRef);
        final List<Profile> importedProfiles = new ArrayList<>();
        for (final Profile profile : allProfiles) {
            if (profile.isImported()) {
                importedProfiles.add(profile);
            }
        }
        return importedProfiles;
    }

    public void importProfile(
            final NodeRef toIgNoderef,
            final NodeRef fromIgNoderef,
            final String fromProfileName,
            final Map<String, Set<String>> servicesPermissions) {
        final Profile targetProfile = getProfile(fromIgNoderef, fromProfileName);

        if (targetProfile.isExported() == false) {
            throw new ProfileException(fromProfileName, "Impossible to import an Profile not exported");
        }

        final Serializable fromIgName = nodeService.getProperty(fromIgNoderef, ContentModel.PROP_NAME);

        servicesPermissions.put(
                CircabcServices.VISIBILITY.toString(),
                Collections.singleton(VisibilityPermissions.VISIBILITY.toString()));

        String newProfileName = fromIgName + "_" + targetProfile.getProfileName();
        final Profile prof =
                addProfileImpl(
                        toIgNoderef,
                        newProfileName,
                        targetProfile.getPrefixedAlfrescoGroupName(),
                        targetProfile.getTitle(),
                        targetProfile.getDescription(),
                        servicesPermissions,
                        true,
                        fromIgNoderef);

        NodeRef childRef;
        ProfileManagerService childService;
        for (final ChildAssociationRef assoc :
                nodeService.getChildAssocs(
                        toIgNoderef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL)) {
            childRef = assoc.getChildRef();
            childService = profileManagerServiceFactory.getProfileManagerService(childRef);

            if (childService != null) {
                childService.updateProfile(childRef, prof.getProfileName(), servicesPermissions, true);
            }
        }

        NodeRef exportedNodeRef = targetProfile.getNodeRef();
        NodeRef impotedNodeRef = getProfileNodesMap(toIgNoderef).get(newProfileName);
        if (exportedNodeRef != null && impotedNodeRef != null) {
            nodeService.createAssociation(
                    exportedNodeRef, impotedNodeRef, ProfileModel.ASSOC_PROFILE_IMPORTED_TO);
        }
    }

    public void exportProfile(final NodeRef nodeRef, final String profileName, boolean export) {
        final Profile profile = getProfile(nodeRef, profileName);
        if (profile != null) {
            if (profile.isImported()) {
                // Not possible to export imported Profile
                throw new ProfileException(profileName, "Impossible to export an imported Profile");
            } else {
                if (export) {
                    if (profile.isExported() == false) {
                        final NodeRef profileNodeRef = getProfileNode(nodeRef, profile.getProfileName());
                        nodeService.setProperty(
                                profileNodeRef, ProfileModel.PROP_PROFILE_EXPORTED, Boolean.TRUE);

                        profileMapCache.remove(nodeRef.toString());
                    } else {
                        throw new ProfileException(profileName, "Profile all ready exported");
                    }
                } else {
                    if (profile.isExported() == true) {
                        // TODO verify if profile is not imported in another IG
                        final NodeRef parentNode = nodeService.getPrimaryParent(nodeRef).getParentRef();

                        final List<Profile> profiles = getImportedProfilesRecursivly(parentNode);

                        if (profiles.contains(profile)) {
                            throw new ProfileException(
                                    profileName, "Impossible to unexport: the Profile is imported");
                        } else {
                            final NodeRef profileNodeRef = getProfileNode(nodeRef, profile.getProfileName());
                            nodeService.setProperty(
                                    profileNodeRef, ProfileModel.PROP_PROFILE_EXPORTED, Boolean.FALSE);

                            profileMapCache.remove(nodeRef.toString());
                        }
                    } else {
                        throw new ProfileException(profileName, "Profile was not exported");
                    }
                }
            }
        } else {
            throw new ProfileException(profileName, "Profile not exist");
        }
    }

    public void addProfileTitles(
            final NodeRef nodeRef, final String profileName, final MLText newTitles) {
        final Profile profile = getProfile(nodeRef, profileName);
        if (profile.getTitle() == null) {
            profile.setTitles(newTitles);
        } else {
            profile.getTitle().putAll(newTitles);
        }
        final NodeRef profileNodeRef = getProfileNode(nodeRef, profile.getProfileName());
        nodeService.setProperty(profileNodeRef, ContentModel.PROP_TITLE, profile.getTitle());

        profileMapCache.remove(nodeRef.toString());
    }

    public void addProfileDescriptions(
            final NodeRef nodeRef, final String profileName, final MLText newDescriptions) {
        final Profile profile = getProfile(nodeRef, profileName);
        if (profile.getDescription() == null) {
            profile.setDescriptions(newDescriptions);
        } else {
            profile.getDescription().putAll(newDescriptions);
        }
        final NodeRef profileNodeRef = getProfileNode(nodeRef, profile.getProfileName());
        nodeService.setProperty(
                profileNodeRef, ContentModel.PROP_DESCRIPTION, profile.getDescription());

        profileMapCache.remove(nodeRef.toString());
    }

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    public boolean isProfileDeletable(final NodeRef nodeRef, final String profileName) {
        return getPersonInProfile(nodeRef, profileName).size() < 1;
    }

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    public List<NodeRef> getRefInExportedProfile(final NodeRef nodeRef, final String profileName) {
        final Profile profile = getProfile(nodeRef, profileName);

        if (profile.isExported() == false) {
            throw new ProfileException(profileName, "This profile is not exported");
        }

        final String authority = profile.getPrefixedAlfrescoGroupName();
        final NodeRef category = managementService.getCurrentCategory(nodeRef);
        final List<NodeRef> refs = new ArrayList<>();

        for (final NodeRef ig : managementService.getInterestGroups(category)) {
            if (ig.equals(nodeRef) == false) {
                for (final Profile prof :
                        getProfileManagerServiceFactory().getIGRootProfileManagerService().getProfiles(ig)) {
                    if (prof.isImported() && prof.getPrefixedAlfrescoGroupName().equals(authority)) {
                        refs.add(ig);
                    }
                }
            }
        }

        return refs;
    }

    protected void resetCache() {
        PermissionUtils.resetCache(
                circabcDynamicAuthorityCache, logger, PermissionUtils.CIRCABC_CACHE_NAME);
    }

    /**
     * @param circabcDynamicAuthorityCache the circabcDynamicAuthorityCache to set
     */
    public void setCircabcDynamicAuthorityCache(
            SimpleCache<Serializable, Object> circabcDynamicAuthorityCache) {
        this.circabcDynamicAuthorityCache = circabcDynamicAuthorityCache;
    }

    public SimpleCache<String, Set<String>> getInvitedUsersCache() {
        return invitedUsersCache;
    }

    public void setInvitedUsersCache(SimpleCache<String, Set<String>> invitedUsersCache) {
        this.invitedUsersCache = invitedUsersCache;
    }
}
