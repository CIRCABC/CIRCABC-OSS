package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ContentApi;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Alfresco webscript endpoint that bulk downloads a set of repository files as a
 * single ZIP archive.
 *
 * <p>Unlike the declarative webscripts in this package, this endpoint extends
 * {@link AbstractWebScript} directly so it can stream the generated archive
 * straight to the HTTP response instead of rendering a FreeMarker template.
 *
 * <p>Behavior: the client supplies one or more {@code nodeIds} request
 * parameters identifying the content nodes to include. The endpoint delegates
 * to {@link ContentApi#buildZip(String[], OutputStream)} to assemble the archive
 * and streams it back with a {@code Content-Disposition: attachment} header and
 * an {@code application/zip} content type (file name {@code bulk.zip}). If no
 * node ids are provided the request fails; if the JVM runs out of memory while
 * building the archive an HTTP 507 (Insufficient Storage) status is returned.
 *
 * @author schwerr
 */
public class BulkDownload extends AbstractWebScript {

  /** Logger for this webscript. */
  static final Log logger = LogFactory.getLog(BulkDownload.class);

  /** API used to assemble the requested nodes into a streamed ZIP archive. */
  @Autowired
  private ContentApi contentApi;

  /**
   * Handles the incoming request by validating the {@code nodeIds} parameter and
   * streaming the resulting ZIP archive to the response.
   *
   * <p>The multilingual-aware flag of {@link MLPropertyInterceptor} is captured
   * before processing and restored afterwards. Errors while building the archive
   * are logged and re-thrown as {@link IOException}, while an
   * {@link OutOfMemoryError} results in an HTTP 507 status rather than a thrown
   * error.
   *
   * @param req the web script request; must contain at least one {@code nodeIds}
   *            parameter value identifying the nodes to download
   * @param res the web script response the ZIP archive is streamed to
   * @throws IOException if no {@code nodeIds} are supplied or the archive cannot
   *                     be produced
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
    } catch (Exception e) {
      logger.error("Could not produce zip file. Error: " + e.getMessage(), e);
      throw new IOException("Could not produce zip file: " + e.getMessage(), e);
    } catch (OutOfMemoryError e) {
      logger.error("Out of memory error while producing zip file", e);
      res.setStatus(507);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /**
   * Sets the archive response headers and streams the ZIP built from the given
   * nodes directly to the response output stream.
   *
   * @param nodeIds  the identifiers of the content nodes to include in the archive
   * @param response the web script response to which the ZIP is written
   * @throws IOException        if the response output stream cannot be written to
   * @throws XMLStreamException if archive assembly fails while producing XML content
   */
  private void buildZip(String[] nodeIds, WebScriptResponse response)
    throws IOException, XMLStreamException {
    try (OutputStream outputStream = response.getOutputStream()) {
      response.setHeader("Content-Disposition", "attachment;filename=bulk.zip");
      response.setContentType("application/zip;charset=UTF-8");
      this.contentApi.buildZip(nodeIds, outputStream);
    } catch (Exception e) {
      logger.error("Could not build zip file. Error: " + e.getMessage(), e);
      throw e;
    }
  }
}
