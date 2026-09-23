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
 * FreeMarker template method extension that resolves the display value of a node's {@code
 * cm:description} property for rendering in the new UI.
 *
 * <p>This extension is exposed to {@code .ftl} response templates through the {@link
 * NodeRefBaseTemplateProcessorExtension} base class, which unwraps the template argument into a
 * {@link NodeRef} before delegating to {@link #getResult(NodeRef)}. The description property may be
 * stored either as a plain {@link String} or as a multilingual {@link MLText} value; in the latter
 * case the default-locale value is returned so a single, language-neutral description is displayed.
 *
 * @author Pierre Beauregard
 */
public class MLDisplayDescriptionMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Returns the display description for the given node.
   *
   * <p>Reads the {@link ContentModel#PROP_DESCRIPTION} property of the node. When the value is a
   * plain {@link String} it is returned as-is; when it is an {@link MLText} its default-locale
   * value is returned. If the node is {@code null} or has no description, an empty string is
   * returned.
   *
   * @param nodeRef the reference of the node whose description should be resolved; may be {@code
   *     null}
   * @return the resolved description, or an empty string when none is available
   * @throws TemplateModelException if the value cannot be resolved during template processing
   */
  @Override
  public String getResult(NodeRef nodeRef) throws TemplateModelException {
    String result = "";

    if (nodeRef != null) {
      Object descObj = getNodeService().getProperty(
        nodeRef,
        ContentModel.PROP_DESCRIPTION
      );

      if (descObj instanceof String str) {
        result = str;
      } else if (descObj instanceof MLText mlText) {
        result = mlText.getDefaultValue();
      }
    }

    return result;
  }
}
