package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class DownloadPostAttachmentTest {

  private DownloadPostAttachment downloadPostAttachment;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    downloadPostAttachment = new DownloadPostAttachment();

    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("topicsApi", topicsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-attachment-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecute_whenHasPermission_thenDownloadsAttachment()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-attachment-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(true);

    OutputStream outputStream = mock(OutputStream.class);
    when(res.getOutputStream()).thenReturn(outputStream);

    downloadPostAttachment.execute(req, res);

    verify(topicsApi).getAttachment("test-attachment-id", outputStream);
  }

  @Test
  public void testExecute_whenNoPermission_thenThrowsIOException() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-attachment-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(false);

    try {
      downloadPostAttachment.execute(req, res);
      fail("Expected IOException");
    } catch (IOException e) {
      assertTrue(e.getMessage().contains("Could not download attachment"));
    }

    verifyNoInteractions(topicsApi);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenGetAttachmentThrows_thenWrapsInIOException()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-attachment-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(true);

    OutputStream outputStream = mock(OutputStream.class);
    when(res.getOutputStream()).thenReturn(outputStream);
    doThrow(new RuntimeException("download failed"))
      .when(topicsApi)
      .getAttachment("test-attachment-id", outputStream);

    downloadPostAttachment.execute(req, res);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DownloadPostAttachment.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(downloadPostAttachment, value);
  }
}
