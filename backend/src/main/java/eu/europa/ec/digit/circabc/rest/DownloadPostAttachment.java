package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Alfresco webscript endpoint that streams a forum post attachment back to the
 * caller as raw binary content.
 *
 * <p>Bound to the HTTP <b>GET</b> operation on the URL pattern
 * {@code post/{id}/attachment/download} (Spring bean
 * {@code webscript.circabc.post.id.attachment.download.get}). The single input
 * is the {@code id} template variable identifying the attachment node.
 *
 * <p>Unlike JSON-rendering endpoints, this class extends {@link AbstractWebScript}
 * and writes the attachment bytes directly to the response output stream instead
 * of returning a model for a FreeMarker template. Before serving the content it
 * verifies that the current user holds newsgroup access permission
 * ({@link NewsGroupPermissions#NWSACCESS}) on the target node.
 *
 * @author schwerr
 */
public class DownloadPostAttachment extends AbstractWebScript {

  /** Logger used to report attachment download failures. */
  static final Log logger = LogFactory.getLog(DownloadPostAttachment.class);

  /** API used to resolve and stream the attachment content by its id. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to check that the current user is authorized to access the attachment. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request by streaming the requested attachment to the response.
   *
   * <p>The attachment {@code id} is read from the request template variables. The
   * current user's newsgroup access permission is verified before the content is
   * served, multilingual-awareness is enabled so the hidden attachment content is
   * resolved correctly, and the attachment bytes are written to the response
   * output stream. The previous multilingual-awareness state is always restored.
   *
   * @param req the web script request; must provide the {@code id} template variable
   * @param res the web script response whose output stream receives the attachment bytes
   * @throws IOException if the user lacks the required permission or the attachment
   *     could not be resolved or streamed
   * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
   * org.springframework.extensions.webscripts.WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Impossible to download attachment, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(true);

      try (OutputStream outStream = res.getOutputStream()) {
        topicsApi.getAttachment(id, outStream);
      }
    } catch (Exception e) {
      logger.error("Could not download attachment.", e);
      throw new IOException("Could not download attachment.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }
}
