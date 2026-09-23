package eu.europa.ec.digit.circabc.rest.service.log;

import java.util.Date;

/**
 * Immutable value object holding the filter criteria used to search activity/audit
 * log entries for an Interest Group.
 *
 * <p>Instances bundle together the parameters that narrow a log query (Interest
 * Group, user, service, method and date range) as well as the pagination window
 * ({@link #startRecord} and {@link #pageSize}). All fields are mandatory and set
 * once at construction time, making this class thread-safe.
 */
public class LogSearchCriteria {

  /** Identifier of the Interest Group whose logs are being searched. */
  private final long igId;

  /** Username to filter log entries by (the actor that performed the action). */
  private final String user;

  /** Service name (e.g. Library, Newsgroup, Events) to filter log entries by. */
  private final String service;

  /** Action/method name to filter log entries by. */
  private final String method;

  /** Lower bound (inclusive) of the date range for which logs are retrieved. */
  private final Date fromDate;

  /** Upper bound (inclusive) of the date range for which logs are retrieved. */
  private final Date toDate;

  /** Zero-based index of the first record to return, used for pagination. */
  private final int startRecord;

  /** Maximum number of records to return in a single page. */
  private final int pageSize;

  /**
   * Creates a fully-populated set of log search criteria.
   *
   * @param igId the Interest Group identifier whose logs are searched
   * @param user the username to filter by
   * @param service the service name to filter by
   * @param method the action/method name to filter by
   * @param fromDate the inclusive start of the date range
   * @param toDate the inclusive end of the date range
   * @param startRecord the zero-based index of the first record to return
   * @param pageSize the maximum number of records to return
   */
  // NOSONAR: Constructor has 8 parameters which exceeds the recommended 7.
  // This is a data transfer object where all fields are required for log search.
  // Using a builder pattern would add unnecessary complexity for this simple DTO.
  public LogSearchCriteria( // NOSONAR
    long igId,
    String user,
    String service,
    String method,
    Date fromDate,
    Date toDate,
    int startRecord,
    int pageSize
  ) {
    this.igId = igId;
    this.user = user;
    this.service = service;
    this.method = method;
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.startRecord = startRecord;
    this.pageSize = pageSize;
  }

  /**
   * @return the Interest Group identifier whose logs are searched
   */
  public long getIgId() {
    return igId;
  }

  /**
   * @return the username used to filter log entries
   */
  public String getUser() {
    return user;
  }

  /**
   * @return the service name used to filter log entries
   */
  public String getService() {
    return service;
  }

  /**
   * @return the action/method name used to filter log entries
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return the inclusive start of the date range
   */
  public Date getFromDate() {
    return fromDate;
  }

  /**
   * @return the inclusive end of the date range
   */
  public Date getToDate() {
    return toDate;
  }

  /**
   * @return the zero-based index of the first record to return
   */
  public int getStartRecord() {
    return startRecord;
  }

  /**
   * @return the maximum number of records to return per page
   */
  public int getPageSize() {
    return pageSize;
  }
}
