package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.exception.AlreadyExistsException;
import io.swagger.model.User;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CreateUserTest {

  private CreateUser createUser;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    createUser = new CreateUser();
    usersApi = mock(UsersApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("usersApi", usersApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

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

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenCreatesUser()
    throws Exception {
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(buildUserJson(null));

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertNotNull(model.get("user"));
    User user = (User) model.get("user");
    assertEquals("jdoe", user.getUserId());
    assertEquals("John", user.getFirstname());
    assertEquals("Doe", user.getLastname());
    assertEquals("john@example.com", user.getEmail());
    verify(usersApi).usersPost(any(User.class));
  }

  @Test
  public void testExecuteImpl_whenDirManageMembers_thenCreatesUser()
    throws Exception {
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq("ig-123"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(buildUserJson("ig-123"));

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    verify(usersApi).usersPost(any(User.class));
  }

  @Test(expected = AccessDeniedException.class)
  public void testExecuteImpl_whenNoPermission_thenThrowsAccessDenied()
    throws Exception {
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(
      permissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(buildUserJson(null));

    callExecuteImpl();
  }

  @Test
  public void testExecuteImpl_whenAlreadyExists_thenReturnsConflict()
    throws Exception {
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(buildUserJson(null));
    doThrow(new AlreadyExistsException("User exists"))
      .when(usersApi)
      .usersPost(any(User.class));

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_CONFLICT, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsNotAcceptable()
    throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json{{{");

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() {
    return createUser.executeImpl(req, status, cache);
  }

  private String buildUserJson(String currentIgId) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"userId\":\"jdoe\",");
    sb.append("\"firstname\":\"John\",");
    sb.append("\"lastname\":\"Doe\",");
    sb.append("\"email\":\"john@example.com\",");
    sb.append("\"phone\":\"+123\",");
    sb.append("\"title\":\"Mr\",");
    sb.append("\"companyId\":\"comp1\",");
    sb.append("\"fax\":\"+456\",");
    sb.append("\"urlAddress\":\"http://example.com\",");
    sb.append("\"postalAddress\":\"123 Street\",");
    sb.append("\"description\":\"A user\",");
    sb.append("\"password\":\"secret\"");
    if (currentIgId != null) {
      sb.append(",\"currentIgId\":\"").append(currentIgId).append("\"");
    }
    sb.append("}");
    return sb.toString();
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CreateUser.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(createUser, value);
  }
}
