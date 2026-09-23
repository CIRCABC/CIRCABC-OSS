package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.db.SearchResult;
import io.swagger.model.db.SearchResultNotify;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class TranslationServiceImplTest {

  private TranslationServiceImpl service;
  private NodeService nodeService;
  private AuthenticationService authenticationService;
  private TranslationDaoService translationDaoService;
  private MachineTranslationService machineTranslationService;
  private CircabcApi circabcApi;
  private CircabcConfig circabcConfig;
  private UserService userService;
  private ContentService contentService;
  private DictionaryService dictionaryService;
  private PersonService personService;
  private MailService mailService;
  private MailPreferencesService mailPreferencesService;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    service = new TranslationServiceImpl();
    nodeService = mock(NodeService.class);
    authenticationService = mock(AuthenticationService.class);
    translationDaoService = mock(TranslationDaoService.class);
    machineTranslationService = mock(MachineTranslationService.class);
    circabcApi = mock(CircabcApi.class);
    circabcConfig = mock(CircabcConfig.class);
    userService = mock(UserService.class);
    contentService = mock(ContentService.class);
    dictionaryService = mock(DictionaryService.class);
    personService = mock(PersonService.class);
    mailService = mock(MailService.class);
    mailPreferencesService = mock(MailPreferencesService.class);

    service.setNodeService(nodeService);
    service.setAuthenticationService(authenticationService);
    service.setTranslationDaoService(translationDaoService);
    service.setMachineTranslationService(machineTranslationService);
    service.setUserService(userService);
    service.setContentService(contentService);
    service.setDictionaryService(dictionaryService);
    service.setPersonService(personService);
    service.setMailService(mailService);
    service.setMailPreferencesService(mailPreferencesService);
    service.setFtpUrl("ftp://localhost");
    service.setFileExtensions(Set.of("docx", "pdf", "txt"));
    service.setLanguages(Set.of("EN", "FR", "DE"));
    service.setMaxFileSizeInBytes(10485760L);
    service.setMtRootSpace("mt");

    setField("circabcConfig", circabcConfig);
    setField("circabcApi", circabcApi);

    when(circabcConfig.getWebRootUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getMTCallbackUrl()).thenReturn(
      "https://callback.europa.eu"
    );
    when(circabcConfig.getMtUsername()).thenReturn("mtuser");
    when(circabcConfig.getMtPassword()).thenReturn("mtpass");
    when(circabcConfig.getMTApplicationName()).thenReturn("CIRCABC");

    service.init();
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TranslationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  private Object getField(String fieldName) throws Exception {
    Field field = TranslationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(service);
  }

  @Test
  public void testInit_setsFieldsFromConfig() throws Exception {
    assertEquals("https://circabc.europa.eu", getField("webRootUrl"));
    assertEquals("https://callback.europa.eu", getField("callbackUrl"));
    assertEquals("mtuser", getField("mtUserName"));
    assertEquals("mtpass", getField("mtPassword"));
    assertEquals("CIRCABC", getField("applicationName"));
  }

  @Test
  public void testGetCallbackURL_whenHttpsAndCallbackSet_returnsCallbackUrl()
    throws Exception {
    setField("webRootUrl", "https://circabc.europa.eu");
    setField("callbackUrl", "https://callback.europa.eu");

    java.lang.reflect.Method method =
      TranslationServiceImpl.class.getDeclaredMethod("getCallbackURL");
    method.setAccessible(true);
    String result = (String) method.invoke(service);

    assertEquals("https://callback.europa.eu", result);
  }

  @Test
  public void testGetCallbackURL_whenHttpUrl_returnsWebRootUrl()
    throws Exception {
    setField("webRootUrl", "http://localhost:8080");
    setField("callbackUrl", "https://callback.europa.eu");

    java.lang.reflect.Method method =
      TranslationServiceImpl.class.getDeclaredMethod("getCallbackURL");
    method.setAccessible(true);
    String result = (String) method.invoke(service);

    assertEquals("http://localhost:8080", result);
  }

  @Test
  public void testGetCallbackURL_whenHttpsAndCallbackNull_returnsWebRootUrl()
    throws Exception {
    setField("webRootUrl", "https://circabc.europa.eu");
    setField("callbackUrl", null);

    java.lang.reflect.Method method =
      TranslationServiceImpl.class.getDeclaredMethod("getCallbackURL");
    method.setAccessible(true);
    String result = (String) method.invoke(service);

    assertEquals("https://circabc.europa.eu", result);
  }

  @Test
  public void testTranslateProperty_whenStringProperty_sendsMTRequest() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    QName property = ContentModel.PROP_TITLE;
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    CircabcUserDataBean userData = new CircabcUserDataBean();
    userData.setEmail("user@test.com");
    userData.setOrgdepnumber("DIGIT");
    userData.setDomain("EC");
    when(userService.getCircabcUserDataBean("testuser")).thenReturn(userData);
    when(nodeService.getProperty(nodeRef, property)).thenReturn("Hello World");

    Set<String> targetLangs = new LinkedHashSet<>(List.of("FR", "DE"));
    service.translateProperty(nodeRef, property, "EN", targetLangs, true);

    verify(translationDaoService).saveRequest(any());
    verify(machineTranslationService).sendMessage(
      any(MachineTranslationRequest.class)
    );
  }

  @Test
  public void testTranslateProperty_whenNotifyFalse_emailIsEmpty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    QName property = ContentModel.PROP_TITLE;
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    CircabcUserDataBean userData = new CircabcUserDataBean();
    userData.setEmail("user@test.com");
    userData.setOrgdepnumber("DIGIT");
    userData.setDomain("EC");
    when(userService.getCircabcUserDataBean("testuser")).thenReturn(userData);
    when(nodeService.getProperty(nodeRef, property)).thenReturn("Hello");

    service.translateProperty(nodeRef, property, "EN", Set.of("FR"), false);

    verify(translationDaoService).saveRequest(
      argThat(request -> request.getEmail().isEmpty())
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTranslateProperty_whenPropertyNotStringOrMLText_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    QName property = ContentModel.PROP_TITLE;
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    CircabcUserDataBean userData = new CircabcUserDataBean();
    when(userService.getCircabcUserDataBean("testuser")).thenReturn(userData);
    when(nodeService.getProperty(nodeRef, property)).thenReturn(
      Integer.valueOf(42)
    );

    service.translateProperty(nodeRef, property, "EN", Set.of("FR"), false);
  }

  @Test
  public void testProcessTranslatedFiles_whenPropertyTranslation_updatesMLText()
    throws Exception {
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-1"
    );
    QName propQname = ContentModel.PROP_TITLE;

    SearchResult searchResult = new SearchResult();
    searchResult.setRequestID("req-1");
    searchResult.setTargetLang("FR");
    searchResult.setTargetPath("ftp://localhost/some/path");
    searchResult.setTranslatedText("Bonjour");
    searchResult.setUsername("testuser");
    searchResult.setDocumentID(docRef.toString());
    searchResult.setPropertyQName(propQname.toString());

    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenReturn(List.of(searchResult));
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      Collections.emptyList()
    );
    when(nodeService.exists(docRef)).thenReturn(true);

    PropertyDefinition propDef = mock(PropertyDefinition.class);
    org.alfresco.service.cmr.dictionary.DataTypeDefinition dataTypeDef = mock(
      org.alfresco.service.cmr.dictionary.DataTypeDefinition.class
    );
    when(propDef.getDataType()).thenReturn(dataTypeDef);
    when(dataTypeDef.getName()).thenReturn(DataTypeDefinition.MLTEXT);
    when(dictionaryService.getProperty(propQname)).thenReturn(propDef);

    MLText mlText = new MLText();
    mlText.addValue(Locale.ENGLISH, "Hello");
    when(nodeService.getProperty(docRef, propQname)).thenReturn(mlText);

    service.processTranslatedFiles();

    verify(nodeService).setProperty(
      eq(docRef),
      eq(propQname),
      any(MLText.class)
    );
    verify(translationDaoService).markAsProccesed("req-1", "FR");
  }

  @Test
  public void testProcessTranslatedFiles_whenNodeDoesNotExist_skipsUpdate()
    throws Exception {
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-1"
    );

    SearchResult searchResult = new SearchResult();
    searchResult.setRequestID("req-1");
    searchResult.setTargetLang("FR");
    searchResult.setTargetPath("ftp://localhost/some/path");
    searchResult.setTranslatedText("Bonjour");
    searchResult.setUsername("testuser");
    searchResult.setDocumentID(docRef.toString());
    searchResult.setPropertyQName(ContentModel.PROP_TITLE.toString());

    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenReturn(List.of(searchResult));
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      Collections.emptyList()
    );
    when(nodeService.exists(docRef)).thenReturn(false);

    service.processTranslatedFiles();

    verify(nodeService, never()).setProperty(any(), any(), any());
  }

  @Test
  public void testProcessTranslatedFiles_whenDifferentFtpUrl_skipsRow()
    throws Exception {
    SearchResult searchResult = new SearchResult();
    searchResult.setRequestID("req-1");
    searchResult.setTargetPath("ftp://other-server/path");
    searchResult.setDocumentID("workspace://SpacesStore/doc-1");

    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenReturn(List.of(searchResult));
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      Collections.emptyList()
    );

    service.processTranslatedFiles();

    verify(translationDaoService, never()).markAsProccesed(any(), any());
  }

  @Test
  public void testProcessTranslatedFiles_whenDaoThrows_doesNotPropagate()
    throws Exception {
    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenThrow(new RuntimeException("DB error"));
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      Collections.emptyList()
    );

    // Should not throw
    service.processTranslatedFiles();
  }

  @Test
  public void testProcessTranslatedFiles_notifiesUser_whenTranslationFinished()
    throws Exception {
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-1"
    );
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );

    SearchResultNotify notifyResult = new SearchResultNotify();
    notifyResult.setRequestID("req-1");
    notifyResult.setTargetLangs("FR");
    notifyResult.setTargetPath("ftp://localhost/some/path");
    notifyResult.setUsername("testuser");
    notifyResult.setDocumentID(docRef.toString());
    notifyResult.setPropertyQName(ContentModel.PROP_TITLE.toString());
    notifyResult.setTranslationCount(1);

    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenReturn(Collections.emptyList());
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      List.of(notifyResult)
    );
    when(translationDaoService.getCountOfErrorTranslation("req-1")).thenReturn(
      0
    );
    when(personService.getPerson("testuser")).thenReturn(personRef);
    when(
      mailPreferencesService.buildDefaultModel(
        eq(docRef),
        eq(personRef),
        isNull()
      )
    ).thenReturn(new HashMap<>());

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(mailWrapper.getSubject(any())).thenReturn("Subject");
    when(mailWrapper.getBody(any())).thenReturn("Body");
    when(
      mailPreferencesService.getDefaultMailTemplate(eq(docRef), any())
    ).thenReturn(mailWrapper);
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("user@test.com");

    service.processTranslatedFiles();

    verify(translationDaoService).markAsNotified("req-1");
  }

  @Test
  public void testProcessTranslatedFiles_whenTranslationNotFinished_doesNotNotify()
    throws Exception {
    SearchResultNotify notifyResult = new SearchResultNotify();
    notifyResult.setRequestID("req-1");
    notifyResult.setTargetLangs("FR,DE");
    notifyResult.setTargetPath("ftp://localhost/some/path");
    notifyResult.setUsername("testuser");
    notifyResult.setDocumentID("workspace://SpacesStore/doc-1");
    notifyResult.setTranslationCount(1); // 2 languages but only 1 translation

    when(
      translationDaoService.getTranslationsToProcess(any(Date.class))
    ).thenReturn(Collections.emptyList());
    when(translationDaoService.getUserToNotify(any(Date.class))).thenReturn(
      List.of(notifyResult)
    );

    service.processTranslatedFiles();

    verify(translationDaoService, never()).markAsNotified(any());
  }
}
