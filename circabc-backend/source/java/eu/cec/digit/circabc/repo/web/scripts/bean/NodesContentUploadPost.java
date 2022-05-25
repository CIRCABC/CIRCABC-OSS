package eu.cec.digit.circabc.repo.web.scripts.bean;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.ContentApi;
import io.swagger.exception.EmptyFileException;
import io.swagger.exception.MaxFileSizeException;
import io.swagger.model.I18nProperty;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;

/**
 * @author beaurpi
 */
public class NodesContentUploadPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(NodesContentUploadPost.class);

    private static final Long MAX_SIZE_UPLOAD = (long) (1024 * 1024 * 300);
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private ContentApi contentApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String language = req.getParameter("language");
        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        try {

            String id = templateVars.get("id");

            if (!this.currentUserPermissionCheckerService.verifyMemberPermission(id,
                    LibraryPermissions.LIBMANAGEOWN.toString())) {
                throw new AccessDeniedException("Not enough permission to upload a file");
            }

            FormData form = (FormData) req.parseContent();

            if ((form == null) || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            String name = "";
            String defaultName = "";
            String mimeType = "";
            String author = "";
            String reference = "";
            String securityRanking = "";
            String statusProp = "";
            List<String> keywords = new ArrayList<>();
            Date expirationDate = null;
            I18nProperty title = null;
            I18nProperty description = null;
            InputStream file = null;
            boolean isPivot = false;
            String lang = "";
            Map<String, Object> dynProps = new HashMap<>();

            Long size = 0L;

            for (FormData.FormField field : form.getFields()) {
                if (field.getName().equals("name")) {
                    name = field.getValue();
                } else if (field.getName().equals("author")) {
                    author = field.getValue();
                } else if (field.getName().equals("reference")) {
                    reference = field.getValue();
                } else if (field.getName().equals("securityRanking")) {
                    securityRanking = field.getValue();
                } else if (field.getName().equals("status")) {
                    statusProp = field.getValue();
                } else if (field.getName().equals("keywords")) {
                    String keyword = field.getContent().getContent();
                    keywords.add(keyword);
                } else if (field.getName().equals("lang")) {
                    lang = field.getValue();
                } else if (field.getName().equals("isPivot")) {
                    isPivot = Boolean.parseBoolean(field.getValue());
                } else if (field.getName().equals("expirationDate") && field.getValue() != null
                        && !"".equals(field.getValue()) && !"null".equals(field.getValue())) {
                    @SuppressWarnings("java:S5361")
                    String value = field.getValue().replaceAll("\"", "").trim();
                    expirationDate = Converter.convertStringToDate(value);
                } else if (field.getName().equals("title")) {
                    I18nProperty titleI18N = new I18nProperty();
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(Converter.getValue(field));
                    if (json != null) {
                        for (String code : SupportedLanguages.availableLangCodes) {
                            if (json.containsKey(code)) {
                                titleI18N.put(code, String.valueOf(json.get(code)));
                            }
                        }
                    }
                    title = titleI18N;
                } else if (field.getName().equals("description")) {
                    I18nProperty titleI18N = new I18nProperty();
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(Converter.getValue(field));
                    if (json != null) {
                        for (String code : SupportedLanguages.availableLangCodes) {
                            if (json.containsKey(code)) {
                                titleI18N.put(code, String.valueOf(json.get(code)));
                            }
                        }
                    }
                    description = titleI18N;
                } else if (field.getName().equals("dynamicProperties")) {
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(Converter.getValue(field));
                    Set<String> keys = json.keySet();
                    for (String key : keys) {
                        dynProps.put(key, json.get(key));
                    }
                } else if (field.getIsFile()) {
                    file = field.getInputStream();
                    defaultName = field.getFilename();
                    mimeType = field.getMimetype();
                    size = field.getContent().getSize();

                    InputStreamReader reader = new InputStreamReader(file);
                    if (!reader.ready()) {
                        reader.close();
                        throw new EmptyFileException("empty file detected during file upload");
                    }
                }

            }

            if (size > MAX_SIZE_UPLOAD) {
                throw new MaxFileSizeException("file is too big for the upload");
            }

            NodeRef fileRef = contentApi.createContent(id, defaultName, title, description, author, reference,
                    securityRanking, statusProp, keywords.toArray(new String[0]), expirationDate, mimeType, file,
                    isPivot, lang, dynProps);

            model.put("nodeRef", fileRef.getId());

        } catch (AccessDeniedException e) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied for guest");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (IllegalArgumentException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error - bad arguments");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (MaxFileSizeException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Maximum file size for the upload");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error during upload file");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public void setContentApi(ContentApi contentApi) {
        this.contentApi = contentApi;
    }
}
