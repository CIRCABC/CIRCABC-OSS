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
 * Plain data-transfer object representing the outcome of a machine-translation request as persisted
 * in / read from the database.
 *
 * <p>Each instance ties a translation request (identified by {@link #requestID}) to the resulting
 * translated text and the contextual information needed to locate and apply it: the target document
 * and content property, the target language, the destination path and the user who initiated the
 * request.
 */
public class SearchResult {

  /** Identifier of the translation request this result belongs to. */
  private String requestID;

  /** Language code of the requested translation (the target language). */
  private String targetLang;

  /** Repository path where the translated content is (or should be) stored. */
  private String targetPath;

  /** The translated text produced for the request. */
  private String translatedText;

  /** Name of the user who initiated the translation request. */
  private String username;

  /** Identifier of the document being translated. */
  private String documentID;

  /** Qualified name (QName) of the content property that holds the translated value. */
  private String propertyQName;

  /**
   * Returns the identifier of the translation request this result belongs to.
   *
   * @return the request identifier
   */
  public String getRequestID() {
    return requestID;
  }

  /**
   * Sets the identifier of the translation request this result belongs to.
   *
   * @param requestID the request identifier to set
   */
  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }

  /**
   * Returns the target language code of the translation.
   *
   * @return the target language code
   */
  public String getTargetLang() {
    return targetLang;
  }

  /**
   * Sets the target language code of the translation.
   *
   * @param targetLang the target language code to set
   */
  public void setTargetLang(String targetLang) {
    this.targetLang = targetLang;
  }

  /**
   * Returns the translated text produced for the request.
   *
   * @return the translated text
   */
  public String getTranslatedText() {
    return translatedText;
  }

  /**
   * Sets the translated text produced for the request.
   *
   * @param translatedText the translated text to set
   */
  public void setTranslatedText(String translatedText) {
    this.translatedText = translatedText;
  }

  /**
   * Returns the name of the user who initiated the translation request.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the name of the user who initiated the translation request.
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
   * Returns the qualified name (QName) of the content property that holds the translated value.
   *
   * @return the property QName
   */
  public String getPropertyQName() {
    return propertyQName;
  }

  /**
   * Sets the qualified name (QName) of the content property that holds the translated value.
   *
   * @param propertyQName the property QName to set
   */
  public void setPropertyQName(String propertyQName) {
    this.propertyQName = propertyQName;
  }

  /**
   * Returns the repository path where the translated content is (or should be) stored.
   *
   * @return the target path
   */
  public String getTargetPath() {
    return targetPath;
  }

  /**
   * Sets the repository path where the translated content is (or should be) stored.
   *
   * @param targetPath the target path to set
   */
  public void setTargetPath(String targetPath) {
    this.targetPath = targetPath;
  }
}
