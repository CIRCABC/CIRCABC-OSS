/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.translation;

import com.google.common.base.Joiner;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.db.Request;
import io.swagger.model.db.SearchResult;
import io.swagger.model.db.SearchResultNotify;
import io.swagger.util.PathUtils;
import jakarta.mail.MessagingException;
import java.io.*;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link TranslationService}.
 *
 * <p>This service integrates CIRCABC with the European Commission's DGT machine translation (MT)
 * back-end. Its responsibilities include:
 *
 * <ul>
 *   <li>Submitting translation requests for both single node properties ({@link
 *       #translateProperty}) and whole documents ({@link #translateDocument}) by building a {@link
 *       MachineTranslationRequest} and dispatching it through the {@link MachineTranslationService}.
 *   <li>Persisting every outgoing request via the {@link TranslationDaoService} so the originating
 *       server can later reconcile the asynchronous MT callbacks.
 *   <li>Preparing the temporary Alfresco folder structure and copying the source document to a
 *       location reachable by the MT service ({@link #copyDocumentToBeTranslated}).
 *   <li>Processing translated results returned by the MT service ({@link #processTranslatedFiles}):
 *       updating multilingual documents/properties in the repository and notifying the requesting
 *       users by e-mail.
 *   <li>Exposing configuration helpers such as the set of supported languages and file extensions,
 *       the maximum translatable file size and the synthetic MT service user account.
 * </ul>
 *
 * <p>Repository mutations that must run with elevated rights are executed through {@link
 * AuthenticationUtil#runAs} as the system user. Collaborators (Alfresco and CIRCABC services) are
 * injected by Spring; configuration values are resolved from {@link CircabcConfig} in {@link
 * #init()}.
 */
public class TranslationServiceImpl implements TranslationService {

  private static final String TRANSLATIONS = "translations";
  private static final String REQUEST_ID = "?requestId=";
  private static final String NOTIFY_SUCCESS_PATH = "/notifySuccess";
  private static final String NOTIFY_ERROR_PATH = "/notifyError";
  private static final Log logger = LogFactory.getLog(
    TranslationServiceImpl.class
  );

  @Autowired
  private CircabcConfig circabcConfig;

  private MachineTranslationService machineTranslationService;
  private String webRootUrl;

  private String callbackUrl;
  private String mtUserName;
  private String mtPassword;

  private String applicationName;

  /**
   * Initialises the runtime configuration read from {@link CircabcConfig}. This is typically wired
   * as the Spring bean {@code init-method} and populates the web root URL, the MT callback URL, the
   * MT service credentials and the MT application name.
   */
  public void init() {
    webRootUrl = circabcConfig.getWebRootUrl();

    callbackUrl = circabcConfig.getMTCallbackUrl();
    mtUserName = circabcConfig.getMtUsername();
    mtPassword = circabcConfig.getMtPassword();

    applicationName = circabcConfig.getMTApplicationName();
  }

  private UserService userService;
  private Set<String> languages;
  private Set<String> fileExtensions;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private String ftpUrl;
  private String mtRootSpace;
  private AuthenticationService authenticationService;
  private ContentService contentService;
  private MultilingualContentService multilingualContentService;
  private TranslationDaoService translationDaoService;
  private OwnableService ownableService;
  private DictionaryService dictionaryService;
  private MailService mailService;
  private MailPreferencesService mailPreferencesService;
  private PersonService personService;
  private ActionService actionService;
  private long maxFileSizeInBytes;

  private static String replaceLast(
    String string,
    String substring,
    String replacement
  ) {
    int index = string.lastIndexOf(substring);
    if (index == -1) {
      return string;
    }
    return (
      string.substring(0, index) +
      replacement +
      string.substring(index + substring.length())
    );
  }

  /**
   * @return the user service used to resolve CIRCABC user details
   */
  public UserService getUserService() {
    return userService;
  }

  /**
   * @param userService the user service to set
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * @return the Alfresco authentication service
   */
  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  /**
   * @param authenticationService the Alfresco authentication service to set
   */
  public void setAuthenticationService(
    AuthenticationService authenticationService
  ) {
    this.authenticationService = authenticationService;
  }

  /**
   * @return the set of language codes supported for translation
   */
  public Set<String> getLanguages() {
    return languages;
  }

  /**
   * @param languages the set of supported language codes to set
   */
  public void setLanguages(Set<String> languages) {
    this.languages = languages;
  }

  /**
   * {@inheritDoc}
   *
   * @return the set of language codes supported for machine translation
   */
  @Override
  public Set<String> getAvailableLanguages() {
    return getLanguages();
  }

  /**
   * @return the DAO service used to persist and query translation requests
   */
  public TranslationDaoService getTranslationDaoService() {
    return translationDaoService;
  }

  /**
   * @param translationDaoService the translation DAO service to set
   */
  public void setTranslationDaoService(
    TranslationDaoService translationDaoService
  ) {
    this.translationDaoService = translationDaoService;
  }

  /**
   * Submits a machine-translation request for a single textual node property.
   *
   * <p>The current property value is extracted (supporting both {@link String} and {@link MLText}
   * values in the given source language), a request record is persisted for later reconciliation of
   * the asynchronous callback, and a {@link MachineTranslationRequest} of type {@code txt} is sent
   * to the MT service. Optionally the requesting user is notified by e-mail once the translation
   * completes.
   *
   * @param nodeRef the node whose property must be translated
   * @param property the qualified name of the property to translate
   * @param sourceLanguage the ISO language code of the source text
   * @param languages the set of target language codes to translate into
   * @param notifyUserByEmail {@code true} to e-mail the requester when the translation is ready
   * @throws IllegalArgumentException if the property value is neither a {@link String} nor {@link
   *     MLText}
   */
  @Override
  public void translateProperty(
    NodeRef nodeRef,
    org.alfresco.service.namespace.QName property,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  ) {
    String externalReference = GUID.generate();
    String username = getAuthenticationService().getCurrentUserName();
    CircabcUserDataBean circabcUserDataBean =
      getUserService().getCircabcUserDataBean(username);

    Serializable propertyValue = getNodeService().getProperty(
      nodeRef,
      property
    );
    String textToTranslate = switch (propertyValue) {
      case MLText mlText -> mlText.getValue(Locale.of(sourceLanguage));
      case String str -> str;
      default -> throw new IllegalArgumentException(
        "property " +
          property.toPrefixString() +
          " of node " +
          nodeRef.toString() +
          " is not String or MLText"
      );
    };
    String targetTranslationPath = "none";
    String email = "";
    if (notifyUserByEmail) {
      email = circabcUserDataBean.getEmail();
    }

    String departmentNumber = circabcUserDataBean.getOrgdepnumber();
    String documentToTranslate = "";
    String domains = "all";
    final String translationCallbackUrl = getCallbackURL();
    String errorCallback =
      translationCallbackUrl +
      NOTIFY_ERROR_PATH +
      REQUEST_ID +
      externalReference;
    String institution = circabcUserDataBean.getDomain();
    String originalFileName = "";
    String outputFormat = "";
    int priority = 6;
    String requesterCallback =
      translationCallbackUrl +
      NOTIFY_SUCCESS_PATH +
      REQUEST_ID +
      externalReference;
    String requestType = "txt";
    String targetLanguage = Joiner.on(",").join(
      Objects.requireNonNull(languages)
    );

    // save in database ftpURL so we know from what serves request was send
    TranslationSaveRequest saveReq = new TranslationSaveRequest();
    saveReq.setNodeRef(nodeRef);
    saveReq.setProperty(property);
    saveReq.setSourceLanguage(sourceLanguage);
    saveReq.setExternalReference(externalReference);
    saveReq.setUsername(username);
    saveReq.setTextToTranslate(textToTranslate);
    saveReq.setTargetTranslationPath(ftpUrl);
    saveReq.setTargetLanguage(targetLanguage);
    saveReq.setNotify(notifyUserByEmail);
    saveReq.setEmail(email);
    savePropertyRequest(saveReq);

    MachineTranslationRequest mtRequest = new MachineTranslationRequest();
    mtRequest.setApplicationName(applicationName);
    mtRequest.setDepartmentNumber(departmentNumber);
    mtRequest.setDocumentToTranslate(documentToTranslate);
    mtRequest.setDomains(domains);
    mtRequest.setErrorCallback(errorCallback);
    mtRequest.setExternalReference(externalReference);
    mtRequest.setInstitution(institution);
    mtRequest.setOriginalFileName(originalFileName);
    mtRequest.setOutputFormat(outputFormat);
    mtRequest.setPriority(priority);
    mtRequest.setRequesterCallback(requesterCallback);
    mtRequest.setRequestType(requestType);
    mtRequest.setSourceLanguage(sourceLanguage);
    mtRequest.setTargetLanguage(targetLanguage);
    mtRequest.setTargetTranslationPath(targetTranslationPath);
    mtRequest.setTextToTranslate(textToTranslate);
    mtRequest.setUsername(username);
    machineTranslationService.sendMessage(mtRequest);
  }

  private void savePropertyRequest(TranslationSaveRequest req) {
    Request request = new Request();
    request.setUsername(req.getUsername());
    request.setDocURL("");
    request.setReqDate(new Date());
    request.setRequestID(req.getExternalReference());
    request.setSourceLang(req.getSourceLanguage());
    request.setTargetLangs(req.getTargetLanguage());
    request.setTargetPath(req.getTargetTranslationPath());
    request.setText(req.getTextToTranslate());
    request.setDocumentID(req.getNodeRef().toString());
    request.setPropertyQName(req.getProperty().toString());
    request.setNotify(req.isNotify());
    request.setEmail(req.getEmail());

    try {
      translationDaoService.saveRequest(request);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when saving request", e);
      }
    }
  }

  /**
   * Submits a machine-translation request for a whole document.
   *
   * <p>Running as the system user, the method resolves the FTP paths of the copied source document
   * and of the target {@code translations} folder, persists the request for later reconciliation of
   * the asynchronous callback, and sends a {@link MachineTranslationRequest} of type {@code doc} to
   * the MT service. Optionally the requesting user is notified by e-mail once the translation
   * completes.
   *
   * @param origianalDocument the original document node stored against the persisted request
   * @param copyOfDocument the working copy of the document made available to the MT service
   * @param sourceLanguage the ISO language code of the source document
   * @param languages the set of target language codes to translate into
   * @param notifyUserByEmail {@code true} to e-mail the requester when the translation is ready
   */
  @Override
  public void translateDocument(
    final NodeRef origianalDocument,
    final NodeRef copyOfDocument,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  ) {
    final String username = getAuthenticationService().getCurrentUserName();
    CircabcUserDataBean circabcUserDataBean =
      getUserService().getCircabcUserDataBean(username);

    final NodeRef source = copyOfDocument;
    final NodeRef parentFolder = AuthenticationUtil.runAs(
      () -> getNodeService().getPrimaryParent(source).getParentRef(),
      AuthenticationUtil.getSystemUserName()
    );

    final String documentToTranslate = AuthenticationUtil.runAs(
      () -> {
        String sourceName = (String) getNodeService().getProperty(
          source,
          ContentModel.PROP_NAME
        );
        return getDocumentToTranslate(parentFolder, sourceName);
      },
      AuthenticationUtil.getSystemUserName()
    );

    final String targetTranslationPath = AuthenticationUtil.runAs(
      () -> getTargetTranslationPath(parentFolder),
      AuthenticationUtil.getSystemUserName()
    );

    String departmentNumber = circabcUserDataBean.getOrgdepnumber();

    String domains = "all";
    String externalReference = GUID.generate();
    final String translationCallbackUrl = getCallbackURL();
    String errorCallback =
      translationCallbackUrl +
      NOTIFY_ERROR_PATH +
      REQUEST_ID +
      externalReference;
    String institution = circabcUserDataBean.getDomain();
    final String originalFileName = (String) AuthenticationUtil.runAs(
      () ->
        getNodeService().getProperty(copyOfDocument, ContentModel.PROP_NAME),
      AuthenticationUtil.getSystemUserName()
    );

    String outputFormat = "default";
    int priority = 1;
    String requesterCallback =
      translationCallbackUrl +
      NOTIFY_SUCCESS_PATH +
      REQUEST_ID +
      externalReference;
    String requestType = "doc";
    String textToTranslate = "";
    String targetLanguage = Joiner.on(",").join(
      Objects.requireNonNull(languages)
    );

    String email = "";
    if (notifyUserByEmail) {
      email = circabcUserDataBean.getEmail();
    }

    TranslationSaveRequest saveReq = new TranslationSaveRequest();
    saveReq.setNodeRef(origianalDocument);
    saveReq.setDocumentToTranslate(documentToTranslate);
    saveReq.setSourceLanguage(sourceLanguage);
    saveReq.setExternalReference(externalReference);
    saveReq.setUsername(username);
    saveReq.setTargetTranslationPath(targetTranslationPath);
    saveReq.setTextToTranslate(textToTranslate);
    saveReq.setTargetLanguage(targetLanguage);
    saveReq.setNotify(notifyUserByEmail);
    saveReq.setEmail(email);
    saveDocumentRequest(saveReq);

    MachineTranslationRequest mtRequest = new MachineTranslationRequest();
    mtRequest.setApplicationName(applicationName);
    mtRequest.setDepartmentNumber(departmentNumber);
    mtRequest.setDocumentToTranslate(documentToTranslate);
    mtRequest.setDomains(domains);
    mtRequest.setErrorCallback(errorCallback);
    mtRequest.setExternalReference(externalReference);
    mtRequest.setInstitution(institution);
    mtRequest.setOriginalFileName(originalFileName);
    mtRequest.setOutputFormat(outputFormat);
    mtRequest.setPriority(priority);
    mtRequest.setRequesterCallback(requesterCallback);
    mtRequest.setRequestType(requestType);
    mtRequest.setSourceLanguage(sourceLanguage);
    mtRequest.setTargetLanguage(targetLanguage);
    mtRequest.setTargetTranslationPath(targetTranslationPath);
    mtRequest.setTextToTranslate(textToTranslate);
    mtRequest.setUsername(username);
    machineTranslationService.sendMessage(mtRequest);
  }

  private String getCallbackURL() {
    if (webRootUrl.startsWith("https") && (callbackUrl != null)) {
      return callbackUrl;
    } else {
      return webRootUrl;
    }
  }

  private void saveDocumentRequest(TranslationSaveRequest req) {
    Request request = new Request();
    request.setUsername(req.getUsername());
    request.setDocURL(req.getDocumentToTranslate());
    request.setReqDate(new Date());
    request.setRequestID(req.getExternalReference());
    request.setSourceLang(req.getSourceLanguage());
    request.setTargetLangs(req.getTargetLanguage());
    request.setTargetPath(req.getTargetTranslationPath());
    request.setText(req.getTextToTranslate());
    request.setNotify(req.isNotify());
    request.setEmail(req.getEmail());
    request.setDocumentID(req.getNodeRef().toString());

    try {
      translationDaoService.saveRequest(request);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when saving request", e);
      }
    }
  }

  private NodeRef createFolderStructure(NodeRef nodeRef, String username) {
    NodeRef mtRootNode = circabcApi.getMTNodeRef();
    if (mtRootNode == null) {
      throw new IllegalStateException(
        "machine translation root folder does not exists"
      );
    } else {
      Calendar now = Calendar.getInstance();
      String year = String.valueOf(now.get(Calendar.YEAR));
      String month = String.valueOf(now.get(Calendar.MONTH) + 1);
      String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

      NodeRef yearFolder = getFolder(year, mtRootNode);
      NodeRef monthFolder = getFolder(month, yearFolder);
      NodeRef dayFolder = getFolder(day, monthFolder);
      NodeRef userFolder = getFolder(username, dayFolder);
      return getFolder(nodeRef.getId(), userFolder);
    }
  }

  private NodeRef getFolder(String spaceName, NodeRef parentNodeRef) {
    NodeRef result = getNodeService().getChildByName(
      parentNodeRef,
      ContentModel.ASSOC_CONTAINS,
      spaceName
    );
    if (result == null) {
      final ChildAssociationRef childRef = getNodeService().createNode(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        org.alfresco.service.namespace.QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          spaceName
        ),
        ContentModel.TYPE_FOLDER
      );
      result = childRef.getChildRef();
      getNodeService().setProperty(result, ContentModel.PROP_NAME, spaceName);
    }
    return result;
  }

  private String getTargetTranslationPath(NodeRef source) {
    return (
      ftpUrl +
      PathUtils.getCircabcPath(getNodeService().getPath(source), true) +
      "/" +
      TRANSLATIONS
    );
  }

  private String getDocumentToTranslate(NodeRef source, String name) {
    return (
      ftpUrl +
      PathUtils.getCircabcPath(getNodeService().getPath(source), true) +
      "/" +
      name
    );
  }

  private NodeRef copyDocument(NodeRef parentFolder, NodeRef originalDocument) {
    String name = (String) getNodeService().getProperty(
      originalDocument,
      ContentModel.PROP_NAME
    );
    Map<org.alfresco.service.namespace.QName, Serializable> props =
      HashMap.newHashMap(1);
    props.put(ContentModel.PROP_NAME, name);
    final org.alfresco.service.namespace.QName qname =
      org.alfresco.service.namespace.QName.createQName(
        NamespaceService.CONTENT_MODEL_1_0_URI,
        name
      );
    final List<ChildAssociationRef> childAssocs =
      getNodeService().getChildAssocs(
        parentFolder,
        ContentModel.ASSOC_CONTAINS,
        qname,
        1,
        false
      );
    if (childAssocs.size() == 1) {
      getNodeService().deleteNode(childAssocs.get(0).getChildRef());
    }
    final ChildAssociationRef childRef = getNodeService().createNode(
      parentFolder,
      ContentModel.ASSOC_CONTAINS,
      qname,
      ContentModel.TYPE_CONTENT,
      props
    );
    NodeRef targetDocument = childRef.getChildRef();

    byte[] binaryData = getContent(originalDocument);

    ContentWriter writer = contentService.getWriter(
      targetDocument,
      ContentModel.PROP_CONTENT,
      true
    );
    writer.putContent(new ByteArrayInputStream(binaryData));

    return targetDocument;
  }

  private byte[] getContent(NodeRef nodeRef) {
    ContentReader reader = contentService.getReader(
      nodeRef,
      ContentModel.PROP_CONTENT
    );
    InputStream originalInputStream = reader.getContentInputStream();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final int BUF_SIZE = 4096;
    byte[] buffer = new byte[BUF_SIZE];
    int bytesRead = -1;
    try {
      while ((bytesRead = originalInputStream.read(buffer)) > -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      originalInputStream.close();
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when copy document to be translated", e);
      }
    }
    return outputStream.toByteArray();
  }

  /**
   * @return the Alfresco node service
   */
  public NodeService getNodeService() {
    return nodeService;
  }

  /**
   * @param nodeService the Alfresco node service to set
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Builds the synthetic CIRCABC user account that represents the DGT machine-translation service.
   * The credentials are taken from configuration; the remaining descriptive fields are filled with
   * fixed placeholder values.
   *
   * @return a populated {@link CircabcUserDataBean} describing the MT service user
   */
  @Override
  public CircabcUserDataBean getMTUserDetails() {
    final CircabcUserDataBean mtUser = new CircabcUserDataBean();
    mtUser.setUserName(mtUserName);
    mtUser.setPassword(mtPassword);
    mtUser.setFirstName("Machine translation");
    mtUser.setLastName("Machine translation");
    mtUser.setEmail("DGT-MT@ec.europa.eu");
    mtUser.setCompanyId("");
    mtUser.setTitle("");
    mtUser.setPhone("");
    mtUser.setFax(">");
    mtUser.setURL("");
    mtUser.setPostalAddress("");
    mtUser.setDescription("");
    mtUser.setDomain("");
    mtUser.setOrgdepnumber("");
    mtUser.setVisibility(false);
    mtUser.setGlobalNotification(false);
    mtUser.setEcasUserName("");

    return mtUser;
  }

  /**
   * @return the base FTP URL used to build MT source/target paths
   */
  public String getFtpUrl() {
    return ftpUrl;
  }

  /**
   * @param ftpUrl the base FTP URL to set
   */
  public void setFtpUrl(String ftpUrl) {
    this.ftpUrl = ftpUrl;
  }

  /**
   * Creates a working copy of the given content node inside the temporary MT folder structure
   * (organised by year/month/day/user/node id). The copy is placed at a location reachable by the
   * machine-translation service and is used as the document actually sent for translation. The
   * folder creation and copy both run as the system user.
   *
   * @param nodeRef the original content node to copy
   * @return the node reference of the newly created working copy
   * @throws IllegalArgumentException if {@code nodeRef} is not of type {@code cm:content}
   * @throws IllegalStateException if the MT root folder does not exist
   */
  @Override
  public NodeRef copyDocumentToBeTranslated(final NodeRef nodeRef) {
    org.alfresco.service.namespace.QName type = getNodeService().getType(
      nodeRef
    );

    final String username = getAuthenticationService().getCurrentUserName();

    if (!type.equals(org.alfresco.model.ContentModel.TYPE_CONTENT)) {
      throw new IllegalArgumentException(
        "invalid type  " +
          type +
          " of node " +
          nodeRef.toString() +
          " is not String or MLText"
      );
    }

    final NodeRef parentFolder = AuthenticationUtil.runAs(
      () -> createFolderStructure(nodeRef, username),
      AuthenticationUtil.getSystemUserName()
    );

    return AuthenticationUtil.runAs(
      () -> copyDocument(parentFolder, nodeRef), // source
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * @return the Alfresco content service
   */
  public ContentService getContentService() {
    return contentService;
  }

  /**
   * @param contentService the Alfresco content service to set
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Processes the machine-translation results produced since the previous day. This is intended to
   * be invoked by a scheduled job. It applies the returned translations to the repository
   * (documents and properties) and sends the pending user notifications.
   */
  @Override
  public void processTranslatedFiles() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    Date yesterday = cal.getTime();
    processTranslations(yesterday);
    processUserNotification(yesterday);
  }

  private void processUserNotification(Date fromDate) {
    try {
      List<SearchResultNotify> translationsToProcess =
        translationDaoService.getUserToNotify(fromDate);
      for (final SearchResultNotify searchResult : translationsToProcess) {
        if (
          searchResult.getTargetPath().startsWith(ftpUrl) &&
          isTranslationProcessFinished(searchResult)
        ) {
          AuthenticationUtil.runAs(
            () -> notifyUser(searchResult),
            AuthenticationUtil.getSystemUserName()
          );
        }
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when getting  translated documents: ", e);
      }
    }
  }

  private Boolean notifyUser(SearchResultNotify searchResult) {
    if (searchResult.getPropertyQName() == null) {
      notifyUserForDocumentTranslation(searchResult);
    } else {
      notifyUserForPropertyTranslation(searchResult);
    }
    try {
      translationDaoService.markAsNotified(searchResult.getRequestID());
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can not mark row as proccesed:", e);
      }
    }
    return true;
  }

  private void notifyUserForPropertyTranslation(
    SearchResultNotify searchResult
  ) {
    final MailTemplate mailtemplate;
    if (hasErrorTranslation(searchResult.getRequestID())) {
      mailtemplate = MailTemplate.UNSUCCESSFUL_PROPERTY_TRANSLATION;
    } else {
      mailtemplate = MailTemplate.SUCCESSFUL_PROPERTY_TRANSLATION;
    }
    sendNotificationMessage(mailtemplate, searchResult);
  }

  private void notifyUserForDocumentTranslation(
    SearchResultNotify searchResult
  ) {
    if (hasErrorTranslation(searchResult.getRequestID())) {
      sendNotificationMessage(
        MailTemplate.UNSUCCESSFUL_DOCUMENT_TRANSLATION,
        searchResult
      );
    } else {
      sendNotificationMessage(
        MailTemplate.SUCCESSFUL_DOCUMENT_TRANSLATION,
        searchResult
      );
    }
  }

  private void sendNotificationMessage(
    MailTemplate mailTemplate,
    SearchResultNotify searchNotifyItem
  ) {
    NodeRef currentRef = new NodeRef(searchNotifyItem.getDocumentID());
    NodeRef otherPerson = getPersonService().getPerson(
      searchNotifyItem.getUsername()
    );
    Map<String, Object> model = getMailPreferencesService().buildDefaultModel(
      currentRef,
      otherPerson,
      null
    );
    model.put("targetLangs", searchNotifyItem.getTargetLangs());
    if (
      searchNotifyItem.getPropertyQName() != null &&
      !searchNotifyItem.getPropertyQName().equals("")
    ) {
      model.put(
        "targetProperty",
        org.alfresco.service.namespace.QName.createQName(
          searchNotifyItem.getPropertyQName()
        ).getLocalName()
      );
    }

    MailWrapper mail = getMailPreferencesService().getDefaultMailTemplate(
      currentRef,
      mailTemplate
    );

    String from = mailService.getNoReplyEmailAddress();
    String to = (String) getNodeService().getProperty(
      otherPerson,
      ContentModel.PROP_EMAIL
    );

    boolean html = true;
    try {
      mailService.send(
        from,
        to,
        null,
        mail.getSubject(model),
        mail.getBody(model),
        html,
        false
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  private boolean isTranslationProcessFinished(
    SearchResultNotify searchResult
  ) {
    return (
      StringUtils.countOccurrencesOf(searchResult.getTargetLangs(), ",") + 1 ==
      searchResult.getTranslationCount()
    );
  }

  private void processTranslations(Date fromDate) {
    try {
      List<SearchResult> translationsToProcess =
        translationDaoService.getTranslationsToProcess(fromDate);
      for (SearchResult searchResult : translationsToProcess) {
        if (searchResult.getTargetPath().startsWith(ftpUrl)) {
          // process only rows from server that init machine translation
          processRow(searchResult);
        }
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when getting  translated documents: ", e);
      }
    }
  }

  private void processRow(SearchResult searchResult) {
    if (searchResult.getPropertyQName() == null) {
      updateMLDocument(searchResult);
    } else {
      updateProperty(searchResult);
    }
    try {
      translationDaoService.markAsProccesed(
        searchResult.getRequestID(),
        searchResult.getTargetLang()
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can not mark row as processed:", e);
      }
    }
  }

  private void updateProperty(SearchResult searchResult) {
    NodeRef docNodeRef = new NodeRef(searchResult.getDocumentID());
    if (!nodeService.exists(docNodeRef)) {
      return;
    }
    org.alfresco.service.namespace.QName propQname =
      org.alfresco.service.namespace.QName.createQName(
        searchResult.getPropertyQName()
      );
    PropertyDefinition propDef = this.getDictionaryService().getProperty(
      propQname
    );

    if (
      (propDef != null &&
        propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
    ) {
      MLText mlText = getMLPropertyValue(docNodeRef, propQname);

      mlText.addValue(
        Locale.of(searchResult.getTargetLang()),
        searchResult.getTranslatedText()
      );
      getNodeService().setProperty(docNodeRef, propQname, mlText);
    }
  }

  private MLText getMLPropertyValue(
    final NodeRef nodeRef,
    final org.alfresco.service.namespace.QName propertyQname
  ) {
    MLText properties = null;

    final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(true);
      properties = (MLText) getNodeService().getProperty(
        nodeRef,
        propertyQname
      );
    } finally {
      MLPropertyInterceptor.setMLAware(wasMLAware);
    }

    return properties;
  }

  private void updateMLDocument(SearchResult searchResult) {
    NodeRef docNodeRef = new NodeRef(searchResult.getDocumentID());
    if (!nodeService.exists(docNodeRef)) {
      return;
    }
    if (multilingualContentService.isTranslation(docNodeRef)) {
      String documentName = (String) getNodeService().getProperty(
        docNodeRef,
        ContentModel.PROP_NAME
      );
      String name = replaceLast(
        documentName,
        ".",
        "_" + searchResult.getTargetLang() + "."
      );

      Long id = (Long) getNodeService().getProperty(
        docNodeRef,
        ContentModel.PROP_NODE_DBID
      );

      String ftpName = String.valueOf(id) + "_" + searchResult.getTargetLang();
      int lastIndexOf = documentName.lastIndexOf(".");

      if (lastIndexOf > -1) {
        String extension = documentName.substring(lastIndexOf + 1);
        ftpName = ftpName + "." + extension;
      }

      NodeRef emptyTranslation = multilingualContentService.addEmptyTranslation(
        docNodeRef,
        name,
        Locale.of(searchResult.getTargetLang())
      );

      NodeRef translationNodeRef = getTranslationNodeRef(
        searchResult.getTargetPath(),
        ftpName
      );

      if (translationNodeRef != null) {
        byte[] binaryData = getContent(translationNodeRef);
        ContentWriter writer = contentService.getWriter(
          emptyTranslation,
          ContentModel.PROP_CONTENT,
          true
        );
        writer.putContent(new ByteArrayInputStream(binaryData));
        getNodeService().addAspect(
          translationNodeRef,
          ContentModel.ASPECT_TEMPORARY,
          null
        );
        getNodeService().deleteNode(translationNodeRef);
      }
      ownableService.setOwner(emptyTranslation, searchResult.getUsername());
      ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(
        docNodeRef
      );
      NodeRef parent = childAssociationRef.getParentRef();
      // reapply rules on parent
      Action action = this.getActionService().createAction(
        ExecuteAllRulesActionExecuter.NAME
      );
      action.setParameterValue(
        ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES,
        true
      );
      // Execute the action
      this.getActionService().executeAction(action, parent);
    }
  }

  private NodeRef getTranslationNodeRef(String ftpPath, String name) {
    NodeRef result = null;

    ftpPath = ftpPath.replace(ftpUrl, "");
    // NOSONAR: Using "/" as path delimiter is intentional here as this is an FTP path,
    // not a local filesystem path. FTP protocol always uses forward slash regardless of OS.
    ftpPath = ftpPath.replace("/" + mtRootSpace + "/", ""); // NOSONAR
    String[] spaces = ftpPath.split("/"); // NOSONAR
    NodeRef currentNode = circabcApi.getMTNodeRef();
    for (String space : spaces) {
      if (!space.isEmpty()) {
        currentNode = getNodeService().getChildByName(
          currentNode,
          ContentModel.ASSOC_CONTAINS,
          space
        );
        if (currentNode == null) {
          break;
        }
      }
    }
    if (currentNode != null) {
      List<ChildAssociationRef> children = getNodeService().getChildAssocs(
        currentNode
      );
      for (ChildAssociationRef childAssoc : children) {
        NodeRef childNodeRef = childAssoc.getChildRef();
        String childName = (String) getNodeService().getProperty(
          childNodeRef,
          ContentModel.PROP_NAME
        );
        if (childName.equalsIgnoreCase(name)) {
          result = childNodeRef;
          break;
        }
      }
    }

    return result;
  }

  private boolean hasErrorTranslation(String requestID) {
    try {
      return (translationDaoService.getCountOfErrorTranslation(requestID) > 0);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error checking for error translation: ", e);
      }
    }
    return false;
  }

  /**
   * @return the Alfresco multilingual content service
   */
  public MultilingualContentService getMultilingualContentService() {
    return multilingualContentService;
  }

  /**
   * @param multilingualContentService the Alfresco multilingual content service to set
   */
  public void setMultilingualContentService(
    MultilingualContentService multilingualContentService
  ) {
    this.multilingualContentService = multilingualContentService;
  }

  /**
   * @return the name of the MT root space (folder) within the repository
   */
  public String getMtRootSpace() {
    return mtRootSpace;
  }

  /**
   * @param mtRootSpace the MT root space name to set
   */
  public void setMtRootSpace(String mtRootSpace) {
    this.mtRootSpace = mtRootSpace;
  }

  /**
   * @return the set of file extensions eligible for translation
   */
  public Set<String> getFileExtensions() {
    return fileExtensions;
  }

  /**
   * @param fileExtensions the set of eligible file extensions to set
   */
  public void setFileExtensions(Set<String> fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  /**
   * Determines whether a file is eligible for machine translation based on its extension. The
   * comparison is case-insensitive and matches against the configured set of supported extensions.
   *
   * @param fileName the file name (including its extension) to check
   * @return {@code true} if the file's extension is among the supported extensions
   */
  @Override
  public boolean canBeTranslated(String fileName) {
    boolean result = false;
    int i = fileName.lastIndexOf('.');
    if (i > 0) {
      String extension = fileName.substring(i + 1);
      //DIGITCIRCABC-5048 we have to call toLowerCase on the extension to manage UpperCase names.
      result =
        extension != null && fileExtensions.contains(extension.toLowerCase());
    }
    return result;
  }

  /**
   * Deletes the temporary MT folder for a given month by marking it with the {@code temporary}
   * aspect and removing it. Used to clean up the MT working area. No action is taken if the
   * corresponding year or month folder does not exist.
   *
   * @param year the calendar year of the folder to remove
   * @param month the calendar month (1-12) of the folder to remove
   */
  @Override
  public void cleanTempSpace(int year, int month) {
    NodeRef mtRoNodeRef = circabcApi.getMTNodeRef();
    final NodeRef yearFolder = getNodeService().getChildByName(
      mtRoNodeRef,
      ContentModel.ASSOC_CONTAINS,
      String.valueOf(year)
    );
    if (yearFolder != null) {
      final NodeRef monthFolder = getNodeService().getChildByName(
        yearFolder,
        ContentModel.ASSOC_CONTAINS,
        String.valueOf(month)
      );
      if (monthFolder != null) {
        nodeService.addAspect(monthFolder, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(monthFolder);
      }
    }
  }

  /**
   * @return the Alfresco ownable service used to assign document ownership
   */
  public OwnableService getOwnableService() {
    return ownableService;
  }

  /**
   * @param ownableService the Alfresco ownable service to set
   */
  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

  /**
   * @return the Alfresco dictionary service used to inspect property definitions
   */
  public DictionaryService getDictionaryService() {
    return dictionaryService;
  }

  /**
   * @param dictionaryService the Alfresco dictionary service to set
   */
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * @return the mailService
   */
  public MailService getMailService() {
    return mailService;
  }

  /**
   * @param mailService the mailService to set
   */
  public void setMailService(MailService mailService) {
    this.mailService = mailService;
  }

  /**
   * @return the nodePreferencesService
   */
  protected final MailPreferencesService getMailPreferencesService() {
    return mailPreferencesService;
  }

  /**
   * @param mailPreferencesService the mailPreferencesService to set
   */
  public final void setMailPreferencesService(
    MailPreferencesService mailPreferencesService
  ) {
    this.mailPreferencesService = mailPreferencesService;
  }

  /**
   * @return the person service used to resolve Alfresco person nodes
   */
  public PersonService getPersonService() {
    return personService;
  }

  /**
   * @param personService the person service to set
   */
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  /**
   * @return the maximum translatable file size, in bytes
   */
  public long getMaxFileSizeInBytes() {
    return maxFileSizeInBytes;
  }

  /**
   * @param maxFileSizeInBytes the maximum translatable file size, in bytes, to set
   */
  public void setMaxFileSizeInBytes(long maxFileSizeInBytes) {
    this.maxFileSizeInBytes = maxFileSizeInBytes;
  }

  /**
   * @return the maximum file size, in bytes, allowed for a document to be translated
   */
  @Override
  public long fileMaxSize() {
    return maxFileSizeInBytes;
  }

  /**
   * {@inheritDoc}
   *
   * @return the set of file extensions eligible for machine translation
   */
  @Override
  public Set<String> getAvailableFileExtensions() {
    return getFileExtensions();
  }

  /**
   * @return the Alfresco action service used to re-apply folder rules
   */
  public ActionService getActionService() {
    return actionService;
  }

  /**
   * @param actionService the Alfresco action service to set
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  /**
   * @return the transport used to send requests to the MT back-end
   */
  public MachineTranslationService getMachineTranslationService() {
    return machineTranslationService;
  }

  /**
   * @param machineTranslationService the MT transport service to set
   */
  public void setMachineTranslationService(
    MachineTranslationService machineTranslationService
  ) {
    this.machineTranslationService = machineTranslationService;
  }
}
