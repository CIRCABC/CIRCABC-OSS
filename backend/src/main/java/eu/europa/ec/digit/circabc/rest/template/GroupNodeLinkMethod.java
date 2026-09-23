/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.template;

import io.swagger.config.CircabcConfig;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method extension that builds a deep link to an Interest Group in the new
 * CIRCABC UI.
 *
 * <p>Registered as a template method (via its {@link NodeRefBaseTemplateProcessorExtension} parent,
 * which implements {@code TemplateMethodModelEx}), this extension can be invoked from FreeMarker
 * templates by passing a group node (a {@code NodeRef} or {@code TemplateNode}). The parent class
 * resolves the argument to a {@link NodeRef} and delegates to {@link #getResult(NodeRef)} to render
 * the final URL.
 *
 * <p>The resulting URL is composed from the new UI base URL and context configured in {@link
 * CircabcConfig}, followed by the {@code group/} path segment and the node identifier, ensuring a
 * single slash separates the context from the path segment.
 *
 * @author Pierre Beauregard
 */
public class GroupNodeLinkMethod extends NodeRefBaseTemplateProcessorExtension {

  /** Provides the new UI base URL and context path used to assemble group links. */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Builds the absolute link to the given group node in the new CIRCABC UI.
   *
   * <p>The URL is formed by concatenating the configured new UI URL and context, a {@code group/}
   * path segment (normalising the slash between the context and the segment), and the node's
   * identifier.
   *
   * @param nodeRef the reference to the Interest Group node the link should point to
   * @return the absolute URL to the group in the new UI
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    String newUiContext = circabcConfig.getNewUiContext();

    return (
      circabcConfig.getNewUiUrl() +
      newUiContext +
      (newUiContext.endsWith("/") ? "group/" : "/group/") +
      nodeRef.getId()
    );
  }
}
