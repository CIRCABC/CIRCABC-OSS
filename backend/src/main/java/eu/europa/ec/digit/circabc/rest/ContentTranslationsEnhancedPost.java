package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.exception.EmptyFileException;
import io.swagger.exception.MaxFileSizeException;
import io.swagger.model.I18nProperty;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code POST} endpoint used to
 * create a translation of an existing library content item in CIRCABC.
 *
 * <p>The endpoint expects a {@code multipart/form-data} request whose path
 * provides the identifier ({@code id}) of the source content node to translate.
 * The uploaded file becomes the translated content, while the accompanying form
 * fields carry the metadata of the new translation, including:
 * <ul>
 *   <li>{@code lang} &ndash; the target language code of the translation;</li>
 *   <li>{@code title} and {@code description} &ndash; i18n property values
 *       supplied as JSON keyed by language code;</li>
 *   <li>{@code author}, {@code reference}, {@code securityRanking},
 *       {@code status}, {@code keywords} &ndash; plain metadata values;</li>
 *   <li>{@code expirationDate} &ndash; an optional expiration date;</li>
 *   <li>{@code dynamicProperties} &ndash; a JSON object of additional dynamic
 *       properties;</li>
 *   <li>the uploaded file field itself, providing content, filename and
 *       MIME type.</li>
 * </ul>
 *
 * <p>Before the translation is created, the current user must hold at least the
 * {@link LibraryPermissions#LIBMANAGEOWN} permission on the target node, and the
 * uploaded file must be non-empty and not exceed {@link #MAX_SIZE_UPLOAD}. On
 * success the model exposes the identifier of the newly created node under the
 * {@code nodeRef} key. Failures are translated into the appropriate HTTP status
 * codes (forbidden, bad request or internal server error).
 */
public class ContentTranslationsEnhancedPost
  extends CircabcDeclarativeWebScript
{

  /** Logger used to record errors raised while handling the upload. */
  static final Log logger = LogFactory.getLog(
    ContentTranslationsEnhancedPost.class
  );

  /** Maximum allowed size, in bytes, of an uploaded translation file (300&nbsp;MB). */
  private static final Long MAX_SIZE_UPLOAD = 1024L * 1024 * 300;

  /** Business API used to create the content translation node. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to verify that the current user holds the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming {@code POST} request: validates permissions and the
   * multipart form, parses the translation metadata and uploaded file, enforces
   * the maximum file size, and delegates creation of the translation node to the
   * {@link ContentApi}.
   *
   * @param req the web script request; its service match must provide the
   *            {@code id} template variable identifying the source content node,
   *            and the body must be a {@code multipart/form-data} payload
   * @param status the response status, populated with an appropriate HTTP code
   *               and message when an error occurs
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code nodeRef} identifier of the created
   *         translation on success, or {@code null} when an error has been
   *         handled and reflected in {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    try {
      String id = templateVars.get("id");
      validatePermission(id);
      FormData form = validateForm(req);
      TranslationData data = parseFormData(form);
      validateFileSize(data.size);

      NodeRef fileRef = contentApi.createContentTranslation(
        id,
        data.defaultName,
        data.title,
        data.description,
        data.author,
        data.reference,
        data.securityRanking,
        data.statusProp,
        data.keywords.split(","),
        data.expirationDate,
        data.mimeType,
        data.file,
        data.lang,
        data.dynProps
      );
      model.put("nodeRef", fileRef.getId());
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad request",
        inre
      );
    } catch (MaxFileSizeException e) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Maximum file size for the upload",
        e
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error during upload file",
        e
      );
    }
    return model;
  }

  private void validatePermission(String id) {
    if (
      !currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBMANAGEOWN
      )
    ) {
      throw new AccessDeniedException(
        "Cannot update content, not enough permissions"
      );
    }
  }

  private FormData validateForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null || !form.getIsMultiPart()) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    return form;
  }

  private void validateFileSize(long size) throws MaxFileSizeException {
    if (size > MAX_SIZE_UPLOAD) {
      throw new MaxFileSizeException("file is too big for the upload");
    }
  }

  private TranslationData parseFormData(FormData form)
    throws ParseException, IOException, EmptyFileException, java.text.ParseException {
    TranslationData data = new TranslationData();
    for (FormData.FormField field : form.getFields()) {
      processField(field, data);
    }
    return data;
  }

  private void processField(FormData.FormField field, TranslationData data)
    throws ParseException, IOException, EmptyFileException, java.text.ParseException {
    String fieldName = field.getName();
    switch (fieldName) {
      case "name":
        break;
      case "author":
        data.author = field.getValue();
        break;
      case "reference":
        data.reference = field.getValue();
        break;
      case "securityRanking":
        data.securityRanking = field.getValue();
        break;
      case "status":
        data.statusProp = field.getValue();
        break;
      case "keywords":
        data.keywords = field.getValue();
        break;
      case "lang":
        data.lang = field.getValue();
        break;
      case "expirationDate":
        data.expirationDate = parseExpirationDate(field);
        break;
      case "title":
        data.title = parseI18nProperty(field);
        break;
      case "description":
        data.description = parseI18nProperty(field);
        break;
      case "dynamicProperties":
        data.dynProps = parseDynamicProperties(field);
        break;
      default:
        if (field.getIsFile()) {
          processFileField(field, data);
        }
    }
  }

  private Date parseExpirationDate(FormData.FormField field)
    throws java.text.ParseException {
    String value = field.getValue();
    if (value == null || value.isEmpty() || "null".equals(value)) {
      return null;
    }
    @SuppressWarnings("java:S5361")
    String cleanValue = value.replaceAll("\"", "").trim();
    return Converter.convertStringToSimpleDate(cleanValue);
  }

  private I18nProperty parseI18nProperty(FormData.FormField field)
    throws ParseException, IOException {
    I18nProperty prop = new I18nProperty();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(Converter.getValue(field));
    if (json != null) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (json.containsKey(code)) {
          prop.put(code, String.valueOf(json.get(code)));
        }
      }
    }
    return prop;
  }

  private Map<String, Object> parseDynamicProperties(FormData.FormField field)
    throws ParseException, IOException {
    Map<String, Object> dynProps = new HashMap<>();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(Converter.getValue(field));
    @SuppressWarnings("unchecked")
    Set<String> keys = json.keySet();
    for (String key : keys) {
      dynProps.put(key, json.get(key));
    }
    return dynProps;
  }

  private void processFileField(FormData.FormField field, TranslationData data)
    throws IOException, EmptyFileException {
    data.file = field.getInputStream();
    data.defaultName = field.getFilename();
    data.mimeType = field.getMimetype();
    data.size = field.getContent().getSize();

    InputStreamReader reader = new InputStreamReader(data.file);
    if (!reader.ready()) {
      reader.close();
      throw new EmptyFileException("empty file detected during file upload");
    }
  }

  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    logger.error(ERROR_OCCURRED, e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }

  private static class TranslationData {

    String defaultName = "";
    String mimeType = "";
    String author = "";
    String reference = "";
    String securityRanking = "";
    String statusProp = "";
    String keywords = "";
    String lang = "";
    Date expirationDate = null;
    I18nProperty title = null;
    I18nProperty description = null;
    InputStream file = null;
    Map<String, Object> dynProps = new HashMap<>();
    long size = 0L;
  }

  /**
   * Returns the content API used to create the translation node.
   *
   * @return the injected {@link ContentApi} instance
   */
  public ContentApi getContentApi() {
    return contentApi;
  }

  /**
   * Sets the content API used to create the translation node.
   *
   * @param contentApi the {@link ContentApi} instance to inject
   */
  public void setContentApi(ContentApi contentApi) {
    this.contentApi = contentApi;
  }

  /**
   * Sets the service used to check the current user's library permissions.
   *
   * @param service the {@link CurrentUserPermissionCheckerService} instance to inject
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService service
  ) {
    this.currentUserPermissionCheckerService = service;
  }
}
