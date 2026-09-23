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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template method extension that builds the direct download URL for a repository
 * content node.
 *
 * <p>It is registered as a FreeMarker method (via its {@link NodeRefBaseTemplateProcessorExtension}
 * base class, which implements {@code TemplateMethodModelEx}) and invoked from {@code .ftl}
 * templates with a single {@link NodeRef} (or {@code TemplateNode}) argument. The returned URL
 * points to the Alfresco download servlet path
 * {@code /d/d/workspace/SpacesStore/<node-id>/<file-name>}, allowing the rendered response to link
 * straight to the binary content of the node.
 *
 * @author Yanick Pignot
 */
public class DirectDowloadUrlMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Builds the direct download URL for the given content node.
   *
   * <p>The URL is composed of the fixed Alfresco download servlet prefix
   * {@code /d/d/workspace/SpacesStore/}, the node identifier and the node's {@code cm:name}
   * property (the file name), so the content can be downloaded directly.
   *
   * @param nodeRef the reference to the content node whose download URL is requested
   * @return the direct download URL for the node's content
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    return (
      "/d/d/workspace/SpacesStore/" +
      nodeRef.getId() +
      "/" +
      super
        .getNodeService()
        .getProperty(nodeRef, ContentModel.PROP_NAME)
        .toString()
    );
  }
}
