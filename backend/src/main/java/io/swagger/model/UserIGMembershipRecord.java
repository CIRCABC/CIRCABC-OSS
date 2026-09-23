/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package io.swagger.model;

import java.io.Serializable;

/**
 * Data transfer object describing a single membership of a user within an Interest Group (IG).
 *
 * <p>Each record links a user's profile to the Interest Group it belongs to and to the Category
 * that owns that Interest Group. Both the technical identifiers (Alfresco node references and
 * short names) and the human-readable titles are carried so that callers can render membership
 * information without additional lookups. When a title is not supplied, the corresponding short
 * name is used as a fallback (see {@link #bestTitleOrName(String, String)}).
 *
 * <p>Instances are typically assembled by the service layer and serialized to JSON as part of the
 * REST responses that list the memberships of a user.
 */
public class UserIGMembershipRecord implements Serializable {

  /** Serialization version identifier for this {@link Serializable} DTO. */
  private static final long serialVersionUID = -8706766125898983357L;

  /** Short name of the Category that owns the Interest Group. */
  private String category;

  /** Alfresco node reference identifier of the Category. */
  private String categoryNodeId;

  /** Short name of the Interest Group the membership belongs to. */
  private String interestGroup;

  /** Alfresco node reference identifier of the Interest Group. */
  private String interestGroupNodeId;

  /** Human-readable title of the Interest Group (falls back to its short name). */
  private String interestGroupTitle;

  /** Name of the membership profile (role) held by the user in the Interest Group. */
  private String profile;

  /** Alfresco node reference identifier of the membership profile. */
  private String profileNodeRefId;

  /** Human-readable title of the Category (falls back to its short name). */
  private String categoryTitle;

  /** Human-readable title of the membership profile (falls back to its name). */
  private String profileTitle;

  /** Name of the underlying Alfresco group backing this membership, if any. */
  private String alfrescoGroup;

  /**
   * Creates a membership record from its core identifiers and titles.
   *
   * @param interestGroupNodeId the Alfresco node id of the Interest Group
   * @param interesGroup the short name of the Interest Group
   * @param categoryNodeId the Alfresco node id of the owning Category
   * @param category the short name of the owning Category
   * @param profile the membership profile (role) name
   * @param categoryTitle the Category title; the short name is used when blank
   * @param interesGroupTitle the Interest Group title; the short name is used when blank
   * @param profileTitle the profile title; the profile name is used when blank
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public UserIGMembershipRecord(
    String interestGroupNodeId,
    String interesGroup,
    String categoryNodeId,
    String category,
    String profile,
    String categoryTitle,
    String interesGroupTitle,
    String profileTitle
  ) {
    this.interestGroupNodeId = interestGroupNodeId;
    this.category = category;
    this.categoryNodeId = categoryNodeId;
    this.interestGroup = interesGroup;
    this.profile = profile;
    this.categoryTitle = bestTitleOrName(categoryTitle, category);
    this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
    this.profileTitle = bestTitleOrName(profileTitle, profile);
  }

  /**
   * Creates a membership record that also carries the backing Alfresco group name.
   *
   * @param interestGroupNodeId the Alfresco node id of the Interest Group
   * @param interesGroup the short name of the Interest Group
   * @param categoryNodeId the Alfresco node id of the owning Category
   * @param category the short name of the owning Category
   * @param profile the membership profile (role) name
   * @param categoryTitle the Category title; the short name is used when blank
   * @param interesGroupTitle the Interest Group title; the short name is used when blank
   * @param profileTitle the profile title; the profile name is used when blank
   * @param alfrescoGroup the name of the underlying Alfresco group
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public UserIGMembershipRecord(
    String interestGroupNodeId,
    String interesGroup,
    String categoryNodeId,
    String category,
    String profile,
    String categoryTitle,
    String interesGroupTitle,
    String profileTitle,
    String alfrescoGroup
  ) {
    this.interestGroupNodeId = interestGroupNodeId;
    this.category = category;
    this.categoryNodeId = categoryNodeId;
    this.interestGroup = interesGroup;
    this.profile = profile;
    this.categoryTitle = bestTitleOrName(categoryTitle, category);
    this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
    this.profileTitle = bestTitleOrName(profileTitle, profile);
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * Creates a fully populated membership record, including the profile node reference.
   *
   * @param interestGroupNodeId the Alfresco node id of the Interest Group
   * @param interesGroup the short name of the Interest Group
   * @param categoryNodeId the Alfresco node id of the owning Category
   * @param category the short name of the owning Category
   * @param profile the membership profile (role) name
   * @param categoryTitle the Category title; the short name is used when blank
   * @param interesGroupTitle the Interest Group title; the short name is used when blank
   * @param profileTitle the profile title; the profile name is used when blank
   * @param alfrescoGroup the name of the underlying Alfresco group
   * @param profileNodeRefId the Alfresco node id of the membership profile
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public UserIGMembershipRecord(
    String interestGroupNodeId,
    String interesGroup,
    String categoryNodeId,
    String category,
    String profile,
    String categoryTitle,
    String interesGroupTitle,
    String profileTitle,
    String alfrescoGroup,
    String profileNodeRefId
  ) {
    this.interestGroupNodeId = interestGroupNodeId;
    this.category = category;
    this.categoryNodeId = categoryNodeId;
    this.interestGroup = interesGroup;
    this.profile = profile;
    this.categoryTitle = bestTitleOrName(categoryTitle, category);
    this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
    this.profileTitle = bestTitleOrName(profileTitle, profile);
    this.alfrescoGroup = alfrescoGroup;
    this.setprofileNodeRefId(profileNodeRefId);
  }

  /**
   * @return the profile
   */
  public String getProfile() {
    return profile;
  }

