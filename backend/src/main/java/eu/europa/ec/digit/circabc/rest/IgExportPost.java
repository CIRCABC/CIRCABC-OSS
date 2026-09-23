package eu.europa.ec.digit.circabc.rest;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.europa.ec.digit.circabc.rest.service.migration.IgExportService;
import io.swagger.util.CurrentUserPermissionCheckerService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;

/**
 * Alfresco web script endpoint handling {@code POST /circabc/ig/{id}/export}.
 *
 * <p>Exports a complete Interest Group (IG) as an XML document using the
 * {@link ImportRoot} JAXB model. The IG to export is identified by the
 * {@code id} URL template variable, which is interpreted as the node UUID of
 * the IG within the Alfresco workspace SpacesStore.</p>
 *
 * <p>The HTTP method implied by the class name is {@code POST}. Behavior:</p>
 * <ul>
 *   <li>Access is restricted to Alfresco administrators; non-admin callers
 *       receive an HTTP 403 (Forbidden) JSON error response.</li>
 *   <li>If the referenced IG node does not exist, an HTTP 404 (Not Found)
 *       JSON error response is returned.</li>
 *   <li>On success, the IG is exported via {@link IgExportService} and the
 *       resulting {@link ImportRoot} is marshalled to formatted XML
 *       ({@code application/xml}, UTF-8) written to the response body.</li>
 *   <li>Any failure during export or marshalling results in an HTTP 500
 *       (Internal Server Error) JSON error response.</li>
 * </ul>
 *
 * <p>Unlike most CIRCABC endpoints that extend {@code CircabcDeclarativeWebScript},
 * this class extends {@link AbstractWebScript} directly so it can stream a raw
 * XML payload rather than render a FreeMarker template.</p>
 */
public class IgExportPost extends AbstractWebScript {

  /** Logger used to report export and marshalling failures. */
  private static final Log logger = LogFactory.getLog(IgExportPost.class);

  /** Alfresco node service used to verify that the target IG node exists. */
  @Autowired
  @Qualifier("NodeService")
  private NodeService nodeService;

  /** Service used to enforce that only Alfresco administrators may export. */
  @Autowired
  private CurrentUserPermissionCheckerService permissionCheckerService;

  /** Service that performs the actual export of an IG into an {@link ImportRoot}. */
  @Autowired
  private IgExportService igExportService;

  /**
   * Handles the export request.
   *
   * <p>Validates administrator access, resolves the IG node from the {@code id}
   * URL template variable, exports it to an {@link ImportRoot}, and writes the
   * marshalled XML to the response. Error conditions are translated into the
   * appropriate HTTP status codes with a JSON error body (403 for non-admins,
   * 404 when the IG is missing, 500 on unexpected failures).</p>
   *
   * @param req the web script request; supplies the {@code id} template
   *            variable identifying the IG node to export
   * @param res the web script response; receives either the exported XML
   *            payload or a JSON error message with the corresponding status
   * @throws java.io.IOException if writing to the response output fails
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws java.io.IOException {
    if (!permissionCheckerService.isAlfrescoAdmin()) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      res.getWriter().write("{\"error\":\"Admin only\"}");
      return;
    }

    Map<String, String> vars = req.getServiceMatch().getTemplateVars();
    String igId = vars.get("id");

    try {
      NodeRef igRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        igId
      );
      if (!nodeService.exists(igRef)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("{\"error\":\"IG not found\"}");
        return;
      }

      ImportRoot importRoot = igExportService.exportIg(igRef);

      JAXBContext ctx = JAXBContext.newInstance(ImportRoot.class);
      Marshaller m = ctx.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      res.setContentType("application/xml");
      res.setContentEncoding("UTF-8");
      StringWriter sw = new StringWriter();
      m.marshal(importRoot, sw);
      res.getWriter().write(sw.toString());
    } catch (Exception e) {
      logger.error("Export failed", e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
    }
  }
}
