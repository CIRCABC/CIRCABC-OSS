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
 * Persistence/data-transfer model representing the response of an asynchronous translation request.
 *
 * <p>An instance captures the outcome reported by the translation service for a previously
 * submitted request: the identifier used to correlate the response with its request, the target
 * language, where the translated content can be retrieved, the translated text itself, any error
 * reported by the service, and status flags indicating whether the translation completed and
 * whether the corresponding document was updated.
 *
 * <p>This is a plain mutable data holder (POJO) with no business logic; it is intended to be
 * populated and read via its getters and setters.
 */
public class Response {

  /** Identifier correlating this response with the originally submitted translation request. */
  private String requestID;

  /** Target language code of the translation (e.g. the language the content was translated into). */
  private String targetLang;

  /** URL from which the translated content (delivery) can be retrieved. */
  private String deliveryURL;

  /** The translated text returned by the translation service. */
  private String translatedText;

  /** Error code reported by the translation service, if the request failed. */
  private String errCode;

  /** Human-readable error message reported by the translation service, if the request failed. */
  private String errMsg;

  /** Date/time at which this response was produced or received. */
  private Date respDate;

  /** Status flag indicating whether the translation has been completed (typically 0/1). */
  private int translated;

  /** Status flag indicating whether the associated document has been updated (typically 0/1). */
  private int documentUpdated;

  /**
   * Returns the identifier correlating this response with the original translation request.
   *
   * @return the request identifier
   */
  public String getRequestID() {
    return requestID;
  }

  /**
   * Sets the identifier correlating this response with the original translation request.
   *
   * @param requestID the request identifier
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
   * @param targetLang the target language code
   */
  public void setTargetLang(String targetLang) {
    this.targetLang = targetLang;
  }

  /**
   * Returns the URL from which the translated content can be retrieved.
   *
   * @return the delivery URL
   */
  public String getDeliveryURL() {
    return deliveryURL;
  }

  /**
   * Sets the URL from which the translated content can be retrieved.
   *
   * @param deliveryURL the delivery URL
   */
  public void setDeliveryURL(String deliveryURL) {
    this.deliveryURL = deliveryURL;
  }

  /**
   * Returns the translated text returned by the translation service.
   *
   * @return the translated text
   */
  public String getTranslatedText() {
    return translatedText;
  }

  /**
   * Sets the translated text returned by the translation service.
   *
   * @param translatedText the translated text
   */
  public void setTranslatedText(String translatedText) {
    this.translatedText = translatedText;
  }

  /**
   * Returns the error code reported by the translation service, if any.
   *
   * @return the error code, or {@code null} if the request succeeded
   */
  public String getErrCode() {
    return errCode;
  }

  /**
   * Sets the error code reported by the translation service.
   *
   * @param errCode the error code
   */
  public void setErrCode(String errCode) {
    this.errCode = errCode;
  }

  /**
   * Returns the human-readable error message reported by the translation service, if any.
   *
   * @return the error message, or {@code null} if the request succeeded
   */
  public String getErrMsg() {
    return errMsg;
  }

  /**
   * Sets the human-readable error message reported by the translation service.
   *
   * @param errMsg the error message
   */
  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  /**
   * Returns the date/time at which this response was produced or received.
   *
   * @return the response date
   */
  public Date getRespDate() {
    return respDate;
  }

  /**
   * Sets the date/time at which this response was produced or received.
   *
   * @param respDate the response date
   */
  public void setRespDate(Date respDate) {
    this.respDate = respDate;
  }

  /**
   * Returns the flag indicating whether the translation has been completed.
   *
   * @return the translated status flag (typically 0 or 1)
   */
  public int getTranslated() {
    return translated;
  }

  /**
   * Sets the flag indicating whether the translation has been completed.
   *
   * @param translated the translated status flag (typically 0 or 1)
   */
  public void setTranslated(int translated) {
    this.translated = translated;
  }

  /**
   * Returns the flag indicating whether the associated document has been updated.
   *
   * @return the document-updated status flag (typically 0 or 1)
   */
  public int getDocumentUpdated() {
    return documentUpdated;
  }

  /**
   * Sets the flag indicating whether the associated document has been updated.
   *
   * @param documentUpdated the document-updated status flag (typically 0 or 1)
   */
  public void setDocumentUpdated(int documentUpdated) {
    this.documentUpdated = documentUpdated;
  }
}
