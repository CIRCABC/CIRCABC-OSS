package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import io.swagger.model.alfresco.UserModel;
import java.util.ArrayList;
import java.util.Collections;
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

public class LoginUserNameMethodTest {

  private LoginUserNameMethod method;
  private ServiceRegistry serviceRegistry;
  private NodeService nodeService;
  private PersonService personService;

  @Before
  public void setUp() {
    method = new LoginUserNameMethod();
    serviceRegistry = mock(ServiceRegistry.class);
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);

    when(serviceRegistry.getNodeService()).thenReturn(nodeService);
    when(serviceRegistry.getPersonService()).thenReturn(personService);

    method.setServiceRegistry(serviceRegistry);
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyString() throws Exception {
    Object result = method.exec(Collections.emptyList());
    assertEquals("", result);
  }

  @Test
  public void testExec_whenBeanModelWithTemplateNode_thenReturnsEcasUserName()
    throws Exception {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(userRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    when(
      nodeService.getProperty(userRef, UserModel.PROP_ECAS_USER_NAME)
    ).thenReturn("ecas-user");

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("ecas-user", result);
  }

  @Test
  public void testExec_whenBeanModelWithTemplateNode_andEcasNull_thenReturnsAlfrescoUsername()
    throws Exception {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(userRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    when(
      nodeService.getProperty(userRef, UserModel.PROP_ECAS_USER_NAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("alfresco-user");

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("alfresco-user", result);
  }

  @Test
  public void testExec_whenSimpleScalarAndPersonExists_thenReturnsLoginName()
    throws Exception {
    String userName = "john";
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );

    when(personService.personExists(userName)).thenReturn(true);
    when(personService.getPerson(userName)).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, UserModel.PROP_ECAS_USER_NAME)
    ).thenReturn("john-ecas");

    SimpleScalar scalar = new SimpleScalar(userName);
    List args = new ArrayList();
    args.add(scalar);

    Object result = method.exec(args);
    assertEquals("john-ecas", result);
  }

  @Test
  public void testExec_whenSimpleScalarAndPersonNotExists_thenReturnsUserName()
    throws Exception {
    String userName = "unknown";

    when(personService.personExists(userName)).thenReturn(false);

    SimpleScalar scalar = new SimpleScalar(userName);
    List args = new ArrayList();
    args.add(scalar);

    Object result = method.exec(args);
    assertEquals("unknown", result);
  }

  @Test
  public void testExec_whenBeanModelWrapsNonTemplateNode_thenReturnsEmptyString()
    throws Exception {
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn("not-a-template-node");

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("", result);
  }
}
