package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Translations;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentTranslationsGetTest {

  private ContentTranslationsGet webscript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    webscript = new ContentTranslationsGet();
    contentApi = mock(ContentApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("contentApi", contentApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsTranslations()
    throws Exception {
    Translations translations = new Translations();
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(contentApi.contentIdTranslationsGet("test-node-id")).thenReturn(
      translations
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(translations, model.get("translationSet"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(contentApi.contentIdTranslationsGet("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callExecuteImpl() throws Exception {
    Method method = DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webscript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentTranslationsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
