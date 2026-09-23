package io.swagger.model;

/**
 * Data model describing a single user entry processed during a bulk import
 * operation.
 *
 * <p>Each instance captures the outcome of importing (or attempting to import) one
 * user into an Interest Group (IG), including the user's identity, the target
 * Interest Group, the source file the entry originated from, the assigned profile
 * and the resulting status of the import.
 */
public class BulkImportUserDataModel {

  /** Login/username of the user being imported. */
  private String username;

  /** Human-readable name of the target Interest Group. */
  private String igName;

  /** Reference (identifier) of the target Interest Group. */
  private String igRef;

  /** Name of the source file the user entry was read from. */
  private String fromFile;

  /** Email address of the user being imported. */
  private String email;

  /** Status of the import for this user (e.g. success or failure indicator). */
  private String status;

  /** Identifier of the profile assigned to the user within the Interest Group. */
  private String profileId;

  /**
   * Creates a fully populated bulk import user data entry.
   *
   * @param username the login/username of the user
   * @param igName the human-readable name of the target Interest Group
   * @param igRef the reference (identifier) of the target Interest Group
   * @param fromFile the name of the source file the entry originated from
   * @param email the email address of the user
   * @param status the status of the import for this user
   * @param profileId the identifier of the profile assigned to the user
   */
  public BulkImportUserDataModel(
    String username,
    String igName,
    String igRef,
    String fromFile,
    String email,
    String status,
    String profileId
  ) {
    super();
    this.username = username;
    this.igName = igName;
    this.igRef = igRef;
    this.fromFile = fromFile;
    this.email = email;
    this.status = status;
    this.profileId = profileId;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
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
   * @return the igRef
   */
  public String getIgRef() {
    return igRef;
  }

  /**
   * @param igRef the igRef to set
   */
  public void setIgRef(String igRef) {
    this.igRef = igRef;
  }

  /**
   * @return the fromFile
   */
  public String getFromFile() {
    return fromFile;
  }

  /**
   * @param fromFile the fromFile to set
   */
  public void setFromFile(String fromFile) {
    this.fromFile = fromFile;
  }

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
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the profileId
   */
  public String getProfileId() {
    return profileId;
  }

  /**
   * @param profileId the profileId to set
   */
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }
}
