package io.swagger.model;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedAutoUploadConfiguration {

  /** The current page of auto-upload configuration items. */
  private List<Configuration> data;

  /** The total number of configuration items available across all pages. */
  private long total;

  /**
   * Creates a paged auto-upload configuration result.
   *
   * @param data the configuration items contained in the current page
   * @param total the total number of configuration items available across all pages
   */
  public PagedAutoUploadConfiguration(List<Configuration> data, long total) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * Returns the configuration items contained in the current page.
   *
   * @return the data
   */
  public List<Configuration> getData() {
    return data;
  }

  /**
   * Returns the total number of configuration items available across all pages.
   *
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
