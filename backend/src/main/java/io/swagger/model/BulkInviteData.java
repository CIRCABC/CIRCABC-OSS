package io.swagger.model;

import java.util.List;

/**
 * Data transfer object describing the payload for a bulk invitation request.
 *
 * <p>It bundles together the collection of users to be imported/invited into an
 * Interest Group along with the set of Interest Group profiles that should be
 * assigned to those users. It is typically used when inviting several users at
 * once and mapping them to specific IG profiles.
 */
public class BulkInviteData {

  /** The list of users to be imported and invited. */
  private List<BulkImportUserDataModel> bulkImportUserData;

  /**
   * The Interest Group profiles (name/value pairs) to associate with the
   * imported users.
   */
  private List<NameValue> igProfiles;

  /**
   * Creates a new {@code BulkInviteData} instance.
   *
   * @param bulkImportUserData the list of users to import and invite
   * @param igProfiles the Interest Group profiles to assign to the users
   */
  public BulkInviteData(
    List<BulkImportUserDataModel> bulkImportUserData,
    List<NameValue> igProfiles
  ) {
    super();
    this.bulkImportUserData = bulkImportUserData;
    this.igProfiles = igProfiles;
  }

  /**
   * @return the bulkImportUserData
   */
  public List<BulkImportUserDataModel> getBulkImportUserData() {
    return bulkImportUserData;
  }

  /**
   * @param bulkImportUserData the bulkImportUserData to set
   */
  public void setBulkImportUserData(
    List<BulkImportUserDataModel> bulkImportUserData
  ) {
    this.bulkImportUserData = bulkImportUserData;
  }

  /**
   * @return the igProfiles
   */
  public List<NameValue> getIgProfiles() {
    return igProfiles;
  }

  /**
   * @param igProfiles the igProfiles to set
   */
  public void setIgProfiles(List<NameValue> igProfiles) {
    this.igProfiles = igProfiles;
  }
}
