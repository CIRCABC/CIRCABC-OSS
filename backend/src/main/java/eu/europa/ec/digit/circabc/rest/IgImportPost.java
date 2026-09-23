package eu.europa.ec.digit.circabc.rest;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.europa.ec.digit.circabc.rest.service.migration.IgImportService;
import eu.europa.ec.digit.circabc.rest.service.migration.ImportResult;
import io.swagger.util.CurrentUserPermissionCheckerService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco Web Script endpoint that imports an Interest Group from an uploaded
 * XML document.
 *
 * <p>Bound to the HTTP <strong>POST</strong> method (as implied by the
 * {@code Post} suffix in the class name) at the URL
 * {@code POST /circabc/ig/{categoryId}/import}, where {@code categoryId} is the
 * identifier of the target Category under which the Interest Group is created.
 *
 * <p>Unlike most CIRCABC endpoints, this class extends {@link AbstractWebScript}
 * directly (rather than {@code CircabcDeclarativeWebScript}) so it can consume a
 * multipart form upload and stream the raw request body itself instead of
 * rendering a FreeMarker template.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Access is restricted to Alfresco administrators; non-admin callers
 *       receive an HTTP 403 (Forbidden) JSON response.</li>
 *   <li>The request must be {@code multipart/form-data} and contain an XML file
 *       part; if no file is present an HTTP 400 (Bad Request) is returned.</li>
 *   <li>The uploaded XML is unmarshalled into an {@link ImportRoot} JAXB object
 *       and delegated to {@link IgImportService#importIg(String, ImportRoot)}.</li>
 *   <li>On success a JSON body reports the number of nodes created and the
 *       number of errors; unexpected failures yield an HTTP 500 with the error
 *       message.</li>
 * </ul>
 *
 * <p>The surrounding transaction is managed by the web script descriptor
 * configuration rather than by this class.
 */
public class IgImportPost extends AbstractWebScript {

  /** Logger for tracing the import lifecycle and reporting failures. */
  private static final Log logger = LogFactory.getLog(IgImportPost.class);

  /** Service used to verify that the current user is an Alfresco administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService permissionCheckerService;

  /** Service that performs the actual Interest Group import from the parsed XML. */
  @Autowired
  private IgImportService igImportService;

  /**
   * Handles the Interest Group import request.
   *
   * <p>Validates that the caller is an Alfresco administrator, extracts the
   * {@code categoryId} template variable, reads the uploaded XML file from the
   * multipart request, unmarshals it into an {@link ImportRoot}, and delegates
   * the import to {@link IgImportService}. The outcome (nodes created and error
   * count, or an error message) is written directly to the response as JSON.
   *
   * @param req the web script request; expected to carry a {@code categoryId}
   *            template variable and a multipart body containing the XML file
   * @param res the web script response used to write the JSON result and set
   *            the appropriate HTTP status code
   * @throws java.io.IOException if writing to the response output fails
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws java.io.IOException {
    logger.info("IgImportPost: execute called");

    if (!permissionCheckerService.isAlfrescoAdmin()) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      res.getWriter().write("{\"error\":\"Admin only\"}");
      return;
    }

    Map<String, String> vars = req.getServiceMatch().getTemplateVars();
    String categoryId = vars.get("categoryId");
    logger.info("IgImportPost: categoryId=" + categoryId);

    try {
      InputStream xmlStream = null;
      FormData form = (FormData) req.parseContent();
      if (form != null && form.getIsMultiPart()) {
        for (FormData.FormField field : form.getFields()) {
          if (field.getIsFile()) {
            xmlStream = field.getInputStream();
            break;
          }
        }
      }
      if (xmlStream == null) {
        logger.error("IgImportPost: No XML file in request");
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("{\"error\":\"No XML file uploaded\"}");
        return;
      }

      JAXBContext ctx = JAXBContext.newInstance(ImportRoot.class);
      Unmarshaller um = ctx.createUnmarshaller();
      ImportRoot importRoot = (ImportRoot) um.unmarshal(xmlStream);
      logger.info("IgImportPost: XML unmarshalled OK");

      ImportResult result = igImportService.importIg(categoryId, importRoot);

      logger.info(
        "IgImportPost: done. nodesCreated=" +
          result.getNodesCreated() +
          " errors=" +
          result.getErrors().size()
      );
      if (!result.getErrors().isEmpty()) {
        logger.error("IgImportPost: errors=" + result.getErrors());
      }

      res.setContentType("application/json");
      res
        .getWriter()
        .write(
          "{\"message\":\"Import completed\",\"nodesCreated\":" +
            result.getNodesCreated() +
            ",\"errors\":" +
            result.getErrors().size() +
            "}"
        );
    } catch (Exception e) {
      logger.error("IgImportPost: FAILED", e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res
        .getWriter()
        .write("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
    }
  }
}
