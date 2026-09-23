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
 * FreeMarker template method extension that builds an absolute link into the new CIRCABC UI
 * pointing to the "group requests" page of a given category.
 *
 * <p>It is invoked from {@code .ftl} response templates and, given a category {@link NodeRef},
 * returns a URL of the form {@code <newUiUrl><newUiContext>/category/<nodeId>/group-requests}. The
 * base URL and context path are resolved from {@link CircabcConfig}. When no node reference is
 * supplied the node identifier segment is left empty.
 *
 * <p>The {@code exec} entry point and node-reference unwrapping are inherited from {@link
 * NodeRefBaseTemplateProcessorExtension}; this class only supplies the concrete link generation
 * through {@link #getResult(NodeRef)}.
 *
 * @author Pierre Beauregard
 */
public class CategoryGroupRequesLinkMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Provides access to the new UI base URL and context path used to compose the generated link.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Builds the absolute link to the group-requests page of the given category in the new UI.
   *
   * @param nodeRef the category node to link to; when {@code null} the node identifier segment of
   *     the URL is rendered as an empty string
   * @return the fully composed URL, ending with {@code /group-requests}
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    String newUiContext = circabcConfig.getNewUiContext();

    return (
      circabcConfig.getNewUiUrl() +
      newUiContext +
      (newUiContext.endsWith("/") ? "category/" : "/category/") +
      (nodeRef != null ? nodeRef.getId() : "") +
      "/group-requests"
    );
  }
}
