/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.repo.profile.ProfileImpl;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

import javax.faces.context.FacesContext;
import java.util.*;

/**
 * Simple wrapper class to represent a Profile.
 *
 * @author Yanick Pignot
 */
public class AccessProfileWrapper {

    private static final String NAME_REPLACEALL_REGEX = "[^A-Za-z0-9]";

    private final String autority;
    private final String profileGroup;
    private final String name;
    private final String displayDescription;
    private final MLText titles;
    private final MLText descriptions;
    private final Boolean imported;
    private final NodeRef fromIg;
    private String displayTitle;
    private String infPermissionValue;
    private String libPermissionValue;
    private String dirPermissionValue;
    private String newPermissionValue;
    private String evePermissionValue;
    private boolean visible;
    private Boolean exported;
    /*lazy loaded */
    private Boolean deleteAvailable;
    private Boolean specialProfile;
    private String regsitredProfileName;
    private Boolean editActionAvailable;
    private Boolean unexportedActionAvailable;

    AccessProfileWrapper(final Profile profile, final NodeRef fromIg) {
        this.profileGroup = profile.getAlfrescoGroupName();
        this.autority = profile.getPrefixedAlfrescoGroupName();
        this.name = profile.getProfileName();
        this.displayTitle = profile.getProfileDisplayName();
        this.displayDescription = profile.getProfileDescription();
        this.titles = (profile.getTitle() == null) ? new MLText() : profile.getTitle();
        this.descriptions =
                (profile.getDescription() == null) ? new MLText() : profile.getDescription();

        if (titles.size() < 1) {
            // manage backward compatibility
            titles.addValue(Locale.ENGLISH, this.name);
        }

        final Map<String, Set<String>> permissions = profile.getServicesPermissions();

        final Set<String> infServicePermissions = permissions
                .get(CircabcServices.INFORMATION.toString());
        final Set<String> libServicePermissions = permissions.get(CircabcServices.LIBRARY.toString());
        final Set<String> dirServicePermissions = permissions.get(CircabcServices.DIRECTORY.toString());
        final Set<String> newServicePermissions = permissions.get(CircabcServices.NEWSGROUP.toString());
        final Set<String> eveServicePermissions = permissions.get(CircabcServices.EVENT.toString());
        final Set<String> visibilityPermissions = profile
                .getServicePermissions(CircabcServices.VISIBILITY.toString());

        this.infPermissionValue = safePermvalue(infServicePermissions);
        this.libPermissionValue = safePermvalue(libServicePermissions);
        this.dirPermissionValue = safePermvalue(dirServicePermissions);
        this.newPermissionValue = safePermvalue(newServicePermissions);
        this.evePermissionValue = safePermvalue(eveServicePermissions);
        this.visible = VisibilityPermissions.VISIBILITY.toString()
                .equals(safePermvalue(visibilityPermissions));

        this.unexportedActionAvailable = false;
        this.editActionAvailable = true;
        this.deleteAvailable = false;

        this.exported = profile.isExported();
        this.imported = profile.isImported();

        this.fromIg = profile.isImported() ? profile.getImportedNodeRef() : fromIg;
    }

    public AccessProfileWrapper(final NodeRef fromIg) {
        this.profileGroup = null;
        this.autority = null;
        this.name = null;
        this.displayTitle = null;
        this.displayDescription = null;
        this.titles = new MLText();
        this.descriptions = new MLText();

        infPermissionValue = InformationPermissions.INFNOACCESS.toString();
        libPermissionValue = LibraryPermissions.LIBNOACCESS.toString();
        dirPermissionValue = DirectoryPermissions.DIRNOACCESS.toString();
        newPermissionValue = NewsGroupPermissions.NWSNOACCESS.toString();
        evePermissionValue = EventPermissions.EVENOACCESS.toString();
        visible = true;

        this.exported = false;
        this.imported = false;

        this.editActionAvailable = true;
        this.unexportedActionAvailable = false;
        this.deleteAvailable = false;

        this.fromIg = fromIg;
    }


