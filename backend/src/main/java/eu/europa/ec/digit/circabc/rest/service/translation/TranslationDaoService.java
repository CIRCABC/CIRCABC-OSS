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
package eu.europa.ec.digit.circabc.rest.service.translation;

import io.swagger.model.db.Request;
import io.swagger.model.db.SearchResult;
import io.swagger.model.db.SearchResultNotify;
import java.util.Date;
import java.util.List;

/**
 * Data-access abstraction for the document translation workflow.
 *
 * <p>Implementations of this interface persist and query the state of translation requests and
 * their asynchronous responses (typically stored in a relational database). A translation request
 * is submitted for one or more target languages; the external translation service later delivers a
 * successful result or an error for each requested language. This DAO records those outcomes,
 * tracks which translations have been processed and which users still need to be notified, and
 * exposes queries used by the scheduled jobs that drive the workflow.
 */
public interface TranslationDaoService {
  /**
   * Records a successful translation response for a specific request and target language.
   *
   * @param requestId the identifier of the original translation request
   * @param targetLanguage the language into which the content was translated
   * @param deliveryUrl the URL from which the translated content can be retrieved
   * @param translatedText the translated content (or a reference to it)
   */
  void saveSuccessResponse(
    String requestId,
    String targetLanguage,
    String deliveryUrl,
    String translatedText
  );

  /**
   * Records a failed translation response for a specific request and target language.
   *
   * @param requestId the identifier of the original translation request
   * @param targetLanguage the language for which the translation failed
   * @param errorCode the error code returned by the translation service
   * @param errorMessage a human-readable description of the failure
   */
  void saveErrorResponse(
    String requestId,
    String targetLanguage,
    String errorCode,
    String errorMessage
  );

  /**
   * Persists a new translation request.
   *
   * @param request the translation request to store
   */
  void saveRequest(Request request);

  /**
   * Marks the translation of a given request and target language as processed, indicating that its
   * response has been handled by the workflow.
   *
   * @param requestId the identifier of the translation request
   * @param targetLanguage the target language whose translation has been processed
   */
  void markAsProccesed(String requestId, String targetLanguage);

  /**
   * Marks a translation request as notified, indicating that the relevant users have been informed
   * that their translation is available.
   *
   * @param requestId the identifier of the translation request
   */
  void markAsNotified(String requestId);

  /**
   * Returns the number of failed (error) translations recorded for the given request.
   *
   * @param requestId the identifier of the translation request
   * @return the count of error responses associated with the request
   */
  int getCountOfErrorTranslation(String requestId);

  /**
   * Retrieves the translations that still need to be processed since the given point in time.
   *
   * @param from the lower time bound used to select translations to process
   * @return the list of matching translation results awaiting processing
   */
  List<SearchResult> getTranslationsToProcess(Date from);

  /**
   * Retrieves the users who still need to be notified about completed translations since the given
   * point in time.
   *
   * @param from the lower time bound used to select users to notify
   * @return the list of matching users to be notified
   */
  List<SearchResultNotify> getUserToNotify(Date from);
}
