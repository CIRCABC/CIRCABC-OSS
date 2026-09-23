package io.swagger.model;

/**
 * Data transfer object describing the details of a request associated with an
 * authentication ticket.
 *
 * <p>It captures the essential attributes of a single HTTP request: when it was
 * made, which HTTP verb (method) was used, and the target path. This information
 * is typically used to bind or validate a ticket against a specific request.
 */
public class TicketRequestInfo {

  /** Date/time at which the request was made, as a string representation. */
  private String requestDate;

  /** HTTP verb (method) of the request, e.g. {@code GET}, {@code POST}. */
  private String httpVerb;

  /** Target path (URL path) of the request. */
  private String path;

  /**
   * Returns the date/time at which the request was made.
   *
   * @return the request date as a string, or {@code null} if not set
   */
  public String getRequestDate() {
    return requestDate;
  }

  /**
   * Sets the date/time at which the request was made.
   *
   * @param requestDate the request date as a string
   */
  public void setRequestDate(String requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * Returns the target path of the request.
   *
   * @return the request path, or {@code null} if not set
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the target path of the request.
   *
   * @param path the request path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Returns the HTTP verb (method) of the request.
   *
   * @return the HTTP verb, or {@code null} if not set
   */
  public String getHttpVerb() {
    return httpVerb;
  }

  /**
   * Sets the HTTP verb (method) of the request.
   *
   * @param httpVerb the HTTP verb, e.g. {@code GET} or {@code POST}
   */
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  /**
   * Computes a hash code based on the HTTP verb, path and request date.
   *
   * @return the hash code value for this object
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((httpVerb == null) ? 0 : httpVerb.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result =
      prime * result + ((requestDate == null) ? 0 : requestDate.hashCode());
    return result;
  }

  /**
   * Compares this object with another for equality based on the HTTP verb,
   * path and request date.
   *
   * @param obj the reference object with which to compare
   * @return {@code true} if this object is equal to the {@code obj} argument;
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TicketRequestInfo other = (TicketRequestInfo) obj;
    if (httpVerb == null) {
      if (other.httpVerb != null) return false;
    } else if (!httpVerb.equals(other.httpVerb)) return false;
    if (path == null) {
      if (other.path != null) return false;
    } else if (!path.equals(other.path)) return false;
    if (requestDate == null) {
      if (other.requestDate != null) return false;
    } else if (!requestDate.equals(other.requestDate)) return false;
    return true;
  }
}
