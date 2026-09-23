package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsInformationNewsPostTest {

  private GroupsInformationNewsPost webScript;
  private InformationApi informationApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private ThreadPoolExecutor asyncThreadPoolExecutor;
  private TransactionService transactionService;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsInformationNewsPost();
    informationApi = mock(InformationApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    asyncThreadPoolExecutor = mock(ThreadPoolExecutor.class);
    transactionService = mock(TransactionService.class);
    notificationService = mock(NotificationService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );

    setField("informationApi", informationApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("asyncThreadPoolExecutor", asyncThreadPoolExecutor);
    setField("transactionService", transactionService);
    setField("notificationService", notificationService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );

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
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsNewsModel()
    throws Exception {
    String igId = "test-ig-id";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    WebScriptRequest req = mockRequest(igId, buildValidNewsJson());

    jakarta.transaction.UserTransaction trx = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );
    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq(infRef.getId()),
        any()
      )
    ).thenReturn(true);

    News createdNews = new News();
    createdNews.setId("news-id");
    when(
      informationApi.groupsIdInformationNewsPost(eq(igId), any(News.class))
    ).thenReturn(createdNews);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(createdNews, model.get("newsInfo"));
    verify(trx).commit();
    verify(asyncThreadPoolExecutor).execute(any(Runnable.class));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String igId = "test-ig-id";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    WebScriptRequest req = mockRequest(igId, buildValidNewsJson());

    jakarta.transaction.UserTransaction trx = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );
    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq(infRef.getId()),
        any()
      )
    ).thenReturn(false);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String igId = "bad-id";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);

    WebScriptRequest req = mockRequest(igId, buildValidNewsJson());

    jakarta.transaction.UserTransaction trx = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );
    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "not found",
        igRef
      )
    );

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    String igId = "test-ig-id";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    WebScriptRequest req = mockRequest(igId, buildValidNewsJson());
    when(req.getParameter("language")).thenReturn("fr");

    jakarta.transaction.UserTransaction trx = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );
    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq(infRef.getId()),
        any()
      )
    ).thenReturn(true);

    News createdNews = new News();
    createdNews.setId("news-id-2");
    when(
      informationApi.groupsIdInformationNewsPost(eq(igId), any(News.class))
    ).thenReturn(createdNews);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(createdNews, model.get("newsInfo"));
  }

  @Test
  public void testExecuteImpl_whenTransactionFails_thenReturnsInternalError()
    throws Exception {
    String igId = "test-ig-id";

    WebScriptRequest req = mockRequest(igId, buildValidNewsJson());

    jakarta.transaction.UserTransaction trx = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );
    doThrow(new jakarta.transaction.SystemException("fail")).when(trx).begin();

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private WebScriptRequest mockRequest(String igId, String body)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);

    java.util.Map<String, String> templateVars = new java.util.HashMap<>();
    templateVars.put("igId", igId);
    String template = "/circabc/groups/{igId}/information/news";
    Match match = new Match(template, templateVars, template);
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(body);
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private String buildValidNewsJson() {
    return "{\"content\":\"Hello\",\"pattern\":\"iframe\",\"layout\":\"normal\",\"size\":1,\"title\":{\"en\":\"Test\"}}";
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsInformationNewsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
