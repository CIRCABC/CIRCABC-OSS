package io.swagger.model;

import java.util.Map;
import java.util.Objects;

/**
 * Data transfer object representing a CIRCABC user.
 *
 * <p>This model carries the user's identity and profile information (login id,
 * name, contact details, language preferences, visibility, avatar and an
 * open-ended map of additional properties) between the REST layer and its JSON
 * representation. It is a plain bean with getters/setters and value-based
 * {@link #equals(Object)}/{@link #hashCode()} semantics.
 */
public class User {

  /** Unique login identifier (user name) of the user. */
  private String userId = null;
  /** User's first (given) name. */
  private String firstname = null;
  /** User's last (family) name. */
  private String lastname = null;
  /** User's email address. */
  private String email = null;
  /** User's phone number. */
  private String phone = null;
  /** Preferred user-interface language (locale code). */
  private String uiLang = null;
  /** Preferred content filtering language (locale code). */
  private String contentFilterLang = null;
  /** Whether the user's profile is visible to others. */
  private Boolean visibility = null;
  /** Additional, extensible user properties keyed by name. */
  private Map<String, String> properties = null;
  /** Reference to the user's avatar image (e.g. node id or URL). */
  private String avatar = null;
  /** Whether the user uses the default avatar rather than a custom one. */
  private boolean defaultAvatar = false;

  /** Creates an empty user with all fields unset. */
  public User() {}

  /**
   * Creates a user with the core identity fields populated.
   *
   * @param userName the login identifier (user name) of the user
   * @param firstName the user's first (given) name
   * @param lastName the user's last (family) name
   * @param email the user's email address
   */
  public User(
    String userName,
    String firstName,
    String lastName,
    String email
  ) {
    this.userId = userName;
    this.firstname = firstName;
    this.lastname = lastName;
    this.email = email;
  }

  /**
   * @return the login identifier (user name) of the user
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the login identifier (user name) to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return the user's first (given) name
   */
  public String getFirstname() {
    return firstname;
  }

  /**
   * @param firstname the first (given) name to set
   */
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  /**
   * @return the user's last (family) name
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * @param lastname the last (family) name to set
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * @return the user's email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email the email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the user's phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * @param phone the phone number to set
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * @return the preferred user-interface language (locale code)
   */
  public String getUiLang() {
    return uiLang;
  }

  /**
   * @param uiLang the preferred user-interface language (locale code) to set
   */
  public void setUiLang(String uiLang) {
    this.uiLang = uiLang;
  }

  /**
   * @return the preferred content filtering language (locale code)
   */
  public String getContentFilterLang() {
    return contentFilterLang;
  }

  /**
   * @param contentFilterLang the preferred content filtering language (locale
   *     code) to set
   */
  public void setContentFilterLang(String contentFilterLang) {
    this.contentFilterLang = contentFilterLang;
  }

  /**
   * @return whether the user's profile is visible to others
   */
  public Boolean getVisibility() {
    return visibility;
  }

  /**
   * @param visibility whether the user's profile should be visible to others
   */
  public void setVisibility(Boolean visibility) {
    this.visibility = visibility;
  }

  /**
   * @return the additional, extensible user properties keyed by name
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * @param properties the additional user properties to set
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * @return a reference to the user's avatar image (e.g. node id or URL)
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * @param avatar the avatar image reference to set
   */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  /**
   * @return the defaultAvatar
   */
  public boolean isDefaultAvatar() {
    return defaultAvatar;
  }

  /**
   * @param defaultAvatar the defaultAvatar to set
   */
  public void setDefaultAvatar(boolean defaultAvatar) {
    this.defaultAvatar = defaultAvatar;
  }

  /**
   * Compares this user with another object for value equality across all
   * fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code User} with equal field
   *     values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return (
      Objects.equals(this.userId, user.userId) &&
      Objects.equals(this.firstname, user.firstname) &&
      Objects.equals(this.lastname, user.lastname) &&
      Objects.equals(this.email, user.email) &&
      Objects.equals(this.phone, user.phone) &&
      Objects.equals(this.uiLang, user.uiLang) &&
      Objects.equals(this.contentFilterLang, user.contentFilterLang) &&
      Objects.equals(this.visibility, user.visibility) &&
      Objects.equals(this.properties, user.properties) &&
      Objects.equals(this.defaultAvatar, user.defaultAvatar) &&
      Objects.equals(this.avatar, user.avatar)
    );
  }

  /**
   * @return a hash code consistent with {@link #equals(Object)}, derived from
   *     all fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      userId,
      firstname,
      lastname,
      email,
      phone,
      uiLang,
      contentFilterLang,
      visibility,
      properties,
      avatar,
      defaultAvatar
    );
  }

  /**
   * @return a human-readable, multi-line representation of this user and its
   *     fields
   */
  @Override
  public String toString() {
    return (
      "class User {\n" +
      "    userId: " +
      toIndentedString(userId) +
      "\n" +
      "    firstname: " +
      toIndentedString(firstname) +
      "\n" +
      "    lastname: " +
      toIndentedString(lastname) +
      "\n" +
      "    email: " +
      toIndentedString(email) +
      "\n" +
      "    phone: " +
      toIndentedString(phone) +
      "\n" +
      "    uiLang: " +
      toIndentedString(uiLang) +
      "\n" +
      "    contentFilterLang: " +
      toIndentedString(contentFilterLang) +
      "\n" +
      "    visibility: " +
      toIndentedString(visibility) +
      "\n" +
      "    properties: " +
      toIndentedString(properties) +
      "\n" +
      "    avatar: " +
      toIndentedString(avatar) +
      "\n" +
      "    defaultAvatar: " +
      toIndentedString(defaultAvatar) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
