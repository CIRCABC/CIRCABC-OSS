package eu.europa.ec.digit.circabc.rest.aspect;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.junit.Before;
import org.junit.Test;

public class CategoryAspectTest {

  private CategoryAspect categoryAspect;
  private PolicyComponent policyComponent;
  private NodeService nodeService;
  private AuthorityService authorityService;
  private CircabcService circabcService;

  @Before
  public void setUp() throws Exception {
    categoryAspect = new CategoryAspect();

    policyComponent = mock(PolicyComponent.class);
    nodeService = mock(NodeService.class);
    authorityService = mock(AuthorityService.class);
    circabcService = mock(CircabcService.class);

    setField("policyComponent", policyComponent);
    setField("nodeService", nodeService);
    setField("authorityService", authorityService);
    setField("circabcService", circabcService);

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
    Field admin = AuthenticationUtil.class.getDeclaredField(
      "defaultAdminUserName"
    );
    admin.setAccessible(true);
    admin.set(null, "admin");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");
  }

  @Test
  public void testInitialise_bindsBeforeDeleteNodePolicy() {
    categoryAspect.initialise();
    verify(policyComponent).bindClassBehaviour(
      any(),
      eq(CircabcModel.ASPECT_CATEGORY),
      any()
    );
  }

  @Test
  public void testBeforeDeleteNode_deletesMasterGroupAndCategory() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(
      nodeService.getProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP)
    ).thenReturn("TestMasterGroup");

    categoryAspect.beforeDeleteNode(nodeRef);

    verify(authorityService).deleteAuthority("GROUP_TestMasterGroup", true);
    verify(circabcService).deleteCategory(nodeRef);
  }

  @Test
  public void testDeleteAuthorityAsAdmin_deletesAuthority() {
    categoryAspect.deleteAuthorityAsAdmin("GROUP_SomeGroup");
    verify(authorityService).deleteAuthority("GROUP_SomeGroup", true);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryAspect, value);
  }
}
