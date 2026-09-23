package eu.europa.ec.digit.circabc.rest.aspect;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class CircabcRootAspectTest {

  private CircabcRootAspect aspect;
  private AuthorityService authorityService;
  private NodeService nodeService;
  private PolicyComponent policyComponent;
  private CircabcService circabcService;

  @Before
  public void setUp() throws Exception {
    aspect = new CircabcRootAspect();
    authorityService = mock(AuthorityService.class);
    nodeService = mock(NodeService.class);
    policyComponent = mock(PolicyComponent.class);
    circabcService = mock(CircabcService.class);

    setField("authorityService", authorityService);
    setField("nodeService", nodeService);
    setField("policyComponent", policyComponent);
    setField("circabcService", circabcService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcRootAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aspect, value);
  }

  @Test
  public void testOnUpdateProperties_whenNameIsCircaBC_thenNoException() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Map<QName, Serializable> before = new HashMap<>();
    Map<QName, Serializable> after = new HashMap<>();
    after.put(ContentModel.PROP_NAME, "CircaBC");

    aspect.onUpdateProperties(nodeRef, before, after);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOnUpdateProperties_whenNameIsNotCircaBC_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Map<QName, Serializable> before = new HashMap<>();
    Map<QName, Serializable> after = new HashMap<>();
    after.put(ContentModel.PROP_NAME, "WrongName");

    aspect.onUpdateProperties(nodeRef, before, after);
  }

  @Test
  public void testOnUpdateProperties_whenAfterIsNull_thenNoException() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Map<QName, Serializable> before = new HashMap<>();

    aspect.onUpdateProperties(nodeRef, before, null);
  }

  @Test
  public void testOnUpdateProperties_whenNamePropertyIsNull_thenNoException() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Map<QName, Serializable> before = new HashMap<>();
    Map<QName, Serializable> after = new HashMap<>();

    aspect.onUpdateProperties(nodeRef, before, after);
  }

  @Test
  public void testBeforeDeleteNode_whenSuccessful_thenDeletesAuthorityAndAll() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(
      nodeService.getProperty(
        nodeRef,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMasters");

    aspect.beforeDeleteNode(nodeRef);

    verify(authorityService).deleteAuthority("GROUP_CircaBCMasters", true);
    verify(circabcService).deleteAll();
  }

  @Test
  public void testBeforeDeleteNode_whenExceptionThrown_thenSwallowsError() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(
      nodeService.getProperty(
        nodeRef,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenThrow(new RuntimeException("fail"));

    aspect.beforeDeleteNode(nodeRef);

    verify(circabcService, never()).deleteAll();
  }

  @Test
  public void testInit_registersPoliciess() {
    aspect.init();

    verify(policyComponent, times(2)).bindClassBehaviour(
      any(),
      any(QName.class),
      any()
    );
  }
}
