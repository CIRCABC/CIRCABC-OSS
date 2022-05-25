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

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.permissions.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProfileImpl implements Profile, Serializable {

    private static final Log logger = LogFactory.getLog(ProfileImpl.class);
    /**
     *
     */
    private static final long serialVersionUID = 8073588990006108569L;
    private String profileName;
    private ProfilePermissions profilePermissions;
    private String alfrescoGroupName;
    private String prefixedAlfrescoGroupName;
    private boolean isImported = false;
    private boolean isExported = false;
    private MLText descriptions;
    private MLText titles;
    private NodeRef importedNodeRef;
    private String importedNodeName = null;
    private NodeRef nodeRef;

    public ProfileImpl(
            final NodeRef nodeRef,
            final String profileName,
            final String authorityGroupName,
            final String parentGroup,
            final MLText titles,
            final MLText descriptions,
            final ServiceRegistry serviceRegistry,
            final CircabcRootProfileManagerService circabcRootProfileManagerService) {
        // serviceRegistry.getAuthorityService();
        final QName authorithyService =
                QName.createQName(NamespaceService.ALFRESCO_URI, "authorityService");
        final AuthorityService authorityService =
                (AuthorityService) serviceRegistry.getService(authorithyService);

        this.profileName = profileName;
        this.titles = titles;
        this.descriptions = descriptions;
        profilePermissions = new ProfilePermissions();
        if (CircabcConstant.GUEST_AUTHORITY.equals(profileName)) {
            alfrescoGroupName = profileName;
            prefixedAlfrescoGroupName = profileName;
        } else {
            if (circabcRootProfileManagerService.getAllCircaUsersGroupName().equals(profileName)) {
                alfrescoGroupName = profileName;
                prefixedAlfrescoGroupName = authorityService.getName(AuthorityType.GROUP, profileName);
            } else {
                if (authorityGroupName == null) {
                    alfrescoGroupName = createProfileGroup(profileName);
                    prefixedAlfrescoGroupName =
                            authorityService.getName(AuthorityType.GROUP, alfrescoGroupName);
                } else {
                    prefixedAlfrescoGroupName = authorityGroupName;
                }
            }
        }

        if (!CircabcConstant.GUEST_AUTHORITY.equals(this.getAlfrescoGroupName())
                && !circabcRootProfileManagerService
                .getAllCircaUsersGroupName()
                .equals(this.getAlfrescoGroupName())) {

            final String prefixedParentGroup = authorityService.getName(AuthorityType.GROUP, parentGroup);

            //			authorityService.getAuthorityDisplayName(parentGroup);

            if (authorityService.authorityExists(prefixedParentGroup)) {
                //				@SuppressWarnings("unused")
                //				final String newGroup = authorityService.createAuthority(AuthorityType.GROUP,
                // prefixedParentGroup, this
                //								.getAlfrescoGroupName(), null);
                // Migration 3.1 -> 3.4.6 - 09/12/2011
                // createAuthority() method changed for version 3.4

                // avoid creating null empty groups
                if (this.getAlfrescoGroupName() != null) {
                    final String newGroup =
                            authorityService.createAuthority(
                                    AuthorityType.GROUP,
                                    this.getAlfrescoGroupName(),
                                    this.getAlfrescoGroupName(),
                                    authorityService.getDefaultZones());

                    // TODO change this test when alfresco has defined a better return value for
                    // createAuthority()
                    // Here, this method return the name that you provide and you don't know if the authority
                    // is really created
                    if (newGroup != null) {
                        authorityService.addAuthority(prefixedParentGroup, newGroup);
                    }
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Parent Authority doesn't exist:" + prefixedParentGroup);
                }
                throw new CircabcRuntimeException("Parent Authority doesn't exist:" + prefixedParentGroup);
            }
        }
    }

    public ProfileImpl() {
        profilePermissions = new ProfilePermissions();
    }

    @Override
    public String toString() {
        return "ProfileImpl [profileName="
                + profileName
                + ", profilePermissions="
                + profilePermissions
                + ", alfrescoGroupName="
                + alfrescoGroupName
                + ", prefixedAlfrescoGroupName="
                + prefixedAlfrescoGroupName
                + ", isImported="
                + isImported
                + ", isExported="
                + isExported
                + ", descriptions="
                + descriptions
                + ", titles="
                + titles
                + ", importedNodeRef="
                + importedNodeRef
                + ", importedNodeName="
                + importedNodeName
                + ", nodeRef="
                + nodeRef
                + "]";
    }

    public void clearNodeServicesPermissions(
            final NodeRef nodeRef, final Set<String> services, final ServiceRegistry serviceRegistry) {
        for (final String serviceName : services) {
            profilePermissions.clearNodePermissions(
                    nodeRef, prefixedAlfrescoGroupName, serviceName, serviceRegistry);
        }
    }

    public void setNodeServicesPermissions(
            final NodeRef nodeRef,
            final Map<String, Set<String>> servicesPermissions,
            final ServiceRegistry serviceRegistry) {
        profilePermissions.setNodePermissions(
                nodeRef, prefixedAlfrescoGroupName, servicesPermissions, serviceRegistry);
    }

    public HashMap<String, Set<String>> getServicesPermissions() {
        final HashMap<String, ServicePermissions> map = profilePermissions.getServicePermissions();
        final HashMap<String, Set<String>> servicesPermissions = new HashMap<>();
        for (final Map.Entry<String, ServicePermissions> entry : map.entrySet()) {
            servicesPermissions.put(entry.getKey(), entry.getValue().getPermissions());
        }
        return servicesPermissions;
    }

    public void setServicesPermissions(final Map<String, Set<String>> servicesPermissions) {
        profilePermissions.setServicePermissions(servicesPermissions);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.profile.Profile#getProfileName()
     */
    public final String getProfileName() {
        return profileName;
    }

    /**
     * @param profileName the profileName to set
     */
    public final void setProfileName(final String profileName) {
        this.profileName = profileName;
    }

    public final String getAlfrescoGroupName() {
        return alfrescoGroupName;
    }

    /**
     * @param alfrescoGroupName the alfrescoGroupName to set
     */
    public final void setAlfrescoGroupName(final String alfrescoGroupName) {
        this.alfrescoGroupName = alfrescoGroupName;
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.profile.Profile#getAlfrescoGroupName()
     */
    public final String getPrefixedAlfrescoGroupName() {
        return prefixedAlfrescoGroupName;
    }

    /**
     * @param prefixedAlfrescoGroupName the prefixedAlfrescoGroupName to set
     */
    public final void setPrefixedAlfrescoGroupName(final String prefixedAlfrescoGroupName) {
        this.prefixedAlfrescoGroupName = prefixedAlfrescoGroupName;
    }

    private String createProfileGroup(final String profileName) {
        // create an associated group name based on the profile concateneted
        // with a uniqueID
        // for every profile, a unique user group is created
        final String profileGroupName = createUniqueProfileGroupName(profileName);
        // TODO Create the Group
        return profileGroupName;
    }

    private String createUniqueProfileGroupName(final String profileName) {
        final String uuid = GUID.generate();
        final String uniqueProfileNameLong = profileName + "--" + uuid;
        String uniqueProfileName;

        // Alfresco Category size limitation
        final int maxLength = 1000;
        if (uniqueProfileNameLong.length() > maxLength) {
            uniqueProfileName = uniqueProfileNameLong.substring(0, maxLength - 1);
        } else {
            uniqueProfileName = uniqueProfileNameLong;
        }
        return uniqueProfileName;
    }

    public Set<String> getServicePermissions(final String serviceName) {
        return profilePermissions.getPermissions(serviceName);
    }

    public MLText getDescription() {
        return this.descriptions;
    }

    public void setDescriptions(final MLText descriptions) {
        this.descriptions = descriptions;
    }

    public MLText getTitle() {
        return this.titles;
    }

    public void setTitles(final MLText titles) {
        this.titles = titles;
    }

    public boolean isExported() {
        return this.isExported;
    }

    public final void setExported(final boolean isExported) {
        this.isExported = isExported;
    }

    public boolean isImported() {
        return this.isImported;
    }

    public final void setImported(final boolean isImported) {
        this.isImported = isImported;
    }

    public NodeRef getImportedNodeRef() {
        return this.importedNodeRef;
    }

    public void setImportedNodeRef(final NodeRef importedNodeRef) {
        this.importedNodeRef = importedNodeRef;
    }

    public String getImportedNodeName() {

        return importedNodeName;
    }

    public void setImportedNodeName(final String importedNodeName) {
        this.importedNodeName = importedNodeName;
    }

    public String getProfileDisplayName() {

        final MLText titles = this.getTitle();
        final String profileName;
        if (titles == null || titles.size() < 1) {
            profileName = this.getProfileName();
        } else if (titles.getDefaultValue() != null) {
            profileName = titles.getDefaultValue();
        } else if (titles.get(Locale.ENGLISH) != null) {
            profileName = titles.get(Locale.ENGLISH);
        } else {
            String candidate = this.getProfileName();

            for (final String value : titles.values()) {
                if (value != null && value.length() > 0) {
                    candidate = value;
                    break;
                }
            }

            profileName = candidate;
        }

        if (this.isImported()) {
            return this.getImportedNodeName() + ":" + profileName;
        } else {
            return profileName;
        }
    }

    public String getProfileDescription() {
        if (this.getDescription() == null || this.getDescription().size() < 1) {
            return this.getProfileName();
        } else {
            return this.getDescription().getDefaultValue();
        }
    }

    public boolean isAdmin() {
        boolean result = false;
        boolean isLibAdmin = false;
        boolean isNewsAdmin = false;
        boolean isInfAdmin = false;
        boolean isDirAdmin = false;
        boolean isEventAdmin = false;
        boolean isCategoryAdmin = false;
        boolean isCircabcAdmin = false;

        final HashMap<String, Set<String>> servicesPermissions = getServicesPermissions();
        for (Entry<String, Set<String>> servicePermission : servicesPermissions.entrySet()) {
            String key = servicePermission.getKey();
            Set<String> value = servicePermission.getValue();
            if (key.equalsIgnoreCase(CircabcServices.LIBRARY.toString())) {
                isLibAdmin = value.contains(LibraryPermissions.LIBADMIN.toString());
            }

            if (key.equalsIgnoreCase(CircabcServices.NEWSGROUP.toString())) {
                isNewsAdmin = value.contains(NewsGroupPermissions.NWSADMIN.toString());
            }
            if (key.equalsIgnoreCase(CircabcServices.INFORMATION.toString())) {
                isInfAdmin = value.contains(InformationPermissions.INFADMIN.toString());
            }
            if (key.equalsIgnoreCase(CircabcServices.DIRECTORY.toString())) {
                isDirAdmin = value.contains(DirectoryPermissions.DIRADMIN.toString());
            }
            if (key.equalsIgnoreCase(CircabcServices.EVENT.toString())) {
                isEventAdmin = value.contains(EventPermissions.EVEADMIN.toString());
            }
            if (key.equalsIgnoreCase(CircabcServices.CATEGORY.toString())) {
                isCategoryAdmin = value.contains(CategoryPermissions.CIRCACATEGORYADMIN.toString());
            }
            if (key.equalsIgnoreCase(CircabcServices.CIRCABC.toString())) {
                isCircabcAdmin = value.contains(CircabcRootPermissions.CIRCABCACCESS.toString());
            }
        }
        result =
                ((isLibAdmin && isNewsAdmin && isInfAdmin && isDirAdmin && isEventAdmin)
                        || (isCategoryAdmin)
                        || (isCircabcAdmin));

        return result;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }
}
