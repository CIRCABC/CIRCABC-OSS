package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.db.Request;
import io.swagger.model.db.Response;
import io.swagger.model.db.Search;
import io.swagger.model.db.SearchResult;
import io.swagger.model.db.SearchResultNotify;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class TranslationDaoServiceImplTest {

  private TranslationDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    service = new TranslationDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    setField("sqlSessionTemplate", sqlSessionTemplate);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TranslationDaoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testSaveRequest_whenValidRequest_thenInsertsWithCorrectStatement() {
    Request request = new Request();
    request.setRequestID("req-123");

    service.saveRequest(request);

    verify(sqlSessionTemplate).insert("Translation.insert_request", request);
  }

  @Test
  public void testSaveSuccessResponse_whenCalled_thenInsertsResponseWithTranslatedFlag() {
    service.saveSuccessResponse(
      "req-1",
      "FR",
      "http://delivery.url",
      "Translated text"
    );

    verify(sqlSessionTemplate).insert(
      eq("Translation.insert_response"),
      argThat(arg -> {
        Response r = (Response) arg;
        return (
          "req-1".equals(r.getRequestID()) &&
          "FR".equals(r.getTargetLang()) &&
          "http://delivery.url".equals(r.getDeliveryURL()) &&
          "Translated text".equals(r.getTranslatedText()) &&
          r.getTranslated() == 1 &&
          r.getRespDate() != null
        );
      })
    );
  }

  @Test
  public void testSaveErrorResponse_whenCalled_thenInsertsResponseWithErrorInfo() {
    service.saveErrorResponse("req-2", "DE", "ERR_01", "Something failed");

    verify(sqlSessionTemplate).insert(
      eq("Translation.insert_response"),
      argThat(arg -> {
        Response r = (Response) arg;
        return (
          "req-2".equals(r.getRequestID()) &&
          "DE".equals(r.getTargetLang()) &&
          r.getTranslated() == 0 &&
          r.getRespDate() != null
        );
      })
    );
  }

  @Test
  public void testMarkAsProccesed_whenCalled_thenUpdatesWithCorrectParams() {
    service.markAsProccesed("req-3", "ES");

    verify(sqlSessionTemplate).update(
      eq("Translation.updateResponse"),
      argThat(arg -> {
        Response r = (Response) arg;
        return (
          "req-3".equals(r.getRequestID()) && "ES".equals(r.getTargetLang())
        );
      })
    );
  }

  @Test
  public void testGetTranslationsToProcess_whenCalled_thenReturnsResults() {
    Date from = new Date();
    List<SearchResult> expected = Arrays.asList(new SearchResult());
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("Translation.selectItemsToProccess"), any());

    List<SearchResult> result = service.getTranslationsToProcess(from);

    assertEquals(expected, result);
  }

  @Test
  public void testGetUserToNotify_whenCalled_thenReturnsNotifyResults() {
    Date from = new Date();
    List<SearchResultNotify> expected = Arrays.asList(new SearchResultNotify());
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("Translation.selectUsersToNotify"), any());

    List<SearchResultNotify> result = service.getUserToNotify(from);

    assertEquals(expected, result);
  }

  @Test
  public void testMarkAsNotified_whenCalled_thenUpdatesRequest() {
    service.markAsNotified("req-4");

    verify(sqlSessionTemplate).update(
      eq("Translation.updateRequest"),
      argThat(arg -> {
        Request r = (Request) arg;
        return "req-4".equals(r.getRequestID());
      })
    );
  }

  @Test
  public void testGetCountOfErrorTranslation_whenCalled_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne("Translation.getEroroCount", "req-5")
    ).thenReturn(3);

    int count = service.getCountOfErrorTranslation("req-5");

    assertEquals(3, count);
  }

  @Test
  public void testGetCountOfErrorTranslation_whenZeroErrors_thenReturnsZero() {
    when(
      sqlSessionTemplate.selectOne("Translation.getEroroCount", "req-6")
    ).thenReturn(0);

    int count = service.getCountOfErrorTranslation("req-6");

    assertEquals(0, count);
  }
}
