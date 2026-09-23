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

import io.swagger.exception.PathNotFoundException;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import java.util.List;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

/**
 * Represents a human-readable, CIRCABC-style hierarchical path to a repository node, e.g.
 * {@code /Circabc/Category/My Interest group/Library/My document.pdf}.
 *
 * <p>An instance is built for a given {@link NodeRef} by walking up the Alfresco node hierarchy
 * (typically following primary parent associations) until the CIRCABC root is reached. Each level
 * of the path is modelled as a {@code SimplePath} node holding the current node reference, its
 * display name and a link to its {@link #getParent() parent} segment. The full path can be rendered
 * via {@link #toString()}.
 *
 * <p>This class is used by FreeMarker webscript templates to expose readable paths of nodes.
 *
 * @author Yanick Pignot
 */
public class SimplePath {

  /** Character used to separate the individual segments when rendering the path as a string. */
  public static final char PATH_SEPARATOR = '/';

  /** The repository node this path segment refers to. */
  private NodeRef nodeRef;

  /** The display name of this path segment. */
  private String name;

  /** The parent path segment, or {@code null} if this segment is the CIRCABC root. */
  private SimplePath parent;

  /**
   * Builds the path segment for the given node, recursively resolving its parent segments.
   *
   * <p>The parent segment is resolved as follows:
   *
   * <ul>
   *   <li>if {@code forcedParent} is provided, it is used as the parent;
   *   <li>if the node carries the CIRCABC root aspect, it has no parent;
   *   <li>if the node is a multilingual container, the parent of its first translation is used;
   *   <li>otherwise the node's primary parent is used.
   * </ul>
   *
   * The segment name is derived from the node title for events, is the literal {@code "Directory"}
   * for directory service nodes, and is the node name otherwise.
   *
   * @param nodeService the Alfresco node service used to read node types, aspects, properties and
   *     associations; must not be {@code null}
   * @param forcedParent an optional node reference to use as the parent of {@code ref}, bypassing
   *     the automatic parent resolution; may be {@code null}
   * @param ref the node reference for which this path segment is built; must not be {@code null}
   * @throws PathNotFoundException if the node has no resolvable parent and is therefore not managed
   *     by CIRCABC
   */
  public SimplePath(
    final NodeService nodeService,
    final NodeRef forcedParent,
    final NodeRef ref
  ) throws PathNotFoundException {
    ParameterCheck.mandatory("NodeService", nodeService);
    ParameterCheck.mandatory("Node Reference", ref);

    final QName type = nodeService.getType(ref);
    final Set<QName> aspects = nodeService.getAspects(ref);

    ChildAssociationRef primaryParent = null;
    NodeRef primaryParentRef = null;
    this.nodeRef = ref;

    if (forcedParent != null) {
      this.parent = new SimplePath(nodeService, forcedParent);
    } else if (aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)) {
      this.parent = null;
    } else if (type.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
      // Get first translation
      final List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(
        ref,
        ContentModel.ASSOC_MULTILINGUAL_CHILD,
        RegexQNamePattern.MATCH_ALL
      );

      if (assocRefs.isEmpty()) {
        this.parent = null;
      } else {
        final NodeRef translationRef = assocRefs.get(0).getChildRef();
        final NodeRef spaceRef = nodeService
          .getPrimaryParent(translationRef)
          .getParentRef();
        this.parent = new SimplePath(nodeService, spaceRef);
      }
    } else if (
      (primaryParent = nodeService.getPrimaryParent(ref)) != null &&
      (primaryParentRef = primaryParent.getParentRef()) != null
    ) {
      parent = new SimplePath(nodeService, primaryParentRef);
    } else {
      throw new PathNotFoundException("The node is not managed by Circabc");
    }

    if (EventModel.TYPE_EVENT.equals(type)) {
      name = (String) nodeService.getProperty(ref, ContentModel.PROP_TITLE);
    } else if (CircabcModel.TYPE_DIRECTORY_SERVICE.equals(type)) {
      name = "Directory";
    } else {
      name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    }
  }

  /**
   * Builds the path segment for the given node, resolving its parent automatically.
   *
   * @param nodeService the Alfresco node service used to resolve the node hierarchy; must not be
   *     {@code null}
   * @param ref the node reference for which this path segment is built; must not be {@code null}
   * @throws PathNotFoundException if the node has no resolvable parent and is therefore not managed
   *     by CIRCABC
   */
  public SimplePath(final NodeService nodeService, final NodeRef ref)
    throws PathNotFoundException {
    this(nodeService, null, ref);
  }

  /**
   * Returns the repository node this path segment refers to.
   *
   * @return the node reference of this segment
   */
  public NodeRef getNodeRef() {
    return this.nodeRef;
  }

  /**
   * Returns the parent path segment.
   *
   * @return the parent segment, or {@code null} if this segment is the CIRCABC root
   */
  public SimplePath getParent() {
    return this.parent;
  }

  /**
   * Returns the display name of this path segment.
   *
   * @return the segment name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Renders the full path from the CIRCABC root down to this segment, with each segment prefixed by
   * the {@link #PATH_SEPARATOR}.
   *
   * @return the string representation of the complete path
   */
  @Override
  public String toString() {
    return (
      ((parent != null) ? parent.toString() : "") + PATH_SEPARATOR + getName()
    );
  }
}
