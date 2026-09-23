package eu.europa.ec.digit.circabc.rest.platformsample;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import eu.europa.ec.digit.circabc.rest.service.translation.TranslationService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class CircabcComponentTest {

  private CircabcComponent component;

  private NodeService nodeService;
  private NodeLocatorService nodeLocatorService;
  private CircabcConfig circabcConfig;
  private CircabcApi circabcApi;
  private NodePreferencesService nodePreferencesService;
  private PermissionService permissionService;
  private PersonService personService;
  private TranslationService translationService;
  private UserService userService;

  @Before
  public void setUp() throws Exception {
    component = new CircabcComponent();

    nodeService = mock(NodeService.class);
    nodeLocatorService = mock(NodeLocatorService.class);
    circabcConfig = mock(CircabcConfig.class);
    circabcApi = mock(CircabcApi.class);
    nodePreferencesService = mock(NodePreferencesService.class);
    permissionService = mock(PermissionService.class);
    personService = mock(PersonService.class);
    translationService = mock(TranslationService.class);
    userService = mock(UserService.class);

    setField("nodeService", nodeService);
    setField("nodeLocatorService", nodeLocatorService);
    setField("circabcConfig", circabcConfig);
    setField("circabcApi", circabcApi);
    setField("nodePreferencesService", nodePreferencesService);
    setField("permissionService", permissionService);
    setField("personService", personService);
    setField("translationService", translationService);
    setField("userService", userService);

    when(circabcConfig.getBuildRelease()).thenReturn("1.0.0");
    when(circabcConfig.getHibernateDialect()).thenReturn(
      "org.hibernate.dialect.PostgreSQLDialect"
    );
  }

  @Test
  public void testExecuteInternal_whenFaqsFolderExists_thenNoCreation()
    throws Throwable {
    NodeRef ddRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dd-id"
    );
    NodeRef faqsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "faqs-id"
    );
    NodeRef faqLinksRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "faq-links-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef mtNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-node"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(ddRef);
    when(
      nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(faqsRef);
    when(
      nodeService.getChildByName(
        ddRef,
        ContentModel.ASSOC_CONTAINS,
        "faqsLinks"
      )
    ).thenReturn(faqLinksRef);
    when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(
      companyHome
    );
    when(
      nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, "MT")
    ).thenReturn(mtNode);

    CircabcUserDataBean mtUser = new CircabcUserDataBean();
    mtUser.setUserName("mtuser");
    when(translationService.getMTUserDetails()).thenReturn(mtUser);
    when(personService.personExists("mtuser")).thenReturn(true);

    invokeExecuteInternal();

    verify(nodeService, never()).createNode(
      any(NodeRef.class),
      any(QName.class),
      any(QName.class),
      any(QName.class)
    );
  }

  @Test
  public void testExecuteInternal_whenFaqsFolderMissing_thenCreatesIt()
    throws Throwable {
    NodeRef ddRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dd-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-faqs"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef mtNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-node"
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(ddRef);
    when(
      nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(null);
    when(
      nodeService.getChildByName(
        ddRef,
        ContentModel.ASSOC_CONTAINS,
        "faqsLinks"
      )
    ).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "existing-links")
    );
    when(
      nodeService.createNode(
        eq(ddRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class),
        eq(ContentModel.TYPE_FOLDER)
      )
    ).thenReturn(childAssoc);
    when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(
      companyHome
    );
    when(
      nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, "MT")
    ).thenReturn(mtNode);

    CircabcUserDataBean mtUser = new CircabcUserDataBean();
    mtUser.setUserName("mtuser");
    when(translationService.getMTUserDetails()).thenReturn(mtUser);
    when(personService.personExists("mtuser")).thenReturn(true);

    invokeExecuteInternal();

    verify(nodeService).setProperty(childRef, ContentModel.PROP_NAME, "faqs");
    verify(permissionService).setPermission(
      childRef,
      "guest",
      "Consumer",
      true
    );
  }

  @Test
  public void testExecuteInternal_whenMTUserMissing_thenCreatesUser()
    throws Throwable {
    NodeRef ddRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dd-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef mtNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-node"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(ddRef);
    when(
      nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "existing-faqs")
    );
    when(
      nodeService.getChildByName(
        ddRef,
        ContentModel.ASSOC_CONTAINS,
        "faqsLinks"
      )
    ).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "existing-links")
    );
    when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(
      companyHome
    );
    when(
      nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, "MT")
    ).thenReturn(mtNode);

    CircabcUserDataBean mtUser = new CircabcUserDataBean();
    mtUser.setUserName("mtuser");
    mtUser.setPassword("mtpass");
    when(translationService.getMTUserDetails()).thenReturn(mtUser);
    when(personService.personExists("mtuser")).thenReturn(false);

    invokeExecuteInternal();

    verify(userService).createUser(mtUser, true);
    verify(userService).setPassword("mtuser", "mtpass".toCharArray());
  }

  @Test
  public void testExecuteInternal_whenExceptionInFaqs_thenContinues()
    throws Throwable {
    NodeRef ddRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dd-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef mtNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-node"
    );

    when(circabcApi.getCircabcDictionaryNodeRef())
      .thenThrow(new RuntimeException("test error"))
      .thenReturn(ddRef);
    when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(
      companyHome
    );
    when(
      nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, "MT")
    ).thenReturn(mtNode);

    CircabcUserDataBean mtUser = new CircabcUserDataBean();
    mtUser.setUserName("mtuser");
    when(translationService.getMTUserDetails()).thenReturn(mtUser);
    when(personService.personExists("mtuser")).thenReturn(true);

    // Should not throw — exceptions are caught internally
    invokeExecuteInternal();

    verify(nodePreferencesService).updateRootReference();
  }

  private void invokeExecuteInternal() throws Throwable {
    Method method = CircabcComponent.class.getDeclaredMethod("executeInternal");
    method.setAccessible(true);
    try {
      method.invoke(component);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcComponent.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(component, value);
  }
}
