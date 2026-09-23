package io.swagger.model.db;

/**
 * Data access object representing a single log template row.
 *
 * <p>A log template associates an operation {@code method} identifier with a {@code template}
 * string used to render human-readable log messages for that operation. Instances are typically
 * loaded from and persisted to the backing database table used by the logging subsystem.
 */
public class LogTemplateDAO {

  /** Database primary key identifier of the log template; may be {@code null} for unsaved rows. */
  private Integer id;

  /** Identifier of the operation/method the template applies to. */
  private String method;

  /** Message template string used to render log entries for the associated method. */
  private String template;

  /** Creates an empty log template with all fields unset. */
  public LogTemplateDAO() {}

  /**
   * Creates a fully populated log template.
   *
   * @param id the database identifier of the template
   * @param method the operation/method identifier the template applies to
   * @param template the message template string
   */
  public LogTemplateDAO(Integer id, String method, String template) {
    this.id = id;
    this.method = method;
    this.template = template;
  }

  /**
   * Creates a log template without an identifier, typically for a row not yet persisted.
   *
   * @param method the operation/method identifier the template applies to
   * @param template the message template string
   */
  public LogTemplateDAO(String method, String template) {
    this.method = method;
    this.template = template;
  }

  /**
   * Returns the database identifier of this log template.
   *
   * @return the identifier, or {@code null} if the template has not been persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the database identifier of this log template.
   *
   * @param id the identifier to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns the operation/method identifier this template applies to.
   *
   * @return the method identifier
   */
  public String getMethod() {
    return method;
  }

  /**
   * Sets the operation/method identifier this template applies to.
   *
   * @param method the method identifier to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Returns the message template string used to render log entries.
   *
   * @return the template string
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the message template string used to render log entries.
   *
   * @param template the template string to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }
}