    public AccessProfileWrapper(
            eu.cec.digit.circabc.repo.app.model.ProfileWithUsersCount profile,
            NodeRef fromIg) {
        this.profileGroup = profile.getAlfrescoGroup();
        if (this.profileGroup.equals(CircabcConstant.GUEST_AUTHORITY)) {
            this.autority = this.profileGroup;
        } else if (this.profileGroup.equals(CircabcConstant.REGISTERED_AUTHORITY)) {
            this.autority = PermissionService.ALL_AUTHORITIES;
        } else {
            this.autority = PermissionService.GROUP_PREFIX + this.profileGroup;
        }

        this.name = profile.getName();
        this.displayTitle = profile.getTitle();
        this.displayDescription = profile.getTitle();
        this.titles = (profile.getTitle() == null) ? new MLText() : new MLText(profile.getTitle());
        this.descriptions =
                (profile.getTitle() == null) ? new MLText() : new MLText(profile.getTitle());

        if (titles.size() < 1) {
            // manage backward compatibility
            titles.addValue(Locale.ENGLISH, this.name);
        }

        if (profile.isImported()) {
            final int lastIndexOf = this.name.lastIndexOf('_');
            if (lastIndexOf > -1) {
                this.displayTitle = this.name.substring(0, lastIndexOf - 1) + ':' + profile.getTitle();
            } else {
                this.displayTitle = this.name;
            }
        }

        this.infPermissionValue = profile.getInformationPermission();
        this.libPermissionValue = profile.getLibraryPermission();
        this.dirPermissionValue = profile.getDirectoryPermission();
        this.newPermissionValue = profile.getNewsgroupPermission();
        this.evePermissionValue = profile.getEventPermission();
        this.visible = profile.isVisible();

        this.unexportedActionAvailable = false;
        this.editActionAvailable = true;
        if ((profile.getAlfrescoGroup().equals(CircabcConstant.GUEST_AUTHORITY)) || (profile
                .getAlfrescoGroup().equals(CircabcConstant.REGISTERED_AUTHORITY))) {
            this.deleteAvailable = false;
        } else if (profile.getNumberOfUsers() == 0) {
            this.deleteAvailable = true;
        } else if (profile.isImported()) {
            this.deleteAvailable = true;
        } else {
            this.deleteAvailable = false;
        }

        this.exported = profile.isExported();
        this.imported = profile.isImported();

        this.fromIg = profile.isImported() ? new NodeRef(profile.getIgFromNodeRef()) : fromIg;
    }

    private String safePermvalue(final Set<String> servicePermissions) {
        if (servicePermissions == null || servicePermissions.size() < 1) {
            return "";
        } else {
            return servicePermissions.iterator().next();
        }
    }

    /**
     * @return the autority
     */
    public final String getAutority() {
        return autority;
    }

    /**
     * @return the descriptions
     */
    public final MLText getDescriptions() {
        return descriptions;
    }

    /**
     * @return the displayDescription
     */
    public final String getDisplayDescription() {
        return displayDescription;
    }

