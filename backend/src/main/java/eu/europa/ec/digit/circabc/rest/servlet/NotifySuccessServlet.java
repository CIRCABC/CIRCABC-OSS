package eu.europa.ec.digit.circabc.rest.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that receives successful translation callback notifications.
 *
 * <p>Mapped to {@code /notifySuccess/*}, this servlet is invoked by the
 * external translation service via HTTP {@code POST} when a translation
 * completes successfully. Building on {@link AbstractNotifyServlet}, which
 * extracts the common {@code requestId} and {@code external-reference}
 * parameters, it additionally reads the {@code target-language} and
 * {@code translated-text} parameters, logs the received response, and
 * persists it through the {@code translationDaoService}.
 *
 * <p>Persistence failures are caught and logged rather than propagated, so a
 * database error does not fail the callback.
 */
@WebServlet("/notifySuccess/*")
public class NotifySuccessServlet extends AbstractNotifyServlet {

  /** Serialization version identifier for this servlet. */
  private static final long serialVersionUID = 4125768300591694348L;

  /** Logger for this servlet. */
  private static final Log logger = LogFactory.getLog(
    NotifySuccessServlet.class
  );

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
   * Handles a successful translation notification.
   *
   * <p>Reads the {@code target-language} and {@code translated-text}
   * parameters from the request, logs the received response, and stores it via
   * {@code translationDaoService.saveSuccessResponse}. Any exception raised
   * while saving is caught and logged so that the callback still completes.
   *
   * @param request the originating HTTP request, used to read the
   *     {@code target-language} and {@code translated-text} parameters
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
    String targetLanguage = request.getParameter("target-language");
    String translatedText = request.getParameter("translated-text");

    if (logger.isInfoEnabled()) {
      logger.info("***** RESPONSE RECEIVED *****");
      logger.info("Request Id : " + requestId);
      logger.info("Target Language : " + targetLanguage);
      logger.info("Translated Text : " + translatedText);
      logger.info("External Reference : " + externalReference);
    }

    try {
      translationDaoService.saveSuccessResponse(
        requestId,
        targetLanguage,
        null,
        translatedText
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("can not save record to database", e);
      }
    }
  }
}
