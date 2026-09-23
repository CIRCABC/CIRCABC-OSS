package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import java.io.IOException;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Alfresco webscript endpoint that serves the bulk invite template file for download.
 *
 * <p>This endpoint streams a template (typically a spreadsheet) that users can fill in
 * and re-upload to invite multiple members at once. It extends {@link AbstractWebScript}
 * rather than a declarative webscript because it writes the raw file content directly to
 * the HTTP response instead of rendering a FreeMarker template. The generation and
 * writing of the template is delegated to {@link UsersApi#writeBulkInviteTemplate}.</p>
 *
 * <p>The HTTP method (GET) and URL are defined by the associated webscript descriptor
 * ({@code *.get.desc.xml}).</p>
 *
 * @author schwerr
 */
public class BulkInviteDownloadTemplate extends AbstractWebScript {

  /** Logger used to report failures while producing the bulk invite template. */
  static final Log logger = LogFactory.getLog(BulkInviteDownloadTemplate.class);

  /** API used to build and write the bulk invite template to the response stream. */
  @Autowired
  private UsersApi usersApi;

  /**
   * Handles the request by writing the bulk invite template directly to the response.
   *
   * <p>Multilingual (ML) property awareness is temporarily enabled while the template is
   * produced so that localized property values are resolved correctly, and the original
   * ML-aware state is always restored afterwards.</p>
   *
   * @param req the incoming webscript request
   * @param res the webscript response the template content is written to
   * @throws IOException if the template cannot be generated or written to the response
   * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
   * org.springframework.extensions.webscripts.WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(true);

      this.usersApi.writeBulkInviteTemplate(res);
    } catch (Exception e) {
      logger.error("Could not export members.", e);
      throw new IOException("Could not export members.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }
}
