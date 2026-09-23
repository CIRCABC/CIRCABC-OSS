package eu.europa.ec.digit.circabc.rest.servlet;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * {@link HttpServletResponseWrapper} that suppresses the {@code WWW-Authenticate}
 * response header.
 *
 * <p>When a server responds with an HTTP 401 status together with a
 * {@code WWW-Authenticate: Basic} header, browsers display a native basic
 * authentication popup dialog. By stripping this header before it reaches the
 * client, this wrapper prevents that popup from appearing, allowing the
 * application to handle authentication failures via its own (e.g. form- or
 * token-based) flow instead.</p>
 *
 * <p>Typically installed by a servlet filter that wraps the outgoing
 * {@link HttpServletResponse} with an instance of this class.</p>
 */
public class DisableBasicAuthenicationPopupResponseWrapper
  extends HttpServletResponseWrapper
{

  /**
   * Creates a wrapper around the given response.
   *
   * @param response the underlying {@link HttpServletResponse} to be wrapped
   */
  public DisableBasicAuthenicationPopupResponseWrapper(
    HttpServletResponse response
  ) {
    super(response);
  }

  /**
   * Sets a response header, silently discarding any attempt to set the
   * {@code WWW-Authenticate} header.
   *
   * <p>All other headers are delegated unchanged to the wrapped response. The
   * comparison against {@code WWW-Authenticate} is case-insensitive.</p>
   *
   * @param name  the header name; if it equals {@code WWW-Authenticate}
   *              (ignoring case) the call is ignored
   * @param value the header value to set for all other header names
   */
  @Override
  public void setHeader(String name, String value) {
    if (!name.equalsIgnoreCase("WWW-Authenticate")) {
      super.setHeader(name, value);
    }
  }
}
