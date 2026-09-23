package io.swagger.model.db;

/**
 * Plain data model that pairs a CIRCABC Header with one of its Categories.
 *
 * <p>Each instance flattens the two levels of the top-level taxonomy
 * (Header &rarr; Category) into a single record, holding the Alfresco node
 * reference, machine name and human-readable title for both the parent header
 * and the associated category. It is typically produced from database or
 * repository lookups and used to expose the header/category structure.
 */
public class HeaderCategory {

  /** Alfresco node reference of the parent header. */
  private String headerNodeRef;
  /** Machine (short) name of the parent header. */
  private String headerName;
  /** Human-readable title of the parent header. */
  private String headerTitle;
  /** Alfresco node reference of the category. */
  private String categoryNodeRef;
  /** Machine (short) name of the category. */
  private String categoryName;
  /** Human-readable title of the category. */
  private String categoryTitle;

  /** Creates an empty {@code HeaderCategory} with all fields unset. */
  public HeaderCategory() {}

  /**
   * Creates a fully populated header/category pair.
   *
   * @param headerNodeRef the Alfresco node reference of the parent header
   * @param headerName the machine (short) name of the parent header
   * @param headerTitle the human-readable title of the parent header
   * @param categoryNodeRef the Alfresco node reference of the category
   * @param categoryName the machine (short) name of the category
   * @param categoryTitle the human-readable title of the category
   */
  public HeaderCategory(
    String headerNodeRef,
    String headerName,
    String headerTitle,
    String categoryNodeRef,
    String categoryName,
    String categoryTitle
  ) {
    super();
    this.headerNodeRef = headerNodeRef;
    this.headerName = headerName;
    this.headerTitle = headerTitle;
    this.categoryNodeRef = categoryNodeRef;
    this.categoryName = categoryName;
    this.categoryTitle = categoryTitle;
  }

  /**
   * Returns the Alfresco node reference of the parent header.
   *
   * @return the header node reference
   */
  public String getHeaderNodeRef() {
    return headerNodeRef;
  }

  /**
   * Sets the Alfresco node reference of the parent header.
   *
   * @param headerNodeRef the header node reference to set
   */
  public void setHeaderNodeRef(String headerNodeRef) {
    this.headerNodeRef = headerNodeRef;
  }

  /**
   * Returns the machine (short) name of the parent header.
   *
   * @return the header name
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * Sets the machine (short) name of the parent header.
   *
   * @param headerName the header name to set
   */
  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  /**
   * Returns the human-readable title of the parent header.
   *
   * @return the header title
   */
  public String getHeaderTitle() {
    return headerTitle;
  }

  /**
   * Sets the human-readable title of the parent header.
   *
   * @param headerTitle the header title to set
   */
  public void setHeaderTitle(String headerTitle) {
    this.headerTitle = headerTitle;
  }

  /**
   * Returns the Alfresco node reference of the category.
   *
   * @return the category node reference
   */
  public String getCategoryNodeRef() {
    return categoryNodeRef;
  }

  /**
   * Sets the Alfresco node reference of the category.
   *
   * @param categoryNodeRef the category node reference to set
   */
  public void setCategoryNodeRef(String categoryNodeRef) {
    this.categoryNodeRef = categoryNodeRef;
  }

  /**
   * Returns the machine (short) name of the category.
   *
   * @return the category name
   */
  public String getCategoryName() {
    return categoryName;
  }

  /**
   * Sets the machine (short) name of the category.
   *
   * @param categoryName the category name to set
   */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  /**
   * Returns the human-readable title of the category.
   *
   * @return the category title
   */
  public String getCategoryTitle() {
    return categoryTitle;
  }

  /**
   * Sets the human-readable title of the category.
   *
   * @param categoryTitle the category title to set
   */
  public void setCategoryTitle(String categoryTitle) {
    this.categoryTitle = categoryTitle;
  }
}
