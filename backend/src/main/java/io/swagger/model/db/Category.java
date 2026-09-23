package io.swagger.model.db;

/**
 * Domain/persistence model representing a CIRCABC Category.
 *
 * <p>In the CIRCABC domain hierarchy (Headers &rarr; Categories &rarr; Interest
 * Groups), a Category groups related Interest Groups under a parent Header. This
 * plain data-holder carries the database identifier along with the associated
 * Alfresco repository node reference, presentation attributes (name, title,
 * optional logo) and the identifier of the owning Header.
 */
public class Category {

  /** Database identifier of the category. */
  private long id;

  /** Machine/short name of the category. */
  private String name;

  /** Human-readable display title of the category. */
  private String title;

  /** Reference to the corresponding node in the Alfresco repository. */
  private String nodeRef;

  /** Reference to the node holding the category logo; empty when none is set. */
  private String logoRef;

  /** Identifier of the parent Header that owns this category. */
  private long headerID;

  /**
   * Creates a category without a logo reference.
   *
   * <p>The {@code logoRef} field is initialised to an empty string.
   *
   * @param id the database identifier of the category
   * @param name the machine/short name of the category
   * @param title the human-readable display title
   * @param nodeRef the Alfresco repository node reference
   * @param headerID the identifier of the parent Header
   */
  public Category(
    long id,
    String name,
    String title,
    String nodeRef,
    long headerID
  ) {
    super();
    this.id = id;
    this.name = name;
    this.title = title;
    this.nodeRef = nodeRef;
    this.headerID = headerID;
    this.logoRef = "";
  }

  /**
   * Creates a fully populated category, including its logo reference.
   *
   * @param id the database identifier of the category
   * @param name the machine/short name of the category
   * @param title the human-readable display title
   * @param nodeRef the Alfresco repository node reference
   * @param headerID the identifier of the parent Header
   * @param logoRef the reference to the node holding the category logo
   */
  public Category(
    long id,
    String name,
    String title,
    String nodeRef,
    long headerID,
    String logoRef
  ) {
    super();
    this.id = id;
    this.name = name;
    this.title = title;
    this.nodeRef = nodeRef;
    this.headerID = headerID;
    this.logoRef = logoRef;
  }

  /** Creates an empty category with default field values. */
  public Category() {}

  /**
   * Returns the database identifier of the category.
   *
   * @return the category id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the database identifier of the category.
   *
   * @param id the category id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the machine/short name of the category.
   *
   * @return the category name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the machine/short name of the category.
   *
   * @param name the category name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the human-readable display title of the category.
   *
   * @return the category title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the human-readable display title of the category.
   *
   * @param title the category title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the Alfresco repository node reference of the category.
   *
   * @return the node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the Alfresco repository node reference of the category.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the identifier of the parent Header that owns this category.
   *
   * @return the parent Header id
   */
  public long getHeaderID() {
    return headerID;
  }

  /**
   * Sets the identifier of the parent Header that owns this category.
   *
   * @param headerID the parent Header id to set
   */
  public void setHeaderID(long headerID) {
    this.headerID = headerID;
  }

  /**
   * Returns the reference to the node holding the category logo.
   *
   * @return the logo node reference, or an empty string when none is set
   */
  public String getLogoRef() {
    return logoRef;
  }

  /**
   * Sets the reference to the node holding the category logo.
   *
   * @param logoRef the logo node reference to set
   */
  public void setLogoRef(String logoRef) {
    this.logoRef = logoRef;
  }

  /**
   * Returns a string representation of this category for debugging/logging.
   *
   * <p>Note: the {@code logoRef} field is not included in the output.
   *
   * @return a string describing the category's id, name, title, node reference
   *     and parent Header id
   */
  @Override
  public String toString() {
    return (
      "Category [id=" +
      id +
      ", name=" +
      name +
      ", title=" +
      title +
      ", nodeRef=" +
      nodeRef +
      ", headerID=" +
      headerID +
      "]"
    );
  }
}
