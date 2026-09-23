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

import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * FreeMarker template method that returns the display label of a repository node.
 *
 * <p>It resolves the node's {@code cm:title} property and returns it when present; otherwise it
 * falls back to the node's {@code cm:name}. When the title is multilingual ({@link MLText}), its
 * default-locale value is used, and an empty title is treated as absent.
 *
 * <p>As a subclass of {@link NodeRefBaseTemplateProcessorExtension}, this method is invoked from
 * FreeMarker templates with a single argument wrapping a {@link NodeRef} (or {@code TemplateNode});
 * the resolved node reference is passed to {@link #getResult(NodeRef)}.
 *
 * @author Yanick Pignot
 */
public class TitleOrNameMethod extends NodeRefBaseTemplateProcessorExtension {

  /**
   * Computes the label for the given node: its {@code cm:title} if set, otherwise its {@code
   * cm:name}.
   *
   * <p>The {@code cm:title} property may be a plain {@link String} or an {@link MLText}. For {@link
   * MLText} the default-locale value is used. In both cases a blank/empty title is ignored and the
   * {@code cm:name} is returned instead.
   *
   * @param nodeRef the repository node reference whose display label is required
   * @return the node's title when available and non-empty, otherwise its name
   */
  @Override
  public String getResult(final NodeRef nodeRef) {
    final Map<QName, Serializable> props = getNodeService().getProperties(
      nodeRef
    );
    final Serializable titleObj = props.get(ContentModel.PROP_TITLE);
    String result = props.get(ContentModel.PROP_NAME).toString();

    if (titleObj instanceof MLText title) {
      if (!"".equals(title.getDefaultValue())) {
        result = title.getDefaultValue();
      }
    } else if (titleObj instanceof String str && !"".equals(str)) {
      result = str;
    }

    return result;
  }
}
