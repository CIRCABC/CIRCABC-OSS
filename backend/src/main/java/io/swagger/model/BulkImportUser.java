/**
 *
 */
package io.swagger.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single user entry parsed from a bulk import payload.
 *
 * <p>This model is bound to an XML {@code <member>} element (via JAXB) and carries the minimal set
 * of attributes needed to create or invite a user during a bulk membership import: the user's
 * login name, last name, email address and the profile (role) to assign within an Interest Group.
 *
 * @author schwerr
 */
@XmlRootElement(name = "member")
public class BulkImportUser {

  /** The login/user name of the member (mapped from the {@code username} XML attribute). */
  String userName = null;

  /** The last name of the member (mapped from the {@code lastname} XML attribute). */
  String lastName = null;

  /** The email address of the member (mapped from the {@code email} XML attribute). */
  String email = null;

  /** The profile (role) to assign to the member (mapped from the {@code profile} XML attribute). */
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
