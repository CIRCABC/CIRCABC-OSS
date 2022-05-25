package eu.cec.digit.circabc.repo.app.model;

public class User {

    long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String nodeRef;
    private Long localeID;
    private String ecasUserName;
    private String domain;
    private Boolean visibility;
    private Boolean globalNotification;

    public User() {
        super();
    }

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
            Boolean globalNotification) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((ecasUserName == null) ? 0 : ecasUserName.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((globalNotification == null) ? 0 : globalNotification.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((localeID == null) ? 0 : localeID.hashCode());
        result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((visibility == null) ? 0 : visibility.hashCode());
        return result;
    }

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

    @Override
    public String toString() {
        return "User [id="
                + id
                + ", userName="
                + userName
                + ", firstName="
                + firstName
                + ", lastName="
                + lastName
                + ", email="
                + email
                + ", nodeRef="
                + nodeRef
                + ", localeID="
                + localeID
                + ", ecasUserName="
                + ecasUserName
                + ", domain="
                + domain
                + ", visibility="
                + visibility
                + ", globalNotification="
                + globalNotification
                + "]";
    }

    public String getEcasUserName() {
        return ecasUserName;
    }

    public void setEcasUserName(String ecasUserName) {
        this.ecasUserName = ecasUserName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public Long getLocaleID() {
        return localeID;
    }

    public void setLocaleID(Long localeID) {
        this.localeID = localeID;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public Boolean getGlobalNotification() {
        return globalNotification;
    }

    public void setGlobalNotification(Boolean globalNotification) {
        this.globalNotification = globalNotification;
    }
}
