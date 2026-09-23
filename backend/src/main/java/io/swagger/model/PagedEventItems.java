package io.swagger.model;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedEventItems {

  /** The page of event items returned to the caller. */
  private List<EventItem> data;

  /** The total number of matching event items across all pages, used for UI pagination. */
  private long total;

  /**
   * Creates a paginated result of event items.
   *
   * @param data the event items contained in the current page
   * @param total the total number of event items available across all pages
   */
  public PagedEventItems(List<EventItem> data, long total) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * @return the data
   */
  public List<EventItem> getData() {
    return data;
  }

  /**
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
