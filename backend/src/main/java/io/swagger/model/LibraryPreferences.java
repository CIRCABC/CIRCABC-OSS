/**
 *
 */
package io.swagger.model;

/**
 * Represents a user's display preferences for the Library service of an Interest Group.
 *
 * <p>This model aggregates the two configurable aspects of the Library view: the column layout
 * (which columns are shown and how) and the listing behavior (how items are listed/paginated). It
 * is used as a data-transfer object exchanged through the REST API.
 *
 * @author beaurpi
 */
public class LibraryPreferences {

  /** Preferences describing the columns displayed in the Library view. */
  private ColumnOptions column = new ColumnOptions();

  /** Preferences describing how Library items are listed. */
  private ListingOptions listing = new ListingOptions();

  /** @return the column */
  public ColumnOptions getColumn() {
    return column;
  }

  /** @param column the column to set */
  public void setColumn(ColumnOptions column) {
    this.column = column;
  }

  /** @return the listing */
  public ListingOptions getListing() {
    return listing;
  }

  /** @param listing the listing to set */
  public void setListing(ListingOptions listing) {
    this.listing = listing;
  }
}
