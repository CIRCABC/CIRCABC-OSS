package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.LibraryApi;
import io.swagger.model.GroupDeletionReport;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CheckDeletionGroupsGetTest {

  private CheckDeletionGroupsGet webscript;
  private NodeService nodeService;
  private LibraryApi libraryApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String GROUP_ID = "group-id-123";
  private static final String CATEGORY_ID = "category-id-456";

  @Before
  public void setUp() throws Exception {
    webscript = new CheckDeletionGroupsGet();
    nodeService = mock(NodeService.class);
    libraryApi = mock(LibraryApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("nodeService", nodeService);
    setField("libraryApi", libraryApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", GROUP_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      GROUP_ID
    );
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      CATEGORY_ID
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(groupRef)).thenReturn(childAssoc);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenReturnsReport() {
    when(permissionCheckerService.isCategoryAdmin(CATEGORY_ID)).thenReturn(
      true
    );
    when(libraryApi.getLockedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedProfiles(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.containsKey("report"));
    GroupDeletionReport report = (GroupDeletionReport) model.get("report");
    assertNotNull(report);
  }

  @Test
  public void testExecuteImpl_whenDirAdmin_thenReturnsReport() {
    when(permissionCheckerService.isCategoryAdmin(CATEGORY_ID)).thenReturn(
      false
    );
    when(permissionCheckerService.isInterestGroupDirAdmin(GROUP_ID)).thenReturn(
      true
    );
    when(libraryApi.getLockedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedProfiles(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.containsKey("report"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(permissionCheckerService.isCategoryAdmin(CATEGORY_ID)).thenReturn(
      false
    );
    when(permissionCheckerService.isInterestGroupDirAdmin(GROUP_ID)).thenReturn(
      false
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      GROUP_ID
    );
    when(nodeService.getPrimaryParent(groupRef)).thenThrow(
      new InvalidNodeRefException(groupRef)
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionCheckerService.isCategoryAdmin(CATEGORY_ID)).thenReturn(
      true
    );
    when(libraryApi.getLockedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedNodes(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );
    when(libraryApi.getSharedProfiles(GROUP_ID)).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.containsKey("report"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CheckDeletionGroupsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
