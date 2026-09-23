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
import io.swagger.model.alfresco.EventModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method extension that builds a deep link to the "agenda details" page of the
 * new CIRCABC UI for a given event/agenda node.
 *
 * <p>It is invoked from {@code .ftl} response templates (via the inherited {@code exec} contract of
 * {@link NodeRefBaseTemplateProcessorExtension}) to turn a node reference into a fully qualified URL
 * of the form:
 *
 * <pre>{@code <newUiUrl><newUiContext>/group/<interestGroupId>/agenda/<nodeId>/details}</pre>
 *
 * <p>The base URL and context path are resolved from {@link CircabcConfig}, and the owning Interest
 * Group is discovered by walking up the primary parent hierarchy of the node.
 *
 * @author Pierre Beauregard
 */
public class AgendaNodeLinkMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /** Provides the base URL and context path of the new UI used to compose the generated link. */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Builds the new-UI "agenda details" URL for the supplied node.
   *
   * @param nodeRef the reference of the event/agenda node to link to
   * @return the fully qualified agenda details URL; the Interest Group segment is left empty when
   *     the owning Interest Group root cannot be resolved
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
      "/agenda/" +
      nodeRef.getId() +
      "/details"
    );
  }

  /**
   * Walks up the primary parent hierarchy of an event node to locate its owning Interest Group
   * root.
   *
   * <p>The traversal only runs when the node carries the event aspect or is of the event type;
   * otherwise no lookup is performed.
   *
   * @param nodeRef the event node to start the upward search from
   * @return the {@link NodeRef} of the enclosing Interest Group root (node bearing the
   *     {@code IGROOT} aspect), or {@code null} if {@code nodeRef} is not an event
   */
  private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
    NodeRef parent = null;

    if (
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_EVENT) ||
      getNodeService().getType(nodeRef).equals(EventModel.TYPE_EVENT)
    ) {
      parent = getNodeService().getPrimaryParent(nodeRef).getParentRef();
      while (!getNodeService().hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
        parent = getNodeService().getPrimaryParent(parent).getParentRef();
      }
    }

    return parent;
  }
}
