package io.swagger.model.db;

/**
 * Database model representing a CIRCABC Header.
 *
 * <p>A Header is the top-level organizational unit in the CIRCABC domain hierarchy
 * (Headers &rarr; Categories &rarr; Interest Groups &rarr; Services). This is a plain data
 * carrier (POJO) that maps a Header record from the persistence layer, holding its
 * identifier, display name, description and the reference to the corresponding node
 * in the underlying Alfresco repository.
 */
public class Header {

  /** Unique numeric identifier of the header (database primary key). */
  private long id;

  /** Human-readable display name of the header. */
  private String name;

  /** Free-text description of the header. */
  private String description;

  /** Reference to the associated node in the Alfresco repository. */
  private String nodeRef;

  /** Creates an empty header with no field values initialized. */
  public Header() {}

  /**
   * Creates a fully populated header.
   *
   * @param id the unique numeric identifier of the header
   * @param name the human-readable display name
   * @param description the free-text description
   * @param nodeRef the reference to the associated Alfresco repository node
   */
  public Header(long id, String name, String description, String nodeRef) {
    super();
    this.id = id;
    this.name = name;
    this.description = description;
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the unique numeric identifier of the header.
   *
   * @return the header id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique numeric identifier of the header.
   *
   * @param id the header id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the human-readable display name of the header.
   *
   * @return the header name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the human-readable display name of the header.
   *
   * @param name the header name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the free-text description of the header.
   *
   * @return the header description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the free-text description of the header.
   *
   * @param description the header description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the reference to the associated node in the Alfresco repository.
   *
   * @return the node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the reference to the associated node in the Alfresco repository.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns a string representation of this header, including all field values.
   *
   * @return a string describing this header
   */
  @Override
  public String toString() {
    return (
      "Header [id=" +
      id +
      ", name=" +
      name +
      ", description=" +
      description +
      ", nodeRef=" +
      nodeRef +
      "]"
    );
  }
}
