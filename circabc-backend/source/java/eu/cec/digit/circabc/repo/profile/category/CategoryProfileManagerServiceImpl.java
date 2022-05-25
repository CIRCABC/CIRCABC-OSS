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
package eu.cec.digit.circabc.repo.profile.category;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.PermissionModel;
import eu.cec.digit.circabc.repo.profile.ProfileManagerServiceImpl;
import eu.cec.digit.circabc.service.profile.*;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class CategoryProfileManagerServiceImpl extends ProfileManagerServiceImpl
        implements CategoryProfileManagerService {

    private static final String PROFILE_PREFIX = "circaCategory";
    private static final Set<String> services = new HashSet<>();
    /**
     * A logger for the class
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(CategoryProfileManagerServiceImpl.class);

    public CategoryProfileManagerServiceImpl() {
        services.add(CircabcServices.CATEGORY.toString());
    }

    /**
     * @return the services
     */
    public final Set<String> getServices() {
        return services;
    }

    public List<QName> getQNameServiceRoles() {
        final List<QName> list = new ArrayList<>();
        // list.add(CircaCategoryAspect.CIRCABC_PERMISSION);
        list.add(PermissionModel.CATEGORY_PERMISSION);
        // list.add(CircaCategoryAspect.LIBRARY_PERMISSION);
        // list.add(CircaCategoryAspect.DIRECTORY_PERMISSION);

        return list;
    }

    public Map<QName, String> getServiceRolesEnum() {
        final Map<QName, String> map = new HashMap<>();
        // map.put(CircaCategoryAspect.CIRCABC_PERMISSION, CircaServices.CIRCABC.toString());
        map.put(PermissionModel.CATEGORY_PERMISSION, CircabcServices.CATEGORY.toString());
        // map.put(CircaCategoryAspect.LIBRARY_PERMISSION, CircaServices.LIBRARY.toString());
        // map.put(CircaCategoryAspect.DIRECTORY_PERMISSION, CircaServices.DIRECTORY.toString());
        return map;
    }

    @Override
    public QName getNodeAspect() {
        return getNodeRootAspect();
    }

    @Override
    public QName getNodeRootAspect() {
        return CircabcModel.ASPECT_CATEGORY;
    }

    public String getProfilePrefix() {
        return PROFILE_PREFIX;
    }

    public boolean hasParentSubsGroup() {
        return true;
    }

    public boolean hasMasterParentGroup() {
        return true;
    }

    public boolean hasApplicantFeature() {
        return false;
    }

    @Override
    public Profile updateProfile(
            final NodeRef nodeRef,
            final String profileName,
            final Map<String, Set<String>> servicesPermissions,
            final boolean updateProfileMap) {
        Profile profile =
                super.updateProfile(nodeRef, profileName, servicesPermissions, updateProfileMap);

        resetCache();

        return profile;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.profile.ProfileManagerServiceImpl#addApplicantPerson(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void addApplicantPerson(
            final NodeRef nodeRef,
            final String userID,
            final String message,
            final String firstName,
            final String lastName)
            throws ProfileException {
        throw new IllegalStateException(
                "This method is not yet implemented for the aspect " + getNodeAspect());
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.profile.ProfileManagerServiceImpl#removeApplicantPerson(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void removeApplicantPerson(final NodeRef nodeRef, final String userID)
            throws ProfileException {
        throw new IllegalStateException(
                "This method is not yet implemented for the aspect " + getNodeAspect());
    }

    public List<Profile> getProfilesRecursivly(final NodeRef nodeRef) {
        final List<Profile> profiles = new ArrayList<>();
        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            NodeRef igNodeRef;
            final IGRootProfileManagerService iGRootProfileManagerService =
                    getProfileManagerServiceFactory().getIGRootProfileManagerService();
            for (final ChildAssociationRef childs : nodeService.getChildAssocs(nodeRef)) {
                igNodeRef = childs.getChildRef();
                if (nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
                    profiles.addAll(iGRootProfileManagerService.getProfilesRecursivly(igNodeRef));
                }
            }
            profiles.addAll(getProfiles(nodeRef));
        }
        return profiles;
    }
}
