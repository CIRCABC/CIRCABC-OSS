package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.GroupsApi;
import java.io.IOException;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * REST webscript endpoint that produces the template for the bulk-import index file.
 *
 * <p>Wired to the Spring bean {@code webscript.circabc.groups.import.template.get}, this
 * endpoint answers an HTTP {@code GET} request and streams back a tab-separated
 * {@code index.txt} file (served as {@code text/csv;charset=UTF-8} and offered as a
 * download attachment). The generated file contains only the header row expected by the
 * document import feature (columns such as NAME, TITLE, DESCRIPTION, AUTHOR, the ATTRI1..20
 * metadata columns, TYPE, LANG, OVERWRITE, etc.), giving users a ready-to-fill template.</p>
 *
 * <p>The endpoint takes no request parameters. The actual file generation is delegated to
 * {@link io.swagger.api.GroupsApi#generateImportIndexFileTemplate(WebScriptResponse)}.
 * Because import metadata may be multilingual, the multilingual (ML) property behaviour is
 * temporarily enabled for the duration of the call and restored afterwards.</p>
 *
 * <p>Unlike most CIRCABC endpoints, this class extends {@link AbstractWebScript} directly
 * (rather than {@code CircabcDeclarativeWebScript}) so it can write the binary/file response
 * straight to the output stream instead of rendering a FreeMarker template.</p>
 */
public class GenerateImportIndexFileTemplate extends AbstractWebScript {

  /** Logger used to report failures while generating the import index template. */
  static final Log logger = LogFactory.getLog(
    GenerateImportIndexFileTemplate.class
  );

  /** API bean that performs the actual generation and streaming of the index template file. */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Handles the incoming GET request and streams the import index file template to the client.
   *
   * <p>Multilingual property awareness is enabled for the duration of the generation and
   * restored to its previous value in a {@code finally} block. Any error raised while
   * generating the template is logged and rethrown as an {@link IOException}.</p>
   *
   * @param req the incoming web script request (no parameters are expected)
   * @param res the web script response to which the generated {@code index.txt} template is written
   * @throws IOException if the index template file cannot be generated or written to the response
   * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
   * org.springframework.extensions.webscripts.WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(true);

      this.groupsApi.generateImportIndexFileTemplate(res);
    } catch (Exception e) {
      logger.error("Could not generate index file.", e);
      throw new IOException("Could not generate index file.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }
}
