package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Profile;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryExportedProfilesGetTest {

  private CategoryExportedProfilesGet webscript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new CategoryExportedProfilesGet();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsProfiles()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("ignoreIgId")).thenReturn("ig-id-456");
    when(
      currentUserPermissionCheckerService.isInterestGroupDirAdmin("ig-id-456")
    ).thenReturn(true);

    List<Profile> profiles = Collections.singletonList(new Profile());
    when(
      categoriesApi.categoriesIdExportedProfilesGet("cat-id-123", "ig-id-456")
    ).thenReturn(profiles);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(profiles, model.get("profiles"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("ignoreIgId")).thenReturn("ig-id-456");
    when(
      currentUserPermissionCheckerService.isInterestGroupDirAdmin("ig-id-456")
    ).thenReturn(true);
    when(
      categoriesApi.categoriesIdExportedProfilesGet("cat-id-123", "ig-id-456")
    ).thenReturn(Collections.emptyList());

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("profiles"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("ignoreIgId")).thenReturn("ig-id-456");
    when(
      currentUserPermissionCheckerService.isInterestGroupDirAdmin("ig-id-456")
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("ignoreIgId")).thenReturn("ig-id-456");
    when(
      currentUserPermissionCheckerService.isInterestGroupDirAdmin("ig-id-456")
    ).thenReturn(true);
    when(
      categoriesApi.categoriesIdExportedProfilesGet("cat-id-123", "ig-id-456")
    ).thenThrow(
      new InvalidNodeRefException(
        "bad ref",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("ignoreIgId")).thenReturn("ig-id-456");
    when(
      currentUserPermissionCheckerService.isInterestGroupDirAdmin("ig-id-456")
    ).thenReturn(true);
    when(
      categoriesApi.categoriesIdExportedProfilesGet("cat-id-123", "ig-id-456")
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryExportedProfilesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
