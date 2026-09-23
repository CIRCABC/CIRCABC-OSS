package eu.europa.ec.digit.circabc.rest.servlet;

import eu.europa.ec.digit.circabc.rest.service.translation.TranslationDaoService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base class for servlets that receive external notification callbacks (for
 * example, translation status callbacks) over HTTP.
 *
 * <p>This servlet handles only {@code POST} requests. It ensures the Spring
 * {@link TranslationDaoService} bean is wired from the surrounding web
 * application context, normalizes the request character encoding to UTF-8, and
 * extracts the common notification parameters before delegating the actual
 * processing to the concrete subclass.
 *
 * <p>Recognized request parameters:
 * <ul>
 *   <li>{@code external-reference} &ndash; an external reference identifier for
 *       the notified item.</li>
 *   <li>{@code requestId} (or its fallback {@code request-id}) &ndash; the
 *       identifier of the originating request.</li>
 * </ul>
 *
 * <p>Concrete subclasses implement {@link #handleNotification} to perform the
 * notification-specific logic and {@link #getLogger()} to expose their own
 * logger.
 */
public abstract class AbstractNotifyServlet extends HttpServlet {

  /**
   * Data access service used to read and update translation-related state.
   * Injected from the Spring web application context during {@link #init}.
   */
  protected transient TranslationDaoService translationDaoService;

  /**
   * Provides the logger used by the concrete subclass.
   *
   * @return the {@link Log} instance for the implementing servlet
   */
  protected abstract Log getLogger();

  /**
   * Initializes the servlet and resolves the {@link TranslationDaoService} bean
   * from the enclosing Spring web application context.
   *
   * @param config the servlet configuration provided by the container
   * @throws ServletException if initialization fails or the web application
   *     context cannot be obtained
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init();
    WebApplicationContext context =
      WebApplicationContextUtils.getRequiredWebApplicationContext(
        getServletContext()
      );
    translationDaoService = (TranslationDaoService) context.getBean(
      "translationDaoService"
    );
  }

  /**
   * Handles an incoming notification {@code POST} request.
   *
   * <p>Lazily initializes the servlet if required, forces UTF-8 request
   * decoding, extracts the {@code external-reference} and {@code requestId}
   * (falling back to {@code request-id}) parameters, and delegates to
   * {@link #handleNotification}.
   *
   * @param request the HTTP request containing the notification parameters
   * @param response the HTTP response
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while processing the request
   */
  @Override
  @SuppressWarnings({ "squid:S1989" })
  protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws ServletException, IOException {
    if (translationDaoService == null) {
      init(getServletConfig());
    }
    request.setCharacterEncoding("UTF-8");

    String externalReference = request.getParameter("external-reference");
    String requestId = request.getParameter("requestId");
    if (requestId == null) {
      requestId = request.getParameter("request-id");
    }

    handleNotification(request, requestId, externalReference);
  }

  /**
   * Performs the notification-specific processing for a received callback.
   *
   * @param request the originating HTTP request, providing access to any
   *     additional parameters
   * @param requestId the identifier of the originating request, or {@code null}
   *     if it was not supplied
   * @param externalReference the external reference identifier, or
   *     {@code null} if it was not supplied
   */
  protected abstract void handleNotification(
    HttpServletRequest request,
    String requestId,
    String externalReference
  );
}
