package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DisableBasicAuthenicationPopupFilterTest {

  private DisableBasicAuthenicationPopupFilter filter;
  private FilterChain chain;
  private ServletRequest request;

  @Before
  public void setUp() {
    filter = new DisableBasicAuthenicationPopupFilter();
    chain = mock(FilterChain.class);
    request = mock(ServletRequest.class);
  }

  @Test
  public void testDoFilter_whenHttpServletResponse_thenWrapsResponse()
    throws ServletException, IOException {
    HttpServletResponse httpResponse = mock(HttpServletResponse.class);

    filter.doFilter(request, httpResponse, chain);

    ArgumentCaptor<ServletResponse> captor = ArgumentCaptor.forClass(
      ServletResponse.class
    );
    verify(chain).doFilter(eq(request), captor.capture());
    assertTrue(
      captor.getValue() instanceof DisableBasicAuthenicationPopupResponseWrapper
    );
  }

  @Test
  public void testDoFilter_whenNonHttpServletResponse_thenChainNotCalled()
    throws ServletException, IOException {
    ServletResponse plainResponse = mock(ServletResponse.class);

    filter.doFilter(request, plainResponse, chain);

    verifyNoInteractions(chain);
  }

  @Test
  public void testInit_doesNotThrow() throws ServletException {
    FilterConfig config = mock(FilterConfig.class);
    filter.init(config);
    // No exception means success
  }

  @Test
  public void testDestroy_doesNotThrow() {
    filter.destroy();
    // No exception means success
  }
}
