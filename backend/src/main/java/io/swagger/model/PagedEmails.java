package io.swagger.model;

import io.swagger.model.db.DistributionEmailDAO;
import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author beaurpi
 */
public class PagedEmails {

  /** The page of distribution email records returned for the current request. */
  private List<DistributionEmailDAO> data;

  /** The total number of matching email records across all pages, used for UI pagination. */
  private long total;

  /**
   * Returns the current page of distribution email records.
   *
   * @return the list of {@link DistributionEmailDAO} items in this page
   */
  public List<DistributionEmailDAO> getData() {
    return data;
  }

  /**
   * Sets the current page of distribution email records.
   *
   * @param data the list of {@link DistributionEmailDAO} items to store
   */
  public void setData(List<DistributionEmailDAO> data) {
    this.data = data;
  }

  /**
   * Returns the total number of matching email records across all pages.
   *
   * @return the total count of items available for pagination
   */
  public long getTotal() {
    return total;
  }

  /**
   * Sets the total number of matching email records across all pages.
   *
   * @param total the total count of items available for pagination
   */
  public void setTotal(long total) {
    this.total = total;
  }
}
