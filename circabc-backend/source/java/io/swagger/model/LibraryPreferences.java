/**
 *
 */
package io.swagger.model;

/** @author beaurpi */
public class LibraryPreferences {

    private ColumnOptions column = new ColumnOptions();
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
