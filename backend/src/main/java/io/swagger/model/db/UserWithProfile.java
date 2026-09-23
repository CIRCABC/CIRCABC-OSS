package io.swagger.model.db;

import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;

/**
 * Data model that extends {@link User} with the profile and per-service
 * permission information a user holds within an Interest Group (IG).
 *
 * <p>In addition to the base user identity, an instance carries the profile the
 * user is assigned to (identifier, name, title and node references) and the
 * effective permission level for each CIRCABC service: Library, Newsgroup,
 * Information, Directory and Events. It also tracks profile lifecycle flags
 * (exported, imported, visible).</p>
 *
 * <p>This class is typically populated from the database when resolving the
 * combination of a user and the profile that grants them access to an IG.</p>
 */
public class UserWithProfile extends User {

  /** Database identifier of the profile assigned to the user. */
  private long profileId;

  /** Name of the underlying Alfresco authority (group) backing the profile. */
  private String alfrescoGroup;

  /** Technical name of the profile. */
  private String profileName;

  /** Human-readable display title of the profile. */
  private String profileTitle;

  /** Directory (members) service permission level granted by the profile. */
  private String directoryPermission;

  /** Information service permission level granted by the profile. */
  private String informationPermission;

  /** Library service permission level granted by the profile. */
  private String libraryPermission;

  /** Newsgroup service permission level granted by the profile. */
  private String newsgroupPermission;

  /** Events service permission level granted by the profile. */
  private String eventPermission;

  /** Whether the profile is flagged as exported. */
  private boolean isExported;

  /** Whether the profile was imported (affects title formatting). */
  private boolean isImported;

  /** Whether the profile is visible to users. */
  private boolean isVisible;

  /** Alfresco node reference of the profile. */
  private String profileNodeRef;

  /** Node reference of the Interest Group the profile originates from. */
  private String profileIgFromNodeRef;

  /**
   * {@inheritDoc}
   *
   * <p>The hash code combines the base {@link User} hash code with all
   * profile-related fields.</p>
   *
   * @return the hash code for this user-with-profile
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
      prime * result + ((alfrescoGroup == null) ? 0 : alfrescoGroup.hashCode());
    result =
      prime * result +
      ((directoryPermission == null) ? 0 : directoryPermission.hashCode());
    result =
      prime * result +
      ((eventPermission == null) ? 0 : eventPermission.hashCode());
    result =
      prime * result +
      ((informationPermission == null) ? 0 : informationPermission.hashCode());
    result = prime * result + (isExported ? 1231 : 1237);
    result = prime * result + (isImported ? 1231 : 1237);
    result = prime * result + (isVisible ? 1231 : 1237);
    result =
      prime * result +
      ((libraryPermission == null) ? 0 : libraryPermission.hashCode());
    result =
      prime * result +
      ((newsgroupPermission == null) ? 0 : newsgroupPermission.hashCode());
    result = prime * result + (int) (profileId ^ (profileId >>> 32));
    result =
      prime * result +
      ((profileIgFromNodeRef == null) ? 0 : profileIgFromNodeRef.hashCode());
    result =
      prime * result + ((profileName == null) ? 0 : profileName.hashCode());
    result =
      prime * result +
      ((profileNodeRef == null) ? 0 : profileNodeRef.hashCode());
    result =
      prime * result + ((profileTitle == null) ? 0 : profileTitle.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Two instances are equal when the base {@link User} state is equal and
   * all profile-related fields match.</p>
   *
   * @param obj the object to compare against
   * @return {@code true} if the given object represents the same user and
   *         profile, {@code false} otherwise
   */
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

  /**
   * @return the database identifier of the assigned profile
   */
  public long getProfileId() {
    return profileId;
  }

  /**
   * @param profileId the database identifier of the assigned profile
   */
  public void setProfileId(long profileId) {
    this.profileId = profileId;
  }

  /**
   * @return the name of the Alfresco authority (group) backing the profile
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * @param alfrescoGroup the name of the Alfresco authority (group) backing the
   *                      profile
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * @return the technical name of the profile
   */
  public String getProfileName() {
    return profileName;
  }

  /**
   * @param profileName the technical name of the profile
   */
  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  /**
   * Returns the display title of the profile.
   *
   * <p>For imported profiles the last {@code "_"} separator is normalised back
   * to a {@code ":"} before the title is returned.</p>
   *
   * @return the profile display title
   */
  public String getProfileTitle() {
    if (this.isImported) {
      return Profile.replaceLast(profileTitle, "_", ":");
    } else {
      return profileTitle;
    }
  }

