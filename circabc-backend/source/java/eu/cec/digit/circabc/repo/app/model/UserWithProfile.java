package eu.cec.digit.circabc.repo.app.model;

import eu.cec.digit.circabc.service.profile.permissions.*;

public class UserWithProfile extends User {

    private long profileId;
    private String alfrescoGroup;
    private String profileName;
    private String profileTitle;
    private String directoryPermission;
    private String informationPermission;
    private String libraryPermission;
    private String newsgroupPermission;
    private String eventPermission;
    private boolean isExported;
    private boolean isImported;
    private boolean isVisible;
    private String profileNodeRef;
    private String profileIgFromNodeRef;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((alfrescoGroup == null) ? 0 : alfrescoGroup.hashCode());
        result = prime * result + ((directoryPermission == null) ? 0 : directoryPermission.hashCode());
        result = prime * result + ((eventPermission == null) ? 0 : eventPermission.hashCode());
        result =
                prime * result + ((informationPermission == null) ? 0 : informationPermission.hashCode());
        result = prime * result + (isExported ? 1231 : 1237);
        result = prime * result + (isImported ? 1231 : 1237);
        result = prime * result + (isVisible ? 1231 : 1237);
        result = prime * result + ((libraryPermission == null) ? 0 : libraryPermission.hashCode());
        result = prime * result + ((newsgroupPermission == null) ? 0 : newsgroupPermission.hashCode());
        result = prime * result + (int) (profileId ^ (profileId >>> 32));
        result =
                prime * result + ((profileIgFromNodeRef == null) ? 0 : profileIgFromNodeRef.hashCode());
        result = prime * result + ((profileName == null) ? 0 : profileName.hashCode());
        result = prime * result + ((profileNodeRef == null) ? 0 : profileNodeRef.hashCode());
        result = prime * result + ((profileTitle == null) ? 0 : profileTitle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserWithProfile other = (UserWithProfile) obj;
        if (alfrescoGroup == null) {
            if (other.alfrescoGroup != null) {
                return false;
            }
        } else if (!alfrescoGroup.equals(other.alfrescoGroup)) {
            return false;
        }
        if (directoryPermission == null) {
            if (other.directoryPermission != null) {
                return false;
            }
        } else if (!directoryPermission.equals(other.directoryPermission)) {
            return false;
        }
        if (eventPermission == null) {
            if (other.eventPermission != null) {
                return false;
            }
        } else if (!eventPermission.equals(other.eventPermission)) {
            return false;
        }
        if (informationPermission == null) {
            if (other.informationPermission != null) {
                return false;
            }
        } else if (!informationPermission.equals(other.informationPermission)) {
            return false;
        }
        if (isExported != other.isExported) {
            return false;
        }
        if (isImported != other.isImported) {
            return false;
        }
        if (isVisible != other.isVisible) {
            return false;
        }
        if (libraryPermission == null) {
            if (other.libraryPermission != null) {
                return false;
            }
        } else if (!libraryPermission.equals(other.libraryPermission)) {
            return false;
        }
        if (newsgroupPermission == null) {
            if (other.newsgroupPermission != null) {
                return false;
            }
        } else if (!newsgroupPermission.equals(other.newsgroupPermission)) {
            return false;
        }
        if (profileId != other.profileId) {
            return false;
        }
        if (profileIgFromNodeRef == null) {
            if (other.profileIgFromNodeRef != null) {
                return false;
            }
        } else if (!profileIgFromNodeRef.equals(other.profileIgFromNodeRef)) {
            return false;
        }
        if (profileName == null) {
            if (other.profileName != null) {
                return false;
            }
        } else if (!profileName.equals(other.profileName)) {
            return false;
        }
        if (profileNodeRef == null) {
            if (other.profileNodeRef != null) {
                return false;
            }
        } else if (!profileNodeRef.equals(other.profileNodeRef)) {
            return false;
        }
        if (profileTitle == null) {
            if (other.profileTitle != null) {
                return false;
            }
        } else if (!profileTitle.equals(other.profileTitle)) {
            return false;
        }
        return true;
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public String getAlfrescoGroup() {
        return alfrescoGroup;
    }

    public void setAlfrescoGroup(String alfrescoGroup) {
        this.alfrescoGroup = alfrescoGroup;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileTitle() {
        if (this.isImported) {
            return Profile.replaceLast(profileTitle, "_", ":");
        } else {
            return profileTitle;
        }
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getDirectoryPermission() {
        return directoryPermission;
    }

    public void setDirectoryPermission(String directoryPermission) {
        this.directoryPermission = directoryPermission;
    }

    public String getInformationPermission() {
        return informationPermission;
    }

    public void setInformationPermission(String informationPermission) {
        this.informationPermission = informationPermission;
    }

    public String getLibraryPermission() {
        return libraryPermission;
    }

    public void setLibraryPermission(String libraryPermission) {
        this.libraryPermission = libraryPermission;
    }

    public String getNewsgroupPermission() {
        return newsgroupPermission;
    }

    public void setNewsgroupPermission(String newsgroupPermission) {
        this.newsgroupPermission = newsgroupPermission;
    }

    public String getEventPermission() {
        return eventPermission;
    }

    public void setEventPermission(String eventPermission) {
        this.eventPermission = eventPermission;
    }

    public boolean isExported() {
        return isExported;
    }

    public void setExported(boolean isExported) {
        this.isExported = isExported;
    }

    public boolean isImported() {
        return isImported;
    }

    public void setImported(boolean isImported) {
        this.isImported = isImported;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getProfileNodeRef() {
        return profileNodeRef;
    }

    public void setProfileNodeRef(String profileNodeRef) {
        this.profileNodeRef = profileNodeRef;
    }

    public String getProfileIgFromNodeRef() {
        return profileIgFromNodeRef;
    }

    public void setProfileIgFromNodeRef(String profileIgFromNodeRef) {
        this.profileIgFromNodeRef = profileIgFromNodeRef;
    }

    public boolean isAdmin() {
        boolean isLibAdmin = this.libraryPermission.equals(LibraryPermissions.LIBADMIN.toString());
        boolean isNewsAdmin = this.newsgroupPermission.equals(NewsGroupPermissions.NWSADMIN.toString());
        boolean isInfAdmin =
                this.informationPermission.equals(InformationPermissions.INFADMIN.toString());
        boolean isDirAdmin = this.directoryPermission.equals(DirectoryPermissions.DIRADMIN.toString());
        boolean isEventAdmin = this.eventPermission.equals(EventPermissions.EVEADMIN.toString());
        boolean result = ((isLibAdmin && isNewsAdmin && isInfAdmin && isDirAdmin && isEventAdmin));
        return result;
    }
}
