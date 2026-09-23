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
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template method extension that resolves the display title of a repository node.
 *
 * <p>Registered as a template method (via {@link NodeRefBaseTemplateProcessorExtension}), it is
 * invoked from {@code .ftl} templates with a single argument that wraps either a {@link NodeRef} or
 * a {@code TemplateNode}. It reads the node's {@link ContentModel#PROP_TITLE cm:title} property and
 * returns it as plain text, unwrapping multilingual ({@link MLText}) values to their default-locale
 * string. This is typically used when building links or labels for the new UI.
 *
 * @author Pierre Beauregard
 */
public class MLDisplayTitleMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Returns the display title of the given node.
   *
   * <p>Reads the {@link ContentModel#PROP_TITLE cm:title} property of the node. If the property is a
   * plain {@link String} it is returned as-is; if it is an {@link MLText} value its default-locale
   * value is returned. When the node is {@code null} or has no title, an empty string is returned.
   *
   * @param nodeRef the reference to the repository node whose title is requested; may be {@code
   *     null}
   * @return the node's title as plain text, or an empty string if the node is {@code null} or has no
   *     title
   * @throws TemplateModelException if the title cannot be resolved during template processing
   */
  @Override
  public String getResult(NodeRef nodeRef) throws TemplateModelException {
    String result = "";

    if (nodeRef != null) {
      Object titleObj = getNodeService().getProperty(
        nodeRef,
        ContentModel.PROP_TITLE
      );

      if (titleObj instanceof String str) {
        result = str;
      } else if (titleObj instanceof MLText mlText) {
        result = mlText.getDefaultValue();
      }
    }

    return result;
  }
}