  /**
   * @param profileTitle the display title of the profile
   */
  public void setProfileTitle(String profileTitle) {
    this.profileTitle = profileTitle;
  }

  /**
   * @return the Directory service permission level
   */
  public String getDirectoryPermission() {
    return directoryPermission;
  }

  /**
   * @param directoryPermission the Directory service permission level
   */
  public void setDirectoryPermission(String directoryPermission) {
    this.directoryPermission = directoryPermission;
  }

  /**
   * @return the Information service permission level
   */
  public String getInformationPermission() {
    return informationPermission;
  }

  /**
   * @param informationPermission the Information service permission level
   */
  public void setInformationPermission(String informationPermission) {
    this.informationPermission = informationPermission;
  }

  /**
   * @return the Library service permission level
   */
  public String getLibraryPermission() {
    return libraryPermission;
  }

  /**
   * @param libraryPermission the Library service permission level
   */
  public void setLibraryPermission(String libraryPermission) {
    this.libraryPermission = libraryPermission;
  }

  /**
   * @return the Newsgroup service permission level
   */
  public String getNewsgroupPermission() {
    return newsgroupPermission;
  }

  /**
   * @param newsgroupPermission the Newsgroup service permission level
   */
  public void setNewsgroupPermission(String newsgroupPermission) {
    this.newsgroupPermission = newsgroupPermission;
  }

  /**
   * @return the Events service permission level
   */
  public String getEventPermission() {
    return eventPermission;
  }

  /**
   * @param eventPermission the Events service permission level
   */
  public void setEventPermission(String eventPermission) {
    this.eventPermission = eventPermission;
  }

  /**
   * @return {@code true} if the profile is flagged as exported
   */
  public boolean isExported() {
    return isExported;
  }

  /**
   * @param isExported whether the profile is flagged as exported
   */
  public void setExported(boolean isExported) {
    this.isExported = isExported;
  }

  /**
   * @return {@code true} if the profile was imported
   */
  public boolean isImported() {
    return isImported;
  }

  /**
   * @param isImported whether the profile was imported
   */
  public void setImported(boolean isImported) {
    this.isImported = isImported;
  }

  /**
   * @return {@code true} if the profile is visible to users
   */
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * @param isVisible whether the profile is visible to users
   */
  public void setVisible(boolean isVisible) {
    this.isVisible = isVisible;
  }

  /**
   * @return the Alfresco node reference of the profile
   */
  public String getProfileNodeRef() {
    return profileNodeRef;
  }

  /**
   * @param profileNodeRef the Alfresco node reference of the profile
   */
  public void setProfileNodeRef(String profileNodeRef) {
    this.profileNodeRef = profileNodeRef;
  }

  /**
   * @return the node reference of the Interest Group the profile originates
   *         from
   */
  public String getProfileIgFromNodeRef() {
    return profileIgFromNodeRef;
  }

  /**
   * @param profileIgFromNodeRef the node reference of the Interest Group the
   *                             profile originates from
   */
  public void setProfileIgFromNodeRef(String profileIgFromNodeRef) {
    this.profileIgFromNodeRef = profileIgFromNodeRef;
  }

  /**
   * Determines whether the profile grants administrator rights across every
   * service.
   *
   * <p>The user is considered an administrator only when the profile holds the
   * admin permission level simultaneously for the Library, Newsgroup,
   * Information, Directory and Events services.</p>
   *
   * @return {@code true} if the profile is an administrator for all services,
   *         {@code false} otherwise
   */
  public boolean isAdmin() {
    boolean isLibAdmin = this.libraryPermission.equals(
      LibraryPermissions.LIBADMIN.toString()
    );
    boolean isNewsAdmin = this.newsgroupPermission.equals(
      NewsGroupPermissions.NWSADMIN.toString()
    );
    boolean isInfAdmin = this.informationPermission.equals(
      InformationPermissions.INFADMIN.toString()
    );
    boolean isDirAdmin = this.directoryPermission.equals(
      DirectoryPermissions.DIRADMIN.toString()
    );
    boolean isEventAdmin = this.eventPermission.equals(
      EventPermissions.EVEADMIN.toString()
    );
    return (
      isLibAdmin && isNewsAdmin && isInfAdmin && isDirAdmin && isEventAdmin
    );
  }
}
