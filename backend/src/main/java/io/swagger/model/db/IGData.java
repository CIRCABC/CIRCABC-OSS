package io.swagger.model.db;

/**
 * Lightweight data holder representing an Interest Group (IG) as stored in / retrieved from the
 * database.
 *
 * <p>It carries the essential identifying information for an Interest Group: its unique identifier,
 * its display name, and the identifier of the Category it belongs to. This is a plain data-transfer
 * object with no business logic.
 */
public class IGData {

  /** Display name of the Interest Group. */
  private String igName;

  /** Unique identifier of the Interest Group. */
  private String igId;

  /** Identifier of the Category the Interest Group belongs to. */
  private String categoryId;

  /** Creates an empty {@code IGData} instance with all fields left unset. */
  public IGData() {}

  /**
   * Creates a fully populated {@code IGData} instance.
   *
   * @param igId the unique identifier of the Interest Group
   * @param igName the display name of the Interest Group
   * @param categoryId the identifier of the Category the Interest Group belongs to
   */
  public IGData(String igId, String igName, String categoryId) {
    super();
    this.igName = igName;
    this.igId = igId;
    this.categoryId = categoryId;
  }

  /**
   * @return the igName
   */
  public String getIgName() {
    return igName;
  }

  /**
   * @param igName the igName to set
   */
  public void setIgName(String igName) {
    this.igName = igName;
  }

  /**
   * @return the igId
   */
  public String getIgId() {
    return igId;
  }

  /**
   * @param igId the igId to set
   */
  public void setIgId(String igId) {
    this.igId = igId;
  }

  /**
   * @return the categoryId
   */
  public String getCategoryId() {
    return categoryId;
  }

  /**
   * @param categoryId the categoryId to set
   */
  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }
}
