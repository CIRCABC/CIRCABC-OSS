package io.swagger.model;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedNotificationSubscribedUsers {

  /** The page of notification-subscribed users returned for the current request. */
  private List<NotifiableUser> data;

  /** The total number of subscribed users available across all pages, used by the UI for pagination. */
  private long total;

  /**
   * Creates a paged result holding a slice of subscribed users together with the overall total.
   *
   * @param data the list of subscribed users for the current page
   * @param total the total number of subscribed users across all pages
   */
  public PagedNotificationSubscribedUsers(
    List<NotifiableUser> data,
    long total
  ) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * @return the data
   */
  public List<NotifiableUser> getData() {
    return data;
  }

  /**
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
