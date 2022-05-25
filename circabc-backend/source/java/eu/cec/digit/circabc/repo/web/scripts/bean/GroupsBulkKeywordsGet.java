package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.KeywordsApi;
import io.swagger.model.I18nProperty;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupsBulkKeywordsGet extends AbstractWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsBulkKeywordsGet.class);

    private KeywordsApi keywordsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static void writeXLS(List<KeywordDefinition> data, OutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        int index = 1;
        int indexSheet = 1;
        int iRow = 1;

        Sheet sheet = workbook.createSheet("Keywords" + indexSheet);
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Index");
        titleRow.createCell(1).setCellValue("Language");
        titleRow.createCell(2).setCellValue("Value");

        for (KeywordDefinition keyword : data) {

            I18nProperty title = keyword.getTitle();

            for (Map.Entry<String, String> entry : title.entrySet()) {
                Row row = sheet.createRow(iRow);
                row.createCell(0).setCellValue(index);
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
                iRow++;
            }

            if (iRow > 1048576) {
                sheet = workbook.createSheet("Keywords" + indexSheet);
                Row titleRow2 = sheet.createRow(0);
                titleRow2.createCell(0).setCellValue("Index");
                titleRow2.createCell(1).setCellValue("Language");
                titleRow2.createCell(2).setCellValue("Value");
                iRow = 1;
            }

            index++;
        }

        workbook.write(outStream);
    }

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

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
            this.currentUserPermissionCheckerService.throwIfCanNotAccessInterestGroup(id);
            List<KeywordDefinition> list = this.keywordsApi.groupsIdKeywordsGet(id);
            writeXLS(list, res.getOutputStream());
        } catch (AccessDeniedException ade) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            throw new IOException("Could not export keywords.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
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