    /**
     * @return the displayTitle
     */
    public final String getDisplayTitle() {
        return displayTitle;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the newPermissions
     */
    public final String getNewPermission() {
        return PermissionUtils.getPermissionLabel(newPermissionValue);
    }

    /**
     * @param newPermission the newPermission to set
     */
    public final void setNewPermission(String newPermission) {
        this.newPermissionValue = newPermission;
    }

    /**
     * @return the dirPermissions
     */
    public final String getDirPermission() {
        return PermissionUtils.getPermissionLabel(dirPermissionValue);
    }

    /**
     * @param dirPermission the dirPermission to set
     */
    public final void setDirPermission(String dirPermission) {
        this.dirPermissionValue = dirPermission;
    }

    /**
     * @return the evePermissions
     */
    public final String getEvePermission() {
        return PermissionUtils.getPermissionLabel(evePermissionValue);
    }

    /**
     * @param evePermission the evePermission to set
     */
    public final void setEvePermission(String evePermission) {
        this.evePermissionValue = evePermission;
    }

    /**
     * @return the infPermissions
     */
    public final String getInfPermission() {
        return PermissionUtils.getPermissionLabel(infPermissionValue);
    }

    /**
     * @param infPermission the infPermission to set
     */
    public final void setInfPermission(String infPermission) {
        this.infPermissionValue = infPermission;
    }

    /**
     * @return the libPermissions
     */
    public final String getLibPermission() {
        return PermissionUtils.getPermissionLabel(libPermissionValue);
    }

    /**
     * @param libPermission the libPermission to set
     */
    public final void setLibPermission(String libPermission) {
        this.libPermissionValue = libPermission;
    }

    /**
     * @return the newPermissions
     */
    public final String getNewPermissionTooltip() {
        return PermissionUtils.getPermissionTooltip(newPermissionValue);
    }

    /**
     * @return the dirPermissions
     */
    public final String getDirPermissionTooltip() {
        return PermissionUtils.getPermissionTooltip(dirPermissionValue);
    }

    /**
     * @return the evePermissions
     */
    public final String getEvePermissionTooltip() {
        return PermissionUtils.getPermissionTooltip(evePermissionValue);
    }

    /**
     * @return the infPermissions
     */
    public final String getInfPermissionTooltip() {
        return PermissionUtils.getPermissionTooltip(infPermissionValue);
    }

    /**
     * @return the libPermissions
     */
    public final String getLibPermissionTooltip() {
        return PermissionUtils.getPermissionTooltip(libPermissionValue);
    }

    /**
     * @return the titles
     */
    public final MLText getTitles() {
        return titles;
    }

    /**
     * @return the exported
     */
    public final boolean isExported() {
        return exported;
    }

    /**
     * @return the imported
     */
    public final boolean isImported() {
        return imported;
    }

    /**
     * @return the fromIg
     */
    public final NodeRef getFromIg() {
        return fromIg;
    }

    public boolean isExportedActionAvailable() {
        if (isImported() || isSpecialProfile()) {
            return Boolean.FALSE;
        } else {
            return !isExported();
        }
    }

    public boolean isUnexportedActionAvailable() {
        return unexportedActionAvailable;
    }

    public void setUnexportedActionAvailable(Boolean value) {
        unexportedActionAvailable = value;
    }

    public boolean isDeleteActionAvailable() {
        return deleteAvailable;
    }

    public void setDeleteActionAvailable(boolean value) {
        deleteAvailable = value;
    }

    public boolean isEditActionAvailable() {
        return editActionAvailable;
    }

    public void setEditActionAvailable(boolean value) {
        editActionAvailable = value;
    }

    /**
     * @return
     */
    public boolean isSpecialProfile() {
        if (specialProfile == null) {
            specialProfile = CircabcConstant.GUEST_AUTHORITY.equals(getName())
                    || getRegistredProfileName().equals(getName());
        }
        return specialProfile;
    }

    private String getRegistredProfileName() {
        if (regsitredProfileName == null) {
            final CircabcRootProfileManagerService profService =
                    Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                            .getCircabcRootProfileManagerService();
            regsitredProfileName = profService.getAllCircaUsersGroupName();
        }
        return regsitredProfileName;
    }

    /**
     * @return the exported
     */
    public final Boolean getExported() {
        return exported;
    }

    /**
     * @param exported the exported to set
     */
    public final void setExported(Boolean exported) {
        this.exported = exported;
    }

    /**
     * Add locale and value to descriptions
     */
    public final void withDescription(final Locale locale, final String value) {
        this.descriptions.addValue(locale, value);
    }

    /**
     * add locale , value to titles
     */
    public final void withTitle(final Locale locale, final String value) {
        this.titles.addValue(locale, value);
    }

    public final Profile toProfile() {
        final Profile prof = new ProfileImpl();

        genererateName(prof);
        generatePermissions(prof);

        prof.setTitles(this.titles);
        prof.setDescriptions(this.descriptions);
        prof.setExported(this.exported);
        prof.setImported(this.imported);
        prof.setImportedNodeRef(this.imported ? fromIg : null);

        prof.setAlfrescoGroupName(this.profileGroup);
        prof.setPrefixedAlfrescoGroupName(this.autority);

        return prof;
    }

    /**
     * @param prof
     */
    private void generatePermissions(final Profile prof) {
        final Map<String, Set<String>> permissions = new HashMap<>(5);

        permissions
                .put(CircabcServices.INFORMATION.toString(), Collections.singleton(infPermissionValue));
        permissions.put(CircabcServices.LIBRARY.toString(), Collections.singleton(libPermissionValue));
        permissions
                .put(CircabcServices.DIRECTORY.toString(), Collections.singleton(dirPermissionValue));
        permissions
                .put(CircabcServices.NEWSGROUP.toString(), Collections.singleton(newPermissionValue));
        permissions.put(CircabcServices.EVENT.toString(), Collections.singleton(evePermissionValue));

        if (visible) {
            permissions.put(CircabcServices.VISIBILITY.toString(),
                    Collections.singleton(VisibilityPermissions.VISIBILITY.toString()));
        } else {
            permissions.put(CircabcServices.VISIBILITY.toString(),
                    Collections.singleton(VisibilityPermissions.NOVISIBILITY.toString()));
        }

        prof.setServicesPermissions(permissions);
    }

    /**
     * @param prof
     */
    private void genererateName(final Profile prof) {
        if (this.name == null) {
            String candidate;

            if (titles.containsKey(Locale.ENGLISH)) {
                candidate = titles.get(Locale.ENGLISH);
            } else {
                candidate = titles.getDefaultValue();
            }

            String finalCandidate = candidate.replaceAll(NAME_REPLACEALL_REGEX, "");
            if (finalCandidate.equals("")) {
                finalCandidate = "X" + Math.round(Math.random() * 100000);
            }
            prof.setProfileName(finalCandidate);
        } else {
            prof.setProfileName(this.name);
        }
    }

    /**
     * @return the dirPermissionValue
     */
    /*package*/
    final String getDirPermissionValue() {
        return dirPermissionValue;
    }

    /**
     * @return the evePermissionValue
     */
    /*package*/
    final String getEvePermissionValue() {
        return evePermissionValue;
    }

    /**
     * @return the infPermissionValue
     */
    /*package*/
    final String getInfPermissionValue() {
        return infPermissionValue;
    }

    /**
     * @return the libPermissionValue
     */
    /*package*/
    final String getLibPermissionValue() {
        return libPermissionValue;
    }

    /**
     * @return the newPermissionValue
     */
    /*package*/
    final String getNewPermissionValue() {
        return newPermissionValue;
    }

    /**
     * @return the visible
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }
}
