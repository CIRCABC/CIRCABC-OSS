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

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method that exposes the labels of the dynamic properties associated with a
 * node.
 *
 * <p>Registered as an Alfresco template processor extension, this class can be invoked from within
 * {@code .ftl} templates as a custom method. Given a single node argument, it resolves the {@link
 * DynamicProperty} instances attached to that node via the {@link DynamicPropertyService} and
 * returns the default-locale label of each one.
 *
 * <p>Typical template usage: {@code ${myMethod(node)}}, where {@code node} is either a wrapped
 * {@link NodeRef} or a {@link TemplateNode}.
 */
public class DynamicPropertyDescriptionMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /** Service used to look up the dynamic properties defined for a given node. */
  @Autowired
  private DynamicPropertyService dynamicPropertyService;

  /**
   * Executes the template method.
   *
   * <p>Expects exactly one argument that wraps either a {@link NodeRef} or a {@link TemplateNode}.
   * When a valid node reference can be resolved, the method returns the default-value labels of the
   * dynamic properties attached to that node; otherwise an empty list is returned.
   *
   * @param args the template method arguments; a single element wrapping a {@link NodeRef} or
   *     {@link TemplateNode} is expected
   * @return a list of dynamic property labels for the resolved node, or an empty list if the
   *     argument count is not exactly one or the node reference cannot be resolved
   * @throws TemplateModelException if the arguments cannot be processed by the template engine
   */
  public Object exec(@SuppressWarnings("rawtypes") List args)
    throws TemplateModelException {
    List<String> result = Collections.emptyList();
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
   * Collects the default-value labels of the dynamic properties bound to the given node.
   *
   * @param ref the reference of the node whose dynamic properties are queried
   * @return a list containing the default-value label of each dynamic property associated with the
   *     node; empty if the node has no dynamic properties
   */
  private List<String> getResult(NodeRef ref) {
    List<String> result = new ArrayList<>();
    final List<DynamicProperty> dynamicProperties =
      dynamicPropertyService.getDynamicProperties(ref);
    for (DynamicProperty dynamicProperty : dynamicProperties) {
      result.add(dynamicProperty.getLabel().getDefaultValue());
    }
    return result;
  }
}
