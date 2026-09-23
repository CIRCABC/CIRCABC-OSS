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
 * Data transfer object describing a single user's membership within a CIRCABC Category.
 *
 * <p>Each record links a user to a Category together with the profile (role) granted to that user
 * in the context of the referenced Category node. Instances are typically assembled server-side and
 * serialized to JSON as part of the REST API responses that expose a user's category-level
 * memberships.
 */
public class UserCategoryMembershipRecord implements Serializable {

  /** Serialization version identifier for this DTO. */
  private static final long serialVersionUID = 3300655181779771569L;
  /** Human-readable name of the Category the membership relates to. */
  private String category;

  /** Name of the profile (role) held by the user within the Category. */
  private String profile;
  /** Alfresco node identifier of the Category node. */
  private String categoryNodeId;

  /**
   * Creates a fully populated category membership record.
   *
   * @param category the human-readable name of the Category
   * @param profile the profile (role) held by the user within the Category
   * @param categoryNodeId the Alfresco node identifier of the Category node
   */
  public UserCategoryMembershipRecord(
    String category,
    String profile,
    String categoryNodeId
  ) {
    this.category = category;
    this.profile = profile;
    this.categoryNodeId = categoryNodeId;
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
   * Returns the human-readable name of the Category.
   *
   * @return the category name
   */
  public String getCategory() {
    return category;
  }

  /**
   * Sets the human-readable name of the Category.
   *
   * @param category the category name to set
   */
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   * Returns the Alfresco node identifier of the Category node.
   *
   * @return the category node id
   */
  public String getCategoryNodeId() {
    return categoryNodeId;
  }

  /**
   * Sets the Alfresco node identifier of the Category node.
   *
   * @param categoryNodeId the category node id to set
   */
  public void setCategoryNodeId(String categoryNodeId) {
    this.categoryNodeId = categoryNodeId;
  }
}
