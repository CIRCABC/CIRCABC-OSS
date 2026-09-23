package io.swagger.model.db;

/**
 * Data model representing a CIRCABC Interest Group (IG) as persisted/retrieved
 * from the database layer.
 *
 * <p>An Interest Group is the central collaboration space in CIRCABC. It is
 * organized under a {@link #categoryID Category} and groups together the
 * services (Library, Newsgroup, Information, Events) shared by its members.
 * This class is a plain data-transfer object: it carries the group's identity,
 * descriptive attributes, its backing Alfresco node reference and visibility
 * flags, but contains no business logic.
 */
public class InterestGroup {

  /** Unique database identifier of the Interest Group. */
  private long id;

  /** Machine-readable / short name of the Interest Group. */
  private String name;

  /** Human-readable display title of the Interest Group. */
  private String title;

  /** Reference to the backing Alfresco repository node for this group. */
  private String nodeRef;

  /** Whether the group's content is publicly visible. */
  private boolean isPublic;

  /** Whether the group is registered (listed) in the platform. */
  private boolean isRegistered;

  /** Whether users are allowed to apply for membership to this group. */
  private boolean isApplyForMembership;

  /** Identifier of the parent Category this group belongs to. */
  private long categoryID;

  /** Reference to the Alfresco node holding the group's logo, if any. */
  private String logoRef;

  /**
   * Creates an empty Interest Group with default field values.
   */
  public InterestGroup() {
    super();
  }

  /**
   * Creates a fully populated Interest Group.
   *
   * @param categoryID identifier of the parent Category
   * @param id unique database identifier of the group
   * @param name machine-readable / short name of the group
   * @param title human-readable display title
   * @param nodeRef reference to the backing Alfresco node
   * @param isPublic whether the group's content is publicly visible
   * @param isRegistered whether the group is registered (listed)
   * @param isApplyForMembership whether users may apply for membership
   * @param logoRef reference to the Alfresco node holding the group's logo
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public InterestGroup(
    long categoryID,
    long id,
    String name,
    String title,
    String nodeRef,
    boolean isPublic,
    boolean isRegistered,
    boolean isApplyForMembership,
    String logoRef
  ) {
    super();
    this.categoryID = categoryID;
    this.id = id;
    this.name = name;
    this.title = title;
    this.nodeRef = nodeRef;
    this.isPublic = isPublic;
    this.isRegistered = isRegistered;
    this.isApplyForMembership = isApplyForMembership;
    this.logoRef = logoRef;
  }

  /**
   * Returns the identifier of the parent Category.
   *
   * @return the parent Category identifier
   */
  public long getCategoryID() {
    return categoryID;
  }

  /**
   * Sets the identifier of the parent Category.
   *
   * @param categoryID the parent Category identifier to set
   */
  public void setCategoryID(long categoryID) {
    this.categoryID = categoryID;
  }

  /**
   * Returns the unique database identifier of the group.
   *
   * @return the group identifier
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique database identifier of the group.
   *
   * @param id the group identifier to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the machine-readable / short name of the group.
   *
   * @return the group name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the machine-readable / short name of the group.
   *
   * @param name the group name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the human-readable display title of the group.
   *
   * @return the group title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the human-readable display title of the group.
   *
   * @param title the group title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the reference to the backing Alfresco node.
   *
   * @return the node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the reference to the backing Alfresco node.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Indicates whether the group's content is publicly visible.
   *
   * @return {@code true} if the group is public, {@code false} otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Sets whether the group's content is publicly visible.
   *
   * @param isPublic {@code true} to mark the group as public
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Indicates whether the group is registered (listed) in the platform.
   *
   * @return {@code true} if the group is registered, {@code false} otherwise
   */
  public boolean isRegistered() {
    return isRegistered;
  }

  /**
   * Sets whether the group is registered (listed) in the platform.
   *
   * @param isRegistered {@code true} to mark the group as registered
   */
  public void setRegistered(boolean isRegistered) {
    this.isRegistered = isRegistered;
  }

  /**
   * Indicates whether users are allowed to apply for membership.
   *
   * @return {@code true} if membership applications are allowed
   */
  public boolean isApplyForMembership() {
    return isApplyForMembership;
  }

  /**
   * Sets whether users are allowed to apply for membership.
   *
   * @param isApplyForMemdership {@code true} to allow membership applications
   */
  public void setApplyForMembership(boolean isApplyForMemdership) {
    this.isApplyForMembership = isApplyForMemdership;
  }

  /**
   * @return the logoRef
   */
  public String getLogoRef() {
    return logoRef;
  }

  /**
   * @param logoRef the logoRef to set
   */
  public void setLogoRef(String logoRef) {
    this.logoRef = logoRef;
  }

  /**
   * Returns a string representation of this Interest Group, including all of
   * its fields. Intended for logging and debugging purposes.
   *
   * @return a string describing this Interest Group
   */
  @Override
  public String toString() {
    return (
      "InterestGroup [categoryID=" +
      categoryID +
      ", id=" +
      id +
      ", name=" +
      name +
      ", title=" +
      title +
      ", nodeRef=" +
      nodeRef +
      ", isPublic=" +
      isPublic +
      ", isRegistered=" +
      isRegistered +
      ", isApplyForMembership=" +
      isApplyForMembership +
      ", logoRef=" +
      logoRef +
      "]"
    );
  }
}
