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

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Base class for FreeMarker template method extensions that compute a {@link String} value from a
 * repository {@link NodeRef}.
 *
 * <p>It is exposed to FreeMarker templates as a callable method (via {@link TemplateMethodModelEx})
 * and, when invoked, expects a single argument that wraps either a {@link NodeRef} or a {@link
 * TemplateNode}. The wrapped node reference is resolved and delegated to {@link #getResult(NodeRef)},
 * which concrete subclasses implement to produce the actual template output.
 *
 * <p>Subclasses are typically wired through Spring and receive a {@link NodeService} used to inspect
 * repository nodes.
 */
public abstract class NodeRefBaseTemplateProcessorExtension
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /** Alfresco service used to read node properties and metadata from the repository. */
  private NodeService nodeService;

  /**
   * Returns the Alfresco {@link NodeService} available to subclasses.
   *
   * @return the configured node service
   */
  protected final NodeService getNodeService() {
    return nodeService;
  }

  /**
   * Injects the Alfresco {@link NodeService} (typically set by Spring).
   *
   * @param services the node service to set
   */
  public final void setNodeService(NodeService services) {
    this.nodeService = services;
  }

  /**
   * Entry point invoked by FreeMarker when this extension is called as a template method.
   *
   * <p>The method accepts exactly one argument. That argument must be a wrapped object that is
   * either a {@link NodeRef} or a {@link TemplateNode}; its underlying node reference is extracted
   * and passed to {@link #getResult(NodeRef)}. If the argument count is not one, or the wrapped
   * object is neither type, an empty string is returned.
   *
   * @param args the template method arguments; the single expected element is a wrapped {@link
   *     NodeRef} or {@link TemplateNode}
   * @return the value produced by {@link #getResult(NodeRef)} for the resolved node, or an empty
   *     string when no valid node reference could be resolved
   * @throws TemplateModelException if resolving the result for the node fails
   */
  @Override
  public Object exec(@SuppressWarnings("rawtypes") List args)
    throws TemplateModelException {
    String result = "";

    if (args.size() == 1) {
      // arg 0 must be a wrapped TemplateNode object
      final BeanModel arg0 = (BeanModel) args.get(0);

      NodeRef ref = null;
      if (arg0.getWrappedObject() instanceof NodeRef nodeRef) {
        ref = nodeRef;
      } else if (arg0.getWrappedObject() instanceof TemplateNode templateNode) {
        ref = templateNode.getNodeRef();
      }

      if (ref != null) {
        result = getResult(ref);
      }
    }

    return result;
  }

  /**
   * Computes the template output for the given node reference.
   *
   * <p>Implemented by concrete subclasses to define the specific value returned to the FreeMarker
   * template for the supplied node.
   *
   * @param nodeRef the repository node reference to process
   * @return the computed string value for the node
   * @throws TemplateModelException if the value cannot be computed
   */
  public abstract String getResult(NodeRef nodeRef)
    throws TemplateModelException;
}
