package io.swagger.model;

import java.util.List;

/**
 * Data holder representing a single page of notification configurations.
 *
 * <p>It bundles the list of {@link NotificationWrapper} items retrieved for the
 * current page together with the total number of items available across all
 * pages. The total count allows the UI to render pagination controls without
 * having to fetch the complete result set.
 *
 * @author schwerr
 */
public class PagedNotificationConfigurations {

  /** The notification configuration items contained in the current page. */
  private List<NotificationWrapper> data;

  /** The total number of notification configuration items across all pages. */
  private long total;

  /**
   * Creates a page of notification configurations.
   *
   * @param data the notification configuration items for the current page
   * @param total the total number of items available across all pages
   */
  public PagedNotificationConfigurations(
    List<NotificationWrapper> data,
    long total
  ) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * Returns the notification configuration items for the current page.
   *
   * @return the data
   */
  public List<NotificationWrapper> getData() {
    return data;
  }

  /**
   * Returns the total number of items available across all pages.
   *
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
