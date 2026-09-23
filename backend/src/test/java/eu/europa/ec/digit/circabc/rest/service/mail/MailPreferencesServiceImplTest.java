package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CustomizationException;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class MailPreferencesServiceImplTest {

  private MailPreferencesServiceImpl service;
  private NodePreferencesService nodePreferencesService;
  private NodeService nodeService;
  private TemplateService templateService;
  private PersonService personService;
  private DictionaryService dictionaryService;
  private CircabcApi circabcApi;
  private MultilingualContentService multilingualContentService;
  private CircabcConfig circabcConfig;
  private ApiToolBox apiToolBox;

  private NodeRef testRef;

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

    service = new MailPreferencesServiceImpl();

    nodePreferencesService = mock(NodePreferencesService.class);
    nodeService = mock(NodeService.class);
    templateService = mock(TemplateService.class);
    personService = mock(PersonService.class);
    dictionaryService = mock(DictionaryService.class);
    circabcApi = mock(CircabcApi.class);
    multilingualContentService = mock(MultilingualContentService.class);
    circabcConfig = mock(CircabcConfig.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodePreferencesService", nodePreferencesService);
    setField("nodeService", nodeService);
    setField("templateService", templateService);
    setField("personService", personService);
    setField("dictionaryService", dictionaryService);
    setField("circabcApi", circabcApi);
    setField("multilingualContentService", multilingualContentService);
    setField("circabcConfig", circabcConfig);
    setField("apiToolBox", apiToolBox);

    testRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MailPreferencesServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetTemplateUserDetails_returnsPopulatedBean() {
    CircabcUserDataBean result = service.getTemplateUserDetails();

    assertNotNull(result);
    assertEquals("<USERNAME>", result.getUserName());
    assertEquals("<USER_FIRST_NAME>", result.getFirstName());
    assertEquals("<USER_LAST_NAME>", result.getLastName());
    assertEquals("<USER_EMAIL>", result.getEmail());
    assertTrue(result.getVisibility());
    assertTrue(result.getGlobalNotification());
  }

  @Test
  public void testGetDefaultMailTemplate_whenTemplateFound_returnsWrapper()
    throws CustomizationException {
    NodeRef templateNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "template-id"
    );
    MailTemplate mailTemplate = MailTemplate.NOTIFY_DOC;

    when(
      nodePreferencesService.getDefaultConfigurationFile(
        testRef,
        "templates",
        "mails",
        mailTemplate.getTemplateDirectoryName()
      )
    ).thenReturn(templateNodeRef);
    when(multilingualContentService.isTranslation(templateNodeRef)).thenReturn(
      false
    );

    MailWrapper result = service.getDefaultMailTemplate(testRef, mailTemplate);

    assertNotNull(result);
    verify(nodePreferencesService).getDefaultConfigurationFile(
      testRef,
      "templates",
      "mails",
      mailTemplate.getTemplateDirectoryName()
    );
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDefaultMailTemplate_whenNoTemplate_throwsException()
    throws CustomizationException {
    MailTemplate mailTemplate = MailTemplate.NOTIFY_DOC;

    when(
      nodePreferencesService.getDefaultConfigurationFile(
        testRef,
        "templates",
        "mails",
        mailTemplate.getTemplateDirectoryName()
      )
    ).thenReturn(null);

    service.getDefaultMailTemplate(testRef, mailTemplate);
  }

  @Test
  public void testGetMailTemplates_whenTemplatesExist_returnsList()
    throws CustomizationException {
    MailTemplate mailTemplate = MailTemplate.NOTIFY_DOC;
    NodeRef templateNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mail-template-1"
    );

    when(
      nodePreferencesService.getConfigurationFiles(
        testRef,
        "templates",
        "mails",
        mailTemplate.getTemplateDirectoryName()
      )
    ).thenReturn(Collections.singletonList(templateNode));
    when(multilingualContentService.isTranslation(templateNode)).thenReturn(
      false
    );

    List<MailWrapper> result = service.getMailTemplates(testRef, mailTemplate);

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  public void testGetMailTemplates_whenEmpty_returnsEmptyList()
    throws CustomizationException {
    MailTemplate mailTemplate = MailTemplate.NOTIFY_DOC;

    when(
      nodePreferencesService.getConfigurationFiles(
        testRef,
        "templates",
        "mails",
        mailTemplate.getTemplateDirectoryName()
      )
    ).thenReturn(Collections.emptyList());

    List<MailWrapper> result = service.getMailTemplates(testRef, mailTemplate);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildDefaultModel_withContentNode() {
    NodeRef currentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    NodeRef meRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "me-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-id"
    );

    when(personService.getPerson("testuser")).thenReturn(meRef);
    when(nodeService.getType(currentRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(true);
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(currentRef)).thenReturn(childAssoc);
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(apiToolBox.getCurrentInterestGroup(currentRef)).thenReturn(null);
    when(apiToolBox.getCurrentCategory(currentRef)).thenReturn(null);

    Map<String, Object> model = service.buildDefaultModel(
      currentRef,
      personRef,
      null
    );

    assertNotNull(model);
    assertEquals(personRef, model.get(MailTemplate.KEY_PERSON));
    assertEquals(currentRef, model.get(MailTemplate.KEY_LOCATION));
    assertEquals(currentRef, model.get(MailTemplate.KEY_DOCUMENT));
    assertEquals(parentRef, model.get(MailTemplate.KEY_SPACE));
    assertEquals(companyHome, model.get(MailTemplate.KEY_COMPANY_HOME));
    assertEquals(circabcRef, model.get(MailTemplate.KEY_CIRCABC));
  }

  @Test
  public void testBuildDefaultModel_withNullCurrentRef() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    NodeRef meRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "me-id"
    );

    when(personService.getPerson("testuser")).thenReturn(meRef);

    Map<String, Object> model = service.buildDefaultModel(
      null,
      personRef,
      null
    );

    assertNotNull(model);
    assertEquals(personRef, model.get(MailTemplate.KEY_PERSON));
    assertEquals(meRef, model.get(MailTemplate.KEY_ME));
    assertNull(model.get(MailTemplate.KEY_LOCATION));
  }

  @Test
  public void testGetDisclamerLogo_whenFound_returnsNodeRef()
    throws CustomizationException {
    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );

    when(
      nodePreferencesService.getDefaultConfigurationFile(
        testRef,
        "templates",
        "disclamer",
        "logo"
      )
    ).thenReturn(logoRef);

    NodeRef result = service.getDisclamerLogo(testRef);

    assertEquals(logoRef, result);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDisclamerLogo_whenNotFound_throwsException()
    throws CustomizationException {
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        testRef,
        "templates",
        "disclamer",
        "logo"
      )
    ).thenReturn(null);

    service.getDisclamerLogo(testRef);
  }

  @Test
  public void testGetHeaderLogo_returnsNodeRef() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico-id"
    );
    NodeRef templatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "templates-id"
    );
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-id"
    );
    NodeRef logoFolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-folder-id"
    );
    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(dicoRef);
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "templates"
      )
    ).thenReturn(templatesRef);
    when(
      nodeService.getChildByName(
        templatesRef,
        ContentModel.ASSOC_CONTAINS,
        "header"
      )
    ).thenReturn(headerRef);
    when(
      nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo")
    ).thenReturn(logoFolderRef);
    when(
      nodeService.getChildByName(
        logoFolderRef,
        ContentModel.ASSOC_CONTAINS,
        "header-mail.jpg"
      )
    ).thenReturn(logoRef);

    NodeRef result = service.getHeaderLogo(testRef);

    assertEquals(logoRef, result);
  }

  @Test
  public void testGetHeaderBackground_returnsNodeRef() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico-id"
    );
    NodeRef templatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "templates-id"
    );
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-id"
    );
    NodeRef logoFolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-folder-id"
    );
    NodeRef bgRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "bg-id"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(dicoRef);
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "templates"
      )
    ).thenReturn(templatesRef);
    when(
      nodeService.getChildByName(
        templatesRef,
        ContentModel.ASSOC_CONTAINS,
        "header"
      )
    ).thenReturn(headerRef);
    when(
      nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo")
    ).thenReturn(logoFolderRef);
    when(
      nodeService.getChildByName(
        logoFolderRef,
        ContentModel.ASSOC_CONTAINS,
        "header-background.jpg"
      )
    ).thenReturn(bgRef);

    NodeRef result = service.getHeaderBackground(testRef);

    assertEquals(bgRef, result);
  }

  @Test
  public void testGetHeaderEULogo_returnsNodeRef() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico-id"
    );
    NodeRef templatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "templates-id"
    );
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-id"
    );
    NodeRef logoFolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-folder-id"
    );
    NodeRef euLogoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "eu-logo-id"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(dicoRef);
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "templates"
      )
    ).thenReturn(templatesRef);
    when(
      nodeService.getChildByName(
        templatesRef,
        ContentModel.ASSOC_CONTAINS,
        "header"
      )
    ).thenReturn(headerRef);
    when(
      nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo")
    ).thenReturn(logoFolderRef);
    when(
      nodeService.getChildByName(
        logoFolderRef,
        ContentModel.ASSOC_CONTAINS,
        "header-eu-circabc-logo.png"
      )
    ).thenReturn(euLogoRef);

    NodeRef result = service.getHeaderEULogo(testRef);

    assertEquals(euLogoRef, result);
  }
}
