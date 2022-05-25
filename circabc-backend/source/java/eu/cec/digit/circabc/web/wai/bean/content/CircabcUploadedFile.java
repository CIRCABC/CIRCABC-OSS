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
package eu.cec.digit.circabc.web.wai.bean.content;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copy paste of Alfresco Upload Bean that hold custom properties submited by user (Notifify,
 * language, editProps, ..)
 *
 * @author Yanick Pignot
 */
public class CircabcUploadedFile implements Serializable {

    public static final String IS_PIVOT = "isPivot";
    public static final String IS_TRANSLATION = "isTranslation";
    public static final String MAIN_LANGUAGE = "isPivot_mainLanguage";
    public static final String MAIN_LANGUAGE_TITLE = "isPivot_mainLanguage_Title";
    public static final String IS_TRANSLATION_OF_DOCUMENT_TITLE = "isTranslationOf";
    /*
     * known file extensions -> means we have a gif images to render it.
     */
    public static final String validExtension = "acp|asf|avi|bmp|csv|doc|docx|eml|exe|ftl|gif|htm|html|jp2|jpe|jpeg|jpg|jpm|jpx|js|lnk|mp2|mp3|mp4|mpeg|mpg|msg|odf|odg|odp|ods|odt|pdf|png|ppt|pptx|psd|rtf|shtml|swf|tif|tiff|txt|url|wmv|wpd|xdp|xls|xlsx|xml|xsd|xsl|zip";
    public static final String defaultExtension = "_default";
    public static final String LANGUAGE = "content-language";
    public static final String DISABLE_NOTIF = "check-disable-notif";
    public static final String EDIT_PROPS = "edit-properties";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String AUTHOR = "author";
    public static final String NAME = "name";
    public static final String REFERENCE = "reference";
    public static final String SECURITY_RANKING = "security_ranking";
    public static final String ISSUE_DATE = "issue_date";
    public static final String EXPIRATION_DATE = "expiration_date";
    public static final String KEYWORD = "keyword";
    public static final String ENCRYPTED = "encrypted";
    public static final String FILE_UPLOAD_BEAN_NAME = "circabc..UploadedFileBean";
    private static final String IMAGES_FILETYPES32 = "/images/filetypes32/";
    private static final long serialVersionUID = 7666574984924957544L;
    private File file;
    private String fileName;
    private String filePath;
    private Map<String, String> submitedProperties = new HashMap<>();
    private List<String> keywords;
    private Date issueDate;
    private Date expirationDate;

    public static String getKey(final String id) {
        return ((id == null || id.length() == 0)
                ? FILE_UPLOAD_BEAN_NAME
                : FILE_UPLOAD_BEAN_NAME + "-" + id);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * @return the submitedProperties
     */
    public Map<String, String> getSubmitedProperties() {
        return submitedProperties;
    }

    /**
     * @param submitedProperties the submitedProperties to set
     */
    public void setSubmitedProperties(Map<String, String> submitedProperties) {
        this.submitedProperties = submitedProperties;
    }

    /**
     * @return Returns the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file The file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public String getResolveFileSize() {
        return humanReadableByteCount(file.length(), true);
    }

    /**
     * @return Returns the name of the file uploaded
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName The name of the uploaded file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return Returns the path of the file uploaded
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath The file path of the uploaded file
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @param key
     * @param value
     */
    public void addSubmitedProperty(final String key, final String value) {
        submitedProperties.put(key, value);
    }

    /**
     * @param key
     * @return
     */
    public String getSubmitedProperty(final String key) {
        return submitedProperties.get(key);
    }

    public String getLanguage() {
        return getSubmitedProperty(LANGUAGE);
    }

    public boolean isNotificationDisabled() {
        return Boolean.parseBoolean(getSubmitedProperty(DISABLE_NOTIF));
    }

    public boolean isEditPropertiesAfter() {
        return Boolean.parseBoolean(getSubmitedProperty(EDIT_PROPS));
    }

    public String getExtension() {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public String getPreparedExtension() {
        String result = IMAGES_FILETYPES32;

        if (validExtension.contains(getExtension())) {
            result += getExtension() + ".gif";
        } else {
            result += defaultExtension + ".gif";
        }

        return result;
    }

    public Boolean getIsPivotMultilingual() {
        return Boolean.valueOf(this.submitedProperties.get(IS_PIVOT));
    }

    public String getLanguageName() {
        return this.submitedProperties.get(MAIN_LANGUAGE);
    }

    public String getLanguageTitle() {
        return this.submitedProperties.get(MAIN_LANGUAGE_TITLE);
    }

    public Boolean getIsTranslation() {
        return Boolean.valueOf(this.submitedProperties.get(IS_TRANSLATION));
    }

    public String getIsTranslationOfDocument() {
        return this.submitedProperties.get(IS_TRANSLATION_OF_DOCUMENT_TITLE);
    }

    public void removeSubmitedProperty(String property) {

        this.submitedProperties.remove(property);
    }

    /**
     * @return the keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * @return the issue_date
     */
    public Date getIssueDate() {
        return issueDate;
    }

    /**
     * @param issue_date the issue_date to set
     */
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    /**
     * @return the expiration_date
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expiration_date the expiration_date to set
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
