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

import freemarker.template.TemplateModelException;
import io.swagger.util.PathUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * FreeMarker template method extension that resolves the human-readable Library path for a given
 * repository node.
 *
 * <p>This extension is exposed to {@code .ftl} response templates (registered as a Spring bean and
 * wired via the {@link NodeRefBaseTemplateProcessorExtension#getNodeService() NodeService}). When
 * invoked from a template with a single {@code NodeRef} or {@code TemplateNode} argument, it looks
 * up the node's repository {@link Path} and converts it into the CIRCABC Library path
 * representation used in JSON responses.
 *
 * <p>The base class ({@link NodeRefBaseTemplateProcessorExtension}) handles argument unwrapping and
 * delegates the actual path computation to {@link #getResult(NodeRef)} implemented here.
 */
public class LibraryNodePathMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Computes the Library path string for the supplied node.
   *
   * <p>Retrieves the node's repository {@link Path} via the configured {@code NodeService} and
   * converts it into the CIRCABC Library path (with a leading slash) using
   * {@link PathUtils#getLibraryPath(Path, boolean)}.
   *
   * @param nodeRef the reference of the node whose Library path is requested
   * @return the Library path of the node, including a leading slash
   * @throws TemplateModelException if the path cannot be resolved while rendering the template
   */
  @Override
  public String getResult(NodeRef nodeRef) throws TemplateModelException {
    Path path = getNodeService().getPath(nodeRef);
    boolean includeFirstSlash = true;
    return PathUtils.getLibraryPath(path, includeFirstSlash);
  }
}
