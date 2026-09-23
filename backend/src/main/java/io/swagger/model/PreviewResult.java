package io.swagger.model;

/**
 * Data model describing the outcome of a content preview (rendition) request.
 *
 * <p>A preview is typically generated asynchronously by the repository. This
 * model conveys whether the preview is ready to be served, together with a
 * human-readable message, a message code for client-side handling, and the
 * size of the available preview content.
 */
public class PreviewResult {

  /** Whether the requested preview is ready to be served. */
  private boolean ready;

  /** Human-readable status message describing the preview state. */
  private String message;

  /** Machine-readable code allowing clients to interpret the status. */
  private String messageCode;

  /** Size, in bytes, of the available preview content. */
  private long contentLength;

  /**
   * Creates a fully populated preview result.
   *
   * @param ready whether the preview is ready to be served
   * @param message human-readable status message
   * @param messageCode machine-readable status code
   * @param contentLength size, in bytes, of the preview content
   */
  public PreviewResult(
    boolean ready,
    String message,
    String messageCode,
    long contentLength
  ) {
    super();
    this.ready = ready;
    this.message = message;
    this.messageCode = messageCode;
    this.contentLength = contentLength;
  }

  /**
   * @return the ready
   */
  public boolean isReady() {
    return ready;
  }

  /**
   * @param ready the ready to set
   */
  public void setReady(boolean ready) {
    this.ready = ready;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the messageCode
   */
  public String getMessageCode() {
    return messageCode;
  }

  /**
   * @param messageCode the messageCode to set
   */
  public void setMessageCode(String messageCode) {
    this.messageCode = messageCode;
  }

  /**
   * @return the contentLength
   */
  public long getContentLength() {
    return contentLength;
  }

  /**
   * @param contentLength the contentLength to set
   */
  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }
}
