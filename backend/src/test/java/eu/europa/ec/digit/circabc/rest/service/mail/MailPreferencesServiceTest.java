package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CustomizationException;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class MailPreferencesServiceTest {

  private MailPreferencesService service;
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

    MailPreferencesServiceImpl impl = new MailPreferencesServiceImpl();

    nodePreferencesService = mock(NodePreferencesService.class);
    nodeService = mock(NodeService.class);
    templateService = mock(TemplateService.class);
    personService = mock(PersonService.class);
    dictionaryService = mock(DictionaryService.class);
    circabcApi = mock(CircabcApi.class);
    multilingualContentService = mock(MultilingualContentService.class);
    circabcConfig = mock(CircabcConfig.class);
    apiToolBox = mock(ApiToolBox.class);

    setField(impl, "nodePreferencesService", nodePreferencesService);
    setField(impl, "nodeService", nodeService);
    setField(impl, "templateService", templateService);
    setField(impl, "personService", personService);
    setField(impl, "dictionaryService", dictionaryService);
    setField(impl, "circabcApi", circabcApi);
    setField(impl, "multilingualContentService", multilingualContentService);
    setField(impl, "circabcConfig", circabcConfig);
    setField(impl, "apiToolBox", apiToolBox);

    service = impl;
    testRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = MailPreferencesServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void testGetTemplateUserDetails_returnsNonNull() {
    CircabcUserDataBean result = service.getTemplateUserDetails();
    assertNotNull(result);
    assertEquals("<USERNAME>", result.getUserName());
  }

  @Test
  public void testGetMailTemplates_returnsListForValidNode()
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
  public void testGetDefaultMailTemplate_returnsWrapper()
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
  }

  @Test
  public void testBuildDefaultModel_returnsModelMap() {
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
  }

  @Test
  public void testGetDisclamerLogo_returnsNodeRef()
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
}
