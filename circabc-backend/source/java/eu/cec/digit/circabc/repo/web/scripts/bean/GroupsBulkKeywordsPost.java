package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupsBulkKeywordsPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsBulkKeywordsPost.class);

    private KeywordsApi keywordsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static Map<Integer, KeywordDefinition> prepareImportedData(HSSFWorkbook wb) {

        Map<Integer, KeywordDefinition> result = new HashMap<>();

        if (wb != null) {
            for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
                Sheet sheet = wb.getSheetAt(iSheet);
                Integer index;

                for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {
                    Row row = sheet.getRow(iRow);

                    if (!((row == null) || (row.getCell(0) == null))) {
                        Cell cell = row.getCell(0);
                        KeywordDefinition keywordEntry = new KeywordDefinition();

                        try {
                            String cellValue;
                            if (cell.getCellType() != 0) {
                                cellValue = cell.getStringCellValue();
                            } else {
                                cellValue = Double.toString(cell.getNumericCellValue());
                            }
                            double db = Double.parseDouble(cellValue);
                            index = (int) db;
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        if (result.keySet().contains(index)) {
                            keywordEntry = result.get(index);
                        }

                        String lang = "en";
                        String value = "";
                        if (row.getCell(1) != null) {
                            lang = row.getCell(1).toString();
                        }
                        if (row.getCell(2) != null) {
                            value = row.getCell(2).toString();
                        }

                        if (!keywordEntry.getTitle().containsKey(lang)) {
                            keywordEntry.getTitle().put(lang, value);
                        }

                        result.put(index, keywordEntry);
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

        try {
            if (!this.currentUserPermissionCheckerService.isInterestGroupLibAdmin(id)) {
                throw new AccessDeniedException("Impossible to import keywords, not enough permissions");
            }

            InputStream fileInputStream = null;

            FormData form = (FormData) req.parseContent();

            if ((form == null) || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            for (FormData.FormField field : form.getFields()) {
                if (field.getIsFile()) {
                    fileInputStream = field.getInputStream();
                }
            }

            if (fileInputStream != null) {
                HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
                Map<Integer, KeywordDefinition> data = prepareImportedData(wb);
                this.importKeywords(id, data);
            }

            List<KeywordDefinition> list = this.keywordsApi.groupsIdKeywordsGet(id);
            model.put("keywords", list);

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } catch (IOException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Error with parsing the imported file");
            if (logger.isErrorEnabled()) {
                logger.error("Error with parsing the imported file", e);
            }
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
        return model;
    }

    private void importKeywords(String id, Map<Integer, KeywordDefinition> impData) {
        List<KeywordDefinition> igKeywords = this.keywordsApi.groupsIdKeywordsGet(id);

        for (Map.Entry<Integer, KeywordDefinition> entry : impData.entrySet()) {
            KeywordDefinition keywordEntry = entry.getValue();
            KeywordDefinition keywordToUpdate = null;

            for (String key : keywordEntry.getTitle().keySet()) {
                for (KeywordDefinition igKey : igKeywords) {
                    if (igKey.getTitle().containsKey(key)
                            && igKey.getTitle().get(key).equals(keywordEntry.getTitle().get(key))) {
                        keywordToUpdate = igKey;
                        break;
                    }
                }
            }

            if (keywordToUpdate != null) {
                for (String key : keywordEntry.getTitle().keySet()) {
                    if (!keywordToUpdate.getTitle().containsKey(key)) {
                        keywordToUpdate.getTitle().put(key, keywordEntry.getTitle().get(key));
                        this.keywordsApi.keywordsKeywordIdPut(keywordToUpdate.getId(), keywordToUpdate);
                    }
                }
            } else {
                this.keywordsApi.groupsIdKeywordsPost(id, keywordEntry);
            }
        }
    }

    /**
     * @return the keywordsApi
     */
    public KeywordsApi getKeywordsApi() {
        return this.keywordsApi;
    }

    /**
     * @param keywordsApi the keywordsApi to set
     */
    public void setKeywordsApi(KeywordsApi keywordsApi) {
        this.keywordsApi = keywordsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
