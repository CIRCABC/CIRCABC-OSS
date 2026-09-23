/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A paginated container for {@link UserRevocationRequest} items.
 *
 * <p>This model bundles a single page of user revocation requests together with the total number of
 * requests available across all pages, allowing API consumers to render paginated results.
 *
 * @author beaurpi
 */
public class PagedUserRevocationRequest {

  /** The user revocation requests contained in the current page. Never {@code null}. */
  private List<UserRevocationRequest> data = new ArrayList<>();

  /** The total number of user revocation requests available across all pages. */
  private Integer total;

  /** @return the data */
  public List<UserRevocationRequest> getData() {
    return data;
  }

  /** @param data the data to set */
  public void setData(List<UserRevocationRequest> data) {
    this.data = data;
  }

  /** @return the total */
  public Integer getTotal() {
    return total;
  }

  /** @param total the total to set */
  public void setTotal(Integer total) {
    this.total = total;
  }
}
