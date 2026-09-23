package io.swagger.model;

/**
 * Domain model representing the definition of a mail (notification) template.
 *
 * <p>A mail template describes the content used when CIRCABC sends
 * notification e-mails. It bundles the template's identifier, a
 * human-readable name and the actual message content split into a subject
 * line and a body text. Instances are typically produced by the service
 * layer and serialized to JSON by the REST API.
 */
public class MailTemplateDefinition {

  /** Unique identifier of the mail template. */
  private String id;

  /** Human-readable name of the mail template. */
  private String name;

  /** Subject line used for e-mails generated from this template. */
  private String subject;

  /** Body content of the e-mail generated from this template. */
  private String text;

  /**
   * Creates a fully populated mail template definition.
   *
   * @param id the unique identifier of the template
   * @param name the human-readable name of the template
   * @param subject the subject line used for generated e-mails
   * @param text the body content used for generated e-mails
   */
  public MailTemplateDefinition(
    String id,
    String name,
    String subject,
    String text
  ) {
    super();
    this.id = id;
    this.name = name;
    this.subject = subject;
    this.text = text;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }
}
