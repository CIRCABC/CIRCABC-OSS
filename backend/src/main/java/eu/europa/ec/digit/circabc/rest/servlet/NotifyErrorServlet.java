package eu.europa.ec.digit.circabc.rest.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that receives error notification callbacks from the external
 * translation service, mapped to the {@code /notifyError/*} URL pattern.
 *
 * <p>As with its {@link AbstractNotifyServlet} base class, notifications are
 * delivered as HTTP {@code POST} requests. When a translation request fails,
 * the external service posts the error details here; this servlet logs them and
 * persists an error response record via the
 * {@link eu.europa.ec.digit.circabc.rest.service.translation.TranslationDaoService}.
 *
 * <p>In addition to the {@code requestId} and {@code external-reference}
 * parameters extracted by the base class, this servlet reads the following
 * request parameters:
 * <ul>
 *   <li>{@code target-languages} &ndash; the target language(s) of the failed
 *       translation request.</li>
 *   <li>{@code error-code} &ndash; the error code reported by the translation
 *       service.</li>
 *   <li>{@code error-message} &ndash; the human-readable error message.</li>
 * </ul>
 */
@WebServlet("/notifyError/*")
public class NotifyErrorServlet extends AbstractNotifyServlet {

  /** Serialization version identifier for this servlet. */
  private static final long serialVersionUID = 6426846930671976111L;

  /** Logger used to record received error notifications and persistence failures. */
  private static final Log logger = LogFactory.getLog(NotifyErrorServlet.class);

  /**
   * {@inheritDoc}
   *
   * @return the {@link Log} instance for this servlet
   */
  @Override
  protected Log getLogger() {
    return logger;
  }

  /**
   * Handles an error notification callback.
   *
   * <p>Reads the {@code target-languages}, {@code error-code} and
   * {@code error-message} parameters from the request, logs the error details,
   * and attempts to persist an error response record through the translation
   * DAO service. Any exception raised while saving is caught and logged so that
   * the failed persistence does not propagate to the caller.
   *
   * @param request the HTTP request carrying the error notification parameters
   * @param requestId the identifier of the originating translation request, or
   *     {@code null} if it was not supplied
   * @param externalReference the external reference identifier, or
   *     {@code null} if it was not supplied
   */
  @Override
  protected void handleNotification(
    HttpServletRequest request,
    String requestId,
    String externalReference
  ) {
    String targetLanguage = request.getParameter("target-languages");
    String errorCode = request.getParameter("error-code");
    String errorMessage = request.getParameter("error-message");

    if (logger.isInfoEnabled()) {
      logger.info("***** ERROR RECEIVED *****");
      logger.info("Request Id : " + requestId);
      logger.info("Target Language : " + targetLanguage);
      logger.info("Error Code : " + errorCode);
      logger.info("Error Message : " + errorMessage);
      logger.info("External Reference" + externalReference);
    }

    try {
      translationDaoService.saveErrorResponse(
        requestId,
        targetLanguage,
        errorCode,
        errorMessage
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("can not save record to database", e);
      }
    }
  }
}