  /**
   * @param profile the profile to set
   */
  public void setProfile(String profile) {
    this.profile = profile;
  }

  /**
   * @return the short name of the owning Category
   */
  public String getCategory() {
    return category;
  }

  /**
   * @param category the Category short name to set
   */
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   * @return the short name of the Interest Group
   */
  public String getInterestGroup() {
    return interestGroup;
  }

  /**
   * @param interesGroup the Interest Group short name to set
   */
  public void setInterestGroup(String interesGroup) {
    this.interestGroup = interesGroup;
  }

  /**
   * @return the Alfresco node id of the Interest Group
   */
  public String getInterestGroupNodeId() {
    return interestGroupNodeId;
  }

  /**
   * @param interestGroupNodeId the Interest Group node id to set
   */
  public void setInterestGroupNodeId(String interestGroupNodeId) {
    this.interestGroupNodeId = interestGroupNodeId;
  }

  /**
   * @return the categoryTitle
   */
  public final String getCategoryTitle() {
    return categoryTitle;
  }

  /**
   * @param categoryTitle the categoryTitle to set
   */
  public final void setCategoryTitle(String categoryTitle) {
    this.categoryTitle = categoryTitle;
  }

  /**
   * @return the interestGroupTitle
   */
  public final String getInterestGroupTitle() {
    return interestGroupTitle;
  }

  /**
   * @param interestGroupTitle the interestGroupTitle to set
   */
  public final void setInterestGroupTitle(String interestGroupTitle) {
    this.interestGroupTitle = interestGroupTitle;
  }

  /**
   * @return the profileTitle
   */
  public final String getProfileTitle() {
    return profileTitle;
  }

  /**
   * @param profileTitle the profileTitle to set
   */
  public final void setProfileTitle(String profileTitle) {
    this.profileTitle = profileTitle;
  }

  /**
   * Returns the given title when it is present, otherwise falls back to the supplied name.
   *
   * @param title the preferred human-readable title, may be {@code null} or blank
   * @param name the fallback short name used when the title is missing
   * @return {@code title} when it is non-blank, otherwise {@code name}
   */
  private String bestTitleOrName(final String title, final String name) {
    if (title == null || title.trim().isEmpty()) {
      return name;
    } else {
      return title;
    }
  }

  /**
   * @return the Alfresco node id of the owning Category
   */
  public String getCategoryNodeId() {
    return categoryNodeId;
  }

  /**
   * @param categoryNodeId the Category node id to set
   */
  public void setCategoryNodeId(String categoryNodeId) {
    this.categoryNodeId = categoryNodeId;
  }

  /**
   * Indicates whether this membership originates from an imported (external) profile.
   *
   * @return {@code true} if the profile title contains a namespace separator ({@code ":"}),
   *     {@code false} otherwise or when no profile title is set
   */
  public boolean isImported() {
    if (profileTitle == null) {
      return false;
    } else {
      return profileTitle.contains(":");
    }
  }

  /**
   * @return the name of the underlying Alfresco group, or {@code null} if none
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * @param alfrescoGroup the Alfresco group name to set
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * @return the Alfresco node id of the membership profile
   */
  public String getprofileNodeRefId() {
    return profileNodeRefId;
  }

  /**
   * @param profileNodeRefId the membership profile node id to set
   */
  public void setprofileNodeRefId(String profileNodeRefId) {
    this.profileNodeRefId = profileNodeRefId;
  }
}
