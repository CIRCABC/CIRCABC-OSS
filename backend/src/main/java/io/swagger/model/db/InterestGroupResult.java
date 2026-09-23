package io.swagger.model.db;

/**
 * Data transfer object representing a single Interest Group entry as returned by
 * database-backed queries.
 *
 * <p>Instances carry the identifying, descriptive and membership-related
 * attributes of an Interest Group (IG), including localized translations and
 * references to associated Alfresco nodes. This class holds no business logic;
 * it is a plain container used to move IG result data between the persistence
 * layer and higher-level services.
 */
public class InterestGroupResult {

  /** Identifier of the current user's membership within this Interest Group. */
  Long memberId;
  /** Short, machine-oriented name of the Interest Group. */
  String name;
  /** Human-readable title of the Interest Group. */
  String title;
  /** Whether the Interest Group is publicly visible. */
  Boolean isPublic;
  /** Whether the current user is registered as a member of the group. */
  Boolean isRegistered;
  /** Whether the current user may apply for membership in the group. */
  Boolean isApplyForMembership;
  /** Alfresco node reference of the Interest Group. */
  String nodeRef;
  /** Localized translation of the Interest Group title. */
  String titleTranslation;
  /** Localized translation of the Interest Group's short description. */
  String lightDescTranslation;
  /** Alfresco node reference of the Interest Group's logo, if any. */
  String logoRef;
  /** Primary identifier of the Interest Group. */
  Long id;

  /** Creates an empty {@code InterestGroupResult} with all fields unset. */
  public InterestGroupResult() {
    super();
  }

  /**
   * Creates a fully populated {@code InterestGroupResult} without a logo reference.
   *
   * @param id primary identifier of the Interest Group
   * @param memberId identifier of the current user's membership within the group
   * @param name short, machine-oriented name of the group
   * @param title human-readable title of the group
   * @param isPublic whether the group is publicly visible
   * @param isRegistered whether the current user is a registered member
   * @param isApplyForMembership whether the current user may apply for membership
   * @param nodeRef Alfresco node reference of the group
   * @param titleTranslation localized translation of the group title
   * @param lightDescTranslation localized translation of the group's short description
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public InterestGroupResult(
    Long id,
    Long memberId,
    String name,
    String title,
    Boolean isPublic,
    Boolean isRegistered,
    Boolean isApplyForMembership,
    String nodeRef,
    String titleTranslation,
    String lightDescTranslation
  ) {
    super();
    this.id = id;
    this.memberId = memberId;
    this.name = name;
    this.title = title;
    this.isPublic = isPublic;
    this.isRegistered = isRegistered;
    this.isApplyForMembership = isApplyForMembership;
    this.nodeRef = nodeRef;
    this.titleTranslation = titleTranslation;
    this.lightDescTranslation = lightDescTranslation;
  }

  /**
   * Creates a fully populated {@code InterestGroupResult} including a logo reference.
   *
   * @param id primary identifier of the Interest Group
   * @param memberId identifier of the current user's membership within the group
   * @param name short, machine-oriented name of the group
   * @param title human-readable title of the group
   * @param isPublic whether the group is publicly visible
   * @param isRegistered whether the current user is a registered member
   * @param isApplyForMembership whether the current user may apply for membership
   * @param nodeRef Alfresco node reference of the group
   * @param titleTranslation localized translation of the group title
   * @param lightDescTranslation localized translation of the group's short description
   * @param logoRef Alfresco node reference of the group's logo
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public InterestGroupResult(
    Long id,
    Long memberId,
    String name,
    String title,
    Boolean isPublic,
    Boolean isRegistered,
    Boolean isApplyForMembership,
    String nodeRef,
    String titleTranslation,
    String lightDescTranslation,
    String logoRef
  ) {
    super();
    this.id = id;
    this.memberId = memberId;
    this.name = name;
    this.title = title;
    this.isPublic = isPublic;
    this.isRegistered = isRegistered;
    this.isApplyForMembership = isApplyForMembership;
    this.nodeRef = nodeRef;
    this.titleTranslation = titleTranslation;
    this.lightDescTranslation = lightDescTranslation;
    this.logoRef = logoRef;
  }

  /**
   * Returns a human-readable string representation of this result, listing its
   * core attributes. The logo reference is not included in the output.
   *
   * @return a string describing this {@code InterestGroupResult}
   */
  @Override
  public String toString() {
    return (
      "InterestGroupResult [id=" +
      id +
      ", memberId=" +
      memberId +
      ", name=" +
      name +
      ", title=" +
      title +
      ", isPublic=" +
      isPublic +
      ", isRegistered=" +
      isRegistered +
      ", isApplyForMembership=" +
      isApplyForMembership +
      ", nodeRef=" +
      nodeRef +
      ", titleTranslation=" +
      titleTranslation +
      ", lightDescTranslation=" +
      lightDescTranslation +
      "]"
    );
  }

