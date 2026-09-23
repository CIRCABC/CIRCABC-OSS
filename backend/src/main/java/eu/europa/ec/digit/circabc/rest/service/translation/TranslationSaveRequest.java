package eu.europa.ec.digit.circabc.rest.service.translation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Data transfer object that carries all parameters required to request the
 * translation of a document (or a piece of text) and to persist the resulting
 * translation within the CIRCABC repository.
 *
 * <p>Instances of this class group together the source content to translate,
 * the source and target languages, the repository location where the
 * translated content should be stored, and the notification details used to
 * inform the requester once the translation is available.
 */
public class TranslationSaveRequest {

  /** Reference to the repository node associated with the translation request. */
  private NodeRef nodeRef;

  /** Qualified name of the node property targeted by the translation. */
  private QName property;

  /** Identifier or content of the document that must be translated. */
  private String documentToTranslate;

  /** Language code of the original (source) content. */
  private String sourceLanguage;

  /** External reference correlating this request with an external system. */
  private String externalReference;

  /** Username of the user who requested the translation. */
  private String username;

  /** Raw text to translate when a plain text (rather than a document) is submitted. */
  private String textToTranslate;

  /** Repository path where the resulting translation should be saved. */
  private String targetTranslationPath;

  /** Language code of the requested (target) translation. */
  private String targetLanguage;

  /** Flag indicating whether the requester should be notified once the translation completes. */
  private boolean notify;

  /** Email address to which the completion notification should be sent. */
  private String email;

  /**
   * Returns the repository node associated with the translation request.
   *
   * @return the node reference, or {@code null} if not set
   */
  public NodeRef getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the repository node associated with the translation request.
   *
   * @param nodeRef the node reference to associate
   */
  public void setNodeRef(NodeRef nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the qualified name of the node property targeted by the translation.
   *
   * @return the property qualified name, or {@code null} if not set
   */
  public QName getProperty() {
    return property;
  }

  /**
   * Sets the qualified name of the node property targeted by the translation.
   *
   * @param property the property qualified name
   */
  public void setProperty(QName property) {
    this.property = property;
  }

  /**
   * Returns the identifier or content of the document to translate.
   *
   * @return the document to translate, or {@code null} if not set
   */
  public String getDocumentToTranslate() {
    return documentToTranslate;
  }

  /**
   * Sets the identifier or content of the document to translate.
   *
   * @param documentToTranslate the document to translate
   */
  public void setDocumentToTranslate(String documentToTranslate) {
    this.documentToTranslate = documentToTranslate;
  }

  /**
   * Returns the language code of the original (source) content.
   *
   * @return the source language code, or {@code null} if not set
   */
  public String getSourceLanguage() {
    return sourceLanguage;
  }

  /**
   * Sets the language code of the original (source) content.
   *
   * @param sourceLanguage the source language code
   */
  public void setSourceLanguage(String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  /**
   * Returns the external reference correlating this request with an external system.
   *
   * @return the external reference, or {@code null} if not set
   */
  public String getExternalReference() {
    return externalReference;
  }

  /**
   * Sets the external reference correlating this request with an external system.
   *
   * @param externalReference the external reference
   */
  public void setExternalReference(String externalReference) {
    this.externalReference = externalReference;
  }

  /**
   * Returns the username of the user who requested the translation.
   *
   * @return the requester's username, or {@code null} if not set
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the user who requested the translation.
   *
   * @param username the requester's username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the raw text to translate when plain text is submitted.
   *
   * @return the text to translate, or {@code null} if not set
   */
  public String getTextToTranslate() {
    return textToTranslate;
  }

  /**
   * Sets the raw text to translate when plain text is submitted.
   *
   * @param textToTranslate the text to translate
   */
  public void setTextToTranslate(String textToTranslate) {
    this.textToTranslate = textToTranslate;
  }

  /**
   * Returns the repository path where the resulting translation should be saved.
   *
   * @return the target translation path, or {@code null} if not set
   */
  public String getTargetTranslationPath() {
    return targetTranslationPath;
  }

  /**
   * Sets the repository path where the resulting translation should be saved.
   *
   * @param targetTranslationPath the target translation path
   */
  public void setTargetTranslationPath(String targetTranslationPath) {
    this.targetTranslationPath = targetTranslationPath;
  }

  /**
   * Returns the language code of the requested (target) translation.
   *
   * @return the target language code, or {@code null} if not set
   */
  public String getTargetLanguage() {
    return targetLanguage;
  }

  /**
   * Sets the language code of the requested (target) translation.
   *
   * @param targetLanguage the target language code
   */
  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  /**
   * Indicates whether the requester should be notified once the translation completes.
   *
   * @return {@code true} if a notification should be sent, {@code false} otherwise
   */
  public boolean isNotify() {
    return notify;
  }

  /**
   * Sets whether the requester should be notified once the translation completes.
   *
   * @param notify {@code true} to send a notification, {@code false} otherwise
   */
  public void setNotify(boolean notify) {
    this.notify = notify;
  }

  /**
   * Returns the email address to which the completion notification should be sent.
   *
   * @return the notification email address, or {@code null} if not set
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address to which the completion notification should be sent.
   *
   * @param email the notification email address
   */
  public void setEmail(String email) {
    this.email = email;
  }
}
