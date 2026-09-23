package eu.europa.ec.digit.circabc.rest.servlet;

import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class DisableBasicAuthenicationPopupResponseWrapperTest {

  private HttpServletResponse mockResponse;
  private DisableBasicAuthenicationPopupResponseWrapper wrapper;

  @Before
  public void setUp() {
    mockResponse = mock(HttpServletResponse.class);
    wrapper = new DisableBasicAuthenicationPopupResponseWrapper(mockResponse);
  }

  @Test
  public void testSetHeader_whenWwwAuthenticate_thenSuppressed() {
    wrapper.setHeader("WWW-Authenticate", "Basic realm=\"test\"");
    verify(mockResponse, never()).setHeader(anyString(), anyString());
  }

  @Test
  public void testSetHeader_whenWwwAuthenticateLowerCase_thenSuppressed() {
    wrapper.setHeader("www-authenticate", "Basic realm=\"test\"");
    verify(mockResponse, never()).setHeader(anyString(), anyString());
  }

  @Test
  public void testSetHeader_whenOtherHeader_thenDelegated() {
    wrapper.setHeader("Content-Type", "application/json");
    verify(mockResponse).setHeader("Content-Type", "application/json");
  }
}
