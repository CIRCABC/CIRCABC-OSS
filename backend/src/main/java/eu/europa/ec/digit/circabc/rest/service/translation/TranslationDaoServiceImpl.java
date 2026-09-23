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
import io.swagger.model.db.Response;
import io.swagger.model.db.Search;
import io.swagger.model.db.SearchResult;
import io.swagger.model.db.SearchResultNotify;
import java.util.Date;
import java.util.List;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link TranslationDaoService}.
 *
 * <p>Persists and queries the state of the document translation workflow through a configured
 * MyBatis {@link SqlSessionTemplate}. Each operation delegates to a mapped statement in the
 * {@code Translation} namespace (for example {@code Translation.insert_request} or
 * {@code Translation.selectItemsToProccess}) to read from or write to the underlying relational
 * store.
 *
 * <p>This bean is wired via Spring; the {@link SqlSessionTemplate} must be injected through
 * {@link #setSqlSessionTemplate(SqlSessionTemplate)} before any operation is invoked.
 */
public class TranslationDaoServiceImpl implements TranslationDaoService {

  /**
   * MyBatis session template used to execute the mapped statements of the {@code Translation}
   * namespace. Injected via {@link #setSqlSessionTemplate(SqlSessionTemplate)}.
   */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Persists a new translation request by executing the {@code Translation.insert_request} mapped
   * statement.
   *
   * @param request the translation request to store
   */
  @Override
  public void saveRequest(Request request) {
    sqlSessionTemplate.insert("Translation.insert_request", request);
  }

  /**
   * Records a successful translation response by building a {@link Response} (with the delivery URL,
   * response date, target language, translated text and a {@code translated} flag set to {@code 1})
   * and inserting it through the {@code Translation.insert_response} mapped statement.
   *
   * @param requestId the identifier of the original translation request
   * @param targetLanguage the language into which the content was translated
   * @param deliveryUrl the URL from which the translated content can be retrieved
   * @param translatedText the translated content (or a reference to it)
   */
  @Override
  public void saveSuccessResponse(
    String requestId,
    String targetLanguage,
    String deliveryUrl,
    String translatedText
  ) {
    Response response = new Response();
    response.setDeliveryURL(deliveryUrl);
    response.setRespDate(new Date());
    response.setTargetLang(targetLanguage);
    response.setTranslated(1);
    response.setTranslatedText(translatedText);

    response.setRequestID(requestId);
    sqlSessionTemplate.insert("Translation.insert_response", response);
  }

  /**
   * Records a failed translation response by building a {@link Response} (with the response date,
   * target language, error information and a {@code translated} flag set to {@code 0}) and
   * inserting it through the {@code Translation.insert_response} mapped statement.
   *
   * @param requestId the identifier of the original translation request
   * @param targetLanguage the language for which the translation failed
   * @param errorCode the error code returned by the translation service
   * @param errorMessage a human-readable description of the failure
   */
  @Override
  public void saveErrorResponse(
    String requestId,
    String targetLanguage,
    String errorCode,
    String errorMessage
  ) {
    Response response = new Response();
    response.setRespDate(new Date());
    response.setTargetLang(targetLanguage);
    response.setErrCode(errorCode);
    response.setErrCode(errorMessage);
    response.setTranslated(0);

    response.setRequestID(requestId);
    sqlSessionTemplate.insert("Translation.insert_response", response);
  }

  /**
   * Marks the translation of a given request and target language as processed by executing the
   * {@code Translation.updateResponse} mapped statement.
   *
   * @param requestId the identifier of the translation request
   * @param targetLanguage the target language whose translation has been processed
   */
  @Override
  public void markAsProccesed(String requestId, String targetLanguage) {
    Response response = new Response();
    response.setRequestID(requestId);
    response.setTargetLang(targetLanguage);

    sqlSessionTemplate.update("Translation.updateResponse", response);
  }

  /**
   * Retrieves the translations that still need to be processed since the given point in time by
   * executing the {@code Translation.selectItemsToProccess} mapped statement.
   *
   * @param from the lower time bound used to select translations to process
   * @return the list of matching translation results awaiting processing
   */
  @Override
  public List<SearchResult> getTranslationsToProcess(Date from) {
    Search params = new Search();
    params.setFromDate(from);
    return sqlSessionTemplate.selectList(
      "Translation.selectItemsToProccess",
      params
    );
  }

  /**
   * Injects the MyBatis session template used to execute the mapped statements.
   *
   * @param sqlSessionTemplate the sqlSessionTemplate to set
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Retrieves the users who still need to be notified about completed translations since the given
   * point in time by executing the {@code Translation.selectUsersToNotify} mapped statement.
   *
   * @param from the lower time bound used to select users to notify
   * @return the list of matching users to be notified
   */
  @Override
  public List<SearchResultNotify> getUserToNotify(Date from) {
    Search params = new Search();
    params.setFromDate(from);
    return sqlSessionTemplate.selectList(
      "Translation.selectUsersToNotify",
      params
    );
  }

  /**
   * Marks a translation request as notified by executing the {@code Translation.updateRequest}
   * mapped statement.
   *
   * @param requestId the identifier of the translation request
   */
  @Override
  public void markAsNotified(String requestId) {
    Request request = new Request();
    request.setRequestID(requestId);
    sqlSessionTemplate.update("Translation.updateRequest", request);
  }

  /**
   * Returns the number of failed (error) translations recorded for the given request by executing
   * the {@code Translation.getEroroCount} mapped statement.
   *
   * @param requestId the identifier of the translation request
   * @return the count of error responses associated with the request
   */
  @Override
  public int getCountOfErrorTranslation(String requestId) {
    return (Integer) sqlSessionTemplate.selectOne(
      "Translation.getEroroCount",
      requestId
    );
  }
}
