package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.model.NotifiableUser;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NewContentNotificationsPostTest {

  private NewContentNotificationsPost webScript;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;

  @Before
  public void setUp() throws Exception {
    webScript = new NewContentNotificationsPost();
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    notificationService = mock(NotificationService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );

    setField("currentUserPermissionCheckerService", permissionCheckerService);
    setField("notificationService", notificationService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenNotifiesUsers()
    throws Exception {
    String parentId = "parent-node-id";
    String childId = "child-node-id";

    WebScriptRequest req = mockRequest(parentId, "[\"" + childId + "\"]", null);
    when(
      permissionCheckerService.hasAlfrescoWritePermission(childId)
    ).thenReturn(true);

    Set<NotifiableUser> users = new HashSet<>();
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      parentId
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(parentRef)
    ).thenReturn(users);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationService).notifyNewFiles(
      eq(parentRef),
      anyList(),
      eq(users),
      eq(MailTemplate.NOTIFY_DOC_BULK)
    );
  }

  @Test
  public void testExecuteImpl_whenNoWritePermission_thenForbidden()
    throws Exception {
    String parentId = "parent-node-id";
    String childId = "child-node-id";

    WebScriptRequest req = mockRequest(parentId, "[\"" + childId + "\"]", null);
    when(
      permissionCheckerService.hasAlfrescoWritePermission(childId)
    ).thenReturn(false);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(notificationService);
  }

  @Test
  public void testExecuteImpl_whenIOException_thenInternalServerError()
    throws Exception {
    String parentId = "parent-node-id";

    WebScriptRequest req = mockRequestWithBadContent(parentId);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    String parentId = "parent-node-id";
    String childId = "child-node-id";

    WebScriptRequest req = mockRequest(parentId, "[\"" + childId + "\"]", "fr");
    when(
      permissionCheckerService.hasAlfrescoWritePermission(childId)
    ).thenReturn(true);

    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      parentId
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(parentRef)
    ).thenReturn(new HashSet<>());

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationService).notifyNewFiles(
      eq(parentRef),
      anyList(),
      anySet(),
      eq(MailTemplate.NOTIFY_DOC_BULK)
    );
  }

  @Test
  public void testExecuteImpl_whenMultipleNodes_thenValidatesAll()
    throws Exception {
    String parentId = "parent-node-id";
    String childId1 = "child-1";
    String childId2 = "child-2";

    WebScriptRequest req = mockRequest(
      parentId,
      "[\"" + childId1 + "\",\"" + childId2 + "\"]",
      null
    );
    when(
      permissionCheckerService.hasAlfrescoWritePermission(childId1)
    ).thenReturn(true);
    when(
      permissionCheckerService.hasAlfrescoWritePermission(childId2)
    ).thenReturn(false);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  private WebScriptRequest mockRequest(
    String parentId,
    String body,
    String language
  ) throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", parentId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(body);
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private WebScriptRequest mockRequestWithBadContent(String parentId)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", parentId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new java.io.IOException("bad"));
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NewContentNotificationsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
