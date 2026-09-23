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
 * FreeMarker template method that builds the direct download URL for a repository content node.
 *
 * <p>Unlike {@code DirectAccessUrlMethod}, this implementation does not rely on the JSF
 * {@code FacesContext}, so it can be used from contexts where no faces context is available (for
 * example scheduled/cron jobs). The URL is assembled purely from the node's identifier and its
 * {@code cm:name} property, following the Alfresco content download pattern
 * {@code /d/d/workspace/SpacesStore/<nodeId>/<fileName>}.
 *
 * <p>As a subclass of {@link NodeRefBaseTemplateProcessorExtension}, it is invoked from FreeMarker
 * with a single argument wrapping either a {@link NodeRef} or a {@code TemplateNode}; the base class
 * resolves that argument to a {@link NodeRef} and delegates to {@link #getResult(NodeRef)}.
 *
 * @author Pierre Beauregard
 */
public class SimpleDirectDowloadUrlMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Builds the direct download URL for the given content node.
   *
   * @param nodeRef the reference of the content node whose download URL is requested
   * @return the direct download URL in the form
   *     {@code /d/d/workspace/SpacesStore/<nodeId>/<fileName>}, where {@code fileName} is the
   *     node's {@code cm:name} property value
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
