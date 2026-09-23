package io.swagger.model;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedShares {

  /** The page of {@link Share} items returned to the caller. */
  private List<Share> data;

  /** The total number of items available across all pages, used by the UI for pagination. */
  private long total;

  /**
   * Creates a paged result holding a slice of shares together with the overall item count.
   *
   * @param data the list of {@link Share} items for the current page
   * @param total the total number of items available across all pages
   */
  public PagedShares(List<Share> data, long total) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * @return the data
   */
  public List<Share> getData() {
    return data;
  }

  /**
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
