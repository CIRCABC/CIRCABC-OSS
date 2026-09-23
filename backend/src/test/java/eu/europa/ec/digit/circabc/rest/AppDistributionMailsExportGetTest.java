package eu.europa.ec.digit.circabc.rest;

import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AppDistributionMailsExportGetTest {

  private AppDistributionMailsExportGet webScript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    webScript = new AppDistributionMailsExportGet();
    appMessageApi = mock(AppMessageApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    setField("appMessageApi", appMessageApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppDistributionMailsExportGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecute_whenAlfrescoAdmin_thenExportsExcel()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    Workbook workbook = mock(Workbook.class);
    OutputStream outputStream = mock(OutputStream.class);
    when(appMessageApi.getdistributionListAsExcel()).thenReturn(workbook);
    when(res.getOutputStream()).thenReturn(outputStream);

    webScript.execute(req, res);

    verify(res).setHeader(
      "Content-Disposition",
      "attachment;filename=distribution-list.xls"
    );
    verify(res).setContentType("application/vnd.ms-excel;charset=UTF-8");
    verify(workbook).write(outputStream);
    verify(outputStream).close();
  }

  @Test
  public void testExecute_whenCircabcAdmin_thenExportsExcel() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    Workbook workbook = mock(Workbook.class);
    OutputStream outputStream = mock(OutputStream.class);
    when(appMessageApi.getdistributionListAsExcel()).thenReturn(workbook);
    when(res.getOutputStream()).thenReturn(outputStream);

    webScript.execute(req, res);

    verify(workbook).write(outputStream);
  }

  @Test
  public void testExecute_whenNotAdmin_thenForbidden() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    webScript.execute(req, res);

    verify(res).setStatus(Status.STATUS_FORBIDDEN);
    verify(appMessageApi, never()).getdistributionListAsExcel();
  }

  @Test
  public void testExecute_whenGetOutputStreamThrowsIOException_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    Workbook workbook = mock(Workbook.class);
    when(appMessageApi.getdistributionListAsExcel()).thenReturn(workbook);
    when(res.getOutputStream()).thenThrow(new IOException("test"));

    webScript.execute(req, res);

    verify(res).setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testExecute_whenUnexpectedException_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(appMessageApi.getdistributionListAsExcel()).thenThrow(
      new RuntimeException("unexpected")
    );

    webScript.execute(req, res);

    verify(res).setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
  }
}
