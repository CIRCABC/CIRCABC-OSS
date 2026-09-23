package io.swagger.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CaptchaApiImplTest {

  private CaptchaApiImpl captchaApi;

  @Before
  public void setUp() {
    captchaApi = new CaptchaApiImpl();
    captchaApi.setCaptchaUrl("http://localhost:8080/captcha");
    captchaApi.setUseProxy("false");
  }

  @Test
  public void testSettersAndGetters() {
    captchaApi.setCaptchaUrl("http://example.com");
    captchaApi.setUseProxy("true");
    captchaApi.setProxyHost("proxy.example.com");
    captchaApi.setProxyPort("8888");
    captchaApi.setProxyUserName("user");
    captchaApi.setProxyPassword("pass");

    assertEquals("http://example.com", captchaApi.getCaptchaUrl());
    assertEquals("true", captchaApi.getUseProxy());
    assertEquals("proxy.example.com", captchaApi.getProxyHost());
    assertEquals("8888", captchaApi.getProxyPort());
    assertEquals("user", captchaApi.getProxyUserName());
    assertEquals("pass", captchaApi.getProxyPassword());
  }

  @Test
  public void testHandleCaptchaResponse_whenResponseContainsSuccess_thenReturnsTrue()
    throws Exception {
    PostMethod postMethod = Mockito.mock(PostMethod.class);
    Mockito.when(postMethod.getResponseBodyAsString()).thenReturn(
      "{\"success\": true}"
    );

    Method handleMethod = CaptchaApiImpl.class.getDeclaredMethod(
      "handleCaptchaResponse",
      PostMethod.class,
      String.class
    );
    handleMethod.setAccessible(true);

    boolean result = (boolean) handleMethod.invoke(
      captchaApi,
      postMethod,
      "http://test-url"
    );
    assertTrue(result);
  }

  @Test
  public void testHandleCaptchaResponse_whenResponseDoesNotContainSuccess_thenReturnsFalse()
    throws Exception {
    PostMethod postMethod = Mockito.mock(PostMethod.class);
    Mockito.when(postMethod.getResponseBodyAsString()).thenReturn(
      "{\"error\": \"invalid\"}"
    );

    Method handleMethod = CaptchaApiImpl.class.getDeclaredMethod(
      "handleCaptchaResponse",
      PostMethod.class,
      String.class
    );
    handleMethod.setAccessible(true);

    boolean result = (boolean) handleMethod.invoke(
      captchaApi,
      postMethod,
      "http://test-url"
    );
    assertFalse(result);
  }

  @Test
  public void testHandleCaptchaResponse_whenIOException_thenThrows()
    throws Exception {
    PostMethod postMethod = Mockito.mock(PostMethod.class);
    Mockito.when(postMethod.getResponseBodyAsString()).thenThrow(
      new IOException("connection reset")
    );

    Method handleMethod = CaptchaApiImpl.class.getDeclaredMethod(
      "handleCaptchaResponse",
      PostMethod.class,
      String.class
    );
    handleMethod.setAccessible(true);

    try {
      handleMethod.invoke(captchaApi, postMethod, "http://test-url");
      fail("Expected IOException wrapped in InvocationTargetException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IOException);
    }
  }

  @Test
  public void testConfigureProxy_whenUseProxyFalse_thenNoProxySet()
    throws Exception {
    captchaApi.setUseProxy("false");
    HttpClient httpClient = new HttpClient();

    Method configureMethod = CaptchaApiImpl.class.getDeclaredMethod(
      "configureProxy",
      HttpClient.class
    );
    configureMethod.setAccessible(true);
    configureMethod.invoke(captchaApi, httpClient);

    assertNull(httpClient.getHostConfiguration().getProxyHost());
  }

  @Test
  public void testConfigureProxy_whenUseProxyTrue_thenProxyConfigured()
    throws Exception {
    captchaApi.setUseProxy("true");
    captchaApi.setProxyHost("proxy.test.com");
    captchaApi.setProxyPort("3128");
    captchaApi.setProxyUserName("admin");
    captchaApi.setProxyPassword("secret");

    HttpClient httpClient = new HttpClient();

    Method configureMethod = CaptchaApiImpl.class.getDeclaredMethod(
      "configureProxy",
      HttpClient.class
    );
    configureMethod.setAccessible(true);
    configureMethod.invoke(captchaApi, httpClient);

    assertEquals(
      "proxy.test.com",
      httpClient.getHostConfiguration().getProxyHost()
    );
    assertEquals(3128, httpClient.getHostConfiguration().getProxyPort());
  }
}
