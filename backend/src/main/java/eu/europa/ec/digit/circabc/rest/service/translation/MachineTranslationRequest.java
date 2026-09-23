package eu.europa.ec.digit.circabc.rest.service.translation;

/**
 * Data transfer object describing a single machine translation request submitted to the external
 * machine translation service (e.g. the European Commission's eTranslation / CEF automated
 * translation platform).
 *
 * <p>An instance carries all the parameters required to translate either an inline text snippet or
 * a document from a source language into a target language, along with the metadata used by the
 * translation backend for identification, prioritisation and asynchronous result delivery
 * (callbacks). It is a plain, mutable bean holding no business logic; instances are typically
 * populated by the CIRCABC translation service and serialised when invoking the remote translation
 * API.
 */
public class MachineTranslationRequest {

  /** Name of the calling application registered with the translation service. */
  private String applicationName;

  /** Identifier of the requesting department. */
  private String departmentNumber;

  /** The document content (typically Base64-encoded) to be translated. */
  private String documentToTranslate;

  /** Comma-separated list of domains hinting the subject matter for the translation. */
  private String domains;

  /** Callback URL invoked by the translation service when the request fails. */
  private String errorCallback;

  /** Caller-supplied reference used to correlate the request with the response. */
  private String externalReference;

  /** Institution on whose behalf the translation is requested. */
  private String institution;

  /** Original file name of the document to translate. */
  private String originalFileName;

  /** Requested output format of the translated result (e.g. the target MIME type or extension). */
  private String outputFormat;

  /** Processing priority of the request; higher values indicate more urgent handling. */
  private int priority;

  /** Callback URL invoked by the translation service to deliver the successful result. */
  private String requesterCallback;

  /** Type of translation request (for example text versus document). */
  private String requestType;

  /** Language code of the source content to translate from. */
  private String sourceLanguage;

  /** Language code of the target language to translate into. */
  private String targetLanguage;

  /** Repository path where the translated document should be stored. */
  private String targetTranslationPath;

  /** Inline text to be translated when no document is supplied. */
  private String textToTranslate;

  /** User name of the requester. */
  private String username;

  /**
   * Returns the name of the calling application.
   *
   * @return the application name
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * Sets the name of the calling application.
   *
   * @param applicationName the application name to set
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * Returns the identifier of the requesting department.
   *
   * @return the department number
   */
  public String getDepartmentNumber() {
    return departmentNumber;
  }

  /**
   * Sets the identifier of the requesting department.
   *
   * @param departmentNumber the department number to set
   */
  public void setDepartmentNumber(String departmentNumber) {
    this.departmentNumber = departmentNumber;
  }

  /**
   * Returns the document content to be translated.
   *
   * @return the document to translate (typically Base64-encoded)
   */
  public String getDocumentToTranslate() {
    return documentToTranslate;
  }

  /**
   * Sets the document content to be translated.
   *
   * @param documentToTranslate the document to translate (typically Base64-encoded)
   */
  public void setDocumentToTranslate(String documentToTranslate) {
    this.documentToTranslate = documentToTranslate;
  }

  /**
   * Returns the domains hinting the subject matter for the translation.
   *
   * @return a comma-separated list of domains
   */
  public String getDomains() {
    return domains;
  }

  /**
   * Sets the domains hinting the subject matter for the translation.
   *
   * @param domains a comma-separated list of domains to set
   */
  public void setDomains(String domains) {
    this.domains = domains;
  }

  /**
   * Returns the callback URL invoked when the translation request fails.
   *
   * @return the error callback URL
   */
  public String getErrorCallback() {
    return errorCallback;
  }

  /**
   * Sets the callback URL invoked when the translation request fails.
   *
   * @param errorCallback the error callback URL to set
   */
  public void setErrorCallback(String errorCallback) {
    this.errorCallback = errorCallback;
  }

  /**
   * Returns the caller-supplied reference used to correlate request and response.
   *
   * @return the external reference
   */
  public String getExternalReference() {
    return externalReference;
  }

  /**
   * Sets the caller-supplied reference used to correlate request and response.
   *
   * @param externalReference the external reference to set
   */
  public void setExternalReference(String externalReference) {
    this.externalReference = externalReference;
  }

  /**
   * Returns the institution on whose behalf the translation is requested.
   *
   * @return the institution
   */
  public String getInstitution() {
    return institution;
  }

  /**
   * Sets the institution on whose behalf the translation is requested.
   *
   * @param institution the institution to set
   */
  public void setInstitution(String institution) {
    this.institution = institution;
  }

  /**
   * Returns the original file name of the document to translate.
   *
   * @return the original file name
   */
  public String getOriginalFileName() {
    return originalFileName;
  }

  /**
   * Sets the original file name of the document to translate.
   *
   * @param originalFileName the original file name to set
   */
  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  /**
   * Returns the requested output format of the translated result.
   *
   * @return the output format
   */
  public String getOutputFormat() {
    return outputFormat;
  }

  /**
   * Sets the requested output format of the translated result.
   *
   * @param outputFormat the output format to set
   */
  public void setOutputFormat(String outputFormat) {
    this.outputFormat = outputFormat;
  }

  /**
   * Returns the processing priority of the request.
   *
   * @return the priority, where higher values indicate more urgent handling
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets the processing priority of the request.
   *
   * @param priority the priority to set, where higher values indicate more urgent handling
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns the callback URL used to deliver the successful translation result.
   *
   * @return the requester callback URL
   */
  public String getRequesterCallback() {
    return requesterCallback;
  }

  /**
   * Sets the callback URL used to deliver the successful translation result.
   *
   * @param requesterCallback the requester callback URL to set
   */
  public void setRequesterCallback(String requesterCallback) {
    this.requesterCallback = requesterCallback;
  }

  /**
   * Returns the type of translation request.
   *
   * @return the request type (for example text versus document)
   */
  public String getRequestType() {
    return requestType;
  }

  /**
   * Sets the type of translation request.
   *
   * @param requestType the request type to set (for example text versus document)
   */
  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  /**
   * Returns the language code of the source content.
   *
   * @return the source language code
   */
  public String getSourceLanguage() {
    return sourceLanguage;
  }

  /**
   * Sets the language code of the source content.
   *
   * @param sourceLanguage the source language code to set
   */
  public void setSourceLanguage(String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  /**
   * Returns the language code of the target language.
   *
   * @return the target language code
   */
  public String getTargetLanguage() {
    return targetLanguage;
  }

  /**
   * Sets the language code of the target language.
   *
   * @param targetLanguage the target language code to set
   */
  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  /**
   * Returns the repository path where the translated document should be stored.
   *
   * @return the target translation path
   */
  public String getTargetTranslationPath() {
    return targetTranslationPath;
  }

  /**
   * Sets the repository path where the translated document should be stored.
   *
   * @param targetTranslationPath the target translation path to set
   */
  public void setTargetTranslationPath(String targetTranslationPath) {
    this.targetTranslationPath = targetTranslationPath;
  }

  /**
   * Returns the inline text to be translated.
   *
   * @return the text to translate
   */
  public String getTextToTranslate() {
    return textToTranslate;
  }

  /**
   * Sets the inline text to be translated.
   *
   * @param textToTranslate the text to translate to set
   */
  public void setTextToTranslate(String textToTranslate) {
    this.textToTranslate = textToTranslate;
  }

  /**
   * Returns the user name of the requester.
   *
   * @return the user name
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the user name of the requester.
   *
   * @param username the user name to set
   */
  public void setUsername(String username) {
    this.username = username;
  }
}