  /**
   * Returns the primary identifier of the Interest Group.
   *
   * @return the group id, or {@code null} if unset
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the primary identifier of the Interest Group.
   *
   * @param id the group id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the current user's membership identifier within the group.
   *
   * @return the member id, or {@code null} if unset
   */
  public Long getMemberId() {
    return memberId;
  }

  /**
   * Sets the current user's membership identifier within the group.
   *
   * @param memberId the member id to set
   */
  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  /**
   * Returns the short, machine-oriented name of the group.
   *
   * @return the group name, or {@code null} if unset
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the short, machine-oriented name of the group.
   *
   * @param name the group name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the human-readable title of the group.
   *
   * @return the group title, or {@code null} if unset
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the human-readable title of the group.
   *
   * @param title the group title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns whether the group is publicly visible.
   *
   * @return {@code true} if public, {@code false} if not, or {@code null} if unset
   */
  public Boolean getIsPublic() {
    return isPublic;
  }

  /**
   * Sets whether the group is publicly visible.
   *
   * @param isPublic the public visibility flag to set
   */
  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Returns whether the current user is a registered member of the group.
   *
   * @return {@code true} if registered, {@code false} if not, or {@code null} if unset
   */
  public Boolean getIsRegistered() {
    return isRegistered;
  }

  /**
   * Sets whether the current user is a registered member of the group.
   *
   * @param isRegistered the registration flag to set
   */
  public void setIsRegistered(Boolean isRegistered) {
    this.isRegistered = isRegistered;
  }

  /**
   * Returns whether the current user may apply for membership in the group.
   *
   * @return {@code true} if application is allowed, {@code false} if not, or
   *     {@code null} if unset
   */
  public Boolean getIsApplyForMembership() {
    return isApplyForMembership;
  }

  /**
   * Sets whether the current user may apply for membership in the group.
   *
   * @param isApplyForMembership the apply-for-membership flag to set
   */
  public void setIsApplyForMembership(Boolean isApplyForMembership) {
    this.isApplyForMembership = isApplyForMembership;
  }

  /**
   * Returns the Alfresco node reference of the group.
   *
   * @return the node reference, or {@code null} if unset
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the Alfresco node reference of the group.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the localized translation of the group title.
   *
   * @return the title translation, or {@code null} if unset
   */
  public String getTitleTranslation() {
    return titleTranslation;
  }

  /**
   * Sets the localized translation of the group title.
   *
   * @param titleTranslation the title translation to set
   */
  public void setTitleTranslation(String titleTranslation) {
    this.titleTranslation = titleTranslation;
  }

  /**
   * Returns the localized translation of the group's short description.
   *
   * @return the short description translation, or {@code null} if unset
   */
  public String getLightDescTranslation() {
    return lightDescTranslation;
  }

  /**
   * Sets the localized translation of the group's short description.
   *
   * @param lightDescTranslation the short description translation to set
   */
  public void setLightDescTranslation(String lightDescTranslation) {
    this.lightDescTranslation = lightDescTranslation;
  }

  /**
   * Returns the Alfresco node reference of the group's logo.
   *
   * @return the logo node reference, or {@code null} if unset
   */
  public String getLogoRef() {
    return logoRef;
  }

  /**
   * Sets the Alfresco node reference of the group's logo.
   *
   * @param logoRef the logo node reference to set
   */
  public void setLogoRef(String logoRef) {
    this.logoRef = logoRef;
  }
}
