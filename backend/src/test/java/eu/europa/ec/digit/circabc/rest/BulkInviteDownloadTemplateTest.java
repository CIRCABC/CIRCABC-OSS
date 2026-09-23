package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class BulkInviteDownloadTemplateTest {

  private BulkInviteDownloadTemplate webscript;
  private UsersApi usersApi;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    webscript = new BulkInviteDownloadTemplate();
    usersApi = mock(UsersApi.class);
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    Field field = BulkInviteDownloadTemplate.class.getDeclaredField("usersApi");
    field.setAccessible(true);
    field.set(webscript, usersApi);
  }

  @Test
  public void testExecute_whenSuccess_thenWritesBulkInviteTemplate()
    throws Exception {
    webscript.execute(req, res);

    verify(usersApi).writeBulkInviteTemplate(res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenUsersApiThrows_thenThrowsIOException()
    throws Exception {
    doThrow(new RuntimeException("fail"))
      .when(usersApi)
      .writeBulkInviteTemplate(res);

    webscript.execute(req, res);
  }

  @Test
  public void testExecute_whenUsersApiThrows_thenExceptionMessageContainsExportMembers() {
    doThrow(new RuntimeException("fail"))
      .when(usersApi)
      .writeBulkInviteTemplate(res);

    try {
      webscript.execute(req, res);
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("Could not export members.", e.getMessage());
      assertNotNull(e.getCause());
    }
  }
}
