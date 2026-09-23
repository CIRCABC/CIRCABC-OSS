package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class BulkDownloadTest {

  private BulkDownload bulkDownload;
  private ContentApi contentApi;
  private WebScriptRequest req;
  private WebScriptResponse res;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkDownload.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(bulkDownload, value);
  }

  @Before
  public void setUp() throws Exception {
    bulkDownload = new BulkDownload();
    contentApi = mock(ContentApi.class);
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);
    setField("contentApi", contentApi);
  }

  @Test
  public void testExecute_whenValidNodeIds_thenBuildsZip() throws Exception {
    String[] nodeIds = new String[] { "id1", "id2" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(res.getOutputStream()).thenReturn(new ByteArrayOutputStream());

    bulkDownload.execute(req, res);

    verify(res).setHeader(
      "Content-Disposition",
      "attachment;filename=bulk.zip"
    );
    verify(res).setContentType("application/zip;charset=UTF-8");
    verify(contentApi).buildZip(eq(nodeIds), any(OutputStream.class));
  }

  @Test(expected = IOException.class)
  public void testExecute_whenNodeIdsNull_thenThrowsIOException()
    throws Exception {
    when(req.getParameterValues("nodeIds")).thenReturn(null);

    bulkDownload.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenNodeIdsEmpty_thenThrowsIOException()
    throws Exception {
    when(req.getParameterValues("nodeIds")).thenReturn(new String[] {});

    bulkDownload.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenBuildZipFails_thenThrowsIOException()
    throws Exception {
    String[] nodeIds = new String[] { "id1" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(res.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    doThrow(new IOException("zip error"))
      .when(contentApi)
      .buildZip(eq(nodeIds), any(OutputStream.class));

    bulkDownload.execute(req, res);
  }

  @Test
  public void testExecute_whenOutOfMemory_thenSets507Status() throws Exception {
    String[] nodeIds = new String[] { "id1" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(res.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    doThrow(new OutOfMemoryError("heap"))
      .when(contentApi)
      .buildZip(eq(nodeIds), any(OutputStream.class));

    bulkDownload.execute(req, res);

    verify(res).setStatus(507);
  }
}
