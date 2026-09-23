package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.UserModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class UserNameOrEcasUserNameMethodTest {

  private UserNameOrEcasUserNameMethod method;
  private NodeService nodeService;
  private CircabcConfig circabcConfig;
  private NodeRef nodeRef;

  @Before
  public void setUp() throws Exception {
    method = new UserNameOrEcasUserNameMethod();
    nodeService = mock(NodeService.class);
    circabcConfig = mock(CircabcConfig.class);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");

    method.setNodeService(nodeService);

    Field configField = UserNameOrEcasUserNameMethod.class.getDeclaredField(
      "circabcConfig"
    );
    configField.setAccessible(true);
    configField.set(method, circabcConfig);
  }

  @Test
  public void testGetResult_whenUseLDAPAndEcasUserNameExists_thenReturnsEcasUserName() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_ECAS_USER_NAME, "ecasUser");
    props.put(ContentModel.PROP_USERNAME, "regularUser");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);
    when(circabcConfig.isUseLDAP()).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals("ecasUser", result);
  }

  @Test
  public void testGetResult_whenUseLDAPAndEcasUserNameNull_thenFallsBackToUsername() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_ECAS_USER_NAME, null);
    props.put(ContentModel.PROP_USERNAME, "regularUser");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);
    when(circabcConfig.isUseLDAP()).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals("regularUser", result);
  }

  @Test
  public void testGetResult_whenNotUseLDAP_thenReturnsUsername() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_ECAS_USER_NAME, "ecasUser");
    props.put(ContentModel.PROP_USERNAME, "regularUser");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    String result = method.getResult(nodeRef);

    assertEquals("regularUser", result);
  }

  @Test
  public void testGetResult_whenNotUseLDAPAndNoUsername_thenReturnsNull() {
    Map<QName, Serializable> props = new HashMap<>();
    when(nodeService.getProperties(nodeRef)).thenReturn(props);
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    String result = method.getResult(nodeRef);

    assertNull(result);
  }
}
