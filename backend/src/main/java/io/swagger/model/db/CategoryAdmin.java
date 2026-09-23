package io.swagger.model.db;

/**
 * Database-level model representing the association between a user and a category for which that
 * user holds administrative rights (a "category admin").
 *
 * <p>Each instance links a user to a single category and carries the fixed permission group name
 * ({@code "CircaCategoryAdmin"}) used to identify category administrators within CIRCABC.
 */
public class CategoryAdmin {

  /** Identifier of the user who is a category administrator. */
  long userID;

  /** Identifier of the category the user administers. */
  long categoryID;

  /**
   * Name of the permission/role group associated with this admin link. Defaults to
   * {@code "CircaCategoryAdmin"}.
   */
  String name;

  /**
   * Creates a category admin link between the given user and category, initializing the role name
   * to {@code "CircaCategoryAdmin"}.
   *
   * @param userID the identifier of the administering user
   * @param categoryID the identifier of the administered category
   */
  public CategoryAdmin(long userID, long categoryID) {
    super();
    this.userID = userID;
    this.categoryID = categoryID;
    this.name = "CircaCategoryAdmin";
  }

  /**
   * Returns the identifier of the administering user.
   *
   * @return the user identifier
   */
  public long getUserID() {
    return userID;
  }

  /**
   * Sets the identifier of the administering user.
   *
   * @param userID the user identifier to set
   */
  public void setUserID(long userID) {
    this.userID = userID;
  }

  /**
   * Returns the identifier of the administered category.
   *
   * @return the category identifier
   */
  public long getCategoryID() {
    return categoryID;
  }

  /**
   * Sets the identifier of the administered category.
   *
   * @param categoryID the category identifier to set
   */
  public void setCategoryID(long categoryID) {
    this.categoryID = categoryID;
  }

  /**
   * Returns the name of the permission/role group associated with this admin link.
   *
   * @return the role group name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the permission/role group associated with this admin link.
   *
   * @param name the role group name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns a string representation of this category admin link, including the user id, category id
   * and role name.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "CategoryAdmin [userID=" +
      userID +
      ", categoryID=" +
      categoryID +
      ", name=" +
      name +
      "]"
    );
  }
}
