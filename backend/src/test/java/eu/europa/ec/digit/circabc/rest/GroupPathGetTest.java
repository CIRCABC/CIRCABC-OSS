package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.api.GroupsApi;
import io.swagger.api.HeadersApi;
import io.swagger.model.Category;
import io.swagger.model.GroupPath;
import io.swagger.model.Header;
import io.swagger.model.InterestGroup;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

@SuppressWarnings("unchecked")
public class GroupPathGetTest {

  private GroupPathGet groupPathGet;
  private GroupsApi groupsApi;
  private HeadersApi headersApi;
  private CategoriesApi categoriesApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
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

    groupPathGet = new GroupPathGet();

    groupsApi = mock(GroupsApi.class);
    headersApi = mock(HeadersApi.class);
    categoriesApi = mock(CategoriesApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("groupsApi", groupsApi);
    setField("headersApi", headersApi);
    setField("categoriesApi", categoriesApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenGroupIdProvided_thenReturnsGroupPath()
    throws Exception {
    String groupId = "test-group-id";
    String categoryId = "test-category-id";
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      groupId
    );
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      categoryId
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(groupId)
    ).thenReturn(true);

    InterestGroup group = new InterestGroup();
    when(groupsApi.getInterestGroupDetails(groupRef, false)).thenReturn(group);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(nodeService.getPrimaryParent(groupRef)).thenReturn(childAssoc);

    Category category = new Category();
    when(categoriesApi.categoriesIdGet(categoryId)).thenReturn(category);

    Header header = new Header();
    when(headersApi.getHeaderByCategory(categoryId)).thenReturn(header);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    GroupPath groupPath = (GroupPath) model.get("groupPath");
    assertNotNull(groupPath);
    assertEquals(group, groupPath.getGroup());
    assertEquals(category, groupPath.getCategory());
    assertEquals(header, groupPath.getHeader());
  }

  @Test
  public void testExecuteImpl_whenGroupIdNull_thenReturnsEmptyModel()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertFalse(model.containsKey("groupPath"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String groupId = "test-group-id";

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(groupId)
    ).thenReturn(false);

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    String groupId = "test-group-id";
    String categoryId = "test-category-id";
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      groupId
    );
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      categoryId
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("fr");

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(groupId)
    ).thenReturn(true);

    InterestGroup group = new InterestGroup();
    when(groupsApi.getInterestGroupDetails(groupRef, false)).thenReturn(group);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(nodeService.getPrimaryParent(groupRef)).thenReturn(childAssoc);

    Category category = new Category();
    when(categoriesApi.categoriesIdGet(categoryId)).thenReturn(category);

    Header header = new Header();
    when(headersApi.getHeaderByCategory(categoryId)).thenReturn(header);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertNotNull(model.get("groupPath"));
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method = GroupPathGet.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      groupPathGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupPathGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupPathGet, value);
  }
}
