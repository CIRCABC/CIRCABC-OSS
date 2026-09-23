package eu.europa.ec.digit.circabc.rest.service.mail;

import java.io.File;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Simple data-transfer object that bundles all the parameters required to send an e-mail through
 * the CIRCABC mail service.
 *
 * <p>Instances are populated by callers (for example REST endpoints or scheduled jobs) and handed
 * to the mail-sending service, which reads the recipients, subject, body and any attachments to
 * build and dispatch the actual message. This class holds state only; it contains no sending logic.
 */
public class MailSendRequest {

  /** Repository node the message relates to (e.g. the content that triggered the mail). */
  private NodeRef content;

  /** Sender address that appears in the message's {@code From} header. */
  private String from;

  /** Primary recipient address. */
  private String to;

  /** Additional recipient addresses (secondary {@code To}/{@code Cc}/{@code Bcc} recipients). */
  private List<String> others;

  /** Address to use for the message's {@code Reply-To} header. */
  private String replyTo;

  /** Subject line of the message. */
  private String subject;

  /** Message body; interpreted as HTML or plain text depending on {@link #html}. */
  private String body;

  /** Whether the {@link #body} should be treated as HTML ({@code true}) or plain text. */
  private boolean html;

  /** Repository nodes to attach to the message. */
  private List<NodeRef> attachments;

  /** Whether the additional recipients should be added as blind carbon copy (BCC). */
  private boolean useBCC;

  /** File-system files to attach to the message, in addition to repository {@link #attachments}. */
  private List<File> fileAttachments;

  /**
   * Returns the repository node the message relates to.
   *
   * @return the related content node, or {@code null} if none is set
   */
  public NodeRef getContent() {
    return content;
  }

  /**
   * Sets the repository node the message relates to.
   *
   * @param content the related content node
   */
  public void setContent(NodeRef content) {
    this.content = content;
  }

  /**
   * Returns the sender address.
   *
   * @return the {@code From} address
   */
  public String getFrom() {
    return from;
  }

  /**
   * Sets the sender address.
   *
   * @param from the {@code From} address
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Returns the primary recipient address.
   *
   * @return the primary recipient address
   */
  public String getTo() {
    return to;
  }

  /**
   * Sets the primary recipient address.
   *
   * @param to the primary recipient address
   */
  public void setTo(String to) {
    this.to = to;
  }

  /**
   * Returns the additional recipient addresses.
   *
   * @return the list of additional recipient addresses
   */
  public List<String> getOthers() {
    return others;
  }

  /**
   * Sets the additional recipient addresses.
   *
   * @param others the list of additional recipient addresses
   */
  public void setOthers(List<String> others) {
    this.others = others;
  }

  /**
   * Returns the reply-to address.
   *
   * @return the {@code Reply-To} address
   */
  public String getReplyTo() {
    return replyTo;
  }

  /**
   * Sets the reply-to address.
   *
   * @param replyTo the {@code Reply-To} address
   */
  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }

  /**
   * Returns the message subject.
   *
   * @return the subject line
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the message subject.
   *
   * @param subject the subject line
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the message body.
   *
   * @return the message body
   */
  public String getBody() {
    return body;
  }

  /**
   * Sets the message body.
   *
   * @param body the message body
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Indicates whether the body is HTML.
   *
   * @return {@code true} if the body is HTML, {@code false} if it is plain text
   */
  public boolean isHtml() {
    return html;
  }

  /**
   * Sets whether the body should be treated as HTML.
   *
   * @param html {@code true} to treat the body as HTML, {@code false} for plain text
   */
  public void setHtml(boolean html) {
    this.html = html;
  }

  /**
   * Returns the repository node attachments.
   *
   * @return the list of repository nodes to attach
   */
  public List<NodeRef> getAttachments() {
    return attachments;
  }

  /**
   * Sets the repository node attachments.
   *
   * @param attachments the list of repository nodes to attach
   */
  public void setAttachments(List<NodeRef> attachments) {
    this.attachments = attachments;
  }

  /**
   * Indicates whether additional recipients are added as blind carbon copy.
   *
   * @return {@code true} to send to additional recipients via BCC
   */
  public boolean isUseBCC() {
    return useBCC;
  }

  /**
   * Sets whether additional recipients should be added as blind carbon copy.
   *
   * @param useBCC {@code true} to send to additional recipients via BCC
   */
  public void setUseBCC(boolean useBCC) {
    this.useBCC = useBCC;
  }

  /**
   * Returns the file-system file attachments.
   *
   * @return the list of files to attach
   */
  public List<File> getFileAttachments() {
    return fileAttachments;
  }

  /**
   * Sets the file-system file attachments.
   *
   * @param fileAttachments the list of files to attach
   */
  public void setFileAttachments(List<File> fileAttachments) {
    this.fileAttachments = fileAttachments;
  }
}
