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
package eu.cec.digit.circabc.repo.profile.interestGroup;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.PermissionModel;
import eu.cec.digit.circabc.repo.profile.ProfileManagerServiceImpl;
import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class IGRootProfileManagerServiceImpl extends ProfileManagerServiceImpl
        implements IGRootProfileManagerService {

    private static final String PROFILE_PREFIX = "circaIGRoot";

    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(IGRootProfileManagerServiceImpl.class);

    private static final Set<String> services = new HashSet<>();

    public IGRootProfileManagerServiceImpl() {
        services.add(CircabcServices.DIRECTORY.toString());
        services.add(CircabcServices.VISIBILITY.toString());
    }

    /**
     * @return the services
     */
    @Override
    public final Set<String> getServices() {
        return services;
    }

    public List<QName> getQNameServiceRoles() {
        final List<QName> list = new ArrayList<>();
        list.add(PermissionModel.DIRECTORY_PERMISSION);
        return list;
    }

    public Map<QName, String> getServiceRolesEnum() {
        final Map<QName, String> map = new HashMap<>();
        map.put(PermissionModel.DIRECTORY_PERMISSION, CircabcServices.DIRECTORY.toString());
        return map;
    }

    @Override
    public QName getNodeAspect() {
        return getNodeRootAspect();
    }

    @Override
    public QName getNodeRootAspect() {
        return CircabcModel.ASPECT_IGROOT;
    }

    @Override
    public String getProfilePrefix() {
        return PROFILE_PREFIX;
    }

    @Override
    public void updateProfileMap(final NodeRef nodeRef, final Profile newProfile) {
        final String profileName = newProfile.getProfileName();
        final HashMap<String, Set<String>> servicesPermissions = newProfile.getServicesPermissions();

        super.updateProfileMap(nodeRef, newProfile);

        final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef);

        NodeRef serviceRef;
        ProfileManagerService serviceProfileManager = null;
        for (final ChildAssociationRef serviceAssoc : assocs) {
            serviceRef = serviceAssoc.getChildRef();
            serviceProfileManager =
                    getProfileManagerServiceFactory().getProfileManagerService(serviceRef);
            if (serviceProfileManager != null) {
                // update permissions on ig service top node
                serviceProfileManager.updateProfile(serviceRef, profileName, servicesPermissions, false);
            }
        }
    }

    @Override
    public Profile updateProfile(
            final NodeRef nodeRef,
            final String profileName,
            final Map<String, Set<String>> servicesPermissions,
            final boolean updateProfileMap) {
        final Profile profile =
                super.updateProfile(nodeRef, profileName, servicesPermissions, updateProfileMap);

        profileMapCache.remove(nodeRef.toString());
        resetCache();

        return profile;
    }

    @Override
    public boolean hasParentSubsGroup() {
        return true;
    }

    @Override
    public boolean hasMasterParentGroup() {
        return true;
    }

    @Override
    public boolean hasApplicantFeature() {
        return true;
    }

    public QName getContactParamName() {
        return CircabcModel.PROP_CONTACT_INFORMATION;
    }

    /**
     * We give nodeRef to verify permission with acl
     */
    public void changePassword(
            final NodeRef nodeRef, final String userName, final char[] newPassword) {
        if (logger.isDebugEnabled()) {
            logger.debug("Change password called");
        }
        authenticationService.setAuthentication(userName, newPassword);
    }

    @Override
    public List<Profile> getProfilesRecursivly(final NodeRef nodeRef) {
        final List<Profile> profiles = new ArrayList<>();
        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            profiles.addAll(getProfiles(nodeRef));
        }
        return profiles;
    }
}
