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
package eu.cec.digit.circabc.repo.translation;

import org.mybatis.spring.SqlSessionTemplate;

import java.util.Date;
import java.util.List;

public class TranslationDaoServiceImpl implements TranslationDaoService {

    private SqlSessionTemplate sqlSessionTemplate = null;

    @Override
    public void saveRequest(Request request) {
        sqlSessionTemplate.insert("Translation.insert_request", request);
    }

    @Override
    public void saveSuccessResponse(
            String requestId, String targetLanguage, String deliveryUrl, String translatedText) {
        Response response = new Response();
        response.setDeliveryURL(deliveryUrl);
        response.setRespDate(new Date());
        response.setTargetLang(targetLanguage);
        response.setTranslated(1);
        response.setTranslatedText(translatedText);

        response.setRequestID(requestId);
        sqlSessionTemplate.insert("Translation.insert_response", response);
    }

    @Override
    public void saveErrorResponse(
            String requestId, String targetLanguage, String errorCode, String errorMessage) {
        Response response = new Response();
        response.setRespDate(new Date());
        response.setTargetLang(targetLanguage);
        response.setErrCode(errorCode);
        response.setErrCode(errorMessage);
        response.setTranslated(0);

        response.setRequestID(requestId);
        sqlSessionTemplate.insert("Translation.insert_response", response);
    }

    @Override
    public void markAsProccesed(String requestId, String targetLanguage) {
        Response response = new Response();
        response.setRequestID(requestId);
        response.setTargetLang(targetLanguage);

        sqlSessionTemplate.update("Translation.updateResponse", response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SearchResult> getTranslationsToProcess(Date from) {
        Search params = new Search();
        params.setFromDate(from);
        return ((List<SearchResult>)
                sqlSessionTemplate.selectList("Translation.selectItemsToProccess", params));
    }

    /**
     * @param sqlSessionTemplate the sqlSessionTemplate to set
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public List<SearchResultNotify> getUserToNotify(Date from) {
        Search params = new Search();
        params.setFromDate(from);
        return ((List<SearchResultNotify>)
                sqlSessionTemplate.selectList("Translation.selectUsersToNotify", params));
    }

    @Override
    public void markAsNotified(String requestId) {
        Request request = new Request();
        request.setRequestID(requestId);
        sqlSessionTemplate.update("Translation.updateRequest", request);
    }

    @Override
    public int getCountOfErrorTranslation(String requestId) {

        return (Integer) sqlSessionTemplate.selectOne("Translation.getEroroCount", requestId);
    }
}
