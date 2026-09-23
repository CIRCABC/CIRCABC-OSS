package eu.europa.ec.digit.circabc.rest.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet {@link Filter} that suppresses the native browser Basic Authentication
 * login popup.
 *
 * <p>When a server responds to an unauthenticated request with a
 * {@code 401 Unauthorized} status and a {@code WWW-Authenticate: Basic} header,
 * browsers display their built-in credential prompt. For a REST API consumed by
 * a single-page application this behavior is undesirable, as the application
 * needs to handle authentication failures itself.
 *
 * <p>This filter wraps the outgoing response in a
 * {@link DisableBasicAuthenicationPopupResponseWrapper}, which strips the
 * {@code WWW-Authenticate} header before it is sent to the client, thereby
 * preventing the popup from appearing while leaving the response otherwise
 * unchanged.
 */
public class DisableBasicAuthenicationPopupFilter implements Filter {

  /**
   * Wraps the HTTP response so that the {@code WWW-Authenticate} header is
   * suppressed, then forwards the request along the filter chain.
   *
   * <p>Non-HTTP responses are ignored and are not passed further down the chain.
   *
   * @param request the incoming servlet request
   * @param response the outgoing servlet response; wrapped when it is an
   *     {@link HttpServletResponse}
   * @param chain the filter chain used to invoke the next filter or resource
   * @throws ServletException if the downstream processing raises a servlet error
   * @throws IOException if an I/O error occurs during downstream processing
   */
  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    if (response instanceof HttpServletResponse httpResponse) {
      HttpServletResponse newResponse =
        new DisableBasicAuthenicationPopupResponseWrapper(httpResponse);
      chain.doFilter(request, newResponse);
    }
  }

  /**
   * Initializes the filter. This filter requires no configuration or set-up,
   * so the method intentionally does nothing.
   *
   * @param conf the filter configuration provided by the servlet container
   * @throws ServletException if initialization fails (never thrown here)
   */
  @Override
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public void init(FilterConfig conf) throws ServletException {
    // No initialization required for this filter
  }

  /**
   * Releases any resources held by the filter. This filter holds no resources,
   * so the method intentionally does nothing.
   */
  @Override
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public void destroy() {
    // No resources to release
  }
}
