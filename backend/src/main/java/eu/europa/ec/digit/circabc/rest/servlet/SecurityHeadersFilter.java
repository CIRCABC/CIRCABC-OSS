package eu.europa.ec.digit.circabc.rest.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that adds security response headers to all API responses.
 *
 * <p>Headers added:
 * <ul>
 *   <li>X-Content-Type-Options: nosniff — prevents MIME sniffing</li>
 *   <li>X-Frame-Options: DENY — prevents clickjacking</li>
 *   <li>Strict-Transport-Security — enforces HTTPS (only when request is HTTPS)</li>
 *   <li>Content-Security-Policy — restricts resource loading origins</li>
 *   <li>Referrer-Policy — limits referrer information leakage</li>
 *   <li>Permissions-Policy — disables unnecessary browser features</li>
 * </ul>
 */
public class SecurityHeadersFilter implements Filter {

  /**
   * The Content-Security-Policy header value applied to every response. It
   * denies all resource loading by default, forbids the response from being
   * framed and restricts the document base URI, providing a restrictive
   * baseline suitable for JSON API responses.
   */
  private static final String CSP_VALUE =
    "default-src 'none'; frame-ancestors 'none'; base-uri 'none'";

  /**
   * Adds the security response headers to every HTTP response before passing
   * the request further down the filter chain.
   *
   * <p>When the request/response pair is HTTP, the standard hardening headers
   * (X-Content-Type-Options, X-Frame-Options, Referrer-Policy,
   * Permissions-Policy and Content-Security-Policy) are set. The
   * Strict-Transport-Security header is only added when the request was made
   * over HTTPS (directly or via the {@code X-Forwarded-Proto} header set by a
   * reverse proxy), so that local HTTP development is not broken. For
   * non-HTTP requests the chain is invoked without modification.
   *
   * @param request the incoming servlet request
   * @param response the outgoing servlet response to which the headers are added
   * @param chain the filter chain used to invoke the next filter or resource
   * @throws ServletException if the downstream filter chain raises a servlet error
   * @throws IOException if an I/O error occurs during processing
   */
  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    if (
      response instanceof HttpServletResponse httpResponse &&
      request instanceof HttpServletRequest httpRequest
    ) {
      httpResponse.setHeader("X-Content-Type-Options", "nosniff");
      httpResponse.setHeader("X-Frame-Options", "DENY");
      httpResponse.setHeader(
        "Referrer-Policy",
        "strict-origin-when-cross-origin"
      );
      httpResponse.setHeader(
        "Permissions-Policy",
        "camera=(), microphone=(), geolocation=()"
      );
      httpResponse.setHeader("Content-Security-Policy", CSP_VALUE);

      // Only add HSTS when behind HTTPS (avoids breaking local HTTP dev)
      if (
        "https".equalsIgnoreCase(httpRequest.getScheme()) ||
        "https".equalsIgnoreCase(httpRequest.getHeader("X-Forwarded-Proto"))
      ) {
        httpResponse.setHeader(
          "Strict-Transport-Security",
          "max-age=31536000; includeSubDomains"
        );
      }

      chain.doFilter(request, httpResponse);
    } else {
      chain.doFilter(request, response);
    }
  }

  /**
   * Initializes the filter. This filter is stateless and requires no
   * configuration, so this method is intentionally empty.
   *
   * @param config the filter configuration provided by the servlet container
   * @throws ServletException if initialization fails
   */
  @Override
  @SuppressWarnings("java:S1186")
  public void init(FilterConfig config) throws ServletException {
    // No initialization required
  }

  /**
   * Releases resources held by the filter. This filter holds no resources, so
   * this method is intentionally empty.
   */
  @Override
  @SuppressWarnings("java:S1186")
  public void destroy() {
    // No resources to release
  }
}
