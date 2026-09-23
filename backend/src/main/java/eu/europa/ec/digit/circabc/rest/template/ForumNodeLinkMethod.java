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
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method that builds a deep link into the new CIRCABC UI for a forum-related
 * node (newsgroup or library discussion element).
 *
 * <p>This is not a REST endpoint; it is a template processor extension (registered as a FreeMarker
 * method) that resolves a {@link NodeRef} to an absolute URL pointing at the corresponding page in
 * the new Angular UI. It walks the node hierarchy up to the owning Interest Group root and appends
 * the appropriate path segments depending on whether the node lives under the newsgroup service or
 * the library service, and on its content type (forum, topic or post).
 *
 * @author Pierre Beauregard
 */
public class ForumNodeLinkMethod extends NodeRefBaseTemplateProcessorExtension {

  /**
   * Application configuration providing the base URL and context path of the new CIRCABC UI, used
   * to compose the generated links.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Builds the new-UI URL that points to the given forum-related node.
   *
   * <p>The URL is composed from the configured new-UI base URL and context, the identifier of the
   * enclosing Interest Group, and a service-specific and type-specific path suffix:
   *
   * <ul>
   *   <li>Newsgroup posts link to the enclosing topic within the forum.
   *   <li>Newsgroup topics link to the enclosing forum.
   *   <li>Newsgroup forums link to themselves.
   *   <li>Library posts and topics link to the details page of the enclosing library document.
   * </ul>
   *
   * @param nodeRef the reference of the forum, topic or post node to build a link for
   * @return the absolute URL to the corresponding page in the new UI; nodes that do not match any
   *     of the handled cases yield the group-level library or forum URL without a type-specific
   *     suffix
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    NodeRef groupRef = findInterestGroupRoot(nodeRef);
    String newUiContext = circabcConfig.getNewUiContext();

    String url =
      circabcConfig.getNewUiUrl() +
      newUiContext +
      (newUiContext.endsWith("/") ? "group/" : "/group/") +
      (groupRef != null ? groupRef.getId() : "") +
      (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
        ? "/forum"
        : "/library");

    if (
      getNodeService().getType(nodeRef).equals(ForumModel.TYPE_POST) &&
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ) {
      url =
        url +
        "/topic/" +
        getNodeService().getPrimaryParent(nodeRef).getParentRef().getId();
    } else if (
      getNodeService().getType(nodeRef).equals(ForumModel.TYPE_TOPIC) &&
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ) {
      url =
        url +
        "/" +
        getNodeService().getPrimaryParent(nodeRef).getParentRef().getId();
    } else if (
      getNodeService().getType(nodeRef).equals(ForumModel.TYPE_FORUM) &&
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ) {
      url = url + "/" + nodeRef.getId();
    } else if (
      getNodeService().getType(nodeRef).equals(ForumModel.TYPE_POST) &&
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      NodeRef topicRef = getNodeService()
        .getPrimaryParent(nodeRef)
        .getParentRef();
      NodeRef discussionRef = getNodeService()
        .getPrimaryParent(topicRef)
        .getParentRef();
      url =
        url +
        "/" +
        getNodeService()
          .getPrimaryParent(discussionRef)
          .getParentRef()
          .getId() +
        "/details";
    } else if (
      getNodeService().getType(nodeRef).equals(ForumModel.TYPE_TOPIC) &&
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      NodeRef discussionRef = getNodeService()
        .getPrimaryParent(nodeRef)
        .getParentRef();
      url =
        url +
        "/" +
        getNodeService()
          .getPrimaryParent(discussionRef)
          .getParentRef()
          .getId() +
        "/details";
    }

    return url;
  }

  /**
   * Walks the primary-parent hierarchy from the given node up to the root of its owning Interest
   * Group.
   *
   * @param nodeRef the newsgroup or library node to start the upward traversal from
   * @return the {@link NodeRef} of the ancestor carrying the Interest Group root aspect, or {@code
   *     null} if the given node is neither a newsgroup nor a library node
   */
  private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
    NodeRef parent = null;

    if (
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP) ||
      getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      parent = getNodeService().getPrimaryParent(nodeRef).getParentRef();
      while (!getNodeService().hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
        parent = getNodeService().getPrimaryParent(parent).getParentRef();
      }
    }

    return parent;
  }
}
