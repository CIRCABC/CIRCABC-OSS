/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data model representing a translation request persisted/exchanged by CIRCABC.
 *
 * <p>An instance captures all the information needed to translate either a document referenced in
 * the Alfresco repository or a raw piece of text: the source and target languages, the location of
 * the content to translate, the requester and the date of the request, together with optional
 * notification preferences. It is a plain data holder (POJO) with no business logic; instances are
 * populated and read through the standard getters and setters below.
 */
public class Request {

  /** Language code of the content to be translated (source language). */
  private String sourceLang;

  /** Target language code(s) the content should be translated into, e.g. a comma-separated list. */
  private String targetLangs;

  /** URL pointing to the document to translate. */
  private String docURL;

  /** Repository path where the translated result should be stored. */
  private String targetPath;

  /** Raw text to translate, used when the request is for plain text rather than a document. */
  private String text;

  /** Identifier of the user who issued the translation request. */
  private String username;

  /** Date on which the translation request was created. */
  private Date reqDate;

  /** Identifier of the document (repository node) associated with the request. */
  private String documentID;

  /** Qualified name of the property that holds or should receive the translated content. */
  private String propertyQName;

  /** Unique identifier of this translation request. */
  private String requestID;

  /** Whether the requester should be notified once the translation is completed. */
  private boolean notify;

  /** Email address used to notify the requester about the request outcome. */
  private String email;

  /**
   * Returns the unique identifier of this translation request.
   *
   * @return the request identifier
   */
  public String getRequestID() {
    return requestID;
  }

  /**
   * Sets the unique identifier of this translation request.
   *
   * @param requestID the request identifier to set
   */
  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }

  /**
   * Returns the source language code of the content to translate.
   *
   * @return the source language code
   */
  public String getSourceLang() {
    return sourceLang;
  }

  /**
   * Sets the source language code of the content to translate.
   *
   * @param sourceLang the source language code to set
   */
  public void setSourceLang(String sourceLang) {
    this.sourceLang = sourceLang;
  }

  /**
   * Returns the target language code(s) for the translation.
   *
   * @return the target language code(s)
   */
  public String getTargetLangs() {
    return targetLangs;
  }

  /**
   * Sets the target language code(s) for the translation.
   *
   * @param targetLangs the target language code(s) to set
   */
  public void setTargetLangs(String targetLangs) {
    this.targetLangs = targetLangs;
  }

  /**
   * Returns the URL of the document to translate.
   *
   * @return the document URL
   */
  public String getDocURL() {
    return docURL;
  }

  /**
   * Sets the URL of the document to translate.
   *
   * @param docURL the document URL to set
   */
  public void setDocURL(String docURL) {
    this.docURL = docURL;
  }

  /**
   * Returns the repository path where the translated result should be stored.
   *
   * @return the target path
   */
  public String getTargetPath() {
    return targetPath;
  }

  /**
   * Sets the repository path where the translated result should be stored.
   *
   * @param targetPath the target path to set
   */
  public void setTargetPath(String targetPath) {
    this.targetPath = targetPath;
  }

  /**
   * Returns the raw text to translate.
   *
   * @return the text to translate
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the raw text to translate.
   *
   * @param text the text to translate to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Returns the identifier of the user who issued the request.
   *
   * @return the requester's username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the identifier of the user who issued the request.
   *
   * @param username the requester's username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the date on which the request was created.
   *
   * @return the request date
   */
  public Date getReqDate() {
    return reqDate;
  }

  /**
   * Sets the date on which the request was created.
   *
   * @param reqDate the request date to set
   */
  public void setReqDate(Date reqDate) {
    this.reqDate = reqDate;
  }

  /**
   * Returns the identifier of the document associated with the request.
   *
   * @return the document identifier
   */
  public String getDocumentID() {
    return documentID;
  }

  /**
   * Sets the identifier of the document associated with the request.
   *
   * @param documentID the document identifier to set
   */
  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  /**
   * Returns the qualified name of the property holding the translated content.
   *
   * @return the property qualified name
   */
  public String getPropertyQName() {
    return propertyQName;
  }

  /**
   * Sets the qualified name of the property holding the translated content.
   *
   * @param propertyQName the property qualified name to set
   */
  public void setPropertyQName(String propertyQName) {
    this.propertyQName = propertyQName;
  }

  /**
   * Indicates whether the requester should be notified when the translation completes.
   *
   * @return {@code true} if the requester should be notified, {@code false} otherwise
   */
  public boolean isNotify() {
    return notify;
  }

  /**
   * Sets whether the requester should be notified when the translation completes.
   *
   * @param notify {@code true} to notify the requester, {@code false} otherwise
   */
  public void setNotify(boolean notify) {
    this.notify = notify;
  }

  /**
   * Returns the email address used to notify the requester.
   *
   * @return the notification email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address used to notify the requester.
   *
   * @param email the notification email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }
}
