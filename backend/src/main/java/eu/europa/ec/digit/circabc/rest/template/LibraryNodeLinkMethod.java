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
import io.swagger.model.alfresco.CircabcModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method extension that builds a deep link into the new CIRCABC UI for a Library
 * node (document or folder).
 *
 * <p>Registered as a template method via the {@link NodeRefBaseTemplateProcessorExtension} base
 * class, it is invoked from {@code .ftl} templates with a single argument (a {@code NodeRef} or a
 * wrapped {@code TemplateNode}). Given the target node, it resolves the enclosing Interest Group
 * root and assembles an absolute URL of the form:
 *
 * <pre>{@code <newUiUrl><newUiContext>/group/<groupId>/library/<nodeId>/details}</pre>
 *
 * <p>The new UI base URL and context path are read from {@link CircabcConfig}.
 *
 * @author Pierre Beauregard
 */
public class LibraryNodeLinkMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /** Provides the new UI base URL and context path used to build the link. */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Builds the absolute link to the Library node details page in the new UI.
   *
   * @param nodeRef the reference to the Library node (document or folder) to link to
   * @return the absolute URL pointing to the node's details page in the new UI; the group segment
   *     is left empty when the enclosing Interest Group root cannot be resolved
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    NodeRef groupRef = findInterestGroupRoot(nodeRef);

    String newUiContext = circabcConfig.getNewUiContext();

    return (
      circabcConfig.getNewUiUrl() +
      newUiContext +
      (newUiContext.endsWith("/") ? "group/" : "/group/") +
      (groupRef != null ? groupRef.getId() : "") +
      "/library/" +
      nodeRef.getId() +
      "/details"
    );
  }

  private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
    NodeRef parent = null;

    if (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      parent = getNodeService().getPrimaryParent(nodeRef).getParentRef();
      while (!getNodeService().hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
        parent = getNodeService().getPrimaryParent(parent).getParentRef();
      }
    }

    return parent;
  }
}
