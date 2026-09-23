package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class MachineTranslationServiceImplTest {

  private MachineTranslationServiceImpl service;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    service = new MachineTranslationServiceImpl();
    circabcConfig = mock(CircabcConfig.class);
    setField("circabcConfig", circabcConfig);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MachineTranslationServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

  private Object getField(String fieldName) throws Exception {
    Field field = MachineTranslationServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    return field.get(service);
  }

  @Test
  public void testInit_whenConfigProvided_thenFieldsPopulated()
    throws Exception {
    when(circabcConfig.getMtServiceUrl()).thenReturn("http://mt.example.com");
    when(circabcConfig.getMtRESTUsername()).thenReturn("user1");
    when(circabcConfig.getMtRESTPassword()).thenReturn("pass1");

    service.init();

    assertEquals("http://mt.example.com", getField("url"));
    assertEquals("user1", getField("username"));
    assertEquals("pass1", getField("password"));
  }

  @Test
  public void testInit_whenConfigReturnsNull_thenFieldsAreNull()
    throws Exception {
    when(circabcConfig.getMtServiceUrl()).thenReturn(null);
    when(circabcConfig.getMtRESTUsername()).thenReturn(null);
    when(circabcConfig.getMtRESTPassword()).thenReturn(null);

    service.init();

    assertNull(getField("url"));
    assertNull(getField("username"));
    assertNull(getField("password"));
  }

  @Test
  public void testCreateTranslationRequest_whenTextTranslation_thenJsonContainsTextToTranslate()
    throws Exception {
    MachineTranslationRequest request = buildRequest();
    request.setDocumentToTranslate("");
    request.setTextToTranslate("Hello world");

    String json = invokeCreateTranslationRequest(request);
    JSONObject obj = new JSONObject(json);

    assertEquals("Hello world", obj.getString("textToTranslate"));
    assertEquals("EN", obj.getString("sourceLanguage"));
    assertEquals("FR", obj.getJSONArray("targetLanguages").getString(0));
    assertEquals("SPD", obj.getString("domain"));
    assertEquals(5, obj.getInt("priority"));
    assertEquals("ref-123", obj.getString("externalReference"));
    assertEquals("http://callback", obj.getString("requesterCallback"));
    assertEquals("http://error", obj.getString("errorCallback"));
    assertFalse(obj.has("documentToTranslatePath"));
    assertFalse(obj.has("destinations"));

    JSONObject caller = obj.getJSONObject("callerInformation");
    assertEquals("CIRCABC", caller.getString("application"));
    assertEquals("testuser", caller.getString("username"));
    assertEquals("EC", caller.getString("institution"));
  }

  @Test
  public void testCreateTranslationRequest_whenDocumentTranslation_thenJsonContainsDocumentPath()
    throws Exception {
    MachineTranslationRequest request = buildRequest();
    request.setDocumentToTranslate("/path/to/doc.pdf");
    request.setTargetTranslationPath("/ftp/output");

    String json = invokeCreateTranslationRequest(request);
    JSONObject obj = new JSONObject(json);

    assertEquals("/path/to/doc.pdf", obj.getString("documentToTranslatePath"));
    assertFalse(obj.has("textToTranslate"));
    JSONObject destinations = obj.getJSONObject("destinations");
    assertEquals(
      "/ftp/output",
      destinations.getJSONArray("ftpDestinations").getString(0)
    );
  }

  @Test
  public void testCreateTranslationRequest_whenMultipleTargetLanguages_thenAllIncluded()
    throws Exception {
    MachineTranslationRequest request = buildRequest();
    request.setDocumentToTranslate("");
    request.setTextToTranslate("text");
    request.setTargetLanguage("FR,DE,ES");

    String json = invokeCreateTranslationRequest(request);
    JSONObject obj = new JSONObject(json);

    assertEquals(3, obj.getJSONArray("targetLanguages").length());
    assertEquals("FR", obj.getJSONArray("targetLanguages").getString(0));
    assertEquals("DE", obj.getJSONArray("targetLanguages").getString(1));
    assertEquals("ES", obj.getJSONArray("targetLanguages").getString(2));
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testSendMessage_whenInvalidUrl_thenThrowsCircabcRuntimeException()
    throws Exception {
    setField("url", "http://invalid-host-that-does-not-exist.test:9999/api");
    setField("username", "user");
    setField("password", "pass");

    MachineTranslationRequest request = buildRequest();
    request.setDocumentToTranslate("");
    request.setTextToTranslate("Hello");

    service.sendMessage(request);
  }

  private MachineTranslationRequest buildRequest() {
    MachineTranslationRequest request = new MachineTranslationRequest();
    request.setApplicationName("CIRCABC");
    request.setUsername("testuser");
    request.setInstitution("EC");
    request.setSourceLanguage("EN");
    request.setTargetLanguage("FR");
    request.setPriority(5);
    request.setExternalReference("ref-123");
    request.setRequesterCallback("http://callback");
    request.setErrorCallback("http://error");
    return request;
  }

  private String invokeCreateTranslationRequest(
    MachineTranslationRequest request
  ) throws Exception {
    Method method = MachineTranslationServiceImpl.class.getDeclaredMethod(
      "createTranslationRequest",
      MachineTranslationRequest.class
    );
    method.setAccessible(true);
    return (String) method.invoke(null, request);
  }
}
