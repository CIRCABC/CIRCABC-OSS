package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CaptchaApi;
import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.File;
import java.util.*;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco webscript endpoint handling the submission of a help/support request.
 *
 * <p>This endpoint is bound to an HTTP {@code POST} request (as implied by the
 * {@code Post} suffix in the class name). It accepts a {@code multipart/form-data}
 * body describing a support enquiry and forwards it to the {@link HelpApi} so that
 * a support ticket is created and/or a notification email is sent.
 *
 * <p>Expected form fields are: {@code reason}, {@code name}, {@code email},
 * {@code subject} and {@code content}. Any additional file parts are collected as
 * attachments. An optional {@code language} request parameter controls the locale
 * used when rendering multilingual content.
 *
 * <p>When the caller is a guest user, a CAPTCHA challenge (provided via the
 * {@code X-EU-CAPTCHA-TOKEN}, {@code X-EU-CAPTCHA-ID} and {@code X-EU-CAPTCHA-TEXT}
 * request headers) is validated before the request is processed.
 */
public class HelpSupportMailPost extends DeclarativeWebScript {

  /** Logger used to report errors occurring while processing a support request. */
  static final Log logger = LogFactory.getLog(HelpSupportMailPost.class);

  /** Business API used to create the support ticket and/or send the support email. */
  @Autowired
  private HelpApi helpApi;

  /** API used to validate the CAPTCHA challenge submitted by guest users. */
  @Autowired
  private CaptchaApi captchaApi;

  /** Service used to determine whether the current caller is an authenticated user or a guest. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the support-request submission.
   *
   * <p>Sets up the locale from the {@code language} request parameter, validates the
   * CAPTCHA when the caller is a guest, parses the multipart form data (including any
   * file attachments) and delegates to {@link HelpApi#contactSupport} to create the
   * support ticket and send the notification email.
   *
   * @param req    the web script request, expected to carry a multipart form body and,
   *               for guests, the CAPTCHA headers
   * @param status the response status; updated with an error code, message and redirect
   *               flag when processing fails
   * @param cache  the response cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error has been handled
   *         and reflected on {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    try {
      validateCaptchaIfGuest(req);
      FormData form = validateForm(req);
      SupportMailData data = parseFormData(form);
      helpApi.contactSupport(
        data.reason,
        data.name,
        data.email,
        data.subject,
        data.content,
        data.attachments
      );
    } catch (AccessDeniedException e) {
      return handleError(status, Status.STATUS_FORBIDDEN, e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error - bad arguments",
        e
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error when creating SMT ticket or sending email",
        e
      );
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Configures the thread's locale for the request.
   *
   * <p>When no language is supplied, multilingual property resolution is enabled so
   * that content is returned in its multilingual form. When a language is supplied,
   * the content and UI locales are set accordingly and multilingual resolution is
   * disabled so that content is returned for the requested locale only.
   *
   * @param language the requested ISO language code, or {@code null} to enable
   *                 multilingual-aware resolution
   */
  private void setupLocale(String language) {
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
  }

  /**
   * Validates the CAPTCHA challenge for guest callers.
   *
   * <p>Authenticated users are exempt and the method returns immediately. For guests,
   * the CAPTCHA token, id and answer are read from the {@code X-EU-CAPTCHA-TOKEN},
   * {@code X-EU-CAPTCHA-ID} and {@code X-EU-CAPTCHA-TEXT} headers and validated via
   * {@link CaptchaApi#validate}.
   *
   * @param req the web script request carrying the CAPTCHA headers
   * @throws AccessDeniedException if any CAPTCHA header is missing or the CAPTCHA
   *                               answer is invalid
   */
  private void validateCaptchaIfGuest(WebScriptRequest req) {
    if (!currentUserPermissionCheckerService.isGuest()) {
      return;
    }
    String captchaToken = req.getHeader("X-EU-CAPTCHA-TOKEN");
    String captchaId = req.getHeader("X-EU-CAPTCHA-ID");
    String answer = req.getHeader("X-EU-CAPTCHA-TEXT");

    if (captchaToken == null || captchaId == null || answer == null) {
      throw new AccessDeniedException("Invalid Captcha parameters");
    }
    if (!captchaApi.validate(captchaToken, captchaId, answer)) {
      throw new AccessDeniedException("invalid captcha answer");
    }
  }

  /**
   * Extracts and validates the multipart form data from the request.
   *
   * @param req the web script request
   * @return the parsed multipart {@link FormData}
   * @throws IllegalArgumentException if the request body is missing or is not a
   *                                  multipart request
   */
  private FormData validateForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null || !form.getIsMultiPart()) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    return form;
  }

  /**
   * Parses all fields of the multipart form into a {@link SupportMailData} instance.
   *
   * @param form the multipart form data
   * @return the populated support-mail data, including any file attachments
   * @throws Exception if a field (in particular a file attachment) cannot be read
   */
  private SupportMailData parseFormData(FormData form) throws Exception {
    SupportMailData data = new SupportMailData();
    for (FormData.FormField field : form.getFields()) {
      processField(field, data);
    }
    return data;
  }

  /**
   * Maps a single form field onto the corresponding {@link SupportMailData} property.
   *
   * <p>Known fields ({@code reason}, {@code name}, {@code email}, {@code subject},
   * {@code content}) populate the matching property. Any other field that is a file
   * with readable content is written to a temporary file and added to the list of
   * attachments.
   *
   * @param field the form field to process
   * @param data  the target support-mail data to populate
   * @throws Exception if the field is a file and its content cannot be read or stored
   */
  private void processField(FormData.FormField field, SupportMailData data)
    throws Exception {
    switch (field.getName()) {
      case "reason":
        data.reason = field.getValue();
        break;
      case "name":
        data.name = field.getValue();
        break;
      case "email":
        data.email = field.getValue();
        break;
      case "subject":
        data.subject = field.getValue();
        break;
      case "content":
        data.content = field.getValue();
        break;
      default:
        if (field.getIsFile() && field.getInputStream() != null) {
          File attachment = TempFileProvider.createTempFile(
            field.getInputStream(),
            field.getFilename(),
            "cbctmp"
          );
          data.attachments.add(attachment);
        }
    }
  }

  /**
   * Records an error on the response and logs it.
   *
   * <p>Sets the given status code and message, marks the response as a redirect and
   * logs the underlying exception at error level.
   *
   * @param status  the response status to update
   * @param code    the HTTP status code to set
   * @param message the human-readable error message
   * @param e       the underlying exception to log
   * @return {@code null}, signalling to the framework that the response has already
   *         been handled via {@code status}
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    if (logger.isErrorEnabled()) {
      logger.error(message, e);
    }
    return null; // NOSONAR
  }

  /**
   * Simple value holder aggregating the parsed support-request fields and attachments
   * collected from the multipart form before they are handed to the {@link HelpApi}.
   */
  private static class SupportMailData {

    String reason = "";
    String name = "";
    String email = "";
    String subject = "";
    String content = "";
    List<File> attachments = new ArrayList<>();
  }
}
