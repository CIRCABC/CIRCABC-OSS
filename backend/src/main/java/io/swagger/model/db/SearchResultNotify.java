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

/**
 * Plain data holder describing a single translation-related search result that must trigger a
 * notification.
 *
 * <p>Instances of this class carry the information required to notify a user about the outcome of a
 * translation request for a specific document: which request it belongs to, the requesting user's
 * identity and email, the source document and property involved, the requested target languages and
 * path, and how many translations are concerned.
 */
public class SearchResultNotify {

  /** Identifier of the originating translation request. */
  private String requestID;

  /** Requested target languages for the translation (typically a comma-separated list). */
  private String targetLangs;

  /** Repository path where the translated content should be stored. */
  private String targetPath;

  /** Username of the user to be notified. */
  private String username;

  /** Email address of the user to be notified. */
  private String email;

  /** Identifier of the document being translated. */
  private String documentID;

  /** Qualified name of the document property associated with the translation. */
  private String propertyQName;

  /** Number of translations concerned by this result. */
  private int translationCount;

  /**
   * Returns the identifier of the originating translation request.
   *
   * @return the request identifier
   */
  public String getRequestID() {
    return requestID;
  }

  /**
   * Sets the identifier of the originating translation request.
   *
   * @param requestID the request identifier to set
   */
  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }

  /**
   * Returns the username of the user to be notified.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the user to be notified.
   *
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the identifier of the document being translated.
   *
   * @return the document identifier
   */
  public String getDocumentID() {
    return documentID;
  }

  /**
   * Sets the identifier of the document being translated.
   *
   * @param documentID the document identifier to set
   */
  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  /**
   * Returns the qualified name of the document property associated with the translation.
   *
   * @return the property qualified name
   */
  public String getPropertyQName() {
    return propertyQName;
  }

  /**
   * Sets the qualified name of the document property associated with the translation.
   *
   * @param propertyQName the property qualified name to set
   */
  public void setPropertyQName(String propertyQName) {
    this.propertyQName = propertyQName;
  }

  /**
   * Returns the repository path where the translated content should be stored.
   *
   * @return the target path
   */
  public String getTargetPath() {
    return targetPath;
  }

  /**
   * Sets the repository path where the translated content should be stored.
   *
   * @param targetPath the target path to set
   */
  public void setTargetPath(String targetPath) {
    this.targetPath = targetPath;
  }

  /**
   * Returns the requested target languages for the translation.
   *
   * @return the target languages
   */
  public String getTargetLangs() {
    return targetLangs;
  }

  /**
   * Sets the requested target languages for the translation.
   *
   * @param targetLangs the target languages to set
   */
  public void setTargetLangs(String targetLangs) {
    this.targetLangs = targetLangs;
  }

  /**
   * Returns the email address of the user to be notified.
   *
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address of the user to be notified.
   *
   * @param email the email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the number of translations concerned by this result.
   *
   * @return the translation count
   */
  public int getTranslationCount() {
    return translationCount;
  }

  /**
   * Sets the number of translations concerned by this result.
   *
   * @param translationCount the translation count to set
   */
  public void setTranslationCount(int translationCount) {
    this.translationCount = translationCount;
  }
}
