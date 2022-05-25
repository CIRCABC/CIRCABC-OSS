/**
 *
 */
package io.swagger.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/** @author schwerr */
@XmlRootElement(name = "member")
public class BulkImportUser {

    String userName = null;
    String lastName = null;
    String email = null;
    String profile = null;

    /** @return the userName */
    public String getUserName() {
        return userName;
    }

    /** @param userName the userName to set */
    @XmlAttribute(name = "username")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** @return the lastName */
    public String getLastName() {
        return lastName;
    }

    /** @param lastName the lastName to set */
    @XmlAttribute(name = "lastname")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** @return the email */
    public String getEmail() {
        return email;
    }

    /** @param email the email to set */
    @XmlAttribute(name = "email")
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the profile */
    public String getProfile() {
        return profile;
    }

    /** @param profile the profile to set */
    @XmlAttribute(name = "profile")
    public void setProfile(String profile) {
        this.profile = profile;
    }
}
