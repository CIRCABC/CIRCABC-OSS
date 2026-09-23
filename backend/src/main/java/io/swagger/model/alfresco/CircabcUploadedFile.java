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
package io.swagger.model.alfresco;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializable bean that holds an uploaded file together with the custom, user-submitted
 * properties that drive how the file is stored in the Alfresco repository.
 *
 * <p>Originally derived from Alfresco's own upload bean, it carries the temporary {@link File}
 * payload, its name and path, plus an arbitrary map of submitted properties (notification
 * preference, content language, "edit properties after upload" flag, multilingual/translation
 * hints, etc.). It also exposes a handful of derived accessors that interpret those properties
 * and resolve presentation details such as the file-type icon and a human-readable size.
 *
 * <p>Instances are typically cached under a key produced by {@link #getKey(String)} while an
 * upload is being processed.
 *
 * @author Yanick Pignot
 */
public class CircabcUploadedFile implements Serializable {

  /** Property key flagging that the file is the pivot document of a multilingual set. */
  public static final String IS_PIVOT = "isPivot";
  /** Property key flagging that the file is a translation of another document. */
  public static final String IS_TRANSLATION = "isTranslation";
  /** Property key holding the main (pivot) language code of a multilingual set. */
  public static final String MAIN_LANGUAGE = "isPivot_mainLanguage";
  /** Property key holding the title associated with the main (pivot) language. */
  public static final String MAIN_LANGUAGE_TITLE = "isPivot_mainLanguage_Title";
  /** Property key identifying the document that this upload is a translation of. */
  public static final String IS_TRANSLATION_OF_DOCUMENT_TITLE =
    "isTranslationOf";
  /*
   * known file extensions -> means we have a gif images to render it.
   */
  /**
   * Pipe-separated list of file extensions for which a dedicated file-type icon (gif) exists.
   * Any extension not present here falls back to {@link #DEFAULT_EXTENSION}.
   */
  public static final String VALID_FILE_EXTENSIONS =
    "acp|asf|avi|bmp|csv|doc|docx|eml|exe|ftl|gif|htm|html|jp2|jpe|jpeg|jpg|jpm|jpx|js|lnk|mp2|mp3|mp4|mpeg|mpg|msg|odf|odg|odp|ods|odt|pdf|png|ppt|pptx|psd|rtf|shtml|swf|tif|tiff|txt|url|wmv|wpd|xdp|xls|xlsx|xml|xsd|xsl|zip";
  /** Fallback icon name used when the file extension has no dedicated icon. */
  public static final String DEFAULT_EXTENSION = "_default";
  /** Property key for the content language of the uploaded file. */
  public static final String LANGUAGE = "content-language";
  /** Property key indicating that notifications should be suppressed for this upload. */
  public static final String DISABLE_NOTIF = "check-disable-notif";
  /** Property key indicating that the user wants to edit properties after uploading. */
  public static final String EDIT_PROPS = "edit-properties";
  /** Property key for the document title. */
  public static final String TITLE = "title";
  /** Property key for the document description. */
  public static final String DESCRIPTION = "description";
  /** Property key for the document status. */
  public static final String STATUS = "status";
  /** Property key for the document author. */
  public static final String AUTHOR = "author";
  /** Property key for the document name. */
  public static final String NAME = "name";
  /** Property key for the document reference. */
  public static final String REFERENCE = "reference";
  /** Property key for the document security ranking. */
  public static final String SECURITY_RANKING = "security_ranking";
  /** Property key for the document issue date. */
  public static final String ISSUE_DATE = "issue_date";
  /** Property key for the document expiration date. */
  public static final String EXPIRATION_DATE = "expiration_date";
  /** Property key for a document keyword. */
  public static final String KEYWORD = "keyword";
  /** Property key indicating whether the document is encrypted. */
  public static final String ENCRYPTED = "encrypted";
  /** Base name under which the upload bean is cached; see {@link #getKey(String)}. */
  public static final String FILE_UPLOAD_BEAN_NAME =
    "circabc..UploadedFileBean";
  /** Base path under which file-type icons (gif) are located. */
  private static final String IMAGES_FILETYPES32 = "/images/filetypes32/";

  private static final long serialVersionUID = 7666574984924957544L;
  /** The temporary file holding the uploaded content. */
  private File file;
  /** The original name of the uploaded file. */
  private String fileName;
  /** The repository path where the file should be stored. */
  private String filePath;
  /** Custom properties submitted by the user, keyed by the constants declared in this class. */
  private Map<String, String> submitedProperties = new HashMap<>();
  /** Keywords associated with the uploaded document. */
  private List<String> keywords;
  /** The issue date associated with the uploaded document. */
  private Date issueDate;
  /** The expiration date associated with the uploaded document. */
  private Date expirationDate;

  /**
   * Builds the cache key for an upload bean associated with the given identifier.
   *
   * @param id the upload identifier; may be {@code null} or empty
   * @return {@link #FILE_UPLOAD_BEAN_NAME} when {@code id} is {@code null}/empty, otherwise
   *     {@link #FILE_UPLOAD_BEAN_NAME} suffixed with {@code "-" + id}
   */
  public static String getKey(final String id) {
    return (
      (id == null || id.isEmpty())
        ? FILE_UPLOAD_BEAN_NAME
        : FILE_UPLOAD_BEAN_NAME + "-" + id
    );
  }

  /**
   * Converts a byte count into a human-readable string using either SI (base 1000) or binary
   * (base 1024) units.
   *
   * @param bytes the size in bytes
   * @param si {@code true} to use SI units (kB, MB, ...), {@code false} for binary units (KiB
   *     conventions using base 1024)
   * @return a formatted, human-readable size string (e.g. {@code "1.5 MB"})
   */
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

  /**
   * Returns the size of the underlying {@link #file} formatted as a human-readable string using
   * SI units.
   *
   * @return the human-readable file size
   */
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
   * Adds or replaces a single submitted property.
   *
   * @param key the property key (typically one of the constants declared in this class)
   * @param value the property value
   */
  public void addSubmitedProperty(final String key, final String value) {
    submitedProperties.put(key, value);
  }

  /**
   * Returns the value of a single submitted property.
   *
   * @param key the property key
   * @return the property value, or {@code null} if not present
   */
  public String getSubmitedProperty(final String key) {
    return submitedProperties.get(key);
  }

  /**
   * @return the submitted content language, or {@code null} if not set
   */
  public String getLanguage() {
    return getSubmitedProperty(LANGUAGE);
  }

  /**
   * @return {@code true} if the user requested notifications to be suppressed for this upload
   */
  public boolean isNotificationDisabled() {
    return Boolean.parseBoolean(getSubmitedProperty(DISABLE_NOTIF));
  }

  /**
   * @return {@code true} if the user requested to edit the document properties after uploading
   */
  public boolean isEditPropertiesAfter() {
    return Boolean.parseBoolean(getSubmitedProperty(EDIT_PROPS));
  }

  /**
   * Extracts the lower-cased file extension from {@link #fileName}.
   *
   * @return the file extension without the leading dot, in lower case
   */
  public String getExtension() {
    return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
  }

  /**
   * Resolves the path to the file-type icon (gif) that represents this file's extension, falling
   * back to the default icon when the extension is not among {@link #VALID_FILE_EXTENSIONS}.
   *
   * @return the icon path under {@link #IMAGES_FILETYPES32}
   */
  public String getPreparedExtension() {
    String result = IMAGES_FILETYPES32;

    if (VALID_FILE_EXTENSIONS.contains(getExtension())) {
      result += getExtension() + ".gif";
    } else {
      result += DEFAULT_EXTENSION + ".gif";
    }

    return result;
  }

  /**
   * @return {@code true} if this file is flagged as the pivot document of a multilingual set
   */
  public Boolean getIsPivotMultilingual() {
    return Boolean.valueOf(this.submitedProperties.get(IS_PIVOT));
  }

  /**
   * @return the main (pivot) language code of the multilingual set, or {@code null} if not set
   */
  public String getLanguageName() {
    return this.submitedProperties.get(MAIN_LANGUAGE);
  }

  /**
   * @return the title associated with the main (pivot) language, or {@code null} if not set
   */
  public String getLanguageTitle() {
    return this.submitedProperties.get(MAIN_LANGUAGE_TITLE);
  }

  /**
   * @return {@code true} if this file is flagged as a translation of another document
   */
  public Boolean getIsTranslation() {
    return Boolean.valueOf(this.submitedProperties.get(IS_TRANSLATION));
  }

  /**
   * @return the identifier of the document this upload is a translation of, or {@code null} if
   *     not set
   */
  public String getIsTranslationOfDocument() {
    return this.submitedProperties.get(IS_TRANSLATION_OF_DOCUMENT_TITLE);
  }

  /**
   * Removes a single submitted property.
   *
   * @param property the key of the property to remove
   */
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
   * @param issueDate the issue_date to set
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
   * @param expirationDate the expiration_date to set
   */
  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }
}
