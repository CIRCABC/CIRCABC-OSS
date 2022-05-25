/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog.keywords;

import eu.cec.digit.circabc.repo.keywords.KeywordEntry;
import eu.cec.digit.circabc.repo.keywords.KeywordImpl;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;


/**
 * Bean that backs the "Manage keywords" WAI page.
 *
 * @author Yanick Pignot
 */
public class ManageKeywordsDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "ManageKeywordsDialog";
    /**
     * The constant for the 'keyword' parameter
     */
    public static final String KEYWORD_PARAMETER = "keyword";
    protected static final String NULL_VALUE = "null";
    private static final long serialVersionUID = 7766640743571143699L;
    /**
     * Logger (coppepa: logger must be final)
     */
    private static final Log logger = LogFactory.getLog(ManageKeywordsDialog.class);

    protected List<Keyword> keywords;
    private SelectItem[] languages;
    private Integer itemToDisplay = 30;
    private List<SelectItem> numberOfItems;
    private String selectedLanguage = null;
    private UploadedFile importedFile;

    private transient KeywordsService keywordsService;
    private transient ContentFilterLanguagesService contentFilterLanguagesService;

    private boolean selectedAllKeywords;
    private boolean unusedFiltered = false;
    private List<String> selectedKeywords;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            initKeywords();
        }

        this.numberOfItems = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            numberOfItems.add(new SelectItem(i * 10, String.valueOf(i * 10)));
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // nothing to do
        return null;
    }

    @Override
    public void restored() {
        initKeywords();
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public String getBrowserTitle() {
        return translate("manage_keyword_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_keyword_dialog_icon_tooltip");
    }

    public Node getInterestGroup() {
        return getActionNode();
    }

    /**
     * Method calls by the dialog to getr the available langages.
     *
     * @return the list of languages where at least one keyword define
     */
    public SelectItem[] getLanguages() {
        if (languages == null) {
            // get the list of filter languages
            final List<String> languagesAsList = new ArrayList<>(30);

            for (final Keyword k : keywords) {
                if (k.isKeywordTranslated()) {
                    final MLText mlValues = k.getMLValues();
                    String lang = null;

                    for (final Map.Entry<Locale, String> entry : mlValues.entrySet()) {
                        lang = entry.getKey().getLanguage();
                        if (lang != null && !languagesAsList.contains(lang)) {
                            languagesAsList.add(lang);
                        }
                    }
                }
            }

            // set the item selection list
            SelectItem[] items = new SelectItem[languagesAsList.size() + 1];
            int idx = 0;

            // include the <All Languages>
            String allLanguagesStr = translate(UserPreferencesBean.MSG_CONTENTALLLANGUAGES);
            items[idx] = new SelectItem(NULL_VALUE, allLanguagesStr);
            idx++;

            for (String lang : languagesAsList) {
                String label = getContentFilterLanguagesService().getLabelByCode(lang);
                items[idx] = new SelectItem(lang, label);
                idx++;
            }

            return items;
        }

        return languages;

    }

    /**
     * @return the keywords
     */
    public List<KeywordWrapper> getKeywords() {
        List<KeywordWrapper> keys = null;

        if (selectedLanguage == null || selectedLanguage.equals(NULL_VALUE)) {
            keys = new ArrayList<>(keywords.size());

            for (final Keyword k : keywords) {
                String value = k.getMLValues().getValues().toString();

                keys.add(new KeywordWrapper(k.getId(), value.substring(1, value.length() - 1), null,
                        (k.getSelected() != null ? k.getSelected() : false)));

            }
        } else {
            keys = new ArrayList<>(keywords.size());

            for (final Keyword k : keywords) {
                final MLText mlValues = k.getMLValues();
                String value = null;

                for (final Map.Entry<Locale, String> entry : mlValues.entrySet()) {
                    if (entry.getKey().getLanguage().equalsIgnoreCase(selectedLanguage)) {
                        if (value == null) {
                            value = entry.getValue();
                            break;
                        }
                    }
                }

                if (value != null) {
                    keys.add(new KeywordWrapper(k.getId(), value, null,
                            (k.getSelected() != null ? k.getSelected() : false)));
                }
            }
        }
        return keys;
    }

    /**
     * Change listener for the method select box
     */
    public void updateList(ValueChangeEvent event) {
        // change in the language selector
        if (event.getComponent().getId().contains("language") && event.getNewValue() != null) {
            this.selectedLanguage = (String) event.getNewValue();
        }
        // change in the number selector
        else if (event.getComponent().getId().contains("number") && event.getNewValue() != null) {
            this.itemToDisplay = Integer.parseInt(event.getNewValue().toString());
        }
    }

    protected void initKeywords() {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to retreive the keyword list for the interest group : " + getInterestGroup()
                            .getName() + "(" + getInterestGroup().getNodeRef() + ")");
        }

        selectedAllKeywords = false;
        selectedLanguage = null;
        languages = null;
        keywords = getKeywordsService().getKeywords(getInterestGroup().getNodeRef());

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Keyword successfully retreived for the Interest Group : " + getInterestGroup().getName()
                            + "(" + getInterestGroup().getNodeRef() + ")"
                            + "\n    All Keywords" + keywords
                            + "\n    All languages " + Arrays.toString(getLanguages()));
        }
    }

    public String export(ActionEvent event) {
        Map<Integer, List<KeywordEntry>> data = generateExportTable();

        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        ServletOutputStream outStream = null;

        try {
            outStream = response.getOutputStream();

            response.setHeader("Content-Disposition", "attachment;filename=KeywordExport.xls");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");

            writeXLS(data, outStream);

            context.responseComplete();

        } catch (Exception ex) {

            logger
                    .error("Error during export of keywords in:" + this.getActionNode().getNodeRefAsString(),
                            ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

        return "";

    }

    private void writeXLS(Map<Integer, List<KeywordEntry>> data,
                          ServletOutputStream outStream) throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Integer index = 1, indexSheet = 1, iRow = 1;
        Integer nbKeyword = data.size();

        Sheet sheet = workbook.createSheet("Keywords" + indexSheet);
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Index");
        titleRow.createCell(1).setCellValue("Language");
        titleRow.createCell(2).setCellValue("Value");

        for (index = 1; index <= nbKeyword; index++) {
            List<KeywordEntry> lKey = data.get(index);

            for (KeywordEntry kEntry : lKey) {
                Row row = sheet.createRow(iRow);
                row.createCell(0).setCellValue(index);
                row.createCell(1).setCellValue(kEntry.getLanguage());
                row.createCell(2).setCellValue(kEntry.getValue());
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
        }

        workbook.write(outStream);

    }

    private Map<Integer, List<KeywordEntry>> generateExportTable() {

        Map<Integer, List<KeywordEntry>> mapResult = new HashMap<>();
        Integer iKey = 1;

        for (Keyword keyword : keywords) {
            if (keyword.getMLValues().size() > 0) {
                List<KeywordEntry> kList = new ArrayList<>();

                for (Entry<Locale, String> translation : keyword.getMLValues().entrySet()) {
                    kList.add(new KeywordEntry(translation.getKey().getLanguage(), translation.getValue()));
                }

                mapResult.put(iKey, kList);
                iKey++;
            }
        }

        return mapResult;
    }

    public String importFile(ActionEvent event) {
        if (importedFile != null) {
            if (importedFile.getName().matches(".*xls") || importedFile.getName().contains(".*xlsx")) {
                try {

                    ByteArrayInputStream is = new ByteArrayInputStream(importedFile.getBytes());
                    HSSFWorkbook wb = new HSSFWorkbook(is);

                    Map<Integer, List<KeywordEntry>> impData = prepareImportedData(wb);
                    Map<String, Integer> result = importKeywords(impData);
                    initKeywords();
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                            translate("manage_keywords_import_result", result.get("updates"),
                                    result.get("adds")));

                } catch (IOException e) {

                    Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                            translate("bulk_invite_error_durring_file_reading"));


                }
            } else {
                Utils.addStatusMessage(FacesMessage.SEVERITY_WARN,
                        translate("bulk_invite_unrecognised_file_type"));
            }
        }

        return "dialog:close";
    }

    private Map<String, Integer> importKeywords(Map<Integer, List<KeywordEntry>> impData) {

        List<Keyword> igKeywords = keywords;
        Map<String, Integer> result = new HashMap<>();
        Integer nbUpdates = 0, nbAdds = 0;

        for (Entry<Integer, List<KeywordEntry>> entry : impData.entrySet()) {
            List<KeywordEntry> lTmp = entry.getValue();
            Keyword keywordToUpdate = null;

            for (KeywordEntry kTmp : lTmp) {
                for (Keyword igKey : igKeywords) {
                    if (igKey.getMLValues().containsValue(kTmp.getValue())) {
                        keywordToUpdate = igKey;
                        break;
                    }
                }
            }

            if (keywordToUpdate != null) {
                for (KeywordEntry kEntry : lTmp) {
                    if (!keywordToUpdate.getMLValues().containsKey(new Locale(kEntry.getLanguage()))) {
                        keywordToUpdate.addTranlatation(new Locale(kEntry.getLanguage()), kEntry.getValue());
                        keywordsService.updateKeyword(keywordToUpdate);

                    }
                }

                nbUpdates++;
            } else {
                MLText mlText = new MLText();

                for (KeywordEntry kEntry : lTmp) {
                    mlText.addValue(new Locale(kEntry.getLanguage()), kEntry.getValue());
                }

                Keyword key = new KeywordImpl(mlText);

                key = getKeywordsService().createKeyword(
                        getActionNode().getNodeRef(),
                        key);

                nbAdds++;
            }
        }

        result.put("updates", nbUpdates);
        result.put("adds", nbAdds);

        return result;

    }

    private Map<Integer, List<KeywordEntry>> prepareImportedData(HSSFWorkbook wb) {

        Map<Integer, List<KeywordEntry>> result = new HashMap<>();

        if (wb != null) {
            for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
                Sheet sheet = wb.getSheetAt(iSheet);
                Integer index = -1;
                List<KeywordEntry> lTmp = new ArrayList<>();

                for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {
                    Row row = sheet.getRow(iRow);

                    if (!(row == null || row.getCell(0) == null)) {
                        Cell cell = row.getCell(0);

                        try {
                            String cellValue = "";
                            if (cell.getCellType() != 0) {
                                cellValue = cell.getStringCellValue();
                            } else {
                                cellValue = Double.toString(cell.getNumericCellValue());
                            }
                            Double db = Double.parseDouble(cellValue);
                            index = db.intValue();
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        if (!result.keySet().contains(index)) {
                            lTmp = new ArrayList<>();
                            result.put(index, lTmp);
                        } else {
                            lTmp = result.get(index);
                        }

                        String lang = "en", value = "";
                        if (row.getCell(1) != null) {
                            lang = row.getCell(1).toString();
                        }
                        if (row.getCell(2) != null) {
                            value = row.getCell(2).toString();
                        }

                        lTmp.add(new KeywordEntry(lang, value));
                    }
                }
            }
        }

        return result;
    }

    public void selectKeyword(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();

        String idKeyword = "";
        if (uLink.getParameterMap().get("idKeyword") != null) {
            idKeyword = uLink.getParameterMap().get("idKeyword");
        }

        Boolean all = false;

        if (uLink.getParameterMap().get("all") != null) {
            all = Boolean.valueOf(uLink.getParameterMap().get("all"));
            setSelectedAllKeywords(true);
        }

        for (Keyword tmpKeyword : keywords) {
            if (all) {
                tmpKeyword.setSelected(true);

            } else if (tmpKeyword.getId() != null) {
                if (tmpKeyword.getId().toString().equalsIgnoreCase(idKeyword)) {
                    tmpKeyword.setSelected(true);
                    break;
                }
            }
        }
    }

    public void unselectKeyword(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();

        String idKeyword = "";
        if (uLink.getParameterMap().get("idKeyword") != null) {
            idKeyword = uLink.getParameterMap().get("idKeyword");
        }

        Boolean all = false;

        if (uLink.getParameterMap().get("all") != null) {
            all = Boolean.valueOf(uLink.getParameterMap().get("all"));
            setSelectedAllKeywords(false);
        }

        for (Keyword tmpKeyword : keywords) {
            if (all) {
                tmpKeyword.setSelected(false);

            } else if (tmpKeyword.getId() != null) {
                if (tmpKeyword.getId().toString().equalsIgnoreCase(idKeyword)) {
                    tmpKeyword.setSelected(false);
                    break;
                }
            }
        }
    }

    /**
     * @return the selectedLanguage
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * @param selectedLanguage the selectedLanguage to set
     */
    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }


    /**
     * @return the keywordsService
     */
    protected final KeywordsService getKeywordsService() {
        if (keywordsService == null) {
            keywordsService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getKeywordsService();
        }
        return keywordsService;
    }

    /**
     * @param keywordsService the keywordsService to set
     */
    public final void setKeywordsService(KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
    }

    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (contentFilterLanguagesService == null) {
            contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentFilterLanguagesService();
        }
        return contentFilterLanguagesService;
    }

    /**
     * @param contentFilterLanguagesService the contentFilterLanguagesService to set
     */
    public final void setContentFilterLanguagesService(
            ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }

    /**
     * @return the itemToDisplay
     */
    public Integer getItemToDisplay() {
        return (itemToDisplay == null ? 10 : itemToDisplay);
    }

    /**
     * @param itemToDisplay the itemToDisplay to set
     */
    public void setItemToDisplay(Integer itemToDisplay) {

        if (itemToDisplay != null) {
            this.itemToDisplay = itemToDisplay;
        }
    }

    /**
     * @return the numberOfItems
     */
    public List<SelectItem> getNumberOfItems() {
        return numberOfItems;
    }

    /**
     * @param numberOfItems the numberOfItems to set
     */
    public void setNumberOfItems(List<SelectItem> numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    /**
     * @return the importedFile
     */
    public UploadedFile getImportedFile() {
        return importedFile;
    }

    /**
     * @param importedFile the importedFile to set
     */
    public void setImportedFile(UploadedFile importedFile) {
        this.importedFile = importedFile;
    }

    /**
     * @return the selectedAllKeywords
     */
    public boolean isSelectedAllKeywords() {
        return selectedAllKeywords;
    }

    /**
     * @param selectedAllKeywords the selectedAllKeywords to set
     */
    public void setSelectedAllKeywords(boolean selectedAllKeywords) {
        this.selectedAllKeywords = selectedAllKeywords;
    }

    public List<String> getSelectedKeywords() {

        List<String> selectedKeywords = new ArrayList<>();

        for (Keyword k : keywords) {
            if (k.getSelected() != null) {
                if (k.getSelected()) {
                    selectedKeywords.add(k.getId().toString());
                }
            }
        }

        return selectedKeywords;
    }

    public void setSelectedKeywords(List<String> selectedKeywords) {
        this.selectedKeywords = selectedKeywords;
    }

    public void listUnusedKeywords() {
        List<Keyword> keywordsTmp = new ArrayList<>();
        for (Keyword kTmp : keywords) {
            List<Keyword> lTmp = new ArrayList<>();
            lTmp.add(kTmp);

            if (keywordsService.getNodesForKeywords(this.getActionNode().getNodeRef(), lTmp).size()
                    == 0) {
                keywordsTmp.add(kTmp);
            }
        }

        keywords = keywordsTmp;
        unusedFiltered = true;
    }

    public void resetUnusedKeywords() {
        initKeywords();
        unusedFiltered = false;
    }

    public void downloadTemplate() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=template-keywords.xls");

        ServletOutputStream outStream = null;
        try {

            outStream = response.getOutputStream();

            Workbook workbook = new HSSFWorkbook();

            Sheet sheet = workbook.createSheet("Keywords");

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("index");
            titleRow.createCell(1).setCellValue("language");
            titleRow.createCell(2).setCellValue("value");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("en");
            row.createCell(2).setCellValue("keyword");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("1");
            row2.createCell(1).setCellValue("fr");
            row2.createCell(2).setCellValue("Mot-clé");

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("2");
            row3.createCell(1).setCellValue("en");
            row3.createCell(2).setCellValue("File");

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("2");
            row4.createCell(1).setCellValue("fr");
            row4.createCell(2).setCellValue("Fichier");

            workbook.write(outStream);

            context.responseComplete();

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during generating template", e);
            }
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

    }

    public boolean isUnusedFiltered() {
        return unusedFiltered;
    }

    public void setUnusedFiltered(boolean unusedFiltered) {
        this.unusedFiltered = unusedFiltered;
    }

}
