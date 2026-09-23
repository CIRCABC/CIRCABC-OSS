package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.exception.MaxFileSizeException;
import io.swagger.model.I18nProperty;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * REST webscript endpoint that handles HTTP {@code POST} requests to upload a
 * new content item (file) into a node in the CIRCABC repository.
 *
 * <p>The parent node is identified by the {@code id} URI template variable. The
 * request must be a {@code multipart/form-data} submission carrying the binary
 * file together with a set of metadata form fields, such as {@code name},
 * {@code author}, {@code reference}, {@code securityRanking}, {@code status},
 * {@code keywords}, {@code lang}, {@code isPivot}, {@code expirationDate},
 * {@code title}, {@code description} and {@code dynamicProperties}. The
 * optional {@code language} request parameter controls the locale used when
 * writing multilingual (ML) properties.</p>
 *
 * <p>Before creating the content the endpoint verifies that the current user
 * holds the {@link LibraryPermissions#LIBMANAGEOWN} permission on the target
 * node and enforces a maximum upload size of 300&nbsp;MB. On success the
 * response model exposes the identifier of the newly created node under the
 * {@code nodeRef} key. Errors are translated into appropriate HTTP status
 * codes (403 for access denial, 400 for oversized files and 500 for other
 * failures).</p>
 */
public class NodesContentUploadPost extends CircabcDeclarativeWebScript {

  /** Logger used to report errors raised while processing the upload. */
  static final Log logger = LogFactory.getLog(NodesContentUploadPost.class);

  /** Maximum allowed size for an uploaded file, in bytes (300&nbsp;MB). */
  private static final Long MAX_SIZE_UPLOAD = (long) (1024 * 1024 * 300);

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Autowired
  private ContentApi contentApi;

  @Autowired
  private ActionService actionService;

  /**
   * Processes the file upload request.
   *
   * <p>Resolves the target parent node from the {@code id} URI variable,
   * checks the caller's permissions, validates the multipart form and its
   * declared file size, then delegates content creation to
   * {@link ContentApi#createContent}. Multilingual awareness is toggled based
   * on the {@code language} request parameter and restored once processing
   * completes.</p>
   *
   * @param req the incoming webscript request; must be a multipart request and
   *            provide the {@code id} URI template variable
   * @param status the response status, updated with an HTTP error code and
   *               message when the upload fails
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code nodeRef} of the created content
   *         on success, or {@code null} when an error has been handled and the
   *         status set accordingly
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
      String id = req.getServiceMatch().getTemplateVars().get("id");
      validatePermission(id);
      FormData form = validateForm(req);
      UploadData data = parseFormData(form, id);
      validateFileSize(data.size);

      String finalName = data.name.equals(data.defaultName)
        ? data.defaultName
        : data.name;
      NodeRef fileRef = contentApi.createContent(
        id,
        finalName,
        data.title,
        data.description,
        data.author,
        data.reference,
        data.securityRanking,
        data.statusProp,
        data.keywords.toArray(new String[0]),
        data.expirationDate,
        data.mimeType,
        data.file,
        data.isPivot,
        data.lang,
        data.dynProps
      );
      model.put("nodeRef", fileRef.getId());
    } catch (AccessDeniedException e) {
      return handleError(
        status,
        Status.STATUS_FORBIDDEN,
        "Access denied for guest",
        e
      );
    } catch (IllegalArgumentException e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error - bad arguments",
        e
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

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

  private void validatePermission(String id) {
    if (
      !currentUserPermissionCheckerService.verifyMemberPermission(
        id,
        LibraryPermissions.LIBMANAGEOWN.toString()
      )
    ) {
      throw new AccessDeniedException("Not enough permission to upload a file");
    }
  }

  private FormData validateForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null || !form.getIsMultiPart()) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    return form;
  }

  private void validateFileSize(Long size) throws MaxFileSizeException {
    if (size > MAX_SIZE_UPLOAD) {
      throw new MaxFileSizeException("file is too big for the upload");
    }
  }

  private UploadData parseFormData(FormData form, String parentId)
    throws ParseException, IOException, java.text.ParseException {
    UploadData data = new UploadData();
    for (FormData.FormField field : form.getFields()) {
      processField(field, data, parentId);
    }
    return data;
  }

  private void processField(
    FormData.FormField field,
    UploadData data,
    String parentId
  ) throws ParseException, IOException, java.text.ParseException {
    switch (field.getName()) {
      case "name":
        data.name = field.getValue();
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
        data.keywords.add(field.getContent().getContent());
        break;
      case "lang":
        data.lang = field.getValue();
        break;
      case "isPivot":
        data.isPivot = Boolean.parseBoolean(field.getValue());
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
          processFileField(field, data, parentId);
        }
    }
  }

  private Date parseExpirationDate(FormData.FormField field)
    throws java.text.ParseException {
    String value = field.getValue();
    if (value == null || value.isEmpty() || "null".equals(value)) return null;
    @SuppressWarnings("java:S5361")
    String cleanValue = value.replaceAll("\"", "").trim();
    return Converter.convertStringToDate(cleanValue);
  }

  private I18nProperty parseI18nProperty(FormData.FormField field)
    throws ParseException, IOException {
    I18nProperty prop = new I18nProperty();
    JSONObject json = (JSONObject) new JSONParser().parse(
      Converter.getValue(field)
    );
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
    JSONObject json = (JSONObject) new JSONParser().parse(
      Converter.getValue(field)
    );
    @SuppressWarnings("unchecked")
    Set<String> keys = json.keySet();
    for (String key : keys) {
      dynProps.put(key, json.get(key));
    }
    return dynProps;
  }

  private void processFileField(
    FormData.FormField field,
    UploadData data,
    String parentId
  ) throws IOException {
    data.file = field.getInputStream();
    data.defaultName = field.getFilename();
    data.mimeType = field.getMimetype();
    data.size = field.getContent().getSize();

    InputStreamReader reader = new InputStreamReader(data.file);
    if (!reader.ready()) {
      reader.close();
      executeRulesAction(parentId);
    }
  }

  private void executeRulesAction(String parentId) {
    Action action = actionService.createAction(
      ExecuteAllRulesActionExecuter.NAME
    );
    action.setParameterValue(
      ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES,
      true
    );
    action.setExecuteAsynchronously(true);
    actionService.executeAction(
      action,
      Converter.createNodeRefFromId(parentId)
    );
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

  private static class UploadData {

    String name = "";
    String defaultName = "";
    String mimeType = "";
    String author = "";
    String reference = "";
    String securityRanking = "";
    String statusProp = "";
    String lang = "";
    List<String> keywords = new ArrayList<>();
    Date expirationDate = null;
    I18nProperty title = null;
    I18nProperty description = null;
    InputStream file = null;
    boolean isPivot = false;
    Map<String, Object> dynProps = new HashMap<>();
    Long size = 0L;
  }

  /**
   * Returns the service used to verify the current user's permissions.
   *
   * @return the configured {@link CurrentUserPermissionCheckerService}
   */
  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }
}
