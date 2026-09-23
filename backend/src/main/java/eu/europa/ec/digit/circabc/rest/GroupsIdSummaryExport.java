package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
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
 * Alfresco web script endpoint that exports a summary of an Interest Group (IG).
 *
 * <p>The class name maps to the REST route {@code groups/{id}/summary/export} and,
 * following the CIRCABC naming convention, implements the HTTP {@code GET} operation
 * that produces a downloadable summary export for the group identified by {@code id}.
 *
 * <p>Unlike most CIRCABC endpoints, this class extends {@link AbstractWebScript}
 * directly (rather than {@code CircabcDeclarativeWebScript}) because it streams a
 * binary/export payload straight to the {@link WebScriptResponse} instead of
 * rendering a FreeMarker JSON template.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} — path variable identifying the Interest Group to export.</li>
 *   <li>{@code format} — request parameter selecting the export format; must be one
 *       of {@code csv}, {@code xls} or {@code xml} (case-insensitive).</li>
 *   <li>{@code type} — request parameter selecting the export content; must be one
 *       of {@code statistics} or {@code timeline} (case-insensitive).</li>
 * </ul>
 *
 * <p>Access is restricted to group administrators: the current user must pass the
 * {@link CurrentUserPermissionCheckerService#isGroupAdmin(String)} check, otherwise
 * an {@link AccessDeniedException} is raised.
 */
public class GroupsIdSummaryExport extends AbstractWebScript {

  /** Logger used to report export failures for this endpoint. */
  static final Log logger = LogFactory.getLog(GroupsIdSummaryExport.class);

  /** API used to perform the actual summary export and write it to the response. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user is an administrator of the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the export request for a group summary.
   *
   * <p>Resolves the {@code id} path variable and the {@code format} and {@code type}
   * request parameters, verifies that the current user is a group administrator,
   * enables ML-aware property resolution for the duration of the call, and delegates
   * the export to {@link GroupsApi#exportSummary(String, String, String,
   * WebScriptResponse)}. The previous ML-aware state is always restored on completion.
   *
   * @param req the incoming web script request; supplies the {@code id} path variable
   *            and the {@code format} and {@code type} parameters
   * @param res the web script response the generated export is written to
   * @throws IOException if the user lacks permission, if a required parameter is
   *                     missing or invalid, or if the export otherwise fails; the
   *                     originating exception is wrapped and logged
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
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "Impossible to get the summary of the Interest Group, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(true);

      String format = req.getParameter("format");

      if (
        (format == null) ||
        !("csv".equalsIgnoreCase(format) ||
          "xls".equalsIgnoreCase(format) ||
          "xml".equalsIgnoreCase(format))
      ) {
        throw new IllegalArgumentException(
          "Export 'format' must be CSV, XML or XLS"
        );
      }

      String type = req.getParameter("type");

      if (
        (type == null) ||
        !("statistics".equalsIgnoreCase(type) ||
          "timeline".equalsIgnoreCase(type))
      ) {
        throw new IllegalArgumentException(
          "Export 'type' must be statistics or timeline"
        );
      }

      this.groupsApi.exportSummary(
        id,
        format.toLowerCase(),
        type.toLowerCase(),
        res
      );
    } catch (Exception e) {
      logger.error("Could not export members.", e);
      throw new IOException("Could not export members.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }
}
