package eu.europa.ec.digit.circabc.rest.service.user;

import java.util.Locale;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Data transfer object holding the details of a CIRCABC user.
 *
 * <p>This is a simple mutable value holder (POJO) used by the user service layer to carry a
 * person's identifiers and profile attributes across the REST layer. It aggregates values coming
 * from several sources in the Alfresco repository:
 *
 * <ul>
 *   <li>mandatory identifiers (the person {@link NodeRef} and the user name);
 *   <li>standard Alfresco user model properties (email, first name, last name);
 *   <li>CIRCABC-specific user aspect properties (description, fax, organisation, phone, postal
 *       address, title, url, notification and visibility flags);
 *   <li>user preferences (UI language, content filter language, signature and avatar).
 * </ul>
 *
 * <p>The class carries no business logic; it only exposes getters and setters for each field.
 */
public class UserDetails {

  //-----------------
  //--  Mandatory values (identifiers)

  /** Reference to the Alfresco person node this user corresponds to. */
  private NodeRef nodeRef;

  /**
   * Sets the reference to the Alfresco person node.
   *
   * @param nodeRef the person {@link NodeRef} to set
   */
  public void setNodeRef(NodeRef nodeRef) {
    this.nodeRef = nodeRef;
  }

  /** Unique login/user name identifying the user. */
  private String userName;

  /** Email address of the user (Alfresco user model property). */
  private String email;

  /** First name of the user (Alfresco user model property). */
  private String firstName;

  /** Last name of the user (Alfresco user model property). */
  private String lastName;

  /** Free-text description of the user (CIRCABC user aspect). */
  private String description;

  /** Fax number of the user (CIRCABC user aspect). */
  private String fax;

  /** Organisation the user belongs to (CIRCABC user aspect). */
  private String organisation;

  /** Phone number of the user (CIRCABC user aspect). */
  private String phone;

  /** Postal address of the user (CIRCABC user aspect). */
  private String postalAddress;

  /** Title of the user (CIRCABC user aspect). */
  private String title;

  /** Personal or organisation URL of the user (CIRCABC user aspect). */
  private String url;

  /** Whether the user has global notifications enabled. */
  private Boolean globalNotification;

  /** Whether the user's profile is visible to other users. */
  private Boolean visibility;

  /** Preferred language for the user interface. */
  private String userInterfaceLanguage;

  /** Signature text appended to the user's messages. */
  private String signature;

  /** Reference to the node holding the user's avatar image. */
  private NodeRef avatar;

  /** Preferred locale used to filter content by language. */
  private Locale contentFilterLanguage;

  /**
   * @return the reference to the Alfresco person node
   */
  public NodeRef getNodeRef() {
    return nodeRef;
  }

  /**
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName the user name to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  //-----------------
  //--  Properties of Alfresco user model

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName the firstName to set
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return the lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param lastName the lastName to set
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  //-----------------
  //--  Properties of circabc user aspect

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the fax
   */
  public String getFax() {
    return fax;
  }

  /**
   * @param fax the fax to set
   */
  public void setFax(String fax) {
    this.fax = fax;
  }

  /**
   * @return the organisation
   */
  public String getOrganisation() {
    return organisation;
  }

  /**
   * @param organisation the organisation to set
   */
  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  /**
   * @return the phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * @param phone the phone to set
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * @return the postalAddress
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * @param postalAddress the postalAddress to set
   */
  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  //-----------------
  //--  Properties located under circabc aspect that should be moved in the preferences

  /**
   * @return the globalNotification
   */
  public Boolean getGlobalNotification() {
    return globalNotification;
  }

  /**
   * @param globalNotification the globalNotification to set
   */
  public void setGlobalNotification(Boolean globalNotification) {
    this.globalNotification = globalNotification;
  }

  /**
   * @return the visibility
   */
  public Boolean getVisibility() {
    return visibility;
  }

  /**
   * @param visibility the visibility to set
   */
  public void setVisibility(Boolean visibility) {
    this.visibility = visibility;
  }

  //-----------------
  //--  Preferences

  /**
   * @return the userInterfaceLanguage
   */
  public String getUserInterfaceLanguage() {
    return this.userInterfaceLanguage;
  }

  /**
   * @param userInterfaceLanguage the userInterfaceLanguage to set
   */
  public void setUserInterfaceLanguage(String userInterfaceLanguage) {
    this.userInterfaceLanguage = userInterfaceLanguage;
  }

  /**
   * @return the contentFilterLanguage
   */
  public Locale getContentFilterLanguage() {
    return contentFilterLanguage;
  }

  /**
   * @param contentFilterLanguage the contentFilterLanguage to set
   */
  public void setContentFilterLanguage(final Locale contentFilterLanguage) {
    this.contentFilterLanguage = contentFilterLanguage;
  }

  /**
   * @return the signature
   */
  public String getSignature() {
    return signature;
  }

  /**
   * @param signature the signature to set
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * @return the avatar
   */
  public NodeRef getAvatar() {
    return avatar;
  }

  /**
   * @param avatarRef the avatar node reference to set
   */
  public void setAvatar(NodeRef avatarRef) {
    this.avatar = avatarRef;
  }
}
