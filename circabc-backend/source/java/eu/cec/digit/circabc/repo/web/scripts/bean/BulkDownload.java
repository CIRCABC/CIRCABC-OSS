package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.ContentApi;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript entry to bulk download set of files as a zip archive
 *
 * @author schwerr
 */
public class BulkDownload extends AbstractWebScript {

  static final Log logger = LogFactory.getLog(BulkDownload.class);

  private ContentApi contentApi;

  /**
   * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
   * org.springframework.extensions.webscripts.WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      String[] nodeIds = req.getParameterValues("nodeIds");

      if ((nodeIds == null) || (nodeIds.length == 0)) {
        throw new IllegalArgumentException("Empty list of 'nodeIds' supplied.");
      }

      this.buildZip(nodeIds, res);
    } catch (AlfrescoRuntimeException ae) {
      logger.error(ae.getMessage(), ae);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error("Could not produce zip file.", e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw new IOException("Could not produce zip file.", e);
    } catch (OutOfMemoryError e) {
      logger.error(e.getMessage(), e);
      res.setStatus(507);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  private void buildZip(String[] nodeIds, WebScriptResponse response)
    throws IOException, XMLStreamException {
    try (OutputStream outputStream = response.getOutputStream()) {
      response.setHeader("Content-Disposition", "attachment;filename=bulk.zip");
      response.setContentType("application/zip;charset=UTF-8");
      this.contentApi.buildZip(nodeIds, outputStream);
    } catch (Exception e) {
      logger.error("Could not build zip file.", e);
      throw e;
    }
  }

  /**
   * @param contentApi the contentApi to set
   */
  public void setContentApi(ContentApi contentApi) {
    this.contentApi = contentApi;
  }
}
