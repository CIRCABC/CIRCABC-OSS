package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class UserDisplayNameMethodTest {

  private UserDisplayNameMethod method;
  private ServiceRegistry serviceRegistry;
  private NodeService nodeService;
  private PersonService personService;

  @Before
  public void setUp() {
    method = new UserDisplayNameMethod();
    serviceRegistry = mock(ServiceRegistry.class);
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);

    when(serviceRegistry.getNodeService()).thenReturn(nodeService);
    when(serviceRegistry.getPersonService()).thenReturn(personService);

    method.setServiceRegistry(serviceRegistry);
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    Object result = method.exec(new ArrayList<>());
    assertEquals("", result);
  }

  @Test
  public void testExec_whenSimpleScalarAndPersonExists_thenReturnsFullName()
    throws TemplateModelException {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    when(personService.personExists("john")).thenReturn(true);
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");

    List<Object> args = new ArrayList<>();
    args.add(new SimpleScalar("john"));

    Object result = method.exec(args);
    assertEquals("John Doe", result);
  }

  @Test
  public void testExec_whenSimpleScalarAndPersonNotExists_thenReturnsUsername()
    throws TemplateModelException {
    when(personService.personExists("unknown")).thenReturn(false);

    List<Object> args = new ArrayList<>();
    args.add(new SimpleScalar("unknown"));

    Object result = method.exec(args);
    assertEquals("unknown", result);
  }

  @Test
  public void testExec_whenSimpleScalarWithAddEmail_thenReturnsFullNameWithEmail()
    throws TemplateModelException {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    when(personService.personExists("john")).thenReturn(true);
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("john@example.com");

    TemplateBooleanModel trueModel = mock(TemplateBooleanModel.class);
    when(trueModel.getAsBoolean()).thenReturn(true);

    List<Object> args = new ArrayList<>();
    args.add(new SimpleScalar("john"));
    args.add(trueModel);

    Object result = method.exec(args);
    assertEquals("John Doe (john@example.com)", result);
  }

  @Test
  public void testExec_whenBeanModelWithTemplateNode_thenReturnsFullName()
    throws TemplateModelException {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(userRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    when(
      nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("Jane");
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Smith");

    List<Object> args = new ArrayList<>();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("Jane Smith", result);
  }

  @Test
  public void testExec_whenNullFirstAndLastName_thenReturnsUsername()
    throws TemplateModelException {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    when(personService.personExists("john")).thenReturn(true);
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_USERNAME)
    ).thenReturn("john");

    List<Object> args = new ArrayList<>();
    args.add(new SimpleScalar("john"));

    Object result = method.exec(args);
    assertEquals("john", result);
  }

  @Test
  public void testExec_whenAllPropertiesNull_thenReturnsEmptyString()
    throws TemplateModelException {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    when(personService.personExists("john")).thenReturn(true);
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_USERNAME)
    ).thenReturn(null);

    List<Object> args = new ArrayList<>();
    args.add(new SimpleScalar("john"));

    Object result = method.exec(args);
    assertEquals("", result);
  }
}
