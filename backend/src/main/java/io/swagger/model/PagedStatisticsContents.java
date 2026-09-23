package io.swagger.model;

import java.util.List;

/**
 * Data holder for a paginated slice of statistics content.
 *
 * <p>Wraps the list of {@link ReportFile} items returned for the current page together with the
 * total number of matching items across all pages, allowing the UI to render pagination controls.
 *
 * @author schwerr
 */
public class PagedStatisticsContents {

  /** The statistics report files contained in the current page. */
  private List<ReportFile> data;

  /** The total number of matching items across all pages, used for pagination. */
  private long total;

  /**
   * Creates a paginated statistics contents holder.
   *
   * @param data the report files for the current page
   * @param total the total number of matching items across all pages
   */
  public PagedStatisticsContents(List<ReportFile> data, long total) {
    super();
    this.data = data;
    this.total = total;
  }

  /**
   * @return the data
   */
  public List<ReportFile> getData() {
    return data;
  }

  /**
   * @return the total
   */
  public long getTotal() {
    return total;
  }
}
