package io.swagger.model.db;

/**
 * Database-level data model representing a CIRCABC user.
 *
 * <p>This is a plain data-transfer object (DTO) that mirrors a user record as
 * persisted in the database. It carries the user's identity, profile details
 * (name and email), the associated Alfresco {@code nodeRef}, locale preference,
 * EU Login (ECAS) credentials and notification/visibility preferences. It holds
 * no business logic beyond standard accessors and value-based
 * {@link #equals(Object)}/{@link #hashCode()} implementations.
 */
public class User {

  /** Primary key / unique database identifier of the user. */
  long id;

  /** Login user name uniquely identifying the user within CIRCABC. */
  private String userName;

  /** User's given (first) name. */
  private String firstName;

  /** User's family (last) name. */
  private String lastName;

  /** User's email address. */
  private String email;

  /** Reference to the corresponding Alfresco node ({@code nodeRef}). */
  private String nodeRef;

  /** Identifier of the user's preferred locale. */
  private Long localeID;

  /** EU Login (ECAS) user name associated with this user. */
  private String ecasUserName;

  /** Domain the user belongs to. */
  private String domain;

  /** Whether the user's profile is visible to others. */
  private Boolean visibility;

  /** Whether the user has opted in to global (platform-wide) notifications. */
  private Boolean globalNotification;

  /** Creates an empty {@code User} instance. */
  public User() {
    super();
  }

  /**
   * Creates a fully populated {@code User} instance.
   *
   * @param id the unique database identifier
   * @param userName the login user name
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param email the user's email address
   * @param nodeRef the associated Alfresco node reference
   * @param localeID the identifier of the user's preferred locale
   * @param ecasUserName the EU Login (ECAS) user name
   * @param domain the domain the user belongs to
   * @param visibility whether the user's profile is visible
   * @param globalNotification whether global notifications are enabled
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public User(
    long id,
    String userName,
    String firstName,
    String lastName,
    String email,
    String nodeRef,
    Long localeID,
    String ecasUserName,
    String domain,
    Boolean visibility,
    Boolean globalNotification
  ) {
    super();
    this.id = id;
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.nodeRef = nodeRef;
    this.localeID = localeID;
    this.ecasUserName = ecasUserName;
    this.domain = domain;
    this.visibility = visibility;
    this.globalNotification = globalNotification;
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)} based on all
   * fields of this user.
   *
   * @return the hash code value for this user
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result =
      prime * result + ((ecasUserName == null) ? 0 : ecasUserName.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result =
      prime * result +
      ((globalNotification == null) ? 0 : globalNotification.hashCode());
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
    result = prime * result + ((localeID == null) ? 0 : localeID.hashCode());
    result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    result =
      prime * result + ((visibility == null) ? 0 : visibility.hashCode());
    return result;
  }

  /**
   * Compares this user with another object for equality. Two users are equal
   * when they are of the same type and all their fields are equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object represents an equal user,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    User other = (User) obj;
    if (domain == null) {
      if (other.domain != null) {
        return false;
      }
    } else if (!domain.equals(other.domain)) {
      return false;
    }
    if (ecasUserName == null) {
      if (other.ecasUserName != null) {
        return false;
      }
    } else if (!ecasUserName.equals(other.ecasUserName)) {
      return false;
    }
    if (email == null) {
      if (other.email != null) {
        return false;
      }
    } else if (!email.equals(other.email)) {
      return false;
    }
    if (firstName == null) {
      if (other.firstName != null) {
        return false;
      }
    } else if (!firstName.equals(other.firstName)) {
      return false;
    }
    if (globalNotification == null) {
      if (other.globalNotification != null) {
        return false;
      }
    } else if (!globalNotification.equals(other.globalNotification)) {
      return false;
    }
    if (id != other.id) {
      return false;
    }
    if (lastName == null) {
      if (other.lastName != null) {
        return false;
      }
    } else if (!lastName.equals(other.lastName)) {
      return false;
    }
    if (localeID == null) {
      if (other.localeID != null) {
        return false;
      }
    } else if (!localeID.equals(other.localeID)) {
      return false;
    }
    if (nodeRef == null) {
      if (other.nodeRef != null) {
        return false;
      }
    } else if (!nodeRef.equals(other.nodeRef)) {
      return false;
    }
    if (userName == null) {
      if (other.userName != null) {
        return false;
      }
    } else if (!userName.equals(other.userName)) {
      return false;
    }
    if (visibility == null) {
      if (other.visibility != null) {
        return false;
      }
    } else if (!visibility.equals(other.visibility)) {
      return false;
    }
    return true;
  }

  /**
   * Returns a human-readable representation of this user, including all fields.
   *
   * @return a string representation of this user
   */
  @Override
  public String toString() {
    return (
      "User [id=" +
      id +
      ", userName=" +
      userName +
      ", firstName=" +
      firstName +
      ", lastName=" +
      lastName +
      ", email=" +
      email +
      ", nodeRef=" +
      nodeRef +
      ", localeID=" +
      localeID +
      ", ecasUserName=" +
      ecasUserName +
      ", domain=" +
      domain +
      ", visibility=" +
      visibility +
      ", globalNotification=" +
      globalNotification +
      "]"
    );
  }

  /**
   * Returns the EU Login (ECAS) user name.
   *
   * @return the ECAS user name
   */
  public String getEcasUserName() {
    return ecasUserName;
  }

  /**
   * Sets the EU Login (ECAS) user name.
   *
   * @param ecasUserName the ECAS user name to set
   */
  public void setEcasUserName(String ecasUserName) {
    this.ecasUserName = ecasUserName;
  }

  /**
   * Returns the domain the user belongs to.
   *
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Sets the domain the user belongs to.
   *
   * @param domain the domain to set
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Returns the unique database identifier of the user.
   *
   * @return the user id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique database identifier of the user.
   *
   * @param id the user id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the login user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the login user name.
   *
   * @param userName the user name to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Returns the user's first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the user's first name.
   *
   * @param firstName the first name to set
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Returns the user's last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the user's last name.
   *
   * @param lastName the last name to set
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns the user's email address.
   *
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the user's email address.
   *
   * @param email the email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the associated Alfresco node reference.
   *
   * @return the node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the associated Alfresco node reference.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the identifier of the user's preferred locale.
   *
   * @return the locale identifier
   */
  public Long getLocaleID() {
    return localeID;
  }

  /**
   * Sets the identifier of the user's preferred locale.
   *
   * @param localeID the locale identifier to set
   */
  public void setLocaleID(Long localeID) {
    this.localeID = localeID;
  }

  /**
   * Returns whether the user's profile is visible to others.
   *
   * @return the visibility flag
   */
  public Boolean getVisibility() {
    return visibility;
  }

  /**
   * Sets whether the user's profile is visible to others.
   *
   * @param visibility the visibility flag to set
   */
  public void setVisibility(Boolean visibility) {
    this.visibility = visibility;
  }

  /**
   * Returns whether global (platform-wide) notifications are enabled.
   *
   * @return the global notification flag
   */
  public Boolean getGlobalNotification() {
    return globalNotification;
  }

  /**
   * Sets whether global (platform-wide) notifications are enabled.
   *
   * @param globalNotification the global notification flag to set
   */
  public void setGlobalNotification(Boolean globalNotification) {
    this.globalNotification = globalNotification;
  }
}
