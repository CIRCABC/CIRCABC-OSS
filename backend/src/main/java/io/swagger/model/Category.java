package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a CIRCABC Category as a JSON object.
 *
 * <p>A Category is the organizational grouping that sits between a Header and its
 * Interest Groups in the CIRCABC domain model. Instances of this class are
 * serialized to / deserialized from the JSON payloads exchanged by the REST API.
 * The class is a plain POJO holding the category's identifier, name, localized
 * title, optional logo reference and contact-related settings.
 */
public class Category {

  /** Unique identifier of the category (Alfresco node reference or id). */
  private String id = null;

  /** Machine/display name of the category. */
  private String name = null;

  /** Localized title of the category, keyed by language. */
  private I18nProperty title = new I18nProperty();

  /** Reference to the category's logo content, or {@code null} if none. */
  private String logoRef = null;

  /** Whether the category uses a single shared contact for its interest groups. */
  private Boolean useSingleContact = false;

  /** List of contact e-mail addresses associated with the category. */
  private List<String> contactEmails = new ArrayList<>();

  /** Whether the category's contact information has been verified. */
  private Boolean contactVerified = false;

  /** Creates an empty {@code Category} with default field values. */
  public Category() {
    super();
  }

  /**
   * Creates a {@code Category} with the given identifier and name.
   *
   * @param id the unique identifier of the category
   * @param name the name of the category
   */
  public Category(String id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the category.
   *
   * @param id the identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the category.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get title
   *
   * @return title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized title of the category.
   *
   * @param title the localized title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Gets the reference to the category's logo content.
   *
   * @return the logo reference, or {@code null} if none
   */
  public String getLogoRef() {
    return logoRef;
  }

  /**
   * Sets the reference to the category's logo content.
   *
   * @param logoRef the logo reference to set
   */
  public void setLogoRef(String logoRef) {
    this.logoRef = logoRef;
  }

  /**
   * Indicates whether the category uses a single shared contact.
   *
   * @return {@code true} if a single contact is used, {@code false} otherwise
   */
  public Boolean getUseSingleContact() {
    return useSingleContact;
  }

  /**
   * Sets whether the category uses a single shared contact.
   *
   * @param useSingleContact {@code true} to use a single contact
   */
  public void setUseSingleContact(Boolean useSingleContact) {
    this.useSingleContact = useSingleContact;
  }

  /**
   * Gets the list of contact e-mail addresses for the category.
   *
   * @return the contact e-mail addresses
   */
  public List<String> getContactEmails() {
    return contactEmails;
  }

  /**
   * Sets the list of contact e-mail addresses for the category.
   *
   * @param contactEmails the contact e-mail addresses to set
   */
  public void setContactEmails(List<String> contactEmails) {
    this.contactEmails = contactEmails;
  }

  /**
   * Indicates whether the category's contact information has been verified.
   *
   * @return {@code true} if the contact is verified, {@code false} otherwise
   */
  public Boolean getContactVerified() {
    return contactVerified;
  }

  /**
   * Sets whether the category's contact information has been verified.
   *
   * @param contactVerified {@code true} if the contact is verified
   */
  public void setContactVerified(Boolean contactVerified) {
    this.contactVerified = contactVerified;
  }

  /**
   * Compares this category with another object for equality. Two categories are
   * equal when all of their fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code Category}
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Category category = (Category) o;
    return (
      Objects.equals(this.id, category.id) &&
      Objects.equals(this.name, category.name) &&
      Objects.equals(this.title, category.title) &&
      Objects.equals(this.logoRef, category.logoRef) &&
      Objects.equals(this.useSingleContact, category.useSingleContact) &&
      Objects.equals(this.contactEmails, category.contactEmails) &&
      Objects.equals(this.contactVerified, category.contactVerified)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * all of the category's fields.
   *
   * @return the hash code value for this category
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      name,
      title,
      logoRef,
      useSingleContact,
      contactEmails,
      contactVerified
    );
  }

  /**
   * Returns a human-readable, multi-line string representation of this category.
   *
   * @return a string describing all of the category's fields
   */
  @Override
  public String toString() {
    return (
      "class Category {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    logoRef: " +
      toIndentedString(logoRef) +
      "\n" +
      "    useSingleContact: " +
      toIndentedString(useSingleContact) +
      "\n" +
      "    contactEmails: " +
      toIndentedString(contactEmails) +
      "\n" +
      "    contactVerified: " +
      toIndentedString(contactVerified) +
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
