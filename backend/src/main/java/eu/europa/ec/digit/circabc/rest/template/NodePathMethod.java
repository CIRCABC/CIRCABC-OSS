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
import io.swagger.exception.PathNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template method extension that resolves the human-readable path of a repository node.
 *
 * <p>Registered as a template method (via {@link NodeRefBaseTemplateProcessorExtension}), it can be
 * invoked from {@code .ftl} response templates to obtain the {@link SimplePath} representation of a
 * given node. The base class handles unwrapping the FreeMarker argument into a {@link NodeRef} and
 * delegates the actual path computation to {@link #getResult(NodeRef)}.
 */
public class NodePathMethod extends NodeRefBaseTemplateProcessorExtension {

  /**
   * Computes the simple, human-readable path of the supplied node.
   *
   * @param nodeRef the reference of the node whose path is requested
   * @return the string representation of the node's {@link SimplePath}
   * @throws TemplateModelException if the node's path cannot be resolved (for example when the node
   *     no longer exists), wrapping the underlying {@link PathNotFoundException}
   */
  @Override
  public String getResult(NodeRef nodeRef) throws TemplateModelException {
    try {
      SimplePath path = new SimplePath(getNodeService(), nodeRef);
      return path.toString();
    } catch (PathNotFoundException e) {
      throw new TemplateModelException(e);
    }
  }
}
