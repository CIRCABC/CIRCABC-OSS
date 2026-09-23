/**
 *
 */
package io.swagger.model;

/**
 * Holds the pagination and sorting parameters used when listing collections of resources through
 * the REST API.
 *
 * <p>Instances of this class capture the requested page number, the maximum number of items to
 * return per page and the sort criteria, allowing callers to page through and order large result
 * sets. Sensible defaults are provided (first page, ten items per page, most recently modified
 * first).
 *
 * @author beaurpi
 */
public class ListingOptions {

  /** The one-based index of the page to return. Defaults to the first page. */
  private Integer page = 1;

  /** The maximum number of items to return per page. Defaults to ten. */
  private Integer limit = 10;

  /**
   * The sort criteria expressed as a {@code <field>_<direction>} token (e.g. {@code
   * modified_DESC}). Defaults to sorting by modification date in descending order.
   */
  private String sort = "modified_DESC";

  /** @return the page */
  public Integer getPage() {
    return page;
  }

  /** @param page the page to set */
  public void setPage(Integer page) {
    this.page = page;
  }

  /** @return the limit */
  public Integer getLimit() {
    return limit;
  }

  /** @param limit the limit to set */
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /** @return the sort */
  public String getSort() {
    return sort;
  }

  /** @param sort the sort to set */
  public void setSort(String sort) {
    this.sort = sort;
  }
}
